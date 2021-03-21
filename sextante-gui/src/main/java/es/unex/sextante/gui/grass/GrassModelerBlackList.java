package es.unex.sextante.gui.grass;

import java.util.ArrayList;
import java.util.List;

import es.unex.sextante.core.Sextante;


/**
 * A hard-coded list of those GRASS algorithms not compatible with the SEXTANTE modeler
 * 
 * @author volaya
 * 
 */
public class GrassModelerBlackList {

   private static List           m_List;
   private static final String[] ALGS = {};

   static {

      m_List = new ArrayList();
      for (int i = 0; i < ALGS.length; i++) {
         m_List.add(ALGS[i]);
      }

   }


   public static boolean isInBlackList(final GrassAlgorithm alg) {

      return (isInBlackList(alg.getName()));

   }


   public static boolean isInBlackList(final String sName) {
      if (m_List.contains(sName)) {
         Sextante.addWarningToLog("SEXTANTE GRASS interface: Module " + sName + " disabled: blacklisted.");
         return (true);
      }
      return (false);
   }


}
