package es.unex.sextante.gridCalculus.unaryOperators;

public class CeilAlgorithm
         extends
            UnaryOperatorAlgorithm {

   @Override
   public void defineCharacteristics() {

      super.defineCharacteristics();

      setName("ceil()");

   }


   @Override
   protected double getProcessedValue() {

      return Math.ceil(m_dValue);

   }

}
