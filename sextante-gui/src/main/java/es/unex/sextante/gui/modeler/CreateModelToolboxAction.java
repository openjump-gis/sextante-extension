package es.unex.sextante.gui.modeler;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.core.ToolboxAction;

public class CreateModelToolboxAction
         extends
            ToolboxAction {

   @Override
   public void execute() {

      SextanteGUI.getGUIFactory().showModelerDialog();

   }


   @Override
   public String getGroup() {

      return Sextante.getText("Tools");
   }


   @Override
   public String getName() {

      return Sextante.getText("CreateNewModel");

   }


   @Override
   public boolean isActive() {

      return true;

   }

}
