/*******************************************************************************
TabulateSubArea.java
Copyright (C) 2009 ETC-LUSI http://etc-lusi.eionet.europa.eu/
 *******************************************************************************/
package es.unex.sextante.gridCategorical.tabulateArea;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;

/**
 * 
 * @author Cesar Martinez Izquierdo
 */
public class TabulateSubArea {

   public static final String   GRID         = "GRID";
   public static final String   GRID2        = "GRID2";
   public static final String   TABLE        = "TABLE";


   private Map<Object, Integer> m_MapValues;
   private Map<Object, Integer> m_MapZones;
   private final IRasterLayer   m_Window, m_Window2;
   TabulateAreaAlgorithm        alg;
   private String               tmpTableName = null;


   public TabulateSubArea(final TabulateAreaAlgorithm alg,
                          final IRasterLayer zonesWindow,
                          final IRasterLayer classesWindow) {
      m_Window = zonesWindow;
      m_Window2 = classesWindow;
      this.alg = alg;
   }


   public SubAreaResult processArea(final int minX,
                                    final int minY,
                                    final int maxX,
                                    final int maxY) throws GeoAlgorithmExecutionException {
      int i, j;
      int x, y;
      int iZoneCount, iValueCount;
      int iZone, iValue;
      int iZoneIdx, iValueIdx;

      SubAreaResult result = new SubAreaResult();
      try {
         createMaps(minX, minY, maxX, maxY);
         iZoneCount = m_MapZones.size();
         iValueCount = m_MapValues.size();

         if ((iZoneCount == 0) || (iValueCount == 0)) {
            // this may happen in tiles filled with noData values
            result = null;
            return result;
         }

         final int[][] table = new int[iZoneCount][iValueCount];
         for (i = 0; i < iValueCount; i++) {
            for (j = 0; j < iZoneCount; j++) {
               table[j][i] = 0;
            }
         }

         for (y = minY; (y <= maxY) && !alg.isCancelled(); y++) {
            for (x = minX; x <= maxX; x++) {
               iZone = m_Window.getCellValueAsInt(x, y);
               iValue = m_Window2.getCellValueAsInt(x, y);
               if (!m_Window.isNoDataValue(iZone) && !m_Window2.isNoDataValue(iValue)) {
                  iZoneIdx = (m_MapZones.get(new Integer(iZone))).intValue();
                  iValueIdx = (m_MapValues.get(new Integer(iValue))).intValue();
                  table[iZoneIdx][iValueIdx]++;
               }
            }
         }

         if (alg.isCancelled()) {
            result.setSuccessful(false);
            return result;
         }

         final String sFields[] = new String[iZoneCount + 1];
         final Class types[] = new Class[iZoneCount + 1];

         sFields[0] = "VALUE";
         types[0] = Integer.class;
         final Iterator<Object> zonesIter = m_MapZones.keySet().iterator();
         while (zonesIter.hasNext()) {
            final Object obj = zonesIter.next();
            final int idx = m_MapZones.get(obj);
            sFields[idx + 1] = obj.toString();
            types[idx + 1] = Integer.class;

         }

         ITable tmpTable = createTempTable(types, sFields);
         Object[] values;
         boolean emptyRow = true;
         int value;

         // iterate using the (ordered) values Map, because we'll need to have ordered values to make possible the merging
         final Iterator<Object> valuesIter = m_MapValues.keySet().iterator();
         while (valuesIter.hasNext()) {
            final Object obj = valuesIter.next();
            final Integer index = m_MapValues.get(obj);
            values = new Object[iZoneCount + 1];
            values[0] = obj;
            for (j = 1; j <= iZoneCount; j++) {
               value = table[j - 1][index];
               if (value > 0) {
                  values[j] = new Integer(value);
                  emptyRow = false;
               }
            }
            if (!emptyRow) { // don't add empty rows, this saves output space and speeds up merging
               tmpTable.addRecord(values);
               emptyRow = true;
            }
         }

         tmpTable.postProcess();
         tmpTable.open();
         if (tmpTable.getRecordCount() == 0) {
            tmpTable.close();
            final File f = new File(tmpTableName);
            f.delete(); // FIXME: this should be automatically done by Sextante
            result = null;
         }
         else {
            final TempTableReader tmpReader = new TempTableReader(tmpTable);
            tmpReader.init();
            result.setReader(tmpReader);
            result.setSuccessful(!alg.isCancelled());
            result.setZones(m_MapZones);
         }

         // this will help garbage collector, as the same TabulateSubArea object is reused several times
         tmpTable = null;
         m_MapValues = null;
         m_MapZones = null;

         return result;
      }
      catch (final IOException e) {
         Sextante.getLogger().addError(e);
         result.setSuccessful(false);
         return result;
      }
      catch (final Exception e) {
         Sextante.getLogger().addError(e);
         result.setSuccessful(false);
         return result;
      }
   }


   private void createMaps(final int minX,
                           final int minY,
                           final int maxX,
                           final int maxY) {
      int iCellValue;
      int x, y;
      m_MapValues = new TreeMap<Object, Integer>();
      m_MapZones = new HashMap<Object, Integer>();
      Integer iClass, iID = 0;

      for (y = minY; y <= maxY; y++) {
         for (x = minX; x <= maxX; x++) {
            iCellValue = m_Window.getCellValueAsInt(x, y);
            iClass = new Integer(iCellValue);
            if (!m_Window.isNoDataValue(iCellValue)) {
               if (!m_MapZones.containsKey(iClass)) {
                  iID = new Integer(m_MapZones.size());
                  m_MapZones.put(iClass, iID);
               }
            }

            iCellValue = m_Window2.getCellValueAsInt(x, y);
            if (!m_Window2.isNoDataValue(iCellValue)) { // don't include no data values
               iClass = new Integer(iCellValue);
               if (!m_MapValues.containsKey(iClass)) {
                  iID = new Integer(m_MapValues.size());
                  m_MapValues.put(iClass, iID);
               }
            }
         }
      }
      // according to my tests, this is quite faster than using Collections.sort or a TreeMap
      // oops... but we need a Map to access by key
      //		m_ArrayZones = (Integer[]) m_ListZones.toArray(new Integer[m_ListZones.size()]);
      //		Arrays.sort(m_ArrayZones);
      //		for (int i=0; i<m_ArrayZones.length; i++) {
      //			m_MapZones.put(m_ArrayZones[i], new Integer(i));
      //		}
   }


   private ITable createTempTable(final Class[] types,
                                  final String[] sFields) throws IOException, UnsupportedOutputChannelException {
      final OutputFactory factory = alg.getOuputFactory();
      tmpTableName = factory.getTempTableFilename();
      final File f = new File(tmpTableName);
      //f.deleteOnExit(); // FIXME: this should be automatically done by Sextante
      // we don't ask for removal because this causes some memory to be used
      final IOutputChannel channel = new FileOutputChannel(tmpTableName);
      return factory.getNewTable(tmpTableName, types, sFields, channel);

   }

}
