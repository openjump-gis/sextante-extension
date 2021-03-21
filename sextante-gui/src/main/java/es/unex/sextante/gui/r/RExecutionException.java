

package es.unex.sextante.gui.r;

import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;


public class RExecutionException
         extends
            GeoAlgorithmExecutionException {

   public RExecutionException(final String s) {
      super(s);
   }


   public RExecutionException() {
      super("Error executing R");
   }

}
