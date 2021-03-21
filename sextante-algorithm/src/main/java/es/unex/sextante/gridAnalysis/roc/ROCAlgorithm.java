package es.unex.sextante.gridAnalysis.roc;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.locationtech.jts.geom.Coordinate;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;

public class ROCAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT               = "INPUT";
   public static final String POINTS              = "POINTS";
   public static final String GRAPH               = "GRAPH";
   public static final String FIELD               = "FIELD";
   public static final String AREA                = "AREA";

   private IVectorLayer       m_Points;
   private IRasterLayer       m_Suitability;
   private XYSeries           serie;
   private boolean            bPresence[];
   private int                m_iField;
   private final double       m_dLastSensitivity  = 0;
   private final double       m_dLastEspecificity = 0;
   private double             m_dArea;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("ROC_curve"));
      setGroup(Sextante.getText("Raster_layer_analysis"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Suitability"), true);
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Observed_points"),
                  AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Presence-abscence_field"), POINTS);
         addOutputChart(GRAPH, Sextante.getText("ROC"));
         addOutputText(AREA, Sextante.getText("ROC_curve"));
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


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_dArea = 0;

      m_Suitability = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      m_Points = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      m_iField = m_Parameters.getParameterValueAsInt(FIELD);

      m_Suitability.setFullExtent();

      final XYSeriesCollection dataset = new XYSeriesCollection();
      serie = new XYSeries(Sextante.getText("Profile"));
      dataset.addSeries(serie);

      createPointsData();
      calculateCurveData();

      final JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, false, true,
               true);

      final ChartPanel jPanelChart = new ChartPanel(chart);
      jPanelChart.setPreferredSize(new java.awt.Dimension(500, 300));
      jPanelChart.setPreferredSize(new java.awt.Dimension(500, 300));
      jPanelChart.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray, 1));

      addOutputChart(GRAPH, Sextante.getText("ROC"), jPanelChart);

      final HTMLDoc doc = new HTMLDoc();
      doc.open(Sextante.getText("ROC_curve"));
      doc.addHeader(Sextante.getText("ROC_curve"), 2);
      doc.startUnorderedList();
      doc.addListElement(Sextante.getText("area_under_the_curve") + ":" + Double.toString(m_dArea));
      doc.closeUnorderedList();
      doc.close();

      addOutputText(AREA, Sextante.getText("ROC_curve"), doc.getHTMLCode());

      return !m_Task.isCanceled();

   }


   private void createPointsData() {

      int iPoints;

      iPoints = m_Points.getShapesCount();

      bPresence = new boolean[iPoints];

      int i = 0;
      final IFeatureIterator iter = m_Points.iterator();
      while (iter.hasNext()) {
         try {
            final IFeature feature = iter.next();
            final Object[] record = feature.getRecord().getValues();
            final String sValue = record[m_iField].toString();
            final double dValue = Double.parseDouble(sValue);
            bPresence[i] = (dValue != 0.0);
         }
         catch (final Exception e) {
            bPresence[i] = false;
         }
         i++;
      }
      iter.close();

   }


   private void calculateCurveData() throws IteratorException {

      double dValue;
      int iFalsePositives;
      int iTruePositives;
      int iTrueNegatives;
      int iFalseNegatives;

      for (int i = 0; (i < 100) && setProgress(i, 100); i++) {
         final double dCutoff = (i) / 100.;
         iFalsePositives = 0;
         iTruePositives = 0;
         iFalseNegatives = 0;
         iTrueNegatives = 0;
         int j = 0;
         final IFeatureIterator iter = m_Points.iterator();
         while (iter.hasNext()) {
            final IFeature feature = iter.next();
            final Coordinate pt = feature.getGeometry().getCoordinate();
            dValue = m_Suitability.getValueAt(pt.x, pt.y);
            if (!m_Suitability.isNoDataValue(dValue)) {
               if (dValue < dCutoff) {
                  if (bPresence[j]) {
                     iTruePositives++;
                  }
                  else {
                     iFalsePositives++;
                  }
               }
               else {
                  if (bPresence[j]) {
                     iFalseNegatives++;
                  }
                  else {
                     iTrueNegatives++;
                  }
               }
            }
            j++;
         }
         iter.close();

         final double dSensitivity = ((double) iTruePositives) / (double) (iTruePositives + iFalseNegatives);
         final double dEspecificity = ((double) iFalsePositives) / (double) (iFalsePositives + iTrueNegatives);
         if (!Double.isNaN(dSensitivity) && !Double.isNaN(dEspecificity)) {
            serie.add(dSensitivity, dEspecificity);
            m_dArea += (dEspecificity + m_dLastEspecificity) / 2. * (dSensitivity - m_dLastSensitivity);
         }
      }

   }


}
