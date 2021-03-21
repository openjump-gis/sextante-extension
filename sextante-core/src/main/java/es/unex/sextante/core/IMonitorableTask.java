package es.unex.sextante.core;

/**
 * A task that can be monitored
 *
 * @author Victor Olaya volaya@unex.es
 *
 */
public interface IMonitorableTask {

   /**
    * Returns true if tha task has been canceled
    *
    * @return true if the task has been canceled
    */
   public boolean isCanceled();


   /**
    * Returns true is the task is finished and is not running anymore
    *
    * @return true is the task is finished and is not running anymore
    */
   public boolean isFinished();

}
