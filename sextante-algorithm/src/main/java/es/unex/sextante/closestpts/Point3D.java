package es.unex.sextante.closestpts;

public class Point3D {

   private double m_dX, m_dY, m_dZ;


   public Point3D(final double dX,
                  final double dY,
                  final double dZ) {

      m_dX = dX;
      m_dY = dY;
      m_dZ = dZ;

   }


   public double getZ() {

      return m_dZ;

   }


   public void setZ(final double dZ) {

      m_dZ = dZ;

   }


   public double getX() {

      return m_dX;

   }


   public void setX(final double dX) {

      m_dX = dX;

   }


   public double getY() {

      return m_dY;

   }


   public void setY(final double dY) {

      m_dY = dY;

   }

}
