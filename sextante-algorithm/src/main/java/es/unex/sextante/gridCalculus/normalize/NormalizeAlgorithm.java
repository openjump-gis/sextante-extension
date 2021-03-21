package es.unex.sextante.gridCalculus.normalize;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class NormalizeAlgorithm
         extends
            GeoAlgorithm {

   public static final String GRID               = "GRID";
   public static final String METHOD             = "METHOD";
   public static final String RESULT             = "RESULT";

   public static final int    METHOD_NORMALIZE   = 0;
   public static final int    METHOD_FROM_0_TO_1 = 1;

   private int                m_iNX, m_iNY;
   private int                m_iMethod;

   private IRasterLayer       m_Grid             = null;
   private IRasterLayer       m_NormalizedGrid;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_Grid = m_Parameters.getParameterValueAsRasterLayer(GRID);
      m_iMethod = m_Parameters.getParameterValueAsInt(METHOD);

      m_NormalizedGrid = getNewRasterLayer(RESULT, m_Grid.getName() + Sextante.getText("[normalized]"),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      m_Grid.setWindowExtent(getAnalysisExtent());

      m_iNX = m_Grid.getNX();
      m_iNY = m_Grid.getNY();

      return normalize();

   }


   @Override
   public void defineCharacteristics() {

      final String sMethod[] = { Sextante.getText("Standard_deviation"), "0 < x < 1" };

      setName(Sextante.getText("Normalize"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(GRID, Sextante.getText("Layer_to_normalize"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         addOutputRasterLayer(RESULT, Sextante.getText("Normalized"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private boolean normalize() throws GeoAlgorithmExecutionException {

      int x, y;
      double z;
      double dValue1 = 0, dValue2 = 0;

      switch (m_iMethod) {
         case 0:
            dValue1 = m_Grid.getMeanValue();
            dValue2 = Math.sqrt(m_Grid.getVariance());
            break;
         case 1:
         default:
            dValue1 = m_Grid.getMinValue();
            dValue2 = m_Grid.getMaxValue() - dValue1;
            break;
      }

      if (dValue2 == 0) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Cannot normalize layer. Wrong values"));
      }

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            z = m_Grid.getCellValueAsDouble(x, y);
            if (!m_Grid.isNoDataValue(z)) {
               m_NormalizedGrid.setCellValue(x, y, (z - dValue1) / dValue2);
            }
            else {
               m_NormalizedGrid.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }

}
