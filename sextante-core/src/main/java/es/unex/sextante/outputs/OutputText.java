package es.unex.sextante.outputs;

import es.unex.sextante.core.Sextante;

/**
 * An output representing a HTML-formatted text
 *
 * @author volaya
 *
 */
public class OutputText
         extends
            Output {

   @Override
   public String getCommandLineParameter() {
      return null;
   }


   @Override
   public void setOutputObject(final Object obj) {

      if (obj instanceof String) {
         m_Object = obj;
      }

   }


   @Override
   public String getTypeDescription() {

      return Sextante.getText("text");

   }

}
