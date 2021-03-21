

package es.unex.sextante.pointAnalysis.distanceMatrix;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public class DistanceMatrixAlgorithm
         extends
            GeoAlgorithm {

   public static final String POINTS = "POINTS";
   public static final String RESULT = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i = 0, j = 0;
      double x1, x2, y1, y2;
      double dDifX, dDifY;
      int iCount;
      String[] sFields;
      Class[] types;
      Object[] values;

      final IVectorLayer layer = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      if (!m_bIsAutoExtent) {
         layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      iCount = layer.getShapesCount();
      final double[][] d = new double[iCount][iCount];
      sFields = new String[iCount + 1];
      types = new Class[iCount + 1];
      values = new Object[iCount + 1];
      sFields[0] = "ID";
      types[0] = Integer.class;
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(i, iCount)) {
         IFeature feature = iter.next();
         sFields[i + 1] = Integer.toString(i + 1);
         types[i + 1] = Double.class;
         Geometry geom = feature.getGeometry();
         Coordinate coord = geom.getCoordinate();
         x1 = coord.x;
         y1 = coord.y;
         final IFeatureIterator iter2 = layer.iterator();
         j = 0;
         while (iter2.hasNext()) {
            feature = iter2.next();
            geom = feature.getGeometry();
            coord = geom.getCoordinate();
            x2 = coord.x;
            y2 = coord.y;
            dDifX = x2 - x1;
            dDifY = y2 - y1;
            d[i][j] = Math.sqrt(dDifX * dDifX + dDifY * dDifY);
            j++;
         }
         iter2.close();
         i++;
      }
      iter.close();

      final String sTableName = Sextante.getText("Distance_matrix_[") + layer.getName() + "]";

      final ITable driver = getNewTable(RESULT, sTableName, types, sFields);

      for (i = 0; (i < iCount) && setProgress(i, iCount); i++) {
         values[0] = new Integer(i + 1);
         for (j = 0; j < iCount; j++) {
            final double dDist = d[i][j];
            values[j + 1] = new Double(dDist);
         }
         driver.addRecord(values);

      }

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Distance_matrix"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
         addOutputTable(RESULT, Sextante.getText("Distance_matrix"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
