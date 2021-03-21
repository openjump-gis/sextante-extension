

package es.unex.sextante.gui.settings;

import java.io.File;
import java.util.HashMap;

import es.unex.sextante.gui.core.SextanteGUI;


public class SextanteGrassSettings
         extends
            Setting {

   public static final String GRASS_FOLDER          = "GrassBinariesFolder";
   public static final String GRASS_MAPSET_FOLDER   = "GrassMapsetFolder";
   public static final String GRASS_3D_V_MODE       = "Grass3DVMode";
   public static final String GRASS_LAT_LON_MODE    = "GrassLatLonMode";
   public static final String GRASS_USE_TEMP_MAPSET = "GrassUseTempMapset";
   public static final String GRASS_WIN_SHELL       = "GrassWinShell";
   public static final String GRASS_IN_POLYLINES    = "GrassInPolylines";
   public static final String GRASS_ACTIVATE        = "GrassActivate";
   public static final String CAN_CONFIGURE_GRASS   = "CanConfigureGrass";


   @Override
   public HashMap<String, String> getInitValues() {

      final HashMap<String, String> map = new HashMap<String, String>();
      map.put(GRASS_FOLDER, SextanteGUI.getSextantePath() + File.separator + "grass");
      map.put(GRASS_MAPSET_FOLDER, "");
      map.put(GRASS_WIN_SHELL, SextanteGUI.getSextantePath() + "\\msys\\bin\\sh.exe");
      map.put(GRASS_3D_V_MODE, Boolean.FALSE.toString());
      map.put(GRASS_LAT_LON_MODE, Boolean.FALSE.toString());
      map.put(GRASS_USE_TEMP_MAPSET, Boolean.TRUE.toString());
      map.put(GRASS_IN_POLYLINES, Boolean.FALSE.toString());
      map.put(GRASS_ACTIVATE, Boolean.FALSE.toString());
      map.put(CAN_CONFIGURE_GRASS, Boolean.FALSE.toString());

      return map;

   }


   @Override
   public void createPanel() {

      panel = new SextanteGrassSettingsPanel();

   }


   @Override
   public String getName() {

      return "GRASS";

   }

}
