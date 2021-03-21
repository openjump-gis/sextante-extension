package es.unex.sextante.additionalInfo;

/**
 * Additional information for a parameter representing a data object (layer or table)
 * 
 * @author Victor Olaya volaya@unex.es
 * 
 */
public abstract class AdditionalInfoDataObject
         implements
            AdditionalInfo {

   private boolean m_bIsMandatory = false;


   /**
    * Creates a new instance
    * 
    * @param bIsMandatory
    *                indicates whether the data object is mandatory (is needed to run the algorithm) or not
    */
   public AdditionalInfoDataObject(final boolean bIsMandatory) {

      m_bIsMandatory = bIsMandatory;

   }


   /**
    * Sets whether the data object is mandatory or not
    * 
    * @param bIsMandatory
    *                indicates wheteher the data object is mandatory or not
    */
   public void setIsMandatory(final boolean bIsMandatory) {

      m_bIsMandatory = bIsMandatory;

   }


   /**
    * Returns true if the data object is mandatory
    * 
    * @return true if the data object is mandatory
    */
   public boolean getIsMandatory() {

      return m_bIsMandatory;

   }

}
