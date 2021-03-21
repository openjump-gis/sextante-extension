package es.unex.sextante.gridCategorical.crossClassification;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class CrossClassificationAlgorithm
         extends
            GeoAlgorithm {

   public static final String KAPPA      = "KAPPA";
   public static final String CROSSCLASS = "CROSCLASS";
   public static final String GRID       = "GRID";
   public static final String GRID2      = "GRID2";
   public static final String TABLE      = "TABLE";

   int                        m_iNX, m_iNY;
   HashMap                    m_Map;
   ArrayList                  m_List;
   IRasterLayer               m_Window, m_Window2;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Cross_checking_Kappa_index"));
      setGroup(Sextante.getText("Raster_categories_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(GRID, Sextante.getText("Grid_1"), true);
         m_Parameters.addInputRasterLayer(GRID2, Sextante.getText("Grid_2"), true);
         addOutputTable(TABLE, Sextante.getText("Cross_classification"));
         addOutputRasterLayer(CROSSCLASS, Sextante.getText("Cross_classification"));
         addOutputText(KAPPA, Sextante.getText("Cross_classification"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i, j;
      int x, y;
      int iValueCount;
      int iCellValue, iCellValue2;
      int iClass, iClass2;
      int iValidCells = 0;

      m_Window = m_Parameters.getParameterValueAsRasterLayer(GRID);
      m_Window2 = m_Parameters.getParameterValueAsRasterLayer(GRID2);

      final IRasterLayer result = getNewRasterLayer(CROSSCLASS, m_Window.getName() + " + " + m_Window2.getName(),
               IRasterLayer.RASTER_DATA_TYPE_INT);

      m_Window.setWindowExtent(result.getWindowGridExtent());
      m_Window.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);
      m_Window2.setWindowExtent(result.getWindowGridExtent());
      m_Window2.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

      m_iNX = m_Window.getNX();
      m_iNY = m_Window.getNY();

      createMap();
      iValueCount = m_Map.size();

      final int[][] table = new int[iValueCount][iValueCount];
      final int iSum[] = new int[iValueCount];
      final int iSum2[] = new int[iValueCount];

      for (i = 0; i < iValueCount; i++) {
         iSum[i] = 0;
         iSum2[i] = 0;
         for (j = 0; j < iValueCount; j++) {
            table[i][j] = 0;
         }
      }

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            iCellValue = m_Window.getCellValueAsInt(x, y);
            iCellValue2 = m_Window2.getCellValueAsInt(x, y);
            if (!m_Window.isNoDataValue(iCellValue) && !m_Window2.isNoDataValue(iCellValue2)) {
               iClass = ((Integer) m_Map.get(new Integer(iCellValue))).intValue();
               iClass2 = ((Integer) m_Map.get(new Integer(iCellValue2))).intValue();
               result.setCellValue(x, y, iClass * iValueCount + iClass2);
               table[iClass][iClass2]++;
               iValidCells++;
            }
            else {
               result.setNoData(x, y);
            }
         }

      }

      if (m_Task.isCanceled()) {
         return false;
      }

      for (i = 0; i < iValueCount; i++) {
         for (j = 0; j < iValueCount; j++) {
            iSum[i] += table[i][j];
            iSum2[j] += table[i][j];
         }
      }

      double dP0 = 0, dPc = 0, dPi, dPi2, dPi3;;
      final double dPartialKappa[] = new double[iValueCount];

      for (i = 0; i < iValueCount; i++) {
         dP0 += table[i][i];
         dPc += (iSum[i] * iSum2[i]);
         dPi = table[i][i] / (double) iValidCells;
         dPi2 = iSum[i] / (double) iValidCells;
         dPi3 = iSum2[i] / (double) iValidCells;
         dPartialKappa[i] = (dPi - dPi2 * dPi3) / (dPi2 - dPi2 * dPi3);
      }
      dP0 /= iValidCells;
      dPc /= (iValidCells * iValidCells);

      final double dKappa = (dP0 - dPc) / (1 - dPc);

      final DecimalFormat df = new DecimalFormat("##.##");
      final HTMLDoc doc = new HTMLDoc();
      doc.open(Sextante.getText("Index_of_agreement"));
      doc.addHeader(Sextante.getText("Index_of_agreement"), 2);
      doc.startUnorderedList();
      doc.addListElement(Sextante.getText("Global_kappa") + ": " + df.format(dKappa));
      for (i = 0; i < iValueCount; i++) {
         doc.addListElement(Sextante.getText("Kappa_for_class") + ": " + Integer.toString(i) + ": " + df.format(dPartialKappa[i]));
      }
      doc.closeUnorderedList();
      doc.close();

      addOutputText(KAPPA, Sextante.getText("Index_of_agreement") + "[" + m_Window.getName() + "-" + m_Window2.getName() + "]",
               doc.getHTMLCode());

      final String sTableName = Sextante.getText("Cross_checking");
      final String sFields[] = new String[iValueCount];
      final Class types[] = new Class[iValueCount];
      for (i = 0; i < iValueCount; i++) {
         sFields[i] = (String) m_List.get(i);
         types[i] = Double.class;
      }
      final ITable outputTable = getNewTable(TABLE, sTableName, types, sFields);
      final Object[] values = new Object[iValueCount];
      for (i = 0; i < iValueCount; i++) {
         for (j = 0; j < iValueCount; j++) {
            values[j] = new Double(table[i][j]);
         }
         outputTable.addRecord(values);
      }

      return !m_Task.isCanceled();

   }


   private void createMap() {

      int iCellValue;
      int x, y;
      m_Map = new HashMap();
      m_List = new ArrayList();
      Integer iClass, iID;

      for (y = 0; y < m_iNY; y++) {
         for (x = 0; x < m_iNX; x++) {
            iCellValue = m_Window.getCellValueAsInt(x, y);
            if (!m_Window.isNoDataValue(iCellValue)) {
               iClass = new Integer(iCellValue);
               if (!m_Map.containsKey(iClass)) {
                  iID = new Integer(m_Map.size());
                  m_Map.put(iClass, iID);
                  m_List.add(iClass.toString());
               }
            }
            iCellValue = m_Window2.getCellValueAsInt(x, y);
            if (!m_Window2.isNoDataValue(iCellValue)) {
               iClass = new Integer(iCellValue);
               if (!m_Map.containsKey(iClass)) {
                  iID = new Integer(m_Map.size());
                  m_Map.put(iClass, iID);
                  m_List.add(iClass.toString());
               }
            }
         }
      }


   }

}
