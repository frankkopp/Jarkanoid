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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Reads and stores the highscore from and to file */
public class HighScore {

  private static final Logger LOG = LoggerFactory.getLogger(HighScore.class);

  // max number of entry to be written in db
  private static final int MAX_ENTRIES = 15;

  /* default value for folder */
  private static final String folderPathPlain = "./var/";
  private static final String fileNamePlain = "highscore.csv";
  private Path _folderPath = FileSystems.getDefault().getPath(folderPathPlain);
  private Path _filePath = FileSystems.getDefault().getPath(folderPathPlain, fileNamePlain);

  // the highscore list
  private List<HighScoreEntry> _list;

  /*
   * Reads the file and adds the entries to _list
   */
  public HighScore() {
    load();
  }

  public HighScore(String folderPathPlain, String fileNamePlain) {
    _folderPath = FileSystems.getDefault().getPath(folderPathPlain);
    _filePath = FileSystems.getDefault().getPath(folderPathPlain, fileNamePlain);
    load();
  }

  private void load() {
    LOG.info("Reading Highscore from file {}", _filePath);

    // Check if folder exists and if not try to create it.
    if (!Files.exists(_folderPath, LinkOption.NOFOLLOW_LINKS)) {
      LOG.warn(
        "While reading high score file: Path {} could not be found. Trying to create it.",
        _folderPath.toString());
      try {
        Files.createDirectories(_folderPath);
      } catch (IOException e) {
        LOG.error(
          String.format(
            "While reading high score file: Path %s could not be created.",
            _folderPath.toString()),
          e);
      }
    }

    // Check if file exists and if not create a empty file
    if (Files.notExists(_filePath, LinkOption.NOFOLLOW_LINKS)) {
      LOG.warn(
        "While reading high score file: File {} could not be found. Trying to create it.",
        _filePath.getFileName().toString());
      try {
        Files.createFile(_filePath);
      } catch (IOException e) {
        LOG.error(
          String.format(
            "While reading high score file: File %s could not be found. Trying to create it.",
            _filePath.getFileName().toString()),
          e);
      }
    }

    // read all lines from file
    Charset charset = StandardCharsets.ISO_8859_1;
    List<String> lines = null;
    try {
      lines = Files.readAllLines(_filePath, charset);
    } catch (CharacterCodingException e) {
      LOG.error(
        "Highscore file '{}' has wrong charset (needs to be ISO-8859-1) - not loaded!",
        _filePath);
    } catch (IOException e) {
      LOG.error("Highscore file '{}' could not be loaded!", _filePath);
    }

    if (lines != null) {
      // create list of high score entries
      _list = Collections.synchronizedList(new ArrayList<HighScoreEntry>(lines.size()));
      lines
        .parallelStream()
        .forEach(
          line -> {
            String[] parts = line.split(";");
            _list.add(
              new HighScoreEntry(
                parts[0].trim(),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                LocalDateTime.parse(parts[3].trim())));
          });

      sortList();

      LOG.info("Read {} entries from highscore file", _list.size());
    }
  }


  /**
   * Return the highscore list as unmodifiable list
   *
   * @return unmodifiable list of high score entries
   */
  public List<HighScoreEntry> getList() {
    return Collections.unmodifiableList(_list);
  }

  /**
   * Test if given score would make high score
   */
  public boolean isHighScore(int score) {
    if (_list.size() < MAX_ENTRIES) return true;
    else return score > _list.get(MAX_ENTRIES-1).score;
  }

  /**
   * Put a new entry into the highscore table
   */
  public void addEntry(String name, int score, int level, LocalDateTime date) {
    this.addEntry(new HighScoreEntry(name, score, level, date));
  }

  /**
   * Put a new entry into the highscore table
   */
  public void addEntry(HighScoreEntry newEntry) {
    _list.add(newEntry);
    sortList();
  }

  /**
   * Put a new entry into the highscore table
   */
  public void addEntryAndSave(
      String name, int score, int level, LocalDateTime date) {
    this.addEntryAndSave(new HighScoreEntry(name, score, level, date));
  }

  /**
   * Put a new entry into the highscore table and save to file
   *
   * @param newEntry
   * @return true if save was successful, false otherwise
   */
  public boolean addEntryAndSave(HighScoreEntry newEntry) {
    addEntry(newEntry);
    return saveFile();
  }

  /**
   * Save the highscore file
   *
   * @return true if success, false if error
   */
  public boolean saveToFile() {
    return saveFile();
  }

  /**
   * Clears all entries from list.
   * Should only be used in unit tests.
   */
  public void clear() {
    _list.clear();
  }

  /*
   * Save _list to file. Max MAX_ENTRIES are written.
   */
  private boolean saveFile() {
    Charset charset = Charset.forName("ISO-8859-1");
    // Use try-with-resource to get auto-closeable writer instance
    try (BufferedWriter writer = Files.newBufferedWriter(_filePath, charset, StandardOpenOption.TRUNCATE_EXISTING)) {
      _list
          .stream()
          .limit(MAX_ENTRIES)
          .forEach(
              (e) -> {
                try {
                  writer.write(e.toString() + System.lineSeparator());
                } catch (IOException e1) {
                  LOG.error(
                      String.format(
                          "While saving high score file: Highscore file \"%s\" could not be saved!",
                          _filePath),
                      e1);
                }
              });
    } catch (IOException e) {
      LOG.error("While saving high score file: Highscore file '{}' could not be saved!", _filePath, e);
      return false;
    }
    return true;
  }

  /*
   * sort the list with the highest score first
   */
  private void sortList() {
    _list.sort((HighScoreEntry e1, HighScoreEntry e2) -> e2.score - e1.score);
  }

  /** A entry in the Highscore list. */
  public static class HighScoreEntry {

    public final String name;
    public final int score;
    public final LocalDateTime date;
    public final int level;

    public HighScoreEntry(
        String name, int score, int level, LocalDateTime date) {
      this.name = name;
      this.score = score;
      this.level = level;
      this.date = date;
    }

    @Override
    public String toString() {
      return name
          + ";"
          + score
          + ";"
          + level
          + ";"
          + date.toString();
    }
  }
}
