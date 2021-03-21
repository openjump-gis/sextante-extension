package es.unex.sextante.gui.cmd;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.core.ToolboxAction;

public class OpenCommandLineInterfaceAction
         extends
            ToolboxAction {

   @Override
   public void execute() {

      SextanteGUI.getGUIFactory().showCommandLineDialog();

   }


   @Override
   public String getGroup() {

      return Sextante.getText("Tools");

   }


   @Override
   public String getName() {

      return Sextante.getText("OpenCommandLineInterface");

   }


   @Override
   public boolean isActive() {

      return true;

   }

}
