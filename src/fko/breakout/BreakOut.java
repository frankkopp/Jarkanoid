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
package fko.breakout;

import java.net.URL;

import fko.breakout.controller.MainController;
import fko.breakout.model.BreakOutGame;
import fko.breakout.view.MainView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * BreakOut / Arkanoid Clone in Java
 * <p>
 * 
 * <p>
 * 02.01.2018
 * @author Frank Kopp
 * 
 * TODO: use Logger
 */
public class BreakOut extends Application {

	// VERSION
	public static final String VERSION = "0.3"; 

	/**
	 * Main
	 */
	public static void main(String[] args) {
		launch(args);
	}

	/**
	 * @see javafx.application.Application#start(javafx.stage.Stage)
	 */
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		BreakOutGame model = new BreakOutGame();
		MainController controller = new MainController(model);
		MainView view = new MainView(model, controller); 

		Scene scene = new Scene(view.asParent());

		controller.bindModelToView(view);

		primaryStage.setScene(scene);
		primaryStage.centerOnScreen();
		primaryStage.setResizable(false);
		
		primaryStage.setTitle("BreakOut Clone by Frank Kopp");

		// closeAction
		primaryStage.setOnCloseRequest(event -> {
			Platform.exit();
			System.exit(0);
			event.consume();
		});

		primaryStage.show();
	}

	/**
	 * @see javafx.application.Application#init()
	 */
	@Override
	public void init() throws Exception {
		super.init();
		final URL urlResource = BreakOut.class.getResource("/fonts/AstronomicMono.otf");
		if (urlResource == null) {
			criticalError("Font could not be found!");
		} else {
			final String url = urlResource.toExternalForm();
			if (Font.loadFont(url, 40) == null) {
				criticalError("Font could not be loaded!");
			};
		}
	}

	/**
	 * Clean up and exit the application
	 */
	public static void exit() {
		exit(0);
	}

	/**
	 * Clean up and exit the application
	 */
	private static void exit(int returnCode) {
		Platform.exit();
		System.exit(returnCode);
	}

	/**
	 * Called when there is an unexpected unrecoverable error.<br>
	 * Prints a stack trace together with a provided message.<br>
	 * Terminates with <tt>exit(1)</tt>.
	 * @param message to be displayed with the exception message
	 */
	public static void fatalError(String message, Object... args) {
		Exception e = new Exception(String.format(message, args));
		e.printStackTrace();
		exit(1);
	}

	/**
	 * Called when there is an unexpected but recoverable error.<br>
	 * Prints a stack trace together with a provided message.<br>
	 * @param message to be displayed with the exception message
	 */
	public static void criticalError(String message, Object... args) {
		Exception e = new Exception(String.format(message, args));
		e.printStackTrace();
	}

	/**
	 * Called when there is an unexpected minor error.<br>
	 * Prints a provided message.<br>
	 * @param message to be displayed
	 */
	public static void minorError(String message,  Object... args) {
		System.err.println(String.format(message, args));
	}
}
