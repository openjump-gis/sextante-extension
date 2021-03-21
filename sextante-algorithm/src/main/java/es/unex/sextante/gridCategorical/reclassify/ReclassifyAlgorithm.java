package es.unex.sextante.gridCategorical.reclassify;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.parameters.FixedTableModel;


public class ReclassifyAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT                      = "INPUT";
   public static final String METHOD                     = "METHOD";
   public static final String RECLASS                    = "RECLASS";
   public static final String LUT                        = "LUT";

   public static final int    METHOD_LOWER_THAN          = 0;
   public static final int    METHOD_LOWER_THAN_OR_EQUAL = 1;


   @Override
   public void defineCharacteristics() {

      final String[] sColumnNames = { Sextante.getText("Min_value"), Sextante.getText("Max_value"), Sextante.getText("New_value") };
      final String[] sMethod = { "Min < x < Max", "Min < x <= Max" };

      this.setName(Sextante.getText("Reclassify"));
      setGroup(Sextante.getText("Reclassify_raster_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer_to_reclassify"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         m_Parameters.addFixedTable(LUT, Sextante.getText("Look-up_table"), sColumnNames, 1, false);
         addOutputRasterLayer(RECLASS, Sextante.getText("Reclassify"));
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
      int iMethod;
      double dCellValue;
      double dMinValue, dMaxValue, dNewValue;
      boolean bClassGroupNotFound = false;
      IRasterLayer grid;

      grid = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      iMethod = m_Parameters.getParameterValueAsInt(METHOD);

      final IRasterLayer result = getNewRasterLayer(RECLASS, grid.getName() + Sextante.getText("[reclassified]"),
               IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      final FixedTableModel lut = (FixedTableModel) m_Parameters.getParameterValueAsObject(LUT);

      grid.setWindowExtent(result.getWindowGridExtent());
      grid.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

      iNX = grid.getNX();
      iNY = grid.getNY();

      if (iMethod == 0) {
         for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
            for (x = 0; x < iNX; x++) {
               dCellValue = grid.getCellValueAsDouble(x, y);
               for (i = 0; i < lut.getRowCount(); i++) {
                  dMinValue = Double.parseDouble(lut.getValueAt(i, 0).toString());
                  if (dCellValue > dMinValue) {
                     dMaxValue = Double.parseDouble(lut.getValueAt(i, 1).toString());
                     if (dCellValue < dMaxValue) {
                        dNewValue = Double.parseDouble(lut.getValueAt(i, 2).toString());
                        result.setCellValue(x, y, dNewValue);
                        break;
                     }
                  }
               }
               if (i >= lut.getRowCount()) {
                  result.setCellValue(x, y, dCellValue);
                  bClassGroupNotFound = true;
               }
            }
         }
      }
      else {
         for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
            for (x = 0; x < iNX; x++) {
               dCellValue = grid.getCellValueAsDouble(x, y);
               for (i = 0; i < lut.getRowCount(); i++) {
                  dMinValue = Double.parseDouble(lut.getValueAt(i, 0).toString());
                  if (dCellValue > dMinValue) {
                     dMaxValue = Double.parseDouble(lut.getValueAt(i, 1).toString());
                     if (dCellValue <= dMaxValue) {
                        dNewValue = Double.parseDouble(lut.getValueAt(i, 2).toString());
                        result.setCellValue(x, y, dNewValue);
                        break;
                     }
                  }
               }
               if (i >= lut.getRowCount()) {
                  result.setCellValue(x, y, dCellValue);
                  bClassGroupNotFound = true;
               }
            }
            setProgress(y, iNY);
         }
      }

      if (bClassGroupNotFound) {
         Sextante.addWarningToLog(Sextante.getText("ClassGroupNotFound"));
      }
      return !m_Task.isCanceled();

   }

}
