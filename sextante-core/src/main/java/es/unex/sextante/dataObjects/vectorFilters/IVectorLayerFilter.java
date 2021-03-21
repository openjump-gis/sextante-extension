package es.unex.sextante.dataObjects.vectorFilters;

import es.unex.sextante.dataObjects.IFeature;


/**
 * A filter to apply to a vector layer.
 * 
 * @author volaya
 * 
 */
public interface IVectorLayerFilter {

   public boolean accept(IFeature feature,
                         int iIndex);


}
