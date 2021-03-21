package es.unex.sextante.gui.history;

import javax.swing.JTextPane;

public class NonWordWrapPane
         extends
            JTextPane {

   public NonWordWrapPane() {

      super();

   }


   @Override
   public boolean getScrollableTracksViewportWidth() {

      return false;

   }

}
