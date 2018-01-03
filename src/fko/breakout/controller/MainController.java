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

		System.out.println("HELLO from CONTROLLER");
		
	}

	/**
	 * @param model
	 */
	public void bindModelToView() {
		// playfield
		playfieldPane.prefWidthProperty().bind(model.playfieldWidthProperty());
		playfieldPane.prefHeightProperty().bind(model.playfieldHeightProperty());
		// paddle
		System.out.println(model.paddleWidthProperty().get());
		System.out.println(paddle.widthProperty().get());
		paddle.widthProperty().bind(model.paddleWidthProperty());
		paddle.heightProperty().bind(model.paddleHeightProperty());
		
		System.out.println(model.paddleXProperty().get()+":"+model.paddleYProperty().get());
		System.out.println(paddle.xProperty().get()+":"+paddle.yProperty().get());
		System.out.println(paddle.translateXProperty().get()+":"+paddle.translateYProperty().get());
		System.out.println(paddle.layoutXProperty().get()+":"+paddle.layoutYProperty().get());
		
		paddle.layoutXProperty().bind(model.paddleXProperty());
		paddle.layoutYProperty().bind(model.paddleYProperty());
		
//		// ball
//		ball.radiusProperty().bind(model.ballRadiusProperty());
//		ball.centerXProperty().bind(model.ballCenterXProperty());
//		ball.centerYProperty().bind(model.ballCenterYProperty());
		
	}
	
	/**
	 * @param b
	 */
	private void onPaddleLeftAction(boolean b) {
		if (b) model.setPaddleLeft(true);
		else model.setPaddleLeft(false);	
	}
	
	/**
	 * @param b
	 */
	private void onPaddleRightAction(boolean b) {
		if (b) model.setPaddleRight(true);
		else model.setPaddleRight(false);
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
	void keyPressedAction(KeyEvent event) {
		System.out.println("Key Pressed: "+event);
		switch (event.getCode()) {
		// game control
		case SPACE: 		; break;
		case ESCAPE:		; break;
		case P: 			; break;
		// paddle control
		case LEFT:		onPaddleLeftAction(true); break;
		case RIGHT:		onPaddleRightAction(true); break;
		default:
		}
		
	}

	@FXML
	void keyReleasedAction(KeyEvent event) {
		System.out.println("Key Released: "+event);
		switch (event.getCode()) {
		// game control
		case SPACE: 		; break;
		case ESCAPE:		; break;
		case P: 			; break;
		// paddle control
		case LEFT: 		onPaddleLeftAction(false); break;
		case RIGHT:		onPaddleRightAction(false); break;
		default:
		}
	}

	@FXML
	void mouseEnteredAction(MouseEvent event) {
		//System.out.println("Mouse Entered: "+event);
	}

	@FXML
	void mouseExitedAction(MouseEvent event) {
		//System.out.println("Mouse Exited: "+event);
	}

	@FXML
	void mouseMovedAction(MouseEvent event) {
		model.setMouseXPosition(event.getX());
	}

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
		System.out.println("Button StartStop: "+event);
	}
	
	@FXML
    void pauseResumeButtonAction(ActionEvent event) {
		System.out.println("Button PauseResume: "+event);
    }

	@FXML
	void soundButtonAction(ActionEvent event) {
		System.out.println("Button Sound: "+event);
	}



}
