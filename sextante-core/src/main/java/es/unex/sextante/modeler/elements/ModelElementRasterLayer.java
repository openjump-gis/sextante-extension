package es.unex.sextante.modeler.elements;


public class ModelElementRasterLayer
         implements
            IModelElement {

   public static final int NUMBER_OF_BANDS_UNDEFINED = -1;
   private int             m_iNumberOfBands          = NUMBER_OF_BANDS_UNDEFINED;


   public int getNumberOfBands() {

      return m_iNumberOfBands;

   }


   public void setNumberOfBands(int numberOfBands) {

      if (numberOfBands < 1) {
         numberOfBands = NUMBER_OF_BANDS_UNDEFINED;
      }
      m_iNumberOfBands = numberOfBands;

   }


   @Override
   public String toString() {

      return this.getClass().toString() + "," + Integer.toString(m_iNumberOfBands);

   }

}
