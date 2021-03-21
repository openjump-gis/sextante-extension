package es.unex.sextante.gui.history;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;

/**
 * A class used to store the history of algorithms and commands executed by sextante. All of them are stored as command-line
 * commands ,and as such can be re-executed later
 * 
 * @author volaya
 * 
 */
public class History {

   private static long m_SessionStartingTime;


   /**
    * Adds a set of expressions to the history
    * 
    * @param sExp
    *                an array of expressions
    */
   public static void addToHistory(final String[] sExp) {

      for (int i = 0; i < sExp.length; i++) {
         addToHistory(sExp[i]);
      }

   }


   /**
    * Adds a single expression to the history
    * 
    * @param sExp
    *                an expression
    */
   public static void addToHistory(final String sExp) {

      String s;
      Writer output = null;
      s = Long.toString(System.currentTimeMillis()) + "@" + sExp.replace("\\", "\\\\") + "\n";

      try {
         output = new BufferedWriter(new FileWriter(getHistoryFile(), true));
         output.write(s);
         output.flush();
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


   /**
    * Returns a list of the 10 most recently used algorithms
    * 
    * @return a list of the 10 most recently used algorithms
    */
   public static GeoAlgorithm[] getRecentlyUsedAlgs() {

      final ArrayList<String> algs = new ArrayList<String>();
      final ArrayList<DateAndCommand> history = getHistory();
      for (int i = history.size() - 1; i > -1; i--) {
         final DateAndCommand dac = history.get(i);
         final String command = dac.getCommand();
         if (command.startsWith("runalg")) {
            final String sAlg = command.split("\"")[1];
            if (!algs.contains(sAlg)) {
               final GeoAlgorithm alg = Sextante.getAlgorithmFromCommandLineName(sAlg);
               if (alg != null) {
                  algs.add(sAlg);
                  if (algs.size() == 10) {
                     break;
                  }
               }
            }
         }

      }

      final GeoAlgorithm[] recentAlgs = new GeoAlgorithm[algs.size()];
      for (int i = 0; i < recentAlgs.length; i++) {
         recentAlgs[i] = Sextante.getAlgorithmFromCommandLineName(algs.get(i));
      }

      return recentAlgs;

   }


   /**
    * Returns the history as an array list with expressions and dates
    * 
    * @return the history
    */
   public static ArrayList<DateAndCommand> getHistory() {

      final ArrayList<DateAndCommand> list = new ArrayList<DateAndCommand>();

      BufferedReader input = null;
      try {
         input = new BufferedReader(new FileReader(getHistoryFile()));
         String sLine = null;
         while ((sLine = input.readLine()) != null) {
            final String[] sTokens = sLine.split("@");
            final DateAndCommand dac = new DateAndCommand();
            dac.setDate(new Date(Long.parseLong(sTokens[0])));
            dac.setCommand(sTokens[1]);
            list.add(dac);
         }
      }
      catch (final FileNotFoundException ex) {
         //ex.printStackTrace();
      }
      catch (final IOException ex) {
         //ex.printStackTrace();
      }
      finally {
         try {
            if (input != null) {
               input.close();
            }
         }
         catch (final IOException ex) {
            //ex.printStackTrace();
         }
      }


      return list;

   }


   private static String getHistoryFile() {

      String sPath = System.getProperty("user.home") + File.separator + "sextante";

      final File sextanteFolder = new File(sPath);
      if (!sextanteFolder.exists()) {
         sextanteFolder.mkdir();
      }

      sPath = sPath + File.separator + "sextante.history";

      return sPath;

   }


   /**
    * Starts this session, storing its starting time This will be used to refer history commands to this session starting time on
    * the history dialog
    */
   public static void startSession() {

      m_SessionStartingTime = System.currentTimeMillis();

   }


   public static long getSessionStartingTime() {

      return m_SessionStartingTime;

   }


   public static void clear() {

      Writer output = null;

      try {
         output = new BufferedWriter(new FileWriter(getHistoryFile(), false));
         output.flush();
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

}
