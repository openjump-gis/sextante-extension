

package es.unex.sextante.vectorTools.gridStatisticsInPolygons;

import java.util.ArrayList;
import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.math.simpleStats.SimpleStats;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.shapesTools.ShapesTools;


public class GridStatisticsInPolygonsAlgorithm
         extends
            GeoAlgorithm {

   public static final String  RESULT = "RESULT";
   public static final String  GRIDS  = "GRIDS";
   public static final String  LAYER  = "LAYER";
   private static final Double NODATA = new Double(-99999.);

   private int                 m_iNX, m_iNY;
   private IVectorLayer        m_Layer;
   private AnalysisExtent      m_Extent;
   private IRasterLayer        m_Window;
   private ArrayList           m_Grids;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Grid_statistics_in_polygons"));
      setGroup(Sextante.getText("Tools_for_polygon_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Polygons"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         m_Parameters.addMultipleInput(GRIDS, Sextante.getText("Grids"), AdditionalInfoMultipleInput.DATA_TYPE_RASTER, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Polygons"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i, j;
      int iLayer = 0;
      int iShapeCount;
      String sName;
      SimpleStats[][] stats;
      IRasterLayer grid;

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      m_Grids = m_Parameters.getParameterValueAsArrayList(GRIDS);

      if (!m_bIsAutoExtent) {
         m_Layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      iShapeCount = m_Layer.getShapesCount();
      stats = new SimpleStats[m_Grids.size()][iShapeCount];

      for (iLayer = 0; (iLayer < m_Grids.size()) && setProgress(iLayer, m_Grids.size()); iLayer++) {
         m_Window = (IRasterLayer) m_Grids.get(iLayer);
         m_Window.setFullExtent();
         m_Extent = m_Window.getWindowGridExtent();
         final Coordinate[] coords = new Coordinate[5];
         coords[0] = new Coordinate(m_Extent.getXMin(), m_Extent.getYMin());
         coords[1] = new Coordinate(m_Extent.getXMin(), m_Extent.getYMax());
         coords[2] = new Coordinate(m_Extent.getXMax(), m_Extent.getYMax());
         coords[3] = new Coordinate(m_Extent.getXMax(), m_Extent.getYMin());
         coords[4] = new Coordinate(m_Extent.getXMin(), m_Extent.getYMin());
         final GeometryFactory gf = new GeometryFactory();
         final LinearRing ring = gf.createLinearRing(coords);
         final Polygon extent = gf.createPolygon(ring, null);
         m_iNX = m_Window.getNX();
         m_iNY = m_Window.getNY();
         final IFeatureIterator iter = m_Layer.iterator();
         i = 0;
         while (iter.hasNext() && !m_Task.isCanceled()) {
            final IFeature feature = iter.next();
            final Geometry geom = feature.getGeometry();
            if (geom.intersects(extent)) {
               stats[iLayer][i] = doPolygon(geom);
            }
            i++;
         }
         iter.close();
      }

      if (m_Task.isCanceled()) {
         return false;
      }

      final Double[][] values = new Double[m_Grids.size() * 4][iShapeCount];
      final String[] sNames = new String[m_Grids.size() * 4];
      final Class[] types = new Class[m_Grids.size() * 4];
      int iGrid;
      for (i = 0; i < m_Grids.size() * 4; i += 4) {
         iGrid = i / 4;
         grid = (IRasterLayer) m_Grids.get(iGrid);
         sName = grid.getName();
         sNames[i] = Sextante.getText("AVG") + "_" + sName;
         sNames[i + 1] = Sextante.getText("VAR") + "_" + sName;
         sNames[i + 2] = Sextante.getText("MIN") + "_" + sName;
         sNames[i + 3] = Sextante.getText("MAX") + "_" + sName;
         for (j = 0; j < 4; j++) {
            types[i + j] = Double.class;
         }
         for (j = 0; j < iShapeCount; j++) {
            if (stats[iGrid][j] != null) {
               values[i][j] = new Double(stats[iGrid][j].getMean());
               values[i + 1][j] = new Double(stats[iGrid][j].getVariance());
               values[i + 2][j] = new Double(stats[iGrid][j].getMin());
               values[i + 3][j] = new Double(stats[iGrid][j].getMax());
            }
            else {
               values[i][j] = NODATA;
               values[i + 1][j] = NODATA;
               values[i + 2][j] = NODATA;
               values[i + 3][j] = NODATA;
            }
         }
      }
      final IOutputChannel channel = getOutputChannel(RESULT);
      final OutputVectorLayer out = new OutputVectorLayer();
      out.setName(RESULT);
      out.setOutputChannel(channel);
      out.setDescription(m_Layer.getName());
      out.setOutputObject(ShapesTools.addFields(m_OutputFactory, m_Layer, channel, sNames, values, types));
      addOutputObject(out);

      return !m_Task.isCanceled();

   }


   private SimpleStats doPolygon(final Geometry geom) {

      final SimpleStats stats = new SimpleStats();

      for (int i = 0; i < geom.getNumGeometries(); i++) {
         final Geometry part = geom.getGeometryN(i);
         doPolygonPart(part, stats);
      }

      return stats;

   }


   private void doPolygonPart(final Geometry geom,
                              final SimpleStats stats) {

      boolean bFill;
      boolean bCrossing[];
      int x, y, ix, xStart, xStop, iPoint;
      double yPos;
      double dValue;
      Coordinate pLeft, pRight, pa, pb;
      final Coordinate p = new Coordinate();
      bCrossing = new boolean[m_iNX];

      final Envelope extent = geom.getEnvelopeInternal();

      xStart = (int) ((extent.getMinX() - m_Extent.getXMin()) / m_Extent.getCellSize()) - 1;
      if (xStart < 0) {
         xStart = 0;
      }

      xStop = (int) ((extent.getMaxX() - m_Extent.getXMin()) / m_Extent.getCellSize()) + 1;
      if (xStop >= m_iNX) {
         xStop = m_iNX - 1;
      }

      final Coordinate[] points = geom.getCoordinates();

      for (y = 0, yPos = m_Extent.getYMax(); y < m_iNY; y++, yPos -= m_Extent.getCellSize()) {
         if ((yPos >= extent.getMinY()) && (yPos <= extent.getMaxY())) {
            Arrays.fill(bCrossing, false);
            pLeft = new Coordinate(m_Extent.getXMin() - 1.0, yPos);
            pRight = new Coordinate(m_Extent.getXMax() + 1.0, yPos);

            pb = points[points.length - 1];

            for (iPoint = 0; iPoint < points.length; iPoint++) {
               pa = pb;
               pb = points[iPoint];

               if ((((pa.y <= yPos) && (yPos < pb.y)) || ((pa.y > yPos) && (yPos >= pb.y)))) {
                  getCrossing(p, pa, pb, pLeft, pRight);

                  ix = (int) ((p.x - m_Extent.getXMin()) / m_Extent.getCellSize() + 1.0);

                  if (ix < 0) {
                     ix = 0;
                  }
                  else if (ix >= m_iNX) {
                     ix = m_iNX - 1;
                  }

                  bCrossing[ix] = !bCrossing[ix];
               }
            }

            for (x = xStart, bFill = false; x <= xStop; x++) {
               if (bCrossing[x]) {
                  bFill = !bFill;
               }
               if (bFill) {
                  dValue = m_Window.getCellValueAsDouble(x, y);
                  if (!m_Window.isNoDataValue(dValue)) {
                     stats.addValue(dValue);
                  }
               }
            }
         }
      }

   }


   private boolean getCrossing(final Coordinate crossing,
                               final Coordinate a1,
                               final Coordinate a2,
                               final Coordinate b1,
                               final Coordinate b2) {

      double lambda, div, a_dx, a_dy, b_dx, b_dy;

      a_dx = a2.x - a1.x;
      a_dy = a2.y - a1.y;

      b_dx = b2.x - b1.x;
      b_dy = b2.y - b1.y;

      if ((div = a_dx * b_dy - b_dx * a_dy) != 0.0) {
         lambda = ((b1.x - a1.x) * b_dy - b_dx * (b1.y - a1.y)) / div;

         crossing.x = a1.x + lambda * a_dx;
         crossing.y = a1.y + lambda * a_dy;

         return true;

      }

      return false;
   }

}
