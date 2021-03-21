package es.unex.sextante.gui.toolbox;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

public class TransparentScrollPane
         extends
            JScrollPane {

   ImageIcon image = null;


   public TransparentScrollPane(final Component view,
                                final int vsbPolicy,
                                final int hsbPolicy) {

      super(view, vsbPolicy, hsbPolicy);
      if (view instanceof JComponent) {
         ((JComponent) view).setOpaque(false);
      }

      this.setOpaque(false);
      this.getViewport().setOpaque(false);

   }


   public TransparentScrollPane(final Component view) {

      this(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

   }


   public TransparentScrollPane(final int vsbPolicy,
                                final int hsbPolicy) {

      this(null, vsbPolicy, hsbPolicy);

   }


   public TransparentScrollPane() {

      this(null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
   }


   @Override
   public void paintComponent(final Graphics g) {

      g.setColor(Color.white);
      g.fillRect(0, 0, this.getWidth(), this.getHeight());

      if (image != null) {
         final int h = image.getIconHeight();
         final int w = image.getIconWidth();
         g.drawImage(image.getImage(), getWidth() - w, getHeight() - h, null, null);
         getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
      }
      super.paintComponent(g);

   }


   public void setBackgroundImage(final ImageIcon image) {

      this.image = image;

   }

}
