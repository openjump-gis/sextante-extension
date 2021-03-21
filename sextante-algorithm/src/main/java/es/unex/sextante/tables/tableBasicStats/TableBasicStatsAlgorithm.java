package es.unex.sextante.tables.tableBasicStats;

import java.text.DecimalFormat;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.IRecordsetIterator;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.math.simpleStats.SimpleStats;

public class TableBasicStatsAlgorithm
         extends
            GeoAlgorithm {

   public static final String TABLE             = "TABLE";
   public static final String FIELD             = "FIELD";
   public static final String STATS             = "STATS";
   public static final String MEAN              = "MEAN";
   public static final String MEAN_SQUARED      = "MEAN_SQUARED";
   public static final String MIN               = "MIN";
   public static final String MAX               = "MAX";
   public static final String VARIANCE          = "VARIANCE";
   public static final String SUM               = "SUM";
   public static final String COEF_OF_VARIATION = "COEF_OF_VARIATION";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iField;
      long iCount;
      double dValue;
      ITable table;
      final SimpleStats stats = new SimpleStats();

      table = m_Parameters.getParameterValueAsTable(TABLE);
      iField = m_Parameters.getParameterValueAsInt(FIELD);

      i = 0;
      iCount = table.getRecordCount();
      final IRecordsetIterator iter = table.iterator();
      while (iter.hasNext() && setProgress(i, (int) iCount)) {
         final IRecord record = iter.next();
         try {
            dValue = Double.parseDouble(record.getValue(iField).toString());
            stats.addValue(dValue);
         }
         catch (final NumberFormatException e) {}
         i++;
      }
      iter.close();

      if (m_Task.isCanceled()) {
         return false;
      }
      else {
         final DecimalFormat df = new DecimalFormat("##.###");
         final HTMLDoc doc = new HTMLDoc();
         doc.open(Sextante.getText("Statistics"));
         doc.addHeader(Sextante.getText("Basic_statistics"), 2);
         doc.startUnorderedList();
         doc.addListElement(Sextante.getText("Mean_value") + ": " + df.format(stats.getMean()));
         doc.addListElement(Sextante.getText("Mean_squared_value") + ": " + df.format(stats.getRMS()));
         doc.addListElement(Sextante.getText("Min_value") + ": " + df.format(stats.getMin()));
         doc.addListElement(Sextante.getText("Max_value") + ": " + df.format(stats.getMax()));
         doc.addListElement(Sextante.getText("Variance") + ": " + df.format(stats.getVariance()));
         doc.addListElement(Sextante.getText("Total_sum") + ": " + df.format(stats.getSum()));
         doc.addListElement(Sextante.getText("Coefficient_of_variation") + ": " + df.format(stats.getCoeffOfVar()));
         doc.closeUnorderedList();
         doc.close();

         addOutputText("STATS", Sextante.getText("Statistics") + "[" + table.getName() + "]", doc.getHTMLCode());

         addOutputNumericalValue(MEAN, Sextante.getText("Mean_value"));
         addOutputNumericalValue(MEAN_SQUARED, Sextante.getText("Mean_squared_value"));
         addOutputNumericalValue(MIN, Sextante.getText("Minimum_value"));
         addOutputNumericalValue(MAX, Sextante.getText("Maximum_value"));
         addOutputNumericalValue(VARIANCE, Sextante.getText("Variance"));
         addOutputNumericalValue(SUM, Sextante.getText("Total_sum"));
         addOutputNumericalValue(COEF_OF_VARIATION, Sextante.getText("Coefficient_of_variation"));

         return true;
      }

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Basic_statistics"));
      setGroup(Sextante.getText("Table_tools"));

      try {
         m_Parameters.addInputTable(TABLE, Sextante.getText("Table"), true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), TABLE);
         addOutputText(STATS, Sextante.getText("Statistics"));
         addOutputNumericalValue(MEAN, Sextante.getText("Mean_value"));
         addOutputNumericalValue(MEAN_SQUARED, Sextante.getText("Mean_squared_value"));
         addOutputNumericalValue(MIN, Sextante.getText("Minimum_value"));
         addOutputNumericalValue(MAX, Sextante.getText("Maximum_value"));
         addOutputNumericalValue(VARIANCE, Sextante.getText("Variance"));
         addOutputNumericalValue(SUM, Sextante.getText("Total_sum"));
         addOutputNumericalValue(COEF_OF_VARIATION, Sextante.getText("Coefficient_of_variation"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final UndefinedParentParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final OptionalParentParameterException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
