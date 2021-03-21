package es.unex.sextante.vegetationIndices.pviPerry;

import es.unex.sextante.vegetationIndices.base.DistanceBasedAlgorithm;

public class PVIPerryAlgorithm
         extends
            DistanceBasedAlgorithm {


   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("PVI(Perry and Lautenschlager)");

   }


   @Override
   protected double getIndex(final double dRed,
                             final double dNIR) {

      return ((m_dSlope * dNIR - dRed + m_dIntercept) / Math.sqrt(m_dSlope * m_dSlope + 1));

   }

}
