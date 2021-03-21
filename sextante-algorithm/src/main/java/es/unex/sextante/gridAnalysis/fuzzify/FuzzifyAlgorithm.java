package es.unex.sextante.gridAnalysis.fuzzify;

import java.util.Arrays;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterWrappers.GridWrapper;

public class FuzzifyAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT                     = "INPUT";
   public static final String FUNCTIONTYPE              = "FUNCTIONTYPE";
   public static final String RESULT                    = "RESULT";
   public static final String A                         = "A";
   public static final String B                         = "B";
   public static final String C                         = "C";
   public static final String D                         = "D";

   public static final int    MEMBER_FUNCTION_LINEAL    = 0;
   public static final int    MEMBER_FUNCTION_SIGMOIDAL = 1;
   public static final int    MEMBER_FUNCTION_J_SHAPED  = 2;


   int                        m_iNX, m_iNY;
   IRasterLayer               m_Grid;
   IRasterLayer               m_Result;


   @Override
   public void defineCharacteristics() {

      final String[] sOptions = { Sextante.getText("Linear"), Sextante.getText("Sigmoidal"), Sextante.getText("J-shaped") };

      setName(Sextante.getText("Fuzzify"));
      setGroup(Sextante.getText("Fuzzy_logic"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Input_Layer"), true);
         m_Parameters.addSelection(FUNCTIONTYPE, Sextante.getText("Member_function"), sOptions);
         m_Parameters.addNumericalValue(A, Sextante.getText("Control_point_A"), 10,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(B, Sextante.getText("Control_point_B"), 10,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(C, Sextante.getText("Control_point_C"), 10,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(D, Sextante.getText("Control_point_D"), 10,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      double dX;
      double dW;
      double dValue;

      m_Grid = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      final int iType = m_Parameters.getParameterValueAsInt(FUNCTIONTYPE);
      double dA = m_Parameters.getParameterValueAsDouble(A);
      double dB = m_Parameters.getParameterValueAsDouble(B);
      double dC = m_Parameters.getParameterValueAsDouble(C);
      double dD = m_Parameters.getParameterValueAsDouble(D);

      final double dPts[] = new double[4];
      dPts[0] = dA;
      dPts[1] = dB;
      dPts[2] = dC;
      dPts[3] = dD;
      Arrays.sort(dPts);
      dA = dPts[0];
      dB = dPts[1];
      dC = dPts[2];
      dD = dPts[3];

      m_Result = getNewRasterLayer(RESULT, m_Grid.getName() + "_" + Sextante.getText("[fuzzy]"),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      final AnalysisExtent extent = m_Result.getWindowGridExtent();

      m_Grid.setWindowExtent(extent);
      m_Grid.setInterpolationMethod(GridWrapper.INTERPOLATION_BSpline);

      final int iNX = m_Grid.getNX();
      final int iNY = m_Grid.getNY();

      for (y = 0; (y < iNY) & setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dValue = m_Grid.getCellValueAsDouble(x, y);
            if (!m_Grid.isNoDataValue(dValue)) {
               if ((dValue <= dA) || (dValue >= dD)) {
                  m_Result.setCellValue(x, y, 0);
               }
               else if ((dValue >= dB) && (dValue <= dC)) {
                  m_Result.setCellValue(x, y, 1);
               }
               else {
                  if (dValue < dB) {
                     dX = dValue - dA;
                     dW = dB - dA;
                  }
                  else {
                     dX = dD - dValue;
                     dW = dD - dC;
                  }
                  switch (iType) {
                     case 0:
                        m_Result.setCellValue(x, y, dX / dW);
                        break;
                     case 1:
                        m_Result.setCellValue(x, y, Math.pow(Math.sin(dX / dW * Math.PI / 2.), 2.));
                        break;
                     case 2:
                        m_Result.setCellValue(x, y, 1. / (1 + Math.pow((dW - dX) / dW, 2.)));
                        break;
                  }
               }
            }
            else {
               m_Result.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }

}
