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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * BrickLayoutTest
 * 04.01.2018
 * @author Frank Kopp
 */
class BrickLayoutTest {

	private DoubleProperty playfieldWidth;
	private DoubleProperty playfieldHeight;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {

		playfieldWidth = new SimpleDoubleProperty(780);
		playfieldHeight = new SimpleDoubleProperty(710);
		
	}
	
	/**
	 * Test method for {@link fko.breakout.model.BrickLayout#BrickLayout(double, double, double)}.
	 */
	@Test
	void testBrickLayout() {
		BrickLayout bl = new BrickLayout(2, playfieldWidth, playfieldHeight);
		assertNotNull(bl);
		assertEquals(2, bl.getBrickGap());
		assertEquals(780, bl.getPlayfieldWidth());
		assertEquals(710, bl.getPlayfieldHeight());
	}

	/**
	 * Test method for {@link fko.breakout.model.BrickLayout#getMatrix()}.
	 */
	@Test
	void testGetMatrix() {
		BrickLayout bl = new BrickLayout(2, playfieldWidth, playfieldWidth);
		assertNotNull(bl.getMatrix());
		assertTrue(bl.getMatrix() instanceof Brick[][]);
//		System.out.println(bl);
	}

	/**
	 * Test method for {@link fko.breakout.model.BrickLayout#getRow(int)}.
	 */
	@Test
	void testGetRow() {
		BrickLayout bl = new BrickLayout(2, playfieldWidth, playfieldWidth);
		assertNotNull(bl.getRow(0));
		assertTrue(bl.getRow(0) instanceof Brick[]);
//		System.out.println(Arrays.toString(bl.getRow(0)));
	}

	/**
	 * Test method for {@link fko.breakout.model.BrickLayout#getBrick(int, int)}.
	 */
	@Test
	void testSetGetBrick() {
		BrickLayout bl = new BrickLayout(2, playfieldWidth, playfieldWidth);
		Brick b1 = new Brick(BrickType.GREY, BrickPowerType.NONE);
		bl.setBrick(4, 4, b1);
		System.out.println(bl);
		assertEquals(b1, bl.getBrick(4, 4));
		assertEquals(b1, bl.getRow(4)[4]);
	}

	@Test
		public void testUpdateDataforMatrix() throws Exception {
			BrickLayout bl = new BrickLayout(2, playfieldWidth, playfieldWidth);
			bl.setMatrix(LevelLoader.getInstance().getLevel(1));
			
	//		System.out.println(
	//				String.format("up: %d / %f lo: %d / %f le: %d / %f ri: %d / %f"
	//				,bl.getUpperRow(), bl.getUpperBound()
	//				,bl.getLowerRow(), bl.getLowerBound()
	//				,bl.getLeftCol(),  bl.getLeftBound()
	//				,bl.getRightCol(), bl.getRightBound()
	//				));
	//		System.out.println();
	//		System.out.println(bl);
	//		
	//		double gap = bl.getBrickGap();
	//		assertEquals(4, bl.getUpperRow());
	//		assertEquals(9, bl.getLowerRow());
	//		assertEquals(0, bl.getLeftCol());
	//		assertEquals(10, bl.getRightCol());
	//		assertEquals(gap + (4*gap) + 4 * bl.getBrickHeight(), bl.getUpperBound());
	//		assertEquals(gap + (9*gap) + 9 * bl.getBrickHeight() + bl.getBrickHeight(), bl.getLowerBound());
	//		assertEquals(gap + (0*gap) + 0 * bl.getBrickWidth(), bl.getLeftBound());
	//		assertEquals(gap + (10*gap) + 10 * bl.getBrickWidth() + bl.getBrickWidth(), bl.getRightBound());
			
		}
}
	
