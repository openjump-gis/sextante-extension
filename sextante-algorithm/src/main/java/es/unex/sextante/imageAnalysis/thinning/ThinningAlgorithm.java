package es.unex.sextante.imageAnalysis.thinning;

import java.awt.Point;
import java.util.ArrayList;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class ThinningAlgorithm
         extends
            GeoAlgorithm {

   private final static int   m_iOffsetX[] = { 0, 1, 1, 1, 0, -1, -1, -1, 0 };
   private final static int   m_iOffsetY[] = { -1, -1, 0, 1, 1, 1, 0, -1, -1 };

   public static final String LAYER        = "LAYER";
   public static final String RESULT       = "RESULT";

   protected final byte       NO_DATA      = 0;

   private IRasterLayer       m_Window;
   private IRasterLayer       m_Result;


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(false);
      setName(Sextante.getText("Thinning"));
      setGroup(Sextante.getText("Image_processing"));

      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Image"), true);
         addOutputRasterLayer(RESULT, Sextante.getText("Thinned_image"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      int iIteration = 1;
      boolean bFoundCell;
      double dValue;
      final ArrayList marked = new ArrayList();

      m_Window = m_Parameters.getParameterValueAsRasterLayer(LAYER);

      m_Window.setFullExtent();

      m_Result = getNewRasterLayer(RESULT, this.getName(), IRasterLayer.RASTER_DATA_TYPE_BYTE, m_Window.getWindowGridExtent());

      m_Result.setNoDataValue(NO_DATA);
      m_Result.assign(1);

      iNX = m_Window.getNX();
      iNY = m_Window.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dValue = m_Window.getCellValueAsDouble(x, y);
            if (m_Window.isNoDataValue(dValue) || (dValue == 0)) {
               m_Result.setNoData(x, y);
            }
            else {
               m_Result.setCellValue(x, y, 1);
            }
         }
      }

      do {
         bFoundCell = false;
         setProgressText(Sextante.getText("Iteration") + " " + Integer.toString(iIteration++));
         for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
            for (x = 0; x < iNX; x++) {
               if (m_Result.getCellValueAsByte(x, y) == 1) {
                  if (applyFirstCondition(x, y)) {
                     marked.add(new Point(x, y));
                     bFoundCell = true;
                  }
               }
            }
         }

         if (bFoundCell) {
            for (int i = 0; i < marked.size(); i++) {
               final Point pt = (Point) marked.get(i);
               m_Result.setNoData(pt.x, pt.y);
            }
            marked.clear();
         }
         else {
            break;
         }

         for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
            for (x = 0; x < iNX; x++) {
               if (m_Result.getCellValueAsByte(x, y) == 1) {
                  if (applySecondCondition(x, y)) {
                     marked.add(new Point(x, y));
                     bFoundCell = true;
                  }
               }
            }
         }

         for (int i = 0; i < marked.size(); i++) {
            final Point pt = (Point) marked.get(i);
            m_Result.setNoData(pt.x, pt.y);
         }
         marked.clear();

      }
      while (bFoundCell && !m_Task.isCanceled() && (iIteration < 100));

      return !m_Task.isCanceled();

   }


   private boolean applyFirstCondition(final int x,
                                       final int y) {

      int iCount = 0;
      int iChanges = 0;
      byte byValue;
      byte byPrev = 1;
      double pw, pe, ps, pn;

      for (int i = 0; i < 9; i++) {
         byValue = m_Result.getCellValueAsByte(x + m_iOffsetX[i], y + m_iOffsetY[i]);
         if (byValue == 1) {
            iCount++;
            if (byPrev == 0) {
               iChanges++;
            }
         }
         byPrev = byValue;
      }

      pn = m_Result.getCellValueAsByte(x + m_iOffsetX[0], y + m_iOffsetY[0]);
      pe = m_Result.getCellValueAsByte(x + m_iOffsetX[2], y + m_iOffsetY[2]);
      ps = m_Result.getCellValueAsByte(x + m_iOffsetX[4], y + m_iOffsetY[4]);
      pw = m_Result.getCellValueAsByte(x + m_iOffsetX[6], y + m_iOffsetY[6]);

      if ((pn * ps * pe == 0) && (pw * pe * ps == 0) && (iCount >= 2) && (iCount <= 6) && (iChanges == 1)) {
         return true;
      }
      else {
         return false;
      }

   }


   private boolean applySecondCondition(final int x,
                                        final int y) {

      int iCount = 0;
      int iChanges = 0;
      byte byValue;
      byte byPrev = 1;
      double pw, pe, ps, pn;

      for (int i = 0; i < 9; i++) {
         byValue = m_Result.getCellValueAsByte(x + m_iOffsetX[i], y + m_iOffsetY[i]);
         if (byValue == 1) {
            iCount++;
            if (byPrev == 0) {
               iChanges++;
            }
         }
         byPrev = byValue;
      }

      pn = m_Result.getCellValueAsByte(x + m_iOffsetX[0], y + m_iOffsetY[0]);
      pe = m_Result.getCellValueAsByte(x + m_iOffsetX[2], y + m_iOffsetY[2]);
      ps = m_Result.getCellValueAsByte(x + m_iOffsetX[4], y + m_iOffsetY[4]);
      pw = m_Result.getCellValueAsByte(x + m_iOffsetX[6], y + m_iOffsetY[6]);

      if ((pn * ps * pw == 0) && (pw * pe * pn == 0) && (iCount >= 2) && (iCount <= 6) && (iChanges == 1)) {
         return true;
      }
      else {
         return false;
      }

   }


}
