package es.unex.sextante.gridAnalysis.cva;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class CVAAlgorithm
         extends
            GeoAlgorithm {

   public static final String A1    = "A1";
   public static final String A2    = "A2";
   public static final String B1    = "B1";
   public static final String B2    = "B2";
   public static final String ANGLE = "ANGLE";
   public static final String DIST  = "DIST";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Change_Vector_Analysis"));
      setGroup(Sextante.getText("Raster_layer_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(A1, Sextante.getText("Layer_1__init"), true);
         m_Parameters.addInputRasterLayer(A2, Sextante.getText("Layer_2__init"), true);
         m_Parameters.addInputRasterLayer(B1, Sextante.getText("Layer_1__final"), true);
         m_Parameters.addInputRasterLayer(B2, Sextante.getText("Layer_2__final"), true);
         addOutputRasterLayer(ANGLE, Sextante.getText("Angle"));
         addOutputRasterLayer(DIST, Sextante.getText("Modulus"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      double dAngle, dDist;
      double a1, a2, b1, b2;
      int iNX, iNY;
      IRasterLayer layerA1, layerA2, layerB1, layerB2;
      IRasterLayer Angle, Distance;

      layerA1 = m_Parameters.getParameterValueAsRasterLayer(A1);
      layerA2 = m_Parameters.getParameterValueAsRasterLayer(A2);
      layerB1 = m_Parameters.getParameterValueAsRasterLayer(B1);
      layerB2 = m_Parameters.getParameterValueAsRasterLayer(B2);

      Distance = getNewRasterLayer(DIST, Sextante.getText("Modulus"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
      Angle = getNewRasterLayer(ANGLE, Sextante.getText("Angle"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      final AnalysisExtent extent = Distance.getWindowGridExtent();

      layerA1.setWindowExtent(extent);
      layerA2.setWindowExtent(extent);
      layerB1.setWindowExtent(extent);
      layerB2.setWindowExtent(extent);

      iNX = extent.getNX();
      iNY = extent.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            a1 = layerA1.getCellValueAsDouble(x, y);
            a2 = layerA2.getCellValueAsDouble(x, y);
            b1 = layerB1.getCellValueAsDouble(x, y);
            b2 = layerB2.getCellValueAsDouble(x, y);
            if (layerA1.isNoDataValue(a1) || layerA2.isNoDataValue(a2) || layerB1.isNoDataValue(b1) || layerB2.isNoDataValue(b2)) {
               Distance.setNoData(x, y);
               Angle.setNoData(x, y);
            }
            else {
               dDist = Math.sqrt((a1 - a2) * (a1 - a2) + (b1 - b2) * (b1 - b2));
               dAngle = Math.atan((a1 - a2) / (b1 - b2));
               Distance.setCellValue(x, y, dDist);
               Angle.setCellValue(x, y, dAngle);
            }

         }
      }

      return !m_Task.isCanceled();

   }


}
