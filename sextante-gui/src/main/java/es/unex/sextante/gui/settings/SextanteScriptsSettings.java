package es.unex.sextante.gui.settings;

import java.util.HashMap;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;

public class SextanteScriptsSettings
         extends
            Setting {

   public static final String SCRIPTS_FOLDER = "ScriptsFolder";


   @Override
   public void createPanel() {

      panel = new SextanteScriptsSettingsPanel();

   }


   @Override
   public String getName() {

      return Sextante.getText("Scripts");

   }


   @Override
   public HashMap<String, String> getInitValues() {

      final HashMap<String, String> map = new HashMap<String, String>();
      map.put(SCRIPTS_FOLDER, SextanteGUI.getHelpPath());

      return map;

   }

}
