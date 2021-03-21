package es.unex.sextante.gridCalculus.binaryOperators;

public class NotEqAlgorithm
         extends
            BinaryOperatorAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("!=");

   }


   @Override
   protected double getProcessedValue() {

      if (m_dValue != m_dValue2) {
         return 1.0;
      }
      else {
         return 0.0;
      }

   }

}
