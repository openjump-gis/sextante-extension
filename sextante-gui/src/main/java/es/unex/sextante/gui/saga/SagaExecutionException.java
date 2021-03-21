package es.unex.sextante.gui.saga;

import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;

public class SagaExecutionException
         extends
            GeoAlgorithmExecutionException {

   public SagaExecutionException(final String sMessage) {

      super(sMessage);

   }


   public SagaExecutionException() {

      super("Saga Execution Exception");

   }

}
