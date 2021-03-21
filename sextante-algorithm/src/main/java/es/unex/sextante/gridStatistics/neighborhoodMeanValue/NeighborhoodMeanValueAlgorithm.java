package es.unex.sextante.gridStatistics.neighborhoodMeanValue;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gridStatistics.base.NeighborhoodStatsBaseAlgorithm;

public class NeighborhoodMeanValueAlgorithm
         extends
            NeighborhoodStatsBaseAlgorithm {

   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Mean__neighbourhood"));
      setGroup(Sextante.getText("Focal_statistics"));
      super.defineCharacteristics();

   }


   @Override
   protected double processValues() {

      int i;
      double dValue;
      int iValidCells = 0;

      double dMean = 0;

      for (i = 0; i < m_dValues.length; i++) {
         dValue = m_dValues[i];
         if (dValue != NO_DATA) {
            dMean += dValue;
            iValidCells++;
         }
      }

      if (iValidCells > 0) {
         return (dMean / iValidCells);
      }
      else {
         return NO_DATA;
      }

   }

}
