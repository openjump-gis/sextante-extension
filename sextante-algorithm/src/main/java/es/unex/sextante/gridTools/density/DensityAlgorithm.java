package es.unex.sextante.gridTools.density;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.rasterWrappers.GridCell;

public class DensityAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER    = "LAYER";
   public static final String FIELD    = "FIELD";
   public static final String DISTANCE = "DISTANCE";
   public static final String DENSITY  = "DENSITY";

   private int                m_iField;
   private IVectorLayer       m_Layer;
   private IRasterLayer       m_Result;
   private AnalysisExtent         m_Extent;
   private int                m_iDistance;
   private boolean            m_bIsValidCell[][];


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Density"));
      setGroup(Sextante.getText("Rasterization_and_interpolation"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Vector_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), "LAYER");
         m_Parameters.addNumericalValue(DISTANCE, Sextante.getText("Search_radius"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 100, 0, Double.MAX_VALUE);
         addOutputRasterLayer(DENSITY, Sextante.getText("Density"));
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
      int x, y;
      int iShapeCount;
      double dValue;
      double dXMin, dYMin;
      double dXMax, dYMax;
      double dDistance;

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      m_iField = m_Parameters.getParameterValueAsInt(FIELD);
      dDistance = m_Parameters.getParameterValueAsDouble(DISTANCE);

      m_Result = getNewRasterLayer(DENSITY, m_Layer.getName() + Sextante.getText("[density]"),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
      m_Result.assign(0.0);

      m_Extent = m_Result.getWindowGridExtent();

      m_iDistance = (int) Math.floor(dDistance / m_Extent.getCellSize());

      dXMin = m_Extent.getXMin() - dDistance;
      dYMin = m_Extent.getYMin() - dDistance;
      dXMax = m_Extent.getXMax() + dDistance;
      dYMax = m_Extent.getYMax() + dDistance;

      m_bIsValidCell = new boolean[2 * m_iDistance + 1][2 * m_iDistance + 1];

      for (y = -m_iDistance; y < m_iDistance + 1; y++) {
         for (x = -m_iDistance; x < m_iDistance + 1; x++) {
            final double dDist = Math.sqrt(x * x + y * y);
            if (dDist < m_iDistance) {
               m_bIsValidCell[x + m_iDistance][y + m_iDistance] = true;
            }
            else {
               m_bIsValidCell[x + m_iDistance][y + m_iDistance] = false;
            }
         }
      }

      i = 0;
      iShapeCount = m_Layer.getShapesCount();
      final IFeatureIterator iter = m_Layer.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate coord = geom.getCoordinate();

         try {
            dValue = Double.parseDouble(feature.getRecord().getValue(m_iField).toString());
         }
         catch (final Exception e) {
            dValue = 1.0;
         }

         if ((coord.x > dXMin) && (coord.x < dXMax) && (coord.y > dYMin) && (coord.y < dYMax)) {
            doPoint(coord, dValue);
         }
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   private void doPoint(final Coordinate coord,
                        final double dValue) {

      int x, y;
      int iX, iY;
      GridCell cell;

      cell = m_Extent.getGridCoordsFromWorldCoords(coord.x, coord.y);
      iX = cell.getX();
      iY = cell.getY();

      for (y = -m_iDistance; y < m_iDistance + 1; y++) {
         for (x = -m_iDistance; x < m_iDistance + 1; x++) {
            if (m_bIsValidCell[x + m_iDistance][y + m_iDistance]) {
               m_Result.addToCellValue(iX + x, iY + y, dValue);
            }
         }
      }
   }

}
