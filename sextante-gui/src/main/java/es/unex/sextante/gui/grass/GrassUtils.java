package es.unex.sextante.gui.grass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.JOptionPane;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.settings.SextanteGrassSettings;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.Output;


public class GrassUtils {


   //For parsing of GRASS message blocks
   private static boolean        lastWasInfoEnd       = false;
   private static boolean        lastWasEmpty         = false;
   private static boolean        insideRegPrimitives  = false;

   //Link to the currently running GRASS algorithm (need this for status updates)
   private static GrassAlgorithm m_Alg;

   //The currently running GRASS process;
   private static Process        m_Proc               = null;
   private static boolean        m_bProcCanceled      = false;
   private static boolean        m_bProcInterruptible = true;

   //A file for process communication (on Windows)
   //private static File           m_ComFile            = null;

   //Char array size for raw input stream processing
   private static final int      MAX_CHARS            = 8192;

   //Temporary color table file constants
   public static final String    colorTableExt        = "colortable";
   public static final String    colorTableIdentifier = "SEXTANTE GRASS Interface Color Table";
   public static final String    colorTableVersion    = "1.0";

   //The default prefix for temporary files and folders
   public static final String    TEMP_PREFIX          = "SGI";

   private static String         m_sGrassTempMapsetFolder;

   static {

      //      readGroupNames();

   }


   //private static HashMap<String, String> m_Groups;


   /*
    * Returns the running GRASS process or null.
    */
   public static Process getProcess() {
      return (m_Proc);
   }


   /*
    * Returns the status of the running process.
    */
   public static boolean isProcessCanceled() {
      return (m_bProcCanceled);
   }


   /*
    * Sets interruptible status of current and future processes.
    */
   public static void setInterruptible(final boolean choice) {
      m_bProcInterruptible = choice;
   }


   /*
    * Cancels the running process.
    * But only if it is interruptible (it is by default).
    */
   public static void cancelProcess() {

      if (m_Proc != null) {
         if (m_bProcInterruptible) {
            m_Proc.destroy();
            m_Proc = null;
            m_bProcCanceled = true;
         }
      }

   }


   /**
    * Creates a compact startup script for GRASS. The script code created may vary, depending on the operating system. This also
    * creates a temporary GRASS settings file (GISCR).
    * 
    * @param cmdline
    * 
    * @return The file handler for the created script, NULL on failure.
    * 
    */
   public static File createStartupScript(final String cmdline) {
      BufferedWriter output = null;

      File script = null;
      File gisrc = null;

      UUID id;
      String tmpPrefix;
      String tmpSuffix;
      String tmpExtension;
      String tmpBase;
      String tmpName;

      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_FOLDER);
      final String sWinShell = SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_WIN_SHELL);

      //Create temporary script file in system's temp dir
      id = UUID.randomUUID();
      tmpPrefix = new String(GrassUtils.TEMP_PREFIX);
      tmpSuffix = new String("_" + id);
      tmpBase = new String(System.getProperty("java.io.tmpdir"));
      if (Sextante.isWindows()) {
         tmpExtension = new String("bat");
      }
      else {
         tmpExtension = new String("sh");
      }
      if (tmpBase.endsWith(File.separator)) {
         tmpName = new String(tmpBase + File.separator + tmpPrefix + tmpSuffix.replace('-', '_') + "." + tmpExtension);
      }
      else {
         tmpName = new String(tmpBase + File.separator + tmpPrefix + tmpSuffix.replace('-', '_') + "." + tmpExtension);
      }
      script = new File(tmpName);
      script.deleteOnExit();

      //Create a temporary GISRC file in system's temp dir
      id = UUID.randomUUID();
      tmpPrefix = new String(GrassUtils.TEMP_PREFIX);
      tmpSuffix = new String("_" + id);
      tmpBase = new String(System.getProperty("java.io.tmpdir"));
      if (tmpBase.endsWith(File.separator)) {
         tmpName = new String(tmpBase + File.separator + tmpPrefix + tmpSuffix.replace('-', '_') + ".gisrc");
      }
      else {
         tmpName = new String(tmpBase + File.separator + tmpPrefix + tmpSuffix.replace('-', '_') + ".gisrc");
      }
      gisrc = new File(tmpName);
      gisrc.deleteOnExit();

      //Get GISDBASE, LOCATION and MAPSET from String parameter
      final String mapset = new String(getGrassMapsetFolder().substring(getGrassMapsetFolder().lastIndexOf(File.separator) + 1,
               getGrassMapsetFolder().length()));
      final String path = new String(getGrassMapsetFolder().substring(0, getGrassMapsetFolder().lastIndexOf(File.separator)));
      final String location = new String(path.substring(path.lastIndexOf(File.separator) + 1, path.length()));
      final String gisdbase = new String(path.substring(0, path.lastIndexOf(File.separator)));

      //Write the temporary GISRC file to use in this session
      try {
         output = new BufferedWriter(new FileWriter(gisrc));
         output.write("GISDBASE: " + gisdbase + "\n");
         output.write("LOCATION_NAME: " + location + "\n");
         output.write("MAPSET: " + mapset + "\n");
         output.write("GRASS_GUI: text\n");
         output.close();
      }
      catch (final Exception e) {
         return (null);
      }

      //Write the startup script
      if (Sextante.isUnix() || Sextante.isMacOSX()) {
         //Startup script for *nix like systems with built-in shell interpreter
         final String grassfolder = new String(sFolder);
         try {
            output = new BufferedWriter(new FileWriter(script));
            output.write("#!/bin/sh\n");
            output.write("export GISRC=\"" + gisrc.getAbsolutePath() + "\"\n");
            output.write("export GISBASE=\"" + grassfolder + "\"\n");
            output.write("export GRASS_PROJSHARE=\"" + grassfolder + File.separator + "share" + File.separator + "proj" + "\"\n");
            output.write("export GRASS_MESSAGE_FORMAT=gui\n");
            output.write("export GRASS_SH=/bin/sh\n");
            output.write("export GRASS_PERL=/usr/bin/perl\n");
            output.write("export GRASS_VERSION=\"" + getGrassVersion() + "\"\n");
            output.write("export GIS_LOCK=$$\n");
            output.write("\n");
            output.write("if [ \"$LC_ALL\" ] ; then\n");
            output.write("\tLCL=`echo \"$LC_ALL\" | sed 's/\\(..\\)\\(.*\\)/\\1/'`\n");
            output.write("elif [ \"$LC_MESSAGES\" ] ; then\n");
            output.write("\tLCL=`echo \"$LC_MESSAGES\" | sed 's/\\(..\\)\\(.*\\)/\\1/'`\n");
            output.write("else\n");
            output.write("\tLCL=`echo \"$LANG\" | sed 's/\\(..\\)\\(.*\\)/\\1/'`\n");
            output.write("fi\n");
            output.write("\n");
            output.write("if [ -n \"$GRASS_ADDON_PATH\" ] ; then\n");
            output.write("\tPATH=\"" + grassfolder + "/bin:" + grassfolder + "/scripts:$GRASS_ADDON_PATH:$PATH\"\n");
            output.write("else\n");
            output.write("\tPATH=\"" + grassfolder + "/bin:" + grassfolder + "/scripts:$PATH\"\n");
            output.write("fi\n");
            output.write("export PATH\n");
            output.write("\n");
            if (Sextante.isMacOSX()) {
               output.write("if [ ! \"$DYLD_LIBRARY_PATH\" ] ; then\n");
               output.write("\tDYLD_LIBRARY_PATH=\"$GISBASE/lib\"\n");
               output.write("else\n");
               output.write("\tDYLD_LIBRARY_PATH=\"$GISBASE/lib:$DYLD_LIBRARY_PATH\"\n");
               output.write("fi\n");
               output.write("export DYLD_LIBRARY_PATH\n");
            }
            else {
               output.write("if [ ! \"$LD_LIBRARY_PATH\" ] ; then\n");
               output.write("\tLD_LIBRARY_PATH=\"$GISBASE/lib\"\n");
               output.write("else\n");
               output.write("\tLD_LIBRARY_PATH=\"$GISBASE/lib:$LD_LIBRARY_PATH\"\n");
               output.write("fi\n");
               output.write("export LD_LIBRARY_PATH\n");
            }
            output.write("\n");
            output.write("if [ ! \"$GRASS_PYTHON\" ] ; then\n");
            output.write("\tGRASS_PYTHON=python\n");
            output.write("fi\n");
            output.write("export GRASS_PYTHON\n");
            output.write("if [ ! \"$PYTHONPATH\" ] ; then\n");
            output.write("\tPYTHONPATH=\"$GISBASE/etc/python\"\n");
            output.write("else\n");
            output.write("\tPYTHONPATH=\"$GISBASE/etc/python:$PYTHONPATH\"\n");
            output.write("fi\n");
            output.write("export PYTHONPATH\n");
            output.write("\n");
            output.write("if [ ! \"$GRASS_GNUPLOT\" ] ; then\n");
            output.write("\tGRASS_GNUPLOT=\"gnuplot -persist\"\n");
            output.write("\texport GRASS_GNUPLOT\n");
            output.write("fi\n");
            output.write("\n");
            output.write("if [ \"$GRASS_FONT_CAP\" ] && [ ! -f \"$GRASS_FONT_CAP\" ] ; then\n");
            output.write("\tg.mkfontcap\n");
            output.write("fi\n");
            output.write("\n");
            output.write("g.gisenv set=\"MAPSET=" + mapset + "\"\n");
            output.write("g.gisenv set=\"LOCATION=" + location + "\"\n");
            output.write("g.gisenv set=\"LOCATION_NAME=" + location + "\"\n");
            output.write("g.gisenv set=\"GISDBASE=" + gisdbase + "\"\n");
            output.write("g.gisenv set=\"GRASS_GUI=text\"\n");
            output.write("\n");
            output.write("\"" + grassfolder + File.separator + "etc" + File.separator + "run\" " + "\"$GRASS_BATCH_JOB\" 1>&2\n");
            output.write("\n");
            output.close();
            setExecutable(script.getAbsolutePath());
         }
         catch (final Exception e) {
            return (null);
         }
      }
      else {//Windows

         //Write windows startup script
         String shToolsPath = "";
         if ((sWinShell.length() < 2) || (sWinShell == null)) {
            return (null);
         }
         shToolsPath = sWinShell;
         shToolsPath = shToolsPath.substring(0, shToolsPath.lastIndexOf(File.separator));
         try {
            //m_ComFile.createNewFile();
            output = new BufferedWriter(new FileWriter(script));
            //Turn on/off verbose output
            //output.write("@echo off\n");
            //Settings that would otherwise be done in grassXx.bat
            output.write("set HOME=" + System.getProperty("user.home") + "\n");
            output.write("set GISRC=" + gisrc.getAbsolutePath() + "\n");
            output.write("set GRASS_SH=" + sWinShell + "\n");
            output.write("set PATH=" + shToolsPath + File.separator + "bin;" + shToolsPath + File.separator + "lib;" + "%PATH%\n");
            output.write("set WINGISBASE=" + sFolder + "\n");
            output.write("set GISBASE=" + sFolder + "\n");
            output.write("set GRASS_PROJSHARE=" + sFolder + File.separator + "share" + File.separator + "proj" + "\n");
            output.write("set GRASS_MESSAGE_FORMAT=gui\n");
            //Replacement code for etc/Init.bat
            output.write("if \"%GRASS_ADDON_PATH%\"==\"\" set PATH=%WINGISBASE%\\bin;%WINGISBASE%\\lib;%PATH%\n");
            output.write("if not \"%GRASS_ADDON_PATH%\"==\"\" set PATH=%WINGISBASE%\\bin;%WINGISBASE%\\lib;%GRASS_ADDON_PATH%;%PATH%\n");
            output.write("\n");
            output.write("set GRASS_VERSION=" + getGrassVersion() + "\n");
            output.write("if not \"%LANG%\"==\"\" goto langset\n");
            output.write("FOR /F \"usebackq delims==\" %%i IN (`\"%WINGISBASE%\\etc\\winlocale\"`) DO @set LANG=%%i\n");
            output.write(":langset\n");
            output.write("\n");
            output.write("set PATHEXT=%PATHEXT%;.PY\n");
            output.write("set PYTHONPATH=%PYTHONPATH%;%WINGISBASE%\\etc\\python;%WINGISBASE%\\etc\\wxpython\\n");
            output.write("\n");
            output.write("g.gisenv.exe set=\"MAPSET=" + mapset + "\"\n");
            output.write("g.gisenv.exe set=\"LOCATION=" + location + "\"\n");
            output.write("g.gisenv.exe set=\"LOCATION_NAME=" + location + "\"\n");
            output.write("g.gisenv.exe set=\"GISDBASE=" + gisdbase + "\"\n");
            output.write("g.gisenv.exe set=\"GRASS_GUI=text\"\n");
            output.write(cmdline);
            output.write("\n");
            /*output.write("call \"%GRASS_BATCH_JOB%\"\n");
            output.write("call \"%GRASS_BATCH_JOB%\" > " + m_ComFile.getAbsolutePath() + " 2>&1\n\n");*/
            output.write("exit\n");
            output.close();
         }
         catch (final Exception e) {
            return (null);
         }
      }

      if (script != null) {
         return script;
      }
      return null;
   }


   /**
    * Returns a ProcesBuilder ready to execute grass.
    * 
    * @param cmdline
    * 
    * @return a ProcesBuilder ready to execute grass
    */
   static ProcessBuilder getGrassExecutable(final String cmdline) {

      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_FOLDER);

      final List<String> list = new ArrayList<String>();
      ProcessBuilder pb;
      pb = new ProcessBuilder(list);
      final Map env = pb.environment();
      env.put("GRASS_BATCH_JOB", getBatchJobFile());
      if (Sextante.isUnix() || Sextante.isMacOSX()) {
         env.put("GRASS_MESSAGE_FORMAT", "gui");
         env.put("GISBASE", sFolder);
         final File script = (createStartupScript(cmdline));
         if (script != null) {
            list.add(script.getAbsolutePath());
         }
         else {
            //TODO: need to catch this one level higher!
            return null;
         }
      }
      else { //windows
         list.add("cmd.exe");
         list.add("/C");
         //list.add("start");
         //list.add("/max");
         final File script = (createStartupScript(cmdline));
         if (script != null) {
            list.add(script.getAbsolutePath());
         }
         else {
            //TODO: need to catch this one level higher!
            return null;
         }
      }
      return pb;
   }


   public static String getBatchJobFile() {

      String sFile;

      if (Sextante.isUnix() || Sextante.isMacOSX()) {
         sFile = "sextante_batch_job.sh";
      }
      else {//Windows
         sFile = "sextante_batch_job.bat";
      }

      sFile = SextanteGUI.getUserFolder() + File.separator + sFile;

      return sFile;

   }


   /**
    * Returns a GRASS map name which can be safely used to store an imported layer or any other GRASS element (a region setting, a
    * group) that is significant in our context. When using the name returned by this function, there is virtually no chance that
    * it will overwrite any existing data in the mapset.
    * 
    * Note: valid GRASS MAP names must not contain spaces or special characters, must start on a letter and should not include "-"
    * (minus)
    * 
    * @param sPrefix
    *                a prefix for the temp map name
    */
   public static String getTempMapName(final String sPrefix) {
      String tmpName;
      String sName;
      UUID id;
      boolean exists;

      do {
         exists = false;
         id = UUID.randomUUID();
         tmpName = new String(sPrefix + id);
         sName = new String(tmpName.replace('-', '_'));
         final File vectordir = new File(getGrassMapsetFolder() + File.separator + "vector" + File.separator + sName);
         final File rasterfile = new File(getGrassMapsetFolder() + File.separator + "cellhd" + File.separator + sName);
         final File regionfile = new File(getGrassMapsetFolder() + File.separator + "windows" + File.separator + sName);
         final File voxelfile = new File(getGrassMapsetFolder() + File.separator + "grid3d" + File.separator + sName);
         final File catfile = new File(getGrassMapsetFolder() + File.separator + "cats" + File.separator + sName);
         final File groupfile = new File(getGrassMapsetFolder() + File.separator + "group" + File.separator + sName);
         if (vectordir.exists() || rasterfile.exists() || regionfile.exists() || voxelfile.exists() || catfile.exists()
             || groupfile.exists()) {
            exists = true;
         }
      }
      while (exists);

      return (sName);
   }


   /**
    * Returns a GRASS map name which can be safely used to store an imported layer (i.e. there is no chance that it will overwrite
    * an existing map in the mapset).
    * 
    * Note: valid GRASS MAP names must not contain spaces or special characters, must start on a letter and should not include "-"
    * (minus)
    * 
    */
   public static String getTempMapName() {
      return (getTempMapName(GrassUtils.TEMP_PREFIX + "_"));
   }


   /**
    * Checks if the current GRASS mapset (rather: the location of which it is part) works in lat/lon degrees. We need this
    * information, because some GRASS modules do not work (well) with lat/lon input data.
    * 
    * @return "true" if the current mapset works in lat/lon, "false" otherwise.
    */
   public static boolean isLatLon() {
      String mapSetFolder;


      mapSetFolder = new String(getGrassMapsetFolder().substring(0, getGrassMapsetFolder().lastIndexOf(File.separator))
                                + File.separator + "PERMANENT/PROJ_UNITS");

      try {
         final FileInputStream fstream = new FileInputStream(mapSetFolder);
         final DataInputStream in = new DataInputStream(fstream);
         final BufferedReader br = new BufferedReader(new InputStreamReader(in));
         String line;
         while ((line = br.readLine()) != null) {
            if (line.contains("degree")) {
               in.close();
               return (true);
            }
         }
         in.close();
      }
      catch (final Exception e) {
         Sextante.addWarningToLog("GRASS Interface: could not determine coordinate system. Assuming X/Y(/Z) cartesian.\n");
      }
      return (false);
   }


   /*
    * Checks whether a GRASS map contains more than one type of geometry primitives.
    * We need to know this for shapefile export.
    *
    * @param sMapName
    * 				Name of GRASS map to query
    */
   public static boolean isMultiGeom(final String sMapName) throws GrassExecutionException {

      boolean is_multi_geom = false;

      //Get number of objects for each type of geometry in GRASS map
      final int num_points = GrassVInfoUtils.getNumPoints(sMapName);
      final int num_lines = GrassVInfoUtils.getNumLines(sMapName);
      final int num_polygons = GrassVInfoUtils.getNumPolygons(sMapName);
      final int num_faces = GrassVInfoUtils.getNumFaces(sMapName);
      final int num_kernels = GrassVInfoUtils.getNumKernels(sMapName);

      //Check for type combinations that would lead to multiple geometry types
      if (num_points > 0) {
         if ((num_kernels > 0) || (num_lines > 0) || (num_polygons > 0) || (num_faces > 0)) {
            is_multi_geom = true;
         }
      }
      if (num_kernels > 0) {
         if ((num_points > 0) || (num_lines > 0) || (num_polygons > 0) || (num_faces > 0)) {
            is_multi_geom = true;
         }
      }
      if (num_lines > 0) {
         if ((num_points > 0) || (num_kernels > 0) || (num_polygons > 0) || (num_faces > 0)) {
            is_multi_geom = true;
         }
      }
      if (num_polygons > 0) {
         if ((num_points > 0) || (num_kernels > 0) || (num_lines > 0) || (num_faces > 0)) {
            is_multi_geom = true;
         }
      }
      if (num_faces > 0) {
         if ((num_points > 0) || (num_kernels > 0) || (num_lines > 0) || (num_polygons > 0)) {
            is_multi_geom = true;
         }
      }

      if (num_points + num_lines + num_polygons + num_faces + num_kernels == 0) {
         //No valid geometries. Maybe a topology error?
         JOptionPane.showMessageDialog(null, Sextante.getText("grass_error_geometries_none_found"),
                  Sextante.getText("grass_error_title"), JOptionPane.WARNING_MESSAGE);
         throw new GrassExecutionException();
      }
      return (is_multi_geom);

   }


   /*
    * Helper function for runGRASS().
    *
    * Takes a line of GRASS output, analyzes it and decides whether to
    * send it to log and/or progress monitor.
    * May also do some reformatting before relaying the string for display.
    *
    */
   public static void filterGRASSOutput(String line) throws GrassExecutionException {

      boolean skip = false;

      //just process output if we are running grass form a sextante algorithm
      if (m_Alg == null) {
         return;
      }

      //Speedhack: if this is a progress info line, we can skip 99% of the remaining checks!
      if (!line.contains("GRASS_INFO_PERCENT")) {
         //All other lines need to be processed more elaborately
         if ((line.length() < 2)) {
            if ((lastWasEmpty == true) || (lastWasInfoEnd == true)) {
               skip = true;
            }
            lastWasEmpty = true;
         }
         else {
            lastWasEmpty = false;
         }

         if (insideRegPrimitives == true) {
            skip = true;
         }

         //substitute lengthy temporary map names
         if (line.contains(GrassUtils.TEMP_PREFIX) || line.contains("_")) {
            for (int i = 0; i < m_Alg.getMapNames().size(); i++) {
               line = line.replace(m_Alg.getMapNames().get(i), "<" + m_Alg.getFileNames().get(i) + ">");
            }
            //Use a regular expression for those internal map names that cannot be fully resolved
            line = line.replaceAll("([_][a-z0-9]{8}[_][a-z0-9]{4}[_][a-z0-9]{4}[_][a-z0-9]{4}[_][a-z0-9]{12})", "[TMP]");
         }

         //remove the startup script lines
         if (line.startsWith("set") || line.startsWith("if") || line.startsWith("FOR")) {
            skip = true;
         }
         //There are some lines that we can always skip (chatter from the
         //GRASS batch job script, etc.)
         if (line.contains("Welcome to GRASS")) {
            skip = true;
         }
         if (line.contains("Closing monitors ...")) {
            skip = true;
         }
         if (line.contains("Cleaning up temporary files ...")) {
            skip = true;
         }
         if (line.contains("(defined in GRASS_BATCH_JOB variable) was executed.")) {
            skip = true;
         }
         if (line.contains("d.mon: not found")) {
            skip = true;
         }
         if (line.contains("Goodbye from GRASS GIS")) {
            skip = true;
         }
         if (line.contains("Starting GRASS ...")) {
            skip = true;
         }
         if (line.contains("Executing '") && line.contains("' ...")) {
            skip = true;
         }
         if (line.contains("Executing '") && line.contains("' ...")) {
            skip = true;
         }
         if (line.contains("r.out.gdal complete.")) {
            skip = true;
         }

         //Some data import/export chit-chat about temporary files we may want to shorten...
         if (line.contains("Building topology for vector map <")) {
            line = "Building topology for vector map...";
            skip = true;
         }
         if (line.contains("Registering primitives...")) {
            insideRegPrimitives = true;
         }
         if (line.contains(" primitives registered")) {
            insideRegPrimitives = false;
            skip = false;
         }

         //Format GRASS INFO type messages
         if (line.contains("GRASS_INFO_MESSAGE")) {
            if (!line.contains(":")) {
               skip = true;
            }
            else {
               if (line.length() >= (line.indexOf(":") + 3)) {
                  line = line.substring(line.indexOf(":") + 2);
               }
               else {
                  line = " ";
               }
            }
         }
         //If we get an error, we cancel the running module
         if (line.contains("GRASS_INFO_ERROR") || line.startsWith("ERROR:")) {
            if (!line.contains(":")) {
               skip = true;
            }
            else {
               if (line.length() >= (line.indexOf(":") + 3)) {
                  line = "ERROR: " + line.substring(line.indexOf(":") + 2);
               }
               else {
                  line = " ";
               }
            }
            JOptionPane.showMessageDialog(null, line, Sextante.getText("grass_error_title"), JOptionPane.ERROR_MESSAGE);
            GrassAlgorithmProvider.addMessage(line);
            m_bProcCanceled = true;//simulate a cancellation
            Sextante.addErrorToLog("SEXTANTE GRASS interface: " + line);
            throw new GrassExecutionException();
         }
         if (line.contains("GRASS_INFO_WARNING")) {
            if (!line.contains(":")) {
               skip = true;
            }
            else {
               if (line.length() >= (line.indexOf(":") + 3)) {
                  line = "WARNING: " + line.substring(line.indexOf(":") + 2);
               }
               else {
                  line = " ";
               }
            }
         }

         //After the GRASS_INFO_END tag, there is an annoying newline which we
         //want to skip...
         if (line.contains("GRASS_INFO_END")) {
            lastWasInfoEnd = true;
         }
         else {
            lastWasInfoEnd = false;
         }

         //Some warning and error messages that can safely be skipped...
         if (line.contains("<PROJ_INFO> file not found")) {
            skip = true;
         }
         if (line.contains("<PROJ_UNITS> file not found")) {
            skip = true;
         }
         if (line.contains("ERROR 6: SetColorTable() only supported")) {
            skip = true;
         }
         if (line.contains("Unable to set projection")) {
            skip = true;
         }
      }

      //Output whatever is left after filtering.
      if (!skip) {
         {//Progress monitor output
            if (line.contains("GRASS_INFO_PERCENT")) {
               //if (Sextante.isUnix() || Sextante.isMacOSX()) {
               m_Alg.updateProgress(Integer.valueOf(line.substring(line.indexOf(":") + 2)), 100);
               //}
            }
         }

         {//Log output
            //For the log, we may need to skip a few more lines.
            if (line.contains("GRASS_INFO_PERCENT")) {
               if (line.contains(": 100")) {
                  line = "(100%)";
               }
               else {
                  skip = true;
               }
            }
            if (line.contains("GRASS_INFO_END")) {
               skip = true;
            }
            if (!skip) {
               GrassAlgorithmProvider.addMessage(line);
            }
         }
      }
      else {
         lastWasEmpty = true;
      }
   }


   /*
    * Checks whether an ASCII character represents a line terminator.
    */
   private static boolean isLineTerminator(final char c) {

      if ((c == '\n') || (c == '\r') || (c == '\u0085') || (c == '\u2028') || (c == '\u2029')) {
         return true;
      }

      return (false);
   }


   /**
    * Runs a batch of GRASS commands.
    * 
    * @param sCommand
    *                String buffer containing GRASS commands
    * 
    * @param sMessage
    *                String with message to write into logfile
    * 
    */
   public static void runGRASS(final StringBuffer sCommand,
                               final String sMessage,
                               final GrassAlgorithm alg) throws GrassExecutionException {
      Writer output = null;

      //Link to algorithm for which this is called.
      m_Alg = alg;

      //Set initial process status
      m_bProcCanceled = false;

      //On Windows, we need to check if a valid sh.exe was supplied by the user
      if (Sextante.isWindows()) {
         final String sWinShell = SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_WIN_SHELL);
         if ((sWinShell == null) || (sWinShell.length() < 2)) {
            JOptionPane.showMessageDialog(null, Sextante.getText("grass_error_shell_missing") + "\n"
                                                + Sextante.getText("grass_shell_url"), Sextante.getText("grass_error_title"),
                     JOptionPane.ERROR_MESSAGE);
            throw new GrassExecutionException();
         }
      }

      //Check that the mapset exists and is not currently locked
      final File mapset = new File(getGrassMapsetFolder());

      if (!mapset.exists()) {
         JOptionPane.showMessageDialog(null, Sextante.getText("grass_error_mapset_missing"),
                  Sextante.getText("grass_error_title"), JOptionPane.ERROR_MESSAGE);
         throw new GrassExecutionException();
      }
      final File lockfile = new File(getGrassMapsetFolder() + File.separator + ".gislock");
      if (lockfile.exists()) {
         JOptionPane.showMessageDialog(null, Sextante.getText("grass_error_mapset_locked"),
                  Sextante.getText("grass_error_title"), JOptionPane.ERROR_MESSAGE);
         throw new GrassExecutionException();
      }

      if (!Sextante.isWindows()) {
         try {
            output = new BufferedWriter(new FileWriter(GrassUtils.getBatchJobFile()));
            output.write(sCommand.toString());
         }
         catch (final Exception e) {
            throw new GrassExecutionException();
         }
         finally {
            if (output != null) {
               try {
                  output.close();
               }
               catch (final IOException e) {
                  throw new GrassExecutionException();
               }
            }
            setExecutable(getBatchJobFile());
         }
      }

      Sextante.addInfoToLog(sMessage + " " + sCommand);

      //Execute GRASS GIS command.
      //We read every single character it outputs to either
      //<stdin> or <stderr> console streams. Doing this unbuffered is extremely
      //inefficient, but the only way we can get "live" status information for
      //CPU intensive tasks!
      try {
         final ProcessBuilder pb = GrassUtils.getGrassExecutable(sCommand.toString());
         if (Sextante.isWindows()) {
            final Process process = pb.start();
            m_Proc = process;
            final StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
            final StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream());
            errorGobbler.start();
            outputGobbler.start();
            process.waitFor();
         }
         else {
            final Process process = pb.start();
            m_Proc = process;
            final InputStream eis = process.getErrorStream();
            final InputStreamReader eisr = new InputStreamReader(eis);
            final InputStream is = process.getInputStream();
            final InputStreamReader isr = new InputStreamReader(is);
            char[] stderr = new char[MAX_CHARS];
            char[] stdin = new char[MAX_CHARS];
            int num_read_stderr;
            int num_read_stdin;
            int pos_stderr = 0;
            int pos_stdin = 0;

            final FileInputStream fis = null;
            final InputStreamReader in = null;
            final char[] line = new char[MAX_CHARS];
            final int pos = 0;
            final int num_read = -1;

            //Read stderr first, as we need that for progress display, and everything
            //important should come in through STDERR.
            while (((num_read_stderr = eisr.read(stderr, pos_stderr, 1)) != -1)) {
               //Process stderr first.
               if (pos_stderr == (MAX_CHARS - 1)) {
                  //safety catch
                  stderr[pos_stderr] = '\n';
               }
               if (!isLineTerminator(stderr[pos_stderr])) {
                  pos_stderr++;
               }
               else {
                  //System.err.println ("STDERR: " + String.copyValueOf(stderr).trim() );
                  filterGRASSOutput(String.copyValueOf(stderr).trim());
                  stderr = new char[MAX_CHARS];
                  pos_stderr = 0;
               }
            }
            //For the rare occasion that something should come in on STDIN: list to it...
            while (((num_read_stdin = isr.read(stdin, pos_stdin, 1)) != -1)) {
               //Process stdin
               if (pos_stdin == (MAX_CHARS - 1)) {
                  //safety catch
                  stdin[pos_stdin] = '\n';
               }
               if (!isLineTerminator(stdin[pos_stdin])) {
                  pos_stdin++;
               }
               else {
                  //System.err.println ("STDIN: " + String.copyValueOf(stdin).trim() );
                  filterGRASSOutput(String.copyValueOf(stdin).trim());
                  stdin = new char[MAX_CHARS];
                  pos_stdin = 0;
               }
            }
         }
      }
      catch (final Exception e) {
         throw new GrassExecutionException();
      }
   }


   /**
    * A convenience method to set the owner's execute permission for this abstract pathname.
    * 
    * @param pathname
    *                a pathname string
    * 
    * @throws GrassExecutionException
    */
   public static void setExecutable(final String pathname) throws GrassExecutionException {

      final String version = System.getProperty("java.version").substring(0, 3);
      final Float f = Float.valueOf(version);
      if (f.floatValue() < (float) 1.6) {
         if (Sextante.isUnix() || Sextante.isMacOSX()) {
            try {
               Runtime.getRuntime().exec("chmod +x " + pathname);
            }
            catch (final IOException e) {
               throw new GrassExecutionException();
            }
         }
      }
      else {
         if (Sextante.isUnix() || Sextante.isMacOSX()) {
            new File(pathname).setExecutable(true);
         }
      }
   }


   /**
    * Creates a temporary location and mapset(s) for GRASS data processing. A minimal set of folders and files is created in the
    * system's default temporary directory. The settings files are written with sane defaults, so GRASS can do its work. File
    * structure and content will vary slightly depending on whether the user wants to process lat/lon or x/y data.
    * 
    */
   public static void createTempMapset() throws IOException {

      final boolean bIsLatLon = new Boolean(SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_LAT_LON_MODE)).booleanValue();
      final String tmpFolder = new String(getGrassMapsetFolder().substring(0, getGrassMapsetFolder().lastIndexOf(File.separator)));
      final boolean b = new File(tmpFolder).mkdir();
      new File(tmpFolder + File.separator + "PERMANENT").mkdir();
      new File(tmpFolder + File.separator + "user").mkdir();
      new File(tmpFolder + File.separator + "PERMANENT" + File.separator + ".tmp").mkdir();
      writeGRASSWindow(tmpFolder + File.separator + "PERMANENT" + File.separator + "DEFAULT_WIND");
      new File(tmpFolder + File.separator + "PERMANENT" + File.separator + "MYNAME").createNewFile();
      try {
         final FileWriter fstream = new FileWriter(tmpFolder + File.separator + "PERMANENT" + File.separator + "MYNAME");
         final BufferedWriter out = new BufferedWriter(fstream);
         if (!bIsLatLon) {
            /* XY location */
            out.write("SEXTANTE GRASS interface: temporary x/y data processing location.\n");
         }
         else {
            /* lat/lon location */
            out.write("SEXTANTE GRASS interface: temporary lat/lon data processing location.\n");
         }
         out.close();
      }
      catch (final IOException e) {
         throw (e);
      }
      if (bIsLatLon) {
         new File(tmpFolder + File.separator + "PERMANENT" + File.separator + "PROJ_INFO").createNewFile();
         try {
            final FileWriter fstream = new FileWriter(tmpFolder + File.separator + "PERMANENT" + File.separator + "PROJ_INFO");
            final BufferedWriter out = new BufferedWriter(fstream);
            out.write("name: Latitude-Longitude\n");
            out.write("proj: ll\n");
            out.write("ellps: wgs84\n");
            out.close();
         }
         catch (final IOException e) {
            throw (e);
         }
         new File(tmpFolder + File.separator + "PERMANENT" + File.separator + "PROJ_UNITS").createNewFile();
         try {
            final FileWriter fstream = new FileWriter(tmpFolder + File.separator + "PERMANENT" + File.separator + "PROJ_UNITS");
            final BufferedWriter out = new BufferedWriter(fstream);
            out.write("unit: degree\n");
            out.write("units: degrees\n");
            out.write("meters: 1.0\n");
            out.close();
         }
         catch (final IOException e) {
            throw (e);
         }
      }
      writeGRASSWindow(tmpFolder + File.separator + "PERMANENT" + File.separator + "WIND");
      new File(tmpFolder + File.separator + "user" + File.separator + "dbf").mkdir();
      new File(tmpFolder + File.separator + "user" + File.separator + ".tmp").mkdir();
      new File(tmpFolder + File.separator + "user" + File.separator + "VAR").createNewFile();
      try {
         final FileWriter fstream = new FileWriter(tmpFolder + File.separator + "user" + File.separator + "VAR");
         final BufferedWriter out = new BufferedWriter(fstream);
         out.write("DB_DRIVER: dbf\n");
         out.write("DB_DATABASE: $GISDBASE/$LOCATION_NAME/$MAPSET/dbf/\n");
         out.close();
      }
      catch (final IOException e) {
         throw (e);
      }
      writeGRASSWindow(tmpFolder + File.separator + "user" + File.separator + "WIND");
   }


   /**
    * Writes GRASS region ("Windows") settings into a text file.
    * 
    * @param filename
    *                Name of the text file to write to
    */
   private static void writeGRASSWindow(final String filename) throws IOException {

      new File(filename).createNewFile();
      try {
         final FileWriter fstream = new FileWriter(filename);
         final BufferedWriter out = new BufferedWriter(fstream);
         final boolean bIsLatLon = new Boolean(SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_LAT_LON_MODE)).booleanValue();
         if (!bIsLatLon) {
            /* XY location */
            out.write("proj:       0\n");
            out.write("zone:       0\n");
            out.write("north:      1\n");
            out.write("south:      0\n");
            out.write("east:       1\n");
            out.write("west:       0\n");
            out.write("cols:       1\n");
            out.write("rows:       1\n");
            out.write("e-w resol:  1\n");
            out.write("n-s resol:  1\n");
            out.write("top:        1\n");
            out.write("bottom:     0\n");
            out.write("cols3:      1\n");
            out.write("rows3:      1\n");
            out.write("depths:     1\n");
            out.write("e-w resol3: 1\n");
            out.write("n-s resol3: 1\n");
            out.write("t-b resol:  1\n");
         }
         else {
            /* lat/lon location */
            out.write("proj:       3\n");
            out.write("zone:       0\n");
            out.write("north:      1N\n");
            out.write("south:      0\n");
            out.write("east:       1E\n");
            out.write("west:       0\n");
            out.write("cols:       1\n");
            out.write("rows:       1\n");
            out.write("e-w resol:  1\n");
            out.write("n-s resol:  1\n");
            out.write("top:        1\n");
            out.write("bottom:     0\n");
            out.write("cols3:      1\n");
            out.write("rows3:      1\n");
            out.write("depths:     1\n");
            out.write("e-w resol3: 1\n");
            out.write("n-s resol3: 1\n");
            out.write("t-b resol:  1\n");
         }
         out.close();
      }
      catch (final IOException e) {
         throw (e);
      }
   }


   /*
    * Helper function for deleteTempMapset()
    */
   private static boolean deleteDirectory(final File path) {
      if (path.exists()) {
         final File[] files = path.listFiles();
         for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
               deleteDirectory(files[i]);
            }
            else {
               files[i].delete();
            }
         }
      }
      return (path.delete());
   }


   /**
    * Deletes the location and mapset(s) for GRASS data processing.
    */
   public static void deleteTempMapset() {

      final boolean bIsTemp = new Boolean(SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_USE_TEMP_MAPSET)).booleanValue();
      if (bIsTemp) {
         if ((getGrassMapsetFolder() != null) && (getGrassMapsetFolder().length() > 2)) {
            String tmpFolder;
            tmpFolder = new String(getGrassMapsetFolder().substring(0, getGrassMapsetFolder().lastIndexOf(File.separator)));
            if (new File(tmpFolder).exists()) {
               deleteDirectory(new File(tmpFolder));
            }
         }
      }
   }


   public static void writeColorTable(final Output out) {

      final FileOutputChannel channel = (FileOutputChannel) out.getOutputChannel();

      //Check if the GRASS module has created a customized color table
      final File colorTable = new File(getGrassMapsetFolder() + File.separator + "colr" + File.separator + out.getName());

      if (colorTable.exists()) {
         //Writer for output to simple ASCII file
         BufferedWriter output = null;

         //We have a GRASS color table: break up into tokens and write as an ASCII file
         //that's easy to parse.
         //Write original name of GRASS output map this is associated with into file
         try {
            final BufferedReader in = new BufferedReader(new FileReader(colorTable));
            String line = new String();
            String token[];
            String from;
            String to;
            String R;
            String G;
            String B;
            int component_no;
            int start_pos;
            int errors = 0;

            //Create a temporary file path to store the color table description
            String filename = channel.getFilename();
            filename = filename.substring(0, filename.lastIndexOf("."));
            filename = filename + "." + GrassUtils.colorTableExt;

            //Register temporary file for deletion after JVM terminates
            final File delFile = new File(filename);
            delFile.deleteOnExit();

            output = new BufferedWriter(new FileWriter(delFile));
            output.write(colorTableIdentifier + ";" + colorTableVersion + "\n");//Header

            while ((line = in.readLine()) != null) {
               if (!line.startsWith("%")) {//Skip header line
                  token = new String[2];
                  //First split into both ends of interval, around " " character.
                  from = null;
                  to = null;
                  errors = 0;
                  for (int i = 0; i < 2; i++) {
                     if (i == 0) {
                        token[i] = line.substring(0, line.indexOf(" "));
                     }
                     else {
                        token[i] = line.substring(line.lastIndexOf(" ") + 1, line.length());
                     }
                     if ((token[i] != null) && (token[i].length() > 1)) {//Process interval start token
                        R = null;
                        G = null;
                        B = null;
                        component_no = 0;
                        //First bit is pixel value
                        if (i == 0) {
                           from = token[i].substring(0, token[i].indexOf(":"));
                        }
                        else {
                           to = token[i].substring(0, token[i].indexOf(":"));
                        }
                        start_pos = token[i].indexOf(":");
                        while (start_pos != -1) {//Further tokenize string
                           token[i] = token[i].substring(start_pos + 1, token[i].length());
                           //Next bit is an RGB component value
                           if (token[i].indexOf(":") != -1) {
                              if (component_no == 0) {
                                 R = token[i].substring(0, token[i].indexOf(":"));
                              }
                              if (component_no == 1) {
                                 G = token[i].substring(0, token[i].indexOf(":"));
                              }
                              if (component_no == 2) {
                                 B = token[i].substring(0, token[i].indexOf(":"));
                              }
                           }
                           else {
                              if (component_no == 0) {
                                 R = token[i].substring(0, token[i].length());
                              }
                              if (component_no == 1) {
                                 G = token[i].substring(0, token[i].length());
                              }
                              if (component_no == 2) {
                                 B = token[i].substring(0, token[i].length());
                              }
                           }
                           start_pos = token[i].indexOf(":");
                           component_no++;
                        }
                        //Sometimes only "R" is specified (gray value)
                        if (G == null) {
                           if (R != null) {
                              G = new String(R);
                           }
                        }
                        if (B == null) {
                           if (R != null) {
                              B = new String(R);
                           }
                        }
                        //Check for errors in color table specification
                        if (R == null) {
                           errors++;
                        }
                        else {
                           if ((Integer.valueOf(R) < 0) || (Integer.valueOf(R) > 255)) {
                              errors++;
                           }
                        }
                        if (G == null) {
                           errors++;
                        }
                        else {
                           if ((Integer.valueOf(G) < 0) || (Integer.valueOf(G) > 255)) {
                              errors++;
                           }
                        }
                        if (B == null) {
                           errors++;
                        }
                        else {
                           if ((Integer.valueOf(B) < 0) || (Integer.valueOf(B) > 255)) {
                              errors++;
                           }
                        }
                        if ((i == 0) && (from == null)) {
                           errors++;
                        }
                        if ((i == 1) && (to == null)) {
                           errors++;
                        }
                        //Write only fully valid interval specifications
                        if (errors == 0) {
                           if (i == 0) {
                              output.write("FROM: " + from + "\n");
                           }
                           else {
                              output.write("TO: " + to + "\n");
                           }
                           output.write("R: " + R + "\n");
                           output.write("G: " + G + "\n");
                           output.write("B: " + B + "\n");
                        }
                     }
                  }
               }
            }
            if (output != null) {
               output.close();
            }
         }
         catch (final Exception e) {
            Sextante.addErrorToLog(Sextante.getText("grass_error_color_table"));
         }
      }
   }


   /**
    * Returns the grass mapset folder as set by the user. Returns a path to a folder in the system's temporary files location if
    * user is working with temporary GRASS mapset.
    * 
    * @return the current grass mapset folder
    */
   public static String getGrassMapsetFolder() {

      final boolean bIsTemp = new Boolean(SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_USE_TEMP_MAPSET)).booleanValue();
      if (bIsTemp) {
         return m_sGrassTempMapsetFolder;
      }
      else {
         return SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_MAPSET_FOLDER);
      }
   }


   /**
    * Creates a path and folder in the system's default temporary files path. This will be used to store temporary GRASS mapsets
    * for data processing. This will only create one new temporary mapset name per session, which should be all we ever need (the
    * actual temporary GRASS settings files within the mapset are written from scratch each time a GRASS module is run).
    */
   public static void createTempMapsetName() {
      UUID id;
      String tmpPrefix;
      String tmpSuffix;
      String tmpBase;
      String tmpFolder;

      id = UUID.randomUUID();
      tmpPrefix = new String(GrassUtils.TEMP_PREFIX);
      tmpSuffix = new String("_" + id);
      tmpBase = new String(System.getProperty("java.io.tmpdir"));
      if (tmpBase.endsWith(File.separator)) {
         tmpFolder = new String(tmpBase + tmpPrefix + tmpSuffix.replace('-', '_') + File.separator + "user");
      }
      else {
         tmpFolder = new String(tmpBase + File.separator + tmpPrefix + tmpSuffix.replace('-', '_') + File.separator + "user");
      }
      m_sGrassTempMapsetFolder = tmpFolder;

   }


   //
   /*
    * Returns the entire version string of the GRASS version we are running.
    * Returns "null" if anything goes wrong.
    */
   private static String getGrassVersion() {

      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_FOLDER);
      InputStreamReader isr = null;
      BufferedReader br = null;
      try {
         final File file = new File(sFolder + File.separator + "etc" + File.separator + "VERSIONNUMBER");
         isr = new InputStreamReader(new FileInputStream(file));
         br = new BufferedReader(isr);
         final String sLine = br.readLine().trim();
         return (sLine);
      }
      catch (final Exception e) {
         return (null);
      }
      finally {
         try {
            br.close();
            isr.close();
         }
         catch (final Exception e) {
            return (null);
         }
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
            try {
               GrassUtils.filterGRASSOutput(String.copyValueOf(line.toCharArray()).trim());
               //System.out.println(line);
            }
            catch (final GrassExecutionException e) {}
         }
      }
      catch (final IOException ioe) {}
   }
}
