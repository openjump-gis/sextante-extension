package es.unex.sextante.gui.cmd.bshcommands;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterSelection;

/**
 * A command to show method values for selection parameters needed to run a geoalgorithm
 *
 * @author volaya
 *
 */
public class options {

   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName) {

      String s;
      final StringBuffer sb = new StringBuffer();

      final GeoAlgorithm alg = Sextante.getAlgorithmFromCommandLineName(sAlgName);

      if (alg == null) {
         s = "Algorithm not found!";
      }
      else {
         final ParametersSet ps = alg.getParameters();
         for (int i = 0; i < ps.getNumberOfParameters(); i++) {
            final Parameter param = ps.getParameter(i);
            if (param instanceof ParameterSelection) {
               try {
                  final AdditionalInfoSelection ai = (AdditionalInfoSelection) param.getParameterAdditionalInfo();
                  final String[] values = ai.getValues();
                  sb.append(param.getParameterName() + "(" + param.getParameterDescription() + ")\n");
                  for (int j = 0; j < values.length; j++) {
                     sb.append("\t * " + Integer.toString(j) + " : " + values[j] + "\n");
                  }
               }
               catch (final NullParameterAdditionalInfoException e) {}
            }
         }

         s = sb.toString();

         if (s.equals("")) {
            s = "No selection parameters in this algorithm";
         }
      }

      env.println(s);

   }

}
