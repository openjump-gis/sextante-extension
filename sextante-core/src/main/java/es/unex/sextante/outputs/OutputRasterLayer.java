package es.unex.sextante.outputs;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;

/**
 * An output representing a raster layer
 * 
 * @author volaya
 * 
 */
public class OutputRasterLayer
         extends
            Output {

   public static final int NUMBER_OF_BANDS_UNDEFINED = -1;

   private int             m_iNumberOfBands          = NUMBER_OF_BANDS_UNDEFINED;


   @Override
   public void setOutputObject(final Object obj) {

      if ((obj instanceof IRasterLayer) || (obj == null)) {
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


   /**
    * Returns the number of bands that this output raster layer will have
    * 
    * @return the number of layers of the output raster layer
    */
   public int getNumberOfBands() {

      return m_iNumberOfBands;

   }


   /**
    * Set the number of bands of this output raster layer. If the number is lower that one, the number of bands is set to
    * undefined.
    * 
    * @param numberOfBands
    *                the number of bands this output raster layer will have
    */
   public void setNumberOfBands(int numberOfBands) {

      if (numberOfBands < 1) {
         numberOfBands = NUMBER_OF_BANDS_UNDEFINED;
      }
      m_iNumberOfBands = numberOfBands;

   }


   @Override
   public Output getNewInstance() {

      final Output out = super.getNewInstance();
      ((OutputRasterLayer) out).setNumberOfBands(m_iNumberOfBands);

      return out;

   }


   @Override
   public void setObjectData(final Output output) {

      super.setObjectData(output);
      if (output instanceof OutputRasterLayer) {
         this.setNumberOfBands(((OutputRasterLayer) output).getNumberOfBands());
      }

   }


   @Override
   public String getTypeDescription() {

      return Sextante.getText("raster");

   }

}
