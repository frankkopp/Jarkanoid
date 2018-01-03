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
package fko.breakout.controller;

import java.net.URL;
import java.util.ResourceBundle;

import fko.breakout.model.BreakOutModel;
import fko.breakout.view.MainView;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
/**
 * MainController
 * 02.01.2018
 * @author Frank Kopp
 */
public class MainController implements Initializable {

	private BreakOutModel model;
	private MainView view;

	/**
	 * @param model
	 */
	public MainController(BreakOutModel model) {
		this.model = model;
	}

	/**
	 * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

	/**
	 * @param view 
	 * @param model
	 */
	public void bindModelToView(MainView view) {
		this.view = view;

		// add keyboard handlers
		view.asParent().getScene().setOnKeyPressed(event -> keyPressedAction(event));
		view.asParent().getScene().setOnKeyReleased(event -> keyReleasedAction(event));

		// add mouse handlers
		view.asParent().getScene().setOnMouseMoved(event -> mouseMovedAction(event));

		// playfield
		playfieldPane.prefWidthProperty().bind(model.playfieldWidthProperty());
		playfieldPane.prefHeightProperty().bind(model.playfieldHeightProperty());

		// paddle
		paddle.widthProperty().bind(model.paddleWidthProperty());
		paddle.heightProperty().bind(model.paddleHeightProperty());
		paddle.xProperty().bind(model.paddleXProperty());
		paddle.yProperty().bind(model.paddleYProperty());

		// ball
		ball.radiusProperty().bind(model.ballRadiusProperty());
		ball.centerXProperty().bind(model.ballCenterXProperty());
		ball.centerYProperty().bind(model.ballCenterYProperty());

		// startstopButton text
		model.isPlayingProperty().addListener((v, o, n) -> {
			if (n == true) {
				startStopButton.setText("Stop");
			} else {
				startStopButton.setText("Play");
			}
		});

		// pauseResumeButton text
		model.isPausedProperty().addListener((v, o, n) -> {
			if (n == true) {
				pauseResumeButton.setText("Resume");
			} else {
				pauseResumeButton.setText("Pause");
			}
		});

		// pauseResumeButton text
		model.isSoundOnProperty().addListener((v, o, n) -> {
			if (n == true) {
				soundButton.setText("Sound Off");
			} else {
				soundButton.setText("Sound On");
			}
		});

		// Level text
		levelLabel.textProperty().bind(new SimpleStringProperty("Level ").concat(model.currentLevelProperty()));
		// remaining lives text
		livesLabel.textProperty().bind(model.currentRemainingLivesProperty().asString());
		// score text
		pointsLabel.textProperty().bind(model.currentScoreProperty().asString("%0,6d"));

		// game over splash text
		gameOverSplash.visibleProperty().bind(model.gameOverProperty());

	}

	private void keyPressedAction(KeyEvent event) {
		switch (event.getCode()) {
		// game control
		case SPACE: 		startStopButtonAction(new ActionEvent()); break;
		case P: 			pauseResumeButtonAction(new ActionEvent());; break;
		// paddle control
		case LEFT:		onPaddleLeftAction(true); break;
		case RIGHT:		onPaddleRightAction(true); break;
		default:
		}
	}

	private void keyReleasedAction(KeyEvent event) {
		switch (event.getCode()) {
		// game control
		case SPACE: 		break;
		case P: 			break;
		// paddle control
		case LEFT: 		onPaddleLeftAction(false); break;
		case RIGHT:		onPaddleRightAction(false); break;
		default:
		}
	}

	private void onPaddleLeftAction(boolean b) {
		if (b) model.setPaddleLeft(true);
		else model.setPaddleLeft(false);	
	}

	private void onPaddleRightAction(boolean b) {
		if (b) model.setPaddleRight(true);
		else model.setPaddleRight(false);
	}

	private void mouseMovedAction(MouseEvent event) {
		model.setMouseXPosition(event.getX());
	}

	/* ********************************************************
	 * FXML 
	 * ********************************************************/

	@FXML
	private Circle ball;

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
	void paddleMouseClickAction(MouseEvent event) {
		System.out.println("Mouse Click: "+event);
	}

	@FXML
	void paddleMouseReleasedAction(MouseEvent event) {
		System.out.println("Mouse Release: "+event);
	}

	@FXML
	void startStopButtonAction(ActionEvent event) {
		if (model.isPlaying()) {
			model.stopPlaying();
		} else {
			model.startPlaying();
		}
	}

	@FXML
	void pauseResumeButtonAction(ActionEvent event) {
		if (model.isPlaying()) {
			if (model.isPaused()) {
				model.resumePlaying();
			} else {
				model.pausePlaying();
			}
		}
	}

	@FXML
	void soundButtonAction(ActionEvent event) {
		if (model.isSoundOnProperty().get()) {
			model.isSoundOnProperty().set(false);
		} else {
			model.isSoundOnProperty().set(true);
		}
	}



}
