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
 * <p>
 * Handles the BreakOut game status, the main game loop and calculations.<br>
 * <p>
 * It has not yet its own Thread - could become necessary later if performance/rendering issue occur. 
 * <p>
 * 02.01.2018
 * @author Frank Kopp
 * TODO: make ball and paddle objects
 * TODO: add option handle multiple balls
 * TODO: add separate thread?? maybe not useful for a frame based game
 * TODO: Refactor game status - can be handle through gameLoop.status 
 *          / or not as loop.status is not boolean which impacts easy binding in view
 * FIXME: ball can get stuck in GOLD bricks 
 * FIXME: ball sometime bounces off bricks in the wrong direction
 */
public class BreakOutGame extends Observable {

  private static final int 	START_LEVEL = 3;
  private static final int 	START_LIVES = 5;
  private static final long SLEEP_BETWEEN_LIVES  = 2000; // in ms
  private static final long SLEEP_BETWEEN_LEVELS = 3000; // in ms

  private static final double PLAYFIELD_INITIAL_HEIGHT = 710;
  private static final double PLAYFIELD_INITIAL_WIDTH = 780;

  private static final double PADDLE_INITIAL_FRAMERATE = 100.0; // Framerate for paddle movements
  private static final double PADDLE_MOVE_STEPS = 5.0; // steps per animation cycle
  private static final double PADDLE_INITIAL_Y = 670;
  private static final double PADDLE_INITIAL_X = 315;
  private static final double PADDEL_INITIAL_WIDTH = 150;
  private static final double PADDLE_INITIAL_HEIGHT = 20;

  private static final double BALL_INITIAL_RADIUS = 8;
  private static final double BALL_MAX_3ANGLE = 60;
  private static final double BALL_INITIAL_X = 390;
  private static final double BALL_INITIAL_Y = PADDLE_INITIAL_Y-BALL_INITIAL_RADIUS;
  private static final double BALL_INITIAL_FRAMERATE = 100.0;  // Framerate for ball movements

  // Absolute speed of ball 
  // when vertical equals px in y
  // when horizontal equals px in x
  private static final double BALL_INITIAL_SPEED = 5.0; 

  private static final double BRICK_GAP = 2;

  /* 
   * These values determine the size and dimension of elements in Breakout.
   * In normal MVC the View would use them to build the View elements. As we
   * us JavaFX and FXML with Scene Builder these values are already set by the FXML.
   * Therefore we duplicate them in den model and make sure they stay synchronized through 
   * property bindings. 
   */

  // Playfield dimensions
  private DoubleProperty playfieldWidth = new SimpleDoubleProperty(PLAYFIELD_INITIAL_WIDTH); // see FXML 800 - 2 * 10 Walls
  private DoubleProperty playfieldHeight = new SimpleDoubleProperty(PLAYFIELD_INITIAL_HEIGHT); // see FXML 520 - 1 * 10 Wall
  public DoubleProperty playfieldWidthProperty() {	return playfieldWidth; }
  public DoubleProperty playfieldHeightProperty() { return playfieldHeight; }

  // Paddle dimensions and position
  private DoubleProperty paddleWidth = new SimpleDoubleProperty(PADDEL_INITIAL_WIDTH); // see FXML
  private DoubleProperty paddleHeight = new SimpleDoubleProperty(PADDLE_INITIAL_HEIGHT); // see FXML
  private DoubleProperty paddleX = new SimpleDoubleProperty(PADDLE_INITIAL_X); // see FXML
  private DoubleProperty paddleY = new SimpleDoubleProperty(PADDLE_INITIAL_Y); // see FXML
  public DoubleProperty paddleHeightProperty() { return paddleHeight; }
  public DoubleProperty paddleWidthProperty() { return paddleWidth; }
  public DoubleProperty paddleXProperty() { return paddleX; }
  public DoubleProperty paddleYProperty() { return paddleY; }

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
  
  // main Game Loop / moves ball(s) and handles collisions
  private Timeline mainGameLoop = new Timeline();

  // paddle movements have their own game loop so we can move it outside of a running game
 private Timeline paddleMovementLoop = new Timeline();
 
  // called when key is pressed/released to indicate paddle movement to movement animation
  private boolean paddleLeft;
  private boolean paddleRight;
  public void setPaddleLeft(boolean b) { paddleLeft = b; }
  public void setPaddleRight(boolean b) { paddleRight = b; }
  
  // used to delay starts of game
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  // the brick layout holds all bricks and its positions of the games
  private final BrickLayout brickLayout;

  /**
   * Constructor - prepares the brick layout and the game loops.
   */
  public BreakOutGame() {

    // setup BrickLayout
    brickLayout = new BrickLayout(BRICK_GAP, playfieldWidth, playfieldHeight);

    // start the paddle movements
    paddleMovementLoop.setCycleCount(Timeline.INDEFINITE);
    KeyFrame movePaddle = 
        new KeyFrame(Duration.seconds(1/PADDLE_INITIAL_FRAMERATE), e -> { paddleMovementLoop();	});
    paddleMovementLoop.getKeyFrames().add(movePaddle);
    paddleMovementLoop.play();

    // prepare ball movements (will be start in startGame())
    mainGameLoop.setCycleCount(Timeline.INDEFINITE);
    KeyFrame moveBall = 
        new KeyFrame(Duration.seconds(1/BALL_INITIAL_FRAMERATE), e -> {	gameLoop();	});
    mainGameLoop.getKeyFrames().add(moveBall);

  }


  /**
   * @param i
   */
  private void loadLevel(int level) {
    // pause animation
    mainGameLoop.pause();

    // load next level or game is won if non available
    final Brick[][] newLevel = LevelLoader.getInstance().getLevel(level);
    if (newLevel == null) { 
      gameOver();
      setChanged();
      notifyObservers(new GameEvent(GameEventType.GAME_WON));
      return;
    };

    // set the received level into the brickLayout
    brickLayout.setMatrix(newLevel);

    // Level done
    setChanged();
    notifyObservers(new GameEvent(GameEventType.LEVEL_START));
  }
 
  /**
   * Starts a new round after loosing a ball or completing a level
   * @param pause
   */
  private void startRound(long pause) {
    // reset the ball position and speed
    resetBallSpeed();
    // move the ball with the paddle before start of game
    bindBallToPaddle();
    // show the ball for a short time then start the animation
    executor.schedule(() -> {
      if (!isPlaying()) return; // check if the game has been stopped while we were waiting
      ballCenterX.unbind();  // unbind the ball from the paddle
      ballCenterY.unbind();  // unbind the ball from the paddle
      mainGameLoop.play(); // start the gameLoop
    }, pause, TimeUnit.MILLISECONDS);
  }
  
  /**
   * Binds the ball to the paddle movement before start of the game
   */
  private void bindBallToPaddle() {
    // bind ball to paddle
    ballCenterX.bind(paddleX.add(paddleWidth.divide(2)).add(20)); // slightly to the right of the middle
    ballCenterY.bind(paddleY.subtract(ballRadius).subtract(1.0));
  }

  /**
   * Called by the <code>paddleMovementTimeline<code> animation event to move the paddles.
   */
  private void paddleMovementLoop() {
    if (isPaused()) return; // no paddle movement when game is pausend
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
   * Called by the <code>mainGameLoop</code> animation event to move the ball.<br>
   * Calls <code>checkCollision()</code>
   */
  private void gameLoop() {
    // move the ball 
    // TODO move ball    
    // check collisions from the ball(s) with anything else
    checkCollision();
  }

  /**
   * Checks if the ball(s) have hit a wall, the paddle, a block or has left 
   * through the bottom. Calculates new speeds for each direction, tells brickLayout 
   * if the ball hits a brick and calls <code>ballLost()</code> when ball has left 
   * through bottom.
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
    checkSideWallCollision(ballLeftBound, ballRightBound);

    // hit wall top
    checkTopWallCollision(ballUpperBound);

    // hit brick
    checkBrickCollision(ballUpperBound, ballLowerBound, ballLeftBound, ballRightBound);

    // hit paddle
    checkPaddleCollision(ballLowerBound, ballLeftBound, ballRightBound, paddleUpperBound, paddleLowerBound,
        paddleLeftBound, paddleRightBound);

    // lost through bottom
    checkBallLostThroughBottom(ballUpperBound);
  }

  /**
   * @param ballLeftBound
   * @param ballRightBound
   * @return true if ball touches one of the side wall
   */
  private void checkSideWallCollision(final double ballLeftBound, final double ballRightBound) {
    if (ballLeftBound <= 0) { // left
      ballCenterX.set(0+ballRadius.get()); // in case it was <0
      vXball *= -1;
      setChanged();
      notifyObservers(new GameEvent(GameEventType.HIT_WALL));
    } else if (ballRightBound >= playfieldWidth.get()) { // right
      ballCenterX.set(playfieldWidth.get()-ballRadius.get()); // in case it was >playFieldWidth
      vXball *= -1;
      setChanged();
      notifyObservers(new GameEvent(GameEventType.HIT_WALL));
    }
  }
  
  /**
   * @param ballUpperBound
   * @return true if ball touches top wall
   */
  private void checkTopWallCollision(final double ballUpperBound) {
    if (ballUpperBound <= 0) {
      ballCenterY.set(ballRadius.get()); // in case it was <0
      vYball *= -1;
      setChanged();
      notifyObservers(new GameEvent(GameEventType.HIT_WALL));
    }
  }
  
  /**
   * @param ballUpperBound
   * @param ballLowerBound
   * @param ballLeftBound
   * @param ballRightBound
   */
  private void checkBrickCollision(final double ballUpperBound, final double ballLowerBound,
      final double ballLeftBound, final double ballRightBound) {

    // calculate ball edge's brick cell
    int ballCenterRow = (int) (ballCenterYProperty().get() 
        / (brickLayout.getBrickHeight()+brickLayout.getBrickGap()));
    int ballCenterCol = (int) (ballCenterXProperty().get() 
        / (brickLayout.getBrickWidth()+brickLayout.getBrickGap()));

    int ballUpperRow = (int) (ballUpperBound 
        / (brickLayout.getBrickHeight()+brickLayout.getBrickGap()));
    int ballLowerRow = (int) ((ballLowerBound - brickLayout.getBrickGap())
        / (brickLayout.getBrickHeight()+brickLayout.getBrickGap()));
    int ballLeftCol = (int) (ballLeftBound 
        / (brickLayout.getBrickWidth() + brickLayout.getBrickGap()));	
    int ballRightCol = (int) ((ballRightBound - brickLayout.getBrickGap())
        / (brickLayout.getBrickWidth() + brickLayout.getBrickGap()));	

    // hit above
    if (brickLayout.getBrick(ballUpperRow, ballCenterCol) != null) {
      currentScore.set(currentScore.get() + brickLayout.hitBrick(ballUpperRow, ballCenterCol));
      vYball *= -1;
      setChanged();
      notifyObservers(new GameEvent(GameEventType.HIT_BRICK, ballUpperRow, ballCenterCol));
    }
    // hit below
    if (brickLayout.getBrick(ballLowerRow, ballCenterCol) != null) {
      currentScore.set(currentScore.get() + brickLayout.hitBrick(ballLowerRow, ballCenterCol));
      vYball *= -1;
      setChanged();
      notifyObservers(new GameEvent(GameEventType.HIT_BRICK, ballLowerRow, ballCenterCol));
    }
    // hit left
    if (brickLayout.getBrick(ballCenterRow, ballLeftCol) != null) {
      currentScore.set(currentScore.get() + brickLayout.hitBrick(ballCenterRow, ballLeftCol));
      vXball *= -1;
      setChanged();
      notifyObservers(new GameEvent(GameEventType.HIT_BRICK, ballCenterRow, ballLeftCol));
    }
    // hit right
    if (brickLayout.getBrick(ballCenterRow, ballRightCol) != null) {
      increaseScore(brickLayout.hitBrick(ballCenterRow, ballRightCol));
      vXball *= -1;
      setChanged();
      notifyObservers(new GameEvent(GameEventType.HIT_BRICK, ballCenterRow, ballRightCol));
    }

    if (brickLayout.getNumberOfBricks() == 0) {
      // Level done
      setChanged();
      notifyObservers(new GameEvent(GameEventType.LEVEL_COMPLETE));

      // load new level or game over WON
      increaseLevel();
      loadLevel(currentLevel.get());
      startRound(SLEEP_BETWEEN_LEVELS);

    }
  }

  /**
   * @param ballLowerBound
   * @param ballLeftBound
   * @param ballRightBound
   * @param paddleUpperBound
   * @param paddleLowerBound
   * @param paddleLeftBound
   * @param paddleRightBound
   * @return true if ball touches paddle
   */
  private void checkPaddleCollision(final double ballLowerBound, final double ballLeftBound,
      final double ballRightBound, final double paddleUpperBound, final double paddleLowerBound,
      final double paddleLeftBound, final double paddleRightBound) {

    if ((ballLowerBound >= paddleUpperBound && ballLowerBound <= paddleLowerBound) // ball on correct height
        && (ballRightBound > paddleLeftBound && ballLeftBound < paddleRightBound)) { // ball touches the paddle

      // determine where the ball hit the paddle
      double hitPointAbsolute = ballCenterX.get() - paddleLeftBound;
      // normalize value to -1 (left), 0 (center), +1 (right)
      double hitPointRelative = 2 * ((hitPointAbsolute / paddleWidth.get()) - 0.5);
      // determine new angle
      double newAngle = hitPointRelative * BALL_MAX_3ANGLE;

      vXball = Math.sin(Math.toRadians(newAngle)) * BALL_INITIAL_SPEED;
      vYball = -Math.cos(Math.toRadians(newAngle)) * BALL_INITIAL_SPEED;

      setChanged();
      notifyObservers(new GameEvent(GameEventType.HIT_PADDLE));
    }
}

/**
   * @param ballUpperBound
   */
  private void checkBallLostThroughBottom(final double ballUpperBound) {
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
      mainGameLoop.pause();
  
      // start new round
      startRound(SLEEP_BETWEEN_LIVES);
    }
  }
/**
 * called when out of lives or after last level
 */
private void gameOver() {
  stopPlaying();
  gameOver.set(true);
}

/**
 * resets the ball's location and speed
 */
private void resetBallSpeed() {
  // reset ball speed and direction (straight down)
  vXball = 0;
  vYball = BALL_INITIAL_SPEED;
}

/**
 * 
 */
private void increaseLevel() {
  currentLevel.set(currentLevel.get() + 1);
}
/**
 * increasing the score after hitting a brick
 * @param i
 * @return
 */
private int increaseScore(int i) { 
  currentScore.set(currentScore.get()+i); 
  return currentScore.get();
}

/**
 * decreases the remaining lives after loosing a ball
 * @return remaining lives
 */
private int decreaseRemainingLives() {
  currentRemainingLives.set(currentRemainingLives.get() - 1);
  return currentRemainingLives.get();
}

/**
 * Called from controller by mouse move events. Moves the paddle according to the mouse's x position
 * when mouse is in window. The paddle's center will be set to the current mouse position. 
 * @param x
 */
public void setMouseXPosition(double mouseX) {
  if (isPaused()) return; 
  double x = mouseX;
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

/**
 * Starts a new game.
 */
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

  loadLevel(currentLevel.get());
  startRound(SLEEP_BETWEEN_LIVES);
}

/**
 * stops the current game
 */
public void stopPlaying() {
  isPlaying.set(false);
  isPaused.set(false);
  gameOver.set(false);
  // stop game loop
  mainGameLoop.stop();
}

/**
 * pauses a running game 
 */
public void pausePlaying() {
  if (!isPlaying()) return; // ignore if not playing
  isPaused.set(true);
  mainGameLoop.pause();
}

/**
 * resumes a paused running game
 */
public void resumePlaying() {
  if (!isPlaying() && !isPaused()) return; // ignore if not playing
  isPaused.set(false);
  mainGameLoop.play();
}

/**
 * @return true of game is running
 */
public boolean isPlaying() {
  return isPlaying.get();
}
/**
 * @return true if game is paused
 */
public boolean isPaused() {
  return isPaused.get();
}

/**
 * @return the current brick layout
 */
public BrickLayout getBrickLayout() {
  return brickLayout;
}

}
