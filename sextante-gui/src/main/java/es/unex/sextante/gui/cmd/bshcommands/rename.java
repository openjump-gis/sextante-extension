package es.unex.sextante.gui.cmd.bshcommands;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.gui.core.SextanteGUI;

/**
 * A BeanShell command to rename a SEXTANTE data object
 *
 * @author volaya
 *
 */
public class rename {

   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sName,
                             final String sNewName) {

      final Object obj = SextanteGUI.getInputFactory().getInputFromName(sName.trim());
      if (obj == null) {
         env.println("Invalid object: " + sName.trim());
      }
      if (obj instanceof IDataObject) {
         ((IDataObject) obj).setName(sNewName);
      }

   }

}
