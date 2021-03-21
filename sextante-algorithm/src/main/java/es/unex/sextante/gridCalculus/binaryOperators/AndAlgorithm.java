package es.unex.sextante.gridCalculus.binaryOperators;

public class AndAlgorithm
         extends
            BinaryOperatorAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("AND");

   }


   @Override
   protected double getProcessedValue() {

      if ((m_dValue != 0) && (m_dValue2 != 0)) {
         return 1.0;
      }
      else {
         return 0.0;
      }

   }

}
