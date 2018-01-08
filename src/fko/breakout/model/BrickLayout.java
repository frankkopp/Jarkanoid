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
 * BrickLayout
 * <p>
 * A <code>BrickLayout</code> holds a 18x13 matrix of <code>Brick</code>s. Bricks will be erased after 
 * they have been hit by the ball the necessary number of times.<br>
 * The matrix is top down - row 0 is the uppermost row.<br>
 * This class also calculates the measurements and positions of bricks based on the playfield size
 * and a gap between the Bricks.<br>
 * It also checks for collision when given a ball with a center and radius.<br>
 * <p>
 * 04.01.2018
 * @author Frank Kopp
 */
public class BrickLayout {

	public static final int ROWS = 18;
	public static final int COLUMNS = 13;

	private double brickGap;
	private DoubleProperty playfieldWidth;
	private DoubleProperty playfieldHeight;

	private Brick[][] brickMatrix = new Brick[ROWS][COLUMNS];

	private DoubleProperty brickWidth = new SimpleDoubleProperty();
	private DoubleProperty brickHeight = new SimpleDoubleProperty();

	private int numberOfBricks = 0;

	/**
	 * Creates an empty BrickLayout.
	 * 
	 * @param brickGap
	 * @param playfieldWidth
	 * @param playfieldHeight
	 */
	public BrickLayout(double brickGap, DoubleProperty playfieldWidth, DoubleProperty playfieldHeight) {
		this.brickGap = brickGap;
		this.playfieldWidth = playfieldWidth;
		this.playfieldHeight = playfieldHeight;

		// calculated bind of brick size to playfield size
		brickWidth.bind(playfieldWidth
				.subtract((COLUMNS+1)*brickGap)
				.divide(COLUMNS));
		brickHeight.bind(playfieldHeight
				.subtract(playfieldHeight.get()*0.4)
				.subtract((ROWS+1)*brickGap)
				.divide(ROWS));
	}

	/**
	 * @param row
	 * @param col
	 * @return number of points for this hit
	 */
	public int hitBrick(int row, int col) {
		final Brick brick = brickMatrix[row][col];
		final int points = brick.getPoints();
		if (brick.increaseHitCount() == 0) {
			brickMatrix[row][col] = null;
			numberOfBricks--;
			return points;
		}
		return 0;
	}

	/**
	 * @return the 2D matrix of Bricks
	 */
	public Brick[][] getMatrix() {
		return brickMatrix;
	}

	/**
	 * @param newMatrix
	 */
	public void setMatrix(Brick[][] newMatrix) {
		this.brickMatrix = newMatrix;
		updateDataforMatrix();
	}

	/**
	 * Updates data fields when loading new matrix
	 */
	private void updateDataforMatrix() {
		for(int row=0; row<brickMatrix.length;row++) {
			for (int col=0; col<brickMatrix[row].length; col++) {
				if (brickMatrix[row][col] != null // found a brick
						&& brickMatrix[row][col].getType() != BrickType.GOLD ) { // gold cannot not be destroyed 
					numberOfBricks++;
				}
			}
		}
	}

	/**
	 * @param row
	 * @return the Brick array for the row
	 */
	public Brick[] getRow(int row) {
		return brickMatrix[row];
	}

	/**
	 * @param row
	 * @param col
	 * @return the Brick at this cell
	 */
	public Brick getBrick(int row, int col) {
		if (row<0 || 
				col<0 ||
				row>=brickMatrix.length || 
				col>=brickMatrix[row].length) {
			return null;
		}
		return brickMatrix[row][col];
	}

	/**
	 * @param row
	 * @param col
	 * @param brick
	 */
	public void setBrick(int row, int col, Brick brick) {
		brickMatrix[row][col] = brick;
	}

	/**
	 * @return the brickGap
	 */
	public double getBrickGap() {
		return brickGap;
	}

	/**
	 * @param brickGap the brickGap to set
	 */
	public void setBrickGap(double brickGap) {
		this.brickGap = brickGap;
	}

	/**
	 * @return the brickWidth
	 */
	public double getPlayfieldWidth() {
		return playfieldWidth.get();
	}

	/**
	 * @return the brickHeight
	 */
	public double getPlayfieldHeight() {
		return playfieldHeight.get();
	}

	/**
	 * @return brickWidth
	 */
	public double getBrickWidth() {
		return brickWidth.get();
	}

	/**
	 * @return brickHeight
	 */
	public double getBrickHeight() {
		return brickHeight.get();
	}

	/**
	 * @return number of bricks left 
	 */
	public int getNumberOfBricks() {
		return numberOfBricks;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BrickLayout [brickGap=");
		builder.append(brickGap);
		builder.append(", playfieldWidth=");
		builder.append(playfieldWidth);
		builder.append(", playfieldHeight=");
		builder.append(playfieldHeight);
		builder.append(", brickMatrix=");
		builder.append(System.lineSeparator());
		for (int row=0; row<brickMatrix.length; row++) {
			for (int col=0; col<brickMatrix[row].length; col++) {
				if (brickMatrix[row][col] == null) {
					builder.append("---- ");					
				} else {
					builder.append(brickMatrix[row][col].toToken()).append(" ");
				}
			}
			builder.append(System.lineSeparator());
		}
		builder.append("]");
		return builder.toString();
	}
}
