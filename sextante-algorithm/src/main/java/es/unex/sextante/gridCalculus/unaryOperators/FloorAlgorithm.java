package es.unex.sextante.gridCalculus.unaryOperators;

public class FloorAlgorithm
         extends
            UnaryOperatorAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("floor()");

   }


   @Override
   protected double getProcessedValue() {

      return Math.floor(m_dValue);

   }

}
