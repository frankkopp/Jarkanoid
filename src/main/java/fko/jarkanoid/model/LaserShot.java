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
 * LaserShot
 *
 * <p>Represents a laser shot which can destroy bricks
 *
 * @author Frank Kopp
 */
public class LaserShot {

  // speed upwards
  private double vY;

  // should this laser be removed
  private boolean isMarkedForRemoval = false;

  /**
   * Creates a new instance of LaserShot with the given position and size.
   *
   * @param x horizontal position of the rectangle
   * @param y vertical position of the rectangle
   * @param width width of the rectangle
   * @param height height of the rectangle
   * @param vY speed with which the laser travels upwards
   */
  public LaserShot(double x, double y, double width, double height, double vY) {
    setWidth(width);
    setHeight(height);
    setX(x);
    setY(y);
    this.vY = vY;
  }

  /** Moves the laser one step further. Expected to be called by the game loop once per frame. */
  public void moveStep() {
    y.set(y.get() - vY);
  }

  /**
   * Tests if the laser shot's rectangular bounds intersect with the given rectangular area
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

  /**
   * Defines the X coordinate of the upper-left corner of the rectangle.
   */
  private DoubleProperty x = new SimpleDoubleProperty();

  public double getX() {
    return x.get();
  }

  public final void setX(double value) {
    x.set(value);
  }

  public DoubleProperty xProperty() {
    return x;
  }

  /**
   * Defines the Y coordinate of the upper-left corner of the rectangle.
   */
  private DoubleProperty y = new SimpleDoubleProperty();

  public final void setY(double value) {
    y.set(value);
  }

  public final double getY() {
    return y.get();
  }

  public final DoubleProperty yProperty() {
    return y;
  }

  /**
   * Defines the width of the rectangle.
   */
  private final DoubleProperty width = new SimpleDoubleProperty();

  public final void setWidth(double value) {
    width.set(value);
  }

  public final double getWidth() {
    return width.get();
  }

  public final DoubleProperty widthProperty() {
    return width;
  }

  /**
   * Defines the height of the rectangle.
   */
  private final DoubleProperty height = new SimpleDoubleProperty();

  public final void setHeight(double value) {
    height.set(value);
  }

  public final double getHeight() {
    return height.get();
  }

  public final DoubleProperty heightProperty() {
    return height;
  }

  /** Marks this ball for removal */
  public void markForRemoval() {
    this.isMarkedForRemoval = true;
  }

  /** @return the isMarkedForRemoval */
  public boolean isMarkedForRemoval() {
    return isMarkedForRemoval;
  }

  public double getUpperBound() {
    return y.get();
  }

  public double getLowerBound() {
    return y.get() + height.get();
  }

  public double getLeftBound() {
    return x.get();
  }

  public double getRightBound() {
    return x.get() + width.get();
  }
}
