package es.unex.sextante.gui.modeler;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ModelFileFilter
         extends
            FileFilter {

   @Override
   public boolean accept(final File f) {

      if (f.isDirectory()) {
         return true;
      }

      final String extension = getExtension(f);
      if (extension != null) {
         if (extension.equals("model") || extension.equals("java")) {
            return true;
         }
         else {
            return false;
         }
      }

      return false;
   }


   @Override
   public String getDescription() {

      return "Models (*.model)";

   }


   private String getExtension(final File f) {

      String ext = null;
      final String s = f.getName();
      final int i = s.lastIndexOf('.');

      if ((i > 0) && (i < s.length() - 1)) {
         ext = s.substring(i + 1).toLowerCase();
      }

      return ext;

   }


}
