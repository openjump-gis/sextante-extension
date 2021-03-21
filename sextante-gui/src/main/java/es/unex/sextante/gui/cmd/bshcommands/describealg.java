package es.unex.sextante.gui.cmd.bshcommands;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;

/**
 * A BeanShell command to describe SEXTANTE algorithms
 *
 * @author volaya
 *
 */
public class describealg {

   /**
    * Describes a geoalgorithm.
    *
    * @param env
    *                the beanshell interpreter
    * @param callstack
    *                the call stack
    * @param sAlgName
    *                the command-line name of the algorithm
    */
   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName) {

      final GeoAlgorithm alg = Sextante.getAlgorithmFromCommandLineName(sAlgName);
      if (alg != null) {
         env.println(alg.getCommandLineHelp());
      }
      else {
         env.println("Algoritmo_no_encontrado");
      }


   }

}
