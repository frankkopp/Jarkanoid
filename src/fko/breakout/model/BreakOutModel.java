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

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;

/**
 * BreakOutModel
 * 02.01.2018
 * @author Frank Kopp
 */
public class BreakOutModel {

	private static final double PADDLE_INITIAL_SPEED = 60.0; // framerate for paddle movements
	private static final Double PADDLE_MOVE_STEPS = 5.0; // steps per animation cycle
	
	/* 
	 * These valued determine the size and dimension of elements in Breakout.
	 * In normal MVC the View would use them to build the View elements. As we
	 * us JavaFX and FXML with Scene Builder we already have these values in the View.
	 * Therefore we need to duplicate the in den model anyway and make sure they stay
	 * synchronized. 
	 */
	
	// Playfield dimensions
	private DoubleProperty playfieldWidth = new SimpleDoubleProperty(780); // see FXML 800 - 2 * 10 Walls
	private DoubleProperty playfieldHeight = new SimpleDoubleProperty(510); // see FXML 520 - 1 * 10 Wall
	public DoubleProperty playfieldWidthProperty() {	return playfieldWidth; }
	public DoubleProperty playfieldHeightProperty() { return playfieldHeight; }
	
	// Paddle dimensions and position
	private DoubleProperty paddleWidth = new SimpleDoubleProperty(150); // see FXML
	private DoubleProperty paddleHeight = new SimpleDoubleProperty(20); // see FXML
	private DoubleProperty paddleX = new SimpleDoubleProperty(315); // see FXML
	private DoubleProperty paddleY = new SimpleDoubleProperty(475); // see FXML
	public DoubleProperty paddleHeightProperty() { return paddleHeight; }
	public DoubleProperty paddleWidthProperty() { return paddleWidth; }
	public DoubleProperty paddleXProperty() { return paddleX; }
	public DoubleProperty paddleYProperty() { return paddleY; }
	
	// Ball dimensions and position
	private DoubleProperty ballRadius = new SimpleDoubleProperty(8); // see FXML
	private DoubleProperty ballCenterX = new SimpleDoubleProperty(390); // see FXML
	private DoubleProperty ballCenterY = new SimpleDoubleProperty(260); // see FXML
	public DoubleProperty ballRadiusProperty() { return ballRadius; }
	public DoubleProperty ballCenterXProperty() { return ballCenterX; }
	public DoubleProperty ballCenterYProperty() { return ballCenterY; }
	
	// animations
	private Timeline paddleMovementTimeline = new Timeline();
	private Timeline ballMovementTimeline = new Timeline();;
	
	// called when key is pressed/released to indicate paddle movement to movement animation
	private boolean paddleLeft;
	private boolean paddleRight;
	public void setPaddleLeft(boolean b) { paddleLeft = b; }
	public void setPaddleRight(boolean b) { paddleRight = b; }
	
	public BreakOutModel() {
		
		// start the paddle movements
		paddleMovementTimeline.setCycleCount(Timeline.INDEFINITE);
		KeyFrame movePaddle = 
				new KeyFrame(Duration.seconds(1/PADDLE_INITIAL_SPEED), e -> { movePaddles();	});
		paddleMovementTimeline.getKeyFrames().add(movePaddle);
		paddleMovementTimeline.play();
	
	}

	/**
	 * Called by the <code>paddleMovementTimeline<code> animation event to move the paddles.
	 */
	private void movePaddles() {
		if (paddleLeft 
				&& paddleX.get() > 0.0) {
			paddleX.setValue(paddleX.getValue() - PADDLE_MOVE_STEPS);
		}
		if (paddleRight 
				&& paddleX.get() + paddleWidth.get() < playfieldWidth.get()) {
			paddleX.setValue(paddleX.getValue() + PADDLE_MOVE_STEPS);
		}
	}
	
	/**
	 * Called from controller by mouse move events. Moves the paddle according to the mouse's x position
	 * when mouse is in window. The paddle's center will be set to the current mouse position. 
	 * @param x
	 */
	public void setMouseXPosition(double x) {
		double halfPaddleWidth = paddleWidthProperty().get()/2;
		if (x - halfPaddleWidth < 0.0) {
			x = halfPaddleWidth;
		} else if (x + halfPaddleWidth > playfieldWidth.get()) {
			x = playfieldWidth.get() - halfPaddleWidth;
		}
		if (paddleX.get() >= 0.0 && paddleX.get() + paddleWidth.get() <= playfieldWidth.get()){
			paddleXProperty().set(x-halfPaddleWidth);
		}
	}

}
