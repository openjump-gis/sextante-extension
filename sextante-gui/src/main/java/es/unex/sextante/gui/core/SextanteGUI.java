

package es.unex.sextante.gui.core;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.IInputFactory;
import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.cmd.ScriptAlgorithmProvider;
import es.unex.sextante.gui.help.HelpIO;
import es.unex.sextante.gui.modeler.ModelerAlgorithmProvider;
import es.unex.sextante.gui.settings.Setting;
import es.unex.sextante.gui.settings.SextanteFolderSettings;
import es.unex.sextante.gui.settings.SextanteGeneralSettings;


/**
 * This class centralizes most actions related to the SextanteGUI, containing methods to show dialogs and retrieve basic values
 * used by GUI elements
 */
public class SextanteGUI {

   public static final int                            HISTORY                     = 0;
   public static final int                            COMMANDLINE                 = 1;

   private static IInputFactory                       m_InputFactory;
   private static OutputFactory                       m_OutputFactory;
   private static IGUIFactory                         m_GUIFactory                = new DefaultGUIFactory();

   private static IPostProcessTaskFactory             m_PostProcessTaskFactory;

   private static JDialog                             m_LastCommandOriginParentPanel;
   private static int                                 m_iLastCommandOrigin;
   private static Frame                               m_MainFrame;

   private static boolean                             m_bShowOnlyActiveAlgorithms = false;
   private static String                              m_sOutputFolder;
   private static String                              m_sSextantePath;
   private static HashMap<String, String>             m_Settings                  = new HashMap<String, String>();
   private static HashMap<String, String>             m_DefaultSettings           = new HashMap<String, String>();
   private static IDataRenderer                       m_Renderer;


   private final static HashMap<String, Class>        m_ParametersPanel           = new HashMap<String, Class>();
   private final static HashMap<String, Class>        m_ModelerParametersPanel    = new HashMap<String, Class>();

   private final static ArrayList<IAlgorithmProvider> m_AlgorithmProviders        = new ArrayList<IAlgorithmProvider>();

   public final static ImageIcon                      SEXTANTE_ICON               = new ImageIcon(
                                                                                           SextanteGUI.class.getClassLoader().getResource(
                                                                                                    "images/module2.png"));

   static {

      m_AlgorithmProviders.add(new ScriptAlgorithmProvider());
      m_AlgorithmProviders.add(new ModelerAlgorithmProvider());

   }


   /**
    * Sets a new main frame. This will be used as the parent frame by SEXTANTE GUI elements
    * 
    * @param frame
    *                The main frame
    */
   public static void setMainFrame(final Frame frame) {

      m_MainFrame = frame;

   }


   /**
    * Returns the current main frame
    * 
    * @return the current main frame
    */
   public static Frame getMainFrame() {

      return m_MainFrame;

   }


   /** ************************************************************ */

   /**
    * Returns the current input factory
    * 
    * @see IInputFactory.
    * @return the current input factory
    */
   public static IInputFactory getInputFactory() {

      return m_InputFactory;

   }


   /**
    * Sets a new input factory as the current one
    * 
    * @param inputFactory
    *                the new input factory
    */
   public static void setInputFactory(final IInputFactory inputFactory) {

      m_InputFactory = inputFactory;

   }


   /**
    * Returns the current OutputFactory
    * 
    * @return the current OutputFactory
    */
   public static OutputFactory getOutputFactory() {

      return m_OutputFactory;

   }


   /**
    * sets a new output factory
    * 
    * @param outputFactory
    *                the new output factory
    */
   public static void setOutputFactory(final OutputFactory outputFactory) {

      m_OutputFactory = outputFactory;
      m_OutputFactory.setDefaultNoDataValue(getDefaultNoDataValue());

   }


   /**
    * Returns the current GUIFactory
    * 
    * @return the current GUIFactory
    */
   public static IGUIFactory getGUIFactory() {

      return m_GUIFactory;

   }


   /**
    * sets a new GUI factory
    * 
    * @param guiFactory
    *                the new GUI factory
    */
   public static void setGUIFactory(final IGUIFactory guiFactory) {

      m_GUIFactory = guiFactory;

   }


   /**
    * Returns the task to post-process the algorithm outputs, usually to add them to the GUI of the GIS app.
    * 
    * @param alg
    *                the algorithm to postprocess. Since this task will mainly deal with output results, the algorithm should have
    *                been previously executed, so it contains non-null output values
    * @param bShowResultsDialog
    *                if this parameter is true, the task will show the results dialog if the algorithm has produced some kind of
    *                output other than layers or tables. If it is false, those results will be added to the set of current
    *                results, but the results dialog will not be shown
    * 
    * @return a task to postprocess the given algorithm
    */
   public static Runnable getPostProcessTask(final GeoAlgorithm alg,
                                             final boolean bShowResultsDialog) {

      return m_PostProcessTaskFactory.getPostProcessTask(alg, bShowResultsDialog);

   }


   /**
    * Sets the current post process task factory
    * 
    * @param factory
    *                the new post-process task factory
    */
   public static void setPostProcessTaskFactory(final IPostProcessTaskFactory factory) {

      m_PostProcessTaskFactory = factory;

   }


   /** ******************************************* */

   /**
    * Adds an algorithm provider. This has to be done before calling initialize(), so the providers can be initialized as well.
    * 
    * @param provider
    *                the algorithm provider to add
    */
   public static void addAlgorithmProvider(final IAlgorithmProvider provider) {

      //providers are added at the beginning of the array, because models and scripts might depend on them
      m_AlgorithmProviders.add(0, provider);

   }


   /**
    * Initializes the GUI parameters and resources. It takes GUI resources (custom panels, etc.) from classpath jars. Should be
    * called before setting parameters using other methods from this class, except for the setSextantePath() and
    * setCustomDefaultSettings() methods.
    */
   public static void initialize() {

      GUIResources.addResourcesFromClasspath();
      _initialize();

   }


   /**
    * Initializes the GUI parameters and resources. It takes GUI resources (custom panels, etc.) from the specified urls. Should
    * be called before setting parameters using other methods from this class, except for the setSextantePath() method.
    * 
    * @param jars
    *                the urls of the jar files with custom GUI resources
    */
   public static void initialize(final URL[] jars) {

      GUIResources.addResourcesFromURLs(jars);
      _initialize();

   }


   /**
    * Initializes the GUI parameters and resources. It takes GUI resources (custom panels, etc.) from the specified folder. Should
    * be called before setting parameters using other methods from this class, except for the setSextantePath() method.
    * 
    * @param sFolder
    *                the folder with jars with custom GUI resources
    */
   public static void initialize(final String sFolder) {

      GUIResources.addResourcesFromFolder(sFolder);
      _initialize();


   }


   private static void _initialize() {

      setDefaultSettings();
      readConfigFile();

      //this loads built-in SEXTANTE algorithms and resources
      loadResources();

      //and this loads additional algorithms from algorithm providers
      for (int i = 0; i < m_AlgorithmProviders.size(); i++) {
         final IAlgorithmProvider provider = m_AlgorithmProviders.get(i);
         provider.initialize();
         if (provider.getAlgorithms().size() > 0) {
            Sextante.addAlgorithmsFromProvider(provider.getName(), provider.getAlgorithms());
            addCustomModelerParametersPanel(provider.getCustomModelerParameterPanels());
            addCustomParametersPanel(provider.getCustomParameterPanels());
         }
      }

   }


   private static void setDefaultSettings() {

      final Setting[] baseSettings = new Setting[] { new SextanteGeneralSettings(), new SextanteFolderSettings() };
      final ArrayList<IAlgorithmProvider> providers = SextanteGUI.getAlgorithmProviders();
      final Setting[] settings = new Setting[baseSettings.length + providers.size()];
      System.arraycopy(baseSettings, 0, settings, 0, baseSettings.length);
      for (int i = 0; i < providers.size(); i++) {
         settings[i + baseSettings.length] = providers.get(i).getSettings();
      }

      for (int i = 0; i < settings.length; i++) {
         final HashMap<String, String> map = settings[i].getInitValues();
         m_Settings.putAll(map);
      }

      if (m_DefaultSettings != null) {
         m_Settings.putAll(m_DefaultSettings);
      }

   }


   /**
    * This methods sets default setting values different from the hard-coded ones. It should be called right before initializing
    * this class
    * 
    * @param map
    *                the map with new default settings
    */
   public static void setCustomDefaultSettings(final HashMap<String, String> map) {

      m_DefaultSettings = map;

   }


   private static void loadResources() {

      final String[] sPanelClassName = GUIResources.getParameterPanelClassNames();
      for (final String element : sPanelClassName) {
         try {
            final Class panelClass = Class.forName(element);
            final String sAlgClassName = element.substring(0, element.indexOf("ParametersPanel")) + "Algorithm";
            final Class algClass = Class.forName(sAlgClassName);
            final GeoAlgorithm alg = (GeoAlgorithm) algClass.newInstance();
            m_ParametersPanel.put(alg.getCommandLineName(), panelClass);
         }
         catch (final Exception e) {/*ignore it if there are problems*/
         }
      }

      final String[] sModelerPanelClassName = GUIResources.getModelerParameterPanelClassNames();
      for (final String element : sModelerPanelClassName) {
         try {
            final Class panelClass = Class.forName(element);
            final String sAlgClassName = element.substring(0, element.indexOf("ModelerParametersPanel")) + "Algorithm";
            final Class algClass = Class.forName(sAlgClassName);
            final GeoAlgorithm alg = (GeoAlgorithm) algClass.newInstance();
            m_ModelerParametersPanel.put(alg.getCommandLineName(), panelClass);
         }
         catch (final Exception e) {/*ignore it if there are problems*/
         }
      }


   }


   /**
    * Returns the path to help files
    * 
    * @return the path to help files
    */
   public static String getHelpPath() {

      return m_sSextantePath + File.separator + "help";

   }


   /**
    * Sets the current path to sextante folder. This is the folder where help files and additional software should be located.
    * This should be done before initializing this class, since this folder is used by some algorithm providers
    * 
    * @param sPath
    *                the path to sextante help and additional programs
    */
   public static void setSextantePath(final String sPath) {

      m_sSextantePath = sPath;

   }


   /**
    * Returns the path to sextante help and additional programs
    * 
    * @return the path to sextante help and additional programs
    */
   public static String getSextantePath() {

      return m_sSextantePath;

   }


   /**
    * Returns the default folder for output data.
    * 
    * @return the default folder for output data.
    */
   public static String getOutputFolder() {

      if ((m_sOutputFolder == null) || m_sOutputFolder.trim().equals("")) {
         return System.getProperty("user.dir");
      }
      else {
         return m_sOutputFolder;
      }

   }


   /**
    * Returns the default value to represent "No Data" (null) raster cells.
    * 
    * @return the current "No Data" value.
    */
   private static Double getDefaultNoDataValue() {

      try {
         final double dNoDataValue = Double.parseDouble(SextanteGUI.getSettingParameterValue(SextanteGeneralSettings.DEFAULT_NO_DATA_VALUE));
         return dNoDataValue;
      }
      catch (final Exception e) {
         return new Double(-99999d);
      }

   }


   /**
    * Loads configuration parameters from the config file
    */
   private static void readConfigFile() {

      BufferedReader input = null;
      try {
         input = new BufferedReader(new FileReader(getConfigFile()));
         String sLine = null;
         while ((sLine = input.readLine()) != null) {
            final String[] sTokens = sLine.split("=");
            if (sTokens.length == 2) {
               m_Settings.put(sTokens[0], sTokens[1]);
            }
         }
      }
      catch (final FileNotFoundException e) {
      }
      catch (final IOException e) {
      }
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


   /**
    * Saves current settings to the config file
    */
   public static void saveSettings() {

      Writer output = null;
      try {
         output = new BufferedWriter(new FileWriter(getConfigFile()));
         final Set<String> set = m_Settings.keySet();
         final Iterator<String> iter = set.iterator();
         while (iter.hasNext()) {
            final String sKey = iter.next();
            final String sValue = m_Settings.get(sKey);
            output.write(sKey + "=" + sValue + "\n");
         }
      }
      catch (final IOException e) {
         Sextante.addErrorToLog(e);
      }
      finally {
         if (output != null) {
            try {
               output.close();
            }
            catch (final IOException e) {
               Sextante.addErrorToLog(e);
            }
         }
      }

   }


   /**
    * Returns the path to the SEXTANTE config file
    * 
    * @return the path to the SEXTANTE config file
    */
   private static String getConfigFile() {

      final String sPath = System.getProperty("user.home") + File.separator + "sextante";

      return sPath + File.separator + "sextante.settings";

   }


   /**
    * Returns the config folder
    * 
    * @return the config folder
    */
   public static String getUserFolder() {

      final String sPath = System.getProperty("user.home") + File.separator + "sextante";

      final File sextanteFolder = new File(sPath);
      if (!sextanteFolder.exists()) {
         sextanteFolder.mkdir();
      }
      return sPath;


   }


   /**
    * Returns the type of the last element from which a command-line command was executed
    * 
    * @return the type of the element from which a command-line command was executed. SextanteGUI.HISTORY if the last component
    *         was the history panel; SextanteGUI.COMMANDLINE if it was the regular SEXTANTE console
    */
   public static int getLastCommandOrigin() {

      return m_iLastCommandOrigin;

   }


   /**
    * Sets the type of the last element from which a command-line command was executed. This has to be called from any component
    * that allows execution of commands
    * 
    * @param iLast
    *                one of the following constants: SextanteGUI.HISTORY if the last component was the history panel;
    *                SextanteGUI.COMMANDLINE if it was the regular SEXTANTE console
    */
   public static void setLastCommandOrigin(final int iLast) {

      m_iLastCommandOrigin = iLast;

   }


   /**
    * Gets the dialog from which the last command--line command was executed. This will be used as the parent dialog for task
    * monitors or message dialogs generated by the execution of that command.
    * 
    * @return the dialog from which the last command--line command was executed
    */
   public static JDialog getLastCommandOriginParentDialog() {

      return m_LastCommandOriginParentPanel;

   }


   /**
    * Sets the dialog (if any) that contains the element from which the last command--line command was executed
    * 
    * @param parent
    *                the dialog (if any) that contains the element from which the last command--line command was executed
    */
   public static void setLastCommandOriginParentDialog(final JDialog parent) {

      m_LastCommandOriginParentPanel = parent;

   }


   /**
    * Returns true if only active algorithms (those that can be executed with the current data objects) should be shown in the
    * toolbox
    * 
    * @return true if only active algorithms (those that can be executed with the current data objects) should be shown in the
    *         toolbox
    */
   public static boolean getShowOnlyActiveAlgorithms() {

      return m_bShowOnlyActiveAlgorithms;

   }


   /**
    * Sets whether only active algorithms (those that can be executed with the current data objects) should be shown in the
    * toolbox
    * 
    * @param showOnlyActiveAlgorithms
    *                must be true if only active algorithms (those that can be executed with the current data objects) should be
    *                shown in the toolbox
    */
   public static void setShowOnlyActiveAlgorithms(final boolean showOnlyActiveAlgorithms) {

      m_bShowOnlyActiveAlgorithms = showOnlyActiveAlgorithms;

   }


   /**
    * puts a collection of settings into the settings map
    * 
    * @param settings
    *                the map with settings values.
    */
   public static void setSettings(final HashMap<String, String> settings) {

      m_Settings.putAll(settings);

   }


   /**
    * Modifies the passed string, so it can be used as a safe data object name (without special characters)
    * 
    * @param sName
    *                the name to modify
    * @return the modified safe name (with no special characters)
    */
   public static String modifyResultName(String sName) {

      sName = sName.replaceAll("\\.", "_");
      sName = sName.replaceAll(" ", "_");
      sName = sName.replaceAll("\\[", "_");
      sName = sName.replaceAll("\\]", "_");
      sName = sName.replaceAll("\\(", "_");
      sName = sName.replaceAll("\\)", "_");
      sName = sName.replaceAll("á", "a");
      sName = sName.replaceAll("é", "e");
      sName = sName.replaceAll("í", "i");
      sName = sName.replaceAll("ó", "o");
      sName = sName.replaceAll("ú", "u");
      sName = sName.replaceAll("ñ", "n");

      return sName;

   }


   private static void addCustomParametersPanel(final HashMap<String, Class> map) {

      m_ParametersPanel.putAll(map);

   }


   private static void addCustomModelerParametersPanel(final HashMap<String, Class> map) {

      m_ModelerParametersPanel.putAll(map);

   }


   public static Class getModelerParametersPanel(final String sName) {

      return m_ModelerParametersPanel.get(sName);

   }


   public static Class getParametersPanel(final String sName) {

      return m_ParametersPanel.get(sName);

   }


   public static ImageIcon getAlgorithmIcon(final GeoAlgorithm alg) {

      final String sName = Sextante.getAlgorithmProviderName(alg);
      for (int i = 0; i < m_AlgorithmProviders.size(); i++) {
         if (m_AlgorithmProviders.get(i).getName().equals(sName)) {
            return m_AlgorithmProviders.get(i).getIcon();
         }
      }
      return SEXTANTE_ICON;

   }


   public static ArrayList<IAlgorithmProvider> getAlgorithmProviders() {

      return m_AlgorithmProviders;

   }


   public static String getSettingParameterValue(final String sParameterName) {

      return m_Settings.get(sParameterName);

   }


   public static String setSettingParameterValue(final String sParameterName,
                                                 final String sValue) {

      return m_Settings.put(sParameterName, sValue);

   }


   public static void updateAlgorithmProvider(final Class providerClass) {

      for (int i = 0; i < m_AlgorithmProviders.size(); i++) {
         final IAlgorithmProvider provider = m_AlgorithmProviders.get(i);
         if (providerClass.isInstance(provider)) {
            provider.update();
            Sextante.addAlgorithmsFromProvider(provider.getName(), provider.getAlgorithms());
         }
      }

   }


   public static Object getAlgorithmHelp(final GeoAlgorithm alg) {

      final String sName = Sextante.getAlgorithmProviderName(alg);
      for (int i = 0; i < m_AlgorithmProviders.size(); i++) {
         if (m_AlgorithmProviders.get(i).getName().equals(sName)) {
            return m_AlgorithmProviders.get(i).getAlgorithmHelp(alg);
         }
      }
      String sFilename;
      String sPath;
      if (sName.equals("SEXTANTE")) {
         sFilename = alg.getCommandLineName() + ".xml";
         sPath = HelpIO.getHelpPath(alg, false);
         return HelpIO.getHelpAsHTMLCode(alg, sPath + File.separator + sFilename);
      }
      else {
         return ""; //TODO:create default help page
      }

   }


   public static String getAlgorithmHelpFilename(final GeoAlgorithm alg,
                                                 final boolean bForceCurrentLocale) {

      final String sName = Sextante.getAlgorithmProviderName(alg);
      for (int i = 0; i < m_AlgorithmProviders.size(); i++) {
         if (m_AlgorithmProviders.get(i).getName().equals(sName)) {
            return m_AlgorithmProviders.get(i).getAlgorithmHelpFilename(alg, bForceCurrentLocale);
         }
      }
      String sFilename;
      String sPath;
      if (sName.equals("SEXTANTE")) {
         sFilename = alg.getCommandLineName() + ".xml";
         sPath = HelpIO.getHelpPath(alg, false);
         return sPath + File.separator + sFilename;
      }
      else {
         return ""; //TODO:create default help page
      }

   }


   public static HashMap<NameAndIcon, ArrayList<ToolboxAction>> getToolboxActions() {


      final HashMap<NameAndIcon, ArrayList<ToolboxAction>> map = new HashMap<NameAndIcon, ArrayList<ToolboxAction>>();
      for (int i = 0; i < m_AlgorithmProviders.size(); i++) {
         final IAlgorithmProvider provider = m_AlgorithmProviders.get(i);
         map.putAll(provider.getToolboxActions());
      }
      map.putAll(SextanteGUI.getGUIFactory().getToolboxActions());
      return map;
   }


   public static IToolboxRightButtonAction[] getToolboxRightButtonActions() {

      final ArrayList<IToolboxRightButtonAction> list = new ArrayList<IToolboxRightButtonAction>();
      for (int i = 0; i < m_AlgorithmProviders.size(); i++) {
         final IAlgorithmProvider provider = m_AlgorithmProviders.get(i);
         final IToolboxRightButtonAction[] actions = provider.getToolboxRightButtonActions();
         for (int j = 0; j < actions.length; j++) {
            list.add(actions[j]);
         }
      }
      return list.toArray(new IToolboxRightButtonAction[0]);

   }


   /**
    * Returns the renderer to use for layers created by SEXTANTE algorithms
    * 
    */
   public static IDataRenderer getDataRenderer() {

      return m_Renderer;

   }


   public static void setDataRenderer(final IDataRenderer renderer) {

      m_Renderer = renderer;

   }
}
