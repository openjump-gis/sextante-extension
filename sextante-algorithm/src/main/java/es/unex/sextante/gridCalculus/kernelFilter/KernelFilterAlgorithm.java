package es.unex.sextante.gridCalculus.kernelFilter;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.parameters.FixedTableModel;

public class KernelFilterAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT = "RESULT";
   public static final String KERNEL = "KERNEL";
   public static final String LAYER  = "LAYER";

   private IRasterLayer       m_Window;
   protected double           m_dValues[];
   private double             m_dNoData;
   private double             m_dCoeffs[];


   @Override
   public void defineCharacteristics() {

      final String sColumnNames[] = { "1", "2", "3" };
      setUserCanDefineAnalysisExtent(false);
      setName(Sextante.getText("User-defined_3_X_3_filter"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Layer"), true);
         m_Parameters.addFixedTable(KERNEL, Sextante.getText("Filter_kernel"), sColumnNames, 3, true);
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

      m_Window = m_Parameters.getParameterValueAsRasterLayer(LAYER);
      final FixedTableModel kernel = (FixedTableModel) m_Parameters.getParameterValueAsObject(KERNEL);
      m_Window.setFullExtent();

      final IRasterLayer result = getNewRasterLayer(RESULT, this.getName(), IRasterLayer.RASTER_DATA_TYPE_DOUBLE,
               m_Window.getLayerGridExtent());

      iNX = m_Window.getNX();
      iNY = m_Window.getNY();

      m_dValues = new double[9];
      m_dCoeffs = new double[9];
      m_dNoData = m_Window.getNoDataValue();

      result.setNoDataValue(m_dNoData);

      int i, j;
      int iCell = 0;

      for (i = 0; i < 3; i++) {
         for (j = 0; j < 3; j++) {
            try {
               m_dCoeffs[iCell] = Double.parseDouble(kernel.getValueAt(i, j).toString());
            }
            catch (final NumberFormatException e) {
               m_dCoeffs[iCell] = 1;
            }
            iCell++;
         }
      }

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            setNeighborhoodValues(x, y);
            result.setCellValue(x, y, processValues());
         }
         setProgress(y, iNY);
      }

      return !m_Task.isCanceled();

   }


   private void setNeighborhoodValues(final int iX,
                                      final int iY) {

      int x, y;
      int iCell = 0;

      for (y = -1; y < 2; y++) {
         for (x = -1; x < 2; x++) {
            m_dValues[iCell] = m_Window.getCellValueAsDouble(iX + x, iY + y);
            iCell++;
         }
      }

   }


   protected double processValues() {

      int i = 0;
      double dResult = 0;

      for (i = 0; i < m_dValues.length; i++) {
         if (m_dValues[i] != m_dNoData) {
            dResult += (m_dCoeffs[i] * m_dValues[i]);
         }
         else {
            return m_dNoData;
         }
      }

      return dResult;

   }


}
