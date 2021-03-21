

package es.unex.sextante.vectorTools.snapPoints;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Point;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;


public class SnapPointsAlgorithm
         extends
            GeoAlgorithm {

   public static final String     SNAPTO    = "SNAPTO";
   public static final String     LAYER     = "LAYER";
   public static final String     TOLERANCE = "TOLERANCE";
   public static final String     RESULT    = "RESULT";

   private IVectorLayer           m_Layer;
   private IVectorLayer           m_SnapTo;
   private IVectorLayer           m_Output;
   private double                 m_dTolerance;
   private NearestNeighbourFinder m_NNF;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Snap_points_to_layer"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Points_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);
         m_Parameters.addInputVectorLayer(SNAPTO, Sextante.getText("Capa_ajuste"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY,
                  true);
         m_Parameters.addNumericalValue(TOLERANCE, Sextante.getText("Tolerance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 10, 0, Double.MAX_VALUE);
         addOutputVectorLayer(RESULT, Sextante.getText("Snapped_points"), OutputVectorLayer.SHAPE_TYPE_POINT);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      m_SnapTo = m_Parameters.getParameterValueAsVectorLayer(SNAPTO);
      m_dTolerance = m_Parameters.getParameterValueAsDouble(TOLERANCE);

      if (!m_bIsAutoExtent) {
         m_Layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         m_SnapTo.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      m_NNF = new NearestNeighbourFinder(m_SnapTo, m_Task);

      m_Output = getNewVectorLayer(RESULT, Sextante.getText("Snapped_points"), m_Layer.getShapeType(), m_Layer.getFieldTypes(),
               m_Layer.getFieldNames());

      i = 0;
      iShapeCount = m_Layer.getShapesCount();
      final IFeatureIterator iter = m_Layer.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate coord = geom.getCoordinate();
         addPoint(coord.x, coord.y, feature.getRecord().getValues());
         i++;
      }

      iter.close();

      return !m_Task.isCanceled();

   }


   private void addPoint(final double x,
                         final double y,
                         final Object[] values) {

      final Point pt = getSnappedPoint(x, y);
      m_Output.addFeature(pt, values);

   }


   private Point getSnappedPoint(final double x,
                                 final double y) {

      final GeometryFactory gf = new GeometryFactory();
      final Coordinate c = new Coordinate(x, y);
      Coordinate closestPoint = new Coordinate(x, y);
      Coordinate closestPointToThisLine;
      double minDist = m_dTolerance;

      try {
         final List<Geometry> list = m_NNF.getClosestGeometries(c, m_dTolerance);
         for (int iGeom = 0; iGeom < list.size(); iGeom++) {
            final Geometry geom = list.get(iGeom);
            final int iNumGeom = geom.getNumGeometries();
            for (int i = 0; i < iNumGeom; i++) {
               final Geometry subGeom = geom.getGeometryN(i);
               final Coordinate[] coords = subGeom.getCoordinates();
	       if (coords.length == 1){
		   //When snapping to point layer
		   final double dist = c.distance(coords[0]);
		   if ((dist < minDist)) {
		       minDist = dist;
		       closestPoint = coords[0];
		   }
	       } else {
		   // When snapping to linestring or polygon layer
		   for (int j = 0; j < coords.length - 1; j++) {
		       final LineSegment line = new LineSegment(coords[j], coords[j + 1]);
		       closestPointToThisLine = line.closestPoint(c);
		       final double dist = c.distance(closestPointToThisLine);
		       if ((dist < minDist)) {
			   minDist = dist;
			   closestPoint = closestPointToThisLine;
		       }
		   }
	       }
            }
         }
      } catch (final Exception e) {
	  // will return closest point so far
      }

      return gf.createPoint(closestPoint);

   }

}
