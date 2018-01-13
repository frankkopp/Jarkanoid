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
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/** */
public class Recorder extends Thread {

  private final Scene recordedScene;

  private final ScheduledThreadPoolExecutor genExecutor = new ScheduledThreadPoolExecutor(4);
  private final ThreadPoolExecutor saveExecutor =
      new ThreadPoolExecutor(
          4,
          8,
          50L,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<Runnable>()); // new ScheduledThreadPoolExecutor(1);

  private final BlockingQueue<WritableImage> queue = new LinkedBlockingQueue<WritableImage>();

  private static final Object LOCK = new Object();

  private AtomicLong genCounter = new AtomicLong(0);
  private AtomicLong saveCounter = new AtomicLong(0);

  private AtomicBoolean isStopped = new AtomicBoolean(false);

  public Recorder(Scene scene) {
    super("Screen Recorder Thread");
    this.recordedScene = scene;
    this.setDaemon(false);
  }

  public void run() {

    System.out.println("Recorder started!");

    genExecutor.scheduleAtFixedRate(() -> takeScreenShotAndQueue(), 0, 100, TimeUnit.MILLISECONDS);

    while (!(isStopped.get() && queue.isEmpty())) {
      try {
        System.out.printf(
            "Taking screenshot from queue for saving (queued: %d) ... %n", queue.size());
        final WritableImage image = queue.take();
        //System.out.printf("Saving screenshot  (queued: %d) ... %n", queue.size());
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

    System.out.printf("Saving screenshot #%s (queued: %d) ... %n", saveCounter.toString(), queue.size());

    File file = new File("screenshots/" + startTime + "_Screenshot.png");
    try {
      final BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
      //      synchronized (LOCK) {
      ImageIO.setUseCache(false);
      ImageIO.write(bufferedImage, "png", file);
      //      }
    } catch (Exception s) {
      s.printStackTrace();
    }
    long endTime = System.nanoTime();

    System.out.printf(
        "Screenshot saved (#%s) (took %,f ms)%n",
        saveCounter.toString(), (endTime - startTime) / 1e6f);
  }

  private void takeScreenShotAndQueue() {
    long startTime = System.nanoTime();
    genCounter.getAndIncrement();

    WritableImage screenshot = new WritableImage(812, 817);
    Platform.runLater(() -> recordedScene.snapshot(screenshot));
    while (screenshot.getProgress() < 1) {
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    queue.add(screenshot);

    long endTime = System.nanoTime();
    System.out.printf(
        "Screenshot #%s queued - took %,f ms (queued: %d remaining: %d)%n",
        genCounter.toString(),
        (endTime - startTime) / 1e6f,
        queue.size(),
        queue.remainingCapacity());
  }

  public void shutdown() {
    System.out.println("SHUTTING DOWN");
    isStopped.set(true);
    try {
      System.out.printf("Shut down - await termination.%n", queue.size());
      this.join();
      genExecutor.awaitTermination(2, TimeUnit.SECONDS);
      saveExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
