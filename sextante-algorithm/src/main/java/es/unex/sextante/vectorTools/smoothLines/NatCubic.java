/*********************************************************
 * Code adapted from Tim Lambert's Java Classes
 * http://www.cse.unsw.edu.au/~lambert/
 *********************************************************/
package es.unex.sextante.vectorTools.smoothLines;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class NatCubic
         extends
            ControlCurve {

   /* calculates the natural cubic spline that interpolates
   y[0], y[1], ... y[n]
   The first segment is returned as
   C[0].a + C[0].b*u + C[0].c*u^2 + C[0].d*u^3 0<=u <1
   the other segments are in C[1], C[2], ...  C[n-1] */

   public NatCubic(final Geometry geom) {

      super(geom);

   }


   Cubic[] calcNaturalCubic(final int n,
                            final double[] x) {
      final double[] gamma = new double[n + 1];
      final double[] delta = new double[n + 1];
      final double[] D = new double[n + 1];
      int i;
      /* We solve the equation
       [2 1       ] [D[0]]   [3(x[1] - x[0])  ]
       |1 4 1     | |D[1]|   |3(x[2] - x[0])  |
       |  1 4 1   | | .  | = |      .         |
       |    ..... | | .  |   |      .         |
       |     1 4 1| | .  |   |3(x[n] - x[n-2])|
       [       1 2] [D[n]]   [3(x[n] - x[n-1])]

       by using row operations to convert the matrix to upper triangular
       and then back sustitution.  The D[i] are the derivatives at the knots.
       */

      gamma[0] = 1.0f / 2.0f;
      for (i = 1; i < n; i++) {
         gamma[i] = 1 / (4 - gamma[i - 1]);
      }
      gamma[n] = 1 / (2 - gamma[n - 1]);

      delta[0] = 3 * (x[1] - x[0]) * gamma[0];
      for (i = 1; i < n; i++) {
         delta[i] = (3 * (x[i + 1] - x[i - 1]) - delta[i - 1]) * gamma[i];
      }
      delta[n] = (3 * (x[n] - x[n - 1]) - delta[n - 1]) * gamma[n];

      D[n] = delta[n];
      for (i = n - 1; i >= 0; i--) {
         D[i] = delta[i] - gamma[i] * D[i + 1];
      }

      /* now compute the coefficients of the cubics */
      final Cubic[] C = new Cubic[n];
      for (i = 0; i < n; i++) {
         C[i] = new Cubic(x[i], D[i], 3 * (x[i + 1] - x[i]) - 2 * D[i] - D[i + 1], 2 * (x[i] - x[i + 1]) + D[i] + D[i + 1]);
      }
      return C;
   }


   @Override
   public LineString getSmoothedLine(final int iSteps) {

      final ArrayList<Coordinate> coords = new ArrayList<Coordinate>();

      if (m_X.length >= 2) {
         final Cubic[] X = calcNaturalCubic(m_X.length - 1, m_X);
         final Cubic[] Y = calcNaturalCubic(m_Y.length - 1, m_Y);

         /* very crude technique - just break each segment up into steps lines */
         coords.add(new Coordinate((int) Math.round(X[0].eval(0)), (int) Math.round(Y[0].eval(0))));
         for (int i = 0; i < X.length; i++) {
            for (int j = 1; j <= iSteps; j++) {
               final float u = j / (float) iSteps;
               coords.add(new Coordinate(X[i].eval(u), Y[i].eval(u)));
            }
         }

         return new GeometryFactory().createLineString((Coordinate[]) coords.toArray(new Coordinate[0]));

      }
      else {
         return new GeometryFactory().createLineString(m_Geometry.getCoordinates());
      }

   }


}
