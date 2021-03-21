package es.unex.sextante.gui.r;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.settings.SextanteRSettings;


public class RUtils {

   private static ArrayList<String> m_ConsoleResults = new ArrayList<String>();
   private static ArrayList<String> m_VerboseCommands;
   private static boolean           m_AddConsoleOutput;


   private static void createBatchJob() {

      final String sFilename = getBatchJobFilename();
      final String sRFolder = SextanteGUI.getSettingParameterValue(SextanteRSettings.R_FOLDER);
      try {
         final BufferedWriter output = new BufferedWriter(new FileWriter(sFilename));
         if (Sextante.isWindows()) {
            //output.write("echo off\n");
            output.write("\"" + sRFolder + File.separator + "bin" + File.separator + "R.exe\" CMD BATCH --vanilla \""
                         + getRScriptFilename() + "\"");
         }
         else {
            //TODO:******************
         }
         output.close();
      }
      catch (final IOException e) {
         Sextante.addErrorToLog(e);
      }


   }


   private static String getBatchJobFilename() {

      String sFile;

      if (Sextante.isUnix() || Sextante.isMacOSX()) {
         sFile = "r_batch_job.sh";
      }
      else {//Windows
         sFile = "r_batch_job.bat";
      }

      sFile = SextanteGUI.getUserFolder() + File.separator + sFile;

      return sFile;

   }


   public static void createRScriptFromRCommands(final String[] sCommands) {

      final String sFilename = getRScriptFilename();
      try {
         final BufferedWriter output = new BufferedWriter(new FileWriter(sFilename));
         for (int i = 0; i < sCommands.length; i++) {
            output.write(sCommands[i] + "\n");
         }
         output.close();
      }
      catch (final IOException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private static String getRScriptFilename() {

      final String sFile = SextanteGUI.getUserFolder() + File.separator + "sextante_script.r";

      return sFile;

   }


   private static String getConsoleOutputFilename() {

      final String sFile = SextanteGUI.getUserFolder() + File.separator + "sextante_script.r.Rout";

      return sFile;

   }


   public static int executeR(final RAlgorithm alg) throws GeoAlgorithmExecutionException {

      m_ConsoleResults.clear();
      m_VerboseCommands = alg.getVerboseCommands();
      m_AddConsoleOutput = false;
      createBatchJob();
      createRScriptFromRCommands(alg.getFullSetOfRCommands());
      final List<String> list = new ArrayList<String>();
      ProcessBuilder pb;
      pb = new ProcessBuilder(list);
      if (Sextante.isUnix() || Sextante.isMacOSX()) {
         //TODO
      }
      else { //windows
         list.add("cmd.exe");
         list.add("/C");
         //list.add("start");
         list.add(getBatchJobFilename());
      }

      Process process;
      try {
         process = pb.start();
         final StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
         final StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
         errorGobbler.start();
         outputGobbler.start();
         final int iReturn = process.waitFor();
         createConsoleOutput();
         return iReturn;
      }
      catch (final Exception e) {
         throw new RExecutionException();
      }


   }


   private static void createConsoleOutput() {

      BufferedReader input = null;
      try {
         input = new BufferedReader(new FileReader(getConsoleOutputFilename()));
         String sLine = null;
         while ((sLine = input.readLine()) != null) {
            processLine(sLine);
         }
      }
      catch (final FileNotFoundException e) {}
      catch (final IOException e) {}
      finally {
         try {
            if (input != null) {
               input.close();
            }
         }
         catch (final IOException e) {
            Sextante.addErrorToLog(e);
         }
      }

   }


   public static void setExecutable(final String sFilename) throws RExecutionException {

      final String version = System.getProperty("java.version").substring(0, 3);
      final Float f = Float.valueOf(version);
      if (f.floatValue() < (float) 1.6) {
         if (Sextante.isUnix() || Sextante.isMacOSX()) {
            try {
               Runtime.getRuntime().exec("chmod +x " + sFilename);
            }
            catch (final IOException e) {
               throw new RExecutionException();
            }
         }
      }
      else {
         if (Sextante.isUnix() || Sextante.isMacOSX()) {
            new File(sFilename).setExecutable(true);
         }
      }
   }


   public static void processLine(String sLine) {

      if (sLine.startsWith(">")) {
         sLine = sLine.substring(1).trim();
         if (m_VerboseCommands.contains(sLine)) {
            m_AddConsoleOutput = true;
         }
         else {
            m_AddConsoleOutput = false;
         }
      }
      else if (m_AddConsoleOutput) {
         m_ConsoleResults.add("<p>" + sLine + "</p>\n");
      }

   }


   public static String getConsoleOutput() {

      final StringBuffer out = new StringBuffer();
      out.append("<font face=\"courier\">\n");
      out.append("<h2> R Output</h2>\n");
      for (int i = 0; i < m_ConsoleResults.size(); i++) {
         out.append(m_ConsoleResults.get(i));
      }
      out.append("</font>\n");

      return out.toString();

   }


   public static String getScriptsFolder() {

      return SextanteGUI.getSettingParameterValue(SextanteRSettings.R_SCRIPTS_FOLDER);// + File.separator + "description";

   }

}


class StreamGobbler
         extends
            Thread {

   InputStream is;
   String      type;


   StreamGobbler(final InputStream is) {

      this.is = is;

   }


   @Override
   public void run() {
      try {
         final InputStreamReader isr = new InputStreamReader(is);
         final BufferedReader br = new BufferedReader(isr);
         String line = null;
         while ((line = br.readLine()) != null) {
            //RUtils.processLine(line);
         }
      }
      catch (final IOException ioe) {
         //ioe.printStackTrace();
      }
   }
}
