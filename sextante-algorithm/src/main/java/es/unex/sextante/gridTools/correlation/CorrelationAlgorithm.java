package es.unex.sextante.gridTools.correlation;

import java.text.DecimalFormat;

import es.unex.sextante.additionalResults.CorrelationGraphCreator;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.math.regression.Regression;

public class CorrelationAlgorithm
         extends
            GeoAlgorithm {

   public static final String REGRESSION_DATA = "REGRESSION_DATA";
   public static final String REGRESSION      = "REGRESSION";
   public static final String METHOD          = "METHOD";
   public static final String GRID2           = "GRID2";
   public static final String GRID            = "GRID";


   @Override
   public void defineCharacteristics() {

      final String sMethod[] = { Sextante.getText("Best_fit"), "y = a + b * x", "y = a + b / x", "y = a / (b - x)",
               "y = a * x^b", "y = a e^(b * x)", "y = a + b * ln(x)" };

      setName(Sextante.getText("Correlation_between_layers"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(GRID, Sextante.getText("Layer_1"), true);
         m_Parameters.addInputRasterLayer(GRID2, Sextante.getText("Layer_2"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Function_type"), sMethod);
         addOutputChart(REGRESSION, Sextante.getText("Regression"));
         addOutputText(REGRESSION_DATA, Sextante.getText("Regression_values"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      double dCellValue, dCellValue2;
      IRasterLayer window, window2;
      final Regression regression = new Regression();

      window = m_Parameters.getParameterValueAsRasterLayer(GRID);
      window2 = m_Parameters.getParameterValueAsRasterLayer(GRID2);
      final int iType = m_Parameters.getParameterValueAsInt(METHOD);

      window.setFullExtent();
      window2.setWindowExtent(window.getWindowGridExtent());
      if ((window2.getDataType() == IRasterLayer.RASTER_DATA_TYPE_INT)
          || (window2.getDataType() == IRasterLayer.RASTER_DATA_TYPE_DOUBLE)) {
         window2.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);
      }

      iNX = window.getNX();
      iNY = window.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dCellValue = window.getCellValueAsDouble(x, y);
            dCellValue2 = window2.getCellValueAsDouble(x, y);
            if (!window.isNoDataValue(dCellValue) && !window2.isNoDataValue(dCellValue2)) {
               regression.addValue(dCellValue, dCellValue2);
            }
         }

      }

      regression.calculate(iType);
      final CorrelationGraphCreator panel = new CorrelationGraphCreator(regression);

      addOutputChart(REGRESSION, Sextante.getText("Regression"), panel.getChartPanel());

      final DecimalFormat df = new DecimalFormat("####.###");
      final HTMLDoc doc = new HTMLDoc();
      doc.open(Sextante.getText("Regression"));
      doc.addHeader(Sextante.getText("Regression"), 2);
      doc.startUnorderedList();
      doc.addListElement(regression.getExpression());
      doc.addListElement("R2 = " + df.format(regression.getR2()));
      doc.closeUnorderedList();
      doc.close();

      addOutputText(REGRESSION_DATA, Sextante.getText("Regression_values"), doc.getHTMLCode());

      return !m_Task.isCanceled();


   }


}
