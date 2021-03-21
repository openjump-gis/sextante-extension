package es.unex.sextante.exceptions;

import es.unex.sextante.core.Sextante;

public class WrongInputException
         extends
            Exception {

   public WrongInputException() {

      super(Sextante.getText("Wrong_or_missing_parameters_definition"));

   }


   public WrongInputException(final String s) {

      super(s);

   }

}
