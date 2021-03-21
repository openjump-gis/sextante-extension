package es.unex.sextante.dataObjects;

/**
 * This is the base interface that all table objects have to implement to be able to be used by SEXTANTE algorithms.
 *
 * Instead of implementing this class directly, it is recommended to extend {@link AbstractTable}
 *
 * @author Victor Olaya. volaya@unex.es
 *
 */
public interface ITable
         extends
            IDataObject {

   /**
    * Creates a new table
    *
    * @param sName
    *                The name of the table
    * @param sFilename
    *                The filename associated with the table
    * @param types
    *                an array with attributes data types
    * @param sFields
    *                an array with attributes names
    */
   //	public void create(String sName,
   //						String sFilename,
   //						Class[] types,
   //						String[] sFields);
   /**
    * adds a new record to the table
    *
    * @param attributes
    *                the values of the record
    */
   public void addRecord(Object[] attributes);


   /**
    * Returns an iterator to iterate the table
    *
    * @return an iterator to iterate the table
    */
   public IRecordsetIterator iterator();


   /**
    * Returns the name of a field
    *
    * @param i
    *                the field. zero-based
    * @return The name of the specified field
    */
   public String getFieldName(int i);


   /**
    * Returns the data type of a field
    *
    * @param i
    *                the field. zero-based
    * @return The data type of the specified field
    */
   public Class getFieldType(int i);


   /**
    * Returns the total number of fields
    *
    * @return the total number of fields
    */
   public int getFieldCount();


   /**
    * Returns an array with data types of all the fields in the table
    *
    * @return an array with data types of all the fields in the table
    */
   public Class[] getFieldTypes();


   /**
    * Returns an array with the names of all the fields in the table
    *
    * @return an array with the names of all the fields in the table
    */
   public String[] getFieldNames();


   /**
    * Returns the total number of records(rows) in the table
    *
    * @return the total number of records(rows) in the table
    */
   public long getRecordCount();


   /**
    * Returns the index of a field from its name. Returns -1 if there is not a field with that name.
    *
    * @param fieldName
    *                the name of the field
    * @return the index of the given field
    */
   public int getFieldIndexByName(String fieldName);


}
