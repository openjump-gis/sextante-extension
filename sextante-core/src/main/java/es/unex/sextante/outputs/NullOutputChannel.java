package es.unex.sextante.outputs;

public class NullOutputChannel
         implements
            IOutputChannel {

   @Override
   public String getAsCommandLineParameter() {

      return "!";

   }

}
