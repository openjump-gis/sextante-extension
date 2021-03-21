package es.unex.sextante.gui.modeler;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.IToolboxRightButtonAction;
import es.unex.sextante.gui.core.SextanteGUI;

public class EditModelAction
         implements
            IToolboxRightButtonAction {

   public void execute(final GeoAlgorithm alg) {

      SextanteGUI.getGUIFactory().showModelerDialog((ModelAlgorithm) alg);

   }


   public boolean canBeExecutedOnAlgorithm(final GeoAlgorithm alg) {

      return alg instanceof ModelAlgorithm;
   }


   public String getDescription() {

      return Sextante.getText("Edit");
   }

}
