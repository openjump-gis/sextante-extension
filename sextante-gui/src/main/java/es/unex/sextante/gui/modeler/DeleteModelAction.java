package es.unex.sextante.gui.modeler;

import java.io.File;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.IToolboxRightButtonAction;
import es.unex.sextante.gui.core.SextanteGUI;

public class DeleteModelAction
         implements
            IToolboxRightButtonAction {

   public boolean canBeExecutedOnAlgorithm(final GeoAlgorithm alg) {

      return alg instanceof ModelAlgorithm;

   }


   public void execute(final GeoAlgorithm alg) {

      final ModelAlgorithm model = (ModelAlgorithm) alg;
      final File file = new File(model.getFilename());
      file.delete();
      SextanteGUI.updateAlgorithmProvider(ModelerAlgorithmProvider.class);
      SextanteGUI.getGUIFactory().updateToolbox();
      //fillTree(m_sLastSearchString, m_bLastSearchIncludedHelpFiles);
      //collapseAll();


   }


   public String getDescription() {

      return Sextante.getText("Delete");

   }

}
