package es.unex.sextante.gridAnalysis.ahp;

import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IRecordsetIterator;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterWrappers.GridWrapper;

public class AHPAlgorithm
         extends
            GeoAlgorithm {

   public static final String  INPUT    = "INPUT";
   public static final String  PAIRWISE = "PAIRWISE";
   public static final String  RESULT   = "RESULT";
   public static final String  STATS    = "STATS";

   private static final double RI[]     = new double[] { 0.0, 0.0, 0.58, 0.9, 1.12, 1.24, 1.32, 1.41, 1.45, 1.49, 1.51, 1.48,
            1.56, 1.57, 1.59           };

   private String              m_sNames[];
   private double              m_dWeights[];
   private ITable              m_Pairwise;
   private double              m_dPairwise[][];
   private ArrayList           layers;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Analytical_Hierarchy_Process__AHP"));
      setGroup(Sextante.getText("Raster_layer_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addMultipleInput(INPUT, Sextante.getText("Layers"), AdditionalInfoMultipleInput.DATA_TYPE_RASTER, true);
         m_Parameters.addInputTable(PAIRWISE, Sextante.getText("Pairwise_comparisons"), true);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
         addOutputText(STATS, Sextante.getText("Statistics"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int x, y;
      double dResult;
      double dValues[];
      boolean bNoDataValue;
      IRasterLayer windows[];

      layers = m_Parameters.getParameterValueAsArrayList(INPUT);
      m_Pairwise = m_Parameters.getParameterValueAsTable(PAIRWISE);
      if ((layers.size() == 0) || (layers.size() > RI.length)) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Error_wrong_number_of_layers"));
      }

      getWeights();
      final IRasterLayer result = getNewRasterLayer(RESULT, Sextante.getText("Analytical_Hierarchy_Process"),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      windows = new IRasterLayer[layers.size()];

      for (i = 0; i < layers.size(); i++) {
         windows[i] = (IRasterLayer) layers.get(i);
         windows[i].setWindowExtent(result.getWindowGridExtent());
         windows[i].setInterpolationMethod(GridWrapper.INTERPOLATION_BSpline);
      }

      final int iNX = result.getWindowGridExtent().getNX();
      final int iNY = result.getWindowGridExtent().getNY();

      dValues = new double[layers.size()];

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            bNoDataValue = false;
            for (i = 0; i < dValues.length; i++) {
               dValues[i] = windows[i].getCellValueAsDouble(x, y);
               if (windows[i].isNoDataValue(dValues[i])) {
                  bNoDataValue = true;
                  break;
               }
            }
            if (bNoDataValue) {
               result.setNoData(x, y);
            }
            else {
               dResult = 0;
               for (i = 0; i < dValues.length; i++) {
                  dResult += (dValues[i] * m_dWeights[i]);
               }
               result.setCellValue(x, y, dResult);
            }
         }
      }

      showInfo();

      return !m_Task.isCanceled();

   }


   private void getWeights() throws GeoAlgorithmExecutionException {

      int i, j;
      int iRow, iCol;
      final int iField[] = new int[layers.size()];
      boolean bMatchFound;
      IRasterLayer layer;
      String sName;
      String sFieldName;
      Object[] record;
      m_dPairwise = new double[layers.size()][layers.size()];

      m_sNames = new String[layers.size()];
      for (i = 0; i < layers.size(); i++) {
         layer = (IRasterLayer) layers.get(i);
         sName = layer.getName();
         m_sNames[i] = sName;
         bMatchFound = false;
         for (j = 0; j < m_Pairwise.getFieldCount(); j++) {
            sFieldName = m_Pairwise.getFieldName(j);
            if (sName.equals(sFieldName)) {
               bMatchFound = true;
               iField[i] = j;
               break;
            }
         }
         if (!bMatchFound) {
            throw new GeoAlgorithmExecutionException("AHP Could not Assign weights");
         }
      }

      i = 0;
      final IRecordsetIterator iter = m_Pairwise.iterator();
      while (iter.hasNext()) {
         record = iter.next().getValues();
         for (j = 0; j < iField.length; j++) {
            m_dPairwise[i][j] = new Double(record[iField[i]].toString()).doubleValue();
         }
         i++;
      }
      iter.close();

      final double[] dSum = new double[m_dPairwise.length];
      for (iCol = 0; iCol < m_dPairwise.length; iCol++) {
         for (iRow = 0; iRow < m_dPairwise.length; iRow++) {
            dSum[iCol] += m_dPairwise[iRow][iCol];
         }
      }

      for (iCol = 0; iCol < m_dPairwise.length; iCol++) {
         for (iRow = 0; iRow < m_dPairwise.length; iRow++) {
            m_dPairwise[iRow][iCol] /= dSum[iCol];
         }
      }

      m_dWeights = new double[m_dPairwise.length];
      for (iRow = 0; iRow < m_dPairwise.length; iRow++) {
         for (iCol = 0; iCol < m_dPairwise.length; iCol++) {
            m_dWeights[iRow] += m_dPairwise[iRow][iCol];
         }
         m_dWeights[iRow] /= m_dPairwise.length;
      }

   }


   private void showInfo() {

      int i, j;
      double dSum;
      //DecimalFormat df = new DecimalFormat("##.###");
      final HTMLDoc doc = new HTMLDoc();
      doc.open("AHP");
      doc.addHeader("AHP", 2);
      doc.addHeader(Sextante.getText("Analytical_Hierarchy_Process_Weights"), 3);
      doc.startUnorderedList();
      for (i = 0; i < m_dWeights.length; i++) {
         doc.addListElement(m_sNames[i] + ": " + Double.toString(m_dWeights[i]));
      }
      doc.closeUnorderedList();

      double dRelSum = 0;

      for (i = 0; i < m_dPairwise.length; i++) {
         dSum = 0;
         for (j = 0; j < m_dPairwise.length; j++) {
            dSum += m_dWeights[j] * m_dPairwise[i][j];
         }
         dRelSum += dSum / m_dWeights[i];
      }
      final double dLambda = dRelSum / m_dWeights.length;
      final double dConsistency = (dLambda - m_dWeights.length) / (m_dWeights.length - 1);
      final double dCR = dConsistency / RI[m_dWeights.length];
      if (dCR < 0.10) {
         doc.addBoldText(Sextante.getText("Warning_Inconsistent_weighting_values"));
         Sextante.addWarningToLog("AHP:" + Sextante.getText("Warning_Inconsistent_weighting_values"));
      }
      doc.close();

      addOutputText(STATS, "AHP", doc.getHTMLCode());


   }

}
