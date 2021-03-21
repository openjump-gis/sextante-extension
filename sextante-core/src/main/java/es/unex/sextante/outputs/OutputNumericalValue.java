package es.unex.sextante.outputs;

import es.unex.sextante.core.Sextante;

/**
 * An output representing a numerical value
 * 
 * @author volaya
 * 
 */
public class OutputNumericalValue
         extends
            Output {

   @Override
   public String getCommandLineParameter() {
      return null;
   }


   @Override
   public void setOutputObject(final Object obj) {

      if (obj instanceof Number) {
         m_Object = obj;
      }

   }


   @Override
   public String getTypeDescription() {

      return Sextante.getText("Numerical_value");

   }

}
