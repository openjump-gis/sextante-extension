package es.unex.sextante.gridCalculus.unaryOperators;

public class LnAlgorithm
         extends
            UnaryOperatorAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("ln()");

   }


   @Override
   protected double getProcessedValue() {

      return Math.log(m_dValue);

   }

}
