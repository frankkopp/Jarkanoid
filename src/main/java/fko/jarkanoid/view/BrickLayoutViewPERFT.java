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
package fko.jarkanoid.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fko.jarkanoid.model.Brick;
import fko.jarkanoid.model.BrickLayout;
import fko.jarkanoid.model.LevelLoader;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * BrickLayoutViewPERFT
 * <p>
 * Test class to test optimizations for rendering the Bricks 
 * <p>
 * 05.01.2018
 * @author Frank Kopp
 */
public class BrickLayoutViewPERFT extends Application {

	public static void main(String[] args) { 
		launch(); 
	}

	BrickLayout brickLayout;
	BrickLayoutView blv;
	Pane root = new Pane();
	Stage stage;
	
	List<Long> timings = new ArrayList<>(100000);

	public BrickLayoutViewPERFT() {
		LevelLoader ll = LevelLoader.getInstance();
		Brick[][] bricks = ll.getLevel(1);

		brickLayout = new BrickLayout(
				new SimpleDoubleProperty(780),
				new SimpleDoubleProperty(710));

		brickLayout.setMatrix(bricks);

		blv = new BrickLayoutView();

	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		stage = primaryStage;
		stage.setScene(new Scene(root, 780, 710));
		stage.setX(1000.0);
		stage.setY(0.0);
		blv.draw(brickLayout);
		root.getChildren().add(blv);
		stage.show();
		
		System.out.println("Warm Up");
		Timeline ticker = new Timeline();
		ticker.setCycleCount(500);
		KeyFrame warmup = new KeyFrame(Duration.millis(1000/120), e -> blv.draw(brickLayout));
		ticker.getKeyFrames().add(warmup);
		ticker.setOnFinished((e) -> afterWarmUp());
		ticker.play();
	}

	private void afterWarmUp() {
		System.out.println("Start timing");
		Timeline ticker = new Timeline();
		ticker.setCycleCount(1200);
		KeyFrame test = 	new KeyFrame(Duration.millis(1000/120), e -> testDraw());
		ticker.getKeyFrames().add(test);
		ticker.setOnFinished((e) -> finished());
		ticker.play();
	}

	void testDraw() {
			final long startNano = System.nanoTime();
			blv.draw(brickLayout);
			final long timeInNano = System.nanoTime()-startNano;
			//System.out.println(String.format("Drawing bricks took %,d ns", timeInNano));
			timings.add(timeInNano);
	}
	
	void finished() {
		
		System.out.println(String.format("Number of Runs: %d",timings.size()));
		System.out.println(String.format("Max: %,d", Collections.max(timings)));
		System.out.println(String.format("Min: %,d", Collections.min(timings)));
		System.out.println(String.format("Avg: %,.2f", timings.stream().mapToDouble(a -> a).average().getAsDouble()));
		
		System.exit(0);
	}

}
