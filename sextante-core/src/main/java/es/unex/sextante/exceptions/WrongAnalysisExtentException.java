package es.unex.sextante.exceptions;

public class WrongAnalysisExtentException
         extends
            GeoAlgorithmExecutionException {

   public WrongAnalysisExtentException(final String s) {

      super(s);

   }


   public WrongAnalysisExtentException() {

      super("Wrong raster extension exception");

   }

}
