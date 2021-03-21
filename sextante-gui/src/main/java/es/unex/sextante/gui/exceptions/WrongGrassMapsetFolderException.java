package es.unex.sextante.gui.exceptions;

import es.unex.sextante.core.Sextante;

public class WrongGrassMapsetFolderException
         extends
            Exception {

   public WrongGrassMapsetFolderException() {

      super(Sextante.getText("grass_error_mapset_folder"));

   }

}
