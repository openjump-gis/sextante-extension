

package es.unex.sextante.vectorTools.linesToEquispacedPoints;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

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


public class LinesToEquispacedPointsAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT   = "RESULT";
   public static final String DISTANCE = "DISTANCE";
   public static final String LINES    = "LINES";

   private IVectorLayer       m_Output;
   private double             m_dDist;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;

      IVectorLayer lines;

      try {
         m_dDist = m_Parameters.getParameterValueAsDouble(DISTANCE);
         lines = m_Parameters.getParameterValueAsVectorLayer(LINES);

         if (!m_bIsAutoExtent) {
            lines.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         }

         m_Output = getNewVectorLayer(RESULT, Sextante.getText("Points") + "(" + lines.getName() + ")",
                  IVectorLayer.SHAPE_TYPE_POINT, lines.getFieldTypes(), lines.getFieldNames());

         i = 0;
         final int iShapeCount = lines.getShapesCount();
         final IFeatureIterator iter = lines.iterator();
         while (iter.hasNext() && setProgress(i, iShapeCount)) {
            final IFeature feature = iter.next();
            final Geometry geom = feature.getGeometry();
            for (int j = 0; j < geom.getNumGeometries(); j++) {
               final Geometry subgeom = geom.getGeometryN(j);
               processLine(subgeom, feature.getRecord().getValues());

            }
            i++;
         }
      }
      catch (final Exception e) {
         throw new GeoAlgorithmExecutionException(e.getMessage());
      }

      return !m_Task.isCanceled();

   }


   private void processLine(final Geometry geom,
                            final Object[] record) {

      int i, j;
      int iPoints;
      double dX1, dX2, dY1, dY2;
      double dAddedPointX, dAddedPointY;
      double dDX, dDY;
      double dRemainingDistFromLastSegment = 0;
      double dDistToNextPoint;
      double dDist;
      Geometry point;

      final Coordinate[] coords = geom.getCoordinates();
      if (coords.length == 0) {
         return;
      }

      final GeometryFactory gf = new GeometryFactory();
      dAddedPointX = dX1 = coords[0].x;
      dAddedPointY = dY1 = coords[0].y;
      point = gf.createPoint(new Coordinate(dAddedPointX, dAddedPointY));
      m_Output.addFeature(point, record);
      for (i = 0; i < coords.length - 1; i++) {
         dX2 = coords[i + 1].x;
         dX1 = coords[i].x;
         dY2 = coords[i + 1].y;
         dY1 = coords[i].y;
         dDX = dX2 - dX1;
         dDY = dY2 - dY1;
         dDistToNextPoint = Math.sqrt(dDX * dDX + dDY * dDY);

         if (dRemainingDistFromLastSegment + dDistToNextPoint > m_dDist) {
            iPoints = (int) ((dRemainingDistFromLastSegment + dDistToNextPoint) / m_dDist);
            dDist = m_dDist - dRemainingDistFromLastSegment;
            for (j = 0; j < iPoints; j++) {
               dDist = m_dDist - dRemainingDistFromLastSegment;
               dDist += j * m_dDist;
               dAddedPointX = dX1 + dDist * dDX / dDistToNextPoint;
               dAddedPointY = dY1 + dDist * dDY / dDistToNextPoint;
               point = gf.createPoint(new Coordinate(dAddedPointX, dAddedPointY));
               m_Output.addFeature(point, record);
            }
            dDX = dX2 - dAddedPointX;
            dDY = dY2 - dAddedPointY;
            dRemainingDistFromLastSegment = Math.sqrt(dDX * dDX + dDY * dDY);
         }
         else {
            dRemainingDistFromLastSegment += dDistToNextPoint;
         }

      }

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Lines_to_equispaced_points"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LINES, Sextante.getText("Lines"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         m_Parameters.addNumericalValue(DISTANCE, Sextante.getText("Distance_between_points"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, 0, Double.MAX_VALUE);
         addOutputVectorLayer(RESULT, Sextante.getText("Points"), OutputVectorLayer.SHAPE_TYPE_POINT);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
