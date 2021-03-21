package es.unex.sextante.gridTools.cropToValidData;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class CropToValidDataAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT = "RESULT";
   public static final String INPUT  = "INPUT";

   private IRasterLayer       m_Window;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Crop_to_valid_data_cells"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer_to_crop"), true);
         addOutputRasterLayer(RESULT, Sextante.getText("Cropped_layer"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;

      m_Window = m_Parameters.getParameterValueAsRasterLayer(INPUT);

      m_Window.setFullExtent();

      final AnalysisExtent extent = getAdjustedGridExtent(m_Window);


      m_Window.setWindowExtent(extent);
      final IRasterLayer output = this.getNewRasterLayer(RESULT, m_Window.getName() + Sextante.getText("[cropped]"),
               m_Window.getDataType(), extent);
      output.setNoDataValue(m_Window.getNoDataValue());

      iNX = extent.getNX();
      iNY = extent.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            output.setCellValue(x, y, m_Window.getCellValueAsDouble(x, y));
         }
      }
      return !m_Task.isCanceled();

   }


   private AnalysisExtent getAdjustedGridExtent(final IRasterLayer input) {

      int x, y;
      int iNX, iNY;
      double iMaxX, iMaxY;
      double iMinX, iMinY;
      double dMinX, dMaxY;
      double dMinX2, dMinY2, dMaxX2, dMaxY2;
      double dCellSize;
      double dValue;
      final AnalysisExtent ge = new AnalysisExtent();

      dMinX = input.getWindowGridExtent().getXMin();
      dMaxY = input.getWindowGridExtent().getYMax();
      dCellSize = input.getWindowGridExtent().getCellSize();

      iNX = input.getWindowGridExtent().getNX();
      iNY = input.getWindowGridExtent().getNY();

      iMinX = iNX;
      iMinY = iNY;
      iMaxX = 0;
      iMaxY = 0;
      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dValue = m_Window.getCellValueAsDouble(x, y);
            if (!m_Window.isNoDataValue(dValue)) {
               if (x < iMinX) {
                  iMinX = x;
               }
               if (x > iMaxX) {
                  iMaxX = x;
               }
               if (y < iMinY) {
                  iMinY = y;
               }
               if (y > iMaxY) {
                  iMaxY = y;
               }
            }
         }
      }

      //      if ((iMinX == 0) && (iMinY == 0) && (iMaxX == iNX - 1) && (iMaxY == iNY - 1)) {
      //         return null;
      //      }

      dMinX2 = dMinX + iMinX * dCellSize;
      dMinY2 = dMaxY - iMaxY * dCellSize;
      dMaxX2 = dMinX + iMaxX * dCellSize;
      dMaxY2 = dMaxY - iMinY * dCellSize;

      ge.setCellSize(dCellSize);
      ge.setXRange(dMinX2, dMaxX2, true);
      ge.setYRange(dMinY2, dMaxY2, true);

      return ge;

   }

}
