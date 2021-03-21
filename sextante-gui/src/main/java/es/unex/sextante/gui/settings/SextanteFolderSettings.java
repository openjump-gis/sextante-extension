package es.unex.sextante.gui.settings;

import java.util.HashMap;

import es.unex.sextante.core.Sextante;

public class SextanteFolderSettings
         extends
            Setting {

   public static final String RESULTS_FOLDER = "ResultsFolder";


   @Override
   public String getName() {

      return Sextante.getText("Folders");
   }


   @Override
   public void createPanel() {

      panel = new SextanteFolderSettingsPanel();

   }


   @Override
   public HashMap<String, String> getInitValues() {

      final HashMap<String, String> map = new HashMap<String, String>();
      map.put(RESULTS_FOLDER, System.getProperty("user.dir"));

      return map;

   }

}
