package es.unex.sextante.gui.settings;

import java.util.HashMap;

import es.unex.sextante.core.Sextante;

public class SextanteGeneralSettings
         extends
            Setting {

   public static final String MODIFY_NAMES          = "ModifyNames";
   public static final String USE_INTERNAL_NAMES    = "UseInternalNames";
   public static final String DEFAULT_NO_DATA_VALUE = "NoDataValue";
   public static final String SHOW_MOST_RECENT      = "ShowMostRecent";


   @Override
   public String getName() {
      return Sextante.getText("General");
   }


   @Override
   public void createPanel() {

      panel = new SextanteGeneralSettingsPanel();

   }


   @Override
   public HashMap<String, String> getInitValues() {

      final HashMap<String, String> map = new HashMap<String, String>();
      map.put(MODIFY_NAMES, Boolean.FALSE.toString());
      map.put(USE_INTERNAL_NAMES, Boolean.FALSE.toString());
      map.put(DEFAULT_NO_DATA_VALUE, "-99999");

      return map;

   }


}
