

package es.unex.sextante.vectorTools.minimumEnclosingPolygon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.AffineTransformation;

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


public class MinimumEnclosingPolygonAlgorithm
         extends
            GeoAlgorithm {

   private static final int   CONVEX_HULL  = 0;
   private static final int   ME_CIRCLE    = 1;
   private static final int   ME_RECTANGLE = 2;

   public static final String POINTS       = "POINTS";
   public static final String FIELD        = "FIELD";
   public static final String METHOD       = "METHOD";
   public static final String USECLASSES   = "USECLASSES";
   public static final String RESULT       = "RESULT";

   private int                m_iMethod;
   private Geometry           m_ConvexHull;
   private IVectorLayer       m_Layer;
   private boolean            m_bUseClasses;
   private int                m_iClass;
   IVectorLayer               m_Output;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iCount;
      final HashMap map = new HashMap(9);
      ArrayList list;
      Object key;

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      m_iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      m_bUseClasses = m_Parameters.getParameterValueAsBoolean(USECLASSES);
      m_iClass = m_Parameters.getParameterValueAsInt(FIELD);

      if (!m_bIsAutoExtent) {
         m_Layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      iCount = m_Layer.getShapesCount();

      if (m_bUseClasses) {
         m_Output = getNewVectorLayer(RESULT, m_Layer.getName() + Sextante.getText("[Enclosing_shape]"),
                  IVectorLayer.SHAPE_TYPE_POLYGON, new Class[] { String.class }, new String[] { m_Layer.getFieldName(m_iClass) });
      }
      else {
         m_Output = getNewVectorLayer(RESULT, m_Layer.getName() + Sextante.getText("[Enclosing_shape]"),
                  IVectorLayer.SHAPE_TYPE_POLYGON, new Class[] { String.class }, new String[] { "ID" });
      }

      i = 0;
      final IFeatureIterator iter = m_Layer.iterator();
      while (iter.hasNext() && setProgress(i, iCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         if (!m_bUseClasses) {
            key = "1";
         }
         else {
            key = feature.getRecord().getValue(m_iClass);
         }
         list = (ArrayList) map.get(key);
         if (list == null) {
            list = new ArrayList();
            map.put(key, list);
         }
         final Coordinate[] coords = geom.getCoordinates();
         for (final Coordinate element : coords) {
            list.add(element);
         }
      }

      final Set set = map.keySet();
      final Iterator setiter = set.iterator();

      while (setiter.hasNext()) {
         key = setiter.next();
         list = (ArrayList) map.get(key);
         final Point points[] = new Point[list.size()];
         final GeometryFactory geometryFactory = new GeometryFactory();
         for (i = 0; i < list.size(); i++) {
            points[i] = geometryFactory.createPoint((Coordinate) list.get(i));
         }
         try {
            final MultiPoint mp = new MultiPoint(points, new GeometryFactory());
            m_ConvexHull = mp.convexHull();
            generateOutput(key);
         }
         catch (final Exception e) {
         }

      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Minimum_enclosing_shapes"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setIsDeterminatedProcess(false);
      setUserCanDefineAnalysisExtent(true);

      final String sMethod[] = { Sextante.getText("Convex_hull"), Sextante.getText("Minimum_enclosing_circle"),
               Sextante.getText("Minimum_enclosing_rectangle") };
      try {
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), "POINTS");
         m_Parameters.addSelection(METHOD, Sextante.getText("Enclosing_shape"), sMethod);
         m_Parameters.addBoolean(USECLASSES, Sextante.getText("Create_one_polygon_for_each_class"), false);
         addOutputVectorLayer(RESULT, Sextante.getText("Minimum_enclosing_shape"), OutputVectorLayer.SHAPE_TYPE_POINT);
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


   private void generateOutput(final Object ob) {

      final Object value[] = new String[] { (String) ob };

      switch (m_iMethod) {
         case CONVEX_HULL:
         default:
            m_Output.addFeature(m_ConvexHull, value);
            break;
         case ME_CIRCLE:
            m_Output.addFeature(getMinEnclosingCircle(), value);
            break;
         case ME_RECTANGLE:
            m_Output.addFeature(getMinEnclosingRectangle(), value);
            break;
      }

   }


   private Geometry getMinEnclosingRectangle() {

      int i2;
      double x, y;
      double dArea;
      double dAngle;
      double dMinArea = Double.MAX_VALUE;
      Geometry convexHull;
      Envelope rect;
      AffineTransformation at, iat;
      Geometry minRect = null;
      final Coordinate pts[] = m_ConvexHull.getCoordinates();

      if (pts.length < 3) {
         return null;
      }

      final GeometryFactory gf = new GeometryFactory();
      for (int i = 0; i < pts.length; i++) {
         i2 = (i + 1) % pts.length;
         x = pts[i2].x - pts[i].x;
         y = pts[i2].y - pts[i].y;
         dAngle = -Math.atan2(y, x);
         at = new AffineTransformation();
         at.translate(pts[i].x, pts[i].y);
         at.rotate(dAngle);
         convexHull = (Geometry) m_ConvexHull.clone();
         convexHull.apply(at);
         rect = convexHull.getEnvelopeInternal();
         dArea = (rect.getMaxX() - rect.getMinX()) * (rect.getMaxY() - rect.getMinY());
         if (dArea < dMinArea) {
            dMinArea = dArea;
            iat = new AffineTransformation();
            iat.rotate(-dAngle);
            iat.translate(-pts[i].x, -pts[i].y);
            final Coordinate[] coord = new Coordinate[5];
            coord[0] = new Coordinate(rect.getMinX(), rect.getMinY());
            coord[1] = new Coordinate(rect.getMaxX(), rect.getMinY());
            coord[2] = new Coordinate(rect.getMaxX(), rect.getMaxY());
            coord[3] = new Coordinate(rect.getMinX(), rect.getMaxY());
            coord[4] = new Coordinate(rect.getMinX(), rect.getMinY());
            final LinearRing ring = gf.createLinearRing(coord);
            minRect = gf.createPolygon(ring, null);
            minRect.apply(iat);
         }

      }

      return minRect;

   }


   private Geometry getMinEnclosingCircle() {

      final Coordinate[] bndry = new Coordinate[3];
      final Coordinate pts[] = m_ConvexHull.getCoordinates();
      final Circle circle = getMinEnclosingCircle(pts.length, pts, 0, bndry);
      final Coordinate center = circle.center;

      return ShapesTools.createCircle(center.x, center.y, circle.radius);

   }


   private Circle findCenterRadius(final Coordinate p1,
                                   final Coordinate p2,
                                   final Coordinate p3) {

      final double x = (p3.x * p3.x * (p1.y - p2.y) + (p1.x * p1.x + (p1.y - p2.y) * (p1.y - p3.y)) * (p2.y - p3.y) + p2.x
                                                                                                                      * p2.x
                                                                                                                      * (-p1.y + p3.y))
                       / (2 * (p3.x * (p1.y - p2.y) + p1.x * (p2.y - p3.y) + p2.x * (-p1.y + p3.y)));

      final double y = (p2.y + p3.y) / 2 - (p3.x - p2.x) / (p3.y - p2.y) * (x - (p2.x + p3.x) / 2);

      final Coordinate c = new Coordinate(x, y);
      final double r = distance(c, p1);

      return new Circle(c, r);

   }


   private Circle getMinEnclosingCircle(final int n,
                                        final Coordinate[] p,
                                        final int m,
                                        final Coordinate[] b) {

      Coordinate c = new Coordinate(-1, -1);
      double r = 0;


      //... Compute the smallest circle defined by B.
      if (m == 1) {
         c = new Coordinate(b[0].x, b[0].y);
         r = 0;
      }
      else if (m == 2) {
         c = new Coordinate((b[0].x + b[1].x) / 2, (b[0].y + b[1].y) / 2);
         r = distance(b[0], c);
      }
      else if (m == 3) {
         return findCenterRadius(b[0], b[1], b[2]);
      }


      Circle minC = new Circle(c, r);

      //... Now see if all the points in P are enclosed.
      for (int i = 0; i < n; i++) {
         if (distance(p[i], minC.center) > minC.radius) {
            //... Compute B <--- B union P[i].
            b[m] = new Coordinate(p[i].x, p[i].y);

            //... Recurse
            minC = getMinEnclosingCircle(i, p, m + 1, b);
         }
      }

      return minC;
   }


   private double distance(final Coordinate p1,
                           final Coordinate p2) {

      return Math.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));

   }

   private class Circle {

      public Coordinate center;
      public double     radius;


      public Circle(final Coordinate c,
                    final double r) {

         center = c;
         radius = r;

      }

   }


}
