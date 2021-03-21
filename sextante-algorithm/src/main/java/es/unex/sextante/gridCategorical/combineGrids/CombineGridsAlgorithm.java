package es.unex.sextante.gridCategorical.combineGrids;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.parameters.FixedTableModel;

public class CombineGridsAlgorithm
         extends
            GeoAlgorithm {

   public static final String GRID   = "GRID";
   public static final String GRID2  = "GRID2";
   public static final String RESULT = "RESULT";
   public static final String LUT    = "LUT";


   @Override
   public void defineCharacteristics() {

      final String[] sColumnNames = { Sextante.getText("Valor_in_grid_1"), Sextante.getText("Valor_in_grid_2"),
               Sextante.getText("New_value") };

      setName(Sextante.getText("Combine_grids"));
      setGroup(Sextante.getText("Raster_categories_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(GRID, Sextante.getText("Grid_1"), true);
         m_Parameters.addInputRasterLayer(GRID2, Sextante.getText("Grid_2"), true);
         m_Parameters.addFixedTable(LUT, Sextante.getText("Look-up_table"), sColumnNames, 1, false);
         addOutputRasterLayer(RESULT, Sextante.getText("Grid_combination"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int x, y;
      int iNX, iNY;
      double dCellValue, dCellValue2;
      double dTableValue, dTableValue2;
      double dValue;

      final IRasterLayer grid = m_Parameters.getParameterValueAsRasterLayer(GRID);
      final IRasterLayer grid2 = m_Parameters.getParameterValueAsRasterLayer(GRID2);

      final IRasterLayer result = getNewRasterLayer(RESULT, grid.getName() + " + " + grid2.getName(),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      final FixedTableModel lut = (FixedTableModel) m_Parameters.getParameterValueAsObject(LUT);

      grid.setWindowExtent(result.getWindowGridExtent());
      grid.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);
      grid2.setWindowExtent(result.getWindowGridExtent());
      grid2.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

      iNX = grid.getNX();
      iNY = grid.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dCellValue = grid.getCellValueAsDouble(x, y);
            dCellValue2 = grid2.getCellValueAsDouble(x, y);
            for (i = 0; i < lut.getRowCount(); i++) {
               dTableValue = Double.parseDouble(lut.getValueAt(i, 0).toString());
               if (dTableValue == dCellValue) {
                  dTableValue2 = Double.parseDouble(lut.getValueAt(i, 1).toString());
                  if (dTableValue2 == dCellValue2) {
                     dValue = Double.parseDouble(lut.getValueAt(i, 2).toString());
                     result.setCellValue(x, y, dValue);
                     break;
                  }
               }
            }
            if (i >= lut.getRowCount()) {
               result.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();


   }


}
