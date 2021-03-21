package es.unex.sextante.gui.grass;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ImageIcon;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.IAlgorithmProvider;
import es.unex.sextante.gui.core.IToolboxRightButtonAction;
import es.unex.sextante.gui.core.NameAndIcon;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.core.ToolboxAction;
import es.unex.sextante.gui.exceptions.WrongGrassFolderException;
import es.unex.sextante.gui.settings.Setting;
import es.unex.sextante.gui.settings.SextanteGrassSettings;

public class GrassAlgorithmProvider
         implements
            IAlgorithmProvider {

   private static HashMap<String, GeoAlgorithm> m_Algs     = new HashMap<String, GeoAlgorithm>();

   private static StringBuffer                  m_Message  = new StringBuffer();

   private final static ImageIcon               GRASS_ICON = new ImageIcon(
                                                                    GrassAlgorithmProvider.class.getClassLoader().getResource(
                                                                             "images/grass.png"));


   private static GrassAlgorithm createAlgorithm(final String sFile) {

      final GrassAlgorithm alg = new GrassAlgorithm();
      try {
         alg.initialize(sFile);
         return alg;
      }
      catch (final UnwrappableGrassProcessException e) {
         return null;
      }

   }


   public void initialize() {

      GrassUtils.createTempMapsetName();
      createAlgorithmsMap();

   }


   private void createAlgorithmsMap() {

      m_Algs.clear();

      try {
         final File file = new File(getGrassDescriptionFolder());
         final String[] files = file.list();
         if (files != null) {
            for (final String element : files) {
               if (element.endsWith(".xml")) {
                  final GrassAlgorithm alg = createAlgorithm(getGrassDescriptionFolder() + File.separator + element);
                  if ((alg != null) && !GrassBlackList.isInBlackList(alg)) {
                     m_Algs.put(alg.getCommandLineName(), alg);
                  }
               }
            }
         }
      }
      catch (final Exception e) {
         m_Algs.clear();
      }

   }


   /**
    * Deletes algorithm descriptions and help files
    */
   public static void deleteDescriptionFiles() {

      final File file = new File(getGrassDescriptionFolder());
      final String[] files = file.list();

      if (files != null) {
         for (final String element : files) {
            new File(file.getAbsoluteFile() + File.separator + element).delete();
         }
      }

   }


   /**
    * Creates xml files calling grass commands using the --interface-description modifier
    */
   public static int createAlgorithmsDescriptionFiles() throws WrongGrassFolderException {

      int iAlgorithms = 0;
      final StringBuffer sb = new StringBuffer();
      try {

         //Get modules from "bin" folder
         final String sFolder = SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_FOLDER);
         File file = new File(sFolder + File.separator + "bin");
         String[] files = file.list();
         if (files != null) {
            if (Sextante.isUnix() || Sextante.isMacOSX()) {
               for (int i = 0; i < files.length; i++) {
                  if ((files[i].startsWith("v.") || files[i].startsWith("r.")) && !files[i].startsWith("r3.")
                      && !files[i].equals("r.mapcalc") && !files[i].equals("r3.mapcalc")) {
                     final String sCommand = files[i];
                     if (!GrassBlackList.isInBlackList(sCommand)) {
                        iAlgorithms++;
                        sb.append(sCommand + " --interface-description > \"" + getGrassDescriptionFolder() + File.separator
                                  + sCommand + ".xml\"\n");
                     }
                  }
               }
               //Get modules from "scripts" folder
               file = new File(sFolder + File.separator + "scripts");
               files = file.list();
               if (files != null) {
                  for (int i = 0; i < files.length; i++) {
                     if ((files[i].startsWith("v.") || files[i].startsWith("r.")) && !files[i].equals("r.out.gdal.sh")
                         && !files[i].startsWith("r3.")) {
                        final String sCommand = files[i];
                        if (!GrassBlackList.isInBlackList(sCommand)) {
                           iAlgorithms++;
                           sb.append(sCommand + " --interface-description > \"" + getGrassDescriptionFolder() + File.separator
                                     + sCommand + ".xml\"\n");
                        }
                     }
                  }
               }
            }
            else {//Windows: scripts and C binaries are all in one folder
               for (int i = 0; i < files.length; i++) {
                  if ((files[i].endsWith(".exe") || files[i].endsWith(".bat"))
                      //if ( (files[i].endsWith(".exe") )
                      && (files[i].startsWith("v.") || files[i].startsWith("r.")) && !files[i].startsWith("r3.")
                      && !files[i].contains("r.out.gdal.sh") && !files[i].equals("r.mapcalc.exe")
                      && !files[i].equals("r3.mapcalc.exe")) {
                     final String sCommand = files[i].substring(0, files[i].length() - 4);
                     if (!GrassBlackList.isInBlackList(sCommand)) {
                        iAlgorithms++;
                        if (files[i].endsWith(".bat")) {
                           sb.append("cmd.exe /C " + sCommand + " --interface-description > \"" + getGrassDescriptionFolder()
                                     + File.separator + sCommand + ".xml\"\n");
                        }
                        else {
                           sb.append(sCommand + " --interface-description > \"" + getGrassDescriptionFolder() + File.separator
                                     + sCommand + ".xml\"\n");
                        }
                     }
                  }
               }
            }
         }

         if (iAlgorithms == 0) {
            throw new WrongGrassFolderException();
         }
      }
      catch (final Exception e) {
         throw new WrongGrassFolderException();
      }

      try {
         final boolean bIsTempMapset = new Boolean(
                  SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_USE_TEMP_MAPSET)).booleanValue();
         if (bIsTempMapset) {
            GrassUtils.createTempMapset();
         }
         GrassUtils.runGRASS(sb, "Creating GRASS algorithm descriptions", null);
      }
      catch (final Exception e) {
         throw new WrongGrassFolderException();
      }

      Sextante.addInfoToLog("SEXTANTE GRASS interface: Done setting up GRASS.");

      //createAlgorithmSiblings();

      return iAlgorithms;
   }


   /**
    * Returns the folder where grass description files (xml file generated using the grass --interface-description modifier) are
    * located
    * 
    * @return Returns the folder where grass description files (xml file generated using the grass --interface-description
    *         modifier) are located
    */
   public static String getGrassDescriptionFolder() {

      final String sPath = SextanteGUI.getSextantePath() + File.separator + "grass" + File.separator + "description";
      //System.getProperty("user.home") + File.separator + "sextante" + File.separator + "grass";

      final File file = new File(sPath);
      if (!file.exists()) {
         file.mkdir();
      }

      return sPath;

   }


   /**
    * Returns the algorithm corresponding to a given grass algorithm name
    * 
    * @param sAlgName
    *                the name of the grass algorithm (the grass command to execute it)
    * @return the algorithm corresponding to the passed grass algorithm name
    */
   public static GrassAlgorithm getGrassAlgorithm(final String sAlgName) {


      return (GrassAlgorithm) m_Algs.get(sAlgName);

   }


   public HashMap<String, GeoAlgorithm> getAlgorithms() {

      if (Boolean.parseBoolean(SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_ACTIVATE))) {
         return m_Algs;
      }
      else {
         return new HashMap<String, GeoAlgorithm>();
      }
   }


   public String getName() {

      return "GRASS";

   }


   public HashMap<String, Class> getCustomModelerParameterPanels() {

      final HashMap<String, Class> map = new HashMap<String, Class>();
      final Set<String> set = m_Algs.keySet();
      final Iterator<String> iter = set.iterator();
      while (iter.hasNext()) {
         map.put(iter.next(), GrassModelerParametersPanel.class);
      }

      return map;

   }


   public HashMap<String, Class> getCustomParameterPanels() {

      final HashMap<String, Class> map = new HashMap<String, Class>();
      final Set<String> set = m_Algs.keySet();
      final Iterator<String> iter = set.iterator();
      while (iter.hasNext()) {
         map.put(iter.next(), GrassParametersPanel.class);
      }

      return map;

   }


   public ImageIcon getIcon() {

      return GRASS_ICON;

   }


   public Setting getSettings() {

      return new SextanteGrassSettings();

   }


   public void update() {

      createAlgorithmsMap();

   }


   public Object getAlgorithmHelp(final GeoAlgorithm alg) {


      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_FOLDER);
      String sName = alg.getName();
      if (sName.contains("(")) {//sibling
         sName = sName.substring(0, sName.indexOf("(")).trim();
      }
      final String sFilename = sName + ".html";
      final String sURLPath = "file:///" + sFolder + File.separator + "docs" + File.separator + "html" + File.separator
                              + sFilename;

      try {
         return new URL(sURLPath);
      }
      catch (final MalformedURLException e1) {
         return null;
      }
   }


   public String getAlgorithmHelpFilename(final GeoAlgorithm alg,
                                          final boolean forceCurrentLocale) {

      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteGrassSettings.GRASS_FOLDER);
      final String sFilename = alg.getName() + ".html";
      final String sFullPath = sFolder + File.separator + "docs" + File.separator + "html" + File.separator + sFilename;

      return sFullPath;

   }


   public boolean canEditHelp() {

      return false;

   }


   public static void deleteAlgorithms() {

      m_Algs.clear();

   }


   public HashMap<NameAndIcon, ArrayList<ToolboxAction>> getToolboxActions() {

      return new HashMap<NameAndIcon, ArrayList<ToolboxAction>>();

   }


   public IToolboxRightButtonAction[] getToolboxRightButtonActions() {

      return new IToolboxRightButtonAction[0];

   }


   public static void addMessage(final String s) {


      m_Message.append(s + "\n");

   }


   public static void publishMessage(final String sDescription) {

      Sextante.getLogger().addToLog(m_Message.toString(), "GRASS", sDescription);

   }


}
