

package es.unex.sextante.gridTools.gridsFromTableAndGrid;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IRecordsetIterator;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;


public class GridsFromTableAndGridAlgorithm
         extends
            GeoAlgorithm {

   private double             NO_DATA;

   public static final String INPUT  = "INPUT";
   public static final String TABLE  = "TABLE";
   public static final String FIELD  = "FIELD";
   public static final String RESULT = "RESULT";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Grids_from_table_and_classified_grid"));
      setGroup(Sextante.getText("Raster_categories_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         m_Parameters.addInputTable(TABLE, Sextante.getText("Look-up_table"), true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), TABLE);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final UndefinedParentParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final OptionalParentParameterException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i, j;
      int x, y;
      int iNX, iNY;
      int iMax = 0;
      int iClass;
      int[] ValidFields;
      int iValidFields = 0;
      Object[] record;
      double[][] Values;
      IRasterLayer[] grids;
      IRasterLayer grid;

      NO_DATA = m_OutputFactory.getDefaultNoDataValue();

      grid = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      final ITable table = m_Parameters.getParameterValueAsTable(TABLE);
      final int iField = m_Parameters.getParameterValueAsInt(FIELD);

      IRecordsetIterator iter = table.iterator();
      iMax = Integer.MIN_VALUE;
      while (iter.hasNext()) {
         record = iter.next().getValues();
         try {
            iClass = new Integer(record[iField].toString()).intValue();
            if (iClass > iMax) {
               iMax = iClass;
            }
         }
         catch (final NumberFormatException e) {
            Sextante.addErrorToLog(e);
            return false;
         }
      }

      for (i = 0; i < table.getFieldCount(); i++) {
         if ((table.getFieldType(i) == Double.class) || (table.getFieldType(i) == Integer.class)
             || (table.getFieldType(i) == Float.class) || (table.getFieldType(i) == Byte.class)
             || ((table.getFieldType(i) == Long.class) && (i != iField))) {
            iValidFields++;
         }
      }

      ValidFields = new int[iValidFields];
      iValidFields = 0;

      for (i = 0; i < table.getFieldCount(); i++) {
         if ((table.getFieldType(i) == Double.class) || (table.getFieldType(i) == Integer.class)
             || (table.getFieldType(i) == Float.class) || (table.getFieldType(i) == Byte.class)
             || ((table.getFieldType(i) == Long.class) && (i != iField))) {
            ValidFields[iValidFields] = i;
            iValidFields++;
         }
      }

      if (iValidFields != 0) {

         Values = new double[iMax + 1][iValidFields];

         iter = table.iterator();
         while (iter.hasNext()) {
            record = iter.next().getValues();
            iClass = new Integer(record[iField].toString()).intValue();
            for (j = 0; j < iValidFields; j++) {
               try {
                  Values[iClass][j] = new Double(record[ValidFields[j]].toString()).doubleValue();
               }
               catch (final Exception e) {
                  Values[iClass][j] = NO_DATA;
               }

            }
         }

         grids = new IRasterLayer[iValidFields];
         for (i = 0; i < iValidFields; i++) {
            grids[i] = getNewRasterLayer(RESULT + Integer.toString(i), "[" + table.getFieldName(ValidFields[i]) + "]",
                     IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
            grids[i].setNoDataValue(NO_DATA);
         }

         grid.setWindowExtent(grids[0].getWindowGridExtent());
         grid.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

         iNX = grid.getNX();
         iNY = grid.getNY();

         for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
            for (x = 0; x < iNX; x++) {
               iClass = grid.getCellValueAsInt(x, y);
               if ((iClass > 0) && (iClass < iMax + 1)) {
                  for (i = 0; i < iValidFields; i++) {
                     grids[i].setCellValue(x, y, Values[iClass][i]);
                  }
               }
            }
         }
         return !m_Task.isCanceled();
      }
      else {
         throw new GeoAlgorithmExecutionException("No valid fields in table");
      }

   }


   @Override
   public boolean isSuitableForModelling() {

      return false;

   }


   @Override
   public boolean generatesLayers() {

      return true;

   }

}
