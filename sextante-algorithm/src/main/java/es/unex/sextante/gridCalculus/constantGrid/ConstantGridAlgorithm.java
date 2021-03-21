package es.unex.sextante.gridCalculus.constantGrid;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class ConstantGridAlgorithm
         extends
            GeoAlgorithm {

   public static final String VALUE     = "VALUE";
   public static final String CONSTGRID = "CONSTGRID";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Constant_grid"));
      setGroup(Sextante.getText("Raster_creation_tools"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addNumericalValue(VALUE, Sextante.getText("Value"), 1, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputRasterLayer(CONSTGRID, Sextante.getText("Constant_grid"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      double dValue;

      final IRasterLayer result = getNewRasterLayer(CONSTGRID, Sextante.getText("Constant_grid"),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      dValue = m_Parameters.getParameterValueAsDouble(VALUE);

      result.assign(dValue);

      return !m_Task.isCanceled();

   }


}
