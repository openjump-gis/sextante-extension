package es.unex.sextante.gui.cmd.bshcommands;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.gui.cmd.CommandLineData;

/**
 * A BeanShell command to activate or deactivate the automatic computation of analysis extents
 * 
 * @author volaya
 * 
 */
public class autoextent {

   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sBoolean) {

      if (sBoolean.trim().equals("true")) {
         CommandLineData.setAutoExtent(true);
      }
      else if (sBoolean.trim().equals("false")) {
         CommandLineData.setAutoExtent(false);
      }
      else {
         env.println("Wrong parameter: " + sBoolean);
      }

   }

}
