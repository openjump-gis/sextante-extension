package es.unex.sextante.gui.cmd.bshcommands;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.gui.core.SextanteGUI;

/**
 * A BeanShell command to describe a SEXTANTE data object
 *
 * @author volaya
 *
 */
public class describe {

   public static void invoke(final Interpreter env,
                             final CallStack callstack,
                             final String sName) {

      final Object obj = SextanteGUI.getInputFactory().getInputFromName(sName.trim());
      if (obj == null) {
         env.println("Invalid object: " + sName.trim());
      }
      if (obj instanceof ITable) {
         env.println(describeTable((ITable) obj));
      }
      else if (obj instanceof IRasterLayer) {
         env.println(describeRasterLayer((IRasterLayer) obj));
      }
      else if (obj instanceof IVectorLayer) {
         env.println(describeVectorLayer((IVectorLayer) obj));
      }

   }


   private static String describeTable(final ITable table) {

      final StringBuffer sb = new StringBuffer();

      final String sFields[] = table.getFieldNames();
      sb.append("Type: Table\n");
      sb.append("Number of records: " + Long.toString(table.getRecordCount()) + "\n");
      sb.append("Table fields: | ");
      for (int i = 0; i < sFields.length; i++) {
         sb.append(sFields[i] + " | ");
      }

      return sb.toString();

   }


   private static String describeVectorLayer(final IVectorLayer vect) {

      final StringBuffer sb = new StringBuffer();

      sb.append("Type: Vector layer - ");
      switch (vect.getShapeType()) {
         case IVectorLayer.SHAPE_TYPE_LINE:
            sb.append("Line\n");
            break;
         case IVectorLayer.SHAPE_TYPE_POLYGON:
            sb.append("Polygon\n");
            break;
         case IVectorLayer.SHAPE_TYPE_POINT:
            sb.append("Point\n");
            break;
      }
      sb.append("Number of entities: " + Integer.toString(vect.getShapesCount()) + "\n");
      sb.append("Table fields: | ");
      final String[] sFields = vect.getFieldNames();
      for (int i = 0; i < sFields.length; i++) {
         sb.append(sFields[i] + " | ");
      }

      return sb.toString();

   }


   private static String describeRasterLayer(final IRasterLayer raster) {

      final StringBuffer sb = new StringBuffer();
      final AnalysisExtent extent = raster.getLayerGridExtent();

      sb.append("Type: Raster layer \n");
      sb.append("X min: " + Double.toString(extent.getXMin()) + "\n");
      sb.append("X max: " + Double.toString(extent.getXMax()) + "\n");
      sb.append("Y min: " + Double.toString(extent.getYMin()) + "\n");
      sb.append("Y max: " + Double.toString(extent.getYMax()) + "\n");
      sb.append("Cellsize X: " + Double.toString(extent.getCellSize()) + "\n");
      sb.append("Rows: " + Integer.toString(extent.getNY()) + "\n");
      sb.append("Cols: " + Integer.toString(extent.getNX()));

      return sb.toString();

   }

}
