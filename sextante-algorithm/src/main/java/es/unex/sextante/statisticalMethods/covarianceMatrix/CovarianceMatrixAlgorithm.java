package es.unex.sextante.statisticalMethods.covarianceMatrix;

import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class CovarianceMatrixAlgorithm
         extends
            GeoAlgorithm {

   public static final String COVARIANCES = "COVARIANCES";
   public static final String INPUT       = "INPUT";

   private static double      NODATA      = -9999999.;

   private ArrayList          m_RasterLayers;
   private IRasterLayer[]     m_Windows;
   private int                m_iNX, m_iNY;
   private double             m_dMean[];


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Statistical_methods"));
      setName(Sextante.getText("Covariance_matrix"));

      try {
         m_Parameters.addMultipleInput(INPUT, Sextante.getText("Input_layers"), AdditionalInfoMultipleInput.DATA_TYPE_RASTER,
                  true);
         addOutputTable(COVARIANCES, Sextante.getText("Covariance_matrix"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i, j;

      m_RasterLayers = m_Parameters.getParameterValueAsArrayList(INPUT);

      if (m_RasterLayers.size() == 0) {
         return false;
      }

      final Object[] values = new Object[m_RasterLayers.size()];
      final String sFields[] = new String[m_RasterLayers.size()];
      final Class iTypes[] = new Class[m_RasterLayers.size()];
      final String sTableName = "Matriz de covarianzas";
      final double dCovar[][] = new double[m_RasterLayers.size()][m_RasterLayers.size()];
      m_dMean = new double[m_RasterLayers.size()];
      this.adjustOutputExtent();

      m_Windows = new IRasterLayer[m_RasterLayers.size()];

      for (i = 0; i < m_RasterLayers.size(); i++) {
         m_Windows[i] = (IRasterLayer) m_RasterLayers.get(i);
         m_Windows[i].setWindowExtent(this.getAnalysisExtent());
         m_Windows[i].setInterpolationMethod(IRasterLayer.INTERPOLATION_BSpline);
         sFields[i] = m_Windows[i].getName();
         iTypes[i] = Double.class;
         m_dMean[i] = m_Windows[i].getMeanValue();
      }

      final ITable table = getNewTable(COVARIANCES, sTableName, iTypes, sFields);

      m_iNX = getAnalysisExtent().getNX();
      m_iNY = getAnalysisExtent().getNY();

      final int iTotal = (int) (m_RasterLayers.size() * m_RasterLayers.size() / 2.);
      int iCount = 0;
      for (i = 0; (i < m_RasterLayers.size() - 1) && setProgress(iCount, iTotal); i++) {
         dCovar[i][i] = 1.0;
         iCount++;
         for (j = i + 1; j < m_RasterLayers.size(); j++) {
            dCovar[i][j] = dCovar[j][i] = getCovar(i, j);
            iCount++;
         }
      }

      for (i = 0; i < m_RasterLayers.size(); i++) {
         for (j = 0; j < m_RasterLayers.size(); j++) {
            values[j] = new Double(dCovar[i][j]);
         }
         table.addRecord(values);
      }

      return !m_Task.isCanceled();

   }


   private double getCovar(final int i,
                           final int j) {

      int x, y;
      int iValues = 0;
      double dValuei, dValuej;
      double dSum = 0;

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            dValuei = m_Windows[i].getCellValueAsDouble(x, y);
            dValuej = m_Windows[j].getCellValueAsDouble(x, y);
            if (!m_Windows[i].isNoDataValue(dValuei) && !m_Windows[j].isNoDataValue(dValuej)) {
               dSum += (dValuei - m_dMean[i]) * (dValuej - m_dMean[j]);
               iValues++;
            }
         }
         if (iValues > 1) {
            return dSum / (iValues - 1);
         }
         else {
            return NODATA;
         }
      }

      return NODATA;

   }
}
