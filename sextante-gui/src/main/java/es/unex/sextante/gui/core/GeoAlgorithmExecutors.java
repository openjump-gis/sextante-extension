package es.unex.sextante.gui.core;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ITaskMonitor;
import es.unex.sextante.core.ProcessTask;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.gui.algorithm.iterative.IterativeAlgorithmMonitorableTask;

/**
 * This class contains convenience methods to call geoalgorithms from the SEXTANTE GUI
 * 
 * @author volaya
 * 
 */
public class GeoAlgorithmExecutors {

   //private static ExecutorService pool;

   static {
      //pool = Executors.newFixedThreadPool(2);
   }


   /**
    * Executes an algorithm in a new thread and then gets the results and puts them into the GUI using the current post process
    * task. The algorithm must already have valid parameter values.
    * 
    * @param alg
    *                the algorithm to execute
    */
   public static void execute(final GeoAlgorithm alg,
                              final JDialog parent) {

      final Runnable run = new Runnable() {

         public void run() {
            ITaskMonitor tm = SextanteGUI.getOutputFactory().getTaskMonitor(alg.getName(), alg.isDeterminatedProcess(), parent);
            ProcessTask task = new ProcessTask(alg, SextanteGUI.getOutputFactory(), tm);
            final ExecutorService pool = Executors.newFixedThreadPool(1);
            Future<Boolean> future = pool.submit(task);
            Boolean success = null;
            try {
               success = future.get();
            }
            catch (InterruptedException e) {
               Sextante.addErrorToLog(e);
            }
            catch (final ExecutionException e) {
               tm.close();
               Sextante.addErrorToLog(e);
               try {
                  SwingUtilities.invokeAndWait(new Runnable() {
                     public void run() {
                        JOptionPane.showMessageDialog(null, e.getMessage(), Sextante.getText("Error"), JOptionPane.ERROR_MESSAGE);
                     }
                  });
               }
               catch (Exception ex) {}
               return;
            }
            tm.close();
            if ((success != null) && success.booleanValue()) {
               Runnable postProcess = SextanteGUI.getPostProcessTask(alg, true);
               if (postProcess != null) {
                  Thread th = new Thread(postProcess);
                  th.start();
               }
            }
         }
      };

      final Thread th = new Thread(run);
      th.start();

   }


   /**
    * Executes an algorithm in a new thread and then gets the results and puts them into the GUI using the current post process
    * task. The algorithm must already have valid parameter values. Since this can be used to call several algorithms in a script,
    * it does not perform the post-process task in a separate thread, but waits instead for its completion.
    * 
    * @param alg
    *                the algorithm to execute
    * @return true if execution went OK and other algorithms after this one should be executed
    */
   public static boolean executeForCommandLine(final GeoAlgorithm alg,
                                               final JDialog parent) throws GeoAlgorithmExecutionException {

      final ITaskMonitor tm = SextanteGUI.getOutputFactory().getTaskMonitor(alg.getName(), alg.isDeterminatedProcess(), parent);
      final ExecutorService pool = Executors.newFixedThreadPool(1);
      final Future<Boolean> p = pool.submit(new ProcessTask(alg, SextanteGUI.getOutputFactory(), tm));
      try {
         final Boolean success = p.get();
         tm.close();
         if ((success != null) && success.booleanValue()) {
            final Runnable postProcess = SextanteGUI.getPostProcessTask(alg, false);
            if (postProcess != null) {
               postProcess.run();
               SextanteGUI.getInputFactory().clearDataObjects();
               SextanteGUI.getInputFactory().createDataObjects();
            }
            return success.booleanValue();
         }
         return false;

      }
      catch (final Exception e) {
         tm.close();
         Sextante.addErrorToLog(e);
         throw new GeoAlgorithmExecutionException(e.getMessage());
      }


   }


   public static void executeIterative(final GeoAlgorithm alg,
                                       final JDialog parent,
                                       final String sParameterName) throws GeoAlgorithmExecutionException {

      final IterativeAlgorithmMonitorableTask task = new IterativeAlgorithmMonitorableTask(alg, parent, sParameterName);
      final Thread th = new Thread(task);
      th.start();

   }


}
