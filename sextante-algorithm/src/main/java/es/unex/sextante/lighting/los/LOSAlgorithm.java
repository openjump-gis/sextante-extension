package es.unex.sextante.lighting.los;

import java.awt.Color;
import java.awt.geom.Point2D;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterWrappers.GridCell;

public class LOSAlgorithm
         extends
            GeoAlgorithm {

   public static final String       DEM       = "DEM";
   public static final String       POINT     = "POINT";
   public static final String       POINT2    = "POINT2";
   public static final String       HEIGHT    = "HEIGHT";
   public static final String       HEIGHT2   = "HEIGHT2";
   public static final String       GRAPH     = "GRAPH";

   private IRasterLayer             m_DEM     = null;
   private GridCell                 m_Point, m_Point2;
   private double                   m_dHeight, m_dHeight2;
   private final XYSeriesCollection m_Dataset = new XYSeriesCollection();
   private double                   m_dMin    = Double.MAX_VALUE;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
      final Point2D pt = m_Parameters.getParameterValueAsPoint(POINT);
      final Point2D pt2 = m_Parameters.getParameterValueAsPoint(POINT2);
      m_dHeight = m_Parameters.getParameterValueAsDouble(HEIGHT);
      m_dHeight2 = m_Parameters.getParameterValueAsDouble(HEIGHT2);

      final AnalysisExtent ge = new AnalysisExtent(m_DEM);
      m_Point = ge.getGridCoordsFromWorldCoords(pt);
      m_Point2 = ge.getGridCoordsFromWorldCoords(pt2);

      final double x = ge.getXMin() + m_Point.getX() * ge.getCellSize();
      final double y = ge.getYMax() - m_Point.getY() * ge.getCellSize();
      final double x2 = ge.getXMin() + m_Point2.getX() * ge.getCellSize();
      final double y2 = ge.getYMax() - m_Point2.getY() * ge.getCellSize();

      ge.setXRange(x, x2, true);
      ge.setYRange(y, y2, true);
      ge.enlargeOneCell();

      m_Point = ge.getGridCoordsFromWorldCoords(pt);
      m_Point2 = ge.getGridCoordsFromWorldCoords(pt2);

      m_DEM.setWindowExtent(ge);

      calculateLOS(m_Point.getX(), m_Point.getY(), m_Point2.getX(), m_Point2.getY());
      createChart();

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         m_Parameters.addPoint(POINT, Sextante.getText("point") + " 1");
         m_Parameters.addPoint(POINT2, Sextante.getText("point") + " 2");
         m_Parameters.addNumericalValue(HEIGHT, Sextante.getText("Height_of_point") + " 1", 0,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(HEIGHT2, Sextante.getText("Height_of_point") + " 2", 0,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputChart(GRAPH, Sextante.getText("Line_of_sight"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

      setName(Sextante.getText("Line_of_sight"));
      setGroup(Sextante.getText("Visibility_and_lighting"));

   }


   private void calculateLOS(int x,
                             int y,
                             final int x2,
                             final int y2) {

      double dx, dy;
      double ix, iy, iz, id, d, dist, z;
      double dSlope, dMaxSlope = Double.MAX_VALUE;
      double dLastZ, dLastD = 0;
      boolean bSeen;
      boolean bLast = true;
      XYSeries serie = new XYSeries("");
      final XYSeries lineSerie = new XYSeries("");


      m_Dataset.addSeries(serie);
      dx = x2 - x;
      dy = y2 - y;

      d = Math.abs(dx) > Math.abs(dy) ? Math.abs(dx) : Math.abs(dy);

      if (d > 0) {
         dist = Math.sqrt(dx * dx + dy * dy);

         dx /= d;
         dy /= d;

         d = dist / d;

         id = 0.0;
         ix = x + 0.5;
         iy = y + 0.5;
         iz = m_DEM.getCellValueAsDouble(x, y);

         if (m_DEM.isNoDataValue(iz)) {
            return;
         }

         iz += m_dHeight; // Add watcher height to watcher Z value
         dLastZ = iz;

         while (id < dist) {
            id += d;

            ix += dx;
            iy += dy;

            x = (int) ix;
            y = (int) iy;

            z = m_DEM.getCellValueAsDouble(x, y);
            if (!m_DEM.isNoDataValue(z)) {
               dSlope = (z - iz) / id;
               if (dMaxSlope == Double.MAX_VALUE) {
                  dMaxSlope = dSlope;
                  bSeen = bLast = true;
                  serie.add(0, iz);
               }
               else if (dSlope <= dMaxSlope) {
                  bSeen = false;
               }
               else {
                  bSeen = true;
                  dMaxSlope = dSlope;
               }

               if (bSeen == bLast) {
                  serie.add(id * m_DEM.getWindowCellSize(), z);
                  dLastD = id * m_DEM.getWindowCellSize();
                  dLastZ = z;
               }
               else {
                  serie = new XYSeries("");
                  m_Dataset.addSeries(serie);
                  serie.add(dLastD, dLastZ);
                  dLastD = id * m_DEM.getWindowCellSize();
                  dLastZ = z;
                  serie.add(dLastD, dLastZ);
                  bLast = bSeen;
               }
               m_dMin = Math.min(m_dMin, dLastZ);
            }
         }
         lineSerie.add(0, iz);
         lineSerie.add(dLastD, dLastZ + m_dHeight2);
         m_Dataset.addSeries(lineSerie);

      }

   }


   private void createChart() {

      int i;

      final JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, m_Dataset, PlotOrientation.VERTICAL, false, true,
               true);

      final ChartPanel jPanelChart = new ChartPanel(chart);
      jPanelChart.setPreferredSize(new java.awt.Dimension(500, 300));
      jPanelChart.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray, 1));
      final XYPlot plot = (XYPlot) chart.getPlot();
      final XYItemRenderer ren = plot.getRenderer();
      for (i = 0; i < m_Dataset.getSeriesCount() - 1; i++) {
         if (i % 2 != 0) {
            ren.setSeriesPaint(i, Color.red);
         }
         else {
            ren.setSeriesPaint(i, Color.green);
         }
      }
      ren.setSeriesPaint(i, Color.BLACK);
      plot.getRangeAxis().setAutoRange(false);
      plot.getRangeAxis().setLowerBound(m_dMin);
      addOutputChart("GRAPH", Sextante.getText("Line_of_sight"), jPanelChart);


   }


}
