package es.unex.sextante.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;

/**
 * A Handler that outputs to the SEXTANTE log file, and also provides some additional methods to access log messages.
 */
public class SextanteLogHandler {

   FileOutputStream      fileOutputStream;
   PrintWriter           printWriter;
   ArrayList<LogElement> logElements = new ArrayList<LogElement>();


   //ArrayList<LogElement> warnings = new ArrayList<LogElement>();
   //ArrayList<LogElement> errors   = new ArrayList<LogElement>();


   public SextanteLogHandler() {
      super();

      try {
         fileOutputStream = new FileOutputStream(getLogFile());
         printWriter = new PrintWriter(fileOutputStream);
         printWriter.println("----Beginning of log file----");
         printWriter.flush();
         printWriter.close();
      }
      catch (final Exception e) {
         e.printStackTrace();
      }

   }


   private void publish(final String sMessage) {

      Writer output = null;

      try {
         output = new BufferedWriter(new FileWriter(getLogFile(), true));
         output.write(sMessage);
         output.flush();
      }
      catch (final IOException e) {
         //Sextante.addErrorToLog(e);
      }
      finally {
         if (output != null) {
            try {
               output.close();
            }
            catch (final IOException e) {
               //Sextante.addErrorToLog(e);
            }
         }
      }

   }


   public void addError(final String sMessage) {

      final Date todaysDate = new java.util.Date();
      final LogElement le = new LogElement(todaysDate, sMessage, LogElement.ERROR);
      publish(le.toString());
      logElements.add(le);

   }


   public void addError(final Throwable e) {

      e.printStackTrace();

      final StringBuffer sb = new StringBuffer(e.toString());
      StackTraceElement[] trace = e.getStackTrace();
      for (int i = 0; i < trace.length; i++) {
         sb.append("\tat " + trace[i] + "\n");
      }
      final Throwable ourCause = e.getCause();
      sb.append("Caused by: " + this);
      if (ourCause != null) {
         trace = ourCause.getStackTrace();
         for (int i = 0; i < trace.length; i++) {
            sb.append("\tat " + trace[i] + "\n");
         }
      }

      final Date todaysDate = new java.util.Date();
      final LogElement le = new LogElement(todaysDate, sb.toString(), e.toString(), LogElement.ERROR);
      publish(le.getAsText());
      logElements.add(le);

   }


   public void addWarning(final String sMessage) {

      final Date todaysDate = new java.util.Date();
      final LogElement le = new LogElement(todaysDate, sMessage, LogElement.WARNING);
      publish(le.getAsText());
      logElements.add(le);

   }


   public void addInfo(final String sMessage) {

      final Date todaysDate = new java.util.Date();
      final LogElement le = new LogElement(todaysDate, sMessage, LogElement.INFO);
      publish(le.getAsText());
      logElements.add(le);

   }


   public void close() throws SecurityException {
      printWriter.close();
   }


   public ArrayList<LogElement> getLogElements() {

      return logElements;

   }


   private String getLogFile() {

      final File sextante = new File(System.getProperty("user.home"), "sextante");
      if (!sextante.exists()) {
         sextante.mkdir();
      }

      return new File(sextante, "sextante.log").getAbsolutePath();

   }


   public void clear() {

      Writer output = null;

      logElements.clear();

      try {
         output = new BufferedWriter(new FileWriter(getLogFile(), false));
         output.flush();
      }
      catch (final IOException e) {
         //Sextante.addErrorToLog(e);
      }
      finally {
         if (output != null) {
            try {
               output.close();
            }
            catch (final IOException e) {
               //Sextante.addErrorToLog(e);
            }
         }
      }

   }


   public void addToLog(final String sMessage,
                        final String sType,
                        final String sShortMessage) {

      final Date todaysDate = new java.util.Date();
      final LogElement le = new LogElement(todaysDate, sMessage, sShortMessage, sType);
      publish(le.getAsText());
      logElements.add(le);

   }

}
