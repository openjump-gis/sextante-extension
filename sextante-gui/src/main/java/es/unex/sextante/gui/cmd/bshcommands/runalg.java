package es.unex.sextante.gui.cmd.bshcommands;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import bsh.CallStack;
import bsh.EvalError;
import bsh.Interpreter;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.gui.cmd.Parser;
import es.unex.sextante.gui.core.GeoAlgorithmExecutors;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.AlgorithmNotFoundException;
import es.unex.sextante.gui.exceptions.CommandLineException;
import es.unex.sextante.gui.exceptions.OutputExtentNotSetException;
import es.unex.sextante.gui.history.History;

/**
 * A BeanShell command to execute SEXTANTE algorithms
 * 
 * @author volaya
 * 
 */
public class runalg {

   /**
    * BeanShell does not work well with varargs, so this methods are a quick (and dirty) solution for algorithms requiring up to
    * 10 parameters
    * 
    * @throws EvalError
    * 
    */

   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName) {
      final String[] sArgs = new String[0];
      invoke(env, callstack, sAlgName, sArgs);

   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName,
                             final Object p1) {
      final Object[] sArgs = { p1 };
      invoke(env, callstack, sAlgName, sArgs);

   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName,
                             final Object p1,
                             final Object p2) {
      final Object[] sArgs = { p1, p2 };
      invoke(env, callstack, sAlgName, sArgs);
   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName,
                             final Object p1,
                             final Object p2,
                             final Object p3) {
      final Object[] sArgs = { p1, p2, p3 };
      invoke(env, callstack, sAlgName, sArgs);
   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName,
                             final Object p1,
                             final Object p2,
                             final Object p3,
                             final Object p4) {
      final Object[] sArgs = { p1, p2, p3, p4 };
      invoke(env, callstack, sAlgName, sArgs);
   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName,
                             final Object p1,
                             final Object p2,
                             final Object p3,
                             final Object p4,
                             final Object p5) {
      final Object[] sArgs = { p1, p2, p3, p4, p5 };
      invoke(env, callstack, sAlgName, sArgs);
   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName,
                             final Object p1,
                             final Object p2,
                             final Object p3,
                             final Object p4,
                             final Object p5,
                             final Object p6) {
      final Object[] sArgs = { p1, p2, p3, p4, p5, p6 };
      invoke(env, callstack, sAlgName, sArgs);
   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName,
                             final Object p1,
                             final Object p2,
                             final Object p3,
                             final Object p4,
                             final Object p5,
                             final Object p6,
                             final Object p7) {
      final Object[] sArgs = { p1, p2, p3, p4, p5, p6, p7 };
      invoke(env, callstack, sAlgName, sArgs);
   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName,
                             final Object p1,
                             final Object p2,
                             final Object p3,
                             final Object p4,
                             final Object p5,
                             final Object p6,
                             final Object p7,
                             final Object p8) {
      final Object[] sArgs = { p1, p2, p3, p4, p5, p6, p7, p8 };
      invoke(env, callstack, sAlgName, sArgs);
   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName,
                             final Object p1,
                             final Object p2,
                             final Object p3,
                             final Object p4,
                             final Object p5,
                             final Object p6,
                             final Object p7,
                             final Object p8,
                             final Object p9) {
      final Object[] sArgs = { p1, p2, p3, p4, p5, p6, p7, p8, p9 };
      invoke(env, callstack, sAlgName, sArgs);
   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName,
                             final Object p1,
                             final Object p2,
                             final Object p3,
                             final Object p4,
                             final Object p5,
                             final Object p6,
                             final Object p7,
                             final Object p8,
                             final Object p9,
                             final Object p10) {
      final Object[] sArgs = { p1, p2, p3, p4, p5, p6, p7, p8, p9, p10 };
      invoke(env, callstack, sAlgName, sArgs);
   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName,
                             final Object p1,
                             final Object p2,
                             final Object p3,
                             final Object p4,
                             final Object p5,
                             final Object p6,
                             final Object p7,
                             final Object p8,
                             final Object p9,
                             final Object p10,
                             final Object p11) {
      final Object[] sArgs = { p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11 };
      invoke(env, callstack, sAlgName, sArgs);
   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName,
                             final Object p1,
                             final Object p2,
                             final Object p3,
                             final Object p4,
                             final Object p5,
                             final Object p6,
                             final Object p7,
                             final Object p8,
                             final Object p9,
                             final Object p10,
                             final Object p11,
                             final Object p12) {
      final Object[] sArgs = { p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12 };
      invoke(env, callstack, sAlgName, sArgs);
   }


   /**
    * Runs a geoalgorithm with a set of parameter values.
    * 
    * @param env
    *                the beanshell interpreter
    * @param callstack
    *                the call stack
    * @param sAlgName
    *                the command-line name of the algorithm
    * @param args
    *                the parameter values
    * @throws EvalError
    */
   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sAlgName,
                             final Object[] args) {

      final String[] sArgs = new String[args.length];
      for (int i = 0; i < args.length; i++) {
         sArgs[i] = args[i].toString();
      }
      try {
         final GeoAlgorithm alg = Parser.getAlgorithm(sAlgName, sArgs);
         try {
            final String[] cmd = alg.getAlgorithmAsCommandLineSentences();
            if (cmd != null) {
               History.addToHistory(cmd);
            }
            boolean bReturn = GeoAlgorithmExecutors.executeForCommandLine(alg, SextanteGUI.getLastCommandOriginParentDialog());
            if (!bReturn) {
               Sextante.addWarningToLog("Process canceled!");
               showError(env, "Process canceled!");
               return;
            }
            else {
               if (SextanteGUI.getLastCommandOrigin() == SextanteGUI.HISTORY) {
                  SextanteGUI.getGUIFactory().updateHistory();
               }
            }
         }
         catch (final GeoAlgorithmExecutionException e) {
            Sextante.addErrorToLog(e);
            showError(env, e.getMessage());
         }
      }
      catch (final AlgorithmNotFoundException e) {
         Sextante.addErrorToLog(e);
         showError(env, e.getMessage());
      }
      catch (final OutputExtentNotSetException e) {
         Sextante.addErrorToLog(e);
         showError(env, e.getMessage());
      }
      catch (final CommandLineException e) {
         Sextante.addErrorToLog(e);
         showError(env, e.getMessage());
         final GeoAlgorithm alg = Sextante.getAlgorithmFromCommandLineName(sAlgName);
         if (alg != null) {
            showError(env, alg.getCommandLineHelp());
         }
      }

   }


   private static void showError(final Interpreter env,
                                 final String sMessage) {

      if (SextanteGUI.getLastCommandOrigin() == SextanteGUI.HISTORY) {
         try {
            SwingUtilities.invokeAndWait(new Runnable() {
               public void run() {
                  JOptionPane.showMessageDialog(null, sMessage, Sextante.getText("Warning"), JOptionPane.ERROR_MESSAGE);
               }
            });
         }
         catch (final Exception e) {}

      }
      else {
         env.println(sMessage);
      }

   }


}
