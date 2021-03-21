package es.unex.sextante.dataObjects;

/**
 * A simple interface for a database record.
 *
 * @author volaya
 *
 */
public interface IRecord {

   /**
    * Returns the value at a field
    *
    * @param iField
    *                the index of the field. zero-based
    * @return The value at the specified field
    */
   public Object getValue(int iField);


   /**
    * Returns an array with all the values of the record
    *
    * @return an array with all the values of the record
    */
   public Object[] getValues();

}
