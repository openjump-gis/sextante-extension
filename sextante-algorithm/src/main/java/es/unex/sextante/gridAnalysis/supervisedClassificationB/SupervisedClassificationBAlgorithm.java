package es.unex.sextante.gridAnalysis.supervisedClassificationB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.IRecordsetIterator;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.parameters.RasterLayerAndBand;

public class SupervisedClassificationBAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT                 = "INPUT";
   public static final String METHOD                = "METHOD";
   public static final String CLASSIFICATION        = "CLASSIFICATION";
   public static final String CLASSES               = "CLASSES";
   public static final String TABLE                 = "TABLE";

   public static final int    METHOD_PARALELLPIPED  = 0;
   public static final int    METHOD_MIN_DISTANCE   = 1;
   public static final int    METHOD_MAX_LIKELIHOOD = 2;

   private IRasterLayer[]     m_Window;
   private IRasterLayer       m_Output;
   private ArrayList          m_Bands;
   private HashMap            m_Classes;
   private int[]              m_iBands;


   @Override
   public void defineCharacteristics() {

      final String sMethod[] = { Sextante.getText("Parallelepiped"), Sextante.getText("Minimum_distance"),
               Sextante.getText("Maximum_likelihood") };

      setName(Sextante.getText("Supervised_classification") + "(B)");
      setGroup(Sextante.getText("Raster_layer_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addMultipleInput(INPUT, Sextante.getText("Bands"), AdditionalInfoMultipleInput.DATA_TYPE_BAND, true);
         m_Parameters.addInputTable(TABLE, Sextante.getText("Classes"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         addOutputRasterLayer(CLASSIFICATION, Sextante.getText("Classification"));
         //addOutputTable(CLASSES, Sextante.getText("Classes"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      AnalysisExtent ge;

      final int iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      m_Bands = m_Parameters.getParameterValueAsArrayList(INPUT);


      if (m_Bands.size() == 0) {
         return false;
      }

      m_Classes = new HashMap();

      getClassInformation();

      if (m_Task.isCanceled()) {
         return false;
      }

      m_Output = getNewRasterLayer(CLASSIFICATION, Sextante.getText("Classification"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
      m_Output.setNoDataValue(-1);
      ge = m_Output.getWindowGridExtent();

      m_Window = new IRasterLayer[m_Bands.size()];
      m_iBands = new int[m_Bands.size()];
      for (i = 0; i < m_Window.length; i++) {
         final RasterLayerAndBand band = (RasterLayerAndBand) m_Bands.get(i);
         m_iBands[i] = band.getBand();
         m_Window[i] = band.getRasterLayer();
         m_Window[i].setWindowExtent(ge);
      }

      switch (iMethod) {
         case 0:
            doParalellpiped();
         case 1:
         default:
            doMinimumDistance();
         case 2:
            doMaximumLikelihood();
      }

      return !m_Task.isCanceled();

   }


   private void getClassInformation() throws GeoAlgorithmExecutionException {

      try {
         final ITable table = m_Parameters.getParameterValueAsTable(TABLE);
         m_Window = new IRasterLayer[m_Bands.size()];

         final IRecordsetIterator iter = table.iterator();
         while (iter.hasNext()) {
            final IRecord record = iter.next();
            final String sClassName = record.getValue(0).toString();
            final ArrayList stats = new ArrayList();
            for (int i = 0; i < m_Window.length; i++) {
               final String sFieldName = m_Window[i].getName() + "|" + Integer.toString(m_iBands[i] + 1);
               final MeanAndStdDev masd = new MeanAndStdDev();
               boolean bMatchFound = false;
               for (int j = 1; j < table.getFieldCount(); j += 2) {
                  if (table.getFieldName(j).equals(sFieldName)) {
                     masd.mean = Double.parseDouble(record.getValue(j).toString());
                     masd.stdDev = Double.parseDouble(record.getValue(j + 1).toString());
                     bMatchFound = true;
                  }
               }
               if (!bMatchFound) {
                  throw new GeoAlgorithmExecutionException(Sextante.getText("Error_reading_table"));
               }
               stats.add(masd);
            }
            m_Classes.put(sClassName, stats);
         }
      }
      catch (final Exception e) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Error_reading_table"));
      }


   }


   private void doParalellpiped() {

      int iNX, iNY;
      int x, y;
      int iMatchingClass = 0;
      int iClass, iGrid;
      final double dMean[][] = new double[m_Classes.size()][m_Window.length];
      final double dStdDev[][] = new double[m_Classes.size()][m_Window.length];
      double dValue;
      ArrayList stats;
      MeanAndStdDev substats;
      Set set;
      Iterator iter;

      iNX = m_Output.getWindowGridExtent().getNX();
      iNY = m_Output.getWindowGridExtent().getNY();

      set = m_Classes.keySet();
      iter = set.iterator();
      iClass = 0;
      while (iter.hasNext()) {
         stats = (ArrayList) m_Classes.get(iter.next());
         for (iGrid = 0; iGrid < m_Window.length; iGrid++) {
            substats = ((MeanAndStdDev) stats.get(iGrid));
            dMean[iClass][iGrid] = substats.mean;
            dStdDev[iClass][iGrid] = substats.stdDev;
         }
         iClass++;
      }

      for (y = 0; y < iNY; y++) {
         for (x = 0; x < iNX; x++) {
            for (iClass = 0; iClass < m_Classes.size(); iClass++) {
               iMatchingClass = iClass;
               for (iGrid = 0; iGrid < m_Window.length; iGrid++) {
                  dValue = m_Window[iGrid].getCellValueAsDouble(x, y);
                  if (!m_Window[iGrid].isNoDataValue(dValue)) {
                     if (Math.abs(m_Window[iGrid].getCellValueAsDouble(x, y) - dMean[iClass][iGrid]) > dStdDev[iClass][iGrid]) {
                        iMatchingClass = -1;
                        break;
                     }
                  }
                  else {
                     break;
                  }
               }
               if (iMatchingClass != -1) {
                  break;
               }
            }
            if (iMatchingClass != -1) {
               m_Output.setCellValue(x, y, iMatchingClass + 1);
            }
            else {
               m_Output.setNoData(x, y);
            }
         }
      }

   }


   private void doMinimumDistance() {

      int iNX, iNY;
      int x, y;
      int iClass, iGrid, iMin = 0;
      final double dMean[][] = new double[m_Classes.size()][m_Window.length];
      double dMin, d, e;
      double dValue;
      ArrayList stats;
      Set set;
      Iterator iter;

      iNX = m_Output.getWindowGridExtent().getNX();
      iNY = m_Output.getWindowGridExtent().getNY();

      set = m_Classes.keySet();
      iter = set.iterator();
      iClass = 0;
      while (iter.hasNext()) {
         stats = (ArrayList) m_Classes.get(iter.next());
         for (iGrid = 0; iGrid < m_Window.length; iGrid++) {
            dMean[iClass][iGrid] = ((MeanAndStdDev) stats.get(iGrid)).mean;
         }
         iClass++;
      }

      for (y = 0; y < iNY; y++) {
         for (x = 0; x < iNX; x++) {
            for (iClass = 0, dMin = -1.0; iClass < m_Classes.size(); iClass++) {
               for (iGrid = 0, d = 0.0; iGrid < m_Window.length; iGrid++) {
                  dValue = m_Window[iGrid].getCellValueAsDouble(x, y);
                  if (!m_Window[iGrid].isNoDataValue(dValue)) {
                     e = m_Window[iGrid].getCellValueAsDouble(x, y) - dMean[iClass][iGrid];
                     d += e * e;
                     if ((dMin < 0.0) || (dMin > d)) {
                        dMin = d;
                        iMin = iClass;
                     }
                  }
                  else {
                     dMin = -1;
                  }
               }
            }

            if (dMin >= 0.0) {
               m_Output.setCellValue(x, y, iMin + 1);
            }
            else {
               m_Output.setNoData(x, y);
            }
         }
      }

   }


   private void doMaximumLikelihood() {

      int iNX, iNY;
      int x, y;
      int iClass, iGrid, iMax = 0;
      final double dMean[][] = new double[m_Classes.size()][m_Window.length];
      final double dStdDev[][] = new double[m_Classes.size()][m_Window.length];
      final double dK[][] = new double[m_Classes.size()][m_Window.length];
      double dMax, d, e;
      double dValue;
      ArrayList stats;
      MeanAndStdDev substats;
      Set set;
      Iterator iter;

      iNX = m_Output.getWindowGridExtent().getNX();
      iNY = m_Output.getWindowGridExtent().getNY();

      set = m_Classes.keySet();
      iter = set.iterator();
      iClass = 0;
      while (iter.hasNext()) {
         stats = (ArrayList) m_Classes.get(iter.next());
         for (iGrid = 0; iGrid < m_Window.length; iGrid++) {
            substats = ((MeanAndStdDev) stats.get(iGrid));
            dMean[iClass][iGrid] = substats.mean;
            dStdDev[iClass][iGrid] = substats.stdDev;
            dK[iClass][iGrid] = 1.0 / (dStdDev[iClass][iGrid] * Math.sqrt(2.0 * Math.PI));
         }
         iClass++;
      }

      for (y = 0; y < iNY; y++) {
         for (x = 0; x < iNX; x++) {
            for (iClass = 0, dMax = 0.0; iClass < m_Classes.size(); iClass++) {
               for (iGrid = 0, d = 0.0; iGrid < m_Window.length; iGrid++) {
                  dValue = m_Window[iGrid].getCellValueAsDouble(x, y);
                  if (!m_Window[iGrid].isNoDataValue(dValue)) {
                     e = (m_Window[iGrid].getCellValueAsDouble(x, y) - dMean[iClass][iGrid]) / dStdDev[iClass][iGrid];
                     e = dK[iClass][iGrid] * Math.exp(-0.5 * e * e);
                     d += e * e;
                     if (dMax < d) {
                        dMax = d;
                        iMax = iClass;
                     }
                  }
                  else {
                     dMax = -1;
                  }
               }
            }

            if (dMax > 0.0) {
               m_Output.setCellValue(x, y, iMax + 1);
            }
            else {
               m_Output.setNoData(x, y);
            }
         }
      }

   }

   private class MeanAndStdDev {

      public double mean   = 0;
      public double stdDev = 0;

   }

}
