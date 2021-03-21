package es.unex.sextante.gui.cmd.bshcommands;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.gui.core.SextanteGUI;

/**
 * A BeanShell command to show the available data object with which SEXTANTE could work
 *
 * @author volaya
 *
 */
public class data {

   public static void invoke(final Interpreter env,
                             final CallStack callstack) {

      final StringBuffer sb = new StringBuffer();
      final IRasterLayer[] rasterLayers = SextanteGUI.getInputFactory().getRasterLayers();
      sb.append("RASTER LAYERS\n");
      sb.append("-----------------\n");
      for (int i = 0; i < rasterLayers.length; i++) {
         sb.append(rasterLayers[i].getName() + "\n");
      }
      sb.append("\n");
      sb.append("VECTOR LAYERS\n");
      sb.append("-----------------\n");
      final IVectorLayer[] vectorLayers = SextanteGUI.getInputFactory().getVectorLayers(AdditionalInfoVectorLayer.SHAPE_TYPE_ANY);
      for (int i = 0; i < vectorLayers.length; i++) {
         sb.append(vectorLayers[i].getName() + "\n");
      }
      sb.append("\n");
      sb.append("TABLES\n");
      sb.append("-----------------\n");
      final ITable[] tables = SextanteGUI.getInputFactory().getTables();
      for (int i = 0; i < tables.length; i++) {
         sb.append(tables[i].getName() + "\n");
      }
      sb.append("\n");
      env.println(sb.toString());

   }

}
