package es.unex.sextante.gui.exceptions;

import es.unex.sextante.core.Sextante;

public class WrongGrassWinShellException
         extends
            Exception {

   public WrongGrassWinShellException() {

      super(Sextante.getText("grass_error_win_shell_binary") + "\n" + Sextante.getText("grass_shell_url"));

   }

}
