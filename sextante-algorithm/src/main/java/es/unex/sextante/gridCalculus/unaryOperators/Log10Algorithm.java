package es.unex.sextante.gridCalculus.unaryOperators;

public class Log10Algorithm
         extends
            UnaryOperatorAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("log10()");

   }


   @Override
   protected double getProcessedValue() {

      return Math.log(m_dValue);

   }

}
