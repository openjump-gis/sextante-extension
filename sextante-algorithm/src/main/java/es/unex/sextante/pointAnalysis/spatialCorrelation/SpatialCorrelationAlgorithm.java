

package es.unex.sextante.pointAnalysis.spatialCorrelation;

import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;


public class SpatialCorrelationAlgorithm
         extends
            GeoAlgorithm {

   public static final String CLOUD      = "CLOUD";
   public static final String RESULT     = "RESULT";
   public static final String INTERVAL   = "INTERVAL";
   public static final String POINTS     = "POINTS";
   public static final String FIELD      = "FIELD";
   private IVectorLayer       m_Layer;
   private int                m_iField;
   private double             m_dMaxDist = 0;
   private double             m_dInterval;
   private double             m_dMean;
   private double             m_dFieldValue[];
   private double             m_dMoran[];
   private double             m_dGeary[];
   private double             m_dSemivar[];


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i, j;
      double x1, x2, y1, y2;
      double dDifX, dDifY;
      double dValue;
      int iCount;

      m_dMean = 0;

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      if (!m_bIsAutoExtent) {
         m_Layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      m_dInterval = m_Parameters.getParameterValueAsDouble(INTERVAL);
      m_iField = m_Parameters.getParameterValueAsInt(FIELD);
      iCount = m_Layer.getShapesCount();
      if (iCount == 0) {
         throw new GeoAlgorithmExecutionException("0 points in layer");
      }
      final double[][] d = new double[iCount][iCount];
      m_dFieldValue = new double[iCount];
      i = 0;
      final IFeatureIterator iter = m_Layer.iterator();
      while (iter.hasNext() && setProgress(i, iCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate coord = geom.getCoordinate();
         x1 = coord.x;
         y1 = coord.y;
         try {
            dValue = Double.parseDouble(feature.getRecord().getValue(m_iField).toString());
            m_dMean += dValue;
            m_dFieldValue[i] = dValue;
         }
         catch (final NumberFormatException e) {
            throw new GeoAlgorithmExecutionException(Sextante.getText("ERROR_Spatial_autocorrelation_Invalid_value_in_table"));
         }
         j = 0;
         final IFeatureIterator iter2 = m_Layer.iterator();
         while (iter2.hasNext()) {
            final IFeature feature2 = iter2.next();
            final Geometry geom2 = feature2.getGeometry();
            final Coordinate coord2 = geom2.getCoordinate();
            x2 = coord2.x;
            y2 = coord2.y;
            dDifX = x2 - x1;
            dDifY = y2 - y1;
            d[i][j] = Math.sqrt(dDifX * dDifX + dDifY * dDifY);
            m_dMaxDist = Math.max(m_dMaxDist, d[i][j]);
            j++;
         }
         iter2.close();
         i++;
      }
      iter.close();

      m_dMean /= iCount;

      if (calculate(d)) {
         createTables();
      }

      return !m_Task.isCanceled();

   }


   private void createTables() throws UnsupportedOutputChannelException {

      int i;
      final int iClasses = (int) (m_dMaxDist / m_dInterval + 2);
      final String[] sFields = { Sextante.getText("Distance"), Sextante.getText("Moran_I"), Sextante.getText("Geary_c"),
               Sextante.getText("Semivariance") };
      final Class[] types = { Double.class, Double.class, Double.class, Double.class };
      final String sTableName = Sextante.getText("Spatial_autocorrelation_[") + m_Layer.getName() + "]";
      final Object[] values = new Object[4];
      final ITable driver = getNewTable(RESULT, sTableName, types, sFields);

      for (i = 0; i < iClasses; i++) {
         values[0] = new Double(m_dInterval * i);
         values[1] = new Double(m_dMoran[i]);
         values[2] = new Double(m_dGeary[i]);
         values[3] = new Double(m_dSemivar[i]);
         driver.addRecord(values);
      }

   }


   private boolean calculate(final double[][] dist) throws UnsupportedOutputChannelException {

      int i, j;
      int iClasses;
      int iClass;
      int[] iPointsInClass;
      double[] dDen;
      double dSemivar;
      boolean bIsInClass[];
      Object[] values = null;
      ITable driver = null;

      final String[] sFields = { Sextante.getText("Distance"), Sextante.getText("Semivariance") };
      final Class[] types = { Double.class, Double.class };
      final String sTableName = Sextante.getText("Variogram_cloud_[") + m_Layer.getName() + "]";
      values = new Object[2];
      driver = getNewTable(CLOUD, sTableName, types, sFields);

      iClasses = (int) (m_dMaxDist / m_dInterval + 2);
      m_dMoran = new double[iClasses];
      m_dGeary = new double[iClasses];
      dDen = new double[iClasses];
      m_dSemivar = new double[iClasses];
      iPointsInClass = new int[iClasses];
      bIsInClass = new boolean[iClasses];

      for (i = 0; (i < dist.length) && setProgress(i, dist.length); i++) {
         Arrays.fill(bIsInClass, false);
         for (j = 0; j < dist.length; j++) {
            iClass = (int) Math.floor((dist[i][j] + m_dInterval / 2.) / m_dInterval);
            iPointsInClass[iClass]++;
            dSemivar = Math.pow((m_dFieldValue[i] - m_dFieldValue[j]), 2.);
            values[0] = new Double(dist[i][j]);
            values[1] = new Double(dSemivar / 2.);
            driver.addRecord(values);
            m_dSemivar[iClass] += dSemivar;
            m_dMoran[iClass] += (m_dFieldValue[i] - m_dMean) * (m_dFieldValue[j] - m_dMean);
            m_dGeary[iClass] = m_dSemivar[iClass];
            bIsInClass[iClass] = true;
         }
         for (j = 0; j < iClasses; j++) {
            if (bIsInClass[j]) {
               dDen[j] += Math.pow(m_dFieldValue[i] - m_dMean, 2.);
            }
         }
      }

      for (i = 0; i < iClasses; i++) {
         if (dDen[i] != 0) {
            m_dMoran[i] /= dDen[i];
            m_dGeary[i] *= ((iPointsInClass[i] - 1) / (2. * iPointsInClass[i] * dDen[i]));
            m_dSemivar[i] /= (2. * iPointsInClass[i]);
         }
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Spatial_autocorrelation"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);
      try {
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), POINTS);
         m_Parameters.addNumericalValue(INTERVAL, Sextante.getText("Distance_interval"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 100, 0, Double.MAX_VALUE);
         addOutputTable(RESULT, Sextante.getText("Spatial_autocorrelation"));
         addOutputTable(CLOUD, Sextante.getText("Nube_del_variograma"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final UndefinedParentParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final OptionalParentParameterException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
