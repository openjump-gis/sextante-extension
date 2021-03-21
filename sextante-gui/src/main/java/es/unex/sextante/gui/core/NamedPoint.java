package es.unex.sextante.gui.core;

import java.awt.geom.Point2D;

public class NamedPoint {

   private final Point2D m_Point;
   private final String  m_sName;


   public NamedPoint(final String sName,
                     final Point2D point) {

      m_Point = point;
      m_sName = sName;

   }


   public Point2D getPoint() {
      return m_Point;
   }


   public String getName() {
      return m_sName;
   }


   @Override
   public String toString() {

      return m_sName;

   }


   public String toStringFull() {

      return m_sName + ":" + Double.toString(m_Point.getX()) + "," + Double.toString(m_Point.getY());

   }


}
