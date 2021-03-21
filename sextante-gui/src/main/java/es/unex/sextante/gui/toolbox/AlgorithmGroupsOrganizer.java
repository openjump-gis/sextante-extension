package es.unex.sextante.gui.toolbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;

public class AlgorithmGroupsOrganizer {

   static {

      readConfigFile();

   }

   private static HashMap<String, AlgorithmGroupConfiguration> m_Map = new HashMap<String, AlgorithmGroupConfiguration>();


   private static void readConfigFile() {

      BufferedReader input = null;
      try {
         input = new BufferedReader(new FileReader(getConfigFile()));
         String sLine = null;
         while ((sLine = input.readLine()) != null) {
            final String[] sTokens = sLine.split("@");
            try {
               final String sName = sTokens[0];
               final AlgorithmGroupConfiguration conf = AlgorithmGroupConfiguration.fromString(sTokens[1]);
               if (conf != null) {
                  m_Map.put(sName, conf);
               }
            }
            catch (final Exception e) {}
         }
      }
      catch (final FileNotFoundException e) {}
      catch (final IOException e) {}
      finally {
         try {
            if (input != null) {
               input.close();
            }
         }
         catch (final IOException e) {
            Sextante.addErrorToLog(e);
         }
      }

   }


   public static void saveSettings() {

      Writer output = null;
      try {
         output = new BufferedWriter(new FileWriter(getConfigFile()));
         final Set<String> set = m_Map.keySet();
         final Iterator<String> iter = set.iterator();
         while (iter.hasNext()) {
            final String sKey = iter.next();
            final AlgorithmGroupConfiguration conf = m_Map.get(sKey);
            output.write(sKey + "@" + conf.toString() + "\n");
         }
      }
      catch (final IOException e) {
         Sextante.addErrorToLog(e);
      }
      finally {
         if (output != null) {
            try {
               output.close();
            }
            catch (final IOException e) {
               Sextante.addErrorToLog(e);
            }
         }
      }

   }


   public static HashMap<String, AlgorithmGroupConfiguration> getGrouppingMap() {

      return m_Map;

   }


   private static String getConfigFile() {

      String sPath = System.getProperty("user.home") + File.separator + "sextante";

      sPath = sPath + File.separator + "sextante_alg_groups.settings";

      return sPath;

   }


   public static void setConfiguration(final HashMap<String, AlgorithmGroupConfiguration> map) {

      m_Map = map;

   }


   public static AlgorithmGroupConfiguration getGroupConfiguration(final GeoAlgorithm alg) {


      return m_Map.get(alg.getCommandLineName());

   }


   public static void restore() {

      m_Map.clear();

   }

}
