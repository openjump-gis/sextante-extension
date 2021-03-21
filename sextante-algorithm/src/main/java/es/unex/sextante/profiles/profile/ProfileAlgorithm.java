

package es.unex.sextante.profiles.profile;

import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;


public class ProfileAlgorithm
         extends
            GeoAlgorithm {

   public static final String ROUTE         = "ROUTE";
   public static final String DEM           = "DEM";
   public static final String LAYERS        = "LAYERS";
   public static final String PROFILEPOINTS = "PROFILEPOINTS";
   public static final String GRAPH         = "GRAPH";
   public static final String INTERPOLATE   = "INTERPOLATE";

   private IVectorLayer       m_Profile;
   private IRasterLayer       m_DEM;
   private IRasterLayer       m_Layer[];
   private double             m_dDist       = 0, m_dHorzDist = 0;
   private double             m_dLastX, m_dLastY, m_dLastZ;
   private int                m_iPoints     = 0;
   private XYSeries           serie;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      Class types[];
      String sFieldNames[];
      final XYSeriesCollection dataset = new XYSeriesCollection();
      serie = new XYSeries(Sextante.getText("Profile"));
      dataset.addSeries(serie);

      boolean bInterpolate = m_Parameters.getParameterValueAsBoolean(INTERPOLATE);

      final IVectorLayer lines = m_Parameters.getParameterValueAsVectorLayer(ROUTE);

      if (lines.getShapesCount() == 0) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Zero_lines_in_layer"));
      }

      final ArrayList layers = m_Parameters.getParameterValueAsArrayList(LAYERS);
      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
      m_DEM.setFullExtent();
      if (!bInterpolate) {
         m_DEM.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);
      }
      final AnalysisExtent extent = m_DEM.getWindowGridExtent();

      m_Layer = new IRasterLayer[layers.size()];
      for (i = 0; i < layers.size(); i++) {
         m_Layer[i] = (IRasterLayer) layers.get(i);
         m_Layer[i].setWindowExtent(extent);
         if (!bInterpolate) {
            m_Layer[i].setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);
         }
      }

      sFieldNames = new String[layers.size() + 5];
      sFieldNames[0] = "X";
      sFieldNames[1] = "Y";
      sFieldNames[2] = "Z";
      sFieldNames[3] = Sextante.getText("ProjDist");
      sFieldNames[4] = Sextante.getText("RealDist");
      for (i = 0; i < layers.size(); i++) {
         sFieldNames[i + 5] = m_Layer[i].getName();
      }

      types = new Class[layers.size() + 5];
      for (i = 0; i < types.length; i++) {
         types[i] = Double.class;
      }

      m_Profile = getNewVectorLayer(PROFILEPOINTS, Sextante.getText("Profile_[points]"), IVectorLayer.SHAPE_TYPE_POINT, types,
               sFieldNames);

      final IFeatureIterator iterator = lines.iterator();
      final Geometry line = iterator.next().getGeometry().getGeometryN(0);
      processLine(line);
      iterator.close();

      final JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, false, true,
               true);

      final ChartPanel jPanelChart = new ChartPanel(chart);
      jPanelChart.setPreferredSize(new java.awt.Dimension(500, 300));
      jPanelChart.setPreferredSize(new java.awt.Dimension(500, 300));
      jPanelChart.setBorder(javax.swing.BorderFactory.createLineBorder(java.awt.Color.gray, 1));

      addOutputChart(GRAPH, Sextante.getText("Profile"), jPanelChart);

      return true;
   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Profile"));
      setGroup(Sextante.getText("Profiles"));
      setUserCanDefineAnalysisExtent(false);
      try {
         m_Parameters.addInputVectorLayer(ROUTE, Sextante.getText("Profile_route"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE,
                  true);
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         m_Parameters.addMultipleInput(LAYERS, Sextante.getText("Additional_layers"),
                  AdditionalInfoMultipleInput.DATA_TYPE_RASTER, false);
         m_Parameters.addBoolean(INTERPOLATE, Sextante.getText("Use_interpolation"), false);
         addOutputVectorLayer(PROFILEPOINTS, Sextante.getText("Profile_[points]"), OutputVectorLayer.SHAPE_TYPE_POINT);
         addOutputChart(GRAPH, Sextante.getText("Profile"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void processLine(final Geometry line) {

      double x, y, x2, y2;
      final Coordinate[] coords = line.getCoordinates();

      for (int i = 0; (i < coords.length - 1) && setProgress(i, coords.length - 1); i++) {
         x = coords[i].x;
         y = coords[i].y;
         x2 = coords[i + 1].x;
         y2 = coords[i + 1].y;
         processSegment(x, y, x2, y2);
      }

   }


   private void processSegment(double x,
                               double y,
                               final double x2,
                               final double y2) {

      double dx, dy, d, n;

      dx = Math.abs(x2 - x);
      dy = Math.abs(y2 - y);

      if ((dx > 0.0) || (dy > 0.0)) {
         if (dx > dy) {
            dx /= m_DEM.getWindowCellSize();
            n = dx;
            dy /= dx;
            dx = m_DEM.getWindowCellSize();
         }
         else {
            dy /= m_DEM.getWindowCellSize();
            n = dy;
            dx /= dy;
            dy = m_DEM.getWindowCellSize();
         }

         if (x2 < x) {
            dx = -dx;
         }

         if (y2 < y) {
            dy = -dy;
         }

         for (d = 0.0; d <= n; d++, x += dx, y += dy) {
            addPoint(x, y);
         }
      }

   }


   private void addPoint(final double x,
                         final double y) {

      int i;
      double z;
      double dDX, dDY, dDZ;
      double dValue;
      final Object values[] = new Object[m_Layer.length + 5];

      z = m_DEM.getValueAt(x, y);

      if (m_iPoints == 0) {
         m_dDist = 0.0;
         m_dHorzDist = 0.0;
      }
      else {
         dDX = x - m_dLastX;
         dDY = y - m_dLastY;
         if (m_DEM.isNoDataValue(z) || m_DEM.isNoDataValue(m_dLastZ)) {
            dDZ = 0.0;
         }
         else {
            dDZ = z - m_dLastZ;
         }
         m_dDist += Math.sqrt(dDX * dDX + dDY * dDY);
         m_dHorzDist += Math.sqrt(dDX * dDX + dDY * dDY + dDZ * dDZ);
      }

      m_dLastX = x;
      m_dLastY = y;
      m_dLastZ = z;
      m_iPoints++;

      final Point geometry = new GeometryFactory().createPoint(new Coordinate(x, y));
      values[0] = new Double(x);
      values[1] = new Double(y);
      values[2] = new Double(z);
      values[3] = new Double(m_dDist);
      values[4] = new Double(m_dHorzDist);
      for (i = 0; i < m_Layer.length; i++) {
         dValue = m_Layer[i].getValueAt(x, y);
         values[i + 5] = new Double(dValue);
      }
      if (!m_DEM.isNoDataValue(z)) {
         serie.add(m_dDist, z);
      }
      m_Profile.addFeature(geometry, values);

   }

}
