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

import javafx.scene.paint.Color;

/**
 * BrickPowerType
 * 04.01.2018
 * @author Frank Kopp
 */
public enum PowerPillType {

  NONE (Color.WHITE, "N"),

  /**
   *  	Collect the red capsule to transform the Vaus into its Laser-firing configuration. In this
   *  form, you can fire lasers at the top of the screen by pushing the fire button. Lasers can be
   *  used against every brick except Gold bricks, and against enemies. Silver bricks can only be
   *  destroyed by lasers when they are hit the required number of times.
   */
  LASER (Color.RED, "L"),

  /**
   * Collect the blue capsule to extend the width of the Vaus.
   */
  ENLARGE (Color.BLUE, "E"),

  /**
   * Collect the green capsule to gain the catch ability. When the ball hits the Vaus, it will stick
   * to the surface. Press the Fire button to release the ball. The ball will automatically release
   * after a certain period of time has passed.
   */
  CATCH (Color.GREEN, "C"),

  /**
   * Collect the orange capsule to slow the velocity at which the ball moves. Collecting multiple orange
   * capsules will have a cumulative effect and the ball velocity can become extremely slow. However,
   * the ball velocity will gradually increase as it bounces and destroys bricks. The velocity may
   * sometimes suddenly increase with little warning.
   */
  SLOW (Color.ORANGE, "S"),

  /**
   * Collect the violet capsule to create a "break out" exit on the right side of the stage. Passing
   * through this exit will cause you to advance to the next stage immediately, as well as earn
   * a 10,000 point bonus.
   */
  BREAK (Color.PURPLE, "B"),

  /**
   * Collect the cyan capsule to cause the ball to split into three instances of itself. All three
   * balls can be kept aloft. There is no penalty for losing the first two balls. No colored capsules
   * will fall as long as there is more than one ball in play. This is the only power up that, while
   * in effect, prevents other power ups from falling.
   */
  DISRUPTION (Color.CYAN, "D"),

  /**
   * Collect the gray capsule to earn an extra Vaus.
   */
  PLAYER (Color.GRAY, "P");

  public final Color color;
  public final String token;

  PowerPillType(Color color, String token) {
    this.color = color;
    this.token = token;
  }

  /**
   * Returns a random power based on the random factor of each power.<br>
   * Therefore some powers are rare (e.g. new live) while others are more common.
   * @return the random power
   */
  public static PowerPillType getRandom() {
    double random = Math.random() * 100;
    if (random <  1                  ) return BREAK;
    if (random >= 1  && random <    5) return PLAYER;
    if (random >= 5  && random <   20) return DISRUPTION;
    if (random >= 20 && random <   40) return SLOW;
    if (random >= 40 && random <   60) return CATCH;
    if (random >= 60 && random <   90) return ENLARGE;
    if (random >= 90 && random <= 100) return LASER;
    return NONE;
  }

  @Override
  public String toString() {
    return "PowerPillType{" +
            "name=" + this.name() +
            ", color=" + color +
            ", token=" + token +
            '}';
  }
}
