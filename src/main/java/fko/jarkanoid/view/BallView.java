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
package fko.jarkanoid.view;

import fko.jarkanoid.model.Ball;
import fko.jarkanoid.model.GameModel;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.StrokeTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BallView extends Circle {

  private static final Logger LOG = LoggerFactory.getLogger(BallView.class);


  private ScaleTransition hitBallScaleTransition;
  private StrokeTransition hitBallStrokeTransition;
  private ParallelTransition ballHitAnimation;

  BallView(GameModel model, Ball ball) {
    super();

    // let the CSS determine the look of the ball
    this.getStyleClass().add("ball");
    //    this.setFill(Color.DODGERBLUE);
    //    this.setStroke(Color.BLACK);

    // bing this ball to the model's ball
    this.centerXProperty().bind(ball.centerXProperty());
    this.centerYProperty().bind(ball.centerYProperty());
    this.radiusProperty().bind(ball.radiusProperty().add(2.0));
    this.visibleProperty().bind(model.isPlayingProperty());

    prepareAnimations();
  }

  public void hit() {
    try {
      ballHitAnimation.play();
    } catch (NullPointerException e) {
      // FIXME: null pointer in animation in BallView.
      LOG.warn("Null Pointer in Ball.hit()");
      // e.printStackTrace();
    }
  }

  public void removed() {
    ballHitAnimation.stop();
  }

  private void prepareAnimations() {
    hitBallScaleTransition = new ScaleTransition(Duration.millis(50), this);
    hitBallScaleTransition.setFromX(1.0);
    hitBallScaleTransition.setFromY(1.0);
    hitBallScaleTransition.setByX(0.1);
    hitBallScaleTransition.setByY(0.1);
    hitBallScaleTransition.setCycleCount(2);
    hitBallScaleTransition.setAutoReverse(true);

    hitBallStrokeTransition = new StrokeTransition(Duration.millis(50), this);
    final Color fromColor = (Color) this.getStroke();
    hitBallStrokeTransition.setFromValue(fromColor);
    hitBallStrokeTransition.setToValue(Color.WHITE);
    hitBallStrokeTransition.setCycleCount(2);
    hitBallStrokeTransition.setAutoReverse(true);

    ballHitAnimation = new ParallelTransition(hitBallScaleTransition, hitBallStrokeTransition);
  }
}
