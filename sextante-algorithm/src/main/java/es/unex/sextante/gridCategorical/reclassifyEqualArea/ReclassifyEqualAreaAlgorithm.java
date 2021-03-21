package es.unex.sextante.gridCategorical.reclassifyEqualArea;

import java.util.Arrays;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterWrappers.GridCell;

public class ReclassifyEqualAreaAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT     = "INPUT";
   public static final String CLASSAREA = "CLASSAREA";
   public static final String RECLASS   = "RECLASS";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Divide_into_n_classes_of_equal_area"));
      setGroup(Sextante.getText("Reclassify_raster_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         m_Parameters.addNumericalValue(CLASSAREA, Sextante.getText("Class_area_[cells]"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 100, 2, Integer.MAX_VALUE);
         addOutputRasterLayer(RECLASS, Sextante.getText("Reclassify"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iCell;
      int iClassArea;
      GridCell cell;

      final IRasterLayer grid = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      iClassArea = m_Parameters.getParameterValueAsInt(CLASSAREA);

      final IRasterLayer result = getNewRasterLayer(RECLASS, grid.getName() + Sextante.getText("[reclassified]"),
               IRasterLayer.RASTER_DATA_TYPE_INT);

      grid.setWindowExtent(getAnalysisExtent());

      final GridCell[] cells = getSortedArrayOfCells(grid);

      result.assignNoData();

      iCell = 0;

      for (i = 0; i < cells.length; i++) {
         cell = cells[i];
         if (!grid.isNoDataValue(cell.getValue())) {
            final int iValue = (iCell / iClassArea);
            result.setCellValue(cell.getX(), cell.getY(), iValue);
            if (!setProgress(iCell, cells.length)) {
               return false;
            }
            iCell++;
         }
         else {
            result.setNoData(cell.getX(), cell.getY());
         }
      }

      return !m_Task.isCanceled();

   }


   public GridCell[] getSortedArrayOfCells(final IRasterLayer layer) {

      int i;
      int iX, iY;
      final int iNX = layer.getNX();
      final int iCells = layer.getNX() * layer.getNY();
      GridCell[] cells;
      GridCell cell;

      cells = new GridCell[iCells];

      for (i = 0; i < iCells; i++) {
         iX = i % iNX;
         iY = i / iNX;
         cell = new GridCell(iX, iY, layer.getCellValueAsDouble(iX, iY));
         cells[i] = cell;
      }

      Arrays.sort(cells);

      return cells;

   }


}
