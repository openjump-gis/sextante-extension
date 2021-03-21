

package es.unex.sextante.vectorTools.lineAverageSlope;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.math.simpleStats.SimpleStats;
import es.unex.sextante.outputs.OutputVectorLayer;


public class LineAverageSlopeAlgorithm
         extends
            GeoAlgorithm {

   public static final String  RESULT   = "RESULT";
   public static final String  DISTANCE = "DISTANCE";
   public static final String  DEM      = "DEM";
   public static final String  LINES    = "LINES";
   private static final double NODATA   = -99999;

   private IVectorLayer        m_Output;
   private double              m_dDist;
   private IRasterLayer        m_DEM;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;

      IVectorLayer lines;

      try {
         m_dDist = m_Parameters.getParameterValueAsDouble(DISTANCE);
         lines = m_Parameters.getParameterValueAsVectorLayer(LINES);
         m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
         m_DEM.setFullExtent();

         if (!m_bIsAutoExtent) {
            lines.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         }

         final Class[] types = new Class[lines.getFieldCount() + 2];
         final String[] sFields = new String[lines.getFieldCount() + 2];
         types[lines.getFieldCount()] = Double.class;
         sFields[lines.getFieldCount()] = Sextante.getText("Slope");
         types[lines.getFieldCount() + 1] = Double.class;
         sFields[lines.getFieldCount() + 1] = "Max " + Sextante.getText("Slope");
         for (int j = 0; j < lines.getFieldCount(); j++) {
            sFields[j] = lines.getFieldName(j);
            types[j] = lines.getFieldType(j);
         }
         m_Output = getNewVectorLayer(RESULT, lines.getName() + "[" + Sextante.getText("Slope") + "]",
                  IVectorLayer.SHAPE_TYPE_LINE, types, sFields);

         i = 0;
         final int iShapeCount = lines.getShapesCount();
         final IFeatureIterator iter = lines.iterator();
         while (iter.hasNext() && setProgress(i, iShapeCount)) {
            final IFeature feature = iter.next();
            final Geometry geom = feature.getGeometry();
            final SimpleStats stats = getSlopeStats(geom);
            final Object[] orgValues = feature.getRecord().getValues();
            final Object[] values = new Object[orgValues.length + 2];
            System.arraycopy(orgValues, 0, values, 0, orgValues.length);
            values[orgValues.length] = stats.getMean();
            values[orgValues.length + 1] = stats.getMax();
            m_Output.addFeature(geom, values);
            i++;
         }
      }
      catch (final Exception e) {
         throw new GeoAlgorithmExecutionException(e.getMessage());
      }

      return !m_Task.isCanceled();

   }


   private SimpleStats getSlopeStats(final Geometry geom) {

      int i, j;
      int iPoints;
      double dX1, dX2, dY1, dY2;
      double dAddedPointX, dAddedPointY;
      double dDX, dDY;
      double dRemainingDistFromLastSegment = 0;
      double dDistToNextPoint;
      double dDist;
      Point point;

      final Coordinate[] coords = geom.getCoordinates();

      final ArrayList<Point> points = new ArrayList<Point>();

      final GeometryFactory gf = new GeometryFactory();
      dAddedPointX = dX1 = coords[0].x;
      dAddedPointY = dY1 = coords[0].y;
      point = gf.createPoint(new Coordinate(dAddedPointX, dAddedPointY));
      points.add(point);
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
               points.add(point);
            }
            dDX = dX2 - dAddedPointX;
            dDY = dY2 - dAddedPointY;
            dRemainingDistFromLastSegment = Math.sqrt(dDX * dDX + dDY * dDY);
         }
         else {
            dRemainingDistFromLastSegment += dDistToNextPoint;
         }

      }

      final SimpleStats stats = new SimpleStats();
      for (int k = 0; k < points.size() - 1; k++) {
         point = points.get(k);
         final double dElevation = m_DEM.getValueAt(point.getX(), point.getY());
         final Point point2 = points.get(k + 1);
         final double dElevation2 = m_DEM.getValueAt(point2.getX(), point2.getY());
         dDist = Math.sqrt(Math.pow(point.getX() - point2.getX(), 2.) + Math.pow(point.getY() - point2.getY(), 2.));
         final double dSlope = Math.abs((dElevation2 - dElevation) / dDist);
         stats.addValue(dSlope);
      }

      return stats;

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Average_slope_line"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LINES, Sextante.getText("Lines"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         m_Parameters.addNumericalValue(DISTANCE, Sextante.getText("Distance_between_points"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, 0, Double.MAX_VALUE);
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("DEM"), true);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
