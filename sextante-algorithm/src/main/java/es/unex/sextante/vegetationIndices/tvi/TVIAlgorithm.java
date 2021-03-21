package es.unex.sextante.vegetationIndices.tvi;

import es.unex.sextante.vegetationIndices.base.SlopeBasedAlgorithm;

public class TVIAlgorithm
         extends
            SlopeBasedAlgorithm {


   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("TVI");

   }


   @Override
   protected double getIndex(final double dRed,
                             final double dNIR) {

      final double dNDVI = getNDVI(dRed, dNIR);

      if (dNDVI > -0.5) {
         return Math.sqrt(dNDVI + 0.5);
      }
      else {
         return m_dNoData;
      }

   }

}
