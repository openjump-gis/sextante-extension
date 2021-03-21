package es.unex.sextante.rasterWrappers;

public class Grid3DCell
         implements
            Comparable {

   private int    m_iX, m_iY, m_iZ;
   private double m_dValue;


   public Grid3DCell(final int iX,
                     final int iY,
                     final int iZ,
                     final double dValue) {

      m_iX = iX;
      m_iY = iY;
      m_iZ = iZ;
      m_dValue = dValue;

   }


   public double getValue() {

      return m_dValue;

   }


   public void setValue(final double dValue) {

      m_dValue = dValue;

   }


   public int getX() {

      return m_iX;

   }


   public void setX(final int iX) {

      m_iX = iX;

   }


   public int getY() {

      return m_iY;

   }


   public void setY(final int iY) {

      m_iY = iY;

   }


   public int getZ() {

      return m_iZ;

   }


   public void setZ(final int iZ) {

      m_iZ = iZ;

   }


   public int compareTo(final Object cell) throws ClassCastException {

      if (!(cell instanceof Grid3DCell)) {
         throw new ClassCastException();
      }

      final double dValue = ((Grid3DCell) cell).getValue();
      final double dDif = this.m_dValue - dValue;

      if (dDif > 0.0) {
         return 1;
      }
      else if (dDif < 0.0) {
         return -1;
      }
      else {
         return 0;
      }

   }


   @Override
   public boolean equals(final Object obj) {

      if (obj instanceof Grid3DCell) {
         final Grid3DCell cell = (Grid3DCell) obj;
         return ((m_iX == cell.getX()) && (m_iY == cell.getY()) && (m_iZ == cell.getZ()) && (m_dValue == cell.getValue()));
      }
      else {
         return false;
      }

   }


}
