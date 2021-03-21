package es.unex.sextante.dataObjects;

import java.util.List;

import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.dataObjects.vectorFilters.IVectorLayerFilter;

/**
 * This is the base interface that all vector layers have to implement to be able to be used by SEXTANTE algorithms.
 * 
 * Instead of implementing this class directly, it is recommended to extend {@link AbstractVectorLayer}.
 * 
 * @author Victor Olaya. volaya@unex.es
 * 
 */
public interface IVectorLayer
         extends
            ILayer {

   public static final int SHAPE_TYPE_POINT   = 0;
   public static final int SHAPE_TYPE_LINE    = 1;
   public static final int SHAPE_TYPE_POLYGON = 2;
   public static final int SHAPE_TYPE_MIXED   = 3;

   /**
    * this constant indicates that the shape type is not compatible with SEXTANTE (for example a layer with multiple types if that
    * is not supported), and should be used to filter out layers
    */
   public static final int SHAPE_TYPE_WRONG   = -1;


   /**
    * Adds a new feature to the layer
    * 
    * @param geometry
    *                the geometry
    * @param attributes
    *                the attributes associated with the geometry
    */
   public void addFeature(Geometry geometry,
                          Object[] attributes);


   /**
    * Adds a new feature to the layer
    * 
    * @param feature
    *                the feature to add
    */
   public void addFeature(IFeature feature);


   /**
    * Returns an iterator to iterate through the entities of this layer
    * 
    * @return an iterator to iterate the layer
    */
   public IFeatureIterator iterator();


   /**
    * Returns the name of a given field in the attributes table
    * 
    * @param index
    *                the zero-based field index
    * @return the name of the selected attribute field
    */
   public String getFieldName(int index);


   /**
    * Return a class representing the data type of a given field
    * 
    * @param index
    *                the zero-based field index
    * @return the data type of the selected attribute field
    */
   public Class getFieldType(int index);


   /**
    * 
    * @return the number of attributes associates to each geometry
    */
   public int getFieldCount();


   /**
    * Returns an array of classes representing the data types of the fields in the attributes table
    * 
    * @return the data types of attribute fields
    */
   public Class[] getFieldTypes();


   /**
    * Returns the names of the fields in the attributes table
    * 
    * @return the names of the attribute fields
    */
   public String[] getFieldNames();


   /**
    * Returns the number of features in this layer
    * 
    * @return the number of features in this layer
    */
   public int getShapesCount();


   /**
    * Returns the type of geometries in this layer
    * 
    * @return the type of geometries in this layer
    */
   public int getShapeType();


   /**
    * Returns the index of a field from its name. Returns -1 if there is not a field with that name.
    * 
    * @param fieldName
    *                the name of the field
    * @return the index of the given field
    */
   public int getFieldIndexByName(String fieldName);


   /**
    * Adds a new filter to this layer. When iterating the features in this layer, only those that pass the filter will be
    * returned.
    * 
    * @param filter
    *                the filter to add
    */
   public void addFilter(IVectorLayerFilter filter);


   /**
    * Removes all filters previously added to this layer
    */
   public void removeFilters();


   /**
    * Returns a list of all the filters currently added to this layer
    * 
    * @return a list of all the filters currently added to this layer
    */
   public List<IVectorLayerFilter> getFilters();


   /**
    * Returns true if the layer can be edited. This includes editing while the layer is being read, so it can be overwritten by
    * algorithms that allow overwriting of input layers
    * 
    * @return true if the layer can be edited
    */
   public boolean canBeEdited();


}
