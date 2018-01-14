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

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.objectplanet.image.PngEncoder;
import javafx.scene.paint.Color;

/**
 * Recorder
 *
 * <p>Records screenshot from node of JavaFX in ficed intervalls</p>
 *
 * FIXME: only producing black images
 */
public class Recorder implements Runnable {

  private final ScheduledThreadPoolExecutor genExecutor = new ScheduledThreadPoolExecutor(4);

  private final ThreadPoolExecutor saveExecutor =
      new ThreadPoolExecutor(
          1,
          1,
          0L,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<Runnable>()); // new ScheduledThreadPoolExecutor(1);

  private final BlockingQueue<WritableImage> queue = new LinkedBlockingQueue<WritableImage>();

  private static final Object LOCK = new Object();

  private AtomicLong genCounter = new AtomicLong(0);
  private AtomicLong saveCounter = new AtomicLong(0);

  private AtomicBoolean isStopped = new AtomicBoolean(false);

  private PngEncoder encoder = new PngEncoder();

  private Thread recorderThread = null;

  private Node recordedNode;
  private int period;
  private int width;
  private int height;

  public Recorder() {}

  /**
   * @param node the node to be recorded
   * @param period the intervall of capturing in ms
   * @param width of the recording area
   * @param height of the recording area
   */
  public void start(Node node, int period, int width, int height) {
    if (node == null) throw new IllegalArgumentException("May not be null");
    if (recorderThread != null) throw new IllegalStateException("Thread excists. Not stopped yet.");

    recorderThread = new Thread(this, "Recorder Thread");
    recorderThread.setDaemon(false);

    this.recordedNode = node;
    this.period = period;
    this.width = width;
    this.height = height;

    recorderThread.start();
  }

  public void stop() {
    if (recorderThread == null) return;

    System.out.println("SHUTTING DOWN");

    isStopped.set(true);

    try {
      System.out.println("Shut down - await termination.");
      recorderThread.join();
      genExecutor.awaitTermination(2, TimeUnit.SECONDS);
      saveExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    recorderThread = null;
  }

  public void run() {

    System.out.println("Recorder started!");

    genExecutor.scheduleAtFixedRate(
        () -> takeScreenShotAndQueue(), 0, period, TimeUnit.MILLISECONDS);

    while (!(isStopped.get() && queue.isEmpty())) {
      try {
        final WritableImage image = queue.take();
        saveExecutor.execute(() -> saveImage(image));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (isStopped.get() && !genExecutor.isShutdown()) {
        System.out.println("Shutdown taking snapshots...");
        genExecutor.shutdown();
      }
    }
    System.out.println("Shutdown saving snapshots...");
    saveExecutor.shutdown();
  }

  private void saveImage(final WritableImage image) {

    long startTime = System.nanoTime();

    saveCounter.getAndIncrement();

    // write the encoded image to disk
    final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
    try {
      final FileOutputStream fout =
          new FileOutputStream("screenshots/" + startTime + "_Screenshot.png");
      synchronized (LOCK) {
        encoder.encode(bufferedImage, fout);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    //    File file = new File("screenshots/" + startTime + "_Screenshot.png");
    //    try {
    //      final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
    //      //      synchronized (LOCK) {
    //      ImageIO.setUseCache(false);
    //      ImageIO.write(bufferedImage, "png", file);
    //      //      }
    //    } catch (Exception s) {
    //      s.printStackTrace();
    //    }
    long endTime = System.nanoTime();

    System.out.printf(
        "SAVING: Screenshot (#%s) saved (took %,f ms)%n",
        saveCounter.toString(), (endTime - startTime) / 1e6f);
  }

  private void takeScreenShotAndQueue() {
    long startTime = System.nanoTime();
    genCounter.getAndIncrement();

    // create new empty image
    final WritableImage screenshot = new WritableImage(width, height);

    SnapshotParameters snapshotParams = new SnapshotParameters();
    snapshotParams.setFill(Color.TRANSPARENT);

    // tell JavaFX Thread to take a snapshot aand store it to the created image
    Platform.runLater(() -> recordedNode.snapshot(snapshotParams, screenshot));

    // wait until the image is done
    while (screenshot.getProgress() < 1) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    // add it to our buffer
    queue.add(screenshot);

    long endTime = System.nanoTime();
    System.out.printf(
        "CAPTURE: Screenshot #%s queued (took %,f ms)%n",
        genCounter.toString(),
        (endTime - startTime) / 1e6f,
        queue.size(),
        queue.remainingCapacity());
  }

  public boolean isRunning() {
    return recorderThread != null;
  }
}
