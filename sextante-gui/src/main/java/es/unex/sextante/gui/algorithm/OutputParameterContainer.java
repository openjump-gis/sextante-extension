package es.unex.sextante.gui.algorithm;

/**
 * A class representing a output object key and the component used to introduce its value (i.e. its associated filename)
 *
 * @author volaya
 *
 */
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
