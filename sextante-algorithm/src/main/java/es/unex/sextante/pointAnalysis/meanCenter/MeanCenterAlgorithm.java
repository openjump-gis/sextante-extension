

package es.unex.sextante.pointAnalysis.meanCenter;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.shapesTools.ShapesTools;


public class MeanCenterAlgorithm
         extends
            GeoAlgorithm {

   private static final int    METHOD_WEIGHTED     = 1;
   private static final int    METHOD_NOT_WEIGHTED = 0;

   private static final String POINTS              = "POINTS";
   private static final String FIELD               = "FIELDS";
   private static final String METHOD              = "METHOD";
   private static final String RESULT              = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iField;
      int iMethod;
      double x, y;
      double dSumX = 0, dSumY = 0;
      double dWeight, dSumWeight = 0;
      double xCenter, yCenter;
      double dDifX, dDifY, dDifDist = 0;
      int iCount;

      final IVectorLayer layer = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      if (!m_bIsAutoExtent) {
         layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      iField = m_Parameters.getParameterValueAsInt(FIELD);
      iCount = layer.getShapesCount();
      if (iCount == 0) {
         throw new GeoAlgorithmExecutionException("0 points in layer");
      }

      i = 0;
      IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(i, iCount * 2)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate coord = geom.getCoordinate();
         x = coord.x;
         y = coord.y;
         if (iMethod == METHOD_WEIGHTED) {
            try {
               dWeight = Double.parseDouble(feature.getRecord().getValue(iField).toString());
            }
            catch (final Exception e) {
               dWeight = 1;
            }
         }
         else {
            dWeight = 1;
         }
         dSumWeight += dWeight;
         dSumX += (dWeight * x);
         dSumY += (dWeight * y);
         i++;
      }
      iter.close();

      xCenter = dSumX / dSumWeight;
      yCenter = dSumY / dSumWeight;
      dSumWeight = 0;

      iter = layer.iterator();
      while (iter.hasNext() && setProgress(i, iCount * 2)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate coord = geom.getCoordinate();
         x = coord.x;
         y = coord.y;
         if (iMethod == METHOD_WEIGHTED) {
            try {
               dWeight = Double.parseDouble(feature.getRecord().getValue(iField).toString());
            }
            catch (final Exception e) {
               dWeight = 1;
            }
         }
         else {
            dWeight = 1;
         }
         dSumWeight += dWeight;
         dDifX = (x - xCenter);
         dDifY = (y - yCenter);
         dDifDist += ((dDifX * dDifX * dWeight) + (dDifY * dDifY * dWeight));
         i++;
      }
      iter.close();

      final double dStdDist = Math.sqrt(dDifDist / dSumWeight);
      final IVectorLayer output = getNewVectorLayer(RESULT, Sextante.getText("Standard_distance"),
               IVectorLayer.SHAPE_TYPE_POLYGON, new Class[] { Double.class },
               new String[] { Sextante.getText("Radius__standard_deviation") });
      final Object[] value = new Object[1];
      value[0] = new Double(dStdDist);

      final Geometry circle = ShapesTools.createCircle(xCenter, yCenter, dStdDist);
      output.addFeature(circle, value);

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      final String sOptions[] = { Sextante.getText("Mean_center"), Sextante.getText("Weighted_mean_center") };

      setName(Sextante.getText("Mean_center_and_standard_distance"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);
      try {
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Weights"), "POINTS");
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sOptions);
         addOutputVectorLayer(RESULT, Sextante.getText("Mean_center_and_standard_distance"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
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
