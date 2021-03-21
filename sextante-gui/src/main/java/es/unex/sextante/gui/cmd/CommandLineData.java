package es.unex.sextante.gui.cmd;

import es.unex.sextante.core.AnalysisExtent;

public class CommandLineData {

   private static boolean        m_bAutoExtent = true;
   private static AnalysisExtent m_GridExtent  = null;


   public static boolean getAutoExtent() {

      return m_bAutoExtent;

   }


   public static void setAutoExtent(final boolean b) {

      m_bAutoExtent = b;

   }


   public static AnalysisExtent getAnalysisExtent() {

      return m_GridExtent;

   }


   public static void setAnalysisExtent(final AnalysisExtent extent) {

      m_bAutoExtent = false;
      m_GridExtent = extent;

   }


}
