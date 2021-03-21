package es.unex.sextante.gridCategorical.lacunarity;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.math.simpleStats.SimpleStats;

public class LacunarityAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT_CHART = "RESULT_CHART";
   public static final String RESULT_TABLE = "RESULT_TABLE";
   public static final String INPUT        = "INPUT";
   public static final String MAX_SIZE     = "MAX_SIZE";

   private int                m_iNX, m_iNY;
   private IRasterLayer       m_Window;
   private int                m_iMaxSize;
   private double[]           m_dLacunarity;


   @Override
   public void defineCharacteristics() {

      this.setName(Sextante.getText("Lacunarity"));
      setGroup(Sextante.getText("Raster_categories_analysis"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Input_Layer"), true);
         m_Parameters.addNumericalValue(MAX_SIZE, Sextante.getText("Largest_window_size"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 10, 4, Integer.MAX_VALUE);
         addOutputTable(RESULT_TABLE, Sextante.getText("Lacunarity"));
         addOutputChart(RESULT_CHART, Sextante.getText("Lacunarity"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_Window = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      m_iMaxSize = m_Parameters.getParameterValueAsInt(MAX_SIZE);
      m_Window.setFullExtent();

      m_iNX = m_Window.getNX();
      m_iNY = m_Window.getNY();

      if (calculateLacunarity()) {
         createTableAndGraph();
         return true;
      }
      else {
         return false;
      }

   }


   private boolean calculateLacunarity() {

      int x, y;

      m_dLacunarity = new double[m_iMaxSize + 1];

      for (int iSize = 2; iSize < m_dLacunarity.length; iSize++) {
         setProgressText("Box size: " + Integer.toString(iSize));
         final SimpleStats stats = new SimpleStats();
         for (y = 0; (y < m_iNY - iSize) && setProgress(y, m_iNY); y++) {
            for (x = 0; x < m_iNX - iSize; x++) {
               final int iBoxMass = getBoxMass(x, y, iSize);
               stats.addValue(iBoxMass);
            }
         }
         m_dLacunarity[iSize] = 1 + (stats.getVariance() / Math.pow(stats.getMean(), 2.));
      }

      return !m_Task.isCanceled();

   }


   private int getBoxMass(final int x,
                          final int y,
                          final int iSize) {

      int iBoxMass = 0;

      for (int i = 0; i < iSize; i++) {
         for (int j = 0; j < iSize; j++) {
            final double dValue = m_Window.getCellValueAsDouble(x + i, y + j);
            if (!m_Window.isNoDataValue(dValue)) {
               iBoxMass++;
            }
         }
      }

      return iBoxMass;

   }


   private void createTableAndGraph() throws UnsupportedOutputChannelException {

      Object[] values;


      final String sFields[] = { Sextante.getText("Window_size"), Sextante.getText("Lacunaridad") };
      final Class types[] = { Integer.class, Double.class };
      final String sTableName = Sextante.getText("Lacunaridad") + "[" + m_Window.getName() + "]";

      final ITable table = getNewTable(RESULT_TABLE, sTableName, types, sFields);
      values = new Object[2];

      final XYSeriesCollection dataset = new XYSeriesCollection();
      final XYSeries serie = new XYSeries(Sextante.getText("Profile"));
      dataset.addSeries(serie);

      for (int i = 2; i < m_dLacunarity.length; i++) {
         values[0] = new Integer(i);
         values[1] = new Double(m_dLacunarity[i]);
         table.addRecord(values);
         serie.add(i, m_dLacunarity[i]);
      }

      final JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, false, true,
               true);

      final ChartPanel jPanelChart = new ChartPanel(chart);
      jPanelChart.setPreferredSize(new java.awt.Dimension(500, 300));
      jPanelChart.setPreferredSize(new java.awt.Dimension(500, 300));
      jPanelChart.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray, 1));

      addOutputChart(RESULT_CHART, Sextante.getText("Profile"), jPanelChart);

   }

}
