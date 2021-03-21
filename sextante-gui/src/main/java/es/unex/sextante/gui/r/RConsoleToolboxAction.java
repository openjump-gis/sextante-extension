package es.unex.sextante.gui.r;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.core.ToolboxAction;

public class RConsoleToolboxAction
         extends
            ToolboxAction {

   @Override
   public void execute() {

      final RScriptEditingPanel panel = new RScriptEditingPanel();
      SextanteGUI.getGUIFactory().showGenericDialog("R", panel);

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
