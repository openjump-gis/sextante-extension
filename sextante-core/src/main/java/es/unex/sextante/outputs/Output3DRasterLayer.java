package es.unex.sextante.outputs;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.I3DRasterLayer;

/**
 * An output representing a 3D raster layer
 * 
 * @author volaya
 * 
 */
public class Output3DRasterLayer
         extends
            Output {


   @Override
   public void setOutputObject(final Object obj) {

      if ((obj instanceof I3DRasterLayer) || (obj == null)) {
         m_Object = obj;
      }

   }


   @Override
   public String getCommandLineParameter() {

      if (m_OutputChannel == null) {
         return "\"#\"";
      }
      else {
         return "\"" + m_OutputChannel.getAsCommandLineParameter() + "\"";
      }

   }


   @Override
   public Output getNewInstance() {

      final Output out = super.getNewInstance();
      //((Output3DRasterLayer) out).setNumberOfBands(m_iNumberOfBands);

      return out;

   }


   @Override
   public void setObjectData(final Output output) {

      super.setObjectData(output);

   }


   @Override
   public String getTypeDescription() {

      return Sextante.getText("3Draster");

   }

}
