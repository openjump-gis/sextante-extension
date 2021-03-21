package es.unex.sextante.dataObjects;

import org.locationtech.jts.geom.Geometry;

/**
 * A simple implementation of the IFeature interface
 *
 * @author volaya
 *
 */
public class FeatureImpl
         implements
            IFeature {

   private final Geometry m_Geometry;
   private final IRecord  m_Record;


   /**
    * Creates a new feature
    *
    * @param geom
    *                the geometry of the feature
    * @param values
    *                the set of associated attributes
    */
   public FeatureImpl(final Geometry geom,
                      final Object values[]) {

      m_Geometry = geom;
      m_Record = new RecordImpl(values);


   }


   public Geometry getGeometry() {

      return m_Geometry;

   }


   public IRecord getRecord() {

      return m_Record;

   }

}
