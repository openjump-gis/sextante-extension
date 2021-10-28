package es.unex.sextante.openjump.gui;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.gui.core.IPostProcessTaskFactory;

public class OpenJUMPPostProcessTaskFactory
         implements
            IPostProcessTaskFactory {

   public Runnable getPostProcessTask(final GeoAlgorithm alg,
                                      final boolean showResultsDialog) {

      return new OpenJUMPPostProcessTask(alg, showResultsDialog);

   }

}
