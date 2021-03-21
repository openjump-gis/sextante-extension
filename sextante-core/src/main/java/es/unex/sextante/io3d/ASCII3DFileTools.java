package es.unex.sextante.io3d;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.dataObjects.I3DRasterLayer;


public class ASCII3DFileTools {

   public static I3DRasterLayer readFile(final File file) throws IOException, NumberFormatException {

      String s = new String();
      int iX, iY, iZ;
      double dCellsize, dCellsizeZ;
      double dX, dY, dZ;
      final BufferedReader fin = new BufferedReader(new FileReader(file));

      s = fin.readLine();
      String[] sTokens = s.split(" ");
      iX = Integer.parseInt(sTokens[0]);
      iY = Integer.parseInt(sTokens[1]);
      iZ = Integer.parseInt(sTokens[2]);
      s = fin.readLine();
      sTokens = s.split(" ");
      dX = Double.parseDouble(sTokens[0]);
      dY = Double.parseDouble(sTokens[1]);
      dZ = Double.parseDouble(sTokens[2]);
      s = fin.readLine();
      sTokens = s.split(" ");
      dCellsize = Double.parseDouble(sTokens[0]);
      dCellsizeZ = Double.parseDouble(sTokens[1]);

      final AnalysisExtent ae = new AnalysisExtent();
      ae.setCellSize(dCellsize);
      ae.setCellSizeZ(dCellsizeZ);
      ae.setXRange(dX, dX + iX * dCellsize, true);
      ae.setYRange(dY, dY + iY * dCellsize, true);
      ae.setZRange(dZ, dZ + iZ * dCellsizeZ, true);

      final Default3DRasterLayer layer = new Default3DRasterLayer();
      layer.create("", file.getAbsolutePath(), ae, null);

      for (int z = 0; z < iZ; z++) {
         for (int y = 0; y < iY; y++) {
            s = fin.readLine();
            sTokens = s.split(" ");
            for (int x = 0; x < iX; x++) {
               layer.setCellValue(x, y, z, Double.parseDouble(sTokens[x]));
            }
         }
      }
      fin.close();

      return layer;

   }


   public static boolean writeFile(final I3DRasterLayer layer,
                                   final File file) {

      int x;
      try {
         final FileWriter writer = new FileWriter(file);
         final BufferedWriter out = new BufferedWriter(writer);
         final AnalysisExtent extent = layer.getLayerExtent();
         out.write(Integer.toString(extent.getNX()) + " " + Integer.toString(extent.getNY()) + " "
                   + Integer.toString(extent.getNZ()) + "\n");
         out.write(Double.toString(extent.getXMin()) + " " + Double.toString(extent.getYMin()) + " "
                   + Double.toString(extent.getZMin()) + "\n");
         out.write(Double.toString(layer.getCellSize()) + " " + Double.toString(layer.getCellSizeZ()) + "\n");
         for (int z = 0; z < layer.getNZ(); z++) {
            for (int y = 0; y < layer.getNY(); y++) {
               for (x = 0; x < layer.getNX() - 1; x++) {
                  out.write(Double.toString(layer.getCellValueAsDouble(x, y, z)) + " ");
               }
               out.write(Double.toString(layer.getCellValueAsDouble(x, y, z)) + "\n");
            }
         }
         out.close();
         writer.close();
      }
      catch (final Exception e) {
         e.printStackTrace();
         return false;
      }

      return true;

   }

}
