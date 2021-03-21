

package es.unex.sextante.gridTools.changeNoDataValue;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public class ChangeNoDataValueAlgorithm
         extends
            GeoAlgorithm {

   public static final String  RESULT        = "RESULT";
   public static final String  INPUT         = "INPUT";
   private static final String NO_DATA_VALUE = "NO_DATA_VALUE";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("ChangeNoDataValue"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Input_layer"), true);
         m_Parameters.addNumericalValue(NO_DATA_VALUE, Sextante.getText("NoDataValue"), -99999.,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IRasterLayer layer = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      final double dNoData = m_Parameters.getParameterValueAsDouble(NO_DATA_VALUE);

      layer.setFullExtent();
      final IRasterLayer result = getNewRasterLayer(RESULT, layer.getName(), layer.getDataType(), layer.getLayerGridExtent());
      result.assign(layer);
      result.setNoDataValue(dNoData);

      return !m_Task.isCanceled();

   }


}
