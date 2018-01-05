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
 * A <code>BrickLayout</code> holds a 18x11 matrix of <code>Brick</code>s. Bricks will be erased after 
 * they have been hit by the ball the necessary number of times.<br>
 * The matrix is top down - row 0 is the uppermost row.<br>
 * This class also calculates the measurements and positions of bricks based on the playfield size
 * and a gap between the Bricks.<br>
 * It also checks for collision when given a ball with a center and radius.<br>
 * 
 * <p>
 * 04.01.2018
 * @author Frank Kopp
 */
public class BrickLayout {

	public static final int ROWS = 18;
	public static final int COLUMNS = 11;
	
	private double brickGap;
	private DoubleProperty playfieldWidth;
	private DoubleProperty playfieldHeight;

	private Brick[][] brickMatrix = new Brick[ROWS][COLUMNS];
	
	private DoubleProperty brickWidth = new SimpleDoubleProperty();;
	private DoubleProperty brickHeight = new SimpleDoubleProperty();;
	
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

		for(int rows=0; rows<brickMatrix.length;rows++) {
			for (int cols=0; cols<brickMatrix[rows].length; cols++) {
				brickMatrix[rows][cols] = null;
			}
		}
		
		// calculated bind of brick size to playfield size
		brickWidth.bind((playfieldWidth
				.subtract((COLUMNS+1)*brickGap))
				.divide(COLUMNS));
		brickHeight.bind((playfieldHeight
				.subtract(playfieldHeight.get()*0.5)
				.subtract((ROWS+1)*brickGap))
				.divide(ROWS));
				
	}

	public Brick[][] getMatrix() {
		return brickMatrix;
	}
	
	public void setMatrix(Brick[][] newMatrix) {
		this.brickMatrix = newMatrix;
	}

	public Brick[] getRow(int row) {
		return brickMatrix[row];
	}

	public Brick getBrick(int row, int col) {
		return brickMatrix[row][col];
	}

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
	 * @return
	 */
	public double getBrickWidth() {
		return brickWidth.get();
	}

	/**
	 * @return
	 */
	public double getBrickHeight() {
		return brickHeight.get();
	}

	/* (non-Javadoc)
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
		for (int row=0; row<brickMatrix.length; row++) {
			for (int col=0; col<brickMatrix[row].length; col++) {
				if (brickMatrix[row][col] == null) {
					builder.append("---- ");					
				} else {
					builder.append(brickMatrix[row][col].toShortString()).append(" ");
				}
			}
			builder.append(System.lineSeparator());
		}
		builder.append("]");
		return builder.toString();
	}

}
