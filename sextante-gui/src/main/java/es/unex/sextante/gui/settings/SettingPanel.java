package es.unex.sextante.gui.settings;

import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.JPanel;

import es.unex.sextante.gui.exceptions.WrongSettingValuesException;

public abstract class SettingPanel
         extends
            JPanel {


   public SettingPanel() {

      super();
      setPreferredSize(new Dimension(400, 300));

      initGUI();
   }


   protected abstract void initGUI();


   public abstract HashMap<String, String> getValues() throws WrongSettingValuesException;

}
