

package es.unex.sextante.gui.cmd;

import java.io.File;
import java.util.ArrayList;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.exceptions.WrongScriptException;


public class ScriptsIO {

   private static String[] m_sFiles = new String[0];


   public static GeoAlgorithm[] loadScriptsAsAlgorithms(final String sFolder) {

      final ArrayList<GeoAlgorithm> list = new ArrayList<GeoAlgorithm>();
      final ArrayList<String> fileList = new ArrayList<String>();
      final File folder = new File(sFolder);
      final String[] files = folder.list();
      if (files != null) {
         for (int i = 0; i < files.length; i++) {
            if (files[i].endsWith("bsh")) {
               final ScriptAlgorithm alg = new ScriptAlgorithm();
               try {
                  alg.initialize(files[i]);
                  list.add(alg);
                  fileList.add(sFolder + File.separator + files[i]);
               }
               catch (final WrongScriptException e) {
                  Sextante.addErrorToLog(e);
               }
            }
         }
         m_sFiles = fileList.toArray(new String[0]);
         return list.toArray(new GeoAlgorithm[0]);
      }
      else {
         return new GeoAlgorithm[0];
      }

   }


   public static String[] getScriptFiles() {

      return m_sFiles;

   }
}
