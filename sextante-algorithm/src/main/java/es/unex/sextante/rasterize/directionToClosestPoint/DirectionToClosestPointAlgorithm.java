package es.unex.sextante.rasterize.directionToClosestPoint;

import java.awt.geom.Point2D;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.closestpts.Point3D;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rTree.SextanteRTree;
import es.unex.sextante.rasterWrappers.GridCell;

public class DirectionToClosestPointAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";

   protected IVectorLayer     m_Layer;

   private SextanteRTree      m_SearchEngine;


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Cost_distances_and_routes"));
      setName(Sextante.getText("Direction_to_closest_point"));

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Point_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);

      m_SearchEngine = new SextanteRTree(m_Layer, 0, m_Task);
      final IRasterLayer result = getNewRasterLayer(RESULT, m_Layer.getName() + "["
                                                            + Sextante.getText("Direction_to_closest_point") + "]",
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      iNX = m_AnalysisExtent.getNX();
      iNY = m_AnalysisExtent.getNY();

      setProgressText(Sextante.getText("Calculating"));
      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            result.setCellValue(x, y, getValueAt(x, y));
         }
      }

      return !m_Task.isCanceled();

   }


   protected double getValueAt(final int x,
                               final int y) {

      final Point2D pt = m_AnalysisExtent.getWorldCoordsFromGridCoords(new GridCell(x, y, 0));
      final Point3D closestPt = m_SearchEngine.getClosestPoint(pt.getX(), pt.getY());
      final Point2D closestPt2D = new Point2D.Double(closestPt.getX(), closestPt.getY());
      final Double y0 = closestPt2D.getY();
      final Double y1 = pt.getY();
      final Double x0 = closestPt2D.getX();
      final Double x1 = pt.getX();
      Double angle;
      if (((y1 - y0) >= 0) & ((x1 - x0) >= 0)) {
         angle = Math.atan((y1 - y0) / (x1 - x0));
      }
      else {
         if (((y1 - y0) >= 0) & ((x1 - x0) < 0)) {
            angle = Math.atan((y1 - y0) / (x1 - x0)) + Math.PI;
         }
         else {
            if (((y1 - y0) < 0) & ((x1 - x0) < 0)) {
               angle = Math.atan((y1 - y0) / (x1 - x0)) + Math.PI;
            }
            else {
               angle = Math.atan((y1 - y0) / (x1 - x0)) + 2 * Math.PI;
            }
         }
      }
      return angle;

   }

}
