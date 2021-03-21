package es.unex.sextante.gridTools.clipBBoxGrid;

import java.awt.geom.Rectangle2D;

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

public class clipBBoxGridAlgorithm
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

      setName(Sextante.getText("Clip_grid_with_bbox_of_polygons"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);
      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer_to_clip"), true);
         m_Parameters.addInputVectorLayer(POLYGONS, Sextante.getText("Polygons"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON,
                  true);
         addOutputRasterLayer(RESULT, Sextante.getText("Clipped_layer"));
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

         i = 0;
         final IFeatureIterator iter = m_Polygons.iterator();
         final int iShapeCount = m_Polygons.getShapesCount();
         while (iter.hasNext() && setProgress(i, iShapeCount)) {
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

      int x, y, xStart, xStop;

      final Envelope extent = geom.getEnvelopeInternal();

      xStart = (int) ((extent.getMinX() - m_Extent.getXMin()) / m_Extent.getCellSize()) - 1;
      if (xStart < 0) {
         xStart = 0;
      }

      xStop = (int) ((extent.getMaxX() - m_Extent.getXMin()) / m_Extent.getCellSize()) + 1;
      if (xStop >= m_iNX) {
         xStop = m_iNX - 1;
      }

      for (y = 0; y < m_iNY; y++) {
         for (x = xStart; x <= xStop; x++) {
            for (int i = 0; i < m_Raster.getBandsCount(); i++) {
               m_Output.setCellValue(x, y, i, m_Raster.getCellValueAsDouble(x, y, i));
            }

         }
      }

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
