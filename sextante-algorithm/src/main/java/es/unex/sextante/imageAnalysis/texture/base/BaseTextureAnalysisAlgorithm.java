package es.unex.sextante.imageAnalysis.texture.base;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public abstract class BaseTextureAnalysisAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER            = "LAYER";
   public static final String WINDOW_RADIUS    = "WINDOW_RADIUS";
   public static final String RESULT           = "RESULT";
   public static final String SHIFTX           = "SHIFTX";
   public static final String SHIFTY           = "SHIFTY";
   public static final int    GRAYSCALE_LEVELS = 16;

   private IRasterLayer       m_Image;
   protected byte[][]         m_GLCM;
   private int                m_iWindowRadius;
   private int                m_iWindowSize;
   private int                m_iShiftX;
   private int                m_iShiftY;
   private double             m_ProbabilityFraction;


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Image_processing"));

      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Image"), true);
         m_Parameters.addNumericalValue(WINDOW_RADIUS, Sextante.getText("Window_radius"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 2, 1, 15);
         m_Parameters.addNumericalValue(SHIFTX, Sextante.getText("ShiftX"), AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER,
                  1, 1, 15);
         m_Parameters.addNumericalValue(SHIFTY, Sextante.getText("ShiftY"), AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER,
                  1, 1, 15);
      }
      catch (final RepeatedParameterNameException e) {
         e.printStackTrace();
      }

      addOutputRasterLayer(RESULT, Sextante.getText("Result"));

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;

      iNX = m_AnalysisExtent.getNX();
      iNY = m_AnalysisExtent.getNY();

      final IRasterLayer result = getNewRasterLayer(RESULT, this.getName(), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
      m_Image = m_Parameters.getParameterValueAsRasterLayer(LAYER);
      m_iWindowRadius = m_Parameters.getParameterValueAsInt(WINDOW_RADIUS);
      m_iShiftX = m_Parameters.getParameterValueAsInt(SHIFTX);
      m_iShiftY = m_Parameters.getParameterValueAsInt(SHIFTY);
      m_iWindowSize = m_iWindowRadius * 2 + 1;
      m_GLCM = new byte[GRAYSCALE_LEVELS][GRAYSCALE_LEVELS];

      m_ProbabilityFraction = (1. / ((m_iWindowSize - m_iShiftY) * (m_iWindowSize - m_iShiftY)));

      for (y = m_iWindowRadius; (y < iNY - m_iWindowRadius) && setProgress(y, iNY); y++) {
         for (x = m_iWindowRadius; x < iNX - m_iWindowRadius; x++) {
            if (calculateGLCM(x, y)) {
               result.setCellValue(x, y, getTextureFeature());
            }
            else {
               result.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }


   private boolean calculateGLCM(int x,
                                 int y) throws GeoAlgorithmExecutionException {

      //center the window
      x -= m_iWindowRadius;
      y -= m_iWindowRadius;


      //init the GLCM
      for (int i = 0; i < GRAYSCALE_LEVELS; i++) {
         for (int j = 0; j < GRAYSCALE_LEVELS; j++) {
            m_GLCM[i][j] = 0;
         }
      }

      try {
         for (int iX = 0; iX < m_iWindowRadius - m_iShiftX; iX++) {
            for (int iY = 0; iY < m_iWindowRadius - m_iShiftY; iY++) {
               final byte valueA = m_Image.getCellValueAsByte(x + iX, y + iY);
               if (m_Image.isNoDataValue(valueA)) {
                  return false;
               }
               final byte valueB = m_Image.getCellValueAsByte(x + iX + m_iShiftX, y + iY + m_iShiftY);
               if (m_Image.isNoDataValue(valueB)) {
                  return false;
               }
               m_GLCM[valueA][valueB] += m_ProbabilityFraction;
               m_GLCM[valueB][valueA] += m_ProbabilityFraction;
            }
         }
      }
      catch (final ArrayIndexOutOfBoundsException e) {
         throw new GeoAlgorithmExecutionException("Wrong_input_layer_for_texture_analysis");
      }


      return true;

   }


   protected abstract double getTextureFeature();

}
