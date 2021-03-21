package es.unex.sextante.gui.modeler;

public class OutputParameterContainer {

   Object m_Container;
   String m_sParameterName;


   public OutputParameterContainer(final String sName,
                                   final Object container) {

      m_sParameterName = sName;
      m_Container = container;

   }


   public Object getContainer() {

      return m_Container;

   }


   public void setContainer(final Object container) {

      m_Container = container;

   }


   public String getName() {

      return m_sParameterName;

   }


   public void setName(final String sName) {

      m_sParameterName = sName;

   }


}
