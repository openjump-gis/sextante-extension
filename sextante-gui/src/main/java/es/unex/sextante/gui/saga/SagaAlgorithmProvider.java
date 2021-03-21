package es.unex.sextante.gui.saga;

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
import es.unex.sextante.gui.settings.Setting;
import es.unex.sextante.gui.settings.SextanteSagaSettings;

public class SagaAlgorithmProvider
         implements
            IAlgorithmProvider {

   private final static ImageIcon              SAGA_ICON = new ImageIcon(
                                                                  GrassAlgorithmProvider.class.getClassLoader().getResource(
                                                                           "images/saga.png"));

   private final HashMap<String, GeoAlgorithm> m_Algs    = new HashMap<String, GeoAlgorithm>();


   public boolean canEditHelp() {

      return false;

   }


   public String getAlgorithmHelp(final GeoAlgorithm alg) {

      return "";

   }


   public String getAlgorithmHelpFilename(final GeoAlgorithm alg,
                                          final boolean forceCurrentLocale) {

      return null;

   }


   public HashMap<String, GeoAlgorithm> getAlgorithms() {

      if (Boolean.parseBoolean(SextanteGUI.getSettingParameterValue(SextanteSagaSettings.SAGA_ACTIVATE))) {
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

      return SAGA_ICON;

   }


   public String getName() {

      return "SAGA";

   }


   public Setting getSettings() {

      return new SextanteSagaSettings();

   }


   public void initialize() {

      loadSagaAlgorithmsFromDescriptions();

   }


   private void loadSagaAlgorithmsFromDescriptions() {

      m_Algs.clear();

      try {
         final File file = new File(SagaUtils.getSagaDescriptionFolder());
         final String[] files = file.list();
         if (files != null) {
            for (final String element : files) {
               if (element.startsWith("alg")) {
                  final SagaAlgorithm alg = createAlgorithm(SagaUtils.getSagaDescriptionFolder() + File.separator + element);
                  if ((alg != null)) {
                     m_Algs.put(alg.getCommandLineName(), alg);
                  }
               }
            }
         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }
   }


   private SagaAlgorithm createAlgorithm(final String sFile) {

      final SagaAlgorithm alg = new SagaAlgorithm();
      try {
         alg.initialize(sFile);
         if (alg.getName() != null) {//a quick way to check that everything went ok
            return alg;
         }
         else {
            return null;
         }
      }
      catch (final UnwrappableSagaAlgorithmException e) {
         return null;
      }

   }


   public void update() {

      initialize();

   }


   public HashMap<NameAndIcon, ArrayList<ToolboxAction>> getToolboxActions() {

      return new HashMap<NameAndIcon, ArrayList<ToolboxAction>>();

   }


   public IToolboxRightButtonAction[] getToolboxRightButtonActions() {

      return new IToolboxRightButtonAction[0];

   }


   public static void addMessage(final String sMessage,
                                 final String sDescription) {

      Sextante.getLogger().addToLog(sMessage, "SAGA", sDescription);

   }


}
