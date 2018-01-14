////////////////////////////////////////////////////////////////
// ImageEncoderTest.java
//
// Copyright (C) 2006-2009 by ObjectPlanet, Inc.
// All rights reserved. 
////////////////////////////////////////////////////////////////

package com.objectplanet.image.test;

import java.awt.Dimension;
import java.io.File;


/**
 * This class tests the different image encoders we have found,
 * comparing them to ObjectPlanet's image encoders.
 *
 * @author Bjorn J. Kvande.
 * @author Philipp Kolibaba.
 */
public class ImageEncoderTest {
	
	
	/**
	 * The size of the chart.
	 */
	public static final Dimension CHART_SIZE = new Dimension(466, 200);
	
	
	/**
	 * The padding for the the frame, x, and y.
	 */
	public static final Dimension FRAME_SIZE = new Dimension(CHART_SIZE.width + 10, CHART_SIZE.height + 34);
	

	/**
	 * Tests the speed of the different image encoders.
	 */
	public static void main(String[] argv) {
		boolean antialiased = false;
		if (argv.length >= 1) {
			antialiased = argv[0].equalsIgnoreCase("antialiased");
		}

		File f = new File("images");
		f.mkdir();

		// test truecolor
		TruecolorTest true_test = new TruecolorTest();
		true_test.runTest(antialiased);

		// test truecolor with alpha
		TruecolorAlphaTest truealpha_test = new TruecolorAlphaTest();
		truealpha_test.runTest(antialiased);

		// test grayscale
		GrayscaleTest gray_test = new GrayscaleTest();
		gray_test.runTest(antialiased);

		// test grayscale with alpha
		GrayscaleAlphaTest grayalpha_test = new GrayscaleAlphaTest();
		grayalpha_test.runTest(antialiased);

		// test indexed
		IndexedTest indexed_test = new IndexedTest();
		indexed_test.runTest(antialiased);

		// test indexed with alpha
		IndexedAlphaTest indexedalpha_test = new IndexedAlphaTest();
		indexedalpha_test.runTest();
	}
}
