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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.StrokeTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 
 */
public class AnimationTest extends Application {

	public static void main(String[] args) { 
		launch(); 
	}

	private Stage stage;
	final Pane root = new Pane();
	private final Circle ball;

	@Override
	public void start(Stage primaryStage) {
		stage = primaryStage;
		final Scene scene = new Scene(root, 400, 400);
		root.setBackground(new Background(new BackgroundFill(Color.BLACK, null, null)));
		stage.setScene(scene);
		stage.setX(1000.0);
		stage.setY(0.0);
		stage.show();

		scene.setOnKeyPressed((event) -> {
			switch (event.getCode()) {
			case SPACE: startAnimation(); break;
			case ESCAPE: stopAnimation(); break;
			default:
			}
		});

		combinedBallAnimation.play();

	}

	private final ScaleTransition ballScaleTransition;
	private final StrokeTransition ballStrokeTransition;
	private final ParallelTransition combinedBallAnimation;
	private ScheduledExecutorService executor;

	public AnimationTest() {
		ball = new Circle();
		ball.setFill(Color.BLUE);
		ball.setStroke(Color.WHITE);
		ball.setRadius(20.0);
		ball.setCenterX(200);
		ball.setCenterY(200);

		root.getChildren().add(ball);

		ballScaleTransition = new ScaleTransition(Duration.millis(50), ball);
		ballScaleTransition.setByX(0.5);
		ballScaleTransition.setByY(0.5);
		ballScaleTransition.setCycleCount(2);
		ballScaleTransition.setAutoReverse(true);

		ballStrokeTransition = new StrokeTransition(Duration.millis(50), ball);
		ballStrokeTransition.setToValue(Color.RED);
		ballStrokeTransition.setCycleCount(2);
		ballStrokeTransition.setAutoReverse(true);

		combinedBallAnimation = new ParallelTransition(ballScaleTransition, ballStrokeTransition);

	}

	private void startAnimation() {
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(() -> {
			//			ball.setCenterX(Math.random() * 400);
			//			ball.setCenterY(Math.random() * 400);
			combinedBallAnimation.play();
		}
		, 0
		, 40
		, TimeUnit.MILLISECONDS);
	}

	private void stopAnimation() {
		executor.shutdownNow();
	}


}
