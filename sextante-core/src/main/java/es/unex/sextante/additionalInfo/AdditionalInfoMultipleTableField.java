

package es.unex.sextante.additionalInfo;

public class AdditionalInfoMultipleTableField
         implements
            AdditionalInfo {

   private String  m_sParentParameterName;
   private boolean m_bIsMandatory;


   /**
    * 
    * @param sParentParameterName
    *                The name of the parent parameter
    * @param bIsMandatory
    *                specifies whether the field is mandatory or not
    */
   public AdditionalInfoMultipleTableField(final String sParentParameterName,
                                           final boolean bIsMandatory) {

      m_sParentParameterName = sParentParameterName;
      m_bIsMandatory = bIsMandatory;

   }


   /**
    * Returns the name of the parent parameter
    * 
    * @return the name of the parent parameter
    */
   public String getParentParameterName() {

      return m_sParentParameterName;

   }


   /**
    * Sets the name of the parent parameter
    * 
    * @param sParentParameterName
    *                The name of the parent parameter
    */
   public void setParentParameterName(final String sParentParameterName) {

      m_sParentParameterName = sParentParameterName;

   }


   /**
    * Sets whether the field is mandatory or not
    * 
    * @param bIsMandatory
    *                indicates whether the field is mandatory or not
    */
   public void setIsMandatory(final boolean bIsMandatory) {

      m_bIsMandatory = bIsMandatory;

   }


   /**
    * Returns true if the field is mandatory
    * 
    * @return true if the field is mandatory
    */
   public boolean getIsMandatory() {

      return m_bIsMandatory;

   }


   public String getTextDescription() {
      // TODO Auto-generated method stub
      return null;
   }

}
