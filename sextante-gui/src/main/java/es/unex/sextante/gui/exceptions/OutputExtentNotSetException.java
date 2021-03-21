package es.unex.sextante.gui.exceptions;

public class OutputExtentNotSetException
         extends
            CommandLineException {

   public OutputExtentNotSetException() {

      super("Could not run algorithm. Output extent not set.");

   }

}
