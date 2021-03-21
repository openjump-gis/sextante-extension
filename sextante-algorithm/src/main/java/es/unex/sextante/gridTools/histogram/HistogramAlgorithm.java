package es.unex.sextante.gridTools.histogram;

import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class HistogramAlgorithm
         extends
            GeoAlgorithm {

   private static final int   CLASS_COUNT = 100;

   public static final String GRID        = "GRID";
   public static final String GRAPH       = "GRAPH";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int i;
      int A;
      int iNX, iNY;
      double z;
      double dMin = 0, dMax = 0;
      double Count[];

      Count = new double[CLASS_COUNT + 1];

      final IRasterLayer input = m_Parameters.getParameterValueAsRasterLayer(GRID);
      input.setFullExtent();

      iNX = input.getNX();
      iNY = input.getNY();

      A = 0;

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            z = input.getCellValueAsDouble(x, y);
            if (!input.isNoDataValue(z)) {
               if (A <= 0) {
                  dMin = dMax = z;
               }
               else {
                  if (dMin > z) {
                     dMin = z;
                  }
                  else if (dMax < z) {
                     dMax = z;
                  }
               }
               A++;
            }
         }
      }

      final double dInterval = (dMax - dMin) / CLASS_COUNT;
      if ((A > 0) && (dMin < dMax)) {
         for (y = 0; y < iNY; y++) {
            for (x = 0; x < iNX; x++) {
               z = input.getCellValueAsDouble(x, y);
               if (!input.isNoDataValue(z)) {
                  i = (int) ((z - dMin) / dInterval);
                  Count[Math.min(i, CLASS_COUNT)]++;
               }
            }
         }

         final ArrayList list = new ArrayList();
         for (i = 0; i < Count.length; i++) {
            final int iCount = (int) (10000 * Count[i] / A);
            for (int j = 0; j < iCount; j++) {
               list.add(new Double(dMin + dInterval * i));
            }
         }

         final double countForHistogram[] = new double[list.size()];
         for (int j = 0; j < list.size(); j++) {
            countForHistogram[j] = ((Double) list.get(j)).doubleValue();
         }

         final HistogramDataset dataset = new HistogramDataset();
         dataset.addSeries(input.getName(), countForHistogram, 100);

         final JFreeChart chart = ChartFactory.createHistogram("", null, null, dataset, PlotOrientation.VERTICAL, true, false,
                  false);

         final ChartPanel jPanelChart = new ChartPanel(chart);
         jPanelChart.setMouseZoomable(true, true);
         jPanelChart.setPreferredSize(new java.awt.Dimension(500, 300));
         jPanelChart.setPreferredSize(new java.awt.Dimension(500, 300));
         jPanelChart.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray, 1));

         addOutputChart(GRAPH, Sextante.getText("Histogram") + "[" + input.getName() + "]", jPanelChart);
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Histogram"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);
      try {
         m_Parameters.addInputRasterLayer(GRID, Sextante.getText("Raster_layer"), true);
         addOutputChart(GRAPH, Sextante.getText("Histogram"), null);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
