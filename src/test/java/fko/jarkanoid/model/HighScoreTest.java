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

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HighScoreTest {

  private static final Logger LOG = LoggerFactory.getLogger(HighScoreTest.class);

  @Test
  void init() {
    final String folderPathPlain = "./var/";
    final String fileNamePlain = "highscore_test.csv";
    HighScore hs = new HighScore(folderPathPlain, fileNamePlain);
    hs.clear();
    for (int i = 1; i<20; i++) {
      hs.addEntry(new HighScore.HighScoreEntry("Test_" + i, 100 + i, 5, LocalDateTime.now()));
    }
    hs.saveToFile();
  }

  @Test
  void isHighScore() {
    final String folderPathPlain = "./var/";
    final String fileNamePlain = "highscore_test.csv";
    HighScore hs = new HighScore(folderPathPlain, fileNamePlain);
    assertTrue(hs.isHighScore(1000));
    assertFalse(hs.isHighScore(10));
  }

  @Test
  void addEntry() {
    final String folderPathPlain = "./var/";
    final String fileNamePlain = "highscore_test.csv";

    HighScore hs = new HighScore(folderPathPlain, fileNamePlain);

    // build up new list
    hs.clear();
    // safe and reopen
    hs.saveToFile();
    hs = new HighScore(folderPathPlain, fileNamePlain);
    // test if empty
    assertTrue(hs.getList().isEmpty());

    // add 10 entries
    for (int i = 1; i<=10; i++) {
      hs.addEntry(new HighScore.HighScoreEntry("First10_" + i, 100 + i, 5, LocalDateTime.now()));
    }
    // safe and reopen
    hs.saveToFile();
    hs = new HighScore(folderPathPlain, fileNamePlain);
    // test if size 10 and order
    assertEquals(10, hs.getList().size());
    assertEquals(110, hs.getList().get(0).score);
    assertEquals(101, hs.getList().get(hs.getList().size()-1).score);
    assertEquals(106, hs.getList().get(4).score);
    // as list is not yet full these should be new entries
    assertTrue(hs.isHighScore(133));
    assertTrue(hs.isHighScore(10));

    // add more entries in a not full list
    hs.addEntry(new HighScore.HighScoreEntry("Add2_" + 99, 133, 5, LocalDateTime.now()));
    hs.addEntry(new HighScore.HighScoreEntry("Add2_" + 99, 10, 5, LocalDateTime.now()));
    // safe and reopen
    hs.saveToFile();
    hs = new HighScore(folderPathPlain, fileNamePlain);
    // test if entries are added at the right place
    assertEquals(133, hs.getList().get(0).score);
    assertEquals(106, hs.getList().get(5).score);
    assertEquals(10, hs.getList().get(hs.getList().size()-1).score);
    // as list is not yet full these should be new entries
    assertTrue(hs.isHighScore(133));
    assertTrue(hs.isHighScore(10));

    // add 10 more entries to fill up from below
    for (int i = 20; i<30; i++) {
      hs.addEntry(new HighScore.HighScoreEntry("Add10_" + i, 100, 5, LocalDateTime.now()));
    }
    // safe and reopen
    hs.saveToFile();
    hs = new HighScore(folderPathPlain, fileNamePlain);
    // test size and entries
    assertEquals(15, hs.getList().size());
    assertEquals(hs.getList().get(0).score, 133);
    assertEquals(100, hs.getList().get(hs.getList().size()-1).score);
    assertEquals(hs.getList().get(10).score, 101);
    assertEquals(hs.getList().get(11).score, 100);

    // add 4 more entries to push out  up from below
    for (int i = 30; i<35; i++) {
      hs.addEntry(new HighScore.HighScoreEntry("Push_" + i, 1000, 5, LocalDateTime.now()));
    }
    // safe and reopen
    hs.saveToFile();
    hs = new HighScore(folderPathPlain, fileNamePlain);
    // test size and entries
    assertEquals(15, hs.getList().size());
    assertEquals(hs.getList().get(0).score, 1000);
    assertEquals(102, hs.getList().get(hs.getList().size()-1).score);
    assertEquals(133, hs.getList().get(5).score);
    assertEquals(110, hs.getList().get(6).score);

  }

}
