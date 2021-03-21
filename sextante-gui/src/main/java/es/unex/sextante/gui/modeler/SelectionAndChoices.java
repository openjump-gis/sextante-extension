package es.unex.sextante.gui.modeler;

public class SelectionAndChoices {

   private String   m_Description;
   private String[] m_Choices;


   public SelectionAndChoices(final String parameterDescription,
                              final String[] choices) {

      m_Description = parameterDescription;
      m_Choices = choices;

   }


   public String getDescription() {

      return m_Description;

   }


   public void setDescription(final String sDescription) {

      m_Description = sDescription;

   }


   public String[] getChoices() {

      return m_Choices;

   }


   public void setChoices(final String[] sChoices) {

      m_Choices = sChoices;

   }


   @Override
   public String toString() {

      return getDescription();

   }
}
