package es.unex.sextante.outputs;

import java.io.PrintStream;


public class StreamOutputChannel
         implements
            IOutputChannel {

   private PrintStream m_Stream;


   public String getAsCommandLineParameter() {

      return null;

   }


   public PrintStream getStream() {

      return m_Stream;

   }


   public void setStream(final PrintStream stream) {

      m_Stream = stream;

   }

}
