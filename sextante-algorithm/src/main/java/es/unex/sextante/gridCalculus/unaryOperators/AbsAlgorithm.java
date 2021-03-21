package es.unex.sextante.gridCalculus.unaryOperators;

public class AbsAlgorithm
         extends
            UnaryOperatorAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("abs()");

   }


   @Override
   protected double getProcessedValue() {

      return Math.abs(m_dValue);

   }

}
