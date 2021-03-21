package es.unex.sextante.gridCategorical.classStatistics;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;

public class ClassStatisticsAlgorithm
         extends
            GeoAlgorithm {

   private final static int   m_iOffsetX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int   m_iOffsetY[] = { 1, 1, 0, -1, -1, -1, 0, 1 };

   public static final String RESULT       = "RESULT";

   public static final String INPUT        = "INPUT";

   int                        m_iNX, m_iNY;
   IRasterLayer               m_Grid;
   HashMap                    m_Map;
   boolean                    m_IsCellAlreadyVisited[][];


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Class_statistics"));
      setGroup(Sextante.getText("Raster_categories_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         addOutputTable(RESULT, Sextante.getText("Class_statistics"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iClass;
      double dArea;
      ClassStatistics stats = null;

      m_Grid = m_Parameters.getParameterValueAsRasterLayer(INPUT);

      m_Grid.setWindowExtent(m_AnalysisExtent);
      m_Grid.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

      m_iNX = m_Grid.getNX();
      m_iNY = m_Grid.getNY();

      m_IsCellAlreadyVisited = new boolean[m_iNX][m_iNY];
      m_Map = new HashMap();

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            if (!m_IsCellAlreadyVisited[x][y]) {
               iClass = m_Grid.getCellValueAsInt(x, y);
               dArea = getClassArea(x, y);
               stats = (ClassStatistics) m_Map.get(new Integer(iClass));
               if (stats != null) {
                  stats.add(dArea);
               }
               else {
                  stats = new ClassStatistics(iClass);
                  stats.add(dArea);
                  m_Map.put(new Integer(iClass), stats);
               }
            }
         }

      }

      if (!m_Task.isCanceled()) {
         createTable();
         return true;
      }
      else {
         return false;
      }

   }


   private void createTable() throws UnsupportedOutputChannelException {

      Object[] values;
      ClassStatistics stats;
      final Set set = m_Map.keySet();
      final Iterator iter = set.iterator();

      final String sFields[] = { Sextante.getText("Class_ID"), Sextante.getText("total_area_Total"),
               Sextante.getText("Number_of_patches"), Sextante.getText("Mean_area"), Sextante.getText("Variance_of_area"),
               Sextante.getText("Max_area"), Sextante.getText("Min_area") };
      final Class types[] = { Integer.class, Double.class, Integer.class, Double.class, Double.class, Double.class, Double.class };
      final String sTableName = Sextante.getText("Class_statistics_[") + m_Grid.getName() + "]";

      final ITable table = getNewTable(RESULT, sTableName, types, sFields);
      values = new Object[7];

      while (iter.hasNext()) {
         stats = (ClassStatistics) m_Map.get(iter.next());
         values[0] = new Integer(stats.getClassID());
         values[1] = new Double(stats.getTotalArea());
         values[2] = new Integer(stats.getZonesCount());
         values[3] = new Double(stats.getMeanArea());
         values[4] = new Double(stats.getVarianceArea());
         values[5] = new Double(stats.getMaxArea());
         values[6] = new Double(stats.getMinArea());
         table.addRecord(values);
      }

   }


   private double getClassArea(int x,
                               int y) {

      int x2, y2;
      int iInitClass;
      int iPt;
      int n;
      int iClass;
      double dArea = 0;
      ArrayList centralPoints = new ArrayList();
      ArrayList adjPoints = new ArrayList();
      Point point;

      iInitClass = m_Grid.getCellValueAsInt(x, y);

      centralPoints.add(new Point(x, y));
      m_IsCellAlreadyVisited[x][y] = true;

      while (centralPoints.size() != 0) {
         for (iPt = 0; iPt < centralPoints.size(); iPt++) {
            dArea += m_Grid.getWindowCellSize() * m_Grid.getWindowCellSize();
            point = (Point) centralPoints.get(iPt);
            x = point.x;
            y = point.y;
            double dClass = m_Grid.getCellValueAsInt(x, y);
            if (!m_Grid.isNoDataValue(dClass)) {
               for (n = 0; n < 8; n++) {
                  x2 = x + m_iOffsetX[n];
                  y2 = y + m_iOffsetY[n];
                  dClass = m_Grid.getCellValueAsDouble(x2, y2);
                  if (!m_Grid.isNoDataValue(dClass)) {
                     iClass = (int) dClass;
                     if (m_IsCellAlreadyVisited[x2][y2] == false) {
                        if (iInitClass == iClass) {
                           m_IsCellAlreadyVisited[x2][y2] = true;
                           adjPoints.add(new Point(x2, y2));
                        }
                     }
                  }
               }
            }
         }

         centralPoints = adjPoints;
         adjPoints = new ArrayList();

      }

      return dArea;

   }

}
