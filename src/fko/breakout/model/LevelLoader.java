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
package fko.breakout.model;

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

import fko.breakout.BreakOut;
import fko.breakout.model.exceptions.LevelLoaderFormatException;
import fko.breakout.model.exceptions.LevelLoaderIOException;
import fko.breakout.model.exceptions.LevelLoaderNoLevelFilesException;

/**
 * LevelLoader
 * 04.01.2018
 * @author Frank Kopp
 * 
 * TODO: Code Documentation
 */
public class LevelLoader {

	private static LevelLoader instance; // Singleton

	private String defaultFolder = "/levels/";
	private String preFix = "Level-";
	private String fileType = ".txt";

	private final Map<String,Brick[][]> levels = new HashMap<String, Brick[][]>();

	/**
	 * Returns Singleton instance of this class.
	 * @return singleton instance of this class
	 * @throws IOException
	 * @throws LevelLoaderFormatException 
	 */
	public static LevelLoader getInstance() {
		if (instance == null) { instance = new LevelLoader(); }
		return instance;
	}

	protected static LevelLoader getNewInstanceForUnitTest(String folder, String preFix, String fileType) {
		return new LevelLoader(folder, preFix, fileType);
	}

	private LevelLoader() {
		initialize(defaultFolder); // default folder
	}

	private LevelLoader(String folder, String preFix, String fileType) {
		this.defaultFolder = folder;
		this.preFix = preFix;
		this.fileType = fileType;
		initialize(folder);
	}

	protected void initialize(String folder) throws LevelLoaderNoLevelFilesException {
		List<String> files = getLevelFiles(folder);
		//System.out.println("Number of Level files: "+files.size());
		if (files.isEmpty()) {
			throw new LevelLoaderNoLevelFilesException("Level load could not find any level files.");
		}
		for (String file : files) {
//			System.out.println(file);
			final List<String> lines = getLinesFromLevelFile(folder, file);
			//			System.out.println(file +" has "+ lines.size() + " lines.");
			// read lines into data structure
			processLinesFromLevel(file, lines);
		}
//		System.out.println("Levels Total: "+levels.size());
//		System.out.println("Levels: "+levels.keySet().toString());

	}

	protected List<String> getLevelFiles(String folder) throws LevelLoaderIOException {
		List<String> files = null;

		/*
		 * This is really SUPER ugly but I have not found another way yet to get all 
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
						.filter(f -> f.getName().lastIndexOf(filterString) >= 0 )
						.filter(f -> f.getName().endsWith(fileType))
						.map(f -> f.getName().substring(f.getName().lastIndexOf(preFix)))
						.collect(Collectors.toList());

				jar.close();

			} catch (IOException e) {
				throw new LevelLoaderIOException(e);
			}

		} else { // Run with IDE

			final URL folderURL = BreakOut.class.getResource(folder);
			if (folderURL == null) {
				throw new LevelLoaderIOException("While loading levels: Folder not found: "+folder);
			}

			String filterString = preFix;

			try {
//				System.out.println(folderURL);
//				System.out.println(Files.list(FileSystems.getDefault().getPath(folderURL.getPath())).count());
				files = Files.list(FileSystems.getDefault().getPath(folderURL.getPath()))
						.map(m -> m.toFile().getName())
						.filter(f -> f.lastIndexOf(filterString) >= 0 )
						.filter(f -> f.endsWith(fileType))
						.collect(Collectors.toList());
//				System.out.println(files.size());
			} catch (IOException e) {
				throw new LevelLoaderIOException(e);
			}
		}
		return files;
	}

	protected List<String> getLinesFromLevelFile(String folder, String file) {
		final InputStream fileStream = BreakOut.class.getResourceAsStream(folder+file);
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
				lines.add(line);
				line = br.readLine();
			}
		} catch (IOException e) {
			throw new LevelLoaderIOException(e);
		}
		return lines;
	}

	protected void processLinesFromLevel(String file, List<String> lines) throws LevelLoaderFormatException {

		Brick[][] tmpMatrix = new Brick[18][11];

		//		System.out.println("Lines: "+lines.size());

		String matchString = "^(--|GY|OR|CY|GR|RE|BL|PU|YE|SI|GO)(--|NO|LA|EN|CA|SL|BR|DI|PL)$";

		int validLineCounter = 0;
		for (int row=0; row<lines.size(); row++) {

			//			System.out.println(lines.get(row));

			if (lines.get(row).trim().isEmpty() || // remove empty lines
					lines.get(row).trim().startsWith("#")) { // remove comment lines
				continue;
			}
			validLineCounter++;
			String[] rowItems = lines.get(row).trim().split(" ");
			if (rowItems.length != 11) { // check if 11 columns
				throw new LevelLoaderFormatException(
						String.format("Bad row format in %s at line %d", file, row+1));
			}
			//			System.out.println(String.format("Row %d has %d bricks", row+1, rowItems.length));
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

				//				System.out.print(b+" ");
			}
			//			System.out.println();
		}
		//		System.out.println("Valid lines: "+validLineCounter);

		if (validLineCounter < 18) { // check if less than 18 rows
			throw new LevelLoaderFormatException(
					String.format("Bad format in %s. Expected 18 lines, found %d.", file, validLineCounter));
		}

		String level = file.substring(preFix.length(), file.lastIndexOf(fileType));
		levels.put(level, tmpMatrix);
	}

	/**
	 * @param string
	 * @return
	 */
	public static Brick itemToBrick(String string) {
		// ignore the empty string
		if (string.equals("----")) return null;

		String bricktype = string.substring(0,2);
		String powertype = string.substring(2,4);

		BrickType bt;
		BrickPowerType bpt;

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
		case "NO": bpt = BrickPowerType.NONE; break;
		case "LA": bpt = BrickPowerType.LASER; break;
		case "EN": bpt = BrickPowerType.ENLARGE; break;
		case "CA": bpt = BrickPowerType.CATCH; break;
		case "SL": bpt = BrickPowerType.SLOW; break;
		case "BR": bpt = BrickPowerType.BREAK; break;
		case "DI": bpt = BrickPowerType.DISRUPTIOM; break;
		case "PL": bpt = BrickPowerType.PLAYER; break;
		default: throw new LevelLoaderFormatException(String.format("Unkwon brick powertype: %s", powertype));
		}

		return new Brick(bt, bpt);
	}

	/**
	 * @param i (>0 <maxAvailableLevel
	 * @return the level matrix or null of no such level
	 */
	public Brick[][] getLevel(int i) {
		if (i < 1) return null;
		return levels.get(""+i);
	}

}
