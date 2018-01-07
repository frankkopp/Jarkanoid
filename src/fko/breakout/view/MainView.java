/**
MIT License

Copyright (c) 2018 Frank Kopp

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package fko.breakout.view;

import java.io.IOException;

import fko.breakout.controller.MainController;
import fko.breakout.model.BreakOutGame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.StrokeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * MainView
 * <p>
 * Loads the main view from the MainView.fxml resource file.<br>
 * Also add additional view elements e.g. BrickLayoutView.<br> 
 * <p>
 * 02.01.2018
 * @author Frank Kopp
 */
public class MainView {

  @SuppressWarnings("unused")
  private BreakOutGame model;

  @SuppressWarnings("unused")
  private MainController controller;

  private final AnchorPane root;
  private final BrickLayoutView brickLayoutView = new BrickLayoutView();

  // animations
  private ScaleTransition    hitPaddleScaleTransition;
  private StrokeTransition   hitPaddleStrokeTransition;
  private ScaleTransition    hitBallScaleTransition;
  private StrokeTransition   hitBallStrokeTransition;
  private ParallelTransition paddleHitAnimation;
  private ParallelTransition ballHitAnimation;

  /**
   * @param model
   * @param controller2
   * @throws IOException 
   */
  public MainView(BreakOutGame model, MainController controller) throws IOException {
    this.model = model;
    this.controller = controller;

    // read FXML file and setup UI
    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/BreakOutMainView.fxml"));
    fxmlLoader.setController(controller);
    root = (AnchorPane) fxmlLoader.load();

    // get playFieldPane and add brickLayoutView
    final Pane playFieldPane = (Pane) fxmlLoader.getNamespace().get("playfieldPane");
    playFieldPane.getChildren().add(brickLayoutView);

    // Game Over splash top front
    final Text gameoverText = (Text) fxmlLoader.getNamespace().get("gameOverSplash");
    gameoverText.toFront();

    // Ball
    final Circle ball = (Circle) fxmlLoader.getNamespace().get("ball");

    // Paddle
    final Rectangle paddle = (Rectangle) fxmlLoader.getNamespace().get("paddle");

    prepareAnimations(ball, paddle);
  }

  /**
   * create the animations for later playing
   * @param paddle 
   * @param ball 
   */
  private void prepareAnimations(Circle ball, Rectangle paddle) {
    hitPaddleScaleTransition = new ScaleTransition(Duration.millis(50), paddle);
    hitPaddleScaleTransition.setFromX(1.0);
    hitPaddleScaleTransition.setFromY(1.0);
    hitPaddleScaleTransition.setByX(0.1);
    hitPaddleScaleTransition.setByY(0.1);
    hitPaddleScaleTransition.setCycleCount(2);
    hitPaddleScaleTransition.setAutoReverse(true);

    hitPaddleStrokeTransition = new StrokeTransition(Duration.millis(50), paddle);
    hitPaddleStrokeTransition.setFromValue((Color) paddle.getStroke());
    hitPaddleStrokeTransition.setToValue(Color.BLACK);
    hitPaddleStrokeTransition.setCycleCount(2);
    hitPaddleStrokeTransition.setAutoReverse(true);

    hitBallScaleTransition = new ScaleTransition(Duration.millis(50), ball);
    hitBallScaleTransition.setFromX(1.0);
    hitBallScaleTransition.setFromY(1.0);
    hitBallScaleTransition.setByX(0.1);
    hitBallScaleTransition.setByY(0.1);
    hitBallScaleTransition.setCycleCount(2);
    hitBallScaleTransition.setAutoReverse(true);

    hitBallStrokeTransition = new StrokeTransition(Duration.millis(50), ball);
    hitBallStrokeTransition.setFromValue((Color) ball.getStroke());
    hitBallStrokeTransition.setToValue(Color.WHITE);
    hitBallStrokeTransition.setCycleCount(2);
    hitBallStrokeTransition.setAutoReverse(true);

    // combined animations
    paddleHitAnimation = new ParallelTransition(hitPaddleScaleTransition, hitPaddleStrokeTransition, hitBallScaleTransition, hitBallStrokeTransition);
    ballHitAnimation = new ParallelTransition(hitBallScaleTransition, hitBallStrokeTransition);

    //      EXAMPLE for animations over arbitrary properties:
    //      final Timeline timeline = new Timeline();
    //      timeline.setCycleCount(Timeline.INDEFINITE);
    //      timeline.setAutoReverse(true);
    //      final KeyValue kv = new KeyValue(rectBasicTimeline.xProperty(), 300);
    //      final KeyFrame kf = new KeyFrame(Duration.millis(500), kv);
    //      timeline.getKeyFrames().add(kf);
    //      timeline.play();
  }
  
  

  /**
   * @return root pane from loaded FXML
   */
  public Parent asParent() {
    return root;
  }

  /**
   * @return the brickLayoutView
   */
  public BrickLayoutView getBrickLayoutView() {
    return brickLayoutView;
  }

  /**
   * Plays hit animation
   */
  public void paddleHit() {
    paddleHitAnimation.play();    
  }

  /**
   * Plays hit animation
   */
  public void ballHit() {
    ballHitAnimation.play();
  }

  /**
   * Plays hit animation
   * @param row
   * @param col
   */
  public void brickHit(int row, int col) {
    brickLayoutView.getBrickView(row, col).hit();
  }

}
