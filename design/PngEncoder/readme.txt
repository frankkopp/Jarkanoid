                          com.objectplanet.image.PngEncoder
		=====================================================================

Changes:
  Version 2.0.1 to 2.0.2
- Fixed a bug where the images were not encoded properly in COLOR_TRUECOLOR, COLOR_TRUECOLOR_ALPHA and 
  COLOR_GRAYSCALE_ALPHA when the source image were read using ImageIO.read().
- Now when encoding to indexed color image the proper algorithm is choosed depending on if the
  original image contains more or less then 256 colors:
  setIndexedColorMode(PngEncoder.INDEXED_COLORS_ORIGINAL) - use pixels from original image,
  should be used when there is less then 256 colors in the original image.
  setIndexedColorMode(PngEncoder.INDEXED_COLORS_CONVERT) - convert pixels to make sure all pixels are written to the target image.
  setIndexedColorMode(PngEncoder.INDEXED_COLORS_AUTO) - let the PngEncoder find the best mode automatically. 
  May take longer time to encode.

  Version 2.0 to 2.0.1
- fixed a bug where some colors where written as white when converting from TRUECOLOR to INDEXED

  Version 1.1 to 2.0
- Added possibility to encode images with alpha transparency.
  PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_TRUECOLOR_ALPHA);
  PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_GRAYSCALE_ALPHA);
  PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_INDEXED_ALPHA);

  Version 1.0.1 to 1.1
- Added possibility for indexed encoding. PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_INDEXED); 
- Added possibility for grayscale encoding. PngEncoder encoder = new PngEncoder(PngEncoder.COLOR_GRAYSCALE); 

  Version 1.0 to 1.0.1
- Removed the obfuscation from the public fields COLOR_INDEXED
  COLOR_TRUECOLOR, DEFAULT_COMPRESSION, BEST_SPEED, and BEST_COMPRESSION
 
		
To use the PngEncoder:

1) Add the com.objectplanet.image.PngEncoder.jar file to 
   your development environment.
	
2) import com.objectplanet.image.PngEncoder;

3) Create an encoder and use it in your application.

import com.objectplanet.image.PngEncoder;
import com.objectplanet.chart.*;

public class PngEncoderTest {

   //...

   public void aMethod() {
      //...

      // create an encoder, create it once and use it many times
      PngEncoder encoder = new PngEncoder();

      //...

      // get your image (image from http://objectplanet.com/EasyCharts)
      Image image = chart.getImage(500,200);

      // write the encoded image to disk
      try {
         FileOutputStream fout = new FileOutputStream("chart.png");
         encoder.encode(image, fout);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
	
   //...
}


To run the encoder test:

java -jar test.jar

Enjoy!
Bjorn J. Kvande and Philipp Kolibaba
ObjectPlanet, Inc.

http://objectplanet.com/PngEncoder
