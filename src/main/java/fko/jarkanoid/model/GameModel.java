/*
 * MIT License
 *
 * Copyright (c) 2018 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package fko.jarkanoid.model;

import fko.jarkanoid.events.GameEvent;
import fko.jarkanoid.events.GameEvent.GameEventType;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Observable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * GameModel
 * <p>
 * <p>Handles the Jarkanoid game status, the main game loop and calculations.<br>
 *
 * @author Frank Kopp
 */
public class GameModel extends Observable {

  private static final Logger LOG = LoggerFactory.getLogger(GameModel.class);

  // TODO: add acceleration
  // TODO: create all levels
  // IDEAS: Powers: multiball (star), Trasher (no bouncing on bricks detroyed), extra wide, smaller
  // IDEAS :paddle, thehered paddle,
  // IDEAS: Powers: upside down, reverse control, cloaked, power not falling in straight line
  // IDEAS: Bricks: moving bricks, zombi bricks - come back to life, shield for bricks
  // IDEAS: Special: flying aliens, flying powers, ball catcher, ball beamer, ball warper

  // debugging constants / for normal playing these have to be all false
  private static final boolean BOUNCING_FLOOR = true;

  // game constants
  private static final int START_LEVEL = 1;
  private static final int START_LIVES = 3;

  private static final long SLEEP_BETWEEN_LIVES = 2000; // in ms
  private static final long SLEEP_BETWEEN_LEVELS = 3000; // in ms

  /*
   * Constants for game dimensions and other relevant settings.
   * Need to be aligned with FXML UI Design.
   */
  private static final double PLAYFIELD_INITIAL_HEIGHT = 710;
  private static final double PLAYFIELD_INITIAL_WIDTH = 780;

  // paddle constants
  private static final double PADDLE_INITIAL_FRAMERATE = 120; // Framerate for paddle movements
  private static final double PADDLE_MOVE_STEPS = 5.0; // steps per animation cycle
  private static final double PADDLE_INITIAL_Y = 670;
  private static final double PADDLE_INITIAL_X = 315;
  private static final double PADDEL_INITIAL_WIDTH = 150; // 150;
  private static final double PADDLE_INITIAL_HEIGHT = 20;
  private static final float  PADDLE_ENLARGEMENT_FACTOR = 1.4f;

  // Ball constants
  private static final double BALL_INITIAL_RADIUS = 6;
  private static final double BALL_MAX_ANGLE = 60;
  private static final double BALL_INITIAL_X = 390;
  private static final double BALL_INITIAL_Y = PADDLE_INITIAL_Y - BALL_INITIAL_RADIUS;

  // Absolute speed of ball, when vertical equals px in y, when horizontal equals px in x
  private static final double BALL_INITIAL_SPEED = 10; // 10px at 60fps this is 600px/sec

  // Framerate for game loop
  private static final double INITIAL_FRAMERATE = 60;
  private static final double maxDeltaTime = 1f/10f;

  // Laser constants
  private static final double LASER_EDGE_OFFSET = 45;
  private static final double LASER_WIDTH = 5;
  private static final double LASER_HEIGHT = 15;
  private static final double LASER_SPEED = 900; // 15px at 60fps this is 900px/sec
                                                 // must be more than brick heigth (23,666px or 1.400px/sec)

  // Template of a starting ball to copy when a new level starts
  private static final Ball BALL_TEMPLATE =
          new Ball(BALL_INITIAL_X, BALL_INITIAL_Y, BALL_INITIAL_RADIUS, 0, BALL_INITIAL_SPEED);

  // power up constants
  // how many destroyed bricks between power ups (needs to be >0)
  private static final int NEXT_POWERUP_OFFSET = 3;
  // power up randomly after 0 to 10 destroyed bricks after offset
  private static final int POWER_UP_FREQUENCY = 8;
  private static final double POWER_PILL_FALLING_SPEED = 300; // 5px at 60fps this is 300px/sec
                                                              // if too fast will tunnel through paddle

  // the maximum number the ball may bounce without hitting the paddle or destroying a brick
  // After this number the ball gets a random nudge in a different direction
  private static final int MAX_NUMBER_OF_LOOP_HITS = 25;

  // the maximal entries in the highscore list
  private static final int HIGHSCORE_MAX_PLACE = 15;

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
  // time from last game loop call to calculate delta
  private long previousTime;

  // paddle movements have their own game loop so we can move it outside of a running game
  private final Timeline paddleMovementLoop = new Timeline();

  // used to delay starts of game
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

  // the brick layout holds all bricks and its positions of the games
  private final BrickLayout brickLayout;

  // set to true when key is pressed/released to indicate paddle
  // movement to movement animation
  private boolean paddleLeft;
  private boolean paddleRight;

  // to delay the start of the ball and to be able to stop a game before this timer delay runs out
  private ScheduledFuture scheduledStart;

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

  // count each time the game loop is called and some other statistics
  private long frameLoopCounter = 0;
  private long frameLoopCounterTimeStamp = System.nanoTime();
  private long lastloopTime;
  private long commulativeLoopTime;
  private final DoubleProperty fps = new SimpleDoubleProperty(INITIAL_FRAMERATE);

  // grower and shrinker timeline of paddles
  private final Timeline paddleGrower = new Timeline();
  private final Timeline paddleShrinker = new Timeline();

  // counter since last paddle or brick hit to detect endless loops with gold bricks
  private int maxLoopHitsCounter = MAX_NUMBER_OF_LOOP_HITS;

  // highscore manager
  private final HighScore highScoreManager = HighScore.getInstance();

  // player name property
  private final StringProperty playerName = new SimpleStringProperty("Unknown Player");

  /**
   * Constructor - prepares the brick layout and the game loops.
   */
  public GameModel() {

    // setup BrickLayout
    brickLayout = new BrickLayout(playfieldWidth, playfieldHeight);

    // configure ball list
    ballManager.set(FXCollections.observableList(new ArrayList<>(3)));

    // configure laserShot list
    laserShotManager.set(FXCollections.observableList(new ArrayList<>()));

    // configure fallingPower list
    fallingPowerPills.set(FXCollections.observableList(new ArrayList<>()));

    // start the paddle movements
    paddleMovementLoop.setCycleCount(Timeline.INDEFINITE);
    KeyFrame movePaddle =
            new KeyFrame(Duration.millis(1000f / PADDLE_INITIAL_FRAMERATE), e -> paddleMovementLoop());
    paddleMovementLoop.getKeyFrames().add(movePaddle);
    paddleMovementLoop.play();

    // prepare ball movements (will be started in startGame())
    mainGameLoop.setCycleCount(Timeline.INDEFINITE);
    KeyFrame moveBall = new KeyFrame(Duration.millis(1000f / INITIAL_FRAMERATE), e -> gameLoop());
    mainGameLoop.getKeyFrames().add(moveBall);

    // animation to grow the paddle slowly when we get an ENLARGE power
    // As we want to be able to move the paddle during the animation and also check if the
    // paddle grows out of the playing field we can't use normal property value timelines.
    final int steps = 25; // do 25 intermediate steps - when at 10ms per step this is a 250ms animation
    paddleGrower.setCycleCount(steps);
    // larger
    final double lSteps = (PADDLE_ENLARGEMENT_FACTOR - 1) / steps;
    // move to the left to make it look as if it grew from the middle
    final double xSteps = (((PADDLE_ENLARGEMENT_FACTOR - 1) / 2) * PADDEL_INITIAL_WIDTH) / steps;
    KeyFrame grow =
            new KeyFrame(
                    Duration.millis(10),
                    (event) -> {
                      paddleWidth.set(paddleWidth.get() * (1.0 + lSteps));
                      paddleX.set(paddleX.get() - xSteps);
                      // push the paddle betweem the walls in case it was outside
                      if (paddleX.get() + paddleWidth.get() >= playfieldWidth.get()) {
                        paddleX.set(playfieldWidth.get() - paddleWidth.get());
                      } else if (paddleX.get() <= 0) {
                        paddleX.set(0);
                      }
                    });
    paddleGrower.getKeyFrames().addAll(grow);
  }

  /**
   * Starts a new game.
   */
  public void startPlaying() {
    if (isPlaying()) return;

    LOG.info("Start playing");

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

  /**
   * Loads a level and sets the brick matrix
   *
   * @param level
   */
  private void loadLevel(int level) {

    LOG.info("Loading level {}", level);

    // load next level or game is won if non available
    final Brick[][] newLevel = LevelLoader.getInstance().getLevel(level);
    if (newLevel == null) {
      gameOver(true);
      return;
    }

    // set the received level into the brickLayout
    brickLayout.setMatrix(newLevel);

    // Level done
    setChanged();
    notifyObservers(new GameEvent(GameEventType.LEVEL_START));
  }

  /**
   * stops the current game
   */
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

    LOG.info("Game stopeed");
  }

  /**
   * Cleans up balls and pills
   */
  private void cleanUpPlayfield() {
    // clear ball manager - delete all balls
    ballManager.clear();

    // clear falling power pills
    fallingPowerPills.clear();
    nextPowerPill = null;

    // clear power
    activatePower(PowerPillType.NONE);

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
                      if (!isPlaying() || isPaused()) {
                        return; // check if the game has been stopped while we were waiting
                      }
                      unbindBallFromPaddle(newBall);
                      mainGameLoop.play(); // start the gameLoop
                    },
                    delay,
                    TimeUnit.MILLISECONDS);

    LOG.debug("Ball launched");
  }

  /**
   * Binds the ball to the paddle movement before start of the game
   */
  private void bindBallToPaddle(Ball ball, double xLocationOnPaddle) {
    // bind ball to paddle
    ball.centerXProperty().bind(paddleX.add(xLocationOnPaddle));
    ball.centerYProperty().bind(paddleY.subtract(ball.getRadius()).subtract(1.0));

    // release the ball after a few seconds
    executor.schedule(
            () -> {
              // check if the game has been stopped while we were waiting
              if (!isPlaying() || isPaused()) return;
              ballCatchedFlag = false;
            },
            5000,
            TimeUnit.MILLISECONDS);

    LOG.debug("Ball bound to paddle for 5 sec");
  }

  /**
   * Releases the ball to the paddle movement before start of the game
   *
   * @param newBall
   */
  private void unbindBallFromPaddle(Ball newBall) {
    newBall.centerXProperty().unbind(); // unbind the ball from the paddle
    newBall.centerYProperty().unbind(); // unbind the ball from the paddle
    LOG.debug("Ball unbound to paddle");
  }

  /**
   * Called by the <code>mainGameLoop</code> animation event to make a new frame of the game.<br>
   */
  private void gameLoop() {
    if (!isPlaying()) return;

    long startLoopTime = System.nanoTime();

    updateGameState(startLoopTime);

    lastloopTime = System.nanoTime() - startLoopTime;
    commulativeLoopTime += lastloopTime;

    updateFPS();
  }

  /**
   * Calculate some statistics
   */
  private void updateFPS() {
    // frame loop counter
    if (++frameLoopCounter % 100 == 0) {
      double timeSinceLastFPS = (System.nanoTime() - frameLoopCounterTimeStamp);
      fps.set(1e9f * (frameLoopCounter / timeSinceLastFPS));

      double tLoop = ((commulativeLoopTime / frameLoopCounter) / 1e6f);
      double tFrame = 1000 / INITIAL_FRAMERATE;
      // System.out.printf("Avg. Time for loop: %.6f ms (framelimit %.6f ms) %n", tLoop, tFrame);
      if (tLoop > tFrame) {
        if (LOG.isWarnEnabled()) {
          LOG.warn(
                  String.format(
                          "FRAME LIMIT VIOLATION: %.6f ms (framelimit %.6f ms) %n", tLoop, tFrame));
        }
      }

      commulativeLoopTime = 0;
      frameLoopCounter = 0;
      frameLoopCounterTimeStamp = System.nanoTime();
    }
  }

  /**
   * Updates the model for the game. Currently based on discrete steps independed from time elapsed
   * TODO: refactor using time elapsed instead of fixed step sizes
   * @param startLoopTime
   */
  private void updateGameState(final long currentTime) {

    // if no more balls we lost a live
    if (ballManager.isEmpty()) {

      LOG.info("Lost last ball");
      decreaseLives();

    } else { // still at least one ball in play

      if (previousTime == 0) {
        previousTime = currentTime;
        return;
      }

      double deltaTime = (currentTime - previousTime) / 1e9f;
      double deltaTimeCapped = Math.min(deltaTime, maxDeltaTime);
      previousTime = currentTime;

      //System.out.format("Elapsed: %f Capped: %f Now: %d %n", deltaTime, deltaTimeCapped, currentTime);

      updatePowerPills(deltaTimeCapped);
      updateLaser(deltaTimeCapped);
      updateBalls(deltaTimeCapped);

      updateLevel();
    }
  }

  private void decreaseLives() {

    currentRemainingLives.set(currentRemainingLives.get() - 1);
    LOG.info("Decreased number of lives to {}", currentRemainingLives.get());

    // out of lives => game over
    if (currentRemainingLives.get() < 0) {
      currentRemainingLives.set(0);
      gameOver(false);
      return;
    }

    // pause animation
    mainGameLoop.pause();

    // launch a new ball
    launchBall(SLEEP_BETWEEN_LIVES);
  }

  private void updateLaser(final double deltaTimeCapped) {
    // loop over all laser shots
    ListIterator<LaserShot> listIterator = laserShotManager.listIterator();
    while (listIterator.hasNext()) {
      LaserShot ls = listIterator.next();
      // remove laser shots from list
      if (ls.isMarkedForRemoval()) {
        listIterator.remove();
        continue;
      }
      // move the laser shot up
      ls.moveStep(deltaTimeCapped);
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

  /**
   * updates all balls, checks collisions from balls with anything else and removes lost balls
   * @param deltaTimeCapped
   */
  private void updateBalls(final double deltaTimeCapped) {
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
        ball.moveStep(deltaTimeCapped);
      }

      // check collisions from the ball(s) with anything else
      checkBallCollisions(ball);

      // ball cought in loop?
      if (maxLoopHitsCounter <= 0) {
        ball.nudgeBall();
        maxLoopHitsCounter = MAX_NUMBER_OF_LOOP_HITS;
        LOG.debug("Possible loop -> nudge ball");
      }
    }
  }

  /**
   * update power pills
   * @param deltaTimeCapped
   */
  private void updatePowerPills(final double deltaTimeCapped) {
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
      pill.fall(deltaTimeCapped);

      // check collisions
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

        activatePowerPill(pill);
      }
    }
  }

  /**
   * Activates the current power pill
   */
  private void activatePowerPill(PowerPill pill) {
    if (activePower == null) {
      activePower.set(PowerPillType.NONE);
    }
    PowerPillType newType = pill.getPowerPillType();
    activatePower(newType);
  }

  /**
   * Activates the current power
   */
  private void activatePower(final PowerPillType newType) {
    PowerPillType oldType = activePower.get();

    LOG.info("Activiating power with {} from {}", newType, oldType);

    // deactivate old power if necessary
    switch (oldType) {
      case NONE:
        break;
      case LASER:
        if (!newType.equals(PowerPillType.LASER)) {
          setChanged();
          notifyObservers(new GameEvent(GameEventType.LASER_OFF));
        }
        break;
      case ENLARGE:
        // only shrink it if the next pill is something else
        if (!newType.equals(PowerPillType.ENLARGE)) {
          shrinkPaddle();
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
        if (!newType.equals(PowerPillType.SLOW) && ballManager.size() > 0) {
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
        setChanged();
        notifyObservers(new GameEvent(GameEventType.LASER_ON));
        break;
      case ENLARGE:
        // if we are not already large we growing big
        if (!oldType.equals(PowerPillType.ENLARGE)) {
          growPaddle();
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

  /**
   * @return number of bricks to be destroyed until next power up
   */
  private int getNextPowerUp() {
    return NEXT_POWERUP_OFFSET + (int) (Math.random() * POWER_UP_FREQUENCY);
  }

  /**
   * Grows paddle over time
   */
  private void growPaddle() {
    paddleGrower.playFromStart();
  }

  /**
   * shrink paddle over time
   */
  private void shrinkPaddle() {
    // shrink the paddle slowly
    paddleShrinker.setCycleCount(1);
    // smaller
    double smallerSize = PADDEL_INITIAL_WIDTH;
    // move to the left to make it look as if it grew from the middle
    double newLeftX = paddleX.get() + ((PADDLE_ENLARGEMENT_FACTOR - 1) / 2) * PADDEL_INITIAL_WIDTH;
    KeyFrame shrinkR = new KeyFrame(Duration.millis(250), new KeyValue(paddleWidth, smallerSize));
    KeyFrame shrinkL = new KeyFrame(Duration.millis(250), new KeyValue(paddleX, newLeftX));
    paddleShrinker.getKeyFrames().addAll(shrinkL, shrinkR);
    paddleShrinker.playFromStart();
  }

  /**
   * Checks if all bricks are gone and if so icreases level and launches new ball.
   * @param deltaTimeCapped
   */
  private void updateLevel() {
    if (brickLayout.getNumberOfBricks() == 0) {
      // pause game animation
      mainGameLoop.pause();
      // Level done
      setChanged();
      notifyObservers(new GameEvent(GameEventType.LEVEL_COMPLETE));
      // load new level or game over WON
      currentLevel.set(currentLevel.get() + 1);
      LOG.info("increased level to {}", currentLevel.get());
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

    /*
     * We use intermediate discrete (<1) steps to avoid "tunneling" through objects.
     * We use the last know ball position and we divide the the path from the last know position to the
     * current position (after the moveBall() step) into x parts (x=velocity of ball). With this we should get
     * steps in X and Y direction which are smaller than 1 and therefore should detect collissions very
     * accurately.
     */

    // convenience variables
    final double radius = ball.getRadius();
    final double vY = ball.getYVelocity();
    final double vX = ball.getXVelocity();
    final double bY = ball.getCenterY();
    final double bX = ball.getCenterX();
    final double bpY = ball.getPreviousCenterY();
    final double bpX = ball.getPreviousCenterX();

    double cbY = bpY; // current Y set up previous Y
    double cbX = bpX; // current

    // step sizes
    final double stepY = vY / ball.getVelocity();
    final double stepX = vX / ball.getVelocity();
    final double stepV = 1;

    if (LOG.isDebugEnabled()) { // to not even create the string when not logging
      LOG.debug(
              String.format(
                      "FULL: vY: %6.2f  vX: %6.2f  v: %6.2f  CURRENT     : Y: %8.2f X: %8.2f PREVIOUS: Y: %8.2f X: %8.2f *** loop=%d",
                      vY, vX, ball.getVelocity(), bY, bX, bpY, bpX, maxLoopHitsCounter));
      // DEBUG - because of floating numbers round this needs to be a fuzzy
      if (bY - vY - bpY > 0.01 && bY - vY - bpY < -0.01
          || bX - vX - bpX > 0.01
          || bX - vX - bpX < -0.01) {
        LOG.warn("WARP ERROR");
      }
    }

    // do discrete intermediate steps
    for (int t = 1; t <= ball.getVelocity(); t++) {

      // advance current ball center position by 1 step
      cbY += stepY;
      cbX += stepX;

      if (LOG.isDebugEnabled()) { // to not even create the string when not logging
        LOG.debug(
                String.format(
                        "STEP: vY: %6.2f  vX: %6.2f  v: %6.2f  INTERMEDIATE: Y: %8.2f X: %8.2f",
                        stepY, stepX, stepV, cbY, cbX));
      }

      // ************************
      //  Collossion Check Bricks
      // ************************

      /*
       * Instead of having an list of all bricks to check against we use the fact that bricks
       * are positioned in a regular matrix of 13 columns and 18 rows. Collission could therefore
       * be reduced to calculate the position of the ball within in this matrix. When the cell
       * the ball is in has a brick then there is a collision.
       * Problem is "tunneling" and multiple collissions at the same time. They could lead to
       * a false bouncing angle.
       * This will be prevented by also checking where the ball is coming from a only allowing
       * one collission at a time. Usually multiple collissions within the same step should be
       * rare as we have very small intermediate steps (<1 in each direction). In case they do
       * happen there should be no harm in ignoring one of them.
       */

      // calculate ball center's brick cell
      final int ballCenterRow = (int) (cbY / (brickLayout.getBrickHeight()));
      final int ballCenterCol = (int) (cbX / (brickLayout.getBrickWidth()));

      // calculate ball edge's brick cell
      final int ballUpperRow = (int) ((cbY - radius) / brickLayout.getBrickHeight());
      final int ballLowerRow = (int) ((cbY + radius) / brickLayout.getBrickHeight());
      final int ballLeftCol = (int) ((cbX - radius) / brickLayout.getBrickWidth());
      final int ballRightCol = (int) ((cbX + radius) / brickLayout.getBrickWidth());

      int hitCounter = 0;
      if (vY < 0 && brickLayout.getBrick(ballUpperRow, ballCenterCol) != null) {
        hitCounter |= 1; // top
      }
      if (vX >= 0 && brickLayout.getBrick(ballCenterRow, ballRightCol) != null) {
        hitCounter |= 2; // right
      }
      if (vX < 0 && brickLayout.getBrick(ballCenterRow, ballLeftCol) != null) {
        hitCounter |= 4; // left
      }
      if (vY > 0 && brickLayout.getBrick(ballLowerRow, ballCenterCol) != null) {
        hitCounter |= 8; // bottom
      }
      if (Integer.bitCount(hitCounter) > 1) {
        LOG.info("MULTIPLE HIT trlb={}", Integer.toBinaryString(hitCounter));
      }

      // hit above
      if (Integer.lowestOneBit(hitCounter) == 1) {
        brickHit(ballUpperRow, ballCenterCol);
        setChanged();
        notifyObservers(new GameEvent(GameEventType.HIT_BRICK, ballUpperRow, ballCenterCol, ball));
        ball.inverseYdirection();
        // actually set the ball exactly onto the intermediate location and return for the next step
        ball.setCenterX(cbX - stepX);
        ball.setCenterY(cbY - stepY);
        // relevant Hit?
        maxLoopHitsCounter--;
        return;
      }

      // hit right
      if (Integer.lowestOneBit(hitCounter) == 2) {
        brickHit(ballCenterRow, ballRightCol);
        setChanged();
        notifyObservers(new GameEvent(GameEventType.HIT_BRICK, ballCenterRow, ballRightCol, ball));
        ball.inverseXdirection();
        // actually set the ball exactly onto the intermediate location and return for the next step
        ball.setCenterX(cbX - stepX);
        ball.setCenterY(cbY - stepY);
        // relevant Hit?
        maxLoopHitsCounter--;
        return;
      }

      // hit left
      if (Integer.lowestOneBit(hitCounter) == 4) {
        brickHit(ballCenterRow, ballLeftCol);
        setChanged();
        notifyObservers(new GameEvent(GameEventType.HIT_BRICK, ballCenterRow, ballLeftCol, ball));
        ball.inverseXdirection();
        // actually set the ball exactly onto the intermediate location and return for the next step
        ball.setCenterX(cbX - stepX);
        ball.setCenterY(cbY - stepY);
        // relevant Hit?
        maxLoopHitsCounter--;
        return;
      }

      // hit below
      if (Integer.lowestOneBit(hitCounter) == 8) {
        brickHit(ballLowerRow, ballCenterCol);
        setChanged();
        notifyObservers(new GameEvent(GameEventType.HIT_BRICK, ballLowerRow, ballCenterCol, ball));
        ball.inverseYdirection();
        // actually set the ball exactly onto the intermediate location and return for the next step
        ball.setCenterX(cbX - stepX);
        ball.setCenterY(cbY - stepY);
        // relevant Hit?
        maxLoopHitsCounter--;
        return;
      }

      // ************************
      //  Collossion Check Paddle
      // ************************

      if (ball.intersects(paddleX.get(), paddleY.get(), paddleWidth.get(), paddleHeight.get())) {

        // relevant Hit - yes - reset
        maxLoopHitsCounter = MAX_NUMBER_OF_LOOP_HITS;

        // determine where the ball hit the paddle
        final double hitPointAbsolute = ball.getCenterX() - paddleX.get();
        // normalize value to -1 (left), 0 (center), +1 (right)
        final double hitPointRelative = 2 * ((hitPointAbsolute / paddleWidth.get()) - 0.5);
        // determine new angle
        final double newAngle = hitPointRelative * BALL_MAX_ANGLE;

        // give the ball the new angle always upwards
        ball.bounceFromPaddle(newAngle);

        // check if we should catch the ball
        if (activePower.get().equals(PowerPillType.CATCH)
            && !ballCatchedFlag // not already catched
            && ballManager.size() == 1) { // only when only one ball in play
          ballCatchedFlag = true;
          bindBallToPaddle(ball, hitPointAbsolute);
          setChanged();
          notifyObservers(new GameEvent(GameEventType.CAUGHT));
        } else {
          setChanged();
          notifyObservers(new GameEvent(GameEventType.HIT_PADDLE, ball));
          // actually set the ball exactly onto the intermediate location and return for the next
          // step
          ball.setCenterX(cbX);
          ball.setCenterY(cbY);
          return;
        }
      }

      // ****************************
      //  Collossion Check Side Walls
      // ****************************

      if (ball.getLeftBound() <= 0) { // left
        setChanged();
        notifyObservers(new GameEvent(GameEventType.HIT_WALL, ball));
        ball.inverseXdirection();
        // actually set the ball exactly onto the intermediate location and return for the next step
        ball.setCenterX(cbX);
        ball.setCenterY(cbY);
        // relevant Hit?
        maxLoopHitsCounter--;
        return;
      } else if (ball.getRightBound() >= playfieldWidth.get()) { // right
        setChanged();
        notifyObservers(new GameEvent(GameEventType.HIT_WALL, ball));
        ball.inverseXdirection();
        // actually set the ball exactly onto the intermediate location and return for the next step
        ball.setCenterX(cbX);
        ball.setCenterY(cbY);
        // relevant Hit?
        maxLoopHitsCounter--;
        return;
      }

      // **************************
      //  Collossion Check TOP WALL
      // **************************
      if (ball.getUpperBound() <= 0) {
        setChanged();
        notifyObservers(new GameEvent(GameEventType.HIT_WALL, ball));
        ball.inverseYdirection();
        // actually set the ball exactly onto the intermediate location and return for the next step
        ball.setCenterX(cbX);
        ball.setCenterY(cbY);
        // relevant Hit?
        maxLoopHitsCounter--;
        return;
      }

      // ************************
      //  Collossion Check Bottom
      // ************************

      if (ball.getUpperBound() >= playfieldHeight.get()) {
        if (BOUNCING_FLOOR) {
          ball.inverseYdirection();
        } else {
          ball.markForRemoval();
          // actually set the ball exactly onto the intermediate location and return for the next
          // step
          ball.setCenterX(cbX);
          ball.setCenterY(cbY);
          // relevant Hit?
          maxLoopHitsCounter = MAX_NUMBER_OF_LOOP_HITS;
        }
        return;
      }
    } // end for intermediate step
  }

  /**
   * @param row
   * @param col
   */
  private void brickHit(final int row, final int col) {
    // which type
    BrickType brickType = brickLayout.getBrick(row, col).getType();
    // hit the brick / get points for every destroyed brick
    final int hitBrickScore = brickLayout.hitBrick(row, col);
    // increase score
    increaseScore(brickType, hitBrickScore);
    // count destroyed bricks
    if (hitBrickScore > 0) {
      // relevant Hit
      maxLoopHitsCounter = MAX_NUMBER_OF_LOOP_HITS;
      destroyedBricksCounter++;
      nextPowerUp--;
      if (nextPowerUp == 0) {
        nextPowerPill =
                new PowerPill(
                        PowerPillType.getRandom(),
                        brickLayout.getLeftBound(row, col),
                        brickLayout.getUpperBound(row, col),
                        brickLayout.getBrickWidth(),
                        brickLayout.getBrickHeight(),
                        POWER_PILL_FALLING_SPEED);
        nextPowerUp = getNextPowerUp();
        LOG.debug("PowerPill generated: {}", nextPowerPill);
      }
    }
  }

  /**
   * Called when out of lives or after last level
   */
  private void gameOver(boolean won) {
    stopPlaying();
    gameOver.set(true);
    if (won) {
      LOG.info("Game Won");
      setChanged();
      notifyObservers(new GameEvent(GameEventType.GAME_WON));
    } else {
      LOG.info("Game Over");
      setChanged();
      notifyObservers(new GameEvent(GameEventType.GAME_OVER));
    }
    // new highscore (1st to 15th place)
    if (highScoreManager.getList().size() < HIGHSCORE_MAX_PLACE - 1
        || currentScore.get() > highScoreManager.getList().get(HIGHSCORE_MAX_PLACE - 1).score) {
      final HighScore.HighScoreEntry entry =
              new HighScore.HighScoreEntry(
                      playerName.get(), currentScore.get(), currentLevel.get(), LocalDateTime.now());
      highScoreManager.addEntryAndSave(entry);
      setChanged();
      notifyObservers(new GameEvent(GameEventType.NEW_HIGHSCORE, entry));
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
    // add new lives after 20.000 and after every 60.000 points
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

  /**
   * adds a live after score thresholds or Player PowerType
   */
  private void increaeRemainingLives() {
    currentRemainingLives.set(currentRemainingLives.get() + 1);
    setChanged();
    notifyObservers(new GameEvent(GameEventType.NEW_LIFE));
    LOG.info("Increased number of lives to {}", currentRemainingLives.get());
  }

  /**
   * Called by the <code>paddleMovementTimeline</code> animation event to move the paddles.
   */
  private void paddleMovementLoop() {
    if (isPaused() || !isPlaying()) return; // no paddle movement when game is paused
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

      setChanged();
      notifyObservers(new GameEvent(GameEventType.LASER_SHOT, ls1, ls2));
    }
  }

  /**
   * is called when a user restarts a catched ball by pressing a key or mouse button *
   */
  public void releaseCaughtBall() {
    if (ballCatchedFlag) {
      ballCatchedFlag = false;
    }
  }

  /**
   * pauses a running game
   */
  public void pausePlaying() {
    if (!isPlaying()) return; // ignore if not playing
    isPaused.set(true);
    mainGameLoop.pause();
    LOG.info("Game paused");
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
   * resumes a paused running game
   */
  public void resumePlaying() {
    if (!isPlaying() && !isPaused()) return; // ignore if not playing
    isPaused.set(false);
    mainGameLoop.play();
    LOG.info("Game resumed");
  }

  /**
   * Called from controller by mouse move events. Moves the paddle according to the mouse's x
   * position when mouse is in window. The paddle's center will be set to the current mouse
   * position.
   *
   * @param mouseX
   */
  public void setMouseXPosition(double mouseX) {
    if (isPaused() || !isPlaying()) return;
    double x = mouseX;
    double halfPaddleWidth = paddleWidthProperty().get() / 2;
    if (x - halfPaddleWidth < 0.0) {
      x = halfPaddleWidth;
    } else if (x + halfPaddleWidth > playfieldWidth.get()) {
      x = playfieldWidth.get() - halfPaddleWidth;
    }
    if (paddleX.get() >= 0.0 && paddleX.get() + paddleWidth.get() <= playfieldWidth.get()) {
      paddleXProperty().set(x - halfPaddleWidth);
    } else if (paddleX.get() < 0.0) {
      paddleX.set(0);
    } else if (paddleX.get() + paddleWidth.get() > playfieldWidth.get()) {
      paddleX.set(playfieldWidth.get() - paddleWidth.get());
    }
  }

  /**
   * Cheat to skip a level even if it is not finished
   */
  public void skipLevelCheat() {
    brickLayout.resetMatrix();
    LOG.info("Cheat: Skip Level");
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

  public String getPlayerName() {
    return playerName.get();
  }

  public StringProperty playerNameProperty() {
    return playerName;
  }

  public void setPlayerName(final String playerName) {
    this.playerName.set(playerName);
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

  /**
   * @return the current brick layout
   */
  public BrickLayout getBrickLayout() {
    return brickLayout;
  }

  /**
   * @return the current fps
   */
  public double getFps() {
    return fps.get();
  }

  public PowerPillType getActivePower() {
    return activePower.get();
  }

  public List<HighScore.HighScoreEntry> getHighScoreManager() {
    return highScoreManager.getList();
  }
}
