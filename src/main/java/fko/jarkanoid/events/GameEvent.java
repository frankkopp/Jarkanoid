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
package fko.jarkanoid.events;

/**
 * GameEvents
 * <p>
 * Is used by the model to signal its listeners (mainly the view) when certain specific events
 * relevant for the BreakOut game occur. Usually when state of model changes significantly. Minor
 * changes are handle be Property Bindings.
 * <p>
 * 03.01.2018
 * @author Frank Kopp
 */
public class GameEvent {

  private final GameEventType eventType;
  private Object args;

  /**
   * Creates a GameEvent of a certain type.
   * @param eventType
   * @param args
   */
  public GameEvent(GameEventType eventType, Object...args) {
    this.eventType = eventType;
    this.args = args;
  }

  /**
   * @return event type of the event
   */
  public GameEventType getEventType() {
    return eventType;
  }

  /**
   * @return returns parameters which might have been added to an event. Usually very
   * event specific.
   */
  public Object getEventParameter() {
    return this.args;
  }

  @Override
  public String toString() {
    return "GameEvent [eventType=" + eventType + "]";
  }

  /**
   * GameEventType
   * <p>
   * Enumeration of possible game event types.
   * <p>
   * 06.01.2018
   * @author Frank Kopp
   */
  public enum GameEventType {
    NONE,
    HIT_PADDLE,
    HIT_WALL,
    HIT_BRICK,
    BALL_LOST,
    LAST_BALL_LOST,
    NEW_BALL,
    LEVEL_COMPLETE,
    LEVEL_START,
    GAME_START,
    GAME_STOPPED,
    GAME_OVER,
    GAME_WON,
    LASER_HIT,
    LASER_SHOT,
    CAUGHT,
    LASER_ON,
    LASER_OFF,
    NEW_LIFE,
    NEW_HIGHSCORE
  }
}
