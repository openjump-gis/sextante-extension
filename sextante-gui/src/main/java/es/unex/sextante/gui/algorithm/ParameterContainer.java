package es.unex.sextante.gui.algorithm;

import javax.swing.JComponent;

import es.unex.sextante.parameters.Parameter;

/**
 * A class representing a parameter key and the component used to introduce its value
 *
 * @author volaya
 *
 */
public class ParameterContainer {

   JComponent m_Container;
   Parameter  m_Parameter;


   public ParameterContainer(final Parameter parameter,
                             final JComponent container) {

      m_Parameter = parameter;
      m_Container = container;

   }


   public JComponent getContainer() {

      return m_Container;

   }


   public void setContainer(final JComponent container) {

      m_Container = container;

   }


   public String getName() {

      return m_Parameter.getParameterName();

   }


   public String getType() {

      return m_Parameter.getParameterTypeName();

   }


   public Parameter getParameter() {

      return m_Parameter;

   }

}
