package es.unex.sextante.imageAnalysis.pixelOpsBase;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public abstract class PixelOpsBaseAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT = "RESULT";
   public static final String IMAGE  = "IMAGE";

   protected IRasterLayer     m_Image;


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Image_processing"));

      try {
         m_Parameters.addInputRasterLayer(IMAGE, Sextante.getText("Image"), true);
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
      double dValue;

      setValues();
      final IRasterLayer result = getNewRasterLayer(RESULT, Sextante.getText("Result"), m_Image.getDataType());

      iNX = m_AnalysisExtent.getNX();
      iNY = m_AnalysisExtent.getNY();

      setProgressText(Sextante.getText("Processing"));
      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dValue = m_Image.getCellValueAsDouble(x, y);
            if (!m_Image.isNoDataValue(dValue)) {
               result.setCellValue(x, y, getValueAt(x, y));
            }
            else {
               result.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }


   protected void setValues() throws GeoAlgorithmExecutionException {

      m_Image = m_Parameters.getParameterValueAsRasterLayer(IMAGE);
      m_Image.setFullExtent();

   }


   protected abstract double getValueAt(int x,
                                        int y);

}
