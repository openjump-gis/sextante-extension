package es.unex.sextante.vegetationIndices.pviQi;

import es.unex.sextante.vegetationIndices.base.DistanceBasedAlgorithm;

public class PVIQiAlgorithm
         extends
            DistanceBasedAlgorithm {


   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("PVI(Qi et al)");

   }


   @Override
   protected double getIndex(final double dRed,
                             final double dNIR) {

      return ((dNIR * m_dIntercept) - (dRed * m_dSlope));

   }

}
