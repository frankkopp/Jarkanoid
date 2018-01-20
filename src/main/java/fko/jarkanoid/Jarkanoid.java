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
package fko.jarkanoid;

import fko.jarkanoid.controller.MainController;
import fko.jarkanoid.model.GameModel;
import fko.jarkanoid.recorder.Recorder;
import fko.jarkanoid.view.MainView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Arkanoid Clone in Java
 *
 *  02.01.2018
 * @author Frank Kopp
 */
public class Jarkanoid extends Application {

  private static final Logger LOG = LoggerFactory.getLogger(Jarkanoid.class);

  // VERSION
  @SuppressWarnings("unused")
  public static final String VERSION = "prototype0.6";

  private static Stage pStage;
  private Scene scene;

  private static Recorder recorder = new Recorder();

  /**
   * Main
   */
  public static void main(String[] args) {

    // setting up logging
    LOG.info("Launching Application starting with args: {}", (Object) args);

    launch(args);
  }

  /**
   * @see javafx.application.Application#start(javafx.stage.Stage)
   */
  @Override
  public void start(Stage primaryStage) throws Exception {

    LOG.info("Starting JavaFX Application");

    pStage = primaryStage;

    GameModel model = new GameModel();
    MainController controller = new MainController(model);
    MainView view = new MainView(model, controller);

    scene = new Scene(view.asParent());

    primaryStage.setTitle("Jarkanoid by Frank Kopp");

    controller.bindModelToView(view);

    primaryStage.setScene(scene);
    primaryStage.centerOnScreen();
    primaryStage.setResizable(false);

    // closeAction
    primaryStage.setOnCloseRequest(event -> exit());

    primaryStage.show();

    LOG.info("JavaFX Application started");
  }

  /**
   * @see javafx.application.Application#init()
   */
  @Override
  public void init() throws Exception {
    super.init();
    String fontName = "/fonts/AstronomicMono.otf";
    final URL urlResource = Jarkanoid.class.getResource(fontName);
    if (urlResource == null) {
      LOG.warn("Font could not be found: {}", fontName );
    } else {
      final String url = urlResource.toExternalForm();
      if (Font.loadFont(url, 40) == null) {
        LOG.warn("Font could not be loaded: {}", url );
      }
    }
  }

  /**
   * Clean up and exit the application
   */
  public static void exit() {
    LOG.info("Application shutting down...");
    recorder.stop();
    LOG.info("Application shut down");
    exit(0);
  }

  /**
   * Clean up and exit the application
   */
  private static void exit(int returnCode) {
    Platform.exit();
    System.exit(returnCode);
  }

  public static Stage getPrimaryStage() {
    return pStage;
  }

  public static Recorder getRecorder() {
    return recorder;
  }

}
