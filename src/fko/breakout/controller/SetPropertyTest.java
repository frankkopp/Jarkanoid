/**
MIT License

Copyright (c) 2017 Frank Kopp

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

import java.util.HashMap;

import fko.breakout.model.Ball;
import fko.breakout.model.BallManager;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.collections.SetChangeListener.Change;

/**
 * 
 */
public class SetPropertyTest {

  public static void main(String[] args) { 
    new SetPropertyTest(); 
  }
  
  HashMap<Ball, String> bvm = new HashMap<Ball, String>();

  public SetPropertyTest() {

    final SetProperty<Ball> ballManager = new SimpleSetProperty<>();
    ballManager.set(FXCollections.observableSet(new BallManager()));

    ballManager.addListener(
        (SetChangeListener.Change<?> change) -> updateBallSet(change));

    ballManager.add(new Ball(100, 100, 1, 1, 1));
    ballManager.add(new Ball(100, 100, 2, 1, 1));
    ballManager.add(new Ball(100, 100, 3, 1, 1));
    ballManager.add(new Ball(100, 100, 4, 1, 1));
    ballManager.add(new Ball(100, 100, 5, 1, 1));

    ballManager.clear();

  }

  /**
   * @param change
   * @return
   */
  private void updateBallSet(Change<?> change) {
    if (change.wasAdded()) {
      final Ball a = (Ball) change.getElementAdded();
      System.out.println("ADD: "+a);
      bvm.put(a, a.toString());
    } else if (change.wasRemoved()) {
      final Ball b = (Ball) change.getElementRemoved();
      System.out.println("DEL: "+b);
      bvm.remove(b);
    }
    
    System.out.println("BVM:");
    for (Ball b : bvm.keySet()) {
      System.out.println(bvm.get(b));
    }
    System.out.println();
  }

}
