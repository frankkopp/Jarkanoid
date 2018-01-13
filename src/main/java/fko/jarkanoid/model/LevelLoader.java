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
package fko.jarkanoid.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import fko.jarkanoid.Jarkanoid;
import fko.jarkanoid.model.exceptions.LevelLoaderFormatException;
import fko.jarkanoid.model.exceptions.LevelLoaderIOException;
import fko.jarkanoid.model.exceptions.LevelLoaderNoLevelFilesException;

/**
 * LevelLoader
 * <p>
 * Loads main.resources.levels from level text files.<br>
 * <p>
 * File format:<br>
 * Lines beginning with '#' mark comments and are ignored
 * There must be 18 rows.<br>
 * Each row has 11 token.<br>
 * Each token has 4 characters and a space in between.<br>
 * A token with 'ORNO' represents an empty cell<br>
 * A taken with 4 letters represent a brick. The first two letters are 
 * from the <code>BrickType.sign</code> and the last two letters represent
 * the fixed power type this brick has. (Usually powers are randomized but
 * this allows for powers to be fixed to certain bricks). 
 *  
 * <p>
 * 04.01.2018
 * @author Frank Kopp
 */
public final class LevelLoader {

	private static LevelLoader instance; // Singleton

	private String defaultFolder = "/levels/";
	private String preFix = "Level-";
	private String fileType = ".txt";

	private final Map<String,Brick[][]> levels = new HashMap<>();

	/**
	 * Returns Singleton instance of this class.
	 * @return singleton instance of this class
	 * @throws LevelLoaderFormatException 
	 */
	public static LevelLoader getInstance() {
		if (instance == null) { instance = new LevelLoader(); }
		return instance;
	}

	/**
	 * For Unit Testing only.
	 * 
	 * @param folder
	 * @param preFix
	 * @param fileType
	 * @return returns a new instance for the purpose of unit testing 
	 */
	protected static LevelLoader getNewInstanceForUnitTest(String folder, String preFix, String fileType) {
		return new LevelLoader(folder, preFix, fileType);
	}

	/**
	 * Private constructor to create the singleton instance.
	 */
	private LevelLoader() {
		initialize(defaultFolder); // default folder
	}

	/**
	 * For Unit Testing only.
	 * Private constructor to create the singleton instance.
	 * @param folder
	 * @param preFix
	 * @param fileType
	 */
	private LevelLoader(String folder, String preFix, String fileType) {
		this.defaultFolder = folder;
		this.preFix = preFix;
		this.fileType = fileType;
		initialize(folder);
	}

	/**
	 * @param i (&gt; 0 and &lt; maxAvailableLevel)
	 * @return the level matrix or null of no such level
	 */
	public Brick[][] getLevel(int i) {
		if (i < 1) return null;
		// get the matrix from the Map
		final Brick[][] myMatrix = levels.get(Integer.toString(i));
		// no more main.resources.levels?
		if (myMatrix == null) return null;
		// when more main.resources.levels return a deep copy
		final Brick[][] myNewMatrix = new Brick[BrickLayout.ROWS][BrickLayout.COLUMNS];
		for (int row=0; row<BrickLayout.ROWS; row++) {
			for (int col=0; col<BrickLayout.COLUMNS;col++) {
				myNewMatrix[row][col] = myMatrix[row][col] == null ? null : new Brick(myMatrix[row][col]);
			}
		}
		return myNewMatrix;
	}

	/**
	 * @param folder
	 * @throws LevelLoaderNoLevelFilesException
	 */
	protected void initialize(String folder) throws LevelLoaderNoLevelFilesException {
		List<String> files = getLevelFiles(folder);
		if (files.isEmpty()) {
			throw new LevelLoaderNoLevelFilesException("Level load could not find any level files.");
		}
		for (String file : files) {
			final List<String> lines = getLinesFromLevelFile(folder, file);
			// read lines into data structure
			processLinesFromLevel(file, lines);
		}
	}

	/**
	 * getLevelFiles from folder.
	 * <p>
	 * Protected instead for private for unit testing.
	 * 
	 * @param folder
	 * @return file names in folder matching the file pattern "Level-&lt;number&gt;.txt"
	 * @throws LevelLoaderIOException
	 */
	protected List<String> getLevelFiles(String folder) throws LevelLoaderIOException {
		List<String> files;

		/*
		 * This is really SUPER ugly but I haven' found another way yet to get all 
		 * files from a resource directory when running from IDE AND running from JAR file.
		 */
		final File jarFile = 
				new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

		if(jarFile.isFile()) {  // Run with JAR file

			JarFile jar;
			try {
				jar = new JarFile(jarFile);
				
				String filterString = folder.substring(1) + preFix;

				files = jar.stream()
						.filter(f -> f.getName().lastIndexOf(filterString) >= 0 ) // match "Level-"
						.filter(f -> f.getName().endsWith(fileType)) // match ".txt"
						.map(f -> f.getName().substring(f.getName().lastIndexOf(preFix)))
						.collect(Collectors.toList());

				jar.close();

			} catch (IOException e) {
				throw new LevelLoaderIOException(e);
			}

		} else { // Run with IDE

			final URL folderURL = Jarkanoid.class.getResource(folder);
			if (folderURL == null) {
				throw new LevelLoaderIOException("While loading main.resources.levels: Folder not found: "+folder);
			}

			String filterString = preFix;

			try {
				
				String platformIndependedPathString = folderURL.getPath().replaceFirst("^/(.:/)", "$1");
				
				files = Files.list(FileSystems.getDefault().getPath(platformIndependedPathString))
						.map(m -> m.toFile().getName())
						.filter(f -> f.lastIndexOf(filterString) >= 0 ) // match "Level-"
						.filter(f -> f.endsWith(fileType)) // match "*.txt"
						.collect(Collectors.toList());
			} catch (IOException e) {
				throw new LevelLoaderIOException(e);
			}
		}
		return files;
	}

	/**
	 * getLinesFromLevelFile
	 * <p>
	 * Protected instead for private for unit testing.
	 * 
	 * @param folder
	 * @param file
	 * @return
	 */
	protected List<String> getLinesFromLevelFile(String folder, String file) {
		final InputStream fileStream = Jarkanoid.class.getResourceAsStream(folder+file);
		if (fileStream == null) {
			throw new LevelLoaderIOException(
					String.format("While loading level file %s: File not found!",folder+file));
		}

		final InputStreamReader isr = new InputStreamReader(fileStream, Charset.defaultCharset());
		final BufferedReader br = new BufferedReader(isr);

		final List<String> lines = new ArrayList<>();
		String line;
		try {
			line = br.readLine();
			while (line != null) {
				lines.add(line.trim());
				line = br.readLine();
			}
		} catch (IOException e) {
			throw new LevelLoaderIOException(e);
		}
		return lines;
	}

	/**
	 * processLinesFromLevel
	 * <p>
	 * Protected instead for private for unit testing.
	 * 
	 * @param file
	 * @param lines
	 * @throws LevelLoaderFormatException
	 */
	protected void processLinesFromLevel(String file, List<String> lines) throws LevelLoaderFormatException {

		Brick[][] tmpMatrix = new Brick[BrickLayout.ROWS][BrickLayout.COLUMNS];

		String matchString = "^(--|GY|OR|CY|GR|RE|BL|PU|YE|SI|GO)(--|NO|LA|EN|CA|SL|BR|DI|PL)$";

		int validLineCounter = 0;
		for (int row=0; row<lines.size(); row++) {

			if (lines.get(row).isEmpty() || // remove empty lines
					lines.get(row).startsWith("#")) { // remove comment lines
				continue;
			}
			validLineCounter++;
			String[] rowItems = lines.get(row).split(" ");
			if (rowItems.length != BrickLayout.COLUMNS) { // check if 13 columns
				throw new LevelLoaderFormatException(
						String.format("Bad row format in %s at line %d. Expected %d columns, found %d"
								, file, row+1, BrickLayout.COLUMNS, rowItems.length));
			}
			for (int col=0; col<rowItems.length; col++) {
				if (rowItems[col].length() != 4
						|| !rowItems[col].trim().matches(matchString)) {
					throw new LevelLoaderFormatException(
							String.format("Bad item format in %s at line %d column %d", file, row+1, col+1));
				}
				if (validLineCounter > 18) { // check if more than 18 rows
					throw new LevelLoaderFormatException(
							String.format("Bad format in %s. Expected 18 lines, found %d.", file, validLineCounter));
				}
				tmpMatrix[validLineCounter-1][col] = itemToBrick(rowItems[col].trim());
			}
		}

		if (validLineCounter < BrickLayout.ROWS) { // check if less than 18 rows
			throw new LevelLoaderFormatException(
					String.format("Bad format in %s. Expected 18 lines, found %d.", file, validLineCounter));
		}

		String level = file.substring(preFix.length(), file.lastIndexOf(fileType));
		levels.put(level, tmpMatrix);
	}

	/**
	 * Converts a string with 4 letters to a Brick instance. 
	 * "ORNO" or any invalid string return null.<br>
	 * Otherwise the first two letters represent the <code>BrickType</code> and the
	 * last two letters the <code>BrickPowerType</code>.
	 * @param string
	 * @return a Brick instance from the given string. Null if not a valid Brick token.
	 */
	public static Brick itemToBrick(String string) {
		// ignore the empty string
		if (string.length() != 4 || string.equals("----")) return null;

		String bricktype = string.substring(0,2);
		String powertype = string.substring(2,4);

		BrickType bt;
		PowerPillType bpt;

		switch (bricktype ) {
		case "GY": bt = BrickType.GREY; break;
		case "OR": bt = BrickType.ORANGE; break;
		case "CY": bt = BrickType.CYAN; break;
		case "GR": bt = BrickType.GREEN; break;
		case "RE": bt = BrickType.RED; break;
		case "BL": bt = BrickType.BLUE; break;
		case "PU": bt = BrickType.PURPLE; break;
		case "YE": bt = BrickType.YELLOW; break;
		case "SI": bt = BrickType.SILVER; break;
		case "GO": bt = BrickType.GOLD; break;
		default: throw new LevelLoaderFormatException(String.format("Unkwon brick type: %s", bricktype));
		}

		switch (powertype) {
		case "NO": bpt = PowerPillType.NONE; break;
		case "LA": bpt = PowerPillType.LASER; break;
		case "EN": bpt = PowerPillType.ENLARGE; break;
		case "CA": bpt = PowerPillType.CATCH; break;
		case "SL": bpt = PowerPillType.SLOW; break;
		case "BR": bpt = PowerPillType.BREAK; break;
		case "DI": bpt = PowerPillType.DISRUPTION; break;
		case "PL": bpt = PowerPillType.PLAYER; break;
		default: throw new LevelLoaderFormatException(String.format("Unkwon brick powertype: %s", powertype));
		}

		return new Brick(bt, bpt);
	}

}
