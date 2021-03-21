package es.unex.sextante.gui.settings;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.algorithm.FileSelectionPanel;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.WrongGrassFolderException;
import es.unex.sextante.gui.exceptions.WrongGrassMapsetFolderException;
import es.unex.sextante.gui.exceptions.WrongGrassWinShellException;
import es.unex.sextante.gui.grass.GrassAlgorithmProvider;


public class SextanteGrassSettingsPanel
         extends
            SettingPanel {

   //Officially supported GRASS version range
   public static final int    MAJOR_MIN = 6;
   public static final int    MAJOR_MAX = 6;
   public static final int    MINOR_MIN = 4;
   public static final int    MINOR_MAX = 4;


   private JLabel             jLabelGrassFolder;
   private FileSelectionPanel jGrassFolder;
   private JLabel             jLabelDescriptionLocation;
   private FileSelectionPanel jDescriptionLocation;
   private JLabel             jLabelDescriptionShell;
   private FileSelectionPanel jDescriptionShell;
   private JButton            jButtonHelp;
   private JLabel             jLabelSetupGRASSHelp;
   private JButton            jButtonSetupGRASS;
   private JPanel             jPanelHelp;
   private JCheckBox          jCheckBox3DV;
   private JCheckBox          jCheckBoxLatLon;
   private JCheckBox          jCheckBoxInPolylines;
   private JCheckBox          jCheckBoxTempMapset;
   private JCheckBox          jActivateCheckBox;


   @Override
   protected void initGUI() {

      final boolean bCanConfigureGrass = new Boolean(
               SextanteGUI.getSettingParameterValue(SextanteGrassSettings.CAN_CONFIGURE_GRASS)).booleanValue();

      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 3.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 3.0 },
               { 3.0, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM,
                        TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM,
                        TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM,
                        TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL, 33.0,
                        TableLayoutConstants.FILL } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      this.setLayout(thisLayout);
      {
         jLabelGrassFolder = new JLabel();
         this.add(jLabelGrassFolder, "1, 1");
         jLabelGrassFolder.setText(Sextante.getText("GRASS_folder"));
         jGrassFolder = new FileSelectionPanel(true, true, (String[]) null, Sextante.getText("GRASS_folder"));
         this.add(jGrassFolder, "2, 1");
         jGrassFolder.setFilepath(SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_FOLDER));
         //jLabelGrassFolder.setVisible(bCanConfigureGrass);
         //jGrassFolder.setVisible(bCanConfigureGrass);

         jCheckBoxTempMapset = new JCheckBox();
         jCheckBoxTempMapset.setText(Sextante.getText("GRASS_use_temp_mapset"));
         jCheckBoxTempMapset.setSelected(new Boolean(
                  SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_USE_TEMP_MAPSET)).booleanValue());
         this.add(jCheckBoxTempMapset, "1, 3, 2, 3");
         jCheckBoxTempMapset.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               jCheckBoxTempMapsetActionPerformed(evt);
            }
         });
         jCheckBoxTempMapset.setVisible(false);

         jCheckBoxLatLon = new JCheckBox();
         jLabelDescriptionLocation = new JLabel();
         jDescriptionLocation = new FileSelectionPanel(true, true, (String[]) null, Sextante.getText("GRASS_mapset"));

         if (jCheckBoxTempMapset.isSelected()) {
            this.add(jCheckBoxLatLon, "1, 5, 2, 5");
            jCheckBoxLatLon.setText(Sextante.getText("GRASS_lat_lon_mode"));
            jCheckBoxLatLon.setSelected(new Boolean(
                     SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_LAT_LON_MODE)).booleanValue());
         }
         else {
            this.add(jLabelDescriptionLocation, "1, 5");
            jLabelDescriptionLocation.setText(Sextante.getText("GRASS_mapset"));
            this.add(jDescriptionLocation, "2, 5");
            jDescriptionLocation.setFilepath(SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_MAPSET_FOLDER));
         }

         jCheckBox3DV = new JCheckBox();
         jCheckBox3DV.setText(Sextante.getText("grass_input_3d"));
         jCheckBox3DV.setSelected(new Boolean(SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_3D_V_MODE)).booleanValue());
         this.add(jCheckBox3DV, "1, 7, 2, 7");

         jCheckBoxInPolylines = new JCheckBox();
         jCheckBoxInPolylines.setText(Sextante.getText("grass_import_polylines"));
         jCheckBoxInPolylines.setSelected(new Boolean(
                  SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_IN_POLYLINES)).booleanValue());
         this.add(jCheckBoxInPolylines, "1, 8, 2, 8");
      }

      if (Sextante.isWindows()) {
         //On Windows, we need a shell interpreter for GRASS (sh.exe)
         jLabelDescriptionShell = new JLabel();
         this.add(jLabelDescriptionShell, "1, 9");
         jLabelDescriptionShell.setText(Sextante.getText("grass_windows_shell"));
         final String[] ext = new String[1];
         ext[0] = "sh.exe";
         jDescriptionShell = new FileSelectionPanel(false, true, ext, Sextante.getText("grass_windows_shell"));
         this.add(jDescriptionShell, "2, 9");
         jDescriptionShell.setFilepath(SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_WIN_SHELL));
         //jLabelDescriptionShell.setVisible(bCanConfigureGrass);
         //jDescriptionShell.setVisible(bCanConfigureGrass);
      }

      {
         //         jLabelSetupGRASSHelp = new JLabel();
         //         this.add(jLabelSetupGRASSHelp, "1,10");
         //         jLabelSetupGRASSHelp.setText(Sextante.getText("grass_setup_help"));
         //         jButtonSetupGRASS = new JButton();
         //         this.add(jButtonSetupGRASS, "2,10");
         //         jButtonSetupGRASS.setText(Sextante.getText("grass_setup"));
         //         jButtonSetupGRASS.addActionListener(new ActionListener() {
         //            public void actionPerformed(final ActionEvent evt) {
         //               jButtonSetupGRASSActionPerformed(evt);
         //            }
         //         });
         //jLabelSetupGRASSHelp.setVisible(bCanConfigureGrass);
         //jButtonSetupGRASS.setVisible(bCanConfigureGrass);
      }

      {
         //         jButtonHelp = new JButton();
         //         jButtonHelp.setText(Sextante.getText("Help"));
         //         jButtonHelp.setMinimumSize(new java.awt.Dimension(14, 7));
         //         jButtonHelp.setMaximumSize(new java.awt.Dimension(140, 70));
         //         jButtonHelp.setPreferredSize(new java.awt.Dimension(85, 21));
         //         jButtonHelp.addActionListener(new ActionListener() {
         //            public void actionPerformed(final ActionEvent evt) {
         //               SextanteGUI.getGUIFactory().showHelpDialog("providers");
         //            }
         //         });
         //         jPanelHelp = new JPanel();
         //         final FlowLayout jPanelHelpLayout = new FlowLayout();
         //         jPanelHelpLayout.setAlignment(FlowLayout.RIGHT);
         //         jPanelHelpLayout.setHgap(0);
         //         jPanelHelpLayout.setVgap(0);
         //         jPanelHelp.setLayout(jPanelHelpLayout);
         //         jPanelHelp.add(jButtonHelp);
         //         this.add(jPanelHelp, "2, 13");

         jActivateCheckBox = new JCheckBox(Sextante.getText("ActivateProvider"));
         final String sActivate = SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_ACTIVATE);
         final boolean bActivate = Boolean.parseBoolean(sActivate);
         jActivateCheckBox.setSelected(bActivate);
         jActivateCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
               setCursor(new Cursor(Cursor.WAIT_CURSOR));
               SextanteGUI.setSettingParameterValue(SextanteGrassSettings.GRASS_ACTIVATE, new Boolean(
                        jActivateCheckBox.isSelected()).toString());
               SextanteGUI.updateAlgorithmProvider(GrassAlgorithmProvider.class);
               setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
         });
         this.add(jActivateCheckBox, "1,13");
      }
   }


   @Override
   public HashMap<String, String> getValues() {

      final HashMap<String, String> map = new HashMap<String, String>();

      final String path = jGrassFolder.getFilepath();
      map.put(SextanteGrassSettings.GRASS_FOLDER, path);
      final String locationPath = jDescriptionLocation.getFilepath();
      map.put(SextanteGrassSettings.GRASS_MAPSET_FOLDER, locationPath);
      if (Sextante.isWindows()) {
         final String shellPath = jDescriptionShell.getFilepath();
         if (shellPath != null) {
            map.put(SextanteGrassSettings.GRASS_WIN_SHELL, shellPath);
         }
      }
      map.put(SextanteGrassSettings.GRASS_3D_V_MODE, new Boolean(jCheckBox3DV.isSelected()).toString());
      map.put(SextanteGrassSettings.GRASS_IN_POLYLINES, new Boolean(jCheckBoxInPolylines.isSelected()).toString());
      map.put(SextanteGrassSettings.GRASS_LAT_LON_MODE, new Boolean(jCheckBoxLatLon.isSelected()).toString());
      map.put(SextanteGrassSettings.GRASS_USE_TEMP_MAPSET, new Boolean(jCheckBoxTempMapset.isSelected()).toString());
      map.put(SextanteGrassSettings.GRASS_ACTIVATE, new Boolean(jActivateCheckBox.isSelected()).toString());
      return map;

   }


   //Attempt to setup GRASS
   private void jButtonSetupGRASSActionPerformed(final ActionEvent evt) {

      //we set this values here in advance, since they are needed to perform grass initialization. Have to look for a workaround to avoid this...
      final HashMap<String, String> map = getValues();
      SextanteGUI.setSettings(map);

      GrassAlgorithmProvider.deleteDescriptionFiles();
      GrassAlgorithmProvider.deleteAlgorithms();

      boolean failed = false;
      //1: GRASS installation folder
      try {
         this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         checkGrassFolder();
         this.setCursor(Cursor.getDefaultCursor());
      }
      catch (final WrongGrassFolderException e) {
         this.setCursor(Cursor.getDefaultCursor());
         JOptionPane.showMessageDialog(null, Sextante.getText("grass_error_binaries_folder"),
                  Sextante.getText("grass_error_title"), JOptionPane.ERROR_MESSAGE);
         jGrassFolder.setFilepath("");
         failed = true;
      }
      finally {
         this.setCursor(Cursor.getDefaultCursor());
      }
      //2: GRASS Mapset folder
      if (!jCheckBoxTempMapset.isSelected() && (failed == false)) {
         try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            checkGrassMapsetFolder(jDescriptionLocation.getFilepath());
            this.setCursor(Cursor.getDefaultCursor());
         }
         catch (final WrongGrassMapsetFolderException e) {
            this.setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(null, Sextante.getText("grass_error_mapset_folder"),
                     Sextante.getText("grass_error_title"), JOptionPane.ERROR_MESSAGE);
            failed = true;
            jDescriptionLocation.setFilepath("");
         }
         finally {
            this.setCursor(Cursor.getDefaultCursor());
         }
      }
      //3: On Windows: sh.exe
      if (Sextante.isWindows() && (failed == false)) {
         try {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (checkGrassWinShell(jDescriptionShell.getFilepath()) > 0) {
               this.setCursor(Cursor.getDefaultCursor());
               JOptionPane.showMessageDialog(null, Sextante.getText("grass_warning_missing_cmd"),
                        Sextante.getText("grass_warning_title"), JOptionPane.WARNING_MESSAGE);
            }
            this.setCursor(Cursor.getDefaultCursor());
         }
         catch (final WrongGrassWinShellException e) {
            this.setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(null, Sextante.getText("grass_error_win_shell_binary") + "\n"
                                                + Sextante.getText("grass_shell_url"), Sextante.getText("grass_error_title"),
                     JOptionPane.ERROR_MESSAGE);
            failed = true;
            GrassAlgorithmProvider.deleteAlgorithms();
            SextanteGUI.getGUIFactory().updateToolbox();
            jDescriptionShell.setFilepath("");
         }
         finally {
            this.setCursor(Cursor.getDefaultCursor());
         }
      }

      //4: GRASS version
      if (!isSupported() && (failed == false)) {
         this.setCursor(Cursor.getDefaultCursor());
         JOptionPane.showMessageDialog(null, Sextante.getText("grass_warning_version"), Sextante.getText("grass_warning_title"),
                  JOptionPane.WARNING_MESSAGE);
      }

      //Setup GRASS!!!
      int num_algs = 0;
      try {
         this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         num_algs = GrassAlgorithmProvider.createAlgorithmsDescriptionFiles();
         this.setCursor(Cursor.getDefaultCursor());
      }
      catch (final Exception e) {
         this.setCursor(Cursor.getDefaultCursor());
         JOptionPane.showMessageDialog(null, Sextante.getText("grass_error_setup"), "", JOptionPane.ERROR_MESSAGE);
      }
      finally {
         this.setCursor(Cursor.getDefaultCursor());
      }
      this.setCursor(Cursor.getDefaultCursor());
      if (num_algs > 0) {
         //Success
         SextanteGUI.updateAlgorithmProvider(GrassAlgorithmProvider.class);
         final HashMap<String, GeoAlgorithm> algs = Sextante.getAlgorithms().get("GRASS");
         int iNumAlgs = 0;
         if (algs != null) {
            iNumAlgs = algs.size();
         }
         JOptionPane.showMessageDialog(null, Sextante.getText("grass_info_setup_success") + " " + iNumAlgs + ". ",
                  Sextante.getText("grass_info_title"), JOptionPane.INFORMATION_MESSAGE);
      }
   }


   private void jCheckBoxTempMapsetActionPerformed(final ActionEvent evt) {

      if (jCheckBoxTempMapset.isSelected()) {
         this.remove(jLabelDescriptionLocation);
         this.remove(jDescriptionLocation);
         this.add(jCheckBoxLatLon, "1, 5, 2, 5");
         jCheckBoxLatLon.setText(Sextante.getText("GRASS_lat_lon_mode"));
         jCheckBoxLatLon.setSelected(new Boolean(SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_LAT_LON_MODE)).booleanValue());
      }
      else {
         this.remove(jCheckBoxLatLon);
         this.add(jLabelDescriptionLocation, "1, 5");
         this.add(jDescriptionLocation, "2, 5");
         jLabelDescriptionLocation.setText(Sextante.getText("GRASS_mapset"));
         jDescriptionLocation.setFilepath(SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_MAPSET_FOLDER));
      }
      jCheckBoxLatLon.repaint();
      jLabelDescriptionLocation.repaint();
      jDescriptionLocation.repaint();
      this.repaint();
   }


   private void checkGrassFolder() throws WrongGrassFolderException {

      final String grassFolder = jGrassFolder.getFilepath();
      //Minimal set of GRASS modules we need
      final String[] check_modules = { "g.region", "g.remove", "r.in.gdal", "r.info", "r.null", "r.out.gdal", "v.in.ogr",
               "v.info", "v.out.ogr", null };

      //Check if this is actually a valid shell and throw an error, if it's not.
      if (grassFolder == null) {
         throw new WrongGrassFolderException();
      }

      if (grassFolder.length() < 2) {
         throw new WrongGrassFolderException();
      }

      if (grassFolder.trim().equals("")) {
         throw new WrongGrassFolderException();
      }

      File check = new File(grassFolder);
      if (!check.exists()) {
         throw new WrongGrassFolderException();
      }

      check = new File(grassFolder + File.separator + "etc" + File.separator + "VERSIONNUMBER");
      if (!check.exists()) {
         throw new WrongGrassFolderException();
      }

      //Check for minimal set of GRASS modules
      int i = 0;
      while (check_modules[i] != null) {
         if (Sextante.isUnix() || Sextante.isMacOSX()) {
            check = new File(grassFolder + File.separator + "bin" + File.separator + check_modules[i]);
         }
         else {
            check = new File(grassFolder + File.separator + "bin" + File.separator + check_modules[i] + ".exe");
         }
         if (!check.exists()) {
            throw new WrongGrassFolderException();
         }
         i++;
      }

   }


   /**
    * Checks whether the GRASS mapset folder is valid.
    * 
    * @param grassMapsetFolder
    *                the path to the GRASS mapset (folder)
    */
   private void checkGrassMapsetFolder(final String grassMapsetFolder) throws WrongGrassMapsetFolderException {

      //Check if this is actually a valid mapset folder and throw an error, if it's not.
      if (grassMapsetFolder == null) {
         throw new WrongGrassMapsetFolderException();
      }

      if (grassMapsetFolder.length() < 2) {
         throw new WrongGrassMapsetFolderException();
      }

      if (grassMapsetFolder.trim().equals("")) {
         throw new WrongGrassMapsetFolderException();
      }

      File check = new File(grassMapsetFolder);
      if (!check.exists()) {
         throw new WrongGrassMapsetFolderException();
      }

      check = new File(grassMapsetFolder + File.separator + "WIND");
      if (!check.exists()) {
         throw new WrongGrassMapsetFolderException();
      }

   }


   private int checkGrassWinShell(final String grassWinShell) throws WrongGrassWinShellException {

      int num_missing = 0;

      //Minimal set of command line tools we need to run GRASS scripts
      final String[] check_commands = { "which", "gawk.exe", "cut.exe", "grep.exe", "basename.exe", "sed.exe", "install.exe",
               "curl.exe", "bc.exe", "wc.exe", "paste.exe", "head.exe", "tail.exe", "cat.exe", "expr.exe", "xargs.exe", "ls.exe",
               "sort.exe", "cs2cs.exe", "gdalwarp.exe", "unzip.exe" };

      //r.in.aster needs gdalwarp
      //r.in.srtm needs unzip
      //bc.exe needs readline5.dll
      //v.in.garmin needs gpstrans and gardump
      //v.in/out.gpsbabel need gpsbabel
      //some scripts need curl
      //some scripts need cs2cs


      //Check if this is actually a valid shell and throw an error, if it's not.
      if (grassWinShell == null) {
         throw new WrongGrassWinShellException();
      }

      if (grassWinShell.length() < 2) {
         throw new WrongGrassWinShellException();
      }

      if (grassWinShell.trim().equals("")) {
         throw new WrongGrassWinShellException();
      }

      final File check = new File(grassWinShell);
      if (!check.exists()) {
         throw new WrongGrassWinShellException();
      }

      //Look for required (recommended) binaries in "bin" folders of MSYS
      //and GRASS installation.
      //Note: if they are missing here, they could still be in some user-defined
      //folder in %PATH%, so we only issue a warning and log all "missing" binary names.
      String shToolsPath = grassWinShell;
      shToolsPath = shToolsPath.substring(0, shToolsPath.lastIndexOf(File.separator));
      String GrassPath = jGrassFolder.getFilepath();
      GrassPath = GrassPath + File.separator + "bin";
      for (final String element : check_commands) {
         boolean missing = false;
         final File check_sh = new File(shToolsPath + File.separator + element);
         final File check_grass = new File(GrassPath + File.separator + element);
         if (!check_sh.exists() && !check_grass.exists()) {
            missing = true;
         }
         if (missing == true) {
            Sextante.addWarningToLog("SEXTANTE GRASS interface: External command " + element + " not found in either '"
                                     + shToolsPath + "' or '" + GrassPath + "'");
            num_missing++;
         }
      }

      return (num_missing);
   }


   /*
    * Returns the major version number of the GRASS version we are running.
    * Returns "-1" if anything goes wrong.
    */
   private static int getGrassMajorVersion() {

      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_FOLDER);
      InputStreamReader isr = null;
      BufferedReader br = null;
      try {
         final File file = new File(sFolder + File.separator + "etc" + File.separator + "VERSIONNUMBER");
         isr = new InputStreamReader(new FileInputStream(file));
         br = new BufferedReader(isr);
         final String sLine = br.readLine();
         final String[] sNumbers = sLine.split("\\.");
         final String sMajor = sNumbers[0];
         return (Integer.parseInt(sMajor));
      }
      catch (final Exception e) {
         return (-1);
      }
      finally {
         try {
            br.close();
            isr.close();
         }
         catch (final Exception e) {
            return (-1);
         }
      }
   }


   /*
    * Returns the minor version number of the GRASS version we are running.
    * Returns "-1" if anything goes wrong.
    */
   private static int getGrassMinorVersion() {

      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_FOLDER);
      InputStreamReader isr = null;
      BufferedReader br = null;
      try {
         final File file = new File(sFolder + File.separator + "etc" + File.separator + "VERSIONNUMBER");
         isr = new InputStreamReader(new FileInputStream(file));
         br = new BufferedReader(isr);
         final String sLine = br.readLine();
         final String[] sNumbers = sLine.split("\\.");
         final String sMinor = sNumbers[1];
         return (Integer.parseInt(sMinor));
      }
      catch (final Exception e) {
         return (-1);
      }
      finally {
         try {
            br.close();
            isr.close();
         }
         catch (final Exception e) {
            return (-1);
         }
      }
   }


   /*
    * Returns true only if the detected GRASS version is not too old or too
    * young to be supported by this interface.
    */
   public static boolean isSupported() {
      if ((getGrassMajorVersion() < MAJOR_MIN) || (getGrassMajorVersion() > MAJOR_MAX)) {
         return (false);
      }
      if ((getGrassMinorVersion() < MINOR_MIN) || (getGrassMinorVersion() > MINOR_MAX)) {
         return (false);
      }
      return (true);
   }


}
