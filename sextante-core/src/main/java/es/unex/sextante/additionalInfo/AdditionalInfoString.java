package es.unex.sextante.additionalInfo;

public class AdditionalInfoString
         implements
            AdditionalInfo {

   private String  m_sDefaultString = "";


   //set to true if it's an attribute table field name (GRASS)
   private boolean m_bIsField       = false;


   public AdditionalInfoString() {}


   public String getDefaultString() {

      return m_sDefaultString;

   }


   public void setDefaultString(final String sDefaultString) {

      m_sDefaultString = sDefaultString;

   }


   public void setIsField(final boolean bIsField) {

      m_bIsField = bIsField;

   }


   public boolean getIsField() {

      return (m_bIsField);

   }


   public String getTextDescription() {
      // TODO Auto-generated method stub
      return null;
   }

}
