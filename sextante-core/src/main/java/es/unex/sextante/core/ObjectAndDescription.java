package es.unex.sextante.core;

public class ObjectAndDescription
         implements
            Comparable {

   private String m_sDescription;
   private Object m_Object;


   public ObjectAndDescription(final String sDescription,
                               final Object object) {

      m_sDescription = sDescription;
      m_Object = object;

   }


   public Object getObject() {

      return m_Object;

   }


   public void setObject(final Object object) {

      m_Object = object;

   }


   public String getDescription() {

      return m_sDescription;

   }


   public void setDescription(final String sDescription) {

      m_sDescription = sDescription;

   }


   @Override
   public String toString() {

      return getDescription();

   }


   public int compareTo(final Object arg0) {

      return getDescription().compareTo(((ObjectAndDescription) arg0).getDescription());

   }


   @Override
   public boolean equals(final Object ob) {

      if (ob == null) {
         return false;
      }

      try {
         final ObjectAndDescription oad = (ObjectAndDescription) ob;
         boolean bObjsEqual;
         boolean bDescsEqual;
         if (oad.getObject() == null) {
            bObjsEqual = m_Object == null;
         }
         else {
            bObjsEqual = oad.getObject().equals(m_Object);
         }
         if (oad.getDescription() == null) {
            bDescsEqual = m_sDescription == null;
         }
         else {
            bDescsEqual = oad.getDescription().equals(m_sDescription);
         }
         return bObjsEqual && bDescsEqual;
      }
      catch (final Exception e) {
         return false;
      }

   }


}
