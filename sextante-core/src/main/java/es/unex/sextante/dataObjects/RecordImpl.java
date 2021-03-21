package es.unex.sextante.dataObjects;

/**
 * A simple implementation of the IRecord interface
 *
 * @author volaya
 *
 */
public class RecordImpl
         implements
            IRecord {

   private final Object m_Values[];


   /**
    * Creates a new record from an array of values
    *
    * @param values
    *                an array of values
    */
   public RecordImpl(final Object[] values) {

      m_Values = values;

   }


   public Object getValue(final int field) {

      return m_Values[field];

   }


   public Object[] getValues() {

      return m_Values;

   }

}
