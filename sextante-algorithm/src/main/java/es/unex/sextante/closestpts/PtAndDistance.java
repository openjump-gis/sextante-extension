package es.unex.sextante.closestpts;

public class PtAndDistance
         implements
            Comparable {

   private final Point3D m_Pt;
   private final double  m_dDist;


   public PtAndDistance(final Point3D pt,
                        final double dDistance) {

      m_Pt = pt;
      m_dDist = dDistance;

   }


   public double getDist() {

      return m_dDist;

   }


   public Point3D getPt() {

      return m_Pt;

   }


   public int compareTo(final Object ob) throws ClassCastException {

      if (!(ob instanceof PtAndDistance)) {
         throw new ClassCastException();
      }

      final double dValue = ((PtAndDistance) ob).getDist();
      final double dDif = this.m_dDist - dValue;

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

}
