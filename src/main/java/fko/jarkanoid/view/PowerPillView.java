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

import fko.jarkanoid.model.PowerPill;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class PowerPillView extends StackPane {

  private final PowerPill powerPill;

  private Timeline pillAnimationTimer = new Timeline();

//  private static final Font newFont = Font.font(null, FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 18);

  /**
   * @param powerPill the model power pill this view shall be based on
   */
  PowerPillView(PowerPill powerPill) {
    super();
    this.powerPill = powerPill;
//    System.out.println("VIEW: Added PowerPill: "+powerPill);

    // let the CSS determine the look of the pill
    this.getStyleClass().add("powerpill");

    final Light.Distant light = new Light.Distant();
    light.setAzimuth(0.0);
    final Lighting effect = new Lighting();
    effect.setLight(light);
    effect.setSurfaceScale(5.0f);

    final Rectangle rectangle = new Rectangle();
    rectangle.setWidth(powerPill.getWidth());
    rectangle.setHeight(powerPill.getHeight());
    rectangle.setEffect(effect);
    rectangle.setFill(powerPill.getPowerPillType().color);
    rectangle.getStyleClass().add("powerpill_rec");
//    rectangle.setArcHeight(20.0);
//    rectangle.setArcWidth(20.0);

    final Text label = new Text(powerPill.getPowerPillType().token);
    label.getStyleClass().add("powerpill_text");
//    label.setFill(Color.DARKGRAY);
//    label.setFont(newFont);

    // animation for power pills while falling
    pillAnimationTimer.setCycleCount(Animation.INDEFINITE);
    pillAnimationTimer.setAutoReverse(false);
    final KeyFrame kf = new KeyFrame(Duration.millis(1000), new KeyValue(label.rotateProperty(), 360));
    final KeyFrame kf2 = new KeyFrame(Duration.millis(1000), new KeyValue(light.azimuthProperty(), 360));
    pillAnimationTimer.getKeyFrames().addAll(kf, kf2);
    pillAnimationTimer.play();

    this.translateXProperty().bind(powerPill.xProperty());
    this.translateYProperty().bind(powerPill.yProperty());

    this.getChildren().addAll(rectangle, label);
  }

  public void removed() {
  }

  @Override
  public String toString() {
    return "PowerPillView{" +
            "powerPill=" + powerPill + System.lineSeparator() +
            "layoutX=" + getLayoutX() + " " +
            "layoutY=" + getLayoutY() + " " +
            "translateX=" + getTranslateX() + " " +
            "translateY=" + getTranslateY() + " " +
            '}';
  }
}
