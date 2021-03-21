

package es.unex.sextante.gridTools.locateExtremeValues;

import java.awt.geom.Point2D;
import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

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
import es.unex.sextante.outputs.OutputVectorLayer;


public class LocateExtremeValuesAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT             = "RESULT";
   public static final String GRID               = "GRID";
   public static final String POLYGONS           = "POLYGONS";
   public static final String EXTREME_VALUE_TYPE = "EXTREME_VALUE_TYPE";

   private int                m_iNX, m_iNY;
   private IVectorLayer       m_Layer;
   private AnalysisExtent     m_Extent;
   private IRasterLayer       m_Window;
   private Coordinate         m_Coordinate;
   private double             m_dExtremeValue;
   private IVectorLayer       m_Output;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Locate_max_values"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(POLYGONS, Sextante.getText("Polygons"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON,
                  true);
         m_Parameters.addInputRasterLayer(GRID, Sextante.getText("Grid"), true);
         addOutputVectorLayer(RESULT, Sextante.getText("Max_values"), OutputVectorLayer.SHAPE_TYPE_POINT);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i = 0;
      int iShapeCount;
      final GeometryFactory gf = new GeometryFactory();

      m_Coordinate = new Coordinate();
      m_Layer = m_Parameters.getParameterValueAsVectorLayer(POLYGONS);
      m_Window = m_Parameters.getParameterValueAsRasterLayer(GRID);

      final String[] sNames = { m_Window.getName() };
      final Class[] types = { Double.class };

      m_Output = getNewVectorLayer(RESULT, Sextante.getText("Result"), IVectorLayer.SHAPE_TYPE_POINT, types, sNames);

      if (!m_bIsAutoExtent) {
         m_Layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      iShapeCount = m_Layer.getShapesCount();

      m_Window.setFullExtent();
      m_Extent = m_Window.getWindowGridExtent();
      final Coordinate[] coords = new Coordinate[5];
      coords[0] = new Coordinate(m_Extent.getXMin(), m_Extent.getYMin());
      coords[1] = new Coordinate(m_Extent.getXMin(), m_Extent.getYMax());
      coords[2] = new Coordinate(m_Extent.getYMax(), m_Extent.getYMax());
      coords[3] = new Coordinate(m_Extent.getXMax(), m_Extent.getYMin());
      coords[4] = new Coordinate(m_Extent.getXMin(), m_Extent.getYMin());

      final LinearRing ring = gf.createLinearRing(coords);
      final Polygon extent = gf.createPolygon(ring, null);
      m_iNX = m_Window.getNX();
      m_iNY = m_Window.getNY();
      final IFeatureIterator iter = m_Layer.iterator();
      i = 0;
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         if (geom.intersects(extent)) {
            m_dExtremeValue = Double.NEGATIVE_INFINITY;
            doPolygon(geom);
            final Point pt = gf.createPoint(new Coordinate(m_Coordinate.x, m_Coordinate.y));
            m_Output.addFeature(pt, new Object[] { new Double(m_dExtremeValue) });
         }
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   private void doPolygon(final Geometry geom) {

      for (int i = 0; i < geom.getNumGeometries(); i++) {
         final Geometry part = geom.getGeometryN(i);
         doPolygonPart(part);
      }


   }


   private void doPolygonPart(final Geometry geom) {

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
                     if (dValue > m_dExtremeValue) {
                        m_dExtremeValue = dValue;
                        final Point2D coord = m_Extent.getWorldCoordsFromGridCoords(x, y);
                        m_Coordinate.x = coord.getX();
                        m_Coordinate.y = coord.getY();
                     }
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
