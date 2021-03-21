package es.unex.sextante.dataObjects;

import org.locationtech.jts.geom.Geometry;

/**
 * A generic interface for features, to be used by SEXTANTE algorithms
 *
 * @author Victor Olaya volaya@unex.es
 *
 */
public interface IFeature {

   /**
    * Returns the geometry of the feature
    *
    * @return the geometry of the feature
    */
   public Geometry getGeometry();


   /**
    * Returns the set of attributes of the feature
    *
    * @return the attributes of the feature
    */
   public IRecord getRecord();

}
