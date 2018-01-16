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
package fko.jarkanoid.model;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fko.jarkanoid.Jarkanoid;
import javafx.scene.media.AudioClip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sounds
 * <p>
 * Loads all main.resources.sounds and maps them to an enumeration for easy access.
 * <p>
 */
public final class SoundManager {

  private static final Logger LOG = LoggerFactory.getLogger(SoundManager.class);

  /**
   * All available audio clips of this class
   */
  public enum Clips {
    // ENUM		Filename w/o .wav
    PADDLE 		("PaddleHit"),
    BRICK		("BrickHit"),
    BRICK_S 	("BrickHitSolid"),
    BALL_LOST	("Ball_Lost"),
    CAUGHT      ("Caught"),
    FINAL       ("FinalMusic"),
    HIT_ALIEN   ("HitAlien"),
    HIT_BOSS    ("HitBoss"),
    INTRO       ("Intro"),
    KILL_BOSS   ("KillBoss"),
    LASER       ("Laser"),
    NEW_LEVEL   ("NewLevel"),
    NEW_LIFE    ("NewLife"),
    PADDLE_HIT  ("PaddleHit"),
    POWER_E     ("Power_Enlarge"),
    POWER_S     ("Power_Enlarge_Shrink"),
    WALL_HIT    ("WallHit");

    private final String _name;

    Clips(String name) {
      _name = name;
    }
  }

  // Singleton instance
  private static SoundManager instance = null;

  // folder to all sound files
  private static final String SOUND_FOLDER = "/sounds/";

  // available main.resources.sounds mapped by the enum
  private final Map<Clips, AudioClip> _sounds;

  // sound on/off
  private boolean soundOn = true;

  /**
   * Get theSounds instance with all main.resources.sounds available
   */
  public synchronized static SoundManager getInstance() {
    if (instance == null) {
      instance = new SoundManager();
    }
    return instance;
  }

  /**
   * Create an object with all Tetris main.resources.sounds available
   */
  private SoundManager() {
    _sounds = new HashMap<>();
    // for all defined values in ENUM Clips
    // read in the Clip and store them in the Map
    Arrays.stream(Clips.values())
    .forEach(c -> {
      final String filename = SOUND_FOLDER + c._name+".wav";
      final URL url = Jarkanoid.class.getResource(filename);
      // create AudioInputStream object
      if (url != null) {
        AudioClip clip = new AudioClip(url.toExternalForm());
        _sounds.put(c, clip);
      } else {
        LOG.warn("Sound file {} cannot be loaded!", filename);
      }
    });
    LOG.info("Loaded {} sounds.", _sounds.size());
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
   * Stops the given clip if it is still playing.
   * @param c enum from Clips
   */
  public void stopClip(Clips c) {
    if (_sounds.get(c) == null || !soundOn) return;
    AudioClip clip = _sounds.get(c);
    if (clip.isPlaying()) clip.stop();
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
