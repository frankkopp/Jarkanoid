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
package fko.jarkanoid.controller;

import fko.jarkanoid.Jarkanoid;
import fko.jarkanoid.events.GameEvent;
import fko.jarkanoid.model.*;
import fko.jarkanoid.model.SoundManager.Clips;
import fko.jarkanoid.recorder.Recorder;
import fko.jarkanoid.view.MainView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

/**
 * MainController
 * <p>
 * <p>The Controller sets up additional ui elements after the FXML loader has done its
 * initialization. The FXML loader calls the Controller's initialize() method.<br>
 * The Controller also receives all input and events from the user interface and the model and
 * executes the appropriate ui updates and model actions. The UI calls the actions methods directly.
 * The model signals via the Observer Interface and Property Bindings that the model has changed and
 * the UI should update its views.
 *
 * @author Frank Kopp
 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
 */
public class MainController implements Initializable, Observer {

  private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

  // handles to model and view
  private final GameModel model;
  // main.resources.sounds
  private final SoundManager sounds = SoundManager.getInstance();
  private MainView view;

  // FXML injected fields
  @FXML
  private Circle recordingIndicator;
  @FXML
  private Button startStopButton;
  @FXML
  private Button pauseResumeButton;
  @FXML
  private Button soundButton;
  @FXML
  private Text levelLabel;
  @FXML
  private Text livesLabel;
  @FXML
  private Text pointsLabel;
  @FXML
  private Pane playfieldPane;
  @FXML
  private Rectangle ceilingWall;
  @FXML
  private Rectangle leftWall;
  @FXML
  private Rectangle rightWall;
  @FXML
  private Rectangle paddle;
  @FXML
  private Text gameOverSplash;
  @FXML
  private VBox gamePreStartSplash;
  @FXML
  private TableView highScoreTable;
  @FXML
  private TextField playerNameTextField;

  // to avoid keeping space pressed and therefore having rapid fire
  private boolean spaceIsPressed = false;

  /**
   * Constructor the MainController
   *
   * @param model
   */
  public MainController(GameModel model) {
    this.model = model;
  }

  /**
   * Called by FXMLLoader
   *
   * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
   */
  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // empty
  }

  /**
   * Bind views to model data mostly through property bindings but also through Observer pattern.
   * <br>
   * See update()
   *
   * @param view
   */
  public void bindViewToModel(MainView view) {

    LOG.info("Binding view to model");

    this.view = view;

    // add controller as listener of model for GameEvents
    model.addObserver(this);

    // scene title
    String tmpTitle = Jarkanoid.getPrimaryStage().getTitle();
    Jarkanoid.getPrimaryStage()
             .titleProperty()
             .bind(
                     new SimpleStringProperty(tmpTitle + " (fps:")
                             .concat(model.fpsProperty().asString("%.2f"))
                             .concat(")")
                             .concat(" (Version: ")
                             .concat(Jarkanoid.VERSION)
                             .concat(")"));

    // add keyboard handlers
    view.asParent().getScene().setOnKeyPressed(this::keyPressedAction);
    view.asParent().getScene().setOnKeyReleased(this::keyReleasedAction);

    // add mouse handlers
    view.asParent().getScene().setOnMouseMoved(this::mouseMovedAction);
    view.asParent().getScene().setOnMousePressed(this::mousePressedAction);

    // playfield dimensions
    playfieldPane.prefWidthProperty().bind(model.playfieldWidthProperty());
    playfieldPane.prefHeightProperty().bind(model.playfieldHeightProperty());

    // paddle dimensions and location
    paddle.widthProperty().bind(model.paddleWidthProperty());
    paddle.heightProperty().bind(model.paddleHeightProperty());
    paddle.xProperty().bind(model.paddleXProperty());
    paddle.yProperty().bind(model.paddleYProperty());

    // update handler for ball manager
    //noinspection unchecked
    model.getBallManager()
         .addListener(
                 (ListChangeListener.Change<?> change) -> view.updateBallList((Change<Ball>) change));

    // update handler for laser shot manager
    //noinspection unchecked
    model.getLaserShotManager()
         .addListener(
                 (ListChangeListener.Change<?> change) ->
                         view.updateLaserShotList((Change<LaserShot>) change));

    // update handler for fallingPills
    //noinspection unchecked
    model.fallingPowerPillsProperty()
         .addListener(
                 (ListChangeListener.Change<?> change) ->
                         view.updateFallingPillList((Change<PowerPill>) change));

    // update handler for active power ups
    //noinspection unchecked
    model.activePowerProperty()
         .addListener((observable, oldValue, newValue) -> updateActivePower(oldValue, newValue));

    // startstopButton text updater
    model.isPlayingProperty()
         .addListener(
                 (observable, oldValue, newValue) -> {
                   if (newValue) {
                     startStopButton.setText("Stop");
                   } else {
                     startStopButton.setText("Play");
                   }
                 });

    // pauseResumeButton text updater
    model.isPausedProperty()
         .addListener(
                 (observable, oldValue, newValue) -> {
                   if (newValue) {
                     pauseResumeButton.setText("Resume");
                   } else {
                     pauseResumeButton.setText("Pause");
                   }
                 });

    // Level text
    levelLabel.textProperty()
              .bind(new SimpleStringProperty("Level ").concat(model.currentLevelProperty()));
    // remaining lives text
    livesLabel.textProperty().bind(model.currentRemainingLivesProperty().asString());
    // score text
    pointsLabel.textProperty().bind(model.currentScoreProperty().asString("%06d"));

    // game over splash text
    gameOverSplash.visibleProperty().bind(model.gameOverProperty());

    // pre start splash
    gamePreStartSplash.visibleProperty().bind(model.isPlayingProperty().not());

    // bind bidirectional playerName
    playerNameTextField.textProperty().bindBidirectional(model.playerNameProperty());

    // to not have focus on playerNameTextField
    view.asParent().requestFocus();
  }

  /**
   * Called by a property binding to the active power property.
   * Is used to accommodate animations when a new power gets active and
   * the old power gets inactive. Not all powers have or need animations.
   *
   * @param oldPowerType
   * @param newPowerType
   */
  private void updateActivePower(final PowerPillType oldPowerType,
                                 final PowerPillType newPowerType) {

    switch (oldPowerType) {
      case NONE:
        break;
      case LASER:
        if (newPowerType != PowerPillType.LASER) {
          view.laserPaddle(false);
        }
        break;
      case ENLARGE:
        if (newPowerType != PowerPillType.ENLARGE) {
          sounds.playClip(Clips.POWER_S);
        }
        break;
      case CATCH:
        break;
      case SLOW:
        break;
      case BREAK:
        break;
      case DISRUPTION:
        break;
      case PLAYER:
        break;
    }

    switch (newPowerType) {
      case NONE:
        break;
      case LASER:
        view.laserPaddle(true);
        break;
      case ENLARGE:
        sounds.playClip(Clips.POWER_E);
        break;
      case CATCH:
        break;
      case SLOW:
        break;
      case BREAK:
        break;
      case DISRUPTION:
        break;
      case PLAYER:
        break;
    }
  }

  /**
   * We use the Observable notification for certain events to enable animations and sound. Most
   * other model changes are handled through Property Bindings.
   *
   * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
   */
  @Override
  public void update(Observable o, Object e) {
    if (!(e instanceof GameEvent)) {
      final String msg = "Unknown event type. Event is not of type GameEvent";
      LOG.error(msg);
      throw new RuntimeException(msg);
    }

    GameEvent gameEvent = (GameEvent) e;

    final Object[] param = (Object[]) gameEvent.getEventParameter();

    // define actions for different events
    switch (gameEvent.getEventType()) {
      case NONE:
        break;
      case HIT_PADDLE:
        view.paddleHit((Ball) param[0]);
        sounds.playClip(Clips.PADDLE);
        break;
      case HIT_WALL:
        view.ballHit((Ball) param[0]);
        // main.resources.sounds.playClip(Clips.WALL);
        break;
      case HIT_BRICK:
        handleHitBrickEvent(gameEvent);
        view.getBrickLayoutView().draw(model.getBrickLayout());
        break;
      case LAST_BALL_LOST:
        sounds.playClip(Clips.BALL_LOST);
        break;
      case BALL_LOST:
        break;
      case NEW_BALL:
        break;
      case LEVEL_COMPLETE:
        break;
      case LEVEL_START:
        sounds.playClip(Clips.NEW_LEVEL);
        view.getBrickLayoutView().draw(model.getBrickLayout());
        break;
      case GAME_START:
        sounds.stopClip(Clips.FINAL); // stops final music in case it was still playing
        // sounds.playClip(Clips.INTRO);
        break;
      case GAME_STOPPED:
        view.getBrickLayoutView().draw(model.getBrickLayout());
        break;
      case GAME_OVER:
        gameOverSplash.setText("GAME OVER");
        break;
      case LASER_HIT:
        view.getBrickLayoutView().draw(model.getBrickLayout());
        break;
      case LASER_SHOT:
        sounds.playClip(Clips.LASER);
        break;
      case GAME_WON:
        sounds.playClip(Clips.FINAL);
        gameOverSplash.setText("  THE END");
        break;
      case CAUGHT:
        sounds.playClip(Clips.CAUGHT);
        break;
      case LASER_ON:
        break;
      case LASER_OFF:
        break;
      case NEW_LIFE:
        sounds.playClip(Clips.NEW_LIFE);
        break;
      case NEW_HIGHSCORE:
        view.getHighScoreListView().updateList((HighScore.HighScoreEntry) param[0]);
        //showHighScoreEditor();
        break;
      default:
    }
  }

  /**
   * @param event
   */
  private void handleHitBrickEvent(GameEvent event) {
    if (event.getEventParameter() != null) {
      final Object[] param = (Object[]) event.getEventParameter();
      final int row = (int) param[0];
      final int col = (int) param[1];
      final Ball ball = (Ball) param[2];
      if (model.getBrickLayout().getBrick(row, col) == null) {
        sounds.playClip(Clips.BRICK);
      } else {
        view.brickHit(row, col);
        sounds.playClip(Clips.BRICK_S);
      }
      view.ballHit(ball);
    }
  }

  /**
   * Handles key pressed events
   *
   * @param event
   */
  private void keyPressedAction(KeyEvent event) {
    switch (event.getCode()) {
      // game control
      case ESCAPE: // fall through
      case ENTER: // fall through
      case N:
        startStopButtonAction(new ActionEvent());
        break;
      case SPACE:
        restartCaughtBallAction(new ActionEvent());
        if (!spaceIsPressed) { // to avoid auto keyboard repeat for laser
          spaceIsPressed = true;
          model.shootLaser();
        }
        break;
      case P:
        pauseResumeButtonAction(new ActionEvent());
        break;
      case S:
        soundButtonAction(new ActionEvent());
        break;
      case R:
        recordingAction();
        break;
      case Q:
        if (event.isControlDown()) {
          model.skipLevelCheat();
        }
        break;

      // paddle control
      case A: // fall through
      case LEFT:
        onPaddleLeftAction(true);
        break;
      case D: // fall through
      case RIGHT:
        onPaddleRightAction(true);
        break;
      default:
    }
  }

  /**
   * Handles key released events
   *
   * @param event
   */
  private void keyReleasedAction(KeyEvent event) {
    switch (event.getCode()) {
      // paddle control
      case A:
      case LEFT:
        onPaddleLeftAction(false);
        break;
      case D:
      case RIGHT:
        onPaddleRightAction(false);
        break;
      case SPACE:
        spaceIsPressed = false;
      default:
    }
  }

  @FXML
  void changePlayerNameAction(ActionEvent event) {
    LOG.debug("Change player name action: {}", event);
    // the value itself has a bidirectional binding to model property
    // to not have focus on playerNameTextField
    view.asParent().requestFocus();
  }

  @FXML
  void playerNameTextFieldClickedAction(MouseEvent event) {
    LOG.debug("Change player name field clicked action: {}", event);
    // the value itself has a bidirectional binding to model property
    // to not have focus on playerNameTextField
    playerNameTextField.selectAll();
  }

  @FXML
  void recordingAction(MouseEvent event) {
    recordingAction();
  }

  private void recordingAction() {
    Recorder recorder = Jarkanoid.getRecorder();
    if (recorder.isRunning()) {
      LOG.info("User requested stop recording");
      recorder.stop();
      recordingIndicator.setFill(Color.GREEN);
    } else {
      LOG.info("User requested start Recording");
      recorder.start(32);
      recordingIndicator.setFill(Color.RED);
    }
  }

  /**
   * Action when the mouse left button has been clicked.
   *
   * @param mouseEvent
   */
  private void mousePressedAction(final MouseEvent mouseEvent) {
    model.releaseCaughtBall();
    model.shootLaser();
    view.asParent().requestFocus();
  }

  /**
   * Paddle action
   *
   * @param b
   */
  private void onPaddleLeftAction(boolean b) {
    if (b) {
      model.setPaddleLeft(true);
    } else {
      model.setPaddleLeft(false);
    }
  }

  /**
   * Paddle action
   *
   * @param b
   */
  private void onPaddleRightAction(boolean b) {
    if (b) {
      model.setPaddleRight(true);
    } else {
      model.setPaddleRight(false);
    }
  }

  /**
   * Toggles Start/Stop game
   */
  @FXML
  private void startStopButtonAction(ActionEvent event) {
    if (model.isPlaying()) {
      LOG.info("User requested stop playing");
      model.stopPlaying();
    } else {
      LOG.info("User requested start playing");
      model.startPlaying();
    }
  }

  /**
   * Called when user wants to restart a caught Ball
   */
  private void restartCaughtBallAction(final ActionEvent actionEvent) {
    model.releaseCaughtBall();
  }

  /**
   * Toggles Pause/Resume game
   *
   * @param event
   */
  @FXML
  private void pauseResumeButtonAction(ActionEvent event) {
    if (model.isPlaying()) {
      if (model.isPaused()) {
        LOG.info("User requested resume playing");
        model.resumePlaying();
      } else {
        LOG.info("User requested pause playing");
        model.pausePlaying();
      }
    }
  }

  /**
   * Toggles sound option
   *
   * @param event
   */
  @FXML
  private void soundButtonAction(ActionEvent event) {
    if (sounds.isSoundOn()) {
      LOG.info("User requested sound off");
      soundButton.setText("Sound On");
      sounds.soundOff();
    } else {
      LOG.info("User requested sound on");
      soundButton.setText("Sound Off");
      sounds.soundOn();
    }
  }

  /**
   * Mouse action
   */
  private void mouseMovedAction(MouseEvent event) {
    model.setMouseXPosition(event.getX());
  }

  @FXML
  void paddleMouseClickAction(MouseEvent event) {
    // not used
  }

  @FXML
  void paddleMouseReleasedAction(MouseEvent event) {
    // not used
  }
}
