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

public class Bezier
         extends
            ControlCurve {

   public Bezier(final Geometry geom) {

      super(geom);

   }


   // the basis function for a Bezier spline
   static float b(final int i,
                  final float t) {
      switch (i) {
         case 0:
            return (1 - t) * (1 - t) * (1 - t);
         case 1:
            return 3 * t * (1 - t) * (1 - t);
         case 2:
            return 3 * t * t * (1 - t);
         case 3:
            return t * t * t;
      }
      return 0; //we only get here if an invalid i is specified
   }


   //evaluate a point on the B spline
   Point p(final int i,
           final float t) {
      float px = 0;
      float py = 0;
      for (int j = 0; j <= 3; j++) {
         px += b(j, t) * m_X[i + j];
         py += b(j, t) * m_Y[i + j];
      }
      return new Point((int) Math.round(px), (int) Math.round(py));
   }


   @Override
   public LineString getSmoothedLine(final int iSteps) {

      final ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
      Point q = p(0, 0);
      coords.add(new Coordinate(q.x, q.y));
      for (int i = 0; i < m_X.length - 3; i += 3) {
         for (int j = 1; j <= iSteps; j++) {
            q = p(i, j / (float) iSteps);
            coords.add(new Coordinate(q.x, q.y));
         }
      }

      return new GeometryFactory().createLineString((Coordinate[]) coords.toArray(new Coordinate[0]));

   }

}
