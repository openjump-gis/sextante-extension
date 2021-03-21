package es.unex.sextante.gui.help;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtilities {

   /**
    * @author David M. Howard
    */

   /**
    * break a path down into individual elements and add to a list. example : if a path is /a/b/c/d.txt, the breakdown will be
    * [d.txt,c,b,a]
    *
    * @param f
    *                input file
    * @return a List collection with the individual elements of the path in reverse order
    */
   private static List getPathList(final File f) {
      List l = new ArrayList();
      File r;
      try {
         r = f.getCanonicalFile();
         while (r != null) {
            l.add(r.getName());
            r = r.getParentFile();
         }
      }
      catch (final IOException e) {
         e.printStackTrace();
         l = null;
      }
      return l;
   }


   /**
    * figure out a string representing the relative path of 'f' with respect to 'r'
    *
    * @param r
    *                home path
    * @param f
    *                path of file
    */
   private static String matchPathLists(final List r,
                                        final List f) {
      int i;
      int j;
      String s;
      // start at the beginning of the lists
      // iterate while both lists are equal
      s = "";
      i = r.size() - 1;
      j = f.size() - 1;

      // first eliminate common root
      while ((i >= 0) && (j >= 0) && (r.get(i).equals(f.get(j)))) {
         i--;
         j--;
      }

      // for each remaining level in the home path, add a ..
      for (; i >= 0; i--) {
         s += ".." + File.separator;
      }

      // for each level in the file path, add the path
      for (; j >= 1; j--) {
         s += f.get(j) + File.separator;
      }

      // file name
      s += f.get(j);
      return s;
   }


   /**
    * get relative path of File 'f' with respect to 'home' directory example : home = /a/b/c f = /a/d/e/x.txt s =
    * getRelativePath(home,f) = ../../d/e/x.txt
    *
    * @param home
    *                base path, should be a directory, not a file, or it doesn't make sense
    * @param f
    *                file to generate path for
    * @return path from home to f as a string
    */
   public static String getRelativePath(final File home,
                                        final File f) {
      final File r;
      List homelist;
      List filelist;
      String s;

      homelist = getPathList(home);
      filelist = getPathList(f);
      s = matchPathLists(homelist, filelist);

      return s;
   }


}
