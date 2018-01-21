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
package fko.jarkanoid.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Ball
 *
 * <p>Represents a Ball for the game.
 *
 * <p>09.01.2018
 *
 * @author Frank Kopp
 */
public class Ball {

  // ball size and position properties
  private final DoubleProperty radius;
  private final DoubleProperty centerX;
  private final DoubleProperty centerY;

  // for easier path calculations for collision checks

  private double previousCenterX;
  private double previousCenterY;
  // ball speeds in each direction

  private double vX;
  private double vY;

  // current total ball speed and angle - will be calculated whenever vX or vY change

  private double velocity;
  // should this ball be removed

  private boolean isMarkedForRemoval = false;

  /** Copy constructor for creating a new ball as a deep copy of an existing one.s */
  public Ball(Ball toCopy) {
    this(
        toCopy.centerXProperty().get(),
        toCopy.centerYProperty().get(),
        toCopy.radiusProperty().get(),
        toCopy.vX,
        toCopy.vY);
    this.isMarkedForRemoval = toCopy.isMarkedForRemoval;
    this.previousCenterX = toCopy.previousCenterX;
    this.previousCenterY = toCopy.previousCenterY;
  }

  /**
   * Creates a new ball
   *
   * @param centerX
   * @param centerY
   * @param radius
   * @param vXball
   * @param vYball
   */
  public Ball(double centerX, double centerY, double radius, double vXball, double vYball) {
    super();
    this.centerX = new SimpleDoubleProperty(centerX);
    this.centerY = new SimpleDoubleProperty(centerY);
    this.radius = new SimpleDoubleProperty(radius);
    setXYVelocity(vXball, vYball);
  }

  /**
   * Sets the velocities for the ball in X and Y direction
   *
   * @param vX
   * @param vY
   */
  public void setXYVelocity(double vX, double vY) {
    this.vX = vX;
    this.vY = vY;
    this.velocity = Math.sqrt(vX * vX + vY * vY);
  }

  /** Moves the ball one step further. Expected to be called by the game loop once per frame. */
  public void moveStep() {
    previousCenterX = centerX.get();
    centerX.set(previousCenterX + vX);
    previousCenterY = centerY.get();
    centerY.set(previousCenterY + vY);
  }

  public void setVelocity(final double newSpeed) {
    final double ratio = newSpeed / velocity;
    setXYVelocity(vX * ratio, vY * ratio);
  }

  /** Creates a clone of the ball and randomly changes direction slightly */
  public Ball split() {
    final Ball newBall = new Ball(this);
    nudgeBall(newBall);
    return newBall;
  }

  /** changes the direction of the ball slightly */
  public void nudgeBall() {
    nudgeBall(this);
  }

  private void nudgeBall(final Ball newBall) {
    newBall.setYVelocity(
        newBall.getYVelocity() + (Math.random() - 0.5) * newBall.getYVelocity() / 5);
    newBall.setXVelocity(
        newBall.getXVelocity() + (Math.random() - 0.5) * newBall.getXVelocity() / 5);
  }

  /**
   * Sets a new angle in degrees at constant speed for the ball.
   *
   * @param newAngle
   */
  public void bounceFromPaddle(double newAngle) {
    final double vXtmp = Math.sin(Math.toRadians(newAngle)) * velocity;
    final double vYtmp = -Math.cos(Math.toRadians(newAngle)) * velocity;
    setXYVelocity(vXtmp, vYtmp);
  }

  /**
   * Tests if the balls rectangular bounds intersect with the given rectangular area
   *
   * @param x
   * @param y
   * @param width
   * @param height
   * @return true if ball intersects with rectangular area
   */
  public boolean intersects(double x, double y, double width, double height) {
    return (x + width >= getLeftBound()
        && y + height >= getUpperBound()
        && x <= getRightBound()
        && y <= getLowerBound());
  }

  public double inverseXdirection() {
    vX *= -1;
    return vX;
  }

  public double inverseYdirection() {
    vY *= -1;
    return vY;
  }

  /** Marks this ball for removal */
  public void markForRemoval() {
    this.isMarkedForRemoval = true;
  }

  /** @see java.lang.Object#clone() */
  @SuppressWarnings("MethodDoesntCallSuperMethod")
  @Override
  protected Ball clone() {
    return new Ball(this);
  }

  /** @see java.lang.Object#toString() */
  @Override
  public String toString() {
    return "Ball [radius="
        + radius
        + ", centerX="
        + centerX
        + ", centerY="
        + centerY
        + ", vX="
        + vX
        + ", vY="
        + vY
        + ", velocity="
        + velocity
        + "]";
  }

  public DoubleProperty radiusProperty() {
    return radius;
  }

  public DoubleProperty centerXProperty() {
    return centerX;
  }

  public DoubleProperty centerYProperty() {
    return centerY;
  }

  public double getUpperBound() {
    return centerY.get() - radius.get();
  }

  public double getLowerBound() {
    return centerY.get() + radius.get();
  }

  public double getLeftBound() {
    return centerX.get() - radius.get();
  }

  public double getRightBound() {
    return centerX.get() + radius.get();
  }

  public double getXVelocity() {
    return vX;
  }

  public void setXVelocity(double newVX) {
    setXYVelocity(newVX, vY);
  }

  public double getYVelocity() {
    return vY;
  }

  public void setYVelocity(double newVY) {
    setXYVelocity(vX, newVY);
  }

  public double getCenterX() {
    return centerX.get();
  }

  public void setCenterX(double value) {
    previousCenterX = centerX.get();
    centerX.set(value);
  }

  public double getCenterY() {
    return centerY.get();
  }

  public void setCenterY(double value) {
    previousCenterY = centerY.get();
    centerY.set(value);
  }

  public double getRadius() {
    return radius.get();
  }

  public void setRadius(double value) {
    radius.set(value);
  }

  public double getVelocity() {
    return velocity;
  }

  public double getPreviousCenterX() { return previousCenterX; }

  public double getPreviousCenterY() { return previousCenterY; }

  /** @return the isMarkedForRemoval */
  public boolean isMarkedForRemoval() {
    return isMarkedForRemoval;
  }
}
