package es.unex.sextante.gridTools.bboxGrid;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

/**
 * Creates a polygon layer with the extension of input grid
 * 
 * @author Nacho Varela
 */
public class bboxGridAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT  = "INPUT";
   public static final String RESULT = "RESULT";

   private IRasterLayer       m_Raster;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Create_grid_bounding_box"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);
      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("INPUT_LAYER"), true);
         addOutputVectorLayer(RESULT, Sextante.getText("bbox"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_Raster = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      final AnalysisExtent ext = m_Raster.getLayerGridExtent();

      final String[] sNames = { "ID" };
      final Class[] types = { Integer.class };

      final IVectorLayer output = getNewVectorLayer(RESULT, Sextante.getText("Result"), IVectorLayer.SHAPE_TYPE_POLYGON, types,
               sNames);

      final Coordinate[] coords = new Coordinate[5];
      coords[0] = new Coordinate(ext.getXMin(), ext.getYMin());
      coords[1] = new Coordinate(ext.getXMin(), ext.getYMax());
      coords[2] = new Coordinate(ext.getXMax(), ext.getYMax());
      coords[3] = new Coordinate(ext.getXMax(), ext.getYMin());
      coords[4] = new Coordinate(ext.getXMin(), ext.getYMin());

      LinearRing result;
      Geometry polygon;
      final GeometryFactory gf = new GeometryFactory();
      result = gf.createLinearRing(coords);
      polygon = gf.createPolygon(result, null);

      output.addFeature(polygon, new Object[] { new Integer(0) });

      return !m_Task.isCanceled();

   }
}
