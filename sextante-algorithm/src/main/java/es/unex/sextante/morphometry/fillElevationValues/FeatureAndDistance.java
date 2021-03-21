package es.unex.sextante.morphometry.fillElevationValues;

import es.unex.sextante.dataObjects.IFeature;

public class FeatureAndDistance
         implements
            Comparable {

   private final IFeature feature;
   private final double   dDist;


   public FeatureAndDistance(final IFeature feature,
                             final double dDist) {

      this.feature = feature;
      this.dDist = dDist;

   }


   public double getDist() {

      return dDist;

   }


   public IFeature getFeature() {

      return feature;

   }


   public int compareTo(final Object ob) throws ClassCastException {

      if (!(ob instanceof FeatureAndDistance)) {
         throw new ClassCastException();
      }

      final double dValue = ((FeatureAndDistance) ob).getDist();
      final double dDif = this.dDist - dValue;

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
