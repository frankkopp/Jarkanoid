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
package fko.breakout.view;

import java.io.IOException;

import fko.breakout.controller.MainController;
import fko.breakout.model.BreakOutGame;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

/**
 * MainView
 * 02.01.2018
 * @author Frank Kopp
 */
public class MainView {

	private BreakOutGame model;
	private MainController controller;
	
	private final AnchorPane root;
	private final BrickLayoutView brickLayoutView = new BrickLayoutView();

	/**
	 * @param model
	 * @param controller2
	 * @throws IOException 
	 */
	public MainView(BreakOutGame model, MainController controller) throws IOException {
		this.model = model;
		this.controller = controller;

		// read FXML file and setup UI
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/BreakOutMainView.fxml"));
		fxmlLoader.setController(controller);
		root = (AnchorPane) fxmlLoader.load();
		
		Pane playFieldPane = (Pane) fxmlLoader.getNamespace().get("playfieldPane");
		playFieldPane.getChildren().add(brickLayoutView);
	}

	/**
	 * @return root pane from loaded FXML
	 */
	public Parent asParent() {
		return root;
	}

	/**
	 * @return the brickLayoutView
	 */
	public BrickLayoutView getBrickLayoutView() {
		return brickLayoutView;
	}

}
