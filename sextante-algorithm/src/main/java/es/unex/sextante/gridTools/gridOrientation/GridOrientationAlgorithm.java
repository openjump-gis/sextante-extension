package es.unex.sextante.gridTools.gridOrientation;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class GridOrientationAlgorithm
         extends
            GeoAlgorithm {

   public static final String METHOD            = "METHOD";
   public static final String INPUT             = "INPUT";
   public static final String RESULT            = "RESULT";

   private int                m_iNX, m_iNY;
   private IRasterLayer       m_Window;
   private IRasterLayer       m_Result;

   public static final int    MIRROR_HORIZONTAL = 0;
   public static final int    MIRROR_VERTICAL   = 1;
   public static final int    INVERT            = 2;


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("Mirror_horizontally"), Sextante.getText("Mirror_vertically"),
               Sextante.getText("Invert") };

      setName(Sextante.getText("Mirror-flip"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("M\u00e9todo"), sMethod);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int iMethod;
      final String[] sMethod = { Sextante.getText("[mirrored]"), Sextante.getText("[mirrored]"), Sextante.getText("[Inverted]") };

      iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      m_Window = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      final AnalysisExtent gridExtent = new AnalysisExtent(m_Window);
      m_Window.setWindowExtent(gridExtent);
      m_Result = getNewRasterLayer(RESULT, m_Window.getName() + sMethod[iMethod], m_Window.getDataType(), gridExtent);

      m_iNX = m_Window.getNX();
      m_iNY = m_Window.getNY();

      switch (iMethod) {
         case 0:
            mirrorHorizontally();
            break;
         case 1:
            mirrorVertically();
            break;
         case 2:
            invert();
            break;
         default:
      }

      return !m_Task.isCanceled();

   }


   private void mirrorHorizontally() {

      int x, y;

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            m_Result.setCellValue(m_iNX - x - 1, y, m_Window.getCellValueAsDouble(x, y));
         }
      }

   }


   private void mirrorVertically() {

      int x, y;

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            m_Result.setCellValue(x, m_iNY - y - 1, m_Window.getCellValueAsDouble(x, y));
         }
         setProgress(y, m_iNY);
      }

   }


   private void invert() {

      int x, y;
      double dMin = 0, dMax = 0;
      double z;
      int A = 0;

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            z = m_Window.getCellValueAsFloat(x, y);
            if (!m_Window.isNoDataValue(z)) {
               if (A <= 0) {
                  dMin = dMax = z;
               }
               else {
                  if (dMin > z) {
                     dMin = z;
                  }
                  else if (dMax < z) {
                     dMax = z;
                  }
               }
               A++;
            }
         }
         setProgress(y, m_iNY);
      }

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            z = m_Window.getCellValueAsFloat(x, y);
            if (m_Window.isNoDataValue(z)) {
               m_Result.setNoData(x, y);
            }
            else {
               m_Result.setCellValue(x, y, dMax - z + dMin);
            }
         }
      }

   }

}
