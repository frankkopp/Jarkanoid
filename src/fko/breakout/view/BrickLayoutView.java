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
package fko.breakout.view;

import fko.breakout.model.BrickLayout;
import javafx.scene.Group;

/**
 * BrickLayoutView
 * <p>
 * Extends <code>Group</code> to cluster all Bricks into one group which will be added
 * to the main view.
 * <p>
 * 05.01.2018
 * @author Frank Kopp
 */
public class BrickLayoutView extends Group {
	
	// store of created BrickViews to be able to selectively delete them
	private BrickView[][] allBricks;
	
	/**
	 * Creates empty BrickLayout
	 */
	public BrickLayoutView() {
		super();
	}

	/**
	 * draws the BrickView. Bricks are removed selectively by using a storage array.
	 * @param brickLayout
	 */
	public void draw(BrickLayout brickLayout) {
		
		// we might need to do this more selective to save time
		if (brickLayout == null) {
			this.getChildren().clear();
			return;
		} 
		 
		// create a store for all BrickViews
		if (allBricks == null) {
			allBricks = new BrickView[BrickLayout.ROWS][BrickLayout.COLUMNS];
		}
		
		// readability
		final double vGap = brickLayout.getBrickGap();
		final double hGap = brickLayout.getBrickGap();
		final double brickWidth = brickLayout.getBrickWidth();
		final double brickHeight = brickLayout.getBrickHeight(); 

		// add all bricks according to the model brickLayout
		for (int row=0; row<BrickLayout.ROWS; row++) {
			for (int col=0; col<BrickLayout.COLUMNS; col++) {
				double y = vGap + (row*vGap) + row * brickHeight;
				double x = hGap + (col*hGap) + col * brickWidth;
				
				// brick exists in model but not in view
				if (brickLayout.getBrick(row, col) != null
						&& allBricks[row][col] == null) {
					
					allBricks[row][col] = new BrickView(x, y, 
							brickWidth, brickHeight,brickLayout.getBrick(row, col));
					this.getChildren().add(allBricks[row][col]);

				// brick exists in view but not in model - erase BrickView
				} else if (brickLayout.getBrick(row, col) == null
						&& allBricks[row][col] != null) {
					
					this.getChildren().remove(allBricks[row][col]);
					allBricks[row][col] = null;

				} 
			} // end for col
		} //end for row
	}

	/**
	 * @param row
	 * @param col
	 * @return
	 */
	public BrickView getBrickView(int row, int col) {
		if (row<0 || col<0 || row>=allBricks.length || col>=allBricks[row].length) {
			return null;
		}
		return allBricks[row][col];
	}
}
