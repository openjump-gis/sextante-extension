package es.unex.sextante.gui.cmd;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.core.ToolboxAction;

public class CreateScriptToolboxAction
         extends
            ToolboxAction {

   @Override
   public void execute() {

      final ScriptEditingPanel panel = new ScriptEditingPanel();
      SextanteGUI.getGUIFactory().showGenericDialog("Script", panel);

   }


   @Override
   public String getGroup() {

      return Sextante.getText("Tools");

   }


   @Override
   public String getName() {

      return Sextante.getText("CreateNewScript");

   }


   @Override
   public boolean isActive() {

      return true;

   }

}
