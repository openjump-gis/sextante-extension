package es.unex.sextante.gridCalculus.gridFromFunction;

import org.nfunk.jep.JEP;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class GridFromFunctionAlgorithm
         extends
            GeoAlgorithm {

   public static final String MINX    = "MINX";
   public static final String MINY    = "MINY";
   public static final String MAXX    = "MAXX";
   public static final String MAXY    = "MAXY";
   public static final String FORMULA = "FORMULA";
   public static final String RESULT  = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      double dx, dy;
      double dMinX, dMinY, dMaxX, dMaxY;
      double dRangeX, dRangeY;
      double dValue;

      String sFormula;
      final JEP jep = new JEP();
      IRasterLayer result;

      jep.addStandardConstants();
      jep.addStandardFunctions();
      jep.addVariable("x", 0);
      jep.addVariable("y", 0);

      dMinX = m_Parameters.getParameterValueAsInt(MINX);
      dMinY = m_Parameters.getParameterValueAsInt(MINY);
      dMaxX = m_Parameters.getParameterValueAsInt(MAXX);
      dMaxY = m_Parameters.getParameterValueAsInt(MAXY);
      sFormula = m_Parameters.getParameterValueAsString(FORMULA).toLowerCase();

      dRangeX = dMaxX - dMinX;
      dRangeY = dMaxY - dMinY;

      result = getNewRasterLayer(RESULT, Sextante.getText("Layer_from_function"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      iNX = result.getWindowGridExtent().getNX();
      iNY = result.getWindowGridExtent().getNY();

      jep.parseExpression(sFormula);
      if (!jep.hasError()) {
         for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
            dy = dMinY + dRangeY * ((double) y / (double) iNY);
            jep.addVariable("y", dy);
            for (x = 0; x < iNX; x++) {
               dx = dMinX + dRangeX * ((double) x / (double) iNX);
               jep.addVariable("x", dx);
               dValue = jep.getValue();
               result.setCellValue(x, y, dValue);
            }
         }
      }
      else {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Wrong_formula"));
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Grid_from_function"));
      setGroup(Sextante.getText("Raster_creation_tools"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addNumericalValue(MINX, "X min", -1, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(MAXX, "X Max", 1, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(MINY, "Y Min", -1, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(MAXY, "Y Max", 1, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addString(FORMULA, Sextante.getText("Formula"));
         addOutputRasterLayer(RESULT, Sextante.getText("Grid_from_function"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


}
