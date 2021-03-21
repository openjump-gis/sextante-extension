package es.unex.sextante.lighting.horizonBlockage;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.rasterWrappers.GridCell;

public class HorizonBlockageAlgorithm
         extends
            GeoAlgorithm {

   public static final String DEM        = "DEM";
   public static final String POINT      = "POINT";
   public static final String HEIGHT     = "HEIGHT";
   public static final String RADIUS     = "RADIUS";

   public static final String RESULT     = "RESULT";
   public static final String GRAPHSLOPE = "GRAPHSLOPE";
   public static final String GRAPHDIST  = "GRAPHDIST";

   private int                m_iNX, m_iNY;
   private IRasterLayer       m_DEM      = null;
   private GridCell           m_Point;
   private double             m_dHeight;
   private int                m_iRadius;
   private IVectorLayer       m_Horizon;
   private HorizonData[]      m_HorizonData;
   private double             m_dLineLength;


   @Override
   public boolean processAlgorithm() {

      try {

         m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
         final Point2D pt = m_Parameters.getParameterValueAsPoint(POINT);
         m_dHeight = m_Parameters.getParameterValueAsDouble(HEIGHT);

         final AnalysisExtent gridExtent = new AnalysisExtent(m_DEM);
         m_DEM.setFullExtent();
         m_Horizon = getNewVectorLayer(RESULT, Sextante.getText("Horizon"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE,
                  new Class[] { Integer.class }, new String[] { "ID" });

         m_iRadius = (int) (m_Parameters.getParameterValueAsInt(RADIUS) / gridExtent.getCellSize());

         if (m_iRadius <= 0) {
            m_iRadius = Integer.MAX_VALUE;
         }

         m_iNX = m_DEM.getNX();
         m_iNY = m_DEM.getNY();

         m_Point = gridExtent.getGridCoordsFromWorldCoords(pt);

         m_dLineLength = Math.sqrt(m_iNX * m_iNX + m_iNY * m_iNY);

         calculateHorizon();
         createResults();

      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
         return false;
      }

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Horizon_blockage"));
      setGroup(Sextante.getText("Visibility_and_lighting"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         m_Parameters.addPoint(POINT, Sextante.getText("Coordinates_of_emitter-receiver"));
         m_Parameters.addNumericalValue(HEIGHT, Sextante.getText("Height_of_emitter-receiver"), 10,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(RADIUS, Sextante.getText("Radius"), 0,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputVectorLayer(RESULT, Sextante.getText("Horizon"), OutputVectorLayer.SHAPE_TYPE_LINE);
         addOutputChart(GRAPHSLOPE, Sextante.getText("Angle"));
         addOutputChart(GRAPHDIST, Sextante.getText("Distance"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void calculateHorizon() {

      m_HorizonData = new HorizonData[360];

      for (int i = 0; i < 360; i++) {
         final GridCell cell = getCellAtAngle(Math.toRadians(i));
         m_HorizonData[i] = calculateHorizonData(cell);
      }

   }


   private GridCell getCellAtAngle(final double dAngle) {

      final int x = (int) (m_Point.getX() + m_dLineLength * Math.sin(dAngle));
      final int y = (int) (m_Point.getY() + m_dLineLength * Math.cos(dAngle));

      return new GridCell(x, y, 0);

   }


   private HorizonData calculateHorizonData(final GridCell cell) {

      double dx = cell.getX() - m_Point.getX();
      double dy = cell.getY() - m_Point.getY();
      double ix, iy, z, id, d, dist;
      double dMaxDist = 0;
      int iMaxX = 0, iMaxY = 0;
      double dSlope, dMaxSlope = Double.NEGATIVE_INFINITY;
      int x, y;

      z = m_DEM.getCellValueAsDouble(m_Point.getX(), m_Point.getY()) + m_dHeight;

      d = Math.abs(dx) > Math.abs(dy) ? Math.abs(dx) : Math.abs(dy);

      if (d > 0) {
         dist = Math.sqrt(dx * dx + dy * dy);

         dx /= d;
         dy /= d;

         d = dist / d;

         id = 0.0;
         ix = m_Point.getX() + 0.5;
         iy = m_Point.getY() + 0.5;

         while (id < dist) {
            id += d;

            ix += dx;
            iy += dy;

            x = (int) ix;
            y = (int) iy;

            if (!m_DEM.getWindowGridExtent().containsCell(x, y) || (id > m_iRadius)) {
               break;
            }

            dSlope = (m_DEM.getCellValueAsDouble(x, y) - z) / id;
            if (dSlope > dMaxSlope) {
               dMaxSlope = dSlope;
               dMaxDist = id;
               iMaxX = x;
               iMaxY = y;
            }

         }

      }

      return new HorizonData(dMaxSlope, dMaxDist, iMaxX, iMaxY);

   }


   private void createResults() {

      final boolean bFirstPoint = true;
      double x, y;

      final XYSeriesCollection datasetSlope = new XYSeriesCollection();
      final XYSeries serieSlope = new XYSeries(Sextante.getText("Angle"));
      datasetSlope.addSeries(serieSlope);

      final XYSeriesCollection datasetDist = new XYSeriesCollection();
      final XYSeries serieDist = new XYSeries(Sextante.getText("Distance"));
      datasetDist.addSeries(serieDist);

      final ArrayList coordinates = new ArrayList();

      for (int i = 0; i < m_HorizonData.length; i++) {
         final double dAngle = Math.toDegrees(Math.atan(m_HorizonData[i].dAngle));
         serieDist.add(i, m_HorizonData[i].dDistance);
         serieSlope.add(i, dAngle);
         x = m_HorizonData[i].x;
         y = m_HorizonData[i].y;
         if ((x != 0) || (y != 0)) {
            final Point2D pt = m_DEM.getWindowGridExtent().getWorldCoordsFromGridCoords(new GridCell((int) x, (int) y, 0));
            x = pt.getX();
            y = pt.getY();
            coordinates.add(new Coordinate(x, y));
         }

      }

      final JFreeChart chartSlope = ChartFactory.createXYLineChart(null, null, null, datasetSlope, PlotOrientation.VERTICAL,
               false, true, true);

      final ChartPanel jPanelChartSlope = new ChartPanel(chartSlope);
      jPanelChartSlope.setPreferredSize(new java.awt.Dimension(500, 300));
      jPanelChartSlope.setPreferredSize(new java.awt.Dimension(500, 300));
      jPanelChartSlope.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray, 1));

      addOutputChart("GRAPHSLOPE", Sextante.getText("Angle"), jPanelChartSlope);

      final JFreeChart chartDist = ChartFactory.createXYLineChart(null, null, null, datasetDist, PlotOrientation.VERTICAL, false,
               true, true);

      final ChartPanel jPanelChartDist = new ChartPanel(chartDist);
      jPanelChartDist.setPreferredSize(new java.awt.Dimension(500, 300));
      jPanelChartDist.setPreferredSize(new java.awt.Dimension(500, 300));
      jPanelChartDist.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray, 1));

      addOutputChart("GRAPHDIST", Sextante.getText("Distance"), jPanelChartDist);

      final Object value[] = new Object[1];
      value[0] = new Integer(1);

      final Coordinate[] coords = new Coordinate[coordinates.size()];
      for (int i = 0; i < coords.length; i++) {
         coords[i] = (Coordinate) coordinates.get(i);
      }
      m_Horizon.addFeature(new GeometryFactory().createLineString(coords), value);

   }

   private class HorizonData {

      HorizonData(final double dA,
                  final double dD,
                  final int iX,
                  final int iY) {

         dAngle = dA;
         dDistance = dD;
         x = iX;
         y = iY;

      }

      public int    x, y;
      public double dAngle    = 0;
      public double dDistance = 0;

   }


}
