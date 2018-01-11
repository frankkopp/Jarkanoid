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
package fko.breakout.model;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fko.breakout.BreakOut;
import javafx.scene.media.AudioClip;

/**
 * Sounds
 * <p>
 * Loads all sounds and maps them to an enumeration for easy access.
 * <p>
 */
public final class SoundManager {

  /**
   * All available audio clips of this class
   */
  public enum Clips {
    // ENUM		Filename w/o .wav
    WALL 		("WallHit"),
    PADDLE 		("PaddleHit"),
    BRICK_S 		("BrickHit_Special"),
    BRICK		("BrickHit_Normal"),
    BALL_LOST	("Ball_Lost");

    private final String _name;

    Clips(String name) {
      _name = name;
    }
  }

  // Singleton instance
  private static SoundManager instance = null;

  // folder to all sound files
  private static final String SOUND_FOLDER = "/sounds/";

  // available sounds mapped by the enum
  private final Map<Clips, AudioClip> _sounds;

  // sound on/off
  private boolean soundOn = true;

  /**
   * Get theSounds instance with all sounds available 
   */
  public synchronized static SoundManager getInstance() {
    if (instance == null) {
      instance = new SoundManager();
    }
    return instance;
  }

  /**
   * Create an object with all Tetris sounds available 
   */
  private SoundManager() {
    _sounds = new HashMap<>();
    // for all defined values in ENUM Clips
    // read in the Clip and store them in the Map
    Arrays.stream(Clips.values())
    .forEach(c -> {
      final String filename = SOUND_FOLDER + c._name+".wav";
      final URL url = BreakOut.class.getResource(filename);
      // create AudioInputStream object
      if (url != null) {
        AudioClip clip = new AudioClip(url.toExternalForm());
        _sounds.put(c, clip);
      } else {
        BreakOut.criticalError("Sound file: "+filename+" cannot be loaded!");
      }
    });
  }

  /**
   * Plays the given clip once.
   * @param c enum from Clips
   */
  public void playClip(Clips c) {
    if (_sounds.get(c) == null || !soundOn) return;
    AudioClip clip = _sounds.get(c);
    clip.play();
  }

  /**
   * Turn sound playing off. Causes the call to playClip to be ignored.
   */
  public void soundOff() {
    soundOn = false;
  }

  /**
   * Turns sound playing on.
   */
  public void soundOn() {
    soundOn = true;
  }

  /**
   * @return true if sound playing is turned on
   */
  public boolean isSoundOn() {
    return soundOn;
  }

}