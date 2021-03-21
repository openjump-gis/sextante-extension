package es.unex.sextante.gui.cmd.bshcommands;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.gui.core.SextanteGUI;


public class getrasterlayers {

   public static String[] invoke(final Interpreter env,
                                 final CallStack callstack) {

      final IRasterLayer[] rasterLayers = SextanteGUI.getInputFactory().getRasterLayers();
      final String[] sNames = new String[rasterLayers.length];
      for (int i = 0; i < rasterLayers.length; i++) {
         sNames[i] = rasterLayers[i].getName();
      }

      return sNames;

   }

}
