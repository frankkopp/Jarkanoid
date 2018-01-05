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

import fko.breakout.model.Brick;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * BrickVew
 * 05.01.2018
 * @author Frank Kopp
 */
public class BrickView extends Rectangle {

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param brick 
	 */
	public BrickView(double x, double y, double width, double height, Brick brick) {
		super(x, y, width, height);
		
		this.setFill(brick.getColor());
		
		Effect r = new javafx.scene.effect.InnerShadow(
				5.0,
				1.0,
				1.0,
				Color.WHITE
				); // DropShadow(); // new Reflection();
		
		this.setEffect(r);
	
	}

}