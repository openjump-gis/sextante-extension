package es.unex.sextante.vegetationIndices.ndvi;

import es.unex.sextante.vegetationIndices.base.SlopeBasedAlgorithm;

public class NDVIAlgorithm
         extends
            SlopeBasedAlgorithm {


   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("NDVI");

   }


   @Override
   protected double getIndex(final double dRed,
                             final double dNIR) {

      return getNDVI(dRed, dNIR);

   }

}
