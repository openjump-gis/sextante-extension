package es.unex.sextante.additionalInfo;

public class AdditionalInfoSelection
         implements
            AdditionalInfo {

   private final String[] m_sValues;


   public AdditionalInfoSelection(final String[] sValues) {

      m_sValues = sValues;
   }


   public String[] getValues() {

      return m_sValues;

   }


   public String getTextDescription() {
      // TODO Auto-generated method stub
      return null;
   }

}
