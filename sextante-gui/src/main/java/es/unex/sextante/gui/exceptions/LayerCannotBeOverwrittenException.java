package es.unex.sextante.gui.exceptions;

import es.unex.sextante.core.Sextante;

public class LayerCannotBeOverwrittenException
         extends
            Exception {

   public LayerCannotBeOverwrittenException() {

      super(Sextante.getText("Layer_cannot_be_overwritten"));

   }

}
