package es.unex.sextante.gui.algorithm;

import java.io.File;

import javax.swing.filechooser.FileFilter;


/**
 * Filtro de fichero generico para los JFileChooser
 * 
 * @author Fernando Gonzalez Cortes
 */
public class GenericFileFilter
         extends
            FileFilter {
   private String[]     extensiones = new String[1];
   private final String description;
   private boolean      dirs        = true;


   /**
    * Crea un nuevo GenericFileFilter.
    * 
    * @param ext
    *                DOCUMENT ME!
    * @param desc
    *                DOCUMENT ME!
    */
   public GenericFileFilter(final String[] ext,
                            final String desc) {
      extensiones = ext;
      description = desc;
   }


   /**
    * Crea un nuevo GenericFileFilter.
    * 
    * @param ext
    *                DOCUMENT ME!
    * @param desc
    *                DOCUMENT ME!
    */
   public GenericFileFilter(final String ext,
                            final String desc) {
      extensiones[0] = ext;
      description = desc;
   }


   /**
    * Crea un nuevo GenericFileFilter.
    * 
    * @param ext
    *                DOCUMENT ME!
    * @param desc
    *                DOCUMENT ME!
    * @param dirs
    *                DOCUMENT ME!
    */
   public GenericFileFilter(final String ext,
                            final String desc,
                            final boolean dirs) {
      extensiones[0] = ext;
      description = desc;
      this.dirs = dirs;
   }


   /**
    * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
    */
   @Override
   public boolean accept(final File f) {
      if (f.isDirectory()) {
         if (dirs) {
            return true;
         }
         else {
            return false;
         }
      }
      if (extensiones[0] == null) {
         return true;
      }
      boolean endsWith = false;
      for (final String element : extensiones) {
         if (f.getName().toUpperCase().endsWith(element.toUpperCase())) {
            endsWith = true;
         }
      }

      return endsWith;

   }


   /**
    * @see javax.swing.filechooser.FileFilter#getDescription()
    */
   @Override
   public String getDescription() {
      return description;
   }
}
