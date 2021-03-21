package es.unex.sextante.gridTools.sortRaster;

import java.util.Arrays;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterWrappers.GridCell;

public class SortRasterAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT  = "INPUT";
   public static final String RESULT = "RESULT";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Sort"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);
      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         addOutputRasterLayer(RESULT, Sextante.getText("Sorted_raster_layer"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iCells;
      GridCell cell;

      final IRasterLayer layer = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      layer.setFullExtent();
      final GridCell[] cells = getSortedArrayOfCells(layer);
      final AnalysisExtent gridExtent = new AnalysisExtent(layer);
      final IRasterLayer sorted = getNewRasterLayer(RESULT, layer.getName() + Sextante.getText("[sorted]"),
               IRasterLayer.RASTER_DATA_TYPE_INT, gridExtent);

      iCells = layer.getNX() * layer.getNY();

      for (i = 0; (i < iCells) && setProgress(i, iCells); i++) {
         cell = cells[i];
         if (!layer.isNoDataValue(cell.getValue())) {
            sorted.setCellValue(cell.getX(), cell.getY(), i);
         }
         else {
            sorted.setNoData(cell.getX(), cell.getY());
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
