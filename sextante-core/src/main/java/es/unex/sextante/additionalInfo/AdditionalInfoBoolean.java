package es.unex.sextante.additionalInfo;

/**
 * Additional information for a parameter representing a boolean value
 * 
 * @author Victor Olaya volaya@unex.es
 * 
 */
public class AdditionalInfoBoolean
         implements
            AdditionalInfo {

   private boolean m_bDefaultValue = false;


   /**
    * Creates a new instance
    * 
    * @param bDefault
    *                the default value of the boolean parameter
    */
   public AdditionalInfoBoolean(final boolean bDefault) {

      m_bDefaultValue = bDefault;

   }


   /**
    * Sets a new default value for the associated boolean parameter
    * 
    * @param bDefault
    *                the new default value
    */
   public void setDefaultValue(final boolean bDefault) {

      m_bDefaultValue = bDefault;

   }


   /**
    * Returns the current default value
    * 
    * @return the default value
    */
   public boolean getDefaultValue() {

      return m_bDefaultValue;

   }


   public String getTextDescription() {

      return "";

   }

}
