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

public class PowerPill {

  private final PowerPillType powerPillType;

  private final DoubleProperty x = new SimpleDoubleProperty(0);
  private final DoubleProperty y = new SimpleDoubleProperty(0);
  private final DoubleProperty width = new SimpleDoubleProperty(0);
  private final DoubleProperty height = new SimpleDoubleProperty(0);

  private double fallingSpeed;

  /**
   * @param powerPillType
   * @param x
   * @param y
   * @param width
   * @param height
   * @param fallingSpeed
   */
  public PowerPill(PowerPillType powerPillType, double x, double y, double width, double height, double fallingSpeed) {
    this.powerPillType = powerPillType;
    this.x.set(x);
    this.y.set(y);
    this.width.set(width);
    this.height.set(height);
    this.fallingSpeed = fallingSpeed;
  }

  public double fall(final double deltaTimeCapped) {
    final double px = this.fallingSpeed * deltaTimeCapped;
    y.set(y.get() + px);
    return getY();
  }

  public PowerPillType getPowerPillType() {
    return powerPillType;
  }

  public double getX() {
    return x.get();
  }

  public DoubleProperty xProperty() {
    return x;
  }

  public void setX(double x) {
    this.x.set(x);
  }

  public double getY() {
    return y.get();
  }

  public DoubleProperty yProperty() {
    return y;
  }

  public void setY(double y) {
    this.y.set(y);
  }

  public double getWidth() {
    return width.get();
  }

  public DoubleProperty widthProperty() {
    return width;
  }

  public void setWidth(double width) {
    this.width.set(width);
  }

  public double getHeight() {
    return height.get();
  }

  public DoubleProperty heightProperty() {
    return height;
  }

  public void setHeight(double height) {
    this.height.set(height);
  }

  public double getFallingSpeed() {
    return fallingSpeed;
  }

  public void setFallingSpeed(double fallingSpeed) {
    this.fallingSpeed = fallingSpeed;
  }

  @Override
  public String toString() {
    return "PowerPill{" +
            "powerPillType=" + powerPillType +
            ", x=" + x +
            ", y=" + y +
            ", width=" + width +
            ", height=" + height +
            ", fallingSpeed=" + fallingSpeed +
            '}';
  }
}

