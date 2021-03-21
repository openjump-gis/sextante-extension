package es.unex.sextante.gui.cmd.bshcommands;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.WrongViewNameException;

/**
 * A BeanShell command to open a file containing a layer or table
 * 
 * @author volaya
 * 
 */
public class open {

   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sFilename,
                             final String sName,
                             final String sViewName) {

      final IDataObject obj = SextanteGUI.getInputFactory().openDataObjectFromFile(sFilename);
      if (obj == null) {
         env.println("Could not open file: " + sFilename.trim());
      }
      else {
         obj.setName(sName);
         SextanteGUI.getInputFactory().addDataObject(obj);
         try {
            SextanteGUI.getGUIFactory().addToView(obj, sViewName);
         }
         catch (final WrongViewNameException e) {
            env.println("Wrong view name");
         }
      }

   }


}
