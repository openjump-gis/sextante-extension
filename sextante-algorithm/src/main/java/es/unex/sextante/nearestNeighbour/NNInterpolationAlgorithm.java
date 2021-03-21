

package es.unex.sextante.nearestNeighbour;

import java.awt.geom.Point2D;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.closestpts.Point3D;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.rTree.SextanteRTree;
import es.unex.sextante.rasterWrappers.GridCell;


public class NNInterpolationAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String FIELD  = "FIELD";
   public static final String RESULT = "RESULT";

   protected int              m_iField;

   protected IVectorLayer     m_Layer;

   private SextanteRTree      m_SearchEngine;


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Rasterization_and_interpolation"));
      setName(Sextante.getText("Nearest_neighbour"));

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Point_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), LAYER);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final UndefinedParentParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final OptionalParentParameterException e) {
         Sextante.addErrorToLog(e);
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
      m_iField = m_Parameters.getParameterValueAsInt(FIELD);

      m_SearchEngine = new SextanteRTree(m_Layer, m_iField, m_Task);
      final IRasterLayer result = getNewRasterLayer(RESULT, m_Layer.getName() + Sextante.getText("[interpolated]"),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      iNX = m_AnalysisExtent.getNX();
      iNY = m_AnalysisExtent.getNY();

      setProgressText(Sextante.getText("Interpolating"));
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
      return closestPt.getZ();

   }

}
