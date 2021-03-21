package es.unex.sextante.additionalInfo;

/**
 * Additional information for a parameter representing a band
 * 
 * @author Victor Olaya volaya@unex.es
 * 
 */
public class AdditionalInfoBand
         implements
            AdditionalInfo {

   private String m_sParentParameterName;


   /**
    * Creates a new instance
    * 
    * @param sParentParameterName
    *                the name of the parent parameter. This should be the name of a parameter representing a layer from which the
    *                band should be taken
    */
   public AdditionalInfoBand(final String sParentParameterName) {

      m_sParentParameterName = sParentParameterName;

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
    *                the name of the parent parameter
    */
   public void setParentParameterName(final String sParentParameterName) {

      m_sParentParameterName = sParentParameterName;

   }


   public String getTextDescription() {

      return "Parent parameter:" + m_sParentParameterName;

   }

}
