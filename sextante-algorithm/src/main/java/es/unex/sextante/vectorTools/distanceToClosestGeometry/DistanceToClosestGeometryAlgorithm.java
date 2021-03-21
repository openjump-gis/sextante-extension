

package es.unex.sextante.vectorTools.distanceToClosestGeometry;

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


public class DistanceToClosestGeometryAlgorithm
         extends
            GeoAlgorithm {

   public static final String DISTTO    = "SNAPTO";
   public static final String LAYER     = "LAYER";
   public static final String TOLERANCE = "TOLERANCE";
   public static final String RESULT    = "RESULT";
   public static final String FIELD     = "FIELD";

   private IVectorLayer       m_Points;
   private IVectorLayer       m_Geometries;
   private IVectorLayer       m_Output;
   //private double                 m_dTolerance;
   private SextanteRTree      m_NNF;
   private int                m_iField;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Distance_to_closest_geometry"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Points_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addInputVectorLayer(DISTTO, Sextante.getText("Lines_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), DISTTO);
         /*m_Parameters.addNumericalValue(TOLERANCE, Sextante.getText("Tolerance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 10, 0, Double.MAX_VALUE);*/
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POINT, LAYER);
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


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;

      m_Points = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      m_Geometries = m_Parameters.getParameterValueAsVectorLayer(DISTTO);
      //m_dTolerance = m_Parameters.getParameterValueAsDouble(TOLERANCE);
      m_iField = m_Parameters.getParameterValueAsInt(FIELD);
      if (!m_bIsAutoExtent) {
         m_Points.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         m_Geometries.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      m_NNF = new SextanteRTree(m_Geometries, m_Task);

      final Class[] types = new Class[m_Points.getFieldCount() + 2];
      final String[] sFields = new String[m_Points.getFieldCount() + 2];
      for (int j = 0; j < sFields.length - 2; j++) {
         types[j] = m_Points.getFieldTypes()[j];
         sFields[j] = m_Points.getFieldName(j);
      }
      sFields[sFields.length - 2] = "ID";
      sFields[sFields.length - 1] = "DIST";
      types[types.length - 2] = String.class;
      types[types.length - 1] = Double.class;

      m_Output = getNewVectorLayer(RESULT, Sextante.getText("Distance_to_closest_geometry"), IVectorLayer.SHAPE_TYPE_POINT,
               types, sFields);

      i = 0;
      iShapeCount = m_Points.getShapesCount();
      final IFeatureIterator iter = m_Points.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         processPoint(feature);
         i++;
      }

      iter.close();

      return !m_Task.isCanceled();

   }


   private void processPoint(final IFeature feature) {

      final Geometry geom = feature.getGeometry();
      final Coordinate coord = geom.getCoordinate();
      final IFeature closest = m_NNF.getClosestFeature(coord.x, coord.y);
      final Object[] record = feature.getRecord().getValues();
      final Object[] attribs = new Object[record.length + 2];
      for (int i = 0; i < attribs.length - 2; i++) {
         attribs[i] = record[i];
      }
      attribs[attribs.length - 2] = closest.getRecord().getValue(m_iField);
      final double dDist = feature.getGeometry().distance(closest.getGeometry());
      attribs[attribs.length - 1] = new Double(dDist);

      m_Output.addFeature(feature.getGeometry(), attribs);

      /*final Coordinate c = new Coordinate(x, y);
      Coordinate closestPointToThisLine;
      double minDist = Double.POSITIVE_INFINITY;
      String closestLineID = null;

      try {
         final List<NamedGeometry> list = m_NNF.getClosestGeometries(c, m_dTolerance);
         for (int iGeom = 0; iGeom < list.size(); iGeom++) {
            final NamedGeometry ng = list.get(iGeom);
            final Geometry geom = ng.geom;
            final int iNumGeom = geom.getNumGeometries();
            for (int i = 0; i < iNumGeom; i++) {
               final Geometry subGeom = geom.getGeometryN(i);
               final Coordinate[] coords = subGeom.getCoordinates();
               for (int j = 0; j < coords.length - 1; j++) {
                  final LineSegment line = new LineSegment(coords[j], coords[j + 1]);
                  closestPointToThisLine = line.closestPoint(c);
                  final double dist = c.distance(closestPointToThisLine);
                  if ((dist < minDist)) {
                     minDist = dist;
                     closestLineID = ng.name;
                  }
               }

            }
         }
      }
      catch (final Exception e) {// will return closest point so far
      }

      if (closestLineID != null) {
         m_Output.addRecord(new Object[] { closestLineID, new Double(minDist) });
      }*/

   }
}
