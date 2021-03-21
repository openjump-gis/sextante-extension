

package es.unex.sextante.rasterize.interpolationBase;

import java.awt.geom.Point2D;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.closestpts.NearestNeighbourFinder;
import es.unex.sextante.closestpts.PtAndDistance;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.rasterWrappers.GridCell;


public abstract class BaseInterpolationAlgorithm
         extends
            GeoAlgorithm {

   protected double                 NO_DATA;

   public static final String       LAYER  = "LAYER";
   public static final String       DIST   = "DIST";
   public static final String       FIELD  = "FIELD";
   public static final String       RESULT = "RESULT";

   protected double                 m_dDistance;
   protected int                    m_iField;
   protected NearestNeighbourFinder m_SearchEngine;
   protected PtAndDistance[]        m_NearestPoints;

   protected IVectorLayer           m_Layer;


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Point_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), LAYER);
         m_Parameters.addNumericalValue(DIST, Sextante.getText("Search_radius"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 100, 0, Double.MAX_VALUE);
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

      int x, y;
      int iNX, iNY;

      NO_DATA = m_OutputFactory.getDefaultNoDataValue();

      setValues();

      m_SearchEngine = new NearestNeighbourFinder(m_Layer, m_iField, m_Task);
      final IRasterLayer result = getNewRasterLayer(RESULT, m_Layer.getName() + Sextante.getText("[interpolated]"),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      iNX = m_AnalysisExtent.getNX();
      iNY = m_AnalysisExtent.getNY();

      setProgressText(Sextante.getText("Interpolating"));
      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            result.setCellValue(x, y, getValueAt(x, y));
         }
      }

      createCrossValidationTable();

      return !m_Task.isCanceled();

   }


   protected void createCrossValidationTable() throws UnsupportedOutputChannelException {

      int i;
      int iPoints;
      double x, y, z;
      double dValue;
      final Object[] values = new Object[5];
      final String sFields[] = { "X", "Y", Sextante.getText("Real_value"), Sextante.getText("Estimated_value"),
               Sextante.getText("Diference") };
      final Class types[] = { Double.class, Double.class, Double.class, Double.class, Double.class };
      final String sTableName = Sextante.getText("Cross_validation_[") + m_Layer.getName() + "]";
      final ITable table = getNewTable("CROSSVALIDATION", sTableName, types, sFields);

      try {
         setProgressText(Sextante.getText("Creating_cross_validation"));
         iPoints = m_Layer.getShapesCount();
         final IFeatureIterator iter = m_Layer.iterator();
         i = 0;
         while (iter.hasNext() && setProgress(i, iPoints)) {
            final IFeature feature = iter.next();
            final Geometry geom = feature.getGeometry();
            final Coordinate coord = geom.getCoordinate();
            x = coord.x;
            y = coord.y;
            values[0] = new Double(x);
            values[1] = new Double(y);
            try {
               z = Double.parseDouble(feature.getRecord().getValue(m_iField).toString());
            }
            catch (final NumberFormatException e) {
               z = 0;
            }
            values[2] = new Double(z);
            dValue = getValueAt(x, y);
            if (dValue != NO_DATA) {
               values[3] = new Double(dValue);
               values[4] = new Double(dValue - z);
               table.addRecord(values);
            }
            i++;
         }
         iter.close();
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
         return;
      }

   }


   protected void setValues() throws GeoAlgorithmExecutionException {

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      m_dDistance = m_Parameters.getParameterValueAsDouble(DIST);
      if (m_dDistance == 0) {
         m_dDistance = Double.MAX_VALUE;
      }
      m_iField = m_Parameters.getParameterValueAsInt(FIELD);

   }


   protected double getValueAt(final int x,
                               final int y) {

      final Point2D pt = m_AnalysisExtent.getWorldCoordsFromGridCoords(new GridCell(x, y, 0));
      m_NearestPoints = m_SearchEngine.getClosestPoints(pt.getX(), pt.getY(), m_dDistance);

      final double dValue = interpolate(pt.getX(), pt.getY());

      return dValue;

   }


   protected double getValueAt(final double x,
                               final double y) {

      try {
         final PtAndDistance[] nearestPoints = m_SearchEngine.getClosestPoints(x, y, m_dDistance);

         m_NearestPoints = new PtAndDistance[nearestPoints.length - 1];
         int iIndex = 0;
         for (final PtAndDistance element : nearestPoints) {
            try {
               if (element.getDist() != 0) {
                  m_NearestPoints[iIndex] = element;
                  iIndex++;
               }
            }
            catch (final Exception e) {
            }
         }
         return interpolate(x, y);
      }
      catch (final Exception e) {
         return NO_DATA;
      }

   }


   protected abstract double interpolate(double x,
                                         double y);


}
