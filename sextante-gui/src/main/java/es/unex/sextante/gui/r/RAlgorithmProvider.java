package es.unex.sextante.gui.r;

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
import es.unex.sextante.gui.settings.Setting;
import es.unex.sextante.gui.settings.SextanteRSettings;


public class RAlgorithmProvider
         implements
            IAlgorithmProvider {

   private HashMap<String, GeoAlgorithm> m_Algs;

   private final static ImageIcon        R_ICON = new ImageIcon(RAlgorithmProvider.class.getClassLoader().getResource(
                                                         "images/r.png"));


   public boolean canEditHelp() {

      return false;

   }


   public Object getAlgorithmHelp(final GeoAlgorithm alg) {

      return "";

   }


   public String getAlgorithmHelpFilename(final GeoAlgorithm alg,
                                          final boolean forceCurrentLocale) {

      return null;

   }


   public HashMap<String, GeoAlgorithm> getAlgorithms() {

      if (Boolean.parseBoolean(SextanteGUI.getSettingParameterValue(SextanteRSettings.R_ACTIVATE))) {
         return m_Algs;
      }
      else {
         return new HashMap<String, GeoAlgorithm>();
      }

   }


   public HashMap<String, Class> getCustomModelerParameterPanels() {

      return new HashMap<String, Class>();

   }


   public HashMap<String, Class> getCustomParameterPanels() {

      return new HashMap<String, Class>();

   }


   public ImageIcon getIcon() {

      return R_ICON;

   }


   public String getName() {

      return "R";

   }


   public Setting getSettings() {

      return new SextanteRSettings();
   }


   public void initialize() {

      m_Algs = new HashMap<String, GeoAlgorithm>();

      final GeoAlgorithm[] algs = RScriptsIO.loadRScriptsAsAlgorithms(RUtils.getScriptsFolder());

      for (int i = 0; i < algs.length; i++) {
         m_Algs.put(algs[i].getCommandLineName(), algs[i]);
      }

   }


   public void update() {

      initialize();

   }


   public HashMap<NameAndIcon, ArrayList<ToolboxAction>> getToolboxActions() {

      final HashMap<NameAndIcon, ArrayList<ToolboxAction>> map = new HashMap<NameAndIcon, ArrayList<ToolboxAction>>();
      final NameAndIcon nai = new NameAndIcon("R", R_ICON);
      final ArrayList<ToolboxAction> list = new ArrayList<ToolboxAction>();
      list.add(new RConsoleToolboxAction());
      map.put(nai, list);

      return map;
   }


   public IToolboxRightButtonAction[] getToolboxRightButtonActions() {

      return new IToolboxRightButtonAction[] { new EditRScriptAction() };

   }


   public static void addMessage(final String s) {

      Sextante.getLogger().addToLog(s, "R", s);

   }

}
