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
package fko.jarkanoid.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BrickTest
 * 04.01.2018
 * @author Frank Kopp
 */
class BrickTest {

	/**
	 * Test method for {@link fko.jarkanoid.model.Brick#Brick(fko.jarkanoid.model.BrickType, PowerPillType)}.
	 */
	@Test
	void testBrick() {
		Brick brick = new Brick(BrickType.GREY, PowerPillType.NONE);
		assertNotNull(brick);
	}

	/**
	 * Test method for {@link fko.jarkanoid.model.Brick#increaseHitCount()}.
	 */
	@Test
	void testIncreaseHitCount() {
		Brick brick = new Brick(BrickType.SILVER, PowerPillType.NONE);
		int remaining = brick.getRemainingHits();
		int newRemaining = brick.increaseHitCount();
		assertTrue(remaining-newRemaining==1);
	}

	/**
	 * Test method for {@link fko.jarkanoid.model.Brick#getHitCount()}.
	 */
	@Test
	void testGetHitCount() {
		Brick brick = new Brick(BrickType.GREY, PowerPillType.NONE);
		assertTrue(brick.getHitCount()==0);
		brick.increaseHitCount();
		assertTrue(brick.getHitCount()==1);
	}

	/**
	 * Test method for {@link fko.jarkanoid.model.Brick#getRemainingHits()}.
	 */
	@Test
	void testGetRemainingHits() {
		Brick brick = new Brick(BrickType.GREY, PowerPillType.NONE);
		brick.increaseHitCount();
		assertTrue(brick.getRemainingHits()==0);
	}

	/**
	 * Test method for {@link fko.jarkanoid.model.Brick#isKilled()}.
	 */
	@Test
	void testIsKilled() {
		Brick brick = new Brick(BrickType.SILVER, PowerPillType.NONE);
		int remainingHits = brick.getRemainingHits(); 
		for (int i=0; i<remainingHits; i++) {
			brick.increaseHitCount();	
		}
		assertTrue(brick.isKilled());
	}

	/**
	 * Test method for {@link fko.jarkanoid.model.Brick#getType()}.
	 */
	@Test
	void testGetType() {
		Brick brick = new Brick(BrickType.SILVER, PowerPillType.NONE);
		assertEquals(BrickType.SILVER, brick.getType());
	}

	/**
	 * Test method for {@link fko.jarkanoid.model.Brick#getPowerType()}.
	 */
	@Test
	void testGetPowerType() {
		Brick brick = new Brick(BrickType.SILVER, PowerPillType.CATCH);
		assertEquals(PowerPillType.CATCH, brick.getPowerType());
	}

	/**
	 * Test method for {@link fko.jarkanoid.model.Brick#getPoints()}.
	 */
	@Test
	void testGetScore() {
		Brick brick = new Brick(BrickType.SILVER, PowerPillType.NONE);
		assertEquals(50, brick.getPoints());
	}

	/**
	 * Test method for {@link fko.jarkanoid.model.Brick#getColor()}.
	 */
	@Test
	void testGetColor() {
		Brick brick = new Brick(BrickType.SILVER, PowerPillType.NONE);
		assertEquals(50, brick.getPoints());
	}

	/**
  	 * Test method for {@link fko.jarkanoid.model.Brick#toToken()}.
  	 */
  	@Test
  	void testToToken() {
  		Brick brick = new Brick(BrickType.SILVER, PowerPillType.NONE);
  		System.out.println(brick.toToken());
  		assertEquals("SINO", brick.toToken());
  	}

	/**
	 * Test method for {@link fko.jarkanoid.model.Brick#clone()}.
	 */
	@Test
	void testClone() {
		Brick brick = new Brick(BrickType.SILVER, PowerPillType.NONE);
		Brick cloneBrick = new Brick(brick);
		assertTrue(brick.equals(cloneBrick));
		assertFalse(brick == cloneBrick);
	}

	/**
	 * Test method.
	 */
	@Test
	void testEquals() {
		Brick brick1 = new Brick(BrickType.SILVER, PowerPillType.NONE);
		Brick brick2 = new Brick(BrickType.SILVER, PowerPillType.NONE);
		assertTrue(brick1.equals(brick2));
		brick2.increaseHitCount();
		assertFalse(brick1.equals(brick2));
		Brick brick3 = new Brick(BrickType.SILVER, PowerPillType.CATCH);
		assertFalse(brick1.equals(brick3));
		Brick brick4 = new Brick(BrickType.GREY, PowerPillType.NONE);
		assertFalse(brick1.equals(brick4));
	}

}
