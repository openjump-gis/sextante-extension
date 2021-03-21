/*
 * 20:25:20 20/05/99
 *
 * The Java Shell: Utilities.
 * (C)1999 Romain Guy, Osvaldo Pinali Doederlein.
 *
 * LICENSE
 * =======
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CHANGES
 * =======
 * 1.0.8 - Filled the listRoots method                   (Romain & Osvaldo)
 * 1.0.7 - Several bug fixes in constructPath            (Romain)
 * 1.0.6 - Split JDK1.1/1.2                              (Osvaldo)
 * 1.0.5 - Important bug fix in constructPath(String)    (Romain)
 * 1.0.4 - Added getSize(Enumeration)                    (Osvaldo)
 * 1.0.3 - Changed sortStrings bubble-sort algorithm to  (Romain)
 *         quick-sort (James Gosling)
 * 1.0.2 - Fixed two little bug in constructPath(String) (Romain)
 * 1.0.1 - Added listFiles(String[], boolean)            (Romain)
 *       - Removed unecessary createWhiteSpace(int)      (Romain)
 *       - Modified getWildCardMatches(String, boolean)  (Romain)
 *       - Slighty improved javadoc comments             (Romain)
 * 1.0.0 - Initial release.                              (Romain & Osvaldo)
 *
 * LINKS
 * =====
 * Contact: mailto@osvaldo.visionnaire.com.br
 * Site #1: http://www.geocities.com/ResearchTriangle/Node/2005/
 * Site #2: http://student.vub.ac.be/~opinalid/
 */

package jsh.shell;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import es.unex.sextante.gui.core.SextanteGUI;

/**
 * The Java Shell: Utility pack.
 *
 * @author Romain Guy.
 * @author Osvaldo Pinali Doederlein.
 */

public class Utils {

   /**
    * Removes some elements from a String array.
    *
    * @param arr
    *                The array.
    * @param first
    *                First element to keep (new 0-index).
    * @param last
    *                Last element to keep.
    * @return Copy of arr[first..]
    */
   public static String[] subarray(final String[] arr,
                                   int first,
                                   final int last) {
      final String[] newArr = new String[last - first + 1];

      for (int i = 0; i < newArr.length; ++i, ++first) {
         newArr[i] = arr[first];
      }

      return newArr;
   }


   /**
    * Removes some leading elements from a String array.
    *
    * @param arr
    *                The array.
    * @param first
    *                First element to keep (new 0-index).
    * @return Copy of arr[first..]
    */
   public static String[] subarray(final String[] arr,
                                   final int first) {
      return subarray(arr, first, arr.length - 1);
   }


   /**
    * Formats a number of bytes for output.
    *
    * @param bytes
    *                Number of bytes.
    * @return "xxxxK" form.
    */
   public static String fmtBytes(final long bytes) {
      return Long.toString(bytes / 1024) + "K";
   }


   /**
    * Formats time for output.
    *
    * @param bytes
    *                Number of milliseconds.
    * @return "x,yyys" form.
    */
   public static String fmtTime(final long ms) {
      return Float.toString(ms / 1000.0f) + "s";
   }


   /**
    * Return a String made of spaces.
    *
    * @param len
    *                Number of spaces
    */
   public static String getSpaces(final int len) {
      final StringBuffer buf = new StringBuffer();

      for (int i = 0; i < len; i++) {
         buf.append(' ');
      }

      return buf.toString();
   }


   public static String[] toArray(final Vector strings) {
      final String[] arr = new String[strings.size()];

      for (int i = 0; i < strings.size(); ++i) {
         arr[i] = (String) strings.elementAt(i);
      }

      return arr;
   }


   /**
    * When the user has to specify file names, he can use wildcards (*, ?). This methods handles the usage of these wildcards.
    *
    * @param s
    *                Wilcards
    * @param sort
    *                Set to true will sort file names
    * @return An array of String which contains all files matching <code>s</code> in current directory.
    * @see Utils#match(String, String)
    */

   public static String[] getWildCardMatches(final String s,
                                             final boolean sort) {
      final String args = new String(s.trim());
      String files[];
      final Vector filesThatMatchVector = new Vector();
      String filesThatMatch[];

      files = (new File(getUserDirectory())).list();

      for (int i = 0; i < files.length; i++) {
         if (match(args, files[i])) {
            final File temp = new File(getUserDirectory(), files[i]);
            filesThatMatchVector.addElement(new String(temp.getName()));
         }
      }

      filesThatMatch = new String[filesThatMatchVector.size()];
      filesThatMatchVector.copyInto(filesThatMatch);

      if (sort) {
         sortStrings(filesThatMatch);
      }

      return filesThatMatch;
   }


   /**
    * This method can determine if a String matches a pattern of wildcards
    *
    * @param pattern
    *                The pattern used for comparison
    * @param string
    *                The String to be checked
    * @return true if <code>string</code> matches <code>pattern</code>
    * @see Utils#getWildCardMatches(String)
    */

   public static boolean match(final String pattern,
                               final String string) {
      for (int p = 0;; p++) {
         for (int s = 0;; p++, s++) {
            final boolean sEnd = (s >= string.length());
            final boolean pEnd = (p >= pattern.length() || pattern.charAt(p) == '|');

            if (sEnd && pEnd) {
               return true;
            }
            if (sEnd || pEnd) {
               break;
            }
            if (pattern.charAt(p) == '?') {
               continue;
            }

            if (pattern.charAt(p) == '*') {
               int i;
               p++;

               for (i = string.length(); i >= s; --i) {
                  if (match(pattern.substring(p), string.substring(i))) {
                     return true;
                  }
               }

               break;
            }

            if (pattern.charAt(p) != string.charAt(s)) {
               break;
            }
         }

         p = pattern.indexOf('|', p);

         if (p == -1) {
            return false;
         }
      }
   }


   /**
    * Quick sort an array of Strings.
    *
    * @param string
    *                Strings to be sorted
    */

   public static void sortStrings(final String[] strings) {
      sortStrings(strings, 0, strings.length - 1);
   }


   /**
    * Quick sort an array of Strings.
    *
    * @param a
    *                Strings to be sorted
    * @param lo0
    *                Lower bound
    * @param hi0
    *                Higher bound
    */

   public static void sortStrings(final String a[],
                                  final int lo0,
                                  final int hi0) {
      int lo = lo0;
      int hi = hi0;
      String mid;

      if (hi0 > lo0) {
         mid = a[(lo0 + hi0) / 2];

         while (lo <= hi) {
            while (lo < hi0 && a[lo].compareTo(mid) < 0) {
               ++lo;
            }

            while (hi > lo0 && a[hi].compareTo(mid) > 0) {
               --hi;
            }

            if (lo <= hi) {
               swap(a, lo, hi);
               ++lo;
               --hi;
            }
         }

         if (lo0 < hi) {
            sortStrings(a, lo0, hi);
         }

         if (lo < hi0) {
            sortStrings(a, lo, hi0);
         }
      }
   }


   /**
    * Swaps two Strings.
    *
    * @param a
    *                The array to be swapped
    * @param i
    *                First String index
    * @param j
    *                Second String index
    */

   public static void swap(final String a[],
                           final int i,
                           final int j) {
      String T;
      T = a[i];
      a[i] = a[j];
      a[j] = T;
   }


   /**
    * Returns the user current directory.
    */
   public static String getUserDirectory() {

      String folder = SextanteGUI.getOutputFolder();
      if (folder == null) {
         folder = System.getProperty("user.home");
      }

      return folder;

   }


   /**
    * Sometimes, Strings are too long to be correctly displayed. This method will reduce a String, keeping first and last
    * characters.
    *
    * @param longString
    *                The String to be modified
    * @param maxLength
    *                The maximum length of the String
    * @return A shorter String
    */

   public static String getShortStringOf(final String longString,
                                         final int maxLength) {
      final int len = longString.length();

      if (len < maxLength) {
         return longString;
      }
      else {
         return longString.substring(0, maxLength / 2) + "..." + longString.substring(len - (maxLength / 2));
      }
   }


   /**
    * Because a lot of people still use JDK 1.1, we need this method to create an array of Files from an array of String.
    *
    * @param names
    *                Names of the files
    * @param construct
    *                Set it to true if names does not contain full paths
    * @return An array of Files
    */
   public static File[] listFiles(final String[] names,
                                  final boolean construct) {
      final File[] files = new File[names.length];

      String path = Utils.getUserDirectory();

      if (construct) {
         if (!path.endsWith(File.separator)) {
            path += File.separator;
         }
      }

      for (int i = 0; i < files.length; i++) {
         if (construct) {
            files[i] = new File(path + names[i]);
         }
         else {
            files[i] = new File(names[i]);
         }
      }

      return files;
   }


   /**
    * Counts things in an Enumeration (and destroys it as a side effect).
    *
    * @param enum
    *                The enumeration, in the start position.
    * @return Elements found.
    */
   public static int getSize(final Enumeration enu) {
      int size = 0;

      while (enu.hasMoreElements()) {
         ++size;
         enu.nextElement();
      }

      return size;
   }


   /**
    * Constructs a new path from current user path. This is an easy way to get a path if the user specified, for example,
    * "..\Java" as new path. This method will return the argument if this one is a path to a root (i.e, if <code>change</code>
    * is equal to C:\Jdk, constructPath will return C:\Jdk).
    *
    * @param change
    *                The modification to apply to the path
    * @see Utils#beginsWithRoot(String), Utils#getRoot(String)
    */

   public static String constructPath(final String change) {
      if (change == null || beginsWithRoot(change)) {
         return change;
      }

      String newPath = getUserDirectory();

      char current;
      char lastChar = '\0';
      boolean toAdd = false;
      StringBuffer buf = new StringBuffer(change.length());

      for (int i = 0; i < change.length(); i++) {
         switch ((current = change.charAt(i))) {
            case '.':
               if (lastChar == '.') {
                  final String parent = (new File(newPath)).getParent();
                  if (parent != null) {
                     newPath = parent;
                  }
               }
               else if (lastChar != '\0' && lastChar != '\\' && lastChar != '/') {
                  buf.append('.');
               }
               lastChar = '.';
               break;
            case '\\':
            case '/':
               if (lastChar == '\0') {
                  newPath = getRoot(newPath);
               }
               else {
                  if (!newPath.endsWith("\\")) {
                     newPath += File.separator + buf.toString();
                  }
                  else {
                     newPath += buf.toString();
                  }
                  buf = new StringBuffer();
                  toAdd = false;
               }
               lastChar = '\\';
               break;
            case '~':
               if (i < change.length() - 1) {
                  if (change.charAt(i + 1) == '\\' || change.charAt(i + 1) == '/') {
                     newPath = System.getProperties().getProperty("user.home");
                  }
                  else {
                     buf.append('~');
                  }
               }
               else if (i == 0) {
                  newPath = System.getProperties().getProperty("user.home");
               }
               else {
                  buf.append('~');
               }
               lastChar = '~';
               break;
            default:
               lastChar = current;
               buf.append(current);
               toAdd = true;
               break;
         }
      }

      if (toAdd) {
         if (!newPath.endsWith(File.separator)) {
            newPath += File.separator + buf.toString();
         }
         else {
            newPath += buf.toString();
         }
      }

      return newPath;
   }


   /**
    * It can be necessary to determine which is the root of a path. For example, the root of D:\Projects\Java is D:\.
    *
    * @param path
    *                The path used to get a root
    * @return The root which contais the specified path
    */

   public static String getRoot(final String path) {
      final File roots[] = listRoots(new File(path));

      for (int i = 0; i < roots.length; i++) {
         if (path.startsWith(roots[i].getPath())) {
            return roots[i].getPath();
         }
      }

      return path;
   }


   /**
    * It can be necessary to determine if a path begins with a root.
    *
    * @param path
    *                The path to check
    * @return True if path begins with a root, false otherwise
    */

   public static boolean beginsWithRoot(final String path) {
      final File roots[] = listRoots(new File(path));

      for (int i = 0; i < roots.length; i++) {
         if (path.regionMatches(true, 0, roots[i].getPath(), 0, roots[i].getPath().length())) {
            return true;
         }
      }

      return false;
   }


   /**
    * We override a Java2 specific method.
    *
    * @param f
    *                A File
    * @return A list of standards roots
    */

   public static File[] listRoots(final File f) {
      if (System.getProperty("os.name").startsWith("Windows")) {
         return new File[] { new File("A:\\"), new File("B:\\"), new File("C:\\"), new File("D:\\"), new File("E:\\"),
                  new File("F:\\"), new File("G:\\"), new File("H:\\"), new File("I:\\") };
         //return new File[]{new File(System.getProperty("java.home").substring(0, 3))};
      }
      else {
         return new File[] { new File("/") };
      }
   }


   /**
    * We override a Java2 spcific method.
    *
    * @param file
    *                Determine if this file is hidden or not
    * @return Always false
    */

   public boolean isHidden(final File file) {
      return false;
   }

}

// End of Utils.java