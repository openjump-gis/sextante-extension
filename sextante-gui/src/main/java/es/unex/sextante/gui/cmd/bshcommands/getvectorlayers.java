package es.unex.sextante.gui.cmd.bshcommands;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.core.AbstractInputFactory;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.gui.core.SextanteGUI;

public class getvectorlayers {

   public static String[] invoke(final Interpreter env,
                                 final CallStack callstack) {

      final IVectorLayer[] vectorLayers = SextanteGUI.getInputFactory().getVectorLayers(AbstractInputFactory.SHAPE_TYPE_ANY);
      final String[] sNames = new String[vectorLayers.length];
      for (int i = 0; i < vectorLayers.length; i++) {
         sNames[i] = vectorLayers[i].getName();
      }

      return sNames;

   }

}
