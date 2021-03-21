package es.unex.sextante.gridCalculus.binaryOperators;

public class ModAlgorithm
         extends
            BinaryOperatorAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("Mod");

   }


   @Override
   protected double getProcessedValue() {

      return m_dValue % m_dValue2;

   }

}
