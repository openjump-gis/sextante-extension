package es.unex.sextante.gui.cmd;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.IAlgorithmProvider;
import es.unex.sextante.gui.core.IToolboxRightButtonAction;
import es.unex.sextante.gui.core.NameAndIcon;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.core.ToolboxAction;
import es.unex.sextante.gui.help.HelpIO;
import es.unex.sextante.gui.settings.Setting;
import es.unex.sextante.gui.settings.SextanteScriptsSettings;

public class ScriptAlgorithmProvider
         implements
            IAlgorithmProvider {

   private HashMap<String, GeoAlgorithm> m_Algs;

   private final static ImageIcon        SCRIPT_ICON = new ImageIcon(ScriptAlgorithmProvider.class.getClassLoader().getResource(
                                                              "images/terminal.png"));


   public HashMap<String, GeoAlgorithm> getAlgorithms() {

      return m_Algs;

   }


   public HashMap<String, Class> getCustomModelerParameterPanels() {

      return new HashMap<String, Class>();

   }


   public HashMap<String, Class> getCustomParameterPanels() {

      return new HashMap<String, Class>();

   }


   public ImageIcon getIcon() {

      return SCRIPT_ICON;

   }


   public String getName() {

      return Sextante.getText("Scripts");

   }


   public void initialize() {

      m_Algs = new HashMap<String, GeoAlgorithm>();

      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteScriptsSettings.SCRIPTS_FOLDER);
      ScriptAlgorithm.resetInterpreter();
      final GeoAlgorithm[] algs = ScriptsIO.loadScriptsAsAlgorithms(sFolder);

      for (int i = 0; i < algs.length; i++) {
         m_Algs.put(algs[i].getCommandLineName(), algs[i]);
      }

   }


   public Setting getSettings() {

      return new SextanteScriptsSettings();

   }


   public void update() {

      initialize();

   }


   public String getAlgorithmHelp(final GeoAlgorithm alg) {

      final String sDescriptionFilename = ((ScriptAlgorithm) alg).getDescriptionFile();
      String sFilename = sDescriptionFilename.substring(sDescriptionFilename.lastIndexOf(File.separator));
      sFilename = sFilename + ".xml";
      return HelpIO.getHelpAsHTMLCode(alg, sFilename);


   }


   public String getAlgorithmHelpFilename(final GeoAlgorithm alg,
                                          final boolean bForceCurrentLocale) {

      final String sDescriptionFilename = ((ScriptAlgorithm) alg).getDescriptionFile();
      String sFilename = sDescriptionFilename.substring(sDescriptionFilename.lastIndexOf(File.separator));
      sFilename = sFilename + ".xml";

      return sFilename;

   }


   public boolean canEditHelp() {

      return true;

   }


   public HashMap<NameAndIcon, ArrayList<ToolboxAction>> getToolboxActions() {

      final HashMap<NameAndIcon, ArrayList<ToolboxAction>> map = new HashMap<NameAndIcon, ArrayList<ToolboxAction>>();
      final NameAndIcon nai = new NameAndIcon(Sextante.getText("Scripts"), SCRIPT_ICON);
      final ArrayList<ToolboxAction> list = new ArrayList<ToolboxAction>();
      list.add(new CreateScriptToolboxAction());
      list.add(new OpenCommandLineInterfaceAction());
      map.put(nai, list);

      return map;


   }


   public IToolboxRightButtonAction[] getToolboxRightButtonActions() {

      return new IToolboxRightButtonAction[] { new EditScriptAction() };

   }


}
