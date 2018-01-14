////////////////////////////////////////////////////////////////
// ImageEncoderAlphaTest.java
//
// Copyright (C) 2006-2009 by ObjectPlanet, Inc.
// All rights reserved. 
////////////////////////////////////////////////////////////////

package com.objectplanet.image.test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.objectplanet.chart.BarChart;
import com.objectplanet.chart.Chart;
import com.objectplanet.image.PngEncoder;

import javax.imageio.ImageIO;


/**
 * This class tests the different image encoders we have found,
 * comparing them to ObjectPlanet's image encoders.
 *
 * @author Bjorn J. Kvande.
 * @author Philipp Kolibaba.
 */
public class TruecolorAlphaTest {
	// used to paint chart
	private static BufferedImage image = null;
	private static Graphics2D g2D = null;

	// the encoder do not need to be instantiated for each image
	private static PngEncoder objectplanet_png_encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR_ALPHA);

	/**
	 * The names of the encoders to use.
	 */
	private static String[] encoders = new String[] {
		"com.objectplanet.image.PngEncoder",
		"com.keypoint.PngEncoder",
		"com.bigfoot.bugar.image.PNGEncoder",
		"javax.imageio.ImageIO"
	};


	/**
	 * Encodes the specified image with the given encoder.
	 * @param image The image to encode.
	 * @param out The output stream to write the image to.
	 * @param encoder The index of the encoder to use.
	 * @return The time it took to encode the image.
	 */
	private static long encode(Image image, ByteArrayOutputStream out, int encoder) throws Exception {
		out.reset();
		long start = System.currentTimeMillis();
		switch (encoder) {
			// objectplanet png
			case 0:
				objectplanet_png_encoder.encode(image, out);
				break;

			// keypoint png encoder
			case 1:
				com.keypoint.PngEncoder keypoint_png = new com.keypoint.PngEncoder(image, true);
				keypoint_png.pngEncode(true);
				break;

			// com.bigfoot.bugar.image png
			case 2:
				com.bigfoot.bugar.image.PNGEncoder bugar_png = new com.bigfoot.bugar.image.PNGEncoder(image, out);
				bugar_png.encodeImage();
				break;

			// javax.imageio.ImageIO png
			case 3:
				ImageIO.write((BufferedImage)image, "PNG", out);
				break;
		}
		return System.currentTimeMillis()-start;
	}


	/**
	 * Sorts the chart.
	 * @param chart The chart to sort.
	 */
	private static void sort(Chart chart) {
		double[] values = chart.getSampleValues(0);
		String[] labels = chart.getSampleLabels();
		qsort(values, labels, 0, values.length-1);
		chart.setSampleValues(0, values);
		chart.setSampleLabels(labels);
		if (g2D != null) {
			chart.paint(g2D);
		}
	}


	/**
	 * Returns the current chart as an image.
	 * @param chart The chart to get the image from.
	 * @param size The size of the image.
	 * @return The chart as an image object.
	 */
	private static Image getImage(Chart chart, Dimension size) {
		if (chart != null) {
			chart.setSize(size.width, size.height);
			if (image == null) {
				image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
			}
			if (g2D == null) {
				g2D = (Graphics2D)image.getGraphics();
				chart.setExternalGraphics(g2D, image);
			}
			if (g2D != null) {
				chart.paint(g2D);
			}
		}
		return image;
	}


	/**
	 * Sorts the specified list using the specified attribute as key.
	 * @param list The list to sort.
	 * @param lo0 left boundary of array partition
	 * @param hi0 right boundary of array partition
	 */
	private static void qsort(double[] list, String[] labels, int lo0, int hi0) {
		// make sure we have valid input
		if (lo0 < 0 || hi0 < 0 || list == null || list.length <= 1) {
			return;
		}

		int low = lo0;
		int high = hi0;
		double mid;

		if (hi0 > lo0) {
			// arbitrarily establishing partition element as the
			// midpoint of the array.
			mid = list[(lo0 + hi0) / 2];

			// loop through the array until indices cross
			while (low <= high) {
				// find the first element that is greater than or equal to
				// the partition element starting from the left Index.
				// If the column is specified as -1, the row index is used,
				// and the row index is a number
				while (low < hi0 && list[low] < mid) {
					++low;
				}

				// find an element that is smaller than or equal to
				// the partition element starting from the right Index.
				while (high > lo0 && list[high] > mid) {
					--high;
				}

				// if the indexes have not crossed, swap
				if (low <= high) {
					double tmp = list[low];
					String tmp_label = labels[low];
					list[low] = list[high];
					labels[low] = labels[high];
					list[high] = tmp;
					labels[high] = tmp_label;
					++low;
					--high;
				}
			}

			// If the right index has not reached the left side of array
			// must now sort the left partition.
			if (lo0 < high) {
				qsort(list, labels, lo0, high);
			}

			// If the left index has not reached the right side of array
			// must now sort the right partition.
			if (low < hi0) {
				qsort(list, labels, low, hi0);
			}
		}
	}


	/**
	 * Calculates encoding of the chart.
	 * @param chart The chart to encode.
	 * @param size True size of the chart component.
	 */
	private static void calculateEncoding(Chart chart, Dimension size) {
		try {
			// create the output stream to encode the image to
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// encode the chart
			for (int encode_round = 0; encode_round < 2; encode_round++) {
				System.out.println("round: " + encode_round);
				for (int encoder = 0; encoder < encoders.length; encoder++) {
					// clean up to avoid garbage collector thread hitting in
					System.gc();

					// get an image of the chart with the current values
					Image image = getImage(chart, size);

					// mark the sample
					chart.setSelection(0, -1, false);
					chart.setSelection(0, encoder, true);
					System.out.println("encoder: " + encoders[encoder]);

					// encode the image
					int count = 20;
					long total_time = 0;
					try {
						for (int i = 0; i < count; i++) {
							total_time += encode(image, out, encoder);
						}
					} catch (AWTException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (NoClassDefFoundError e) {
						e.printStackTrace();
					}

					// set the encoding time
					chart.setSampleValue(0, encoder, total_time/count);
					chart.setRelativeRange(0, 1.1, 50);
					chart.setSelection(0, -1, false);
				}
			}
			chart.setSelection(0, -1, false);
			System.out.println("done");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Runs the test.
	 */
	public void runTest() {
		runTest(false);
	}


	/**
	 * Runs the test.
	 * @param antialiased If thue the chart is antialiased
	 */
	public void runTest(boolean antialiased) {
		// create the bar chart to test
		BarChart chart = new BarChart();
		chart.setTitle("Encoding Truecolor Image with Alpha");
		chart.setTitleOn(true);
		Dimension size = ImageEncoderTest.CHART_SIZE;
		chart.setSampleCount(encoders.length);
		chart.setSampleLabels(encoders);
		chart.setBarAlignment(BarChart.HORIZONTAL);
		chart.setBarLabelsOn(true);
		chart.setLabel("rangeAxisLabel", "milliseconds");
		chart.setValueLabelsOn(true);
		chart.setMultiColorOn(true);
		for (int i = 0; i < chart.getSampleCount(); i++) {
			chart.setSampleColor(i, new Color(102,153,150,128));
		}
		chart.setSampleColor(0, new Color(173,0,0,128));
		chart.setValueLinesOn(true);
		chart.setBarWidth(0.5);
		chart.setChartBackground(new Color(231, 221, 231, 64));
		chart.setBackground(new Color(255, 255, 255, 0));
		chart.setValueLinesColor(Color.white);
		chart.setPreferredSize(size.width, size.height);
		chart.setServletModeOn(true);

		// comment this out if you want the chart to be displayed anti-aliased
		chart.setAntialiasingOn(antialiased);

		// display the chart
		try {
			Frame f = new Frame();
			f.add("Center", chart);
			f.pack();
			f.setVisible(true);
			f.setBounds(ImageEncoderTest.FRAME_SIZE.width, 0, ImageEncoderTest.FRAME_SIZE.width, ImageEncoderTest.FRAME_SIZE.height);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// encode the chart
		calculateEncoding(chart, size);

		// write the sorted chart to the disk
		try {
			sort(chart);
			Thread.sleep(1000);
			Image image = getImage(chart, size);
			String path = "images/truecolor_alpha" + (antialiased ? "_antialiased" : "") + ".png";
			FileOutputStream fos = new FileOutputStream(path);
			objectplanet_png_encoder.encode(image, fos);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Tests the speed of the different image encoders.
	 */
	public static void main(String[] argv) {
		TruecolorAlphaTest test = new TruecolorAlphaTest();
		test.runTest();
	}
}
