

package es.unex.sextante.core;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * 
 * Methods in this class act as entry points for algorithms and resource files.
 * 
 * Algorithms are loaded when the library is initialized. Adding new algorithms after calling {@link Sextante#initialize()} will
 * not add them to the list of currently available ones, so all additional algorithms should be added before initializing the
 * library
 * 
 */
public class AlgorithmsAndResources {

   private static ArrayList<String> m_ClassNames      = new ArrayList<String>();
   private static ArrayList<String> m_PropertiesFiles = new ArrayList<String>();


   /**
    * Returns an array of class names of all the algorithms currently included in the library
    * 
    * @return an array of algorithm class names
    */
   public static String[] getAlgorithmClassNames() {

      return m_ClassNames.toArray(new String[m_ClassNames.size()]);

   }


   /**
    * Returns an array of names of properties files containing resource strings for i18N
    * 
    * @return an array of properties filenames
    */
   public static String[] getPropertiesFilenames() {

      return m_PropertiesFiles.toArray(new String[m_PropertiesFiles.size()]);

   }


   /**
    * Adds a new algorithm to the list or currently available ones
    * 
    * @param sAlgClassName
    *                the name of the class representing the algorithm
    */
   public static void addAlgorithm(final String sAlgClassName) {

      m_ClassNames.add(sAlgClassName);

   }


   /**
    * Adds a new properties file to the list
    * 
    * @param sName
    *                the base name of the properties file
    */
   public static void addPropertiesFile(final String sName) {

      m_PropertiesFiles.add(sName);

   }


   /**
    * Adds all sextante algorithms and properties files located in the jar files in the classpath.
    */
   public static void addAlgorithmsAndPropertiesFromClasspath() {

      final StringTokenizer localTokenizer = new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator, false);

      //final Set<String> filenames = new LinkedHashSet();

      while (localTokenizer.hasMoreTokens()) {
         final String classpathElement = localTokenizer.nextToken();
         final File classpathFile = new File(classpathElement);
         final String sFilename = classpathFile.getName();
         if (classpathFile.exists() && classpathFile.canRead()) {
            if (sFilename.toLowerCase().endsWith(".jar") && sFilename.toLowerCase().startsWith("sextante")) {
               addAlgorithmsAndResourcesFromJarFile(classpathElement);
            }

         }
      }

      final ClassLoader classloader = AlgorithmsAndResources.class.getClassLoader();
      URL[] webappClasses = new URL[0];
      if (classloader instanceof URLClassLoader) {
         final URLClassLoader urlClassLoader = (URLClassLoader) classloader;
         webappClasses = urlClassLoader.getURLs();
      }

      addAlgorithmsAndPropertiesFromURLs(webappClasses);

   }


   public static void addAlgorithmsAndPropertiesFromURLs(final URL[] urls) {

      for (final URL url : urls) {
         final File classpathFile = new File(URLDecoder.decode(url.getFile()));
         final String sFilename = classpathFile.getName();
         if (classpathFile.exists() && classpathFile.canRead()) {
            if (sFilename.toLowerCase().endsWith(".jar") && sFilename.toLowerCase().startsWith("sextante")) {
               addAlgorithmsAndResourcesFromJarFile(classpathFile.getAbsolutePath());
            }
         }
      }


   }


   /**
    * Adds all sextante algorithms and properties files located in the jar files of a given folder.
    * 
    * <b>NOTE:</b> i18n files must be named like 'nameOfPackage.properties' (without any '_' character on the name, except that
    * files of other languages, e.g, nameOfPackage_es.properties
    * 
    * @param sFolder
    *                the folder where jar files are located
    */
   public static void addAlgorithmsAndPropertiesFromFolder(final String sFolder) {

      final File folder = new File(sFolder);
      final File[] directoryFiles = folder.listFiles();
      for (int i = 0; i < directoryFiles.length; i++) {
         if (!directoryFiles[i].isDirectory()) {
            final String sFilename = directoryFiles[i].getName();
            if (sFilename.endsWith(".jar") && sFilename.startsWith("sextante")) {
               addAlgorithmsAndResourcesFromJarFile(directoryFiles[i].getAbsolutePath());
            }
         }
      }
   }


   /**
    * Adds all sextante algorithms and properties files located in a given jar file
    * 
    * @param sFilename
    *                the filename of the jar file
    */
   private static void addAlgorithmsAndResourcesFromJarFile(final String sFilename) {

      try {
         final ZipFile zip = new ZipFile(sFilename);
         final Enumeration entries = zip.entries();
         while (entries.hasMoreElements()) {
            final ZipEntry entry = (ZipEntry) entries.nextElement();
            final String sName = entry.getName();
            if (!entry.isDirectory()) {
               if (sName.toLowerCase().endsWith("algorithm.class")) {
                  final String sClassName = sName.substring(0, sName.lastIndexOf('.')).replace('/', '.');
                  addAlgorithm(sClassName);
               }
               else if (sName.endsWith(".properties") && (sName.indexOf("_") == -1)) {
                  addPropertiesFile(sName.substring(0, sName.lastIndexOf('.')));
               }
            }
         }
      }
      catch (final Exception e) {/*ignore file*/
      }

   }

}
