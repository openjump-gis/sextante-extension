package es.unex.sextante.gui.exceptions;

import es.unex.sextante.core.Sextante;

public class WrongGrassFolderException
         extends
            Exception {

   public WrongGrassFolderException() {

      super(Sextante.getText("grass_error_binaries_folder"));

   }

}
