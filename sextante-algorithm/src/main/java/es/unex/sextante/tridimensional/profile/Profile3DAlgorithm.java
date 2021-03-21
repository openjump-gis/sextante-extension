/**
 *
 * Ported from
 * B.Pendleton.  line3d - 3D Bresenham's (a 3D line drawing algorithm)
 * ftp://ftp.isc.org/pub/usenet/comp.sources.unix/volume26/line3d, 1992
 *
 */

package es.unex.sextante.tridimensional.profile;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.rasterWrappers.Grid3DCell;

public class Profile3DAlgorithm
         extends
            GeoAlgorithm {


   public static final String LINE          = "LINE";
   public static final String RASTER_LAYER  = "RASTER_LAYER";
   public static final String PROFILEPOINTS = "PROFILEPOINTS";
   public static final String GRAPH         = "GRAPH";

   private I3DRasterLayer     m_RasterLayer;
   private XYDataset          dataset;
   private IVectorLayer       m_Profile;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("3D_Profile"));
      setGroup(Sextante.getText("3D"));

      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInput3DRasterLayer(RASTER_LAYER, Sextante.getText("3D_Raster_Layer"), true);
         m_Parameters.addInputVectorLayer(LINE, Sextante.getText("Profile_line"), IVectorLayer.SHAPE_TYPE_LINE, true);
         addOutputVectorLayer(PROFILEPOINTS, Sextante.getText("Profile_[points]"), OutputVectorLayer.SHAPE_TYPE_POINT);
         addOutputChart(GRAPH, Sextante.getText("Profile"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer lines = m_Parameters.getParameterValueAsVectorLayer(LINE);
      m_RasterLayer = m_Parameters.getParameterValueAs3DRasterLayer(RASTER_LAYER);

      if (lines.getShapesCount() == 0) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Zero_lines_in_layer"));
      }

      final String[] sFieldNames = new String[] { "X", "Y", "Z", m_RasterLayer.getName() };
      final Class[] types = new Class[] { Double.class, Double.class, Double.class, Double.class };

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


      return !m_Task.isCanceled();

   }


   private void processLine(final Geometry line) {

      final Coordinate[] coords = line.getCoordinates();

      for (int i = 0; (i < coords.length - 1) && setProgress(i, coords.length - 1); i++) {
         processSegment(coords[i], coords[i + 1]);
      }

   }


   private void processSegment(final Coordinate coord,
                               final Coordinate coord2) {

      final GeometryFactory gf = new GeometryFactory();
      final AnalysisExtent extent = m_RasterLayer.getLayerExtent();

      final Grid3DCell cell = extent.getGridCoordsFromWorldCoords(coord.x, coord.y, coord.z);

      final int x1 = cell.getX();
      final int y1 = cell.getY();
      final int z1 = cell.getZ();

      final Grid3DCell cell2 = extent.getGridCoordsFromWorldCoords(coord2.x, coord2.y, coord2.z);

      final int x2 = cell2.getX();
      final int y2 = cell2.getY();
      final int z2 = cell2.getZ();

      final int dx = x2 - x1;
      final int dy = y2 - y1;
      final int dz = z2 - z1;

      final int ax = Math.abs(dx) * 2;
      final int ay = Math.abs(dy) * 2;
      final int az = Math.abs(dz) * 2;

      final int sx = (int) Math.signum(dx);
      final int sy = (int) Math.signum(dy);
      final int sz = (int) Math.signum(dz);

      int x = x1;
      int y = y1;
      int z = z1;
      int idx = 1;

      if (ax >= Math.max(ay, az)) { // x dominant
         double yd = ay - ax / 2;
         double zd = az - ax / 2;

         while (true) {
            final Coordinate newCoord = extent.getWorldCoordsFromGridCoords(x, y, z);
            final double dValue = m_RasterLayer.getCellValueAsDouble(x, y, z);
            if (!m_RasterLayer.isNoDataValue(dValue)) {
               final Point pt = gf.createPoint(newCoord);
               m_Profile.addFeature(pt, new Object[] { new Double(x), new Double(y), new Double(z), new Double(dValue) });
            }
            idx++;

            if (x == x2) { // end
               break;
            }

            if (yd >= 0) { // move along y
               y = y + sy;
               yd = yd - ax;
            }

            if (zd >= 0) { // move along z
               z = z + sz;
               zd = zd - ax;
            }

            x = x + sx; // move along x
            yd = yd + ay;
            zd = zd + az;
         }
      }
      else if (ay >= Math.max(ax, az)) { // y dominant
         double xd = ax - ay / 2;
         double zd = az - ay / 2;

         while (true) {
            final Coordinate newCoord = extent.getWorldCoordsFromGridCoords(x, y, z);
            final double dValue = m_RasterLayer.getCellValueAsDouble(x, y, z);
            if (!m_RasterLayer.isNoDataValue(dValue)) {
               final Point pt = gf.createPoint(newCoord);
               m_Profile.addFeature(pt, new Object[] { new Double(x), new Double(y), new Double(z), new Double(dValue) });
            }
            idx++;

            if (y == y2) { //end
               break;
            }

            if (xd >= 0) { // move along x
               x = x + sx;
               xd = xd - ay;
            }

            if (zd >= 0) { // move along z
               z = z + sz;
               zd = zd - ay;
            }

            y = y + sy; // move along y
            xd = xd + ax;
            zd = zd + az;
         }
      }
      else if (az >= Math.max(ax, ay)) { // z dominant
         double xd = ax - az / 2;
         double yd = ay - az / 2;

         while (true) {
            final Coordinate newCoord = extent.getWorldCoordsFromGridCoords(x, y, z);
            final double dValue = m_RasterLayer.getCellValueAsDouble(x, y, z);
            if (!m_RasterLayer.isNoDataValue(dValue)) {
               final Point pt = gf.createPoint(newCoord);
               m_Profile.addFeature(pt, new Object[] { new Double(x), new Double(y), new Double(z), new Double(dValue) });
            }
            idx++;

            if (z == z2) { // end
               break;
            }

            if (xd >= 0) { // move along x
               x = x + sx;
               xd = xd - az;
            }

            if (yd >= 0) { // move along y
               y = y + sy;
               yd = yd - az;
            }

            z = z + sz; // move along z
            xd = xd + ax;
            yd = yd + ay;
         }
      }

   }
}
