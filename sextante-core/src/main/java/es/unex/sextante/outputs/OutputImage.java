

package es.unex.sextante.outputs;

import es.unex.sextante.core.Sextante;


/**
 * An output representing a chart
 * 
 * @author volaya
 * 
 */
public class OutputImage
         extends
            Output {

   @Override
   public String getCommandLineParameter() {
      return null;
   }


   @Override
   public void setOutputObject(final Object obj) {

      if (obj instanceof ImageContainer) {
         m_Object = obj;
      }

   }


   @Override
   public String getTypeDescription() {

      return Sextante.getText("image");

   }

}
