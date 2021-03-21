

package es.unex.sextante.gridStatistics.semivariances;

import java.util.Arrays;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;


public class SemivariancesAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT  = "RESULT";
   public static final String LAYER   = "LAYER";
   public static final String MAXDIST = "MAXDIST";

   private int                m_iNX, m_iNY;
   private int                m_iMaxDist;
   private double             m_dSemivarHorz[];
   private double             m_dSemivarVert[];
   private int                m_iValidCellsHorz[];
   private int                m_iValidCellsVert[];
   private IRasterLayer       m_Layer;


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(false);
      setName(Sextante.getText("Semivariances__raster"));
      setGroup(Sextante.getText("Geostatistics"));

      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Layer"), true);
         m_Parameters.addNumericalValue(MAXDIST, Sextante.getText("Maximum_distance__pixels"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 20, 1, Integer.MAX_VALUE);
         addOutputRasterLayer(RESULT, Sextante.getText("Semivariances"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;

      m_iMaxDist = m_Parameters.getParameterValueAsInt(MAXDIST);
      m_Layer = m_Parameters.getParameterValueAsRasterLayer(LAYER);

      m_Layer.setFullExtent();

      m_iNX = m_Layer.getNX();
      m_iNY = m_Layer.getNY();

      m_iMaxDist = Math.min(m_iMaxDist, m_iNX);
      m_iMaxDist = Math.min(m_iMaxDist, m_iNY);

      m_dSemivarHorz = new double[m_iMaxDist];
      m_dSemivarVert = new double[m_iMaxDist];
      m_iValidCellsHorz = new int[m_iMaxDist];
      m_iValidCellsVert = new int[m_iMaxDist];

      Arrays.fill(m_dSemivarHorz, 0.0);
      Arrays.fill(m_dSemivarVert, 0.0);
      Arrays.fill(m_iValidCellsHorz, 0);
      Arrays.fill(m_iValidCellsVert, 0);

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            calculateSemivariances(x, y);
         }
      }

      if (!m_Task.isCanceled()) {
         postProcessValues();
         createTable();
      }
      return !m_Task.isCanceled();

   }


   private void createTable() throws UnsupportedOutputChannelException {

      int i;
      final String[] sFields = { Sextante.getText("Distance"), Sextante.getText("Semivar__Horz"),
               Sextante.getText("Semivar__Vert") };
      final Class types[] = { Double.class, Double.class, Double.class };
      final String sTableName = Sextante.getText("Semivariances[") + m_Layer.getName() + "]";
      final Object[] values = new Object[3];
      final ITable driver = getNewTable(RESULT, sTableName, types, sFields);

      for (i = 0; i < m_iMaxDist; i++) {
         values[0] = new Double(m_Layer.getWindowCellSize() * i);
         values[1] = new Double(m_dSemivarHorz[i]);
         values[2] = new Double(m_dSemivarVert[i]);
         driver.addRecord(values);
      }

   }


   private void postProcessValues() {

      for (int i = 0; i < m_iMaxDist; i++) {
         if (m_iValidCellsHorz[i] != 0) {
            m_dSemivarHorz[i] /= (2. * m_iValidCellsHorz[i]);
         }
         if (m_iValidCellsVert[i] != 0) {
            m_dSemivarVert[i] /= (2. * m_iValidCellsVert[i]);
         }
      }

   }


   private void calculateSemivariances(final int iInitX,
                                       final int iInitY) {

      int iDist;
      int x, y;
      int x1, x2, y1, y2;
      double dValue;
      final double dCenterValue = m_Layer.getCellValueAsDouble(iInitX, iInitY);

      if (m_Layer.isNoDataValue(dCenterValue)) {
         return;
      }

      x1 = Math.max(iInitX - m_iMaxDist, 0);
      x2 = Math.min(iInitX + m_iMaxDist, m_iNX - 1);
      y1 = Math.max(iInitY - m_iMaxDist, 0);
      y2 = Math.min(iInitY + m_iMaxDist, m_iNY - 1);

      for (x = x1; x < x2 + 1; x++) {
         iDist = Math.abs(x - iInitX) - 1;
         if (iDist > 0) {
            dValue = m_Layer.getCellValueAsDouble(x, iInitY);
            if (!m_Layer.isNoDataValue(dValue)) {
               m_dSemivarHorz[iDist] += Math.pow(dValue - dCenterValue, 2);
               m_iValidCellsHorz[iDist]++;
            }
         }
      }

      for (y = y1; y < y2 + 1; y++) {
         iDist = Math.abs(y - iInitY) - 1;
         if (iDist > 0) {
            dValue = m_Layer.getCellValueAsDouble(iInitX, y);
            if (!m_Layer.isNoDataValue(dValue)) {
               m_dSemivarVert[iDist] += Math.pow(dValue - dCenterValue, 2);
               m_iValidCellsVert[iDist]++;
            }
         }
      }

   }


}
