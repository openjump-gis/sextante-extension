package es.unex.sextante.gui.r;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.IToolboxRightButtonAction;
import es.unex.sextante.gui.core.SextanteGUI;

public class EditRScriptAction
         implements
            IToolboxRightButtonAction {

   public void execute(final GeoAlgorithm alg) {

      final String sFilename = ((RAlgorithm) alg).getFilename();
      final RScriptEditingPanel panel = new RScriptEditingPanel(sFilename);
      SextanteGUI.getGUIFactory().showGenericDialog("R", panel);

   }


   public boolean canBeExecutedOnAlgorithm(final GeoAlgorithm alg) {

      return alg instanceof RAlgorithm;

   }


   public String getDescription() {

      return Sextante.getText("Edit");

   }

}
