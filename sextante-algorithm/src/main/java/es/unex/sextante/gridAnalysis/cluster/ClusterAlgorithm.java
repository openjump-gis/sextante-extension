package es.unex.sextante.gridAnalysis.cluster;

import java.util.ArrayList;
import java.util.Arrays;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.parameters.RasterLayerAndBand;

public class ClusterAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULTLAYER = "RESULTLAYER";
   public static final String RESULTTABLE = "RESULTTABLE";
   public static final String INPUT       = "INPUT";
   public static final String NUMCLASS    = "NUMCLASS";

   private int                m_iNX, m_iNY;
   private int                m_iClasses;
   private int                m_iCells[];
   private int                m_iThreshold;
   private double             m_dMean[][];
   private ArrayList          m_Bands;
   private int                m_iBands[];
   private IRasterLayer       m_Windows[];
   private IRasterLayer       m_Result;


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);
      this.setIsDeterminatedProcess(false);

      setName(Sextante.getText("Unsupervised_classification__clustering"));
      setGroup(Sextante.getText("Raster_layer_analysis"));
      try {
         m_Parameters.addMultipleInput(INPUT, Sextante.getText("Bands"), AdditionalInfoMultipleInput.DATA_TYPE_BAND, true);
         m_Parameters.addNumericalValue(NUMCLASS, Sextante.getText("Number_of_classes"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 3, 2, Integer.MAX_VALUE);
         addOutputRasterLayer(RESULTLAYER, Sextante.getText("Clusters"));
         addOutputTable(RESULTTABLE, Sextante.getText("Statistics"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      RasterLayerAndBand band;

      m_Bands = m_Parameters.getParameterValueAsArrayList(INPUT);
      m_iClasses = m_Parameters.getParameterValueAsInt(NUMCLASS);
      if ((m_Bands.size() == 0) || (m_iClasses < 2)) {
         return false;
      }

      m_Result = getNewRasterLayer(RESULTLAYER, Sextante.getText("Clusters"), IRasterLayer.RASTER_DATA_TYPE_INT);

      m_Windows = new IRasterLayer[m_Bands.size()];
      m_iBands = new int[m_Bands.size()];
      for (i = 0; i < m_Bands.size(); i++) {
         band = (RasterLayerAndBand) m_Bands.get(i);
         m_iBands[i] = band.getBand();
         m_Windows[i] = band.getRasterLayer();
         m_Windows[i].setWindowExtent(m_Result.getWindowGridExtent());
         m_Windows[i].setInterpolationMethod(IRasterLayer.INTERPOLATION_BSpline);
      }

      m_dMean = new double[m_iClasses][m_Windows.length];
      m_iCells = new int[m_iClasses];

      m_iNX = m_Result.getWindowGridExtent().getNX();
      m_iNY = m_Result.getWindowGridExtent().getNY();

      if (classify()) {
         createTable();
         return true;
      }
      else {
         return false;
      }

   }


   private void createTable() throws UnsupportedOutputChannelException {

      int i, j;
      RasterLayerAndBand band;
      final String[] sFields = new String[m_Bands.size() + 2];
      final Class types[] = new Class[m_Bands.size() + 2];
      final Object[] values = new Object[m_Bands.size() + 2];

      sFields[0] = "Class";
      sFields[1] = "Count";
      types[0] = Integer.class;
      types[1] = Integer.class;
      for (i = 0; i < m_Bands.size(); i++) {
         band = (RasterLayerAndBand) m_Bands.get(i);
         sFields[i + 2] = new String(band.getRasterLayer().getName() + "[" + Integer.toString(band.getBand()) + "]");
         types[i + 2] = Double.class;

      }
      final ITable table = getNewTable(RESULTTABLE, Sextante.getText("Classification"), types, sFields);

      for (i = 0; i < m_iClasses; i++) {
         values[0] = new Integer(i);
         values[1] = new Integer(m_iCells[i]);
         for (j = 0; j < m_Bands.size(); j++) {
            values[j + 2] = new Double(m_dMean[i][j]);
         }
         table.addRecord(values);
      }

   }


   private boolean classify() {

      int i;
      int x, y;
      int iChangedCells;
      int iPrevClass;
      int iClass;
      final double dValues[] = new double[m_Windows.length];
      double dNewMean[][];
      double swap[][];

      initValues();

      dNewMean = new double[m_iClasses][m_Windows.length];

      do {
         Arrays.fill(m_iCells, 0);
         iChangedCells = 0;

         for (i = 0; i < m_iClasses; i++) {
            Arrays.fill(dNewMean[i], 0.0);
         }
         for (y = 0; y < m_iNY; y++) {
            for (x = 0; x < m_iNX; x++) {
               iPrevClass = m_Result.getCellValueAsInt(x, y);
               if (!m_Result.isNoDataValue(iPrevClass)) {
                  for (i = 0; i < m_Windows.length; i++) {
                     dValues[i] = m_Windows[i].getCellValueAsDouble(x, y, m_iBands[i]);
                  }
                  iClass = getClass(dValues);
                  m_Result.setCellValue(x, y, iClass);
                  for (i = 0; i < m_Windows.length; i++) {
                     dNewMean[iClass][i] += dValues[i];
                  }
                  m_iCells[iClass]++;
                  if (iClass != iPrevClass) {
                     iChangedCells++;
                  }
               }
            }
         }

         for (i = 0; i < m_Windows.length; i++) {
            for (int j = 0; j < m_iClasses; j++) {
               dNewMean[j][i] /= m_iCells[j];
            }
         }

         swap = m_dMean;
         m_dMean = dNewMean;
         dNewMean = swap;

         setProgressText(Sextante.getText("Modified_cells") + Integer.toString(iChangedCells));

         if (m_Task.isCanceled()) {
            return false;
         }

      }
      while (iChangedCells > m_iThreshold);

      return true;

   }


   private int getClass(final double[] dValues) {

      int iClass = 0;
      double dMinDist = Double.MAX_VALUE;
      double dDist;
      double dDif;

      for (int i = 0; i < m_iClasses; i++) {
         dDist = 0;
         for (int j = 0; j < dValues.length; j++) {
            dDif = m_dMean[i][j] - dValues[j];
            dDist += (dDif * dDif);
         }
         if (dDist < dMinDist) {
            dMinDist = dDist;
            iClass = i;
         }
      }

      return iClass;
   }


   private void initValues() {

      int i;
      int x, y;
      int iCells = 0;
      boolean bNoData;
      double dStep;
      double dValue;
      final double dMin[] = new double[m_Windows.length];
      final double dMax[] = new double[m_Windows.length];

      for (i = 0; i < m_Windows.length; i++) {
         dMin[i] = Double.MAX_VALUE;
         dMax[i] = Double.NEGATIVE_INFINITY;
      }

      for (y = 0; y < m_iNY; y++) {
         for (x = 0; x < m_iNX; x++) {
            bNoData = false;
            for (i = 0; i < m_Windows.length; i++) {
               dValue = m_Windows[i].getCellValueAsDouble(x, y, m_iBands[i]);
               if (!m_Windows[i].isNoDataValue(dValue)) {
                  dMin[i] = Math.min(dMin[i], dValue);
                  dMax[i] = Math.max(dMax[i], dValue);
               }
               else {
                  bNoData = true;
               }
            }
            if (bNoData) {
               m_Result.setNoData(x, y);
            }
            else {
               iCells++;
               m_Result.setCellValue(x, y, 0);
            }
         }
      }

      for (i = 0; i < m_Windows.length; i++) {
         dStep = (dMax[i] - dMin[i]) / ((m_iClasses + 2));
         for (int j = 0; j < m_iClasses; j++) {
            m_dMean[j][i] = dMin[i] + dStep * (j + 1);
         }
      }

      m_iThreshold = (int) (iCells * 0.02);

   }


}
