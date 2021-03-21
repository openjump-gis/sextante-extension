package es.unex.sextante.gui.cmd.bshcommands;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.gui.core.SextanteGUI;

public class close {

   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sName) {

      final Object obj = SextanteGUI.getInputFactory().getInputFromName(sName.trim());
      if (obj == null) {
         env.println("Invalid object: " + sName.trim());
      }
      else if (obj instanceof IDataObject) {
         final IDataObject layer = (IDataObject) obj;
         SextanteGUI.getInputFactory().close(layer.getName());
      }

   }

}
