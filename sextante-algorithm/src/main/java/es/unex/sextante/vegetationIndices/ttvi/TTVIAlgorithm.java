package es.unex.sextante.vegetationIndices.ttvi;

import es.unex.sextante.vegetationIndices.base.SlopeBasedAlgorithm;

public class TTVIAlgorithm
         extends
            SlopeBasedAlgorithm {


   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("TTVI");

   }


   @Override
   protected double getIndex(final double dRed,
                             final double dNIR) {

      final double dNDVI = getNDVI(dRed, dNIR);

      return Math.sqrt(Math.abs(dNDVI + 0.5));

   }

}
