

package es.unex.sextante.tables.vectorHistogram;

import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;


public class VectorHistogramAlgorithm
         extends
            GeoAlgorithm {

   public static final String HISTOGRAM = "HISTOGRAM";
   public static final String FIELD     = "FIELD";
   public static final String LAYER     = "LAYER";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iField;
      int iShapesCount;
      double dValue;
      IVectorLayer layer;

      layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      iField = m_Parameters.getParameterValueAsInt(FIELD);

      i = 0;
      final ArrayList<Double> array = new ArrayList<Double>();
      iShapesCount = layer.getShapesCount();
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(i, iShapesCount)) {
         final IFeature feature = iter.next();
         try {
            dValue = Double.parseDouble(feature.getRecord().getValue(iField).toString());
            array.add(new Double(dValue));
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
         final HistogramDataset dataset = new HistogramDataset();
         final double[] values = new double[array.size()];
         for (int j = 0; j < array.size(); j++) {
            values[j] = array.get(j).doubleValue();
         }
         dataset.addSeries("Histogram", values, 100);
         final JFreeChart chart = ChartFactory.createHistogram("", null, null, dataset, PlotOrientation.VERTICAL, true, false,
                  false);
         final ChartPanel jPanelChart = new ChartPanel(chart);
         jPanelChart.setMouseZoomable(true, true);
         jPanelChart.setPreferredSize(new java.awt.Dimension(500, 300));
         jPanelChart.setPreferredSize(new java.awt.Dimension(500, 300));
         jPanelChart.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray, 1));

         addOutputChart(HISTOGRAM, Sextante.getText("Histogram"), jPanelChart);
         return true;
      }

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Histogram"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);
      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), "LAYER");
         addOutputChart(HISTOGRAM, Sextante.getText("Histogram"));
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
