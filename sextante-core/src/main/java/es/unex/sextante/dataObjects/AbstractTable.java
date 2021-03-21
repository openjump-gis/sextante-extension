package es.unex.sextante.dataObjects;

/**
 * A convenience class which implements some of the methods of the ITable interface. Extending this class is recommended instead
 * of implementing the interface directly
 * 
 * @author volaya
 * 
 */
public abstract class AbstractTable
         implements
            ITable {


   public String[] getFieldNames() {

      final String[] names = new String[getFieldCount()];

      for (int i = 0; i < names.length; i++) {
         names[i] = getFieldName(i);
      }

      return names;

   }


   public Class[] getFieldTypes() {

      final Class[] types = new Class[getFieldCount()];

      for (int i = 0; i < types.length; i++) {
         types[i] = getFieldType(i);
      }

      return types;

   }


   public int getFieldIndexByName(final String sFieldName) {

      for (int i = 0; i < this.getFieldCount(); i++) {
         final String sName = getFieldName(i);
         if (sName.equals(sFieldName)) {
            return i;
         }
      }

      return -1;

   }


   @Override
   public String toString() {

      return this.getName();

   }


}
