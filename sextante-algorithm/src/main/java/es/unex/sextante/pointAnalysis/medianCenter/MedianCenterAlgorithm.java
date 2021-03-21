

package es.unex.sextante.pointAnalysis.medianCenter;

import java.awt.geom.Rectangle2D;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

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


public class MedianCenterAlgorithm
         extends
            GeoAlgorithm {

   public static final int    METHOD_NOT_WEIGHTED = 0;
   public static final int    METHOD_WEIGHTED     = 1;

   public static final String METHOD              = "METHOD";
   public static final String FIELD               = "FIELD";
   public static final String POINTS              = "POINTS";
   public static final String RESULT              = "RESULT";

   private static final int   ITERATIONS          = 100;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i, j;
      int iField;
      int iMethod;
      double dSumX = 0, dSumY = 0;
      double dX[], dY[], dWeight[];
      double dDist;
      double dDistWeight;
      double dSumWeight = 0;
      double xCenter, yCenter;
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

      dX = new double[iCount];
      dY = new double[iCount];
      dWeight = new double[iCount];
      i = 0;
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(i, iCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate coord = geom.getCoordinate();
         dX[i] = coord.x;
         dY[i] = coord.y;
         if (iMethod == METHOD_WEIGHTED) {
            try {
               dWeight[i] = Double.parseDouble(feature.getRecord().getValue(iField).toString());
            }
            catch (final Exception e) {
               dWeight[i] = 1;
            }
         }
         else {
            dWeight[i] = 1;
         }
         dSumWeight += dWeight[i];
         dSumX += (dWeight[i] * dX[i]);
         dSumY += (dWeight[i] * dY[i]);
         i++;
      }
      iter.close();

      xCenter = dSumX / dSumWeight;
      yCenter = dSumY / dSumWeight;

      for (j = 0; j < ITERATIONS; j++) {
         dSumX = 0;
         dSumY = 0;
         dSumWeight = 0;
         setProgressText(Sextante.getText("Iteration") + Integer.toString(j));
         for (i = 0; (i < iCount) && setProgress(i, iCount); i++) {
            dDist = Math.sqrt(Math.pow(dX[i] - xCenter, 2) + Math.pow(dY[i] - yCenter, 2));
            dDistWeight = dWeight[i] / dDist;
            dSumWeight += dDistWeight;
            dSumX += (dDistWeight * dX[i]);
            dSumY += (dDistWeight * dY[i]);
         }
         xCenter = dSumX / dSumWeight;
         yCenter = dSumY / dSumWeight;
      }

      final GeometryFactory gf = new GeometryFactory();
      final IVectorLayer output = getNewVectorLayer(RESULT, Sextante.getText("Median_center"), IVectorLayer.SHAPE_TYPE_LINE,
               new Class[] { Double.class }, new String[] { Sextante.getText("Coord") });
      final Coordinate coords[] = new Coordinate[2];
      final Rectangle2D rect = layer.getFullExtent();
      final Object[] value = new Object[1];
      value[0] = new Double(xCenter);
      coords[0] = new Coordinate(xCenter, rect.getMinY());
      coords[1] = new Coordinate(xCenter, rect.getMaxY());
      final LineString line = gf.createLineString(coords);
      output.addFeature(line, value);
      value[0] = new Double(yCenter);
      final Coordinate coords2[] = new Coordinate[2];
      coords2[0] = new Coordinate(rect.getMinX(), yCenter);
      coords2[1] = new Coordinate(rect.getMaxX(), yCenter);
      final LineString line2 = gf.createLineString(coords2);
      output.addFeature(line2, value);

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      final String sOptions[] = { Sextante.getText("Median_center"), Sextante.getText("Weighted_median_center") };

      setName(Sextante.getText("Median_center"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Weights"), POINTS);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sOptions);
         addOutputVectorLayer(RESULT, Sextante.getText("Median_center"), OutputVectorLayer.SHAPE_TYPE_LINE);
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
