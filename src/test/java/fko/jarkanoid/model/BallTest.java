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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * BallTest
 * 09.01.2018
 * @author Frank Kopp
 */
public class BallTest {

  /**
   * @throws java.lang.Exception
   */
  @BeforeAll
  public static void setUp() throws Exception {
  }

  @Test
  public void testBall() throws Exception {
    Ball b;
    b = new Ball(100, 100, 5, -1, 1);

    assertTrue(b!=null);
    assertEquals(b.getCenterX(), 100, 0.001);
    assertEquals(b.getCenterY(), 100, 0.001);
    assertEquals(b.getRadius(), 5, 0.001);
    assertEquals(b.getXVelocity(), -1, 0.001);
    assertEquals(b.getYVelocity(), 1, 0.001);
    assertEquals(b.getVelocity(), Math.sqrt(2), 0.001);
    assertEquals(b.getUpperBound(), 95, 0.001);
    assertEquals(b.getLowerBound(), 105, 0.001);
    assertEquals(b.getLeftBound(), 95, 0.001);
    assertEquals(b.getRightBound(), 105, 0.001);
  }

  @Test
  public void testSetVelocity() throws Exception {
//    Ball b = new Ball(100, 100, 5, 1, 1);
//    b.setVelocity(Math.sqrt(8));
//    assertEquals(2.0, b.getXVelocity(), 0.001);
//    assertEquals(2.0, b.getYVelocity(), 0.001);
//    b.setVelocity(Math.sqrt(32));
//    assertEquals(4.0, b.getXVelocity(), 0.001);
//    assertEquals(4.0, b.getYVelocity(), 0.001);
//    b.inverseYdirection();
//    b.inverseXdirection();
//    assertEquals(-4.0, b.getXVelocity(), 0.001);
//    assertEquals(-4.0, b.getYVelocity(), 0.001);
//    assertEquals(Math.sqrt(32), b.getVelocity());
  }

  @Test
    public void testBounceFromPaddle() throws Exception {
  //    Ball b;
  //    b = new Ball(100, 100, 5, -1, 0);
  //    System.out.println(b.getAngle());
  //    assertEquals(-90, b.getAngle(), 0.001);
  //    b = new Ball(100, 100, 5, -1, -1);
  //    System.out.println(b.getAngle());
  //    assertEquals( -45, b.getAngle(), 0.001);
  //    b = new Ball(100, 100, 5, 0, -1);
  //    System.out.println(b.getAngle());
  //    assertEquals(0, b.getAngle(), 0.001);
  //    b = new Ball(100, 100, 5, 1, -1);
  //    System.out.println(b.getAngle());
  //    assertEquals(45, b.getAngle(), 0.001);
  //    b = new Ball(100, 100, 5, 1, 0);
  //    System.out.println(b.getAngle());
  //    assertEquals(90, b.getAngle(), 0.001);
  //
  //    b = new Ball(100, 100, 5, -1, 0);
  //    System.out.println(b.getAngle());
  //    assertEquals(-90, b.getAngle(), 0.001);
  //    b = new Ball(100, 100, 5, -1, 1);
  //    System.out.println(b.getAngle());
  //    assertEquals( -45, b.getAngle(), 0.001);
  //    b = new Ball(100, 100, 5, 0, 1);
  //    System.out.println(b.getAngle());
  //    assertEquals(0, b.getAngle(), 0.001);
  //    b = new Ball(100, 100, 5, 1, 1);
  //    System.out.println(b.getAngle());
  //    assertEquals(45, b.getAngle(), 0.001);
  //    b = new Ball(100, 100, 5, 1, 0);
  //    System.out.println(b.getAngle());
  //    assertEquals(90, b.getAngle(), 0.001);
  //
  //    b = new Ball(100, 100, 5, -1, -1);
  //    
  //    b.setAngle(-90);
  //    assertEquals(0, b.getYVelocity(), 0.001);
  //    assertEquals(-Math.sqrt(2), b.getXVelocity(), 0.001);
  //    b.setAngle(-45);
  //    assertEquals(Math.sqrt(2), b.getYVelocity(), 0.001);
  //    assertEquals(Math.sqrt(2), b.getXVelocity(), 0.001);
  //    b.setAngle(45);
  //    assertEquals(Math.sqrt(2), b.getYVelocity(), 0.001);
  //    assertEquals(0, b.getXVelocity(), 0.001);
  //    b.setAngle(90);
  //    assertEquals(0, b.getYVelocity(), 0.001);
  //    assertEquals(Math.sqrt(2), b.getXVelocity(), 0.001);
  
    }

  @Test
  void setSpeed() {
    Ball b;
    b = new Ball(100, 100, 5, -1, 1);
    assertEquals(b.getVelocity(), Math.sqrt(2), 0.001);
    System.out.printf("Speed %f x:%f y:%f %n", b.getVelocity(), b.getXVelocity(), b.getYVelocity());
    b.setVelocity(Math.sqrt(8));
    System.out.printf("Speed %f x:%f y:%f %n", b.getVelocity(), b.getXVelocity(), b.getYVelocity());

    b = new Ball(100, 100, 5, 1, 2);
    assertEquals(b.getVelocity(), Math.sqrt(5), 0.001);
    System.out.printf("Speed %f x:%f y:%f %n", b.getVelocity(), b.getXVelocity(), b.getYVelocity());
    b.setVelocity(Math.sqrt(12));
    System.out.printf("Speed %f x:%f y:%f %n", b.getVelocity(), b.getXVelocity(), b.getYVelocity());

    b = new Ball(100, 100, 5, 2, -5);
    assertEquals(b.getVelocity(), Math.sqrt(29), 0.001);
    System.out.printf("Speed %f x:%f y:%f %n", b.getVelocity(), b.getXVelocity(), b.getYVelocity());
    b.setVelocity(Math.sqrt(29)/2);
    System.out.printf("Speed %f x:%f y:%f %n", b.getVelocity(), b.getXVelocity(), b.getYVelocity());

  }
}
