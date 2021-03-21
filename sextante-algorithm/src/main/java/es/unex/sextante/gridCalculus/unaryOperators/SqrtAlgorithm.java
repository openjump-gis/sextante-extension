package es.unex.sextante.gridCalculus.unaryOperators;

public class SqrtAlgorithm
         extends
            UnaryOperatorAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("sqrt()");

   }


   @Override
   protected double getProcessedValue() {

      return Math.sqrt(m_dValue);

   }

}
