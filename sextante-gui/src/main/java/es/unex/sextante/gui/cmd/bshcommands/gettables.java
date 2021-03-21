package es.unex.sextante.gui.cmd.bshcommands;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.gui.core.SextanteGUI;

public class gettables {

   public static String[] invoke(final Interpreter env,
                                 final CallStack callstack) {

      final ITable[] tables = SextanteGUI.getInputFactory().getTables();
      final String[] sNames = new String[tables.length];
      for (int i = 0; i < tables.length; i++) {
         sNames[i] = tables[i].getName();
      }

      return sNames;

   }

}
