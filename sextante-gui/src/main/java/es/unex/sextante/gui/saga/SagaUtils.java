package es.unex.sextante.gui.saga;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.settings.SextanteSagaSettings;


public class SagaUtils {


   public static final String   SAGACMD_USE_START_PARAMETER = "SAGACMD_USE_START_PARAMETER";

   private static SagaAlgorithm m_Alg;
   private static StringBuffer  m_sMessages;


   public static int installSaga() throws SagaExecutionException {

      deleteDescriptionFiles();
      createLibrariesListFile();
      createLibrariesDescriptionFiles();
      return createAlgorithmsDescriptionFiles();

   }


   public static void deleteDescriptionFiles() {

      final File file = new File(getSagaDescriptionFolder());
      final String[] files = file.list();

      if (files != null) {
         for (final String element : files) {
            new File(file.getAbsoluteFile() + File.separator + element).delete();
         }
      }

   }


   public static void createLibrariesListFile() throws SagaExecutionException {


      final String[] sCommands = new String[] { " > " + getSagaDescriptionFolder() + File.separator + "sagalibs.txt" };
      createSagaBatchJobFileFromSagaCommands(sCommands);
      executeSaga(null);

   }


   private static void createLibrariesDescriptionFiles() throws SagaExecutionException {


      final ArrayList<String> commands = new ArrayList<String>();

      final String sFile = getSagaDescriptionFolder() + File.separator + "sagalibs.txt";
      BufferedReader input = null;
      try {
         input = new BufferedReader(new FileReader(sFile));
         String sLine = null;
         while ((sLine = input.readLine()) != null) {
            if (sLine.startsWith("-")) {
               final String sLibraryName = sLine.substring(1, sLine.lastIndexOf(".")).trim();
               commands.add(sLibraryName + " > " + getSagaDescriptionFolder() + File.separator + "lib_" + sLibraryName + ".txt");
            }
         }
         input.close();
         createSagaBatchJobFileFromSagaCommands(commands.toArray(new String[0]));
         executeSaga(null);
      }
      catch (final Exception e) {
         throw new SagaExecutionException();
      }

   }


   private static int createAlgorithmsDescriptionFiles() throws SagaExecutionException {

      int iAlg = 0;
      final ArrayList<String> commands = new ArrayList<String>();

      File file = new File(getSagaDescriptionFolder());
      final String[] files = file.list();

      if (files != null) {
         for (final String sFilename : files) {
            if (sFilename.startsWith("lib")) {
               int iAlgsInLibrary = 0;
               final String sLibraryName = sFilename.substring(4, sFilename.length() - 4);
               file = new File(getSagaDescriptionFolder() + File.separator + sFilename);
               BufferedReader input = null;
               try {
                  input = new BufferedReader(new FileReader(file.getAbsolutePath()));
                  String sLine = null;
                  while ((sLine = input.readLine()) != null) {
                     final String[] sTokens = sLine.split("\t");
                     if (sTokens.length > 0) {
                        if (isNumber(sTokens[0].trim())) {
                           commands.add(sLibraryName + " " + Integer.toString(iAlgsInLibrary) + " >" + getSagaDescriptionFolder()
                                        + File.separator + "alg_" + sLibraryName + "_" + Integer.toString(iAlg) + ".txt 2>&1");
                           iAlgsInLibrary++;
                           iAlg++;

                        }
                     }
                  }
                  input.close();
               }
               catch (final Exception e) {
                  throw new SagaExecutionException();
               }
            }
         }
      }

      if (commands.size() != 0) {
         createSagaBatchJobFileFromSagaCommands(commands.toArray(new String[0]));
         executeSaga(null);
      }

      return iAlg;

   }


   private static boolean isNumber(final String s) {

      try {
         Integer.parseInt(s);
         return true;
      }
      catch (final NumberFormatException e) {
         return false;
      }

   }


   public static String getSagaDescriptionFolder() {


      final String sPath = SextanteGUI.getSextantePath() + File.separator + "saga" + File.separator + "description";

      final File file = new File(sPath);
      if (!file.exists()) {
         file.mkdir();
      }

      return sPath;

   }


   public static String getBatchJobFilename() {

      String sFile;

      if (Sextante.isUnix() || Sextante.isMacOSX()) {
         sFile = "saga_batch_job.sh";
      }
      else {//Windows
         sFile = "saga_batch_job.bat";
      }

      sFile = SextanteGUI.getUserFolder() + File.separator + sFile;

      return sFile;

   }


   public static void createSagaBatchJobFileFromSagaCommands(final String[] sCommands) {

      final String sFilename = getBatchJobFilename();
      final String sSagaFolder = SextanteGUI.getSettingParameterValue(SextanteSagaSettings.SAGA_FOLDER);
      try {
         final BufferedWriter output = new BufferedWriter(new FileWriter(sFilename));
         if (Sextante.isWindows()) {
            output.write("set SAGA=" + sSagaFolder + "\n");
            output.write("set SAGA_MLB=" + sSagaFolder + File.separator + "modules" + "\n");
            output.write("PATH=PATH;%SAGA%;%SAGA_MLB%\n");
         }
         else {
            //output.write("!#/bin/sh\n");
            output.write("export SAGA_MLB=" + sSagaFolder + File.separator + "modules" + "\n");
            output.write("PATH=$PATH:" + sSagaFolder + File.separator + "modules" + "\n");
            output.write("export PATH");
         }

         final String value = SextanteGUI.getSettingParameterValue(SAGACMD_USE_START_PARAMETER);
         boolean b = false;
         if (value != null) {
            b = Boolean.parseBoolean(value);
         }
         for (int i = 0; i < sCommands.length; i++) {
            if (b) {
               output.write("saga_cmd " + sCommands[i] + "> dummy.txt \n");
            }
            else {
               output.write("saga_cmd " + sCommands[i] + "\n");
            }
         }
         output.write("exit");
         output.close();
      }
      catch (final IOException e) {
         Sextante.addErrorToLog(e);
      }


   }


   public static int executeSaga(final SagaAlgorithm alg) throws SagaExecutionException {


      m_Alg = alg;
      final List<String> list = new ArrayList<String>();
      ProcessBuilder pb;
      pb = new ProcessBuilder(list);
      if (Sextante.isUnix() || Sextante.isMacOSX()) {
         setExecutable(getBatchJobFilename());
         list.add(getBatchJobFilename());
      }
      else { //windows
         list.add("cmd.exe");
         list.add("/C");
         //list.add("start");
         final String value = SextanteGUI.getSettingParameterValue(SAGACMD_USE_START_PARAMETER);
         if (value != null) {
            final boolean b = Boolean.parseBoolean(value);
            if (b) {
               //list.add("start");
            }
         }
         list.add(getBatchJobFilename());
      }

      m_sMessages = new StringBuffer();

      Process process;
      try {
         process = pb.start();
         final StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
         final StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
         errorGobbler.start();
         outputGobbler.start();
         final int iReturn = process.waitFor();
         SagaAlgorithmProvider.addMessage(m_sMessages.toString(), "SAGA execution");
         return iReturn;
      }
      catch (final Exception e) {
         throw new SagaExecutionException();
      }


   }


   public static void setExecutable(final String pathname) throws SagaExecutionException {

      final String version = System.getProperty("java.version").substring(0, 3);
      final Float f = Float.valueOf(version);
      if (f.floatValue() < (float) 1.6) {
         if (Sextante.isUnix() || Sextante.isMacOSX()) {
            try {
               Runtime.getRuntime().exec("chmod +x " + pathname);
            }
            catch (final IOException e) {
               throw new SagaExecutionException();
            }
         }
      }
      else {
         if (Sextante.isUnix() || Sextante.isMacOSX()) {
            new File(pathname).setExecutable(true);
         }
      }
   }


   public static void processLine(String line) {

      if (m_Alg == null) {
         return;
      }

      line = line.replace("%", "").trim();
      try {
         final int i = Integer.parseInt(line);
         m_Alg.updateProgress(i, 100);
      }
      catch (final Exception e) {
         //System.out.println(line);
         m_sMessages.append(line + "\n");

      }

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
            SagaUtils.processLine(line);
         }
      }
      catch (final IOException ioe) {

      }
   }
}
