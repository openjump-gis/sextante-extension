package es.unex.sextante.lighting.fresnelLos;

import java.awt.geom.Point2D;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
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

public class FresnelLOSAlgorithm
         extends
            GeoAlgorithm {

   public static final String       DEM       = "DEM";
   public static final String       POINT     = "POINT";
   public static final String       POINT2    = "POINT2";
   public static final String       HEIGHT    = "HEIGHT";
   public static final String       HEIGHT2   = "HEIGHT2";
   public static final String       GRAPH     = "GRAPH";
   public static final String       LAMBDA    = "LAMBDA";

   private IRasterLayer             m_DEM     = null;
   private GridCell                 m_Point, m_Point2;
   private double                   m_dHeight, m_dHeight2;
   private final XYSeriesCollection m_Dataset = new XYSeriesCollection();
   private double                   m_dFrequency;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
      final Point2D pt = m_Parameters.getParameterValueAsPoint(POINT);
      final Point2D pt2 = m_Parameters.getParameterValueAsPoint(POINT2);
      m_dHeight = m_Parameters.getParameterValueAsDouble(HEIGHT);
      m_dHeight2 = m_Parameters.getParameterValueAsDouble(HEIGHT2);
      m_dFrequency = m_Parameters.getParameterValueAsDouble(LAMBDA);

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
         m_Parameters.addNumericalValue(LAMBDA, Sextante.getText("Wavelength"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 0, 0, Double.MAX_VALUE);

         addOutputChart(GRAPH, Sextante.getText("Line_of_sight"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

      setName(Sextante.getText("RF_Line_of_sight"));
      setGroup(Sextante.getText("Visibility_and_lighting"));

   }


   private void calculateLOS(int x,
                             int y,
                             final int x2,
                             final int y2) {

      double dx, dy;
      double ix, iy, id, d, dist, z;
      double dHeight, dHeight2;
      double dAngle;
      final XYSeries serie = new XYSeries("");
      final XYSeries fresnelSerie = new XYSeries("");
      final XYSeries straightlineSerie = new XYSeries("");

      m_Dataset.addSeries(serie);
      m_Dataset.addSeries(straightlineSerie);
      m_Dataset.addSeries(fresnelSerie);

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
         dHeight = m_DEM.getCellValueAsDouble(x, y);
         dHeight2 = m_DEM.getCellValueAsDouble(x2, y2);

         if (m_DEM.isNoDataValue(dHeight) || m_DEM.isNoDataValue(dHeight2)) {
            return;
         }

         dHeight += m_dHeight;
         dHeight2 += m_dHeight2;
         dAngle = Math.atan2(dHeight2 - dHeight, dist * m_DEM.getWindowCellSize());

         straightlineSerie.add(0, dHeight);
         straightlineSerie.add(dist * m_DEM.getWindowCellSize(), dHeight2);
         fresnelSerie.add(0, dHeight);

         while (id < dist) {
            id += d;

            ix += dx;
            iy += dy;

            x = (int) ix;
            y = (int) iy;

            z = m_DEM.getCellValueAsDouble(x, y);
            if (!m_DEM.isNoDataValue(z)) {
               serie.add(id * m_DEM.getWindowCellSize(), z);
               final double dDist = id * m_DEM.getWindowCellSize() / Math.cos(dAngle);
               final double dDist2 = (dist - id) * m_DEM.getWindowCellSize() / Math.cos(dAngle);
               final Point2D pt = getFresnelZone(dDist, dDist2, dAngle, dHeight);
               if (!Double.isNaN(pt.getX()) && !Double.isNaN(pt.getY())) {
                  fresnelSerie.add(pt.getX(), pt.getY());
               }
            }
         }
         fresnelSerie.add(dist * m_DEM.getWindowCellSize(), dHeight2);

         final JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, m_Dataset, PlotOrientation.VERTICAL, false,
                  true, true);

         final ChartPanel jPanelChart = new ChartPanel(chart);
         jPanelChart.setPreferredSize(new java.awt.Dimension(500, 300));
         jPanelChart.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray, 1));
         addOutputChart("GRAPH", Sextante.getText("Line_of_sight"), jPanelChart);
      }

   }


   private Point2D getFresnelZone(final double dDist,
                                  final double dDist2,
                                  final double dAngle,
                                  final double dHeight) {

      final double dRadius = Math.sqrt(m_dFrequency * (dDist * dDist2) / (dDist + dDist2));
      double y = dHeight + dDist * Math.sin(dAngle);
      double x = dDist * Math.cos(dAngle);

      x += (dRadius * Math.sin(dAngle));
      y -= (dRadius * Math.cos(dAngle));

      System.out.println(dRadius);


      return new Point2D.Double(x, y);

   }

}
