package es.unex.sextante.additionalResults;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
//import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import es.unex.sextante.math.regression.Regression;

/**
 * This class creates a graph representing a regression analysis, with a cloud of points and an adjusted function
 * 
 * @author volaya
 * 
 */
public class CorrelationGraphCreator {

   private JFreeChart       chart;
   private ChartPanel       jPanelChart = null;
   private final Regression regression;


   /**
    * Constructs the graph based on a given regression
    * 
    * @param reg
    *                the regression
    */
   public CorrelationGraphCreator(final Regression reg) {

      regression = reg;

      createChart();

      final Plot plot = chart.getPlot();
      plot.setOutlineStroke(new BasicStroke(1));
      plot.setOutlinePaint(Color.blue);

   }


   /**
    * Returns a panel containing the chart
    * 
    * @return a panel containing the chart
    */
   public ChartPanel getChartPanel() {

      if (jPanelChart == null) {
         jPanelChart = new ChartPanel(chart);
      }
      return jPanelChart;
   }


   /**
    * Returns the regression chart
    * 
    * @return the regression chart
    */
   public JFreeChart getChart() {

      return chart;

   }


   private void createChart() {

      final XYDataset data1 = getOriginalDataset();
      chart = ChartFactory.createScatterPlot(null, null, null, data1, PlotOrientation.VERTICAL, false, true, true);

      final XYPlot plot = chart.getXYPlot();
      plot.setRenderer(new XYDotRenderer());
      plot.setDomainCrosshairVisible(true);
      plot.setRangeCrosshairVisible(true);
      plot.getRenderer().setSeriesPaint(0, Color.blue);

      final XYDataset data2 = getDatasetFromFitting();
      final XYItemRenderer renderer = new StandardXYItemRenderer();
      renderer.setSeriesPaint(0, Color.red);

      plot.setDataset(1, data2);
      plot.setRenderer(1, renderer);

   }


   private XYDataset getOriginalDataset() {

      int i;
      final XYSeries series = new XYSeries("");
      final int NUM_POINTS_FOR_GRAPH = 2000;

      final double cellValue1[] = new double[NUM_POINTS_FOR_GRAPH];
      final double cellValue2[] = new double[NUM_POINTS_FOR_GRAPH];
      regression.getRestrictedSample(cellValue1, cellValue2, NUM_POINTS_FOR_GRAPH);
      for (i = 0; i < cellValue1.length; i++) {
         series.add(cellValue1[i], cellValue2[i]);
      }

      XYSeriesCollection dataset;

      dataset = new XYSeriesCollection();
      dataset.addSeries(series);

      return dataset;

   }


   private XYDataset getDatasetFromFitting() {

      int i;
      final int STEPS = 200;
      double x, y;
      final XYSeries series = new XYSeries("");
      XYSeriesCollection dataset;

      final double dStep = (regression.getXMax() - regression.getXMin()) / STEPS;

      for (i = 0; i < STEPS; i++) {
         x = regression.getXMin() + dStep * i;
         y = regression.getY(x);
         series.add(x, y);
      }

      dataset = new XYSeriesCollection();
      dataset.addSeries(series);

      return dataset;

   }

}
