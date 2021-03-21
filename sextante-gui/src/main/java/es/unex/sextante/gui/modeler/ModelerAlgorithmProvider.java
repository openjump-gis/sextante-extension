package es.unex.sextante.gui.modeler;

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
import es.unex.sextante.gui.grass.GrassAlgorithmProvider;
import es.unex.sextante.gui.help.HelpIO;
import es.unex.sextante.gui.settings.Setting;
import es.unex.sextante.gui.settings.SextanteModelerSettings;

public class ModelerAlgorithmProvider
         implements
            IAlgorithmProvider {

   private HashMap<String, GeoAlgorithm> m_Algs;

   private final static ImageIcon        MODEL_ICON = new ImageIcon(GrassAlgorithmProvider.class.getClassLoader().getResource(
                                                             "images/model.png"));


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

      return MODEL_ICON;

   }


   public String getName() {

      return Sextante.getText("Models");

   }


   public void initialize() {

      m_Algs = new HashMap<String, GeoAlgorithm>();

      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteModelerSettings.MODELS_FOLDER);
      final GeoAlgorithm[] algs = ModelAlgorithmIO.loadModelsAsAlgorithms(sFolder);

      for (int i = 0; i < algs.length; i++) {
         m_Algs.put(algs[i].getCommandLineName(), algs[i]);
      }

   }


   public Setting getSettings() {

      return new SextanteModelerSettings();

   }


   public void update() {

      initialize();

   }


   public String getAlgorithmHelp(final GeoAlgorithm alg) {

      final String sModelFilename = ((ModelAlgorithm) alg).getFilename();
      String sFilename = sModelFilename.substring(sModelFilename.lastIndexOf(File.separator));
      sFilename = sFilename + ".xml";
      final String sPath = SextanteGUI.getSettingParameterValue(SextanteModelerSettings.MODELS_FOLDER);
      final String sFullPath = sPath + File.separator + sFilename;
      return HelpIO.getHelpAsHTMLCode(alg, sFullPath);

   }


   public String getAlgorithmHelpFilename(final GeoAlgorithm alg,
                                          final boolean bForceCurrentLocale) {

      final String sModelFilename = ((ModelAlgorithm) alg).getFilename();
      String sFilename = sModelFilename.substring(sModelFilename.lastIndexOf(File.separator));
      sFilename = sFilename + ".xml";
      final String sPath = SextanteGUI.getSettingParameterValue(SextanteModelerSettings.MODELS_FOLDER);
      final String sFullPath = sPath + File.separator + sFilename;

      return sFullPath;

   }


   public boolean canEditHelp() {

      return true;

   }


   public HashMap<NameAndIcon, ArrayList<ToolboxAction>> getToolboxActions() {

      final HashMap<NameAndIcon, ArrayList<ToolboxAction>> map = new HashMap<NameAndIcon, ArrayList<ToolboxAction>>();
      final NameAndIcon nai = new NameAndIcon(getName(), MODEL_ICON);
      final ArrayList<ToolboxAction> list = new ArrayList<ToolboxAction>();
      list.add(new CreateModelToolboxAction());
      map.put(nai, list);

      return map;
   }


   public IToolboxRightButtonAction[] getToolboxRightButtonActions() {

      return new IToolboxRightButtonAction[] { new EditModelAction(), new DeleteModelAction() };

   }


}
