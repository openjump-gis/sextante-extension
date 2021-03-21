package es.unex.sextante.dataObjects.vectorFilters;

import es.unex.sextante.dataObjects.IFeature;

/**
 * A generic filter to apply to a vector layer. It just filters features one by one according to any implemented rule.
 *
 * @author volaya
 *
 */

public interface GenericVectorFilter
         extends
            IVectorLayerFilter {

   /**
    * Returns true if the feature passes the filter. False otherwise
    *
    * @param feature
    *                the feature to filter
    * @return true if the feature passes the filter. False otherwise
    */
   public boolean accept(IFeature feature);

}
