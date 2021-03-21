package es.unex.sextante.gui.exceptions;

import es.unex.sextante.core.Sextante;

public class WrongSettingValuesException
         extends
            Exception {

   public WrongSettingValuesException() {

      super(Sextante.getText("WrongSettingValues"));

   }

}
