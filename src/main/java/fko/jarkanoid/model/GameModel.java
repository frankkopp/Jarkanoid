/**
 * MIT License
 *
 * <p>Copyright (c) 2018 Frank Kopp
 *
 * <p>Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * <p>The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * <p>THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package fko.jarkanoid.model;

import fko.jarkanoid.events.GameEvent;
import fko.jarkanoid.events.GameEvent.GameEventType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * BreakOutModel
 *
 * <p>Handles the BreakOut game status, the main game loop and calculations.<br>
 *
 * <p>It has not yet its own Thread - could become necessary later if performance/rendering issue
 * occur.
 *
 * @author Frank Kopp
 */
public class GameModel extends Observable {

  // TODO: add acceleration
  // TODO: create all main.resources.levels
  // TODO: improve powers / animation etc.
  // TODO: Highscore
  // FIXME: ball caught

  /*
   * Constants for game dimensions and other relevant settings.
   * Need to be aligned with FXML UI Design.
   */
  private static final int START_LEVEL = 1;
  private static final int START_LIVES = 3;

  private static final long SLEEP_BETWEEN_LIVES = 2000; // in ms
  private static final long SLEEP_BETWEEN_LEVELS = 3000; // in ms

  private static final double PLAYFIELD_INITIAL_HEIGHT = 710;
  private static final double PLAYFIELD_INITIAL_WIDTH = 780;

  // paddle constants
  private static final double PADDLE_INITIAL_FRAMERATE = 100; // Framerate for paddle movements
  private static final double PADDLE_MOVE_STEPS = 5.0; // steps per animation cycle
  private static final double PADDLE_INITIAL_Y = 670;
  private static final double PADDLE_INITIAL_X = 315;
  private static final double PADDEL_INITIAL_WIDTH = 150;
  private static final double PADDLE_INITIAL_HEIGHT = 20;
  private static final float PADDLE_ENLARGEMENT_FACTOR = 1.4f;

  // Ball constants
  private static final double BALL_INITIAL_RADIUS = 8;
  private static final double BALL_MAX_ANGLE = 60;
  private static final double BALL_INITIAL_X = 390;
  private static final double BALL_INITIAL_Y = PADDLE_INITIAL_Y - BALL_INITIAL_RADIUS;
  private static final double BALL_INITIAL_FRAMERATE = 60; // Framerate for ball movements
  // Absolute speed of ball, when vertical equals px in y, when horizontal equals px in x
  private static final double BALL_INITIAL_SPEED = 10.0;

  // Laser constants
  private static final double LASER_EDGE_OFFSET = 45;
  private static final double LASER_WIDTH = 5;
  private static final double LASER_HEIGHT = 15;
  private static final double LASER_SPEED = 15;

  // Template of a starting ball to copy when a new level starts
  private static final Ball BALL_TEMPLATE =
      new Ball(BALL_INITIAL_X, BALL_INITIAL_Y, BALL_INITIAL_RADIUS, 0, BALL_INITIAL_SPEED);

  // power up constants
  // how many destroyed bricks between power ups (needs to be >0)
  private static final int NEXT_POWERUP_OFFSET = 3;
  // power up randomly after 0 to 10 destroyed bricks after offset
  private static final int POWER_UP_FREQUENCY = 10;

  /*
   * These values determine the size and dimension of elements in Breakout.
   * In normal MVC the View would use them to build the View elements. As we
   * us JavaFX and FXML with Scene Builder these values are already set by the FXML.
   * Therefore we duplicate them in den model and make sure they stay synchronized through
   * property bindings.
   */

  // Playfield dimensions
  private final DoubleProperty playfieldWidth =
      new SimpleDoubleProperty(PLAYFIELD_INITIAL_WIDTH); // see FXML 800 - 2 * 10 Walls
  private final DoubleProperty playfieldHeight =
      new SimpleDoubleProperty(PLAYFIELD_INITIAL_HEIGHT); // see FXML 520 - 1 * 10 Wall

  // Paddle dimensions and position
  private final DoubleProperty paddleWidth =
      new SimpleDoubleProperty(PADDEL_INITIAL_WIDTH); // see FXML
  private final DoubleProperty paddleHeight =
      new SimpleDoubleProperty(PADDLE_INITIAL_HEIGHT); // see FXML
  private final DoubleProperty paddleX = new SimpleDoubleProperty(PADDLE_INITIAL_X); // see FXML
  private final DoubleProperty paddleY = new SimpleDoubleProperty(PADDLE_INITIAL_Y); // see FXML

  // game status
  private final ReadOnlyBooleanWrapper isPlaying = new ReadOnlyBooleanWrapper(false);
  private final ReadOnlyBooleanWrapper isPaused = new ReadOnlyBooleanWrapper(false);
  private final ReadOnlyBooleanWrapper gameOver = new ReadOnlyBooleanWrapper(false);

  // game statistics
  private final ReadOnlyIntegerWrapper currentLevel = new ReadOnlyIntegerWrapper(START_LEVEL);
  private final ReadOnlyIntegerWrapper currentRemainingLives =
      new ReadOnlyIntegerWrapper(START_LIVES);
  private final ReadOnlyIntegerWrapper currentScore = new ReadOnlyIntegerWrapper(0);

  // ball manager
  private final ListProperty<Ball> ballManager = new SimpleListProperty<>();

  // LaserShot manager
  private final ListProperty<LaserShot> laserShotManager = new SimpleListProperty<>();

  // main Game Loop / moves ball(s) and handles collisions
  private final Timeline mainGameLoop = new Timeline();

  // paddle movements have their own game loop so we can move it outside of a running game
  private final Timeline paddleMovementLoop = new Timeline();

  // used to delay starts of game
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  // the brick layout holds all bricks and its positions of the games
  private final BrickLayout brickLayout;

  // called when key is pressed/released to indicate paddle movement to movement animation
  private boolean paddleLeft;
  private boolean paddleRight;;
  private ScheduledFuture scheduledStart;

  // signals the game loop to split the ball into multiple balls
  private boolean splitBallFlag = false;;

  // count all destroyed bricks
  private int destroyedBricksCounter = 0;

  // power ups
  private int lastPowerUp = 0;
  private int nextPowerUp = getNextPowerUp();
  private PowerPill nextPowerPill;
  private final ListProperty<PowerPill> fallingPowerPills = new SimpleListProperty<>();
  private final ObjectProperty<PowerPillType> activePower =
      new SimpleObjectProperty<PowerPillType>(PowerPillType.NONE);
  private boolean ballCatchedFlag = false;

  // count each time hte game loop is called
  private long frameLoopCounter = 0;
  private long frameLoopCounterTimeStamp = System.nanoTime();
  private long lastloopTime;
  private long commulativeLoopTime;
  private final DoubleProperty fps = new SimpleDoubleProperty(BALL_INITIAL_FRAMERATE);;

  /** Constructor - prepares the brick layout and the game loops. */
  public GameModel() {

    // setup BrickLayout
    brickLayout = new BrickLayout(playfieldWidth, playfieldHeight);

    // configure ballManager
    ballManager.set(FXCollections.observableList(new ArrayList<>(3)));

    // configure laserShotManager
    laserShotManager.set(FXCollections.observableList(new ArrayList<>()));

    // configure fallingPower list
    fallingPowerPills.set(FXCollections.observableList(new ArrayList<>()));

    // start the paddle movements
    paddleMovementLoop.setCycleCount(Timeline.INDEFINITE);
    KeyFrame movePaddle =
        new KeyFrame(Duration.seconds(1.0 / PADDLE_INITIAL_FRAMERATE), e -> paddleMovementLoop());
    paddleMovementLoop.getKeyFrames().add(movePaddle);
    paddleMovementLoop.play();

    // prepare ball movements (will be start in startGame())
    mainGameLoop.setCycleCount(Timeline.INDEFINITE);
    KeyFrame moveBall =
        new KeyFrame(Duration.seconds(1.0 / BALL_INITIAL_FRAMERATE), e -> gameLoop());
    mainGameLoop.getKeyFrames().add(moveBall);
  }

  /** Starts a new game. */
  public void startPlaying() {
    if (isPlaying()) return;

    isPlaying.set(true);
    isPaused.set(false);
    gameOver.set(false);

    // initialize new game
    currentLevel.set(START_LEVEL);
    currentRemainingLives.set(START_LIVES);
    currentScore.set(0);
    brickLayout.resetMatrix();
    destroyedBricksCounter = 0;
    setChanged();
    notifyObservers(new GameEvent(GameEventType.GAME_START));

    loadLevel(currentLevel.get());

    launchBall(SLEEP_BETWEEN_LIVES);
  }

  /** @return true of game is running */
  public boolean isPlaying() {
    return isPlaying.get();
  }

  /**
   * Loads a level and sets the brick matrix
   *
   * @param level
   */
  private void loadLevel(int level) {

    // load next level or game is won if non available
    final Brick[][] newLevel = LevelLoader.getInstance().getLevel(level);
    if (newLevel == null) {
      gameWon();
      return;
    }
    ;

    // set the received level into the brickLayout
    brickLayout.setMatrix(newLevel);

    // Level done
    setChanged();
    notifyObservers(new GameEvent(GameEventType.LEVEL_START));
  }

  /** Called when game is won - last level is cleared */
  private void gameWon() {
    stopPlaying();
    gameOver.set(true);
    setChanged();
    notifyObservers(new GameEvent(GameEventType.GAME_WON));
  }

  /** stops the current game */
  public void stopPlaying() {
    if (!isPlaying()) return;

    // incase we already started a game
    scheduledStart.cancel(true);

    // set status
    isPlaying.set(false);
    isPaused.set(false);
    gameOver.set(false);

    // stop game loop
    mainGameLoop.stop();

    // clean up
    cleanUpPlayfield();
    brickLayout.resetMatrix();
    setChanged();
    notifyObservers(new GameEvent(GameEventType.GAME_STOPPED));
  }

  /** Cleans up balls and pills */
  private void cleanUpPlayfield() {
    // clear ball manager - delete all balls
    ballManager.clear();

    // clear falling power pills
    fallingPowerPills.clear();

    // clear power
    activePower.set(PowerPillType.NONE);

    // clear lasers
    laserShotManager.clear();
  }

  /**
   * Starts a new round after loosing a ball or completing a level
   *
   * @param delay
   */
  private void launchBall(long delay) {
    // remove balls, pills
    cleanUpPlayfield();

    // create new ball
    Ball newBall = new Ball(BALL_TEMPLATE);

    // add it to ball manager
    ballManager.add(newBall);

    // move the ball with the paddle before start of game
    bindBallToPaddle(newBall, paddleWidth.get() / 2 + 20);

    // show the ball for a short time then start the animation
    // check if the game has been stopped while we were waiting
    // start the gameLoop
    scheduledStart =
        executor.schedule(
            () -> {
              if (!isPlaying() || isPaused())
                return; // check if the game has been stopped while we were waiting
              unbindBallFromPaddle(newBall);
              mainGameLoop.play(); // start the gameLoop
            },
            delay,
            TimeUnit.MILLISECONDS);
  }

  /** Binds the ball to the paddle movement before start of the game */
  private void bindBallToPaddle(Ball ball, double xLocationOnPaddle) {
    // bind ball to paddle
    ball.centerXProperty()
        .bind(paddleX.add(xLocationOnPaddle)); // slightly to the right of the middle
    ball.centerYProperty().bind(paddleY.subtract(ball.getRadius()).subtract(1.0));
  }

  /**
   * Releases the ball to the paddle movement before start of the game
   *
   * @param newBall
   */
  private void unbindBallFromPaddle(Ball newBall) {
    newBall.centerXProperty().unbind(); // unbind the ball from the paddle
    newBall.centerYProperty().unbind(); // unbind the ball from the paddle
  }

  /**
   * Called by the <code>mainGameLoop</code> animation event to make a new frame of the game.<br>
   */
  private void gameLoop() {
    if (!isPlaying()) return;

    long startLoopTime = System.nanoTime();

    updateGameState();

    lastloopTime = System.nanoTime() - startLoopTime;
    commulativeLoopTime += lastloopTime;

    updateFPS();
  }

  /** Calculate some statistics */
  private void updateFPS() {
    // frame loop counter
    if (++frameLoopCounter % 100 == 0) {
      double timeSinceLastFPS = (System.nanoTime() - frameLoopCounterTimeStamp);
      fps.set(1e9f * (frameLoopCounter / timeSinceLastFPS));

      double tLoop = ((commulativeLoopTime / frameLoopCounter) / 1e6f);
      double tFrame = 1000 / BALL_INITIAL_FRAMERATE;
      // System.out.printf("Avg. Time for loop: %.6f ms (framelimit %.6f ms) %n", tLoop, tFrame);
      if (tLoop > tFrame) {
        System.err.printf("FRAME LIMIT VIOLATION: %.6f ms (framelimit %.6f ms) %n", tLoop, tFrame);
      }

      commulativeLoopTime = 0;
      frameLoopCounter = 0;
      frameLoopCounterTimeStamp = System.nanoTime();
    }
  }

  private void updateGameState() {

    // if no more balls we lost a live
    if (ballManager.isEmpty()) {

      updateLives();

    } else { // still at least one ball in play

      updatePowerPills();
      updateLaser();
      updateBalls();
      updateLevel();
    }
  }

  private void updateLives() {
    final int remainingLives = decreaseRemainingLives();

    // out of lives => game over
    if (remainingLives < 0) {
      currentRemainingLives.set(0);
      gameOver();
      return;
    }

    // pause animation
    mainGameLoop.pause();

    // launch a new ball
    launchBall(SLEEP_BETWEEN_LIVES);
  }

  private void updateLaser() {
    // else loop over all laser shots
    ListIterator<LaserShot> listIterator = laserShotManager.listIterator();
    while (listIterator.hasNext()) {
      LaserShot ls = listIterator.next();
      // remove laser shots from list
      if (ls.isMarkedForRemoval()) {
        listIterator.remove();
        continue;
      }
      // move the laser shot up
      ls.moveStep();
      // check collisions from the ball(s) with anything else
      checkLaserCollisions(ls);
    }
  }

  private void checkLaserCollisions(final LaserShot ls) {
    // check if hit upper wall
    if (ls.getUpperBound() <= 0) {
      ls.markForRemoval();
      return;
    }

    // calculate laser edge's brick cell
    final int lsRow = (int) (ls.getUpperBound() / brickLayout.getBrickHeight());
    final int lsCol = (int) ((ls.getLeftBound() + LASER_WIDTH / 2) / brickLayout.getBrickWidth());

    // hit above
    if (brickLayout.getBrick(lsRow, lsCol) != null) {
      brickHit(lsRow, lsCol);
      ls.markForRemoval();
      setChanged();
      notifyObservers(new GameEvent(GameEventType.LASER_HIT, lsRow, lsCol, ls));
    }
  }

  /** updates all balls, checks collisions fom balls with anything else and removes lost balls */
  private void updateBalls() {
    // else loop over all balls
    ListIterator<Ball> listIterator = ballManager.listIterator();
    while (listIterator.hasNext()) {
      Ball ball = listIterator.next();

      if (ball.isMarkedForRemoval()) {
        listIterator.remove();
        if (ballManager.isEmpty()) { // lost last ball
          setChanged();
          notifyObservers(new GameEvent(GameEventType.LAST_BALL_LOST, ball));
        } else {
          setChanged();
          notifyObservers(new GameEvent(GameEventType.BALL_LOST, ball));
        }
        continue;
      }

      // move the ball
      if (!ballCatchedFlag) {
        unbindBallFromPaddle(ball);
        ball.moveStep();
      }

      // check collisions from the ball(s) with anything else
      checkBallCollisions(ball);
    }
  }

  /** update power pills */
  private void updatePowerPills() {
    // release next power up - no new powers when more than 1 ball in play
    if (nextPowerPill != null && ballManager.size() == 1) {
      fallingPowerPills.add(nextPowerPill);
      nextPowerPill = null;
    }

    // move power ups down, catch or erase them
    ListIterator<PowerPill> powerPillListIterator = fallingPowerPills.listIterator();
    while (powerPillListIterator.hasNext()) {
      PowerPill pill = powerPillListIterator.next();
      // move the pill down
      pill.fall();
      // pill is lost -> erase it
      if (pill.getY() >= playfieldHeight.get()) {
        powerPillListIterator.remove();
      }
      // pill hits paddle
      else if (pill.getY() + pill.getHeight() >= paddleY.get()
          && pill.getY() <= paddleY.get() + paddleHeight.get()
          && pill.getX() + pill.getWidth() >= paddleX.get()
          && pill.getX() <= paddleX.get() + paddleWidth.get()) {

        powerPillListIterator.remove();

        activatePower(pill);
      }
    }
  }

  /** Activates the current power */
  private void activatePower(PowerPill pill) {

    if (activePower == null) {
      activePower.set(PowerPillType.NONE);
    }

    PowerPillType oldType = activePower.get();
    PowerPillType newType = pill.getPowerPillType();

    // deactivate old power if necessary
    switch (oldType) {
      case NONE:
        break;
      case LASER:
        break;
      case ENLARGE:
        // only shrink it if the next pill is something else
        if (!newType.equals(PowerPillType.ENLARGE)) {
          paddleWidth.set(PADDEL_INITIAL_WIDTH);
          // move to the right to make it look as if it grew from the middle
          paddleX.set(paddleX.get() + ((PADDLE_ENLARGEMENT_FACTOR - 1) / 2) * PADDEL_INITIAL_WIDTH);
        }
        break;
      case CATCH:
        if (!newType.equals(PowerPillType.CATCH)) {
          ballManager.forEach(this::unbindBallFromPaddle);
          ballCatchedFlag = false;
        }
        break;
      case SLOW:
        // deactivate only if it is not SLOW again
        if (!newType.equals(PowerPillType.SLOW)) {
          // reset speed
          ballManager.get(0).setVelocity(BALL_INITIAL_SPEED);
        }
        break;
      case BREAK:
        break;
      case DISRUPTION:
        // do nothing - will be handled before pills are generated as we do not get any new pills
        // if we have more than 1 ball.
        break;
      case PLAYER:
        break;
    }

    // set new power
    activePower.set(newType);

    // activate new power
    switch (activePower.get()) {
      case NONE:
        break;
      case LASER:
        break;
      case ENLARGE:
        // if we are not already large we growing big
        if (!oldType.equals(PowerPillType.ENLARGE)) {
          // bigger
          paddleWidth.set(PADDLE_ENLARGEMENT_FACTOR * PADDEL_INITIAL_WIDTH);
          // move to the left to make it look as if it grew from the middle
          paddleX.set(paddleX.get() - ((PADDLE_ENLARGEMENT_FACTOR - 1) / 2) * PADDEL_INITIAL_WIDTH);
          // push the paddle betweem the walls in case it was outside
          if (paddleX.get() + paddleWidth.get() >= playfieldWidth.get()) {
            paddleX.set(playfieldWidth.get() - paddleWidth.get());
          } else if (paddleX.get() <= 0) {
            paddleX.set(0);
          }
        }
        break;
      case CATCH:
        // is handled in paddle colission and updateBall
        break;
      case SLOW:
        assert ballManager.size() == 1;
        final Ball b = ballManager.get(0);
        b.setVelocity(b.getYVelocity() * 0.8f);
        break;
      case BREAK:
        // clear matrix and advance to next level
        // TODO: open a portal instead directly to the next level
        brickLayout.resetMatrix();
        break;
      case DISRUPTION:
        // add balls up to three balls
        switch (ballManager.size()) {
          case 1:
            ballManager.add(ballManager.get(0).split());
            // fall through
          case 2:
            ballManager.add(ballManager.get(0).split());
            break;
          default:
        }
        break;
      case PLAYER:
        // adds a player live
        increaeRemainingLives();
        break;
    }
  }

  /** Checks if all bricks are gone and if so icreases level and launches new ball. */
  private void updateLevel() {
    if (brickLayout.getNumberOfBricks() == 0) {
      // pause game animation
      mainGameLoop.pause();
      // Level done
      setChanged();
      notifyObservers(new GameEvent(GameEventType.LEVEL_COMPLETE));
      // load new level or game over WON
      increaseLevel();
      loadLevel(currentLevel.get());
      launchBall(SLEEP_BETWEEN_LEVELS);
    }
  }

  /**
   * Checks if the ball(s) have hit a wall, the paddle, a block or has left through the bottom.
   * Calculates new speeds for each direction, tells brickLayout if the ball hits a brick and calls
   * <code>ballLost()</code> when ball has left through bottom.
   */
  private void checkBallCollisions(Ball ball) {
    // hit wall left or right
    checkSideWallCollision(ball);
    // hit wall top
    checkTopWallCollision(ball);
    // hit brick
    checkBrickCollision(ball);
    // hit paddle
    checkPaddleCollision(ball);
    // lost through bottom
    checkBallLostThroughBottom(ball);
  }

  private void checkSideWallCollision(Ball ball) {
    if (ball.getLeftBound() <= 0) { // left
      ball.setCenterX(ball.getRadius()); // in case it was <0
      ball.inverseXdirection();
      setChanged();
    } else if (ball.getRightBound() >= playfieldWidth.get()) { // right
      ball.setCenterX(playfieldWidth.get() - ball.getRadius()); // in case it was >playFieldWidth
      ball.inverseXdirection();
      setChanged();
    }
    notifyObservers(new GameEvent(GameEventType.HIT_WALL, ball));
  }

  private void checkTopWallCollision(Ball ball) {
    if (ball.getUpperBound() <= 0) {
      ball.setCenterY(ball.getRadius()); // in case it was <0
      ball.inverseYdirection();
      setChanged();
    }
    notifyObservers(new GameEvent(GameEventType.HIT_WALL, ball));
  }

  private void checkBrickCollision(Ball ball) {

    // calculate ball center's brick cell
    final int ballCenterRow = (int) (ball.getCenterY() / (brickLayout.getBrickHeight()));
    final int ballCenterCol = (int) (ball.getCenterX() / (brickLayout.getBrickWidth()));
    // calculate ball edge's brick cell
    final int ballUpperRow = (int) (ball.getUpperBound() / brickLayout.getBrickHeight());
    final int ballLowerRow = (int) (ball.getLowerBound() / brickLayout.getBrickHeight());
    final int ballLeftCol = (int) (ball.getLeftBound() / brickLayout.getBrickWidth());
    final int ballRightCol = (int) (ball.getRightBound() / brickLayout.getBrickWidth());

    /*
     * We allow only one hit detection per frame. After each hit the ball will be placed out side the cell
     * against the direction it was coming from. This prevents the ball to end up inside the cell as we do increase
     * our movement steps with more than 1 usually.
     */

    // hit above
    if (ball.getYVelocity() < 0 && brickLayout.getBrick(ballUpperRow, ballCenterCol) != null) {
      // make sure ball is outside the brick cell
      ball.setCenterY(
          1 + ball.getRadius() + brickLayout.getLowerBound(ballUpperRow, ballCenterCol));
      brickHit(ballUpperRow, ballCenterCol);
      // bounce ball
      ball.inverseYdirection();
      setChanged();
      notifyObservers(new GameEvent(GameEventType.HIT_BRICK, ballUpperRow, ballCenterCol, ball));
    }
    // hit below
    else if (ball.getYVelocity() > 0 && brickLayout.getBrick(ballLowerRow, ballCenterCol) != null) {
      // make sure ball is outside the brick cell
      ball.setCenterY(
          -1 - ball.getRadius() + brickLayout.getUpperBound(ballLowerRow, ballCenterCol));
      brickHit(ballLowerRow, ballCenterCol);
      // bounce ball
      ball.inverseYdirection();
      setChanged();
      notifyObservers(new GameEvent(GameEventType.HIT_BRICK, ballLowerRow, ballCenterCol, ball));
    }
    // hit left
    else if (ball.getXVelocity() < 0 && brickLayout.getBrick(ballCenterRow, ballLeftCol) != null) {
      // make sure ball is outside the brick cell
      ball.setCenterX(1 + ball.getRadius() + brickLayout.getRightBound(ballCenterRow, ballLeftCol));
      brickHit(ballCenterRow, ballLeftCol);
      // bounce ball
      ball.inverseXdirection();
      setChanged();
      notifyObservers(new GameEvent(GameEventType.HIT_BRICK, ballCenterRow, ballLeftCol, ball));
    }
    // hit right
    else if (ball.getYVelocity() > 0 && brickLayout.getBrick(ballCenterRow, ballRightCol) != null) {
      // make sure ball is outside the brick cell
      ball.setCenterX(
          -1 - ball.getRadius() + brickLayout.getLeftBound(ballCenterRow, ballRightCol));
      brickHit(ballCenterRow, ballRightCol);
      // bounce ball
      ball.inverseXdirection();
      setChanged();
      notifyObservers(new GameEvent(GameEventType.HIT_BRICK, ballCenterRow, ballRightCol, ball));
    }
  }

  /**
   * Checks if ball touches paddle and if so reverses the ball
   *
   * @return true if ball touches paddle
   */
  private void checkPaddleCollision(Ball ball) {

    if (ball.intersects(paddleX.get(), paddleY.get(), paddleWidth.get(), paddleHeight.get())) {
      //    if ((ball.getLowerBound() >= paddleUpperBound && ball.getLowerBound() <=
      // paddleLowerBound) // ball on correct height
      //        && (ball.getRightBound() > paddleLeftBound && ball.getLeftBound() <
      // paddleRightBound)) { // ball touches the paddle

      // determine where the ball hit the paddle
      final double hitPointAbsolute = ball.getCenterX() - paddleX.get();
      // normalize value to -1 (left), 0 (center), +1 (right)
      final double hitPointRelative = 2 * ((hitPointAbsolute / paddleWidth.get()) - 0.5);
      // determine new angle
      final double newAngle = hitPointRelative * BALL_MAX_ANGLE;

      // give the ball the new angle always upwards
      ball.bounceFromPaddle(newAngle);

      // check if we should
      if (activePower.get().equals(PowerPillType.CATCH)
          && !ballCatchedFlag // not already catched
          && ballManager.size() == 1) { // only when only one ball in play
        ballCatchedFlag = true;
        bindBallToPaddle(ball, hitPointAbsolute);
      }

      setChanged();
      notifyObservers(new GameEvent(GameEventType.HIT_PADDLE, ball));
    }
  }

  /**
   * Checks if the ball went through the bottom. If so marks the ball for removal in the next game
   * loop.
   *
   * @param ball
   */
  private void checkBallLostThroughBottom(Ball ball) {
    if (ball.getUpperBound() >= playfieldHeight.get()) {
      ball.markForRemoval();
    }
  }

  /** Called when out of lives or after last level */
  private void gameOver() {
    stopPlaying();
    gameOver.set(true);
    setChanged();
    notifyObservers(new GameEvent(GameEventType.GAME_OVER));
  }

  /**
   * @param row
   * @param col
   */
  private void brickHit(final int row, final int col) {
    // which type
    Brick brick = brickLayout.getBrick(row, col);
    BrickType brickType = brick.getType();
    // hit the brick / get points for every destroyed brick
    final int hitBrickScore = brickLayout.hitBrick(row, col);
    // increase score
    increaseScore(brickType, hitBrickScore);
    // count destroyed bricks
    if (hitBrickScore > 0) {
      destroyedBricksCounter++;
      nextPowerUp--;
      if (nextPowerUp == 0) {
        nextPowerPill =
            new PowerPill(
                PowerPillType.getRandom(),
                brickLayout.getLeftBound(row, col),
                brickLayout.getUpperBound(row, col),
                brickLayout.getBrickWidth(),
                brickLayout.getBrickHeight());
        nextPowerUp = getNextPowerUp();
      }
    }
  }

  /**
   * Increases score and adds lives at certain score main.resources.levels
   *
   * @param hitBrickScore
   */
  private void increaseScore(final BrickType brickType, int hitBrickScore) {
    if (brickType.equals(BrickType.SILVER)) { // Silver brick is special case
      hitBrickScore = currentLevel.get() * hitBrickScore;
    }
    final int previousScore = currentScore.get();
    final int newScore = previousScore + hitBrickScore;
    currentScore.set(newScore);
    // add new lives after 20.000 and after every other 60.000 points
    if (previousScore < 20000 && newScore > 20000) {
      increaeRemainingLives();
    } else if (previousScore > 20000) {
      int modBefore = previousScore / 60000;
      int modAfter = newScore / 60000;
      if (modAfter > modBefore) {
        increaeRemainingLives();
      }
    }
  }

  /** Increases level by 1 */
  private void increaseLevel() {
    currentLevel.set(currentLevel.get() + 1);
  }

  /** adds a lives after score thresholds or Player PowerType */
  private void increaeRemainingLives() {
    currentRemainingLives.set(currentRemainingLives.get() + 1);
  }

  /**
   * decreases the remaining lives after loosing a ball
   *
   * @return remaining lives
   */
  private int decreaseRemainingLives() {
    currentRemainingLives.set(currentRemainingLives.get() - 1);
    return currentRemainingLives.get();
  }

  /**
   * Called by the <code>paddleMovementTimeline<code> animation event to move the paddles.
   */
  private void paddleMovementLoop() {
    if (isPaused()) return; // no paddle movement when game is paused
    if (paddleLeft && paddleX.get() > 0.0) {
      paddleX.setValue(paddleX.getValue() - PADDLE_MOVE_STEPS);
    }
    if (paddleRight && paddleX.get() + paddleWidth.get() < playfieldWidth.get()) {
      paddleX.setValue(paddleX.getValue() + PADDLE_MOVE_STEPS);
    }
  }

  public void shootLaser() {
    if (isPlaying() && !isPaused() && activePower.get().equals(PowerPillType.LASER)) {
      LaserShot ls1 =
          new LaserShot(
              paddleX.get() + LASER_EDGE_OFFSET,
              paddleY.get(),
              LASER_WIDTH,
              LASER_HEIGHT,
              LASER_SPEED);
      LaserShot ls2 =
          new LaserShot(
              paddleX.get() + paddleWidth.get() - LASER_EDGE_OFFSET,
              paddleY.get(),
              LASER_WIDTH,
              LASER_HEIGHT,
              LASER_SPEED);

      laserShotManager.addAll(ls1, ls2);
    }
  }

  /** is called when a user restarts a catched ball by pressing a key or mouse button * */
  public void releaseCaughtBall() {
    if (ballCatchedFlag) {
      ballCatchedFlag = false;
    }
  }

  /** pauses a running game */
  public void pausePlaying() {
    if (!isPlaying()) return; // ignore if not playing
    isPaused.set(true);
    mainGameLoop.pause();
  }

  /** @return true if game is paused */
  public boolean isPaused() {
    return isPaused.get();
  }

  /** resumes a paused running game */
  public void resumePlaying() {
    if (!isPlaying() && !isPaused()) return; // ignore if not playing
    isPaused.set(false);
    mainGameLoop.play();
  }

  /**
   * Called from controller by mouse move events. Moves the paddle according to the mouse's x
   * position when mouse is in window. The paddle's center will be set to the current mouse
   * position.
   *
   * @param mouseX
   */
  public void setMouseXPosition(double mouseX) {
    if (isPaused()) return;
    double x = mouseX;
    double halfPaddleWidth = paddleWidthProperty().get() / 2;
    if (x - halfPaddleWidth < 0.0) {
      x = halfPaddleWidth;
    } else if (x + halfPaddleWidth > playfieldWidth.get()) {
      x = playfieldWidth.get() - halfPaddleWidth;
    }
    if (paddleX.get() >= 0.0 && paddleX.get() + paddleWidth.get() <= playfieldWidth.get()) {
      paddleXProperty().set(x - halfPaddleWidth);
    }
  }

  public DoubleProperty paddleWidthProperty() {
    return paddleWidth;
  }

  public DoubleProperty paddleXProperty() {
    return paddleX;
  }

  public DoubleProperty playfieldWidthProperty() {
    return playfieldWidth;
  }

  public DoubleProperty playfieldHeightProperty() {
    return playfieldHeight;
  }

  public DoubleProperty paddleHeightProperty() {
    return paddleHeight;
  }

  public DoubleProperty paddleYProperty() {
    return paddleY;
  }

  public ReadOnlyBooleanProperty isPlayingProperty() {
    return isPlaying.getReadOnlyProperty();
  }

  public ReadOnlyBooleanProperty isPausedProperty() {
    return isPaused.getReadOnlyProperty();
  }

  public ReadOnlyBooleanProperty gameOverProperty() {
    return gameOver.getReadOnlyProperty();
  }

  public ReadOnlyIntegerProperty currentLevelProperty() {
    return currentLevel.getReadOnlyProperty();
  }

  public ReadOnlyIntegerProperty currentRemainingLivesProperty() {
    return currentRemainingLives.getReadOnlyProperty();
  }

  public ReadOnlyIntegerProperty currentScoreProperty() {
    return currentScore.getReadOnlyProperty();
  }

  public void setPaddleLeft(boolean b) {
    paddleLeft = b;
  }

  public void setPaddleRight(boolean b) {
    paddleRight = b;
  }

  public ListProperty<PowerPill> fallingPowerPillsProperty() {
    return fallingPowerPills;
  }

  public ObjectProperty<PowerPillType> activePowerProperty() {
    return activePower;
  }

  public DoubleProperty fpsProperty() {
    return fps;
  }

  public ListProperty<Ball> getBallManager() {
    return ballManager;
  }

  public ListProperty<LaserShot> getLaserShotManager() {
    return laserShotManager;
  }

  /** @return the current brick layout */
  public BrickLayout getBrickLayout() {
    return brickLayout;
  }

  /** @return the current fps */
  public double getFps() {
    return fps.get();
  }

  /** @return number of bricks to be destroyed until next power up */
  private int getNextPowerUp() {
    return NEXT_POWERUP_OFFSET + (int) (Math.random() * POWER_UP_FREQUENCY);
  }

  public PowerPillType getActivePower() {
    return activePower.get();
  }
}
