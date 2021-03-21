package es.unex.sextante.gui.cmd.bshcommands;

import java.awt.geom.Rectangle2D;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.ILayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.gui.cmd.CommandLineData;
import es.unex.sextante.gui.core.SextanteGUI;

/**
 * A BeanShell command to change the grid extent used to generate new raster layers from SEXTANTE algorithms
 * 
 * @author volaya
 * 
 */
public class extent {

   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sLayer) {
      try {
         final ILayer layer = (ILayer) SextanteGUI.getInputFactory().getInputFromName(sLayer);
         if ((layer instanceof I3DRasterLayer) || (layer instanceof IRasterLayer)) {
            CommandLineData.setAnalysisExtent(new AnalysisExtent(layer));
         }
         else {
            env.println("Wrong layer:" + sLayer.trim());
         }
      }
      catch (final ClassCastException e) {
         env.println("Wrong layer:" + sLayer.trim());
      }

   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sLayer,
                             final double dCellSize) {

      try {
         final IVectorLayer layer = (IVectorLayer) SextanteGUI.getInputFactory().getInputFromName(sLayer);
         final AnalysisExtent ge = new AnalysisExtent();
         ge.setCellSize(dCellSize);
         final Rectangle2D rect = layer.getFullExtent();
         ge.setXRange(rect.getMinX(), rect.getMaxX(), true);
         ge.setYRange(rect.getMinY(), rect.getMaxY(), true);
         CommandLineData.setAnalysisExtent(ge);
      }
      catch (final ClassCastException e) {
         env.println("Wrong layer:" + sLayer.trim());
      }
      catch (final Exception e) {
         env.println("Could not set analysis extent");
      }


   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final double xMin,
                             final double yMin,
                             final double xMax,
                             final double yMax,
                             final double dCellSize) {

      try {
         final AnalysisExtent ge = new AnalysisExtent();
         ge.setCellSize(dCellSize);
         ge.setXRange(xMin, xMax, true);
         ge.setYRange(yMin, yMax, true);
         CommandLineData.setAnalysisExtent(ge);
      }
      catch (final NumberFormatException e) {
         env.print("Wrong dimensions");
      }

   }


   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final double xMin,
                             final double yMin,
                             final double zMin,
                             final double xMax,
                             final double yMax,
                             final double zMax,
                             final double dCellSize,
                             final double dCellSizeZ) {

      try {
         final AnalysisExtent ae = new AnalysisExtent();
         ae.setCellSize(dCellSize);
         ae.setXRange(xMin, xMax, true);
         ae.setYRange(yMin, yMax, true);
         ae.setCellSizeZ(dCellSizeZ);
         ae.setZRange(zMin, zMax, true);
         CommandLineData.setAnalysisExtent(ae);
      }
      catch (final NumberFormatException e) {
         env.print("Wrong dimensions");
      }

   }

}
