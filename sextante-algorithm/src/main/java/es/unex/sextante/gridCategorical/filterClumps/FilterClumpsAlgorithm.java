package es.unex.sextante.gridCategorical.filterClumps;

import java.awt.Point;
import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class FilterClumpsAlgorithm
         extends
            GeoAlgorithm {

   private final static int   m_iOffsetX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int   m_iOffsetY[] = { 1, 1, 0, -1, -1, -1, 0, 1 };

   public static final String INPUT        = "INPUT";
   public static final String MINAREA      = "MINAREA";
   public static final String RESULT       = "RESULT";

   int                        m_iArea;
   int                        m_iNX, m_iNY;
   IRasterLayer               m_Grid;
   IRasterLayer               m_Result;
   boolean                    m_IsCellAlreadyVisited[][];
   boolean                    m_IsCellAlreadyVisitedArea[][];


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Filter_clumps"));
      setGroup(Sextante.getText("Raster_categories_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         m_Parameters.addNumericalValue(MINAREA, Sextante.getText("Min_area_[cells]"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 100, 1, Integer.MAX_VALUE);
         addOutputRasterLayer(RESULT, Sextante.getText("Filtered_layer"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;

      m_Grid = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      m_iArea = m_Parameters.getParameterValueAsInt(MINAREA);

      m_Result = getNewRasterLayer(RESULT, m_Grid.getName() + "[filt]", IRasterLayer.RASTER_DATA_TYPE_INT);

      m_Grid.setWindowExtent(m_Result);
      m_Grid.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

      m_iNX = m_Grid.getNX();
      m_iNY = m_Grid.getNY();

      m_IsCellAlreadyVisited = new boolean[m_iNX][m_iNY];
      m_IsCellAlreadyVisitedArea = new boolean[m_iNX][m_iNY];

      m_Result.assign(m_Grid);

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            if (!m_IsCellAlreadyVisited[x][y]) {
               int iArea = getArea(x, y);
               if (iArea < m_iArea) {
                  setToNoData(x, y);
               }
               else {
                  iArea = iArea + 1;
               }
            }
         }
      }

      return !m_Task.isCanceled();

   }


   private int getArea(int x,
                       int y) {

      int x2, y2;
      int iInitClass;
      int iPt;
      int n;
      int iClass;
      int iArea = 1;
      ArrayList centralPoints = new ArrayList();
      ArrayList adjPoints = new ArrayList();
      Point point;

      if (m_IsCellAlreadyVisitedArea[x][y]) {
         return Integer.MAX_VALUE;
      }

      iInitClass = m_Grid.getCellValueAsInt(x, y);

      centralPoints.add(new Point(x, y));
      m_IsCellAlreadyVisitedArea[x][y] = true;

      while (centralPoints.size() != 0) {
         for (iPt = 0; iPt < centralPoints.size(); iPt++) {
            point = (Point) centralPoints.get(iPt);
            x = point.x;
            y = point.y;
            iClass = m_Grid.getCellValueAsInt(x, y);
            if (!m_Grid.isNoDataValue(iClass)) {
               for (n = 0; n < 8; n++) {
                  x2 = x + m_iOffsetX[n];
                  y2 = y + m_iOffsetY[n];
                  iClass = m_Grid.getCellValueAsInt(x2, y2);
                  if (!m_Grid.isNoDataValue(iClass)) {
                     if (m_IsCellAlreadyVisitedArea[x2][y2] == false) {
                        if (iInitClass == iClass) {
                           m_IsCellAlreadyVisitedArea[x2][y2] = true;
                           adjPoints.add(new Point(x2, y2));
                           iArea += 1;
                        }
                     }
                  }
               }
            }
         }

         centralPoints = adjPoints;
         adjPoints = new ArrayList();

         if (m_Task.isCanceled()) {
            return Integer.MAX_VALUE;
         }

      }

      return iArea;

   }


   private void setToNoData(int x,
                            int y) {

      int x2, y2;
      int iInitClass;
      int iPt;
      int n;
      int iClass;
      ArrayList centralPoints = new ArrayList();
      ArrayList adjPoints = new ArrayList();
      Point point;

      iInitClass = m_Result.getCellValueAsInt(x, y);

      centralPoints.add(new Point(x, y));

      while (centralPoints.size() != 0) {
         for (iPt = 0; iPt < centralPoints.size(); iPt++) {
            point = (Point) centralPoints.get(iPt);
            x = point.x;
            y = point.y;
            iClass = m_Result.getCellValueAsInt(x, y);
            if (!m_Result.isNoDataValue(iClass)) {
               m_Result.setNoData(x, y);
               for (n = 0; n < 8; n++) {
                  x2 = x + m_iOffsetX[n];
                  y2 = y + m_iOffsetY[n];
                  iClass = m_Result.getCellValueAsInt(x2, y2);
                  if (!m_Result.isNoDataValue(iClass)) {
                     if (iInitClass == iClass) {
                        adjPoints.add(new Point(x2, y2));
                     }
                  }
               }
            }
         }

         centralPoints = adjPoints;
         adjPoints = new ArrayList();

         if (m_Task.isCanceled()) {
            return;
         }

      }

   }

}
