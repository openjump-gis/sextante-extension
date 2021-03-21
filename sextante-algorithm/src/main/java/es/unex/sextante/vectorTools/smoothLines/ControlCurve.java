/*********************************************************
 * Code adapted from Tim Lambert's Java Classes
 * http://www.cse.unsw.edu.au/~lambert/
 *********************************************************/
package es.unex.sextante.vectorTools.smoothLines;

/** This class represents a curve defined by a sequence of control points */

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

public abstract class ControlCurve {

   protected double[] m_X;
   protected double[] m_Y;
   protected Geometry m_Geometry;


   public ControlCurve(final Geometry geom) {

      final Coordinate[] coords = geom.getCoordinates();
      m_X = new double[coords.length];
      m_Y = new double[coords.length];
      for (int i = 0; i < coords.length; i++) {
         m_X[i] = coords[i].x;
         m_Y[i] = coords[i].y;
      }
      m_Geometry = geom;

   }


   public abstract LineString getSmoothedLine(int iSteps);

}
