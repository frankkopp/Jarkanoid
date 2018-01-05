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

import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import fko.breakout.events.GameEvent;
import fko.breakout.events.GameEvent.GameEventType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;

/**
 * BreakOutModel
 * 02.01.2018
 * @author Frank Kopp
 */
public class BreakOutGame extends Observable {

	private static final int START_LEVEL = 1;
	private static final int MAX_LEVEL 	= 2;
	
	private static final int START_LIVES = 5;
	private static final long SLEEP_BETWEEN_LIVES = 1000; // in ms

	private static final double PADDLE_INITIAL_FRAMERATE = 60.0; // Framerate for paddle movements
	private static final Double PADDLE_MOVE_STEPS = 5.0; // steps per animation cycle

	private static final double BALL_MAX_3ANGLE = 60;
	private static final int BALL_INITIAL_X = 390;
	private static final int BALL_INITIAL_Y = 450;
	private static final double BALL_INITIAL_FRAMERATE = 60.0;  // Framerate for ball movements
	private static final double BALL_INITIAL_SPEED = 5.0; // Absolute speed of ball - 

	private static final double BRICK_GAP = 2;
	

	// when vertical equals px in y
	// when horizontal equals px in x

	/* 
	 * These valued determine the size and dimension of elements in Breakout.
	 * In normal MVC the View would use them to build the View elements. As we
	 * us JavaFX and FXML with Scene Builder we already have these values in the View.
	 * Therefore we need to duplicate the in den model anyway and make sure they stay
	 * synchronized. 
	 */

	// Playfield dimensions
	private DoubleProperty playfieldWidth = new SimpleDoubleProperty(780); // see FXML 800 - 2 * 10 Walls
	private DoubleProperty playfieldHeight = new SimpleDoubleProperty(710); // see FXML 520 - 1 * 10 Wall
	public DoubleProperty playfieldWidthProperty() {	return playfieldWidth; }
	public DoubleProperty playfieldHeightProperty() { return playfieldHeight; }

	// Paddle dimensions and position
	private DoubleProperty paddleWidth = new SimpleDoubleProperty(150); // see FXML
	private DoubleProperty paddleHeight = new SimpleDoubleProperty(20); // see FXML
	private DoubleProperty paddleX = new SimpleDoubleProperty(315); // see FXML
	private DoubleProperty paddleY = new SimpleDoubleProperty(670); // see FXML
	public DoubleProperty paddleHeightProperty() { return paddleHeight; }
	public DoubleProperty paddleWidthProperty() { return paddleWidth; }
	public DoubleProperty paddleXProperty() { return paddleX; }
	public DoubleProperty paddleYProperty() { return paddleY; }

	// Ball dimensions and position
	private DoubleProperty ballRadius = new SimpleDoubleProperty(8); // see FXML
	private DoubleProperty ballCenterX = new SimpleDoubleProperty(BALL_INITIAL_X); // see FXML
	private DoubleProperty ballCenterY = new SimpleDoubleProperty(BALL_INITIAL_Y); // see FXML
	public DoubleProperty ballRadiusProperty() { return ballRadius; }
	public DoubleProperty ballCenterXProperty() { return ballCenterX; }
	public DoubleProperty ballCenterYProperty() { return ballCenterY; }

	// game status
	private ReadOnlyBooleanWrapper isPlaying = new ReadOnlyBooleanWrapper(false);
	public ReadOnlyBooleanProperty isPlayingProperty() { return isPlaying.getReadOnlyProperty(); }
	private ReadOnlyBooleanWrapper isPaused = new ReadOnlyBooleanWrapper(false);
	public ReadOnlyBooleanProperty isPausedProperty() { return isPaused.getReadOnlyProperty(); }
	private ReadOnlyBooleanWrapper gameOver = new ReadOnlyBooleanWrapper(false);
	public ReadOnlyBooleanProperty gameOverProperty() { return gameOver.getReadOnlyProperty(); }

	// game statistics
	private ReadOnlyIntegerWrapper currentLevel = new ReadOnlyIntegerWrapper(START_LEVEL);
	public ReadOnlyIntegerProperty currentLevelProperty() { return currentLevel.getReadOnlyProperty(); };
	private ReadOnlyIntegerWrapper currentRemainingLives = new ReadOnlyIntegerWrapper(START_LIVES);
	public ReadOnlyIntegerProperty currentRemainingLivesProperty() { return currentRemainingLives.getReadOnlyProperty(); };
	private ReadOnlyIntegerWrapper currentScore = new ReadOnlyIntegerWrapper(0);
	public ReadOnlyIntegerProperty currentScoreProperty() { return currentScore.getReadOnlyProperty(); };

	// animations
	private Timeline paddleMovementTimeline = new Timeline();
	private Timeline ballMovementTimeline = new Timeline();;

	// called when key is pressed/released to indicate paddle movement to movement animation
	private boolean paddleLeft;
	private boolean paddleRight;

	public void setPaddleLeft(boolean b) { paddleLeft = b; }
	public void setPaddleRight(boolean b) { paddleRight = b; }

	// ball speeds in each direction
	private double ball_vX = 1;
	private double ball_vY = BALL_INITIAL_SPEED;
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	
	private final BrickLayout brickLayout;

	/**
	 * Constructor
	 */
	public BreakOutGame() {
		
		// setup BrickLayout
		brickLayout = new BrickLayout(BRICK_GAP, playfieldWidth, playfieldHeight);
		
		// load first level
		brickLayout.setMatrix(LevelLoader.getInstance().getLevel(1));

		// start the paddle movements
		paddleMovementTimeline.setCycleCount(Timeline.INDEFINITE);
		KeyFrame movePaddle = 
				new KeyFrame(Duration.seconds(1/PADDLE_INITIAL_FRAMERATE), e -> { movePaddles();	});
		paddleMovementTimeline.getKeyFrames().add(movePaddle);
		paddleMovementTimeline.play();

		// prepare ball movements (will be start in startGame())
		ballMovementTimeline.setCycleCount(Timeline.INDEFINITE);
		KeyFrame moveBall = 
				new KeyFrame(Duration.seconds(1/BALL_INITIAL_FRAMERATE), e -> {	moveBall();	});
		ballMovementTimeline.getKeyFrames().add(moveBall);
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
	 * Called by the <code>ballMovementTimeline</code> animation event to move the ball.
	 */
	private void moveBall() {
		ballCenterX.set(ballCenterX.get() + ball_vX);
		ballCenterY.set(ballCenterY.get() + ball_vY);
		checkCollision();
	}

	/**
	 * Checks if the ball has hit a wall, the paddle, a block or has left 
	 * through the bottom. Calculates new speeds for each direction or calls
	 * <code>ballLost()</code> when ball has left through bottom.
	 */
	private void checkCollision() {
		
		// convenience variables 
		final double ballUpperBound = ballCenterY.get() - ballRadius.get();
		final double ballLowerBound = ballCenterY.get() + ballRadius.get();
		final double ballLeftBound = ballCenterX.get() - ballRadius.get();
		final double ballRightBound = ballCenterX.get() + ballRadius.get();

		final double paddleUpperBound = paddleY.get();
		final double paddleLowerBound = paddleY.get() + paddleHeight.get();
		final double paddleLeftBound = paddleX.get();
		final double paddleRightBound = paddleX.get() + paddleWidth.get();

		// hit wall left or right
		if (ballLeftBound <= 0) { // left
			ballCenterX.set(0+ballRadius.get()); // in case it was <0
			ball_vX *= -1;
			setChanged();
			notifyObservers(new GameEvent(GameEventType.HIT_WALL));
			return;
		}
		if (ballRightBound >= playfieldWidth.get()) { // right
			ballCenterX.set(playfieldWidth.get()-ballRadius.get()); // in case it was >playFieldWidth
			ball_vX *= -1;
			setChanged();
			notifyObservers(new GameEvent(GameEventType.HIT_WALL));
			return;
		}

		// hit wall top
		if (ballUpperBound <= 0) {
			ball_vY *= -1;
			setChanged();
			notifyObservers(new GameEvent(GameEventType.HIT_WALL));
			return;
		}

		// hit paddle
		if (ballLowerBound >= paddleUpperBound && ballLowerBound <= paddleLowerBound) {
			if (ballRightBound > paddleLeftBound && ballLeftBound < paddleRightBound) {

				increaseScore(1); // TODO: just for DEBUG

				// determine where the ball hit the paddle
				double hitPointAbsolute = ballCenterX.get() - paddleLeftBound;
				// normalize value to -1 (left), 0 (center), +1 (right)
				double hitPointRelative = 2 * ((hitPointAbsolute / paddleWidth.get()) - 0.5);
				// determine new angle
				double newAngle = hitPointRelative * BALL_MAX_3ANGLE;

				ball_vX = Math.sin(Math.toRadians(newAngle)) * BALL_INITIAL_SPEED;
				ball_vY = -Math.cos(Math.toRadians(newAngle)) * BALL_INITIAL_SPEED;

				setChanged();
				notifyObservers(new GameEvent(GameEventType.HIT_PADDLE));
				return;
			}
		}

		// hit brick
		// TODO: hit brick

		// lost through bottom
		if (ballUpperBound >= playfieldHeight.get()) {

			if (decreaseRemainingLives() < 0) {
				currentRemainingLives.set(0);
				gameOver();
				setChanged();
				notifyObservers(new GameEvent(GameEventType.GAME_OVER));
				return;
			};

			setChanged();
			notifyObservers(new GameEvent(GameEventType.BALL_LOST));

			// pause animation
			ballMovementTimeline.pause();

			// start new round
			startRound();
		}

	}

	private void gameOver() {
		stopPlaying();
		gameOver.set(true);
	}

	private void startRound() {
		// reset the ball position and speed
		resetBall();
		// show the ball for a short time then start the animation
		executor.schedule(() -> {
			if (!isPlaying()) return; // maybe the game has already been stopped
			ballMovementTimeline.play(); 
		}, SLEEP_BETWEEN_LIVES, TimeUnit.MILLISECONDS);
	}

	private void resetBall() {
		// reset ball speed and direction (straight down)
		ball_vX = 0;
		ball_vY = BALL_INITIAL_SPEED;

		// reset ball position
		ballCenterX.set(BALL_INITIAL_X);
		ballCenterY.set(BALL_INITIAL_Y);
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

	public void startPlaying() {
		isPlaying.set(true);
		isPaused.set(false);
		gameOver.set(false);

		// initialize new game
		currentLevel.set(START_LEVEL);
		currentRemainingLives.set(START_LIVES);
		currentScore.set(0);

		setChanged();
		notifyObservers(new GameEvent(GameEventType.GAME_START));

		// start the ball movement
		startRound();


	}
	public void stopPlaying() {
		isPlaying.set(false);
		isPaused.set(false);
		gameOver.set(false);
		// stop ball movement
		ballMovementTimeline.stop();
	}

	public boolean isPlaying() {
		return isPlaying.get();
	}

	public void pausePlaying() {
		if (!isPlaying()) return; // ignore if not playing
		isPaused.set(true);
		ballMovementTimeline.pause();
	}

	public void resumePlaying() {
		if (!isPlaying() && !isPaused()) return; // ignore if not playing
		isPaused.set(false);
		ballMovementTimeline.play();

	}

	public boolean isPaused() {
		return isPaused.get();
	}

	private int increaseScore(int i) { 
		currentScore.set(currentScore.get()+i); 
		return currentScore.get();
	};

	private int decreaseRemainingLives() {
		currentRemainingLives.set(currentRemainingLives.get() - 1);
		return currentRemainingLives.get();
	}
}
