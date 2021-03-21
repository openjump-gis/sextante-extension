

package es.unex.sextante.gui.settings;

import java.io.File;
import java.util.HashMap;

import es.unex.sextante.gui.core.SextanteGUI;


public class SextanteSagaSettings
         extends
            Setting {

   public static final String SAGA_FOLDER        = "SagaFolder";
   public static final String SAGA_ACTIVATE      = "SagaActivate";
   public static final String CAN_CONFIGURE_SAGA = "CanConfigureSaga";


   @Override
   public void createPanel() {

      panel = new SextanteSagaSettingsPanel();

   }


   @Override
   public String getName() {

      return "SAGA";

   }


   @Override
   public HashMap<String, String> getInitValues() {

      final HashMap<String, String> map = new HashMap<String, String>();
      map.put(SAGA_FOLDER, SextanteGUI.getSextantePath() + File.separator + "saga");
      map.put(SAGA_ACTIVATE, Boolean.FALSE.toString());
      map.put(CAN_CONFIGURE_SAGA, Boolean.FALSE.toString());
      return map;

   }

}
