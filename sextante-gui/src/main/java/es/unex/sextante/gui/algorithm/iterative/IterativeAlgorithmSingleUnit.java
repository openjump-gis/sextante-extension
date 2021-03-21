package es.unex.sextante.gui.algorithm.iterative;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ITaskMonitor;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ProcessTask;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputNumericalValue;

/**
 * A task representing a single algorithm execution as a part of a batch process. The process is comprised of several single units
 * like the one represented by this class
 * 
 * @author volaya
 * 
 */
public class IterativeAlgorithmSingleUnit
         implements
            Callable<Boolean> {

   private final GeoAlgorithm                               m_Algorithm;
   private StringBuffer                                     m_sLog;
   private final ITaskMonitor                               m_TaskMonitor;
   private final String                                     m_sUnitName;
   private final ArrayList<ArrayList<OutputNumericalValue>> m_NumericalOutputs;


   /**
    * 
    * @param alg
    *                the algorithm to execute
    * @param monitor
    *                the task monitor to use
    * @param sUnitName
    *                the name of the unit to be executed
    * @param numericalOutputs
    *                an array list to collect numerical outputs produced by the algorithm
    */
   public IterativeAlgorithmSingleUnit(final GeoAlgorithm alg,
                                       final ITaskMonitor monitor,
                                       final String sUnitName,
                                       final ArrayList<ArrayList<OutputNumericalValue>> numericalOutputs) {

      m_Algorithm = alg;
      m_TaskMonitor = monitor;
      m_sUnitName = sUnitName;
      m_NumericalOutputs = numericalOutputs;

   }


   /**
    * Executes the algorithm
    * 
    * @return true if the algorithm was not canceled
    * @throws GeoAlgorithmExecutionException
    */
   public Boolean call() throws GeoAlgorithmExecutionException {

      final ExecutorService pool = Executors.newFixedThreadPool(1);
      final Future<Boolean> p = pool.submit(new ProcessTask(m_Algorithm, SextanteGUI.getOutputFactory(), m_TaskMonitor));
      try {
         final Boolean success = p.get();
         if ((success != null) && success.booleanValue()) {
            changeOutputDescriptions();
            collectIndividualNumericalOutputs();
            final Runnable postProcess = SextanteGUI.getPostProcessTask(m_Algorithm, false);
            if (postProcess != null) {
               postProcess.run();
            }
            return true;
         }
         else {
            return false;
         }

      }
      catch (final Exception e) {
         throw new GeoAlgorithmExecutionException(e.getMessage());
      }
   }


   private void collectIndividualNumericalOutputs() {

      final ArrayList<OutputNumericalValue> numericalOutputs = new ArrayList<OutputNumericalValue>();
      final OutputObjectsSet ooset = m_Algorithm.getOutputObjects();
      for (int i = 0; i < ooset.getOutputObjectsCount(); i++) {
         final Output out = ooset.getOutput(i);
         if (out instanceof OutputNumericalValue) {
            numericalOutputs.add((OutputNumericalValue) out);
         }
      }

      m_NumericalOutputs.add(numericalOutputs);

   }


   private void changeOutputDescriptions() {

      final OutputObjectsSet ooset = m_Algorithm.getOutputObjects();
      for (int i = 0; i < ooset.getOutputObjectsCount(); i++) {
         final Output out = ooset.getOutput(i);
         if (!(out instanceof OutputNumericalValue)) {
            out.setDescription(out.getDescription() + "[" + m_sUnitName + "]");
         }
      }

   }


   /**
    * Returns a string with information about the executed process
    * 
    * @return a string with information about the executed process
    */
   public String getLog() {

      return m_sLog.toString();

   }

}
