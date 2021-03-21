package es.unex.sextante.rasterize.linearDecrease;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.closestpts.Point3D;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterize.interpolationBase.BaseInterpolationAlgorithm;

public class LinearDecreaseAlgorithm
         extends
            BaseInterpolationAlgorithm {

   public static final String CROSSVALIDATION = "CROSSVALIDATION";
   public static final String POWER           = "POWER";

   private double             m_dPower;


   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();
      setGroup(Sextante.getText("Rasterization_and_interpolation"));
      setName(Sextante.getText("Linear_decrease"));

      try {
         m_Parameters.addNumericalValue(POWER, Sextante.getText("Exponent"), AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE,
                  2, 0, Double.MAX_VALUE);
         addOutputTable(CROSSVALIDATION, Sextante.getText("Cross_validation"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   protected void setValues() throws GeoAlgorithmExecutionException {

      super.setValues();

      m_dPower = m_Parameters.getParameterValueAsDouble(POWER);

   }


   @Override
   protected double interpolate(final double x,
                                final double y) {

      int i;
      Point3D pt;
      double d;
      double dDistSum = 0, dZSum = 0;
      int iPts = 0;
      if (m_NearestPoints.length != 0) {
         final int iSize = m_NearestPoints.length;
         for (i = 0; i < iSize; i++) {
            if (m_NearestPoints[i] != null) {
               iPts++;
               pt = m_NearestPoints[i].getPt();
               d = m_NearestPoints[i].getDist();

               if (d <= 0.0) {
                  return pt.getZ();
               }

               d = 1 - Math.pow(d / m_dDistance, m_dPower);

               dZSum += d * pt.getZ();
               dDistSum += d;
            }
         }
         if (iPts > 0) {
            return dZSum / dDistSum;
         }
         else {
            return NO_DATA;
         }
      }
      else {
         return NO_DATA;
      }

   }

}
