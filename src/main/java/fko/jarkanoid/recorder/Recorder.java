/*
 * MIT License
 *
 * Copyright (c) 2018 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package fko.jarkanoid.recorder;

import fko.jarkanoid.Jarkanoid;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Recorder
 *
 * <p>Records screenshot from node of JavaFX in ficed intervalls
 *
 * @author Frank Kopp
 */
public class Recorder implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(Recorder.class);

  public static final String SCREENSHOTS_FOLDER = "screenshots/";

  private final ScheduledThreadPoolExecutor genExecutor = new ScheduledThreadPoolExecutor(4);

  private final ThreadPoolExecutor saveExecutor =
      new ThreadPoolExecutor(
          8,
          8,
          0L,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<Runnable>());

  private final BlockingQueue<BufferedImage> queue = new LinkedBlockingQueue<BufferedImage>();

  private static final Object LOCK = new Object();

  private final AtomicLong genCounter = new AtomicLong(0);
  private final AtomicLong saveCounter = new AtomicLong(0);

  private final AtomicBoolean isStopped = new AtomicBoolean(false);

  private Thread recorderThread = null;

  private Node recordedNode;
  private int period;
  private int width;
  private int height;

  private Robot robot = null;

  public Recorder() {
    try {
      robot = new Robot();
    } catch (AWTException e) {
      LOG.error("" + e);
    }
  }

  /** @param period the intervall of capturing in ms */
  public void start(int period) {
    if (recorderThread != null) throw new IllegalStateException("Thread excists. Not stopped yet.");
    if (robot == null) throw new RuntimeException("Robot is not initialized.");

    recorderThread = new Thread(this, "Recorder Thread");
    recorderThread.setDaemon(false);

    this.period = period;
    this.width = width;
    this.height = height;

    recorderThread.start();
  }

  public void stop() {
    if (recorderThread == null) return;

    LOG.debug("Recorder shutting down...");

    isStopped.set(true);

    try {
      LOG.debug("Shut down - await termination.");
      recorderThread.join();
      genExecutor.awaitTermination(2, TimeUnit.SECONDS);
      saveExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      LOG.warn("While stopping recording", (e));
    }

    recorderThread = null;

    LOG.info("Recording stopped");
  }

  public void run() {

    LOG.info(
        "Recording started - storing screenshots in {} every {} ms", SCREENSHOTS_FOLDER, period);

    genExecutor.scheduleAtFixedRate(
        () -> takeScreenShotAndQueue(), 0, period, TimeUnit.MILLISECONDS);

    while (!(isStopped.get() && queue.isEmpty())) {
      try {
        final BufferedImage image = queue.take();
        saveExecutor.execute(() -> saveImage(image));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (isStopped.get() && !genExecutor.isShutdown()) {
        LOG.info("Shutdown taking snapshots...");
        genExecutor.shutdown();
        LOG.info("Saving queued snapshots to disk...");
      }
    }
    LOG.debug("Shutdown saving snapshots...");
    saveExecutor.shutdown();
  }

  private void takeScreenShotAndQueue() {
    long startTime = System.nanoTime();
    genCounter.getAndIncrement();

    final Stage primaryStage = Jarkanoid.getPrimaryStage();

    Rectangle stageRect =
        new Rectangle(
            (int) (primaryStage.getX()),
            (int) primaryStage.getY(),
            (int) primaryStage.getWidth(),
            (int) primaryStage.getHeight());

    final BufferedImage screenshotBI = robot.createScreenCapture(stageRect);

    // add it to our buffer
    queue.add(screenshotBI);

    long endTime = System.nanoTime();
    LOG.debug(
        "CAPTURE: Screenshot #{} queued (took {} ms - queue size:{})",
        genCounter.toString(),
        (endTime - startTime) / 1e6f,
        saveExecutor.getQueue().size());
  }

  private void saveImage(final BufferedImage image) {

    long startTime = System.nanoTime();

    saveCounter.getAndIncrement();

    String format = "jpg";

    // write the encoded image to disk

    // DEPENDENCY: PnGEncoder
    //    try {
    //      final FileOutputStream fout =
    //          new FileOutputStream(SCREENSHOTS_FOLDER + startTime + "_Screenshot.png");
    //        encoder.encode(image, fout);
    //    } catch (IOException e) {
    //      e.printStackTrace();
    //    }

    File file = new File("screenshots/" + startTime + "_Screenshot." + format);
    try {
      ImageIO.write(image, format, file);
    } catch (Exception s) {
      s.printStackTrace();
    }

    long endTime = System.nanoTime();

    LOG.debug(
        "SAVING: Screenshot (#{}) saved (took {} ms queue size: {})",
        saveCounter.toString(),
        (endTime - startTime) / 1e6f,
        saveExecutor.getQueue().size());
  }

  public boolean isRunning() {
    return recorderThread != null;
  }
}
