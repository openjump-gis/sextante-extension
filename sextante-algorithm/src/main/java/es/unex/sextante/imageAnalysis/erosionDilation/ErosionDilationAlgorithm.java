package es.unex.sextante.imageAnalysis.erosionDilation;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class ErosionDilationAlgorithm
         extends
            GeoAlgorithm {

   public static final int    ERODE     = 0;
   public static final int    DILATE    = 1;

   public static final String LAYER     = "LAYER";
   public static final String OPERATION = "OPERATION";
   public static final String RADIUS    = "RADIUS";
   public static final String RESULT    = "RESULT";

   protected final byte       NO_DATA   = 0;

   private int                m_iRadius;
   private boolean            m_bIsValidCell[][];
   private boolean            m_bIsForegroundCell[][];
   private IRasterLayer       m_Image;
   private int                m_iOperationType;


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("Erosion"), Sextante.getText("Dilation") };

      setUserCanDefineAnalysisExtent(false);
      setName(Sextante.getText("Erosion-Dilation"));
      setGroup(Sextante.getText("Image_processing"));

      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Image"), true);
         m_Parameters.addSelection(OPERATION, Sextante.getText("Operation"), sMethod);
         m_Parameters.addNumericalValue(RADIUS, Sextante.getText("Radius"), AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER,
                  1, 1, 20);
         addOutputRasterLayer(RESULT, this.getName());
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      int iValidCells = 0;

      m_iRadius = m_Parameters.getParameterValueAsInt(RADIUS);
      m_iOperationType = m_Parameters.getParameterValueAsInt(OPERATION);
      m_Image = m_Parameters.getParameterValueAsRasterLayer(LAYER);

      m_Image.setFullExtent();

      final IRasterLayer result = getNewRasterLayer(RESULT, this.getName(), IRasterLayer.RASTER_DATA_TYPE_BYTE,
               m_Image.getWindowGridExtent());

      result.setNoDataValue(NO_DATA);
      result.assignNoData();

      iNX = m_Image.getNX();
      iNY = m_Image.getNY();

      m_bIsValidCell = new boolean[2 * m_iRadius + 1][2 * m_iRadius + 1];
      m_bIsForegroundCell = new boolean[2 * m_iRadius + 1][2 * m_iRadius + 1];

      for (y = -m_iRadius; y < m_iRadius + 1; y++) {
         for (x = -m_iRadius; x < m_iRadius + 1; x++) {
            final double dDist = Math.sqrt(x * x + y * y);
            if (dDist <= m_iRadius) {
               m_bIsValidCell[x + m_iRadius][y + m_iRadius] = true;
               iValidCells++;
            }
            else {
               m_bIsValidCell[x + m_iRadius][y + m_iRadius] = false;
            }
         }
      }

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            setNeighborhoodValues(x, y);
            result.setCellValue(x, y, operate());
         }
      }

      return !m_Task.isCanceled();

   }


   private byte operate() {

      switch (m_iOperationType) {
         case ERODE:
         default:
            for (int i = 0; i < m_bIsValidCell.length; i++) {
               for (int j = 0; j < m_bIsValidCell[0].length; j++) {
                  if ((m_bIsValidCell[i][j] == true) && (m_bIsForegroundCell[i][j] == false)) {
                     return NO_DATA;
                  }
               }
            }
            return 1;
         case DILATE:
            for (int i = 0; i < m_bIsValidCell.length; i++) {
               for (int j = 0; j < m_bIsValidCell[0].length; j++) {
                  if ((m_bIsValidCell[i][j] == true) && (m_bIsForegroundCell[i][j] == true)) {
                     return 1;
                  }
               }
            }
            return NO_DATA;
      }
   }


   private void setNeighborhoodValues(final int iX,
                                      final int iY) {

      int x, y;
      int iCell = 0;
      double dValue;

      for (y = -m_iRadius; y < m_iRadius + 1; y++) {
         for (x = -m_iRadius; x < m_iRadius + 1; x++) {
            if (m_bIsValidCell[x + m_iRadius][y + m_iRadius]) {
               dValue = m_Image.getCellValueAsDouble(iX + x, iY + y);
               if (!m_Image.isNoDataValue(dValue) && (dValue != 0)) {
                  m_bIsForegroundCell[x + m_iRadius][y + m_iRadius] = true;
               }
               else {
                  m_bIsForegroundCell[x + m_iRadius][y + m_iRadius] = false;
               }
               iCell++;
            }
         }
      }

   }


}
