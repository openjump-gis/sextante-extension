package es.unex.sextante.gridTools.clipGrid;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;

public class ClipGridAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT    = "INPUT";
   public static final String POLYGONS = "POLYGONS";
   public static final String RESULT   = "RESULT";

   private AnalysisExtent     m_Extent;
   private int                m_iMinX, m_iMinY;
   private IRasterLayer       m_Output;
   private IRasterLayer       m_Raster;
   private IVectorLayer       m_Polygons;
   private int                m_iNX;
   private int                m_iNY;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Crop_grid_with_polygon_layer"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);
      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer_to_crop"), true);
         m_Parameters.addInputVectorLayer(POLYGONS, Sextante.getText("Polygons"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON,
                  true);
         addOutputRasterLayer(RESULT, Sextante.getText("Cropped_layer"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_Raster = m_Parameters.getParameterValueAsRasterLayer("INPUT");
      m_Polygons = m_Parameters.getParameterValueAsVectorLayer("POLYGONS");

      clip();

      return !m_Task.isCanceled();

   }


   private void clip() throws UnsupportedOutputChannelException, IteratorException {

      int i;
      m_Extent = getAdjustedGridExtent();

      if (m_Extent != null) {
         m_Raster.setWindowExtent(m_Extent);
         m_Output = getNewRasterLayer(RESULT, Sextante.getText("Result"), m_Raster.getDataType(), m_Extent,
                  m_Raster.getBandsCount());
         m_Output.setNoDataValue(m_Raster.getNoDataValue());
         m_Output.assignNoData();

         m_iNX = m_Extent.getNX();
         m_iNY = m_Extent.getNY();

         i = 1;
         final IFeatureIterator iter = m_Polygons.iterator();
         final int iShapeCount = m_Polygons.getShapesCount();
         while (iter.hasNext() && !m_Task.isCanceled()) {
            setProgressText(Integer.toString(i) + "/" + Integer.toString(iShapeCount));
            final IFeature feature = iter.next();
            final Geometry geom = feature.getGeometry();
            doPolygon(geom);
            if (m_Task.isCanceled()) {
               return;
            }
            i++;
         }
         iter.close();

      }

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
      Coordinate pLeft, pRight, pa, pb;
      final Coordinate p = new Coordinate();
      bCrossing = new boolean[m_iNX];
      final int iBands = m_Raster.getBandsCount();

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

      for (y = 0, yPos = m_Extent.getYMax(); (y < m_iNY) && setProgress(y, m_iNY); y++, yPos -= m_Extent.getCellSize()) {
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
                  for (int i = 0; i < iBands; i++) {
                     final double dValue = m_Raster.getCellValueAsDouble(x, y, i);
                     m_Output.setCellValue(x, y, i, dValue);
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


   private AnalysisExtent getAdjustedGridExtent() {

      double iMaxX, iMaxY;
      double dMinX, dMinY;
      double dMinX2, dMinY2, dMaxX2, dMaxY2;
      double dCellSize;
      final AnalysisExtent ge = new AnalysisExtent();

      final Rectangle2D rect = m_Polygons.getFullExtent();
      dMinX = m_Raster.getLayerGridExtent().getXMin();
      dMinY = m_Raster.getLayerGridExtent().getYMin();
      dCellSize = m_Raster.getLayerGridExtent().getCellSize();

      m_iMinX = (int) Math.floor((rect.getMinX() - dMinX) / dCellSize);
      iMaxX = Math.ceil((rect.getMaxX() - dMinX) / dCellSize);
      m_iMinY = (int) Math.floor((rect.getMinY() - dMinY) / dCellSize);
      iMaxY = Math.ceil((rect.getMaxY() - dMinY) / dCellSize);

      dMinX2 = dMinX + m_iMinX * dCellSize;
      dMinY2 = dMinY + m_iMinY * dCellSize;
      dMaxX2 = dMinX + iMaxX * dCellSize;
      dMaxY2 = dMinY + iMaxY * dCellSize;

      ge.setCellSize(dCellSize);
      ge.setXRange(dMinX2, dMaxX2, true);
      ge.setYRange(dMinY2, dMaxY2, true);

      return ge;

   }

}
