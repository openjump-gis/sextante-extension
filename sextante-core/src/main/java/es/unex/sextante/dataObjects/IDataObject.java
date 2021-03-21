package es.unex.sextante.dataObjects;

import es.unex.sextante.outputs.IOutputChannel;

/**
 * Interface for data objects (layers and tables). This interface should be used to wrap other data objects, so they are
 * compatible with SEXTANTE and thus can be used as inputs to geoalgorithms
 * 
 * @author Victor Olaya volaya@unex.es
 * 
 */
public interface IDataObject {

   /**
    * Returns the base data object (i.e. the object that this class wraps, which contains the data itself)
    * 
    * @return the base data object
    */
   public Object getBaseDataObject();


   /**
    * Returns the name of this data object
    * 
    * @return the name of this data object
    */
   public String getName();


   /**
    * Sets a new name for this object
    * 
    * @param sName
    *                the new name
    */
   public void setName(String sName);


   /**
    * Returns the channel associated to this data object.
    * 
    * @return the channel associated to this data object.
    */
   public IOutputChannel getOutputChannel();


   /**
    * This method post-processes the object after it has been created. If, for instance, data are kept in memory before they are
    * dumped to file, this method should write that file.
    */
   public void postProcess() throws Exception;


   /**
    * This methods initialize the data object, so it is ready to be accessed
    */
   public void open();


   /**
    * This method closes the data object, which was opened using the open() method.
    */
   public void close();


   /**
    * This method deallocates memory used by the data object. Calling this method might render the object unusable for querying,
    * even if the open() method is called again, so call it it use only if you are sure that the object will no longer be used.
    */
   public void free();

}
