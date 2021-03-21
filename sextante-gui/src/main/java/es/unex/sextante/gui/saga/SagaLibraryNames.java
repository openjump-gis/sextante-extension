

package es.unex.sextante.gui.saga;

import java.util.HashMap;


public class SagaLibraryNames {

   private static HashMap<String, String> map = new HashMap<String, String>();

   static {

      map.put("contrib_a_perego", "Contributions");
      map.put("docs_html", null);
      map.put("docs_pdf", null);
      map.put("geostatistics_grid", "Geostatistics");
      map.put("geostatistics_kriging", "Kriging");
      map.put("geostatistics_points", "Geostatistics");
      map.put("geostatistics_regression", "Geostatistics");
      map.put("grid_analysis", "Grid - Analysis");
      map.put("grid_calculus", "Grid - Calculus");
      map.put("grid_calculus_bsl", "Grid - Calculus");
      map.put("grid_discretisation", "Grid - Discretisation");
      map.put("grid_filter", "Grid - Filter");
      map.put("grid_gridding", "Grid - Gridding");
      map.put("grid_spline", "Grid - Spline");
      map.put("grid_tools", "Grid - Tools");
      map.put("grid_visualisation", "Grid - Visualization");
      map.put("hacres", "Hacres");
      map.put("imagery_segmentation", "Imagery - Segmentation");
      map.put("imagery_classification", "Imagery - Classification");
      map.put("imagery_rga", "Imagery - RGA");
      map.put("io_esri_e00", "I/O");
      map.put("io_gdal", "I/O");
      map.put("io_gps", "I/O");
      map.put("io_grid", "I/O");
      map.put("io_grid_grib2", "I/O");
      map.put("io_grid_image", "I/O");
      map.put("io_odbc", "I/O");
      map.put("io_shapes", "I/O");
      map.put("io_shapes_dxf", "I/O");
      map.put("io_shapes_las", "I/O");
      map.put("io_table", "I/O");
      map.put("lectures_introduction", "Lectures");
      map.put("pj_georeference", "Georeferencing");
      map.put("pj_geotrans", "Projections and Transformations");
      map.put("pj_proj4", "Projections and Transformations");
      map.put("pointcloud_tools", "Point clouds");
      map.put("pointcloud_viewer", null);
      map.put("recreations_fractals", "Recreations");
      map.put("recreations_games", "Diversiones");
      map.put("shapes_grid", "Shapes - Grid");
      map.put("shapes_lines", "Shapes - Lines");
      map.put("shapes_points", "Shapes - Points");
      map.put("shapes_polygons", "Shapes - Polygons");
      map.put("shapes_tools", "Shapes - Tools");
      map.put("shapes_transect", "Shapes - Transect");
      map.put("sim_cellular_automata", "Simulation - CA");
      map.put("sim_ecosystems_hugget", "Simulation - Ecosystems");
      map.put("sim_fire_spreading", "Simulation - Fire Spreading");
      map.put("sim_hydrology", "Simulation - Hydrology");
      map.put("table_calculus", "Table - Calculus");
      map.put("table_tools", "Table - Tools");
      map.put("ta_channels", "Terrain Analysis - Channels");
      map.put("ta_compound", "Terrain Analysis - Morphometry");
      map.put("ta_hydrology", "Terrain Analysis - Hydrology");
      map.put("ta_lighting", "Terrain Analysis - Lighting");
      map.put("ta_morphometry", "Terrain Analysis - Morphometry");
      map.put("ta_preprocessor", "Terrain Analysis - Hydrology");
      map.put("ta_profiles", "Terrain Analysis - Profiles");
      map.put("tin_tools", "TIN");
      map.put("vigra", "Vigra");

   }


   public static String getDecoratedLibraryName(final String sName) {

      final String s = map.get(sName);

      if (s != null) {
         return s;
      }
      else {
         return sName;
      }

   }

}
