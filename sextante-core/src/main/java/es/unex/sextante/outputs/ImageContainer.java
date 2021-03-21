

package es.unex.sextante.outputs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class ImageContainer
         extends
            Component {

   BufferedImage img;


   @Override
   public void paint(final Graphics g) {
      g.drawImage(img, 0, 0, null);
   }


   public ImageContainer(final String sFilepath) {
      try {
         img = ImageIO.read(new File(sFilepath));
      }
      catch (final IOException e) {
      }

   }


   @Override
   public Dimension getPreferredSize() {
      if (img == null) {
         return new Dimension(100, 100);
      }
      else {
         return new Dimension(img.getWidth(null), img.getHeight(null));
      }
   }

}
