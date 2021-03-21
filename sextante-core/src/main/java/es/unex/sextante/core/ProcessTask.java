package es.unex.sextante.core;

import java.util.concurrent.Callable;

import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;

/**
 * This task processes a geoalgorithm
 *
 * @author Victor Olaya volaya@unex.es
 *
 */
public class ProcessTask
         implements
            IMonitorableTask,
            Callable<Boolean> {

   private final boolean       m_bFinished = false;
   private final GeoAlgorithm  m_Algorithm;
   private final ITaskMonitor  m_ProgressMonitor;
   private final OutputFactory m_OutputFactory;


   /**
    * Creates a new process task
    *
    * @param algorithm
    *                the algorithm to process
    * @param outputFactory
    *                the output factory to use for executing the algorithm
    * @param taskMonitor
    *                the task monitor to use for monitoring algorithm execution
    */
   public ProcessTask(final GeoAlgorithm algorithm,
                      final OutputFactory outputFactory,
                      final ITaskMonitor taskMonitor) {

      m_Algorithm = algorithm;
      m_OutputFactory = outputFactory;
      m_ProgressMonitor = taskMonitor;

      if (taskMonitor != null) {
         m_ProgressMonitor.setProgress(0);
      }

   }


   /**
    * Starts the execution of the task
    *
    * @return true if the algorithm was executed completely, false if it was canceled
    * @throws GeoAlgorithmExecutionException
    *                 if there were problems during algorithm execution
    *
    */
   public Boolean call() throws GeoAlgorithmExecutionException {

      final boolean bReturn = m_Algorithm.execute(m_ProgressMonitor, m_OutputFactory);

      return new Boolean(bReturn);

   }


   /**
    * Returns true if the task has been canceled
    *
    * @return true if the algorithm has been canceled
    */
   public boolean isCanceled() {

      return m_ProgressMonitor.isCanceled();

   }


   /**
    * Returns true if the task has been finished
    *
    * @return true if the algorithm has been finished
    */
   public boolean isFinished() {

      return m_bFinished;

   }

}
