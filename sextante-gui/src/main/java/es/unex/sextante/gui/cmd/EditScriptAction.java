package es.unex.sextante.gui.cmd;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.IToolboxRightButtonAction;

public class EditScriptAction
         implements
            IToolboxRightButtonAction {

   public void execute(final GeoAlgorithm alg) {
   // TODO Auto-generated method stub

   }


   public String getDescription() {

      return Sextante.getText("Edit");

   }


   public boolean canBeExecutedOnAlgorithm(final GeoAlgorithm alg) {

      return alg instanceof ScriptAlgorithm;

   }

}
