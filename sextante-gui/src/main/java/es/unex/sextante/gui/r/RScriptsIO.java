

package es.unex.sextante.gui.r;

import java.io.File;
import java.util.ArrayList;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.exceptions.WrongScriptException;


public class RScriptsIO {

   public static GeoAlgorithm[] loadRScriptsAsAlgorithms(final String sFolder) {

      final ArrayList<GeoAlgorithm> list = new ArrayList<GeoAlgorithm>();
      final File folder = new File(sFolder);
      final String[] files = folder.list();
      if (files != null) {
         for (int i = 0; i < files.length; i++) {
            if (files[i].endsWith("rsx")) {
               final RAlgorithm alg = new RAlgorithm();
               try {
                  alg.initialize(files[i]);
                  list.add(alg);
               }
               catch (final WrongScriptException e) {
                  Sextante.addErrorToLog(e);
               }
            }
         }
         return list.toArray(new GeoAlgorithm[0]);
      }
      else {
         return new GeoAlgorithm[0];
      }

   }

}
