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

package fko.breakout.view;

import fko.breakout.model.BreakOutGame;
import fko.breakout.model.LaserShot;
import javafx.scene.shape.Rectangle;

public class LaserShotView extends Rectangle {

  private final LaserShot laserShot;

  /**
   * Creates a new Brick view which is an extension of a Rectangle
   *
   * @param laserShot
   */
  public LaserShotView(BreakOutGame model, LaserShot laserShot) {
    this.laserShot = laserShot;

    // let the CSS determine the look of the ball
    this.getStyleClass().add("lasershot");

    //    this.setArcWidth(5.0);
    //    this.setArcHeight(5.0);
    //    this.setStroke(Color.BLACK);
    //    this.setStrokeType(StrokeType.INSIDE);
    //    this.setStrokeWidth(1.0);
    //    this.setEffect(effect);

    //    solidBrickHitTimeline = new FillTransition(Duration.millis(75));
    //    solidBrickHitTimeline.setFromValue(brick.getColor());
    //    solidBrickHitTimeline.setToValue(Color.WHITE);
    //    solidBrickHitTimeline.setCycleCount(2);
    //    solidBrickHitTimeline.setAutoReverse(true);
    //    solidBrickHitTimeline.setShape(this);
  }

  private LaserShot getLaserShot() {
    return laserShot;
  }

  /** Called from Controller after LASER_HIT event to stop an aniymation still running */
  public void hit() {
    System.out.println("LASER hit something");
  }

  public void removed() {

  }
}
