package es.unex.sextante.morphometry.curvatures;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class CurvaturesAlgorithm
         extends
            GeoAlgorithm {

   private final static double PLAN_THRESHOLD = 0.00001;
   private final static int    m_iOffsetX[]   = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int    m_iOffsetY[]   = { 1, 1, 0, -1, -1, -1, 0, 1 };

   public static final String  METHOD         = "METHOD";
   public static final String  DEM            = "DEM";
   public static final String  HORZ           = "HORZ";
   public static final String  VERT           = "VERT";
   public static final String  GLOBAL         = "GLOBAL";
   public static final String  CLASS          = "CLASS";

   IRasterLayer                m_DEM          = null;
   IRasterLayer                m_Curv;
   IRasterLayer                m_hCurv;
   IRasterLayer                m_vCurv;
   IRasterLayer                m_CurvClass;

   private double              _6DX;
   private double              _2DX;
   private double              _4_DX2;
   private double              _DX2;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;

      final int iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);

      m_hCurv = getNewRasterLayer(HORZ, Sextante.getText("Horizontal_curvature"), IRasterLayer.RASTER_DATA_TYPE_FLOAT);
      m_vCurv = getNewRasterLayer(VERT, Sextante.getText("Vertical_curvature"), IRasterLayer.RASTER_DATA_TYPE_FLOAT);
      m_Curv = getNewRasterLayer(GLOBAL, Sextante.getText("Curvature"), IRasterLayer.RASTER_DATA_TYPE_FLOAT);
      m_CurvClass = getNewRasterLayer(CLASS, Sextante.getText("Classification"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
      final AnalysisExtent extent = m_hCurv.getWindowGridExtent();

      m_DEM.setWindowExtent(extent);

      iNX = m_DEM.getNX();
      iNY = m_DEM.getNY();

      _2DX = m_DEM.getWindowCellSize() * 2.;
      _DX2 = m_DEM.getWindowCellSize() * m_DEM.getWindowCellSize();
      _4_DX2 = 4 * m_DEM.getWindowCellSize() * m_DEM.getWindowCellSize();
      _6DX = m_DEM.getWindowCellSize() * 6.;

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            switch (iMethod) {
               case 0:
                  Do_FD_BRM(x, y);
                  break;

               case 1:
                  Do_FD_Heerdegen(x, y);
                  break;

               case 2:
                  Do_FD_Zevenbergen(x, y);
                  break;

               case 3:
                  Do_FD_Haralick(x, y);
                  break;
            }
         }
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("Fit_2_Degree_Polynom__Bauer_Rohdenburg_Bork_1985"),
               Sextante.getText("Fit_2_Degree_Polynom__Heerdegen_&_Beran_1982"),
               Sextante.getText("Fit_2_Degree_Polynom__Zevenbergen_&_Thorne_1987"),
               Sextante.getText("Fit_3_Degree_Polynom__Haralick_1983") };

      setName(Sextante.getText("Curvatures"));
      setGroup(Sextante.getText("Geomorphometry_and_terrain_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         addOutputRasterLayer(HORZ, Sextante.getText("Horizontal_curvature"));
         addOutputRasterLayer(VERT, Sextante.getText("Vertical_curvature"));
         addOutputRasterLayer(GLOBAL, Sextante.getText("Curvature"));
         addOutputRasterLayer(CLASS, Sextante.getText("Classification"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void Do_FD_BRM(final int x,
                          final int y) {

      double zm[], D, E, F, G, H;

      zm = new double[9];

      if (Get_SubMatrix3x3(x, y, zm)) {
         D = ((zm[0] + zm[2] + zm[3] + zm[5] + zm[6] + zm[8]) - 2 * (zm[1] + zm[4] + zm[7])) / _DX2;
         E = ((zm[0] + zm[6] + zm[1] + zm[7] + zm[2] + zm[8]) - 2 * (zm[3] + zm[4] + zm[5])) / _DX2;
         F = (zm[8] + zm[0] - zm[7]) / _4_DX2;
         G = ((zm[2] - zm[0]) + (zm[5] - zm[3]) + (zm[8] - zm[6])) / _6DX;
         H = ((zm[6] - zm[0]) + (zm[7] - zm[1]) + (zm[8] - zm[2])) / _6DX;

         Set_Parameters_Derive(x, y, D, E, F, G, H);
      }
   }


   private void Do_FD_Heerdegen(final int x,
                                final int y) {

      double zm[], a, b, D, E, F, G, H;

      zm = new double[9];

      if (Get_SubMatrix3x3(x, y, zm)) {
         a = zm[0] + zm[2] + zm[3] + zm[5] + zm[6] + zm[8];
         b = zm[0] + zm[1] + zm[2] + zm[6] + zm[7] + zm[8];
         D = (0.3 * a - 0.2 * b) / _DX2;
         E = (0.3 * b - 0.2 * a) / _DX2;
         F = (zm[0] - zm[2] - zm[6] + zm[8]) / _4_DX2;
         G = (-zm[0] + zm[2] - zm[3] + zm[5] - zm[6] + zm[8]) / _6DX;
         H = (-zm[0] - zm[1] - zm[2] + zm[6] + zm[7] + zm[8]) / _6DX;

         Set_Parameters_Derive(x, y, D, E, F, G, H);
      }
   }


   private void Do_FD_Zevenbergen(final int x,
                                  final int y) {

      double zm[], D, E, F, G, H;

      zm = new double[9];

      if (Get_SubMatrix3x3(x, y, zm)) {

         D = ((zm[3] + zm[5]) / 2.0 - zm[4]) / _DX2;
         E = ((zm[1] + zm[7]) / 2.0 - zm[4]) / _DX2;
         F = (zm[0] - zm[2] - zm[6] + zm[8]) / _4_DX2;
         G = (zm[5] - zm[3]) / _2DX;
         H = (zm[7] - zm[1]) / _2DX;

         Set_Parameters_Derive(x, y, D, E, F, G, H);
      }
   }


   private void Do_FD_Haralick(final int x,
                               final int y) {

      final int Mtrx[][][] = {
               { { 31, -5, -17, -5, 31 }, { -44, -62, -68, -62, -44 }, { 0, 0, 0, 0, 0 }, { 44, 62, 68, 62, 44 },
                        { -31, 5, 17, 5, -31 } },
               { { 31, -44, 0, 44, -31 }, { -5, -62, 0, 62, 5 }, { -17, -68, 0, 68, 17 }, { -5, -62, 0, 62, 5 },
                        { 31, -44, 0, 44, -31 } },
               { { 2, 2, 2, 2, 2 }, { -1, -1, -1, -1, -1 }, { -2, -2, -2, -2, -2 }, { -1, -1, -1, -1, -1 }, { 2, 2, 2, 2, 2 } },
               { { 4, 2, 0, -2, -4 }, { 2, 1, 0, -1, -2 }, { 0, 0, 0, 0, 0 }, { -2, -1, 0, 1, 2 }, { -4, -2, 0, 2, 4 } },
               { { 2, -1, -2, -1, 2 }, { 2, -1, -2, -1, 2 }, { 2, -1, -2, -1, 2 }, { 2, -1, -2, -1, 2 }, { 2, -1, -2, -1, 2 } } };

      final int QMtrx[] = { 4200, 4200, 700, 1000, 700 };

      int i, ix, iy, n;
      double Sum, zm[], k[];

      zm = new double[25];
      k = new double[5];

      if (Get_SubMatrix5x5(x, y, zm)) {
         for (i = 0; i < 5; i++) {
            for (n = 0, Sum = 0.0, iy = 0; iy < 5; iy++) {
               for (ix = 0; ix < 5; ix++, n++) {
                  Sum += zm[n] * Mtrx[i][ix][iy];
               }
            }

            k[i] = Sum / QMtrx[i];
         }

         Set_Parameters_Derive(x, y, k[4], k[2], k[3], k[1], k[0]);
      }
   }


   // additional methods

   private void Set_Parameters(final int x,
                               final int y,
                               final double dCurv,
                               final double dHCurv,
                               final double dVCurv) {

      int iClass;

      m_Curv.setCellValue(x, y, dCurv);
      m_hCurv.setCellValue(x, y, dHCurv);
      m_vCurv.setCellValue(x, y, dVCurv);


      iClass = dHCurv < -PLAN_THRESHOLD ? 0 : (dHCurv <= PLAN_THRESHOLD ? 3 : 6);
      iClass += dVCurv < -PLAN_THRESHOLD ? 0 : (dVCurv <= PLAN_THRESHOLD ? 1 : 2);

      m_CurvClass.setCellValue(x, y, iClass);

   }


   private void Set_Parameters_Derive(final int x,
                                      final int y,
                                      final double D,
                                      final double E,
                                      final double F,
                                      final double G,
                                      final double H) {

      double k1, k2, Curv, vCurv, hCurv;

      k1 = F * G * H;
      k2 = G * G + H * H;

      if (k2 != 0) {
         Curv = -2.0 * (E + D);
         vCurv = -2.0 * (D * G * G + E * H * H + k1) / k2;
         hCurv = -2.0 * (D * H * H + E * G * G - k1) / k2;

      }
      else {
         Curv = vCurv = hCurv = 0.0;
      }

      Set_Parameters(x, y, Curv, hCurv, vCurv);
   }


   private void Set_Parameters_NoData(final int x,
                                      final int y) {

      m_Curv.setNoData(x, y);
      m_hCurv.setNoData(x, y);
      m_vCurv.setNoData(x, y);

   }


   //	---------------------------------------------------------
   //	 Indexing of the Submatrix:
   //
   //	  +-------+    +-------+
   //	  | 7 0 1 |    | 2 5 8 |
   //	  | 6 * 2 | => | 1 4 7 |
   //	  | 5 4 3 |    | 0 3 6 |
   //	  +-------+    +-------+
   //
   //	---------------------------------------------------------
   private boolean Get_SubMatrix3x3(final int x,
                                    final int y,
                                    final double SubMatrix[]) {

      final int iSub[] = { 5, 8, 7, 6, 3, 0, 1, 2 };

      int i;
      double z, z2;

      z = m_DEM.getCellValueAsDouble(x, y);

      if (m_DEM.isNoDataValue(z)) {
         Set_Parameters_NoData(x, y);
      }
      else {
         SubMatrix[4] = 0.0;
         for (i = 0; i < 8; i++) {
            z2 = m_DEM.getCellValueAsDouble(x + m_iOffsetX[i], y + m_iOffsetY[i]);
            if (!m_DEM.isNoDataValue(z2)) {
               SubMatrix[iSub[i]] = z2 - z;
            }
            else {
               z2 = m_DEM.getCellValueAsDouble(x + m_iOffsetY[(i + 4) % 8], y + m_iOffsetY[(i + 4) % 8]);
               if (!m_DEM.isNoDataValue(z2)) {
                  SubMatrix[iSub[i]] = z - z2;
               }
               else {
                  SubMatrix[iSub[i]] = 0.0;
               }
            }
         }

         return (true);
      }

      return (false);
   }


   private boolean Get_SubMatrix5x5(final int x,
                                    final int y,
                                    final double SubMatrix[]) {
      int i, ix, iy, jx, jy;
      double z, z2;

      z = m_DEM.getCellValueAsDouble(x, y);

      if (!m_DEM.isNoDataValue(z)) {
         for (i = 0, iy = y - 2; iy <= y + 2; iy++) {
            jy = iy < 0 ? 0 : (iy >= m_DEM.getNY() ? m_DEM.getNY() - 1 : iy);
            for (ix = x - 2; ix <= x + 2; ix++, i++) {
               jx = ix < 0 ? 0 : (ix >= m_DEM.getNX() ? m_DEM.getNY() - 1 : ix);
               z2 = m_DEM.getCellValueAsDouble(jx, jy);
               if (!m_DEM.isNoDataValue(z2)) {
                  SubMatrix[i] = z2 - z;
               }
               else {
                  SubMatrix[i] = 0.0;
               }
            }
         }

         return (true);
      }

      return (false);
   }


}
