package es.unex.sextante.vectorize.rasterToPoints;

import java.awt.geom.Point2D;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class RasterToPointsAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT = "RESULT";
   public static final String LAYER  = "LAYER";

   private IRasterLayer       m_Window;
   private IVectorLayer       m_Points;
   private int                m_iNX, m_iNY;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Raster_layer_to_points_layer"));
      setGroup(Sextante.getText("Vectorization"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Input_layer"), true);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POINT);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      double dValue;
      Point2D pt;
      Geometry point;
      final Object[] value = new Object[1];

      m_Window = m_Parameters.getParameterValueAsRasterLayer(LAYER);

      final String sFields[] = new String[] { m_Window.getName() };
      final Class types[] = { Double.class };

      m_Window.setWindowExtent(m_AnalysisExtent);
      m_Points = getNewVectorLayer(RESULT, Sextante.getText("Points"), IVectorLayer.SHAPE_TYPE_POINT, types, sFields);


      m_iNX = m_Window.getNX();
      m_iNY = m_Window.getNY();

      final GeometryFactory gf = new GeometryFactory();
      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            dValue = m_Window.getCellValueAsDouble(x, y);
            if (!m_Window.isNoDataValue(dValue)) {
               pt = m_Window.getWindowGridExtent().getWorldCoordsFromGridCoords(x, y);
               point = gf.createPoint(new Coordinate(pt.getX(), pt.getY()));
               value[0] = new Double(dValue);
               m_Points.addFeature(point, value);
            }
         }
      }

      return !m_Task.isCanceled();

   }


}
