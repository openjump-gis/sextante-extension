package es.unex.sextante.gui.grass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import es.unex.sextante.core.Sextante;

public class GrassVInfoUtils {

   //GRASS geometric primitives
   public static final int GEOM_TYPE_POINT   = 0;
   public static final int GEOM_TYPE_LINE    = 1;
   public static final int GEOM_TYPE_POLYGON = 2;
   public static final int GEOM_TYPE_FACE    = 3; //A simple 3D polygon (3D triangle)
   public static final int GEOM_TYPE_KERNEL  = 4; //A 3D centroid. Will be treated like a 3D point


   /**
    * Returns the number of geometries of a specified type in a GRASS map. This methods runs the GRASS command "v.info" to obtain
    * statistics for the GRASS map.
    * 
    * @param sMapName
    *                Name of GRASS map to query
    * 
    * @param iGeomType
    *                Type of geometry to query. Choices are: GEOM_TYPE_POINT, GEOM_TYPE_LINE, GEOM_TYPE_POLYGON
    */
   public static int getNumGeoms(final String sMapName,
                                 final int iGeomType) throws GrassExecutionException {

      //      if (isProcessCanceled()) {
      //         return 0;
      //      }

      final String cmdline = "v.info -t " + sMapName;
      Integer numGeoms = 0;

      if (!Sextante.isWindows()) {
         Writer output = null;
         try {
            output = new BufferedWriter(new FileWriter(GrassUtils.getBatchJobFile()));
            output.write(cmdline);
         }
         catch (final Exception e) {
            throw new GrassExecutionException();
         }
         finally {
            if (output != null) {
               try {
                  output.close();
               }
               catch (final IOException e) {
                  throw new GrassExecutionException();
               }
            }
            GrassUtils.setExecutable(GrassUtils.getBatchJobFile());
         }
      }

      //Execute GRASS
      try {
         final ProcessBuilder pb = GrassUtils.getGrassExecutable(cmdline);
         final Process process = pb.start();
         if (Sextante.isUnix() || Sextante.isMacOSX()) {
            //Proper OS: read from STDIN
            final InputStream is = process.getErrorStream();
            final InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = null;
            br = new BufferedReader(isr);
            String line;
            process.waitFor();

            while ((line = br.readLine()) != null) {
               if (line.contains("points=")) {
                  if (iGeomType == GEOM_TYPE_POINT) {
                     numGeoms = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                     break;
                  }
               }
               if (line.contains("lines=")) {
                  if (iGeomType == GEOM_TYPE_LINE) {
                     numGeoms = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                     break;
                  }
               }
               if (line.contains("areas=")) {
                  if (iGeomType == GEOM_TYPE_POLYGON) {
                     numGeoms = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                     break;
                  }
               }
               if (line.contains("faces=")) {
                  if (iGeomType == GEOM_TYPE_FACE) {
                     numGeoms = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                     break;
                  }
               }
               if (line.contains("kernels=")) {
                  if (iGeomType == GEOM_TYPE_KERNEL) {
                     numGeoms = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                     break;
                  }
               }
            }
            return (numGeoms.intValue());
         }
         else {
            final NumGeomStreamGobbler errorGobbler = new NumGeomStreamGobbler(process.getErrorStream(), iGeomType);
            final NumGeomStreamGobbler outputGobbler = new NumGeomStreamGobbler(process.getInputStream(), iGeomType);
            errorGobbler.start();
            outputGobbler.start();
            process.waitFor();
            return outputGobbler.getNumGeoms();
         }
      }
      catch (final Exception e) {
         throw new GrassExecutionException();
      }

   }


   /**
    * Returns the number of geometries of type POINT in a GRASS map. This methods runs the GRASS command "v.info" to obtain
    * statistics for the GRASS map.
    * 
    * @param sMapName
    *                Name of GRASS map to query
    * 
    */
   public static int getNumPoints(final String sMapName) throws GrassExecutionException {
      int result = 0;

      try {
         result = getNumGeoms(sMapName, GEOM_TYPE_POINT);
      }
      catch (final Exception e) {
         throw new GrassExecutionException();
      }
      return (result);
   }


   /**
    * Returns the number of geometries of type LINE in a GRASS map. This methods runs the GRASS command "v.info" to obtain
    * statistics for the GRASS map.
    * 
    * @param sMapName
    *                Name of GRASS map to query
    * 
    */
   public static int getNumLines(final String sMapName) throws GrassExecutionException {
      int result = 0;

      try {
         result = getNumGeoms(sMapName, GEOM_TYPE_LINE);
      }
      catch (final Exception e) {
         throw new GrassExecutionException();
      }
      return (result);
   }


   /**
    * Returns the number of geometries of type POLYGON in a GRASS map. This methods runs the GRASS command "v.info" to obtain
    * statistics for the GRASS map.
    * 
    * @param sMapName
    *                Name of GRASS map to query
    * 
    */
   public static int getNumPolygons(final String sMapName) throws GrassExecutionException {
      int result = 0;

      try {
         result = getNumGeoms(sMapName, GEOM_TYPE_POLYGON);
      }
      catch (final Exception e) {
         throw new GrassExecutionException();
      }
      return (result);
   }


   /**
    * Returns the number of geometries of type FACE in a GRASS map. A face is a simple 3D polygon used to build triangulated 3D
    * meshes. Some GRASS modules output 3D triangles (faces).
    * 
    * Note: In GRASS, the entitites that are usually called "polygons" in any other GIS are actually called "areas" and they are
    * topological objects, consisting of boundaries and centroids. Proper GRASS areas only exist if there are no major topological
    * problems in the data (specifidally: overlap). Data with badly overlapping polygons cannot be processed in GRASS without
    * topological cleaning.
    * 
    * This methods runs the GRASS command "v.info" to obtain statistics for the GRASS map.
    * 
    * @param sMapName
    *                Name of GRASS map to query
    * 
    */
   public static int getNumFaces(final String sMapName) throws GrassExecutionException {
      int result = 0;

      try {
         result = getNumGeoms(sMapName, GEOM_TYPE_FACE);
      }
      catch (final Exception e) {
         throw new GrassExecutionException();
      }
      return (result);
   }


   /**
    * Returns the number of geometries of type KERNEL in a GRASS map. A kernel is a 3D point. It has the same function as a 2D
    * centroid for an area in GRASS. However, threre is still a discussion about what is the full equivalent of a 2D area in 3D.
    * So for the time being, we will just treat kernels like simple 3D points. This methods runs the GRASS command "v.info" to
    * obtain statistics for the GRASS map.
    * 
    * @param sMapName
    *                Name of GRASS map to query
    * 
    */
   public static int getNumKernels(final String sMapName) throws GrassExecutionException {
      int result = 0;

      try {
         result = getNumGeoms(sMapName, GEOM_TYPE_KERNEL);
      }
      catch (final Exception e) {
         throw new GrassExecutionException();
      }
      return (result);
   }


   /**
    * Checks if a GRASS vector map contains 3D data. This methods runs the GRASS command "v.info" to obtain statistics for the
    * GRASS map.
    * 
    * @param sMapName
    *                Name of GRASS map to query
    * 
    */
   public static boolean isMap3D(final String sMapName) throws GrassExecutionException {

      boolean is3D = false;

      final String cmdline = "v.info -t " + sMapName;

      if (!Sextante.isWindows()) {
         Writer output = null;
         try {
            output = new BufferedWriter(new FileWriter(GrassUtils.getBatchJobFile()));
            output.write(cmdline);
         }
         catch (final Exception e) {
            throw new GrassExecutionException();
         }
         finally {
            if (output != null) {
               try {
                  output.close();
               }
               catch (final IOException e) {
                  throw new GrassExecutionException();
               }
            }
            GrassUtils.setExecutable(GrassUtils.getBatchJobFile());
         }
      }

      //Execute GRASS
      try {
         final ProcessBuilder pb = GrassUtils.getGrassExecutable(cmdline);
         final Process process = pb.start();
         if (Sextante.isUnix() || Sextante.isMacOSX()) {
            //Proper OS: read from STDIN
            final InputStream is = process.getErrorStream();
            final InputStreamReader isr = new InputStreamReader(is);
            final FileInputStream fis = null;
            final InputStreamReader in = null;
            BufferedReader br = null;
            br = new BufferedReader(isr);
            String line;
            process.waitFor();
            while ((line = br.readLine()) != null) {
               if (line.contains("map3d=1")) {
                  is3D = true;
                  break;
               }
            }
         }
         else {
            final Is3DStreamGobbler errorGobbler = new Is3DStreamGobbler(process.getErrorStream());
            final Is3DStreamGobbler outputGobbler = new Is3DStreamGobbler(process.getInputStream());
            errorGobbler.start();
            outputGobbler.start();
            process.waitFor();
            return outputGobbler.is3D();
         }
      }
      catch (final Exception e) {
         throw new GrassExecutionException();
      }

      return (is3D);
   }


}


class NumGeomStreamGobbler
         extends
            Thread {

   InputStream is;
   //String      type;
   int         iGeomType;
   Integer     numGeoms;


   NumGeomStreamGobbler(final InputStream is,
                        final int iGeomType) {

      this.iGeomType = iGeomType;
      this.is = is;

   }


   public int getNumGeoms() {

      return (numGeoms.intValue());

   }


   @Override
   public void run() {
      try {
         final InputStreamReader isr = new InputStreamReader(is);
         final BufferedReader br = new BufferedReader(isr);
         String line = null;
         while ((line = br.readLine()) != null) {
            if (line.contains("points=")) {
               if (iGeomType == GrassVInfoUtils.GEOM_TYPE_POINT) {
                  numGeoms = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                  break;
               }
            }
            if (line.contains("lines=")) {
               if (iGeomType == GrassVInfoUtils.GEOM_TYPE_LINE) {
                  numGeoms = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                  break;
               }
            }
            if (line.contains("areas=")) {
               if (iGeomType == GrassVInfoUtils.GEOM_TYPE_POLYGON) {
                  numGeoms = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                  break;
               }
            }
            if (line.contains("faces=")) {
               if (iGeomType == GrassVInfoUtils.GEOM_TYPE_FACE) {
                  numGeoms = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                  break;
               }
            }
            if (line.contains("kernels=")) {
               if (iGeomType == GrassVInfoUtils.GEOM_TYPE_KERNEL) {
                  numGeoms = Integer.parseInt(line.substring(line.indexOf("=") + 1));
                  break;
               }
            }
         }
      }
      catch (final IOException ioe) {}
   }
}


class Is3DStreamGobbler
         extends
            Thread {

   InputStream is;
   boolean     bIs3D;


   Is3DStreamGobbler(final InputStream is) {

      bIs3D = false;
      this.is = is;

   }


   public boolean is3D() {

      return bIs3D;

   }


   @Override
   public void run() {
      try {
         final InputStreamReader isr = new InputStreamReader(is);
         final BufferedReader br = new BufferedReader(isr);
         String line = null;
         while ((line = br.readLine()) != null) {
            if (line.contains("map3d=1")) {
               bIs3D = true;
               break;
            }
         }
      }
      catch (final IOException ioe) {}
   }
}
