package es.unex.sextante.gui.cmd.bshcommands;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.gui.core.SextanteGUI;

/**
 * A BeanShell command to set the nodata value of a raster layer
 *
 * @author volaya
 *
 */
public class setnodata {

   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sName,
                             final double dNoDataValue) {

      final Object obj = SextanteGUI.getInputFactory().getInputFromName(sName.trim());
      if (obj == null) {
         env.println("Invalid object: " + sName.trim());
      }
      else if (obj instanceof IRasterLayer) {
         final IRasterLayer layer = (IRasterLayer) obj;
         layer.setNoDataValue(dNoDataValue);
      }
      else {
         env.println("Object must be a raster layer");
      }

   }

}
