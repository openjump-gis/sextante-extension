package es.unex.sextante.vegetationIndices.pviRichardson;

import es.unex.sextante.vegetationIndices.base.DistanceBasedAlgorithm;

public class PVIRichardsonAlgorithm
         extends
            DistanceBasedAlgorithm {


   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("PVI(Richardson and Wiegand)");

   }


   @Override
   protected double getIndex(final double dRed,
                             final double dNIR) {

      final double dA1 = m_dSlope;
      final double dA0 = m_dIntercept;
      final double dB1 = 1 / m_dSlope;
      final double dB0 = dRed - dB1 * dNIR;

      final double dRgg5 = (dB1 * dA0 - dB0 * dA1) / (dB1 - dA1);
      final double dRgg7 = (dA0 - dB0) / (dB1 - dA1);

      return (Math.sqrt(Math.pow(dRgg5 - dRed, 2.) + Math.pow(dRgg7 - dNIR, 2.)));

   }

}
