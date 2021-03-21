/*********************************************************
 * Code adapted from Tim Lambert's Java Classes
 * http://www.cse.unsw.edu.au/~lambert/
 *********************************************************/
package es.unex.sextante.vectorTools.smoothLines;

import java.awt.Point;
import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class BSpline
         extends
            ControlCurve {

   public BSpline(final Geometry geom) {

      super(geom);

   }


   // the basis function for a cubic B spline
   float b(final int i,
           float t) {
      switch (i) {
         case -2:
            return (((-t + 3) * t - 3) * t + 1) / 6;
         case -1:
            return (((3 * t - 6) * t) * t + 4) / 6;
         case 0:
            return (((-3 * t + 3) * t + 3) * t + 1) / 6;
         case 1:
            return (t * t * t) / 6;
      }
      return 0; //we only get here if an invalid i is specified
   }


   //evaluate a point on the B spline
   Point p(final int i,
           final float t) {
      float px = 0;
      float py = 0;
      for (int j = -2; j <= 1; j++) {
         px += b(j, t) * m_X[i + j];
         py += b(j, t) * m_Y[i + j];
      }
      return new Point((int) Math.round(px), (int) Math.round(py));
   }


   @Override
   public LineString getSmoothedLine(final int iSteps) {

      final ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
      Point q = p(2, 0);
      coords.add(new Coordinate(q.x, q.y));
      for (int i = 2; i < m_X.length - 1; i++) {
         for (int j = 1; j <= iSteps; j++) {
            q = p(i, j / (float) iSteps);
            coords.add(new Coordinate(q.x, q.y));
         }
      }

      return new GeometryFactory().createLineString((Coordinate[]) coords.toArray(new Coordinate[0]));

   }

}
