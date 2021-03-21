package es.unex.sextante.gridTools.gridBasicStats;

import java.text.DecimalFormat;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.math.simpleStats.SimpleStats;

public class GridBasicStatsAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT             = "INPUT";
   public static final String STATS             = "STATS";
   public static final String MEAN              = "MEAN";
   public static final String MEAN_SQUARED      = "MEAN_SQUARED";
   public static final String MIN               = "MIN";
   public static final String MAX               = "MAX";
   public static final String VARIANCE          = "VARIANCE";
   public static final String SUM               = "SUM";
   public static final String COEF_OF_VARIATION = "COEF_OF_VARIATION";
   public static final String VALID_CELLS       = "VALID_CELLS";
   public static final String NO_DATA_CELLS     = "NO_DATA_CELLS";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Basic_statistics"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         addOutputText(STATS, Sextante.getText("Statistics"));
         addOutputNumericalValue(MEAN, Sextante.getText("Mean_value"));
         addOutputNumericalValue(MEAN_SQUARED, Sextante.getText("Mean_squared_value"));
         addOutputNumericalValue(MIN, Sextante.getText("Minimum_value"));
         addOutputNumericalValue(MAX, Sextante.getText("Maximum_value"));
         addOutputNumericalValue(VARIANCE, Sextante.getText("Variance"));
         addOutputNumericalValue(SUM, Sextante.getText("Total_sum"));
         addOutputNumericalValue(COEF_OF_VARIATION, Sextante.getText("Coefficient_of_variation"));
         addOutputNumericalValue(VALID_CELLS, Sextante.getText("Valid_data_cells"));
         addOutputNumericalValue(NO_DATA_CELLS, Sextante.getText("No_data_cells"));

      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      int iNoDataCells = 0;
      double dValue;
      IRasterLayer input;
      final SimpleStats stats = new SimpleStats();

      input = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      input.setFullExtent();
      iNX = input.getNX();
      iNY = input.getNY();
      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dValue = input.getCellValueAsDouble(x, y);
            if (!input.isNoDataValue(dValue)) {
               stats.addValue(dValue);
            }
            else {
               iNoDataCells++;
            }
         }

      }
      if (m_Task.isCanceled()) {
         return false;
      }

      final DecimalFormat df = new DecimalFormat("##.###");
      final HTMLDoc doc = new HTMLDoc();
      doc.open(Sextante.getText("Statistics"));
      doc.addHeader(Sextante.getText("Basic_statistics"), 2);
      doc.startUnorderedList();
      doc.addListElement(Sextante.getText("Mean_value") + ": " + df.format(stats.getMean()));
      doc.addListElement(Sextante.getText("Mean_squared_value") + ": " + df.format(stats.getRMS()));
      doc.addListElement(Sextante.getText("Minimum_value") + ": " + df.format(stats.getMin()));
      doc.addListElement(Sextante.getText("Maximum_value") + ": " + df.format(stats.getMax()));
      doc.addListElement(Sextante.getText("Variance") + ": " + df.format(stats.getVariance()));
      doc.addListElement(Sextante.getText("Total_sum") + ": " + df.format(stats.getSum()));
      doc.addListElement(Sextante.getText("Coefficient_of_variation") + ": " + df.format(stats.getCoeffOfVar()));
      doc.addListElement(Sextante.getText("Valid_data_cells") + ": " + Integer.toString(stats.getCount()));
      doc.addListElement(Sextante.getText("No_data_cells") + ": " + Integer.toString((iNX * iNY) - stats.getCount()));
      doc.closeUnorderedList();
      doc.close();

      addOutputText(STATS, Sextante.getText("Statistics") + "[" + input.getName() + "]", doc.getHTMLCode());

      addOutputNumericalValue(MEAN, stats.getMean());
      addOutputNumericalValue(MEAN_SQUARED, stats.getRMS());
      addOutputNumericalValue(MIN, stats.getMin());
      addOutputNumericalValue(MAX, stats.getMax());
      addOutputNumericalValue(VARIANCE, stats.getVariance());
      addOutputNumericalValue(SUM, stats.getSum());
      addOutputNumericalValue(COEF_OF_VARIATION, stats.getCoeffOfVar());
      addOutputNumericalValue(VALID_CELLS, stats.getCount());
      addOutputNumericalValue(NO_DATA_CELLS, (iNX * iNY) - stats.getCount());

      return true;

   }

}
