package es.unex.sextante.vegetationIndices.nrvi;

import es.unex.sextante.vegetationIndices.base.SlopeBasedAlgorithm;

public class NRVIAlgorithm
         extends
            SlopeBasedAlgorithm {


   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("NRVI");

   }


   @Override
   protected double getIndex(final double dRed,
                             final double dNIR) {

      return (dRed / dNIR - 1) / (dRed + dNIR + 1);

   }

}
