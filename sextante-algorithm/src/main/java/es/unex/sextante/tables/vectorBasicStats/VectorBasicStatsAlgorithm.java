

package es.unex.sextante.tables.vectorBasicStats;

import java.text.DecimalFormat;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.math.simpleStats.SimpleStats;


public class VectorBasicStatsAlgorithm
         extends
            GeoAlgorithm {

   public static final String STATS             = "STATS";
   public static final String FIELD             = "FIELD";
   public static final String LAYER             = "LAYER";
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
      int iCount;
      double dValue;
      IVectorLayer layer;
      final SimpleStats stats = new SimpleStats();

      layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      iField = m_Parameters.getParameterValueAsInt(FIELD);

      i = 0;
      iCount = layer.getShapesCount();
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(i, iCount)) {
         final IFeature feature = iter.next();
         try {
            dValue = Double.parseDouble(feature.getRecord().getValue(iField).toString());
            stats.addValue(dValue);
         }
         catch (final Exception e) {
         }
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

         addOutputText(STATS, Sextante.getText("Statistics") + "[" + layer.getName() + "]", doc.getHTMLCode());

         addOutputNumericalValue(MEAN, stats.getMean());
         addOutputNumericalValue(MEAN_SQUARED, stats.getRMS());
         addOutputNumericalValue(MIN, stats.getMin());
         addOutputNumericalValue(MAX, stats.getMax());
         addOutputNumericalValue(VARIANCE, stats.getVariance());
         addOutputNumericalValue(SUM, stats.getSum());
         addOutputNumericalValue(COEF_OF_VARIATION, stats.getCoeffOfVar());


         return true;
      }

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Basic_statistics"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);
      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), "LAYER");
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
