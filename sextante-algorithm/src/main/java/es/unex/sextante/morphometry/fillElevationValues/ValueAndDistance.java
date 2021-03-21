package es.unex.sextante.morphometry.fillElevationValues;

public class ValueAndDistance
         implements
            Comparable {

   private final double m_dValue;
   private final double m_dDist;


   public ValueAndDistance(final double dValue,
                           final double dDistance) {

      m_dValue = dValue;
      m_dDist = dDistance;

   }


   public double getDist() {

      return m_dDist;

   }


   public double getValue() {

      return m_dValue;

   }


   public int compareTo(final Object ob) throws ClassCastException {

      if (!(ob instanceof ValueAndDistance)) {
         throw new ClassCastException();
      }

      final double dValue = ((ValueAndDistance) ob).getDist();
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
