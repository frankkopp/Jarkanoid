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
package fko.breakout.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Ball
 * <p>
 * Represents a Ball for the game.
 * <p>
 * 09.01.2018
 * 
 * @author Frank Kopp
 */
public class Ball {

  // ball size and position properties
  private final DoubleProperty ballRadius;
  private final DoubleProperty ballCenterX;
  private final DoubleProperty ballCenterY;

  // ball speeds in each direction
  private double vX;
  private double vY;
  
  // current total ball speed / will be calculated whenever vX or vY change
  private double velocity;
  private double angle;

  /**
   * @param ballCenterX
   * @param ballCenterY
   * @param ballRadius
   * @param vXball
   * @param vYball
   */
  public Ball(double ballCenterX, double ballCenterY, double ballRadius, double vXball, double vYball) {
    this.ballCenterX = new SimpleDoubleProperty(ballCenterX);
    this.ballCenterY = new SimpleDoubleProperty(ballCenterY);
    this.ballRadius = new SimpleDoubleProperty(ballRadius);
    setXYVelocity(vXball, vYball);
  }
  
  private void setXYVelocity(double vX, double vY) {
    this.vX = vX;
    this.vY = vY;
    this.velocity = Math.sqrt(Math.pow(this.vX, 2.0) + Math.pow(this.vY,2.0));
    this.angle = Math.toDegrees(Math.atan(vY/vX));  // alpha = atan(y/x)
  }

  /**
   * Sets a new velocity without changing direction and therefore
   * not changing the ratio of vX to vY.
   * @param newV
   */
  public void setVelocity(double newV) {
    final double vXtmp = newV * Math.cos(Math.toRadians(angle)); // cos(alpha) * c
    final double vYtmp = newV * Math.sin(Math.toRadians(angle)); // sin(alpha) * c
    setXYVelocity(vXtmp, vYtmp);
  }

  public void moveBall() {
    ballCenterX.set(ballCenterX.get() + vX);
    ballCenterY.set(ballCenterY.get() + vY);
  }
  
  /**
   * Gets the current angle in degrees.
   * 0 being straight up. 180 being straight down.
   * 90 being right. 270 being left.
   * @param newAngle
   */
  public double getAngle() {
    return angle;
  }
  
  /**
   * Sets a new angle in degrees at constant speed for the ball.
   * 0 being straight up. 180 being straight down.
   * 90 being right. 270 being left.
   * @param newAngle
   */
  public void setNewAngle(double newAngle) {
    final double vXtmp = Math.sin(Math.toRadians(newAngle)) * velocity;
    final double vYtmp = Math.cos(Math.toRadians(newAngle)) * velocity;
    setXYVelocity(vXtmp, vYtmp);
  }
  
  public double inverseXdirection() {
    vX *= -1;
    return vX;
  }

  public double inverseYdirection() {
    vY *= -1;
    return vY;
  }
  
  public double getUpperBound() {
    return ballCenterY.get() - ballRadius.get();
  }
  
  public double getLowerBound() {
    return ballCenterY.get() + ballRadius.get();
  }
  
  public double getLeftBound() {
    return ballCenterX.get() - ballRadius.get();
  }
  
  public double getRightBound() {
    return ballCenterX.get() + ballRadius.get();
  }

  public DoubleProperty ballRadiusProperty() {
    return ballRadius;
  }

  public double getBallRadius() {
    return ballRadius.get();
  }

  public void setBallRadius(double value) {
    ballRadius.set(value);
  }

  public DoubleProperty ballCenterXProperty() {
    return ballCenterX;
  }

  public double getBallCenterX() {
    return ballCenterX.get();
  }

  public void setBallCenterX(double value) {
    ballCenterX.set(value);
  }

  public DoubleProperty ballCenterYProperty() {
    return ballCenterY;
  }

  public double getBallCenterY() {
    return ballCenterX.get();
  }

  public void setBallCenterY(double value) {
    ballCenterX.set(value);
  }

}
