

package es.unex.sextante.vectorTools.geometricPropertiesLinesB;

import java.util.ArrayList;

import org.locationtech.jts.algorithm.Angle;
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
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.shapesTools.ShapesTools;


public class GeometricPropertiesLinesBAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT   = "RESULT";
   public static final String LINES    = "LINES";
   public static final String DISTANCE = "DISTANCE";
   public static final String DEM      = "DEM";

   private IRasterLayer       m_DEM;
   private double             m_dDist;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Geometric_properties_of_lines_b"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LINES, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("DEM"), true);
         m_Parameters.addNumericalValue(DISTANCE, Sextante.getText("Distance_between_points"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, 0, Double.MAX_VALUE);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_LINE, LINES);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      final Class[] types = { Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class };
      final String[] sFields = { Sextante.getText("Length"), Sextante.getText("Straight_length"), Sextante.getText("Sinuosity"),
               Sextante.getText("Average_angle"), Sextante.getText("Direction"), Sextante.getText("Slope"),
               "Max." + Sextante.getText("Slope")/*, Sextante.getText("Slope" + "%"),
                                                                           "Max." + Sextante.getText("Slope")+ "%"*/};

      m_dDist = m_Parameters.getParameterValueAsDouble(DISTANCE);
      final IVectorLayer lines = m_Parameters.getParameterValueAsVectorLayer(LINES);
      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
      m_DEM.setFullExtent();
      if (!m_bIsAutoExtent) {
         lines.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      if (lines.getShapesCount() == 0) {
         throw new GeoAlgorithmExecutionException("zero lines in layer");
      }

      i = 0;
      final int iShapesCount = lines.getShapesCount();
      final Object[][] values = new Object[sFields.length][iShapesCount];
      final IFeatureIterator iter = lines.iterator();
      while (iter.hasNext() && setProgress(i, iShapesCount)) {
         final IFeature feature = iter.next();
         final LineProperties lp = getProperties(feature.getGeometry());
         final AdditionalStats stats = getAdditionalStats(feature.getGeometry());
         values[0][i] = new Double(lp.length);
         values[1][i] = new Double(lp.straightLength);
         values[2][i] = new Double(lp.sinuosity);
         values[3][i] = new Double(stats.meanAngle);
         values[4][i] = new Double(lp.direction);
         values[5][i] = new Double(stats.meanSlope * 100);
         values[6][i] = new Double(stats.maxSlope * 100);
         i++;
      }

      final IOutputChannel channel = getOutputChannel(RESULT);
      final Output out = m_OutputObjects.getOutput(RESULT);
      out.setDescription(lines.getName());
      out.setName("RESULT");
      out.setOutputChannel(channel);
      out.setOutputObject(ShapesTools.addFields(m_OutputFactory, lines, channel, sFields, values, types));

      return !m_Task.isCanceled();

   }


   private AdditionalStats getAdditionalStats(final Geometry geom) {

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

      final SimpleStats slopeStats = new SimpleStats();
      final SimpleStats angleStats = new SimpleStats();
      for (int k = 0; k < points.size() - 1; k++) {
         point = points.get(k);
         final double dElevation = m_DEM.getValueAt(point.getX(), point.getY());
         final Point point2 = points.get(k + 1);
         final double dElevation2 = m_DEM.getValueAt(point2.getX(), point2.getY());
         dDist = Math.sqrt(Math.pow(point.getX() - point2.getX(), 2.) + Math.pow(point.getY() - point2.getY(), 2.));
         final double dSlope = Math.abs((dElevation2 - dElevation) / dDist);
         slopeStats.addValue(dSlope);
         if ((k > 0) && (k < points.size() - 2)) {
            final Point point3 = points.get(k + 2);
            final double dAngle = getAngle(point, point2, point3);
            angleStats.addValue(dAngle);
         }
      }

      final AdditionalStats aStats = new AdditionalStats();
      aStats.maxSlope = slopeStats.getMax();
      aStats.meanSlope = slopeStats.getMean();
      aStats.meanAngle = angleStats.getMean();
      return aStats;

   }


   private LineProperties getProperties(final Geometry geometry) {

      double x, y;
      double dDifX, dDifY;
      double dInitX, dInitY;
      double dDist = 0, dStraightDist, dDirection;

      final Coordinate[] coords = geometry.getCoordinates();

      dInitX = x = coords[0].x;
      dInitY = y = coords[0].y;

      final LineProperties lp = new LineProperties();
      for (int i = 0; i < coords.length; i++) {
         if ((i > 0) && (i < coords.length - 1)) {
            lp.angleStats.addValue(getAngle(coords[i - 1], coords[i], coords[i + 1]));
         }
         dDifX = coords[i].x - x;
         dDifY = coords[i].y - y;
         dDist += Math.sqrt(dDifX * dDifX + dDifY * dDifY);
         x = coords[i].x;
         y = coords[i].y;
      }

      dDifX = x - dInitX;
      dDifY = y - dInitY;

      dStraightDist = Math.sqrt(Math.pow(dDifX, 2) + Math.pow(dDifY, 2));
      dDirection = Math.toDegrees(Math.atan2(dDifX, dDifY));

      lp.length = dDist;
      lp.straightLength = dStraightDist;
      lp.sinuosity = dDist / dStraightDist;
      lp.direction = dDirection;

      return lp;

   }


   private double getAngle(final Coordinate p1,
                           final Coordinate p2,
                           final Coordinate p3) {

      return Math.toDegrees(Angle.angleBetween(p1, p2, p3));

   }


   private double getAngle(final Point p1,
                           final Point p2,
                           final Point p3) {

      final Coordinate c1 = new Coordinate(p1.getX(), p1.getY());
      final Coordinate c2 = new Coordinate(p2.getX(), p2.getY());
      final Coordinate c3 = new Coordinate(p3.getX(), p3.getY());
      return Math.toDegrees(Angle.angleBetween(c1, c2, c3));

   }

   private class LineProperties {

      public double      length, straightLength, sinuosity, direction;
      public SimpleStats angleStats = new SimpleStats();

   }

   private class AdditionalStats {

      public double meanSlope, maxSlope, meanAngle;

   }


}
