/*******************************************************************************
TabulateAreaAlgorithm.java
Copyright (C) 2009 ETC-LUSI http://etc-lusi.eionet.europa.eu/
 *******************************************************************************/
package es.unex.sextante.gridCategorical.tabulateArea;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

/**
 * 
 * @author Cesar Martinez Izquierdo
 */
public class TabulateAreaAlgorithm
         extends
            GeoAlgorithm {

   public static final String GRID          = "GRID";
   public static final String GRID2         = "GRID2";
   public static final String TABLE         = "TABLE";
   public static final String COMPACT       = "COMPACT";

   private int                m_iNX, m_iNY;
   private IRasterLayer       m_Window, m_Window2;

   private int                numAreas;                                               // number of areas (or tiles) that the input raster are divided in to process

   // for logging purposes:
   private int                absStep;
   private int                normStep;
   private final String       LOGGING_TEXT1 = Sextante.getText("Tabulating subzones");
   private final String       LOGGING_TEXT2 = Sextante.getText("Merging results");

   private final int          MAXTILESIZE   = 8388608;                                // max number of pixels per subArea (it will define the tile size we'll use)


   //	private final int MAXTILESIZE = 4194304; // max number of pixels per subArea (it will define the tile size we'll use)
   //	private final int MAXTILESIZE = 1048576; // max number of pixels per subArea (it will define the tile size we'll use)


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Tabulate_Area"));
      setGroup(Sextante.getText("Raster_categories_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(GRID, Sextante.getText("Zones_Grid"), true);
         m_Parameters.addInputRasterLayer(GRID2, Sextante.getText("Values_Grid"), true);
         m_Parameters.addBoolean(COMPACT, Sextante.getText("Compact_output"), false);
         addOutputTable(TABLE, Sextante.getText("Tabulate_Area"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {
      final long time1 = System.currentTimeMillis();
      m_Window = m_Parameters.getParameterValueAsRasterLayer(GRID);
      m_Window2 = m_Parameters.getParameterValueAsRasterLayer(GRID2);
      final boolean useCompactOutput = m_Parameters.getParameterValueAsBoolean(COMPACT);

      m_Window.setWindowExtent(m_AnalysisExtent);
      m_Window.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);
      m_Window2.setWindowExtent(m_AnalysisExtent);
      m_Window2.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

      m_iNX = m_Window.getNX();
      m_iNY = m_Window.getNY();
      final double cellArea = m_AnalysisExtent.getCellSize() * m_AnalysisExtent.getCellSize();

      // compute the maximum number of rows per subarea
      int maxRowPerArea;
      if (MAXTILESIZE > m_iNX) {
         maxRowPerArea = (int) Math.floor(MAXTILESIZE / m_iNX);
      }
      else {
         maxRowPerArea = 1;
      }
      numAreas = (int) Math.ceil((float) m_iNY / (float) maxRowPerArea);

      TabulateSubArea subArea = new TabulateSubArea(this, m_Window, m_Window2);

      // process all the sub-areas
      int currY = 0;
      SubAreaResult[] result = new SubAreaResult[numAreas];
      System.out.println("Num Areas: " + numAreas);
      for (int curArea = 0; curArea < numAreas; curArea++) {
         final int maxY = Math.min((currY + maxRowPerArea - 1), m_iNY - 1);
         result[curArea] = subArea.processArea(0, currY, m_iNX - 1, maxY);
         setProgress(LOGGING_TEXT1);
         System.gc();

         if ((result[curArea] != null) && !result[curArea].isSuccessful()) {
            for (final SubAreaResult element : result) {
               try {
                  if (result[curArea].getReader() != null) {
                     result[curArea].getReader().close();
                  }
               }
               catch (final IOException e) {}
            }
            return false;
         }
         currY += maxRowPerArea;
      }
      subArea = null;
      System.gc();

      // we'll assign a global ID for all the zones
      // first, we order the zones
      final HashSet<Object> globalZonesSet = new HashSet<Object>();
      for (int curArea = 0; curArea < numAreas; curArea++) {
         if (result[curArea] == null) {
            continue;
         }
         final Map<Object, Integer> currZonesMap = result[curArea].getZones();
         final Iterator<Object> it = currZonesMap.keySet().iterator();
         while (it.hasNext()) {
            final Object o = it.next();
            if (!globalZonesSet.contains(o)) {
               globalZonesSet.add(o);
            }
         }
      }

      // according to my tests, this is quite faster than using Collections.sort or a TreeMap
      Object[] m_ArrayZones = globalZonesSet.toArray(new Object[globalZonesSet.size()]);
      Arrays.sort(m_ArrayZones);
      final HashMap<Object, Integer> globalZonesMap = new HashMap<Object, Integer>();
      for (int i = 0; i < m_ArrayZones.length; i++) {
         globalZonesMap.put(m_ArrayZones[i], new Integer(i + 1));
      }
      m_ArrayZones = null;

      Sextante.addInfoToLog("Total zones: " + globalZonesMap.size());
      setProgressText(LOGGING_TEXT2);
      setProgress(51, 100);


      // create final output table
      final String sTableName = Sextante.getText("Tabulate_Area");

      String sFields[];
      Class types[];

      if (useCompactOutput) {
         sFields = new String[3]; // value, zone, area
         types = new Class[3];
         sFields[0] = "VALUE";
         types[0] = Integer.class;
         sFields[1] = "ZONE";
         types[1] = Integer.class;
         sFields[2] = "AREA";
         types[2] = Integer.class;
      }
      else {
         sFields = new String[globalZonesMap.size() + 1];
         types = new Class[globalZonesMap.size() + 1];
         sFields[0] = "VALUE";
         types[0] = Integer.class;
         final Iterator<Object> it = globalZonesMap.keySet().iterator();
         while (it.hasNext()) {
            final Object zone = it.next();
            final int idx = globalZonesMap.get(zone);
            sFields[idx] = zone.toString();
            types[idx] = zone.getClass(); // because different drivers might return different classes: Long, Integer...
         }
      }

      final ITable outputTable = getNewTable(TABLE, sTableName, types, sFields);

      // Merge all the sub-area results in a single table.
      // The general strategy here is:
      //   - there are several on-disk tables with partial results
      //   - each table contains one record per value, and the records are ordered by value
      //   - each record contains the value as the first fields, and then one field per zone,
      //       which contains the count of the current value in this zone
      //   - we will read one record from every table, and store the next available values in a TreeSet ("nextValues")
      //   - then we will calculate the total sum for a value (summing the partial results for the value)
      //   - after a value is calculated, it is deleted from the "nextValues" TreeSet and the result is written to the final output table
      //   - when there are no values available in "nextValues" TreeSet, the algorithm finishes
      try {
         final TreeSet nextValues = new TreeSet();
         for (int curArea = 0; curArea < numAreas; curArea++) {
            if (result[curArea] == null) {
               continue;
            }
            if (result[curArea].getReader().hasNext()) {
               final IRecord next = result[curArea].getReader().getNext();
               if (next != null) {
                  nextValues.add(next.getValue(0));
               }
            }
         }
         Object[] row; // this will contain the merged row
         final int zoneCount = 0;
         while (nextValues.size() > 0) {
            row = new Object[globalZonesMap.size() + 1];
            final Object curValue = nextValues.first();
            row[0] = curValue;
            for (int k = 1; k < row.length; k++) {
               row[k] = 0;
            }
            for (int curArea = 0; curArea < numAreas; curArea++) {
               if (result[curArea] == null) {
                  continue;
               }
               IRecord cur = result[curArea].getReader().getCurrent();
               if ((cur != null) && cur.getValue(0).equals(curValue)) {
                  final Map<Object, Integer> zonesMap = result[curArea].getZones();
                  final Iterator zoneIt = zonesMap.keySet().iterator();
                  while (zoneIt.hasNext()) {
                     final Object theZone = zoneIt.next();
                     final int globalIndex = globalZonesMap.get(theZone);
                     final int localIndex = zonesMap.get(theZone);
                     row[globalIndex] = new Long(((Number) row[globalIndex]).longValue()
                                                 + ((Number) cur.getValue(localIndex + 1)).longValue());
                  }
                  if (result[curArea].getReader().hasNext()) {
                     cur = result[curArea].getReader().getNext();
                     if (cur != null) {
                        nextValues.add(cur.getValue(0));
                     }
                  }
                  else {
                     // The reader has reached the end of the file,
                     // so we can safely remove this result to speed up the process
                     result[curArea] = null;
                     setProgress(LOGGING_TEXT2);
                  }
               }
            }
            nextValues.remove(curValue); // this zone has been merged
            if (useCompactOutput) {
               final Object[] tmpRow = new Object[3];
               final Iterator<Object> iter = globalZonesMap.keySet().iterator();
               while (iter.hasNext()) {
                  final Object zone = iter.next();
                  final int globalIdx = globalZonesMap.get(zone);
                  final int value = ((Number) row[globalIdx]).intValue();
                  if (value > 0) {
                     tmpRow[0] = row[0];
                     tmpRow[1] = zone;
                     final double areaValue = ((Number) row[globalIdx]).longValue() * cellArea;
                     tmpRow[2] = new Long(Math.round(areaValue));
                     outputTable.addRecord(tmpRow);
                  }
               }
            }
            else {
               for (int i = 1; i < row.length; i++) {
                  final double areaValue = ((Number) row[i]).longValue() * cellArea;
                  row[i] = new Long(Math.round(areaValue));
               }
               outputTable.addRecord(row);
            }

         }
         final long time2 = System.currentTimeMillis();
         setProgress(100, 100);
         result = null;
         System.gc();
         System.out.println("Processing time: " + ((time2 - time1) / 1000.0) + "secs.");
         Sextante.addInfoToLog(Sextante.getText("Processing_time") + ((time2 - time1) / 1000.0) + "secs.");
         System.out.println("Cell size: " + m_AnalysisExtent.getCellSize());
         // not necessary, it is automatically performed by Sextante // outputTable.postProcess();
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
         return false;
      }

      return !m_Task.isCanceled();
   }


   protected void setProgress(final String text) {
      // to avoid redundant logging
      absStep++;
      final int curStep = absStep * 100 / (2 * numAreas);
      if (curStep > normStep) {
         normStep = curStep;
         setProgressText(text);
         setProgress(normStep, 100);
      }
   }


   protected boolean isCancelled() {
      return m_Task.isCanceled();
   }


   protected OutputFactory getOuputFactory() {
      return m_OutputFactory;
   }

}
