package es.unex.sextante.vegetationIndices.pviWalther;

import es.unex.sextante.vegetationIndices.base.DistanceBasedAlgorithm;

public class PVIWaltherAlgorithm
         extends
            DistanceBasedAlgorithm {


   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("PVI(Walther and Shabaani)");

   }


   @Override
   protected double getIndex(final double dRed,
                             final double dNIR) {

      return (((dNIR - m_dIntercept) * (dRed + m_dSlope)) / Math.sqrt(1 + m_dIntercept));

   }

}
