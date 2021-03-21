package es.unex.sextante.core;

/**
 * A task monitor that does not show task progress or allows for user interaction
 * 
 * @author volaya
 * 
 */
public class SilentTaskMonitor
         implements
            ITaskMonitor {

   /**
    * Creates a new silent monitor
    */
   public SilentTaskMonitor() {}


   public boolean isCanceled() {

      return false;

   }


   public void setProgress(final int iStep) {}


   public void setProgressText(final String sText) {}


   public void close() {}


   public void setProgress(final int step,
                           final int totalNumberOfSteps) {}


   public void setDeterminate(final boolean determinate) {}


   public void setProcessDescription(final String description) {}


   public void setDescriptionPrefix(final String prefix) {}

}
