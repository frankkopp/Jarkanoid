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

import javafx.scene.paint.Color;

/**
 * BrickType
 * 04.01.2018
 * @author Frank Kopp
 */
public enum BrickType {

	GREY 	(50,  1, Color.GREY),
	ORANGE 	(60,  1, Color.ORANGE),
	CYAN 	(70,  1, Color.CYAN),
	GREEN	(80,  1, Color.GREEN),
	RED		(90,  1, Color.RED),
	BLUE		(100, 1, Color.BLUE),
	PURPLE	(110, 1, Color.PURPLE),
	YELLOW	(120, 1, Color.YELLOW),
	SILVER	(150, 3, Color.SILVER),
	GOLD		(0, Integer.MAX_VALUE, Color.GOLD);
	
	public final int points;
	public final int hits;
	public final Color color;

	private BrickType(int points, int hits, Color color) {
		this.points = points;
		this.hits = hits;
		this.color = color;
	}
}
