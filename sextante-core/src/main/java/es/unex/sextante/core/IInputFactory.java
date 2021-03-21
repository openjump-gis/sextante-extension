package es.unex.sextante.core;

import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.dataObjects.ILayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.parameters.RasterLayerAndBand;

/**
 * Interface to be implemented by all input factories An input factory is used by SEXTANTE to retrieve data objects from an
 * application.
 * 
 * If you want to integrate SEXTANTE graphical elements into your GIS app, it will need to prompt the user to select elements such
 * as layers or tables, so it needs to know which elements are available (ie. already opened in your application) The input
 * factory will create objects that implement the needed SEXTANTE interfaces from objects currently available in your application
 * 
 * Apart from wrapping data objects from the application, an input factory must also be able to open files and create data objects
 * from them. This will be used when calling the batch processing interface
 * 
 * Instead of implementing this interface, it is more convenient to extend
 * 
 * @see {@link AbstractInputFactory}
 * 
 * @author volaya
 * 
 */
public interface IInputFactory {

   /**
    * Creates the SEXTANTE data objects
    */
   public void createDataObjects();


   /**
    * Clears the set of SEXTANTE data objects
    */
   public void clearDataObjects();


   /**
    * Returns all objects in the set(layers and tables)
    * 
    * @return all objects in the set(layers and tables)
    */
   public IDataObject[] getDataObjects();


   /**
    * Adds a new data object to the ones already existing. This method should be called from post-process tasks, to incorporate
    * output object to the inputs list without having to create all the already existing objects again
    * 
    * @param obj
    *                the data object to add
    */
   public void addDataObject(IDataObject obj);


   /**
    * Returns the raster layers in the set
    * 
    * @return an array of raster layers
    */
   public IRasterLayer[] getRasterLayers();


   /**
    * Returns the 3D raster layers in the set
    * 
    * @return an array of 3D raster layers
    */
   public I3DRasterLayer[] get3DRasterLayers();


   /**
    * Returns all the vector layers of a particular type currently in the set
    * 
    * @param shapeType
    *                The type of vector layer. Use the constants defined in
    * @see {@link IVectorLayer}
    * @return an array of vector layers
    */
   public IVectorLayer[] getVectorLayers(int shapeType);


   /**
    * Returns the tables in the set
    * 
    * @return an array of tables
    */
   public ITable[] getTables();


   /**
    * Returns al the individual bands of the raster layers in the set
    * 
    * @return an array of bands
    */
   public RasterLayerAndBand[] getBands();


   /**
    * Returns the raster and vector layers in the set
    * 
    * @return an array of layers
    */
   public ILayer[] getLayers();


   /**
    * Returns the predefined extents available
    * 
    * @return an array of named extents
    */
   public NamedExtent[] getPredefinedExtents();


   /**
    * Returns an input object based on its name
    * 
    * @return the input object corresponding to the specified name. Returns null if no object with that name was found
    */
   public IDataObject getInputFromName(String value);


   /**
    * Returns an array of extensions that this InputFactory supports for opening raster layers
    * 
    * @return the supported extensions for raster layers
    */
   public abstract String[] getRasterLayerInputExtensions();


   /**
    * Returns an array of extensions that this InputFactory supports for opening vector layers
    * 
    * @return the supported extensions for vector layers
    */
   public abstract String[] getVectorLayerInputExtensions();


   /**
    * Returns an array of extensions that this InputFactory supports for opening tables
    * 
    * @return the supported extensions for tables
    */
   public abstract String[] getTableInputExtensions();


   /**
    * Returns an array of extensions that this InputFactory supports for opening 3D layers
    * 
    * @return the supported extensions for 3D layers
    */
   public String[] get3DRasterLayerInputExtensions();


   /**
    * Returns a data object created from the specified filename
    * 
    * @param filename
    *                the filename
    * @return a data object. null if could not create it from the specified file
    */
   public abstract IDataObject openDataObjectFromFile(String filename);


   /**
    * Closes (eliminates it from the application running SEXTANTE) a data object (table or layer) given its name. If it is not
    * opened in the application (for example, not being shown in a view or map), but it is contained in the input factory list of
    * objects (such as layers loaded using the open() method from the command-line interface), it should remove it from that list.
    * 
    * @param input
    *                the name of the data object to close
    */
   public abstract void close(String input);


}
