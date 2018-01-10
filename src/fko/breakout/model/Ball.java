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
public class Ball implements Cloneable {

  // ball size and position properties
  private final DoubleProperty ballRadius;
  private final DoubleProperty ballCenterX;
  private final DoubleProperty ballCenterY;

  // ball speeds in each direction
  private double vX;
  private double vY;
  
  // current total ball speed and angle - will be calculated whenever vX or vY change
  private double velocity;
  
  // should this ball be removed
  private boolean isMarkedForRemomval = false; 

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
  
  public void moveStep() {
    ballCenterX.set(ballCenterX.get() + vX);
    ballCenterY.set(ballCenterY.get() + vY);
  }
  
  /**
   * Creates a clone of the ball and randomly changes direction slightly
   */
  public Ball split() {
    final Ball newBall = this.clone();
    newBall.setYVelocity(newBall.getYVelocity() + (Math.random()-0.5) * newBall.getYVelocity() / 10); 
    newBall.setXVelocity(newBall.getXVelocity() + (Math.random()-0.5) * newBall.getXVelocity() / 10);
    return newBall;    
  }

  private void setXYVelocity(double vX, double vY) {
    this.vX = vX;
    this.vY = vY;
    this.velocity = Math.sqrt(Math.pow(this.vX, 2.0) + Math.pow(this.vY,2.0));
  }
  
  /**
   * Sets a new angle in degrees at constant speed for the ball.
   * @param newAngle
   */
  public void bounceFromPaddle(double newAngle) {
    final double vXtmp = Math.sin(Math.toRadians(newAngle)) * velocity;
    final double vYtmp = -Math.cos(Math.toRadians(newAngle)) * velocity;
    setXYVelocity(vXtmp, vYtmp);
  }

  public boolean intersects(double x, double y, double width, double height) {
    return (x + width >= getLeftBound() &&
            y + height >= getUpperBound() &&
            x <= getRightBound() &&
            y <= getLowerBound());
  }
  
  public double getXVelocity() {
    return vX;
  }

  public void setXVelocity(double newVX) {
    setXYVelocity(newVX, vY);
  }

  public void setYVelocity(double newVY) {
    setXYVelocity(vX, newVY);
  }
  
  public double getYVelocity() {
    return vY;
  }
  
  public double getVelocity() {
    return velocity;
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

  public DoubleProperty radiusProperty() {
    return ballRadius;
  }

  public double getRadius() {
    return ballRadius.get();
  }

  public void setRadius(double value) {
    ballRadius.set(value);
  }

  public DoubleProperty centerXProperty() {
    return ballCenterX;
  }

  public double getCenterX() {
    return ballCenterX.get();
  }

  public void setCenterX(double value) {
    ballCenterX.set(value);
  }

  public DoubleProperty centerYProperty() {
    return ballCenterY;
  }

  public double getCenterY() {
    return ballCenterY.get();
  }

  public void setCenterY(double value) {
    ballCenterY.set(value);
  }

  /**
   * @return the isMarkedForRemomval
   */
  public boolean isMarkedForRemoval() {
    return isMarkedForRemomval;
  }

  /**
   * @param isMarkedForRemomval the isMarkedForRemomval to set
   */
  public void markForRemoval() {
    this.isMarkedForRemomval = true;
  }

  /**
   * @see java.lang.Object#clone()
   */
  @Override
  protected Ball clone() {
    return new Ball(
        this.centerXProperty().get(),
        this.centerYProperty().get(),
        this.radiusProperty().get(),
        this.vX,
        this.vY);
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Ball [ballRadius=");
    builder.append(ballRadius);
    builder.append(", ballCenterX=");
    builder.append(ballCenterX);
    builder.append(", ballCenterY=");
    builder.append(ballCenterY);
    builder.append(", vX=");
    builder.append(vX);
    builder.append(", vY=");
    builder.append(vY);
    builder.append(", velocity=");
    builder.append(velocity);
    builder.append("]");
    return builder.toString();
  }

}
