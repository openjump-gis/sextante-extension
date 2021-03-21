package es.unex.sextante.gridCategorical.reclassifyDisjoint;

import java.awt.Point;
import java.util.ArrayList;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class ReclassifyDisjointAlgorithm
         extends
            GeoAlgorithm {

   private final static int   m_iOffsetX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int   m_iOffsetY[] = { 1, 1, 0, -1, -1, -1, 0, 1 };

   public static final String INPUT        = "INPUT";
   public static final String RECLASS      = "RECLASS";

   int                        m_iNX, m_iNY;
   IRasterLayer               m_Window;
   IRasterLayer               m_Result;
   boolean                    m_IsCellAlreadyVisited[][];


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Reclassify_into_disjoint_classes"));
      setGroup(Sextante.getText("Reclassify_raster_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         addOutputRasterLayer(RECLASS, Sextante.getText("Reclassify"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iClass = 0;

      m_Window = m_Parameters.getParameterValueAsRasterLayer(INPUT);

      m_Result = getNewRasterLayer(RECLASS, m_Window.getName() + Sextante.getText("[reclassified]"),
               IRasterLayer.RASTER_DATA_TYPE_INT);

      m_Result.assignNoData();

      m_Window.setWindowExtent(m_Result);
      m_Window.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

      m_iNX = m_Window.getNX();
      m_iNY = m_Window.getNY();

      m_IsCellAlreadyVisited = new boolean[m_iNX][m_iNY];

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            final double dValue = m_Window.getCellValueAsDouble(x, y);
            if (!m_Window.isNoDataValue(dValue)) {
               if (!m_IsCellAlreadyVisited[x][y]) {
                  iClass++;
                  setClass(x, y, iClass);
               }
            }
         }
      }

      return !m_Task.isCanceled();


   }


   private void setClass(int x,
                         int y,
                         final int iNewClass) {

      int x2, y2;
      int iInitClass;
      int iPt;
      int n;
      int iClass;
      ArrayList centralPoints = new ArrayList();
      ArrayList adjPoints = new ArrayList();
      Point point;

      iInitClass = m_Window.getCellValueAsInt(x, y);

      centralPoints.add(new Point(x, y));
      m_IsCellAlreadyVisited[x][y] = true;

      while (centralPoints.size() != 0) {
         for (iPt = 0; iPt < centralPoints.size(); iPt++) {
            point = (Point) centralPoints.get(iPt);
            x = point.x;
            y = point.y;
            iClass = m_Window.getCellValueAsInt(x, y);
            if (!m_Window.isNoDataValue(iClass)) {
               for (n = 0; n < 8; n++) {
                  x2 = x + m_iOffsetX[n];
                  y2 = y + m_iOffsetY[n];
                  iClass = m_Window.getCellValueAsInt(x2, y2);
                  if (!m_Window.isNoDataValue(iClass)) {
                     if (m_IsCellAlreadyVisited[x2][y2] == false) {
                        if (iInitClass == iClass) {
                           m_IsCellAlreadyVisited[x2][y2] = true;
                           m_Result.setCellValue(x2, y2, iNewClass);
                           adjPoints.add(new Point(x2, y2));

                        }
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
