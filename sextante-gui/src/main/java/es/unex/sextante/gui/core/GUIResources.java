package es.unex.sextante.gui.core;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import es.unex.sextante.core.Sextante;

/**
 * 
 * Methods in this class act as entry points for algorithms and resource files.
 * 
 * Algorithms are loaded when the library is initialized. Adding new algorithms after calling {@link Sextante#initialize()} will
 * not add them to the list of currently available ones, so all additional algorithms should be added before initializing the
 * library
 * 
 */
public class GUIResources {

   private static ArrayList<String> m_ParameterPanelNames        = new ArrayList<String>();
   private static ArrayList<String> m_ModelerParameterPanelNames = new ArrayList<String>();


   /**
    * Adds all sextante GUI resources located in the jar files in the classpath.
    */
   public static void addResourcesFromClasspath() {

      final StringTokenizer localTokenizer = new StringTokenizer(System.getProperty("java.class.path"), File.pathSeparator, false);

      //final Set<String> filenames = new LinkedHashSet();

      while (localTokenizer.hasMoreTokens()) {
         final String classpathElement = localTokenizer.nextToken();
         final File classpathFile = new File(classpathElement);
         final String sFilename = classpathFile.getName();
         if (classpathFile.exists() && classpathFile.canRead()) {
            if (sFilename.toLowerCase().endsWith(".jar") && sFilename.toLowerCase().startsWith("sextante")) {
               addResourcesFromJarFile(classpathElement);
            }

         }
      }

      final ClassLoader classloader = GUIResources.class.getClassLoader();
      URL[] webappClasses = new URL[0];
      if (classloader instanceof URLClassLoader) {
         final URLClassLoader urlClassLoader = (URLClassLoader) classloader;
         webappClasses = urlClassLoader.getURLs();
      }

      addResourcesFromURLs(webappClasses);

   }


   public static void addResourcesFromURLs(final URL[] urls) {

      for (final URL url : urls) {
         final File classpathFile = new File(URLDecoder.decode(url.getFile()));
         final String sFilename = classpathFile.getName();
         if (classpathFile.exists() && classpathFile.canRead()) {
            if (sFilename.toLowerCase().endsWith(".jar") && sFilename.toLowerCase().startsWith("sextante")) {
               addResourcesFromJarFile(classpathFile.getAbsolutePath());
            }
         }
      }


   }


   /**
    * Adds all sextante GUI resources from files located in the jar files of a given folder.
    */
   public static void addResourcesFromFolder(final String sFolder) {

      final File folder = new File(sFolder);
      final File[] directoryFiles = folder.listFiles();
      for (int i = 0; i < directoryFiles.length; i++) {
         if (!directoryFiles[i].isDirectory()) {
            final String sFilename = directoryFiles[i].getName();
            if (sFilename.endsWith(".jar") && sFilename.startsWith("sextante")) {
               addResourcesFromJarFile(directoryFiles[i].getAbsolutePath());
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
   private static void addResourcesFromJarFile(final String sFilename) {

      try {
         final ZipFile zip = new ZipFile(sFilename);
         final Enumeration entries = zip.entries();
         while (entries.hasMoreElements()) {
            final ZipEntry entry = (ZipEntry) entries.nextElement();
            final String sName = entry.getName();
            if (!entry.isDirectory()) {
               if (sName.toLowerCase().endsWith("parameterspanel.class")) {
                  final String sClassName = sName.substring(0, sName.lastIndexOf('.')).replace('/', '.');
                  m_ParameterPanelNames.add(sClassName);
               }
               if (sName.toLowerCase().endsWith("modelerparameterspanel.class")) {
                  final String sClassName = sName.substring(0, sName.lastIndexOf('.')).replace('/', '.');
                  m_ModelerParameterPanelNames.add(sClassName);
               }
            }
         }
      }
      catch (final Exception e) {/*ignore file*/}

   }


   /**
    * Returns an array of class names corresponding to custom parameter panels found in jar files
    * 
    * @return an array of class names corresponding to custom parameter panels found in jar files
    */
   public static String[] getParameterPanelClassNames() {

      return m_ParameterPanelNames.toArray(new String[0]);

   }


   /**
    * Returns an array of class names corresponding to custom modeler parameter panels found in jar files
    * 
    * @return an array of class names corresponding to custom modeler parameter panels found in jar files
    */
   public static String[] getModelerParameterPanelClassNames() {

      return m_ModelerParameterPanelNames.toArray(new String[0]);

   }

}
