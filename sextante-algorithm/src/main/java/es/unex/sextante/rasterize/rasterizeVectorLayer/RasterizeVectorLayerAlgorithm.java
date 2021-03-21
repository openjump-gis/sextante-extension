

package es.unex.sextante.rasterize.rasterizeVectorLayer;

import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.rasterWrappers.GridCell;


public class RasterizeVectorLayerAlgorithm
         extends
            GeoAlgorithm {

   private double             NO_DATA;

   public static final String LAYER  = "LAYER";
   public static final String FIELD  = "FIELD";
   public static final String RESULT = "RESULT";

   private int                m_iField;
   private int                m_iNX, m_iNY;
   private IVectorLayer       m_Layer;
   private IRasterLayer       m_Result;
   private AnalysisExtent     m_Extent;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Rasterize_vector_layer"));
      setGroup(Sextante.getText("Rasterization_and_interpolation"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Vector_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), LAYER);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final UndefinedParentParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final OptionalParentParameterException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShape = 1;
      int iType;
      int iShapeCount;
      double dValue;

      NO_DATA = m_OutputFactory.getDefaultNoDataValue();

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      m_iField = m_Parameters.getParameterValueAsInt(FIELD);

      m_Result = getNewRasterLayer(RESULT, m_Layer.getName() + Sextante.getText("[rasterizedo]"),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
      m_Result.setNoDataValue(NO_DATA);
      m_Result.assignNoData();

      m_Extent = m_Result.getWindowGridExtent();

      m_iNX = m_Extent.getNX();
      m_iNY = m_Extent.getNY();

      final Coordinate[] coords = new Coordinate[5];
      coords[0] = new Coordinate(m_Extent.getXMin(), m_Extent.getYMin());
      coords[1] = new Coordinate(m_Extent.getXMin(), m_Extent.getYMax());
      coords[2] = new Coordinate(m_Extent.getXMax(), m_Extent.getYMax());
      coords[3] = new Coordinate(m_Extent.getXMax(), m_Extent.getYMin());
      coords[4] = new Coordinate(m_Extent.getXMin(), m_Extent.getYMin());
      final GeometryFactory gf = new GeometryFactory();
      final LinearRing ring = gf.createLinearRing(coords);
      final Polygon extent = gf.createPolygon(ring, null);

      i = 0;
      iType = m_Layer.getShapeType();
      iShapeCount = m_Layer.getShapesCount();
      final IFeatureIterator iter = m_Layer.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final IRecord record = feature.getRecord();
         try {
            dValue = Double.parseDouble(record.getValue(m_iField).toString());
            iShape++;
         }
         catch (final Exception e) {
            dValue = iShape;
            iShape++;
         }

         final Geometry geometry = feature.getGeometry();

         if (geometry.intersects(extent)) {
            switch (iType) {
               case IVectorLayer.SHAPE_TYPE_POINT:
                  doPoint(geometry, dValue);
                  break;
               case IVectorLayer.SHAPE_TYPE_LINE:
                  doLine(geometry, dValue);
                  break;
               case IVectorLayer.SHAPE_TYPE_POLYGON:
                  doPolygon(geometry, dValue);
                  break;
            }
         }
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();


   }


   private void doPolygon(final Geometry geom,
                          final double dValue) {

      final GeometryFactory gf = new GeometryFactory();
      for (int i = 0; i < geom.getNumGeometries(); i++) {
         final Polygon poly = (Polygon) geom.getGeometryN(i);
         LinearRing lr = gf.createLinearRing(poly.getExteriorRing().getCoordinates());
         Polygon part = gf.createPolygon(lr, null);
         doPolygonPart(part, dValue, false);
         for (int j = 0; j < poly.getNumInteriorRing(); j++) {
            lr = gf.createLinearRing(poly.getInteriorRingN(j).getCoordinates());
            part = gf.createPolygon(lr, null);
            doPolygonPart(part, dValue, true);
         }
      }

   }


   private void doPolygonPart(final Polygon geom,
                              final double dValue,
                              final boolean bIsHole) {

      boolean bFill;
      boolean bCrossing[];
      int x, y, ix, xStart, xStop, iPoint;
      double yPos;;
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
                  final double dPrevValue = m_Result.getCellValueAsDouble(x, y);
                  if (bIsHole) {
                     if (dPrevValue == dValue) {
                        m_Result.setNoData(x, y);
                     }
                  }
                  else {
                     if (dPrevValue == NO_DATA) {
                        m_Result.setCellValue(x, y, dValue);
                     }
                  }
               }
            }
         }
      }

   }


   private void doLine(final Geometry geom,
                       final double dValue) {

      for (int i = 0; i < geom.getNumGeometries(); i++) {
         final Geometry part = geom.getGeometryN(i);
         doLineString(part, dValue);
      }

   }


   private void doLineString(final Geometry geom,
                             final double dValue) {

      int i;
      double x, y, x2, y2;
      final Coordinate[] coords = geom.getCoordinates();
      for (i = 0; i < coords.length - 1; i++) {
         x = coords[i].x;
         y = coords[i].y;
         x2 = coords[i + 1].x;
         y2 = coords[i + 1].y;
         writeSegment(x, y, x2, y2, dValue);
      }

   }


   private void writeSegment(double x,
                             double y,
                             final double x2,
                             final double y2,
                             final double dValue) {

      double dx, dy, d, n;
      GridCell cell;

      dx = Math.abs(x2 - x);
      dy = Math.abs(y2 - y);

      if ((dx > 0.0) || (dy > 0.0)) {
         if (dx > dy) {
            dx /= m_Result.getWindowCellSize();
            n = dx;
            dy /= dx;
            dx = m_Result.getWindowCellSize();
         }
         else {
            dy /= m_Result.getWindowCellSize();
            n = dy;
            dx /= dy;
            dy = m_Result.getWindowCellSize();
         }

         if (x2 < x) {
            dx = -dx;
         }

         if (y2 < y) {
            dy = -dy;
         }

         for (d = 0.0; d <= n; d++, x += dx, y += dy) {
            if (m_Extent.contains(x, y)) {
               cell = m_Extent.getGridCoordsFromWorldCoords(x, y);
               //System.out.println(cell.getX() + " " + cell.getY());
               m_Result.setCellValue(cell.getX(), cell.getY(), dValue);
            }
         }
      }

   }


   private void doPoint(final Geometry geometry,
                        final double dValue) {

      final Coordinate coord = geometry.getCoordinate();
      final GridCell cell = m_Extent.getGridCoordsFromWorldCoords(coord.x, coord.y);
      m_Result.setCellValue(cell.getX(), cell.getY(), dValue);

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
