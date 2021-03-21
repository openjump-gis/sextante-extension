package es.unex.sextante.core;

/**
 * This interface must be implemented by all classes used to monitor the execution of a GeoAlgorithm or any other process within
 * SEXTANTE
 * 
 * @author volaya
 * 
 */
public interface ITaskMonitor {

   /**
    * Sets the current progress
    * 
    * @param iStep
    *                the current progress, in percentage (0-100)
    */
   public void setProgress(int iStep);


   /**
    * Sets the current progress
    * 
    * @param step
    *                the current step
    * @param totalNumberOfSteps
    *                the total number of steps in the task
    */
   public void setProgress(int step,
                           int totalNumberOfSteps);


   /**
    * Sets the current text to display by this task monitor
    * 
    * @param sText
    *                The text to display
    */
   public void setProgressText(String sText);


   /**
    * Returns rue is the task that is being monitored has been canceled using this monitor
    * 
    * @return true is the task that is being monitored has been canceled using this monitor
    */
   public boolean isCanceled();


   /**
    * Closes the monitor. This is called once the task has been finished
    */
   public void close();


   /**
    * Set whether the process is determinate or not
    * 
    * @param bDeterminate
    *                whether the process to monitor is determinate (the number of total steps to complete is known)
    */
   public void setDeterminate(boolean bDeterminate);


   /**
    * Sets the description of the process being monitored
    * 
    * @param sDescription
    *                the description of the process being monitored
    */
   public void setProcessDescription(String sDescription);


   /**
    * sets a prefix to be prepended to the description title. This can be used for processes that include several algorithms, so
    * when each algorithm sets its owns description, it will also contain a string indicating the part of the global process that
    * it represents. This method should, therefore, not be called by simple algorithms, but just from complex processes
    * 
    * @param sPrefix
    *                the prefix to prepend to the description title.
    */
   public void setDescriptionPrefix(String sPrefix);


}
