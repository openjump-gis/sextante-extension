package es.unex.sextante.gui.grass;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;

public class GrassExecutionException
         extends
            GeoAlgorithmExecutionException {

   public GrassExecutionException() {

      super(Sextante.getText("Error_executing_GRASS_process"));

   }

}
