package es.unex.sextante.gui.settings;

import java.util.HashMap;

public abstract class Setting {

   protected SettingPanel panel;


   public SettingPanel getPanel() {

      if (panel == null) {
         createPanel();
      }

      return panel;

   }


   public abstract void createPanel();


   public abstract String getName();


   @Override
   public String toString() {

      return getName();

   }


   public abstract HashMap<String, String> getInitValues();

}
