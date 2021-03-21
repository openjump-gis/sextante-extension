

package es.unex.sextante.vectorTools.splitLinesWithPoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;

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


public class SplitLinesWithPointsAlgorithm
         extends
            GeoAlgorithm {

   public static final String     RESULT    = "RESULT";
   public static final String     METHOD    = "METHOD";
   public static final String     TOLERANCE = "TOLERANCE";
   public static final String     LINES     = "LINES";
   public static final String     POINTS    = "POINTS";

   private IVectorLayer           m_Points;
   private IVectorLayer           m_Lines;

   private IVectorLayer           m_Output;
   private double                 m_dTolerance;
   private int                    m_iMethod;
   private ArrayList              m_Path;
   private Object                 m_LastValue[];
   private BitSet                 m_UsedPoints;
   private NearestNeighbourFinder m_NNF;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Split_lines_with_points_layer"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      final String sOptions[] = { Sextante.getText("Add_data_before_point"), Sextante.getText("Add_data_after_point") };

      try {
         m_Parameters.addInputVectorLayer(LINES, Sextante.getText("Capa_de_lineas"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE,
                  true);
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);
         m_Parameters.addNumericalValue(TOLERANCE, Sextante.getText("Tolerance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 10, 0, Double.MAX_VALUE);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sOptions);
         addOutputVectorLayer(RESULT, Sextante.getText("Splitted_lines"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final RepeatedParameterNameException e) {

      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;

      m_Points = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      m_Lines = m_Parameters.getParameterValueAsVectorLayer(LINES);
      m_iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      m_dTolerance = m_Parameters.getParameterValueAsDouble(TOLERANCE);

      if (!m_bIsAutoExtent) {
         m_Points.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         m_Lines.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final Class[] types = new Class[m_Lines.getFieldCount() + m_Points.getFieldCount()];
      final String[] sFields = new String[m_Lines.getFieldCount() + m_Points.getFieldCount()];
      for (i = 0; i < m_Lines.getFieldCount(); i++) {
         types[i] = m_Lines.getFieldType(i);
         sFields[i] = m_Lines.getFieldName(i);
      }
      for (i = 0; i < m_Points.getFieldCount(); i++) {
         sFields[i + m_Lines.getFieldCount()] = m_Points.getFieldName(i);
         types[i + m_Lines.getFieldCount()] = m_Points.getFieldType(i);
      }

      m_Output = getNewVectorLayer(RESULT, Sextante.getText("Splitted_lines"), m_Lines.getShapeType(), types, sFields);

      m_NNF = new NearestNeighbourFinder(m_Points, this.m_Task);
      m_UsedPoints = new BitSet(m_Points.getShapesCount());

      iShapeCount = m_Lines.getShapesCount();
      i = 0;
      final IFeatureIterator iter = m_Lines.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         splitLine(feature.getGeometry(), feature.getRecord().getValues());
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   private void splitLine(final Geometry line,
                          final Object[] row) {

      final Object[] nullValue = new Object[m_Points.getFieldCount()];
      for (int i = 0; i < nullValue.length; i++) {
         try {
            final Class c = m_Points.getFieldType(i);
            nullValue[i] = c.newInstance();
         }
         catch (final Exception e) {
         };
      }
      m_LastValue = nullValue;
      m_Path = new ArrayList();

      Coordinate[] coords;
      for (int i = 0; i < line.getNumGeometries(); i++) {
         try {
            final Geometry geom = line.getGeometryN(i);
            coords = geom.getCoordinates();
            m_Path.add(coords[0]);
            LineSegment segment = null;
            for (int j = 0; j < coords.length - 1; j++) {
               segment = new LineSegment(coords[j], coords[j + 1]);
               splitSegment(segment, row);
            }
            if (segment != null) {
               m_Path.add(segment.p1);
            }
            final Object[] values = new Object[row.length + m_Points.getFieldCount()];
            for (int j = 0; j < row.length; j++) {
               values[j] = row[j];
            }
            if (m_iMethod == 0) {
               for (int j = 0; j < m_Points.getFieldCount(); j++) {
                  values[row.length + j] = nullValue[j];
               }
            }
            else {
               for (int j = 0; j < m_Points.getFieldCount(); j++) {
                  values[row.length + j] = m_LastValue[j];
               }
            }

            addCurrentGeometry(values);

         }
         catch (final Exception e) {
            Sextante.addErrorToLog(e);
         }
      }

   }


   private void splitSegment(final LineSegment seg,
                             final Object[] row) throws Exception {

      final ArrayList closePoints = new ArrayList();

      final PtAndAttributes[] pts = m_NNF.getClosestPoints(seg, m_dTolerance);

      for (int i = 0; i < pts.length; i++) {
         if (!m_UsedPoints.get(pts[i].id)) {
            final Coordinate c = new Coordinate(pts[i].x, pts[i].y);
            double dDist = seg.distance(c);
            if (dDist < m_dTolerance) {
               final Coordinate closest = seg.closestPoint(c);
               final double dX = closest.x - seg.p0.x;
               final double dY = closest.y - seg.p0.y;
               dDist = dX * dX + dY * dY;
               closePoints.add(new CoordinateAndDist(closest, dDist, pts[i].attrs));
               m_UsedPoints.set(pts[i].id);
            }
         }
      }

      final CoordinateAndDist[] ptArray = new CoordinateAndDist[closePoints.size()];
      for (int i = 0; i < closePoints.size(); i++) {
         ptArray[i] = (CoordinateAndDist) closePoints.get(i);
      }

      Arrays.sort(ptArray);

      for (int i = 0; i < ptArray.length; i++) {
         final Object[] values = new Object[row.length + m_Points.getFieldCount()];
         for (int j = 0; j < row.length; j++) {
            values[j] = row[j];
         }
         if (m_iMethod == 0) {
            for (int j = 0; j < m_Points.getFieldCount(); j++) {
               values[row.length + j] = ptArray[i].val[j];
            }
         }
         else {
            for (int j = 0; j < m_Points.getFieldCount(); j++) {
               values[row.length + j] = m_LastValue[j];
            }
            m_LastValue = ptArray[i].val;
         }

         m_Path.add(ptArray[i].coord);
         addCurrentGeometry(values);
         m_Path = new ArrayList();
         m_Path.add(ptArray[i].coord);
      }

      m_Path.add(seg.p1);

   }


   private void addCurrentGeometry(final Object[] values) {

      if (m_Path.size() != 0) {
         final Coordinate[] coords = new Coordinate[m_Path.size()];
         for (int i = 0; i < coords.length; i++) {
            final Coordinate c = (Coordinate) m_Path.get(i);
            coords[i] = c;
         }
         final GeometryFactory gf = new GeometryFactory();
         m_Output.addFeature(gf.createLineString(coords), values);
      }
   }

   private class CoordinateAndDist
            implements
               Comparable {

      private final Coordinate coord;
      private final double     dist;
      private final Object[]   val;


      CoordinateAndDist(final Coordinate c,
                        final double d,
                        final Object[] v) {

         coord = c;
         dist = d;
         val = v;

      }


      public int compareTo(final Object ob) {

         if (!(ob instanceof CoordinateAndDist)) {
            throw new ClassCastException();
         }

         final double dist2 = ((CoordinateAndDist) ob).dist;

         return (int) Math.signum(this.dist - dist2);

      }


   }

}
