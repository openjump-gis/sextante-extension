

package es.unex.sextante.vectorTools.geometricPropertiesLines;

import org.locationtech.jts.algorithm.Angle;
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
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.math.simpleStats.SimpleStats;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.shapesTools.ShapesTools;


public class GeometricPropertiesLinesAlgorithm
         extends
            GeoAlgorithm {

   private static final String RESULT = "RESULT";
   private static final String LINES  = "LINES";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Geometric_properties_of_lines"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LINES, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_LINE, LINES);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      final Class[] types = { Double.class, Double.class, Double.class, Double.class, Double.class };
      final String[] sFields = { Sextante.getText("Length"), Sextante.getText("Straight_length"), Sextante.getText("Sinuosity"),
               Sextante.getText("Average_angle"), Sextante.getText("Direction") };

      final IVectorLayer lines = m_Parameters.getParameterValueAsVectorLayer(LINES);
      if (!m_bIsAutoExtent) {
         lines.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      if (lines.getShapesCount() == 0) {
         throw new GeoAlgorithmExecutionException("zero lines in layer");
      }

      i = 0;
      final int iShapesCount = lines.getShapesCount();
      final Object[][] values = new Object[5][iShapesCount];
      final IFeatureIterator iter = lines.iterator();
      while (iter.hasNext() && setProgress(i, iShapesCount)) {
         final IFeature feature = iter.next();
         final LineProperties lp = getProperties(feature.getGeometry());
         values[0][i] = new Double(lp.length);
         values[1][i] = new Double(lp.straightLength);
         values[2][i] = new Double(lp.sinuosity);
         values[3][i] = new Double(lp.angleStats.getMean());
         values[4][i] = new Double(lp.direction);
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

   private class LineProperties {

      public double      length, straightLength, sinuosity, direction;
      public SimpleStats angleStats = new SimpleStats();

   }


}
