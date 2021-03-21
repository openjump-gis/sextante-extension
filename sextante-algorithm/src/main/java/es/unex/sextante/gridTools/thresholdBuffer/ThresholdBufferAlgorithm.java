package es.unex.sextante.gridTools.thresholdBuffer;

import java.awt.Point;
import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class ThresholdBufferAlgorithm
         extends
            GeoAlgorithm {

   private static final int   BUFFER                                = 1;
   private static final int   FEATURE                               = 2;
   private final static int   m_iOffsetX[]                          = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int   m_iOffsetY[]                          = { 1, 1, 0, -1, -1, -1, 0, 1 };

   public static final String FEATURES                              = "FEATURES";
   public static final String VALUES                                = "VALUES";
   public static final String THRESHOLD                             = "THRESHOLD";
   public static final String METHOD                                = "METHOD";
   public static final String GLOBAL_THRESHOLD                      = "GLOBAL_THRESHOLD";
   public static final String BUFFER_LAYER                          = "BUFFER_LAYER";

   public static final int    THRESHOLD_TYPE_ABSOLUTE               = 0;
   public static final int    THRESHOLD_TYPE_RELATIVE_TO_CELL_VALUE = 0;

   int                        m_iNX, m_iNY;
   int                        m_iThresholdType;
   double                     m_dThreshold;
   boolean                    m_bThresholdGridDefined;
   IRasterLayer               m_Features, m_Threshold, m_Values;
   IRasterLayer               m_Buffer;


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("Absolute"), Sextante.getText("Relative_to_cell_value") };

      setName(Sextante.getText("Threshold_buffer"));
      setGroup(Sextante.getText("Buffers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(FEATURES, Sextante.getText("Layer"), true);
         m_Parameters.addInputRasterLayer(VALUES, Sextante.getText("Threshold_parameter"), true);
         m_Parameters.addInputRasterLayer(THRESHOLD, Sextante.getText("Threshold_values"), false);
         m_Parameters.addSelection(METHOD, Sextante.getText("Threshold"), sMethod);
         m_Parameters.addNumericalValue(GLOBAL_THRESHOLD, Sextante.getText("global_threshold"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 100, Double.NEGATIVE_INFINITY, Double.MAX_VALUE);
         addOutputRasterLayer(BUFFER_LAYER, Sextante.getText("Buffer"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      double dValue;

      m_iThresholdType = m_Parameters.getParameterValueAsInt(METHOD);
      m_dThreshold = m_Parameters.getParameterValueAsDouble(GLOBAL_THRESHOLD);
      m_Features = m_Parameters.getParameterValueAsRasterLayer(FEATURES);
      m_Values = m_Parameters.getParameterValueAsRasterLayer(VALUES);
      m_Threshold = m_Parameters.getParameterValueAsRasterLayer(THRESHOLD);

      m_Buffer = getNewRasterLayer(BUFFER_LAYER, Sextante.getText("Buffer") + "[" + m_Features.getName() + "]",
               IRasterLayer.RASTER_DATA_TYPE_BYTE);

      m_Features.setWindowExtent(m_Buffer.getWindowGridExtent());
      m_Features.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);
      m_Values.setWindowExtent(m_Buffer.getWindowGridExtent());
      if (m_Threshold != null) {
         m_Threshold.setWindowExtent(m_Buffer.getWindowGridExtent());
         m_bThresholdGridDefined = true;
      }
      else {
         m_bThresholdGridDefined = false;
      }

      m_Buffer.assign(0.0);

      m_iNX = m_Features.getNX();
      m_iNY = m_Features.getNY();

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            dValue = m_Features.getCellValueAsDouble(x, y);
            if ((dValue != 0) && !m_Features.isNoDataValue(dValue)) {
               bufferPoint(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }


   private void bufferPoint(int x,
                            int y) {

      int x2, y2;
      int iValue;
      int iPt;
      int n;
      double dBaseValue;
      double dThreshold;
      double dValue;
      final ArrayList centralPoints = new ArrayList();
      final ArrayList adjPoints = new ArrayList();
      Point point;

      dBaseValue = m_Values.getCellValueAsDouble(x, y);

      if (m_bThresholdGridDefined) {
         dThreshold = m_Threshold.getCellValueAsDouble(x, y);
      }
      else {
         dThreshold = m_dThreshold;
      }

      centralPoints.add(new Point(x, y));
      m_Buffer.setCellValue(x, y, FEATURE);

      while (centralPoints.size() != 0) {
         for (iPt = 0; iPt < centralPoints.size(); iPt++) {
            point = (Point) centralPoints.get(iPt);
            x = point.x;
            y = point.y;
            dValue = m_Values.getCellValueAsDouble(x, y);
            if (!m_Values.isNoDataValue(dValue)) {
               for (n = 0; n < 8; n++) {
                  x2 = x + m_iOffsetX[n];
                  y2 = y + m_iOffsetY[n];
                  dValue = m_Values.getCellValueAsDouble(x2, y2);
                  if (!m_Values.isNoDataValue(dValue)) {
                     iValue = m_Buffer.getCellValueAsInt(x2, y2);
                     if (iValue == 0) {
                        if (m_iThresholdType == ThresholdBufferAlgorithm.THRESHOLD_TYPE_RELATIVE_TO_CELL_VALUE) {
                           dValue = Math.abs(dValue - dBaseValue);
                        }
                        if (dValue < dThreshold) {
                           m_Buffer.setCellValue(x2, y2, BUFFER);
                           adjPoints.add(new Point(x2, y2));
                        }
                     }
                  }
               }
            }
         }

         centralPoints.clear();
         for (iPt = 0; iPt < adjPoints.size(); iPt++) {
            point = (Point) adjPoints.get(iPt);
            centralPoints.add(new Point(point.x, point.y));
         }
         adjPoints.clear();

      }

   }

}
