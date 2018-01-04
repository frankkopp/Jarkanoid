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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import fko.breakout.BreakOut;

/**
 * LevelLoader
 * 04.01.2018
 * @author Frank Kopp
 */
public class LevelLoader {

	private static LevelLoader instance; // Singleton

	private final String folder = "/levels/";
	private final String preFix = "Level-";
	private final String fileType = ".txt";

	public static LevelLoader getInstance() throws IOException {
		if (instance == null) { instance = new LevelLoader(); }
		return instance;
	}

	private LevelLoader() throws IOException {
		
		List<String> files = null;

		/*
		 * This is really SUPER ugly but I have not found another way yet to get all 
		 * files from a directory when running from IDE AND running from JAR file.
		 */
		
		final File jarFile = 
				new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

		if(jarFile.isFile()) {  // Run with JAR file
			
		    final JarFile jar = new JarFile(jarFile);
		
		    String filterString = folder.substring(1) + preFix;
		    
		    files = jar.stream()
		    .filter(f -> f.getName().lastIndexOf(filterString) >= 0 )
		    .filter(f -> f.getName().endsWith(fileType))
		    .map(f -> f.getName())
		    .collect(Collectors.toList());
		    
		    jar.close();
		    
		} else { // Run with IDE
			
			final InputStream folderAsStream = BreakOut.class.getResourceAsStream("/levels/");
			if (folderAsStream == null) {
				BreakOut.fatalError("While loading levels: Folder not found");
			}
			
			String filterString = preFix;
			
			files = IOUtils.readLines(folderAsStream, Charset.defaultCharset())
					.stream()
					.filter(f -> f.lastIndexOf(filterString) >= 0 )
				    .filter(f -> f.endsWith(fileType))
				    .collect(Collectors.toList());
		}

		System.out.println(files.size());
		System.out.println("Listing all Level files");
		files.forEach(f -> System.out.println(f));
		
//		final InputStream fileStream = BreakOut.class.getResourceAsStream(folder+"Test.txt");
//		if (fileStream == null) {
//			BreakOut.fatalError("While loading Test: Test not found");
//		}
//		List<String> lines = IOUtils.readLines(fileStream, Charset.defaultCharset());
//		System.out.println("Listing all lines");
//		lines.forEach(f -> System.out.println(f));

	}

	/**
	 * @param i
	 * @return
	 */
	public static Brick[][] getLevel(int i) {
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LevelLoader []");
		return builder.toString();
	}



}
