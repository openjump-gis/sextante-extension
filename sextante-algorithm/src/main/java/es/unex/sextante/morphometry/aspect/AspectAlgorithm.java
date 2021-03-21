package es.unex.sextante.morphometry.aspect;


import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class AspectAlgorithm
         extends
            GeoAlgorithm {

   private final static int    m_iOffsetX[]         = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int    m_iOffsetY[]         = { -1, -1, 0, 1, 1, 1, 0, -1 };

   public static final String  ASPECT               = "ASPECT";
   public static final String  METHOD               = "METHOD";
   public static final String  UNITS                = "UNITS";
   public static final String  DEM                  = "DEM";

   public final static int     UNITS_RADIANS        = 0;
   public final static int     UNITS_DEGREES        = 1;
   public final static int     UNITS_PERCENTAGE     = 2;

   public final static int     METHOD_MAXIMUM_SLOPE = 0;
   public final static int     METHOD_TARBOTON      = 1;
   public final static int     METHOD_BURGESS       = 2;
   public final static int     METHOD_BAUER         = 3;
   public final static int     METHOD_HEERDEGEN     = 4;
   public final static int     METHOD_ZEVENBERGEN   = 5;
   public final static int     METHOD_HARALICK      = 6;

   private static final double DEG_45_IN_RAD        = Math.toRadians(45);
   private static final double DEG_90_IN_RAD        = Math.toRadians(90);
   private static final double DEG_180_IN_RAD       = Math.toRadians(180);
   private static final double DEG_270_IN_RAD       = Math.toRadians(270);

   IRasterLayer                m_DEM                = null;
   IRasterLayer                m_Aspect;

   private double              _6DX;
   private double              _2DX;

   private int                 m_iUnits;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;

      final int iMethod = m_Parameters.getParameterValueAsInt(METHOD);

      m_iUnits = m_Parameters.getParameterValueAsInt(UNITS);

      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);

      m_Aspect = getNewRasterLayer(ASPECT, Sextante.getText("Aspect"), IRasterLayer.RASTER_DATA_TYPE_FLOAT);

      final AnalysisExtent extent = m_Aspect.getWindowGridExtent();

      m_DEM.setWindowExtent(extent);

      iNX = m_DEM.getNX();
      iNY = m_DEM.getNY();

      _2DX = m_DEM.getWindowCellSize() * 2.;
      _6DX = m_DEM.getWindowCellSize() * 6.;

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            switch (iMethod) {
               case 0:
                  Do_MaximumSlope(x, y);
                  break;

               case 1:
                  Do_Tarboton(x, y);
                  break;

               case 2:
                  Do_LeastSquare(x, y);
                  break;

               case 3:
                  Do_FD_BRM(x, y);
                  break;

               case 4:
                  Do_FD_Heerdegen(x, y);
                  break;

               case 5:
                  Do_FD_Zevenbergen(x, y);
                  break;

               case 6:
                  Do_FD_Haralick(x, y);
                  break;
            }
         }
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("M\u00e1ximum_slope__Travis_et_al_1975"),
               Sextante.getText("Maximum_Triangle_Slope__Tarboton_1997"),
               Sextante.getText("Plane_fitting__Costa-Cabral_&_Burgess_1996"),
               Sextante.getText("Fit_2_Degree_Polynom__Bauer_Rohdenburg_Bork_1985"),
               Sextante.getText("Fit_2_Degree_Polynom__Heerdegen_&_Beran_1982"),
               Sextante.getText("Fit_2_Degree_Polynom__Zevenbergen_&_Thorne_1987"),
               Sextante.getText("Fit_3_Degree_Polynom__Haralick_1983") };

      final String[] sUnits = { Sextante.getText("Radians"), Sextante.getText("Degrees") };

      setName(Sextante.getText("Aspect"));
      setGroup(Sextante.getText("Geomorphometry_and_terrain_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);

         m_Parameters.addSelection(UNITS, Sextante.getText("Units"), sUnits);

         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         addOutputRasterLayer(ASPECT, Sextante.getText("Aspect"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void Do_MaximumSlope(final int x,
                                final int y) {

      int i, Aspect;
      double z, z2, dSlope, dMaxSlope;

      z = m_DEM.getCellValueAsDouble(x, y);

      if (m_DEM.isNoDataValue(z)) {
         Set_Parameters_NoData(x, y);
      }
      else {
         dMaxSlope = 0.0;
         for (Aspect = -1, i = 0; i < 8; i++) {
            z2 = m_DEM.getCellValueAsDouble(x + m_iOffsetX[i], y + m_iOffsetY[i]);
            if (!m_DEM.isNoDataValue(z2)) {
               dSlope = Math.atan((z - z2) / m_DEM.getDistToNeighborInDir(i));
               if (dSlope > dMaxSlope) {
                  Aspect = i;
                  dMaxSlope = dSlope;
               }
            }
         }

         if (Aspect < 0.0) {
            Set_Parameters_NoData(x, y);
         }
         else {
            Set_Parameters(x, y, Aspect * DEG_45_IN_RAD);
         }
      }
   }


   private void Do_Tarboton(final int x,
                            final int y) {

      int i, j;
      double z, z2, zm[], iSlope, iAspect, Slope, Aspect, G, H;

      zm = new double[8];

      z = m_DEM.getCellValueAsDouble(x, y);

      if (m_DEM.isNoDataValue(z)) {
         Set_Parameters_NoData(x, y);
      }
      else {
         for (i = 0; i < 8; i++) {

            z2 = m_DEM.getCellValueAsDouble(x + m_iOffsetX[i], y + m_iOffsetY[i]);
            if (!m_DEM.isNoDataValue(z2)) {
               zm[i] = z2;
            }
            else {
               z2 = m_DEM.getCellValueAsDouble(x + m_iOffsetX[(i + 4) % 8], y + m_iOffsetY[(i + 4) % 8]);
               if (!m_DEM.isNoDataValue(z2)) {
                  zm[i] = z - (z2 - z);
               }
               else {
                  zm[i] = z;
               }
            }
         }

         Slope = 0.0;
         Aspect = -1.0;

         for (i = 0, j = 1; i < 8; i++, j = (j + 1) % 8) {
            if ((i % 2) != 0) // i => diagonal
            {
               G = (z - zm[j]) / m_DEM.getWindowCellSize();
               H = (zm[j] - zm[i]) / m_DEM.getWindowCellSize();
            }
            else // i => orthogonal
            {
               G = (z - zm[i]) / m_DEM.getWindowCellSize();
               H = (zm[i] - zm[j]) / m_DEM.getWindowCellSize();
            }

            if (H < 0.0) {
               iAspect = 0.0;
               iSlope = G;
            }
            else if (H > G) {
               iAspect = DEG_45_IN_RAD;
               iSlope = (z - zm[((i % 2) != 0) ? i : j]) / (Math.sqrt(2.0) * m_DEM.getWindowCellSize());
            }
            else {
               iAspect = Math.atan(H / G);
               iSlope = Math.sqrt(G * G + H * H);
            }

            if (iSlope > Slope) {
               Aspect = i * DEG_45_IN_RAD + (((i % 2) != 0) ? DEG_45_IN_RAD - iAspect : iAspect);
               Slope = iSlope;
            }
         }

         if (Aspect < 0.0) {
            Set_Parameters_NoData(x, y);
         }
         else {
            Set_Parameters(x, y, Aspect);
         }
      }
   }


   private void Do_LeastSquare(final int x,
                               final int y) {

      double zm[], a, b;

      zm = new double[9];

      if (Get_SubMatrix3x3(x, y, zm)) {
         a = ((zm[2] + 2 * zm[5] + zm[8]) - (zm[0] + 2 * zm[3] + zm[6])) / (8 * m_DEM.getWindowCellSize());
         b = ((zm[6] + 2 * zm[7] + zm[8]) - (zm[0] + 2 * zm[1] + zm[2])) / (8 * m_DEM.getWindowCellSize());

         if (a != 0.0) {
            Set_Parameters(x, y, DEG_180_IN_RAD + Math.atan2(b, a));
         }
         else if (b > 0.0) {
            Set_Parameters(x, y, DEG_270_IN_RAD);
         }
         else if (b < 0.0) {
            Set_Parameters(x, y, DEG_90_IN_RAD);
         }
         else {
            Set_Parameters_NoData(x, y);
         }
      }
   }


   private void Do_FD_BRM(final int x,
                          final int y) {

      double zm[], G, H;

      zm = new double[9];

      if (Get_SubMatrix3x3(x, y, zm)) {
         G = ((zm[2] - zm[0]) + (zm[5] - zm[3]) + (zm[8] - zm[6])) / _6DX;
         H = ((zm[6] - zm[0]) + (zm[7] - zm[1]) + (zm[8] - zm[2])) / _6DX;
         Set_Parameters_Derive(x, y, G, H);
      }
   }


   private void Do_FD_Heerdegen(final int x,
                                final int y) {

      double zm[], G, H;

      zm = new double[9];

      if (Get_SubMatrix3x3(x, y, zm)) {
         G = (-zm[0] + zm[2] - zm[3] + zm[5] - zm[6] + zm[8]) / _6DX;
         H = (-zm[0] - zm[1] - zm[2] + zm[6] + zm[7] + zm[8]) / _6DX;
         Set_Parameters_Derive(x, y, G, H);
      }
   }


   private void Do_FD_Zevenbergen(final int x,
                                  final int y) {

      double zm[], G, H;

      zm = new double[9];

      if (Get_SubMatrix3x3(x, y, zm)) {
         G = (zm[5] - zm[3]) / _2DX;
         H = (zm[7] - zm[1]) / _2DX;
         Set_Parameters_Derive(x, y, G, H);
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
      k = new double[2];

      if (Get_SubMatrix5x5(x, y, zm)) {
         for (i = 0; i < 2; i++) {
            for (n = 0, Sum = 0.0, iy = 0; iy < 5; iy++) {
               for (ix = 0; ix < 5; ix++, n++) {
                  Sum += zm[n] * Mtrx[i][ix][iy];
               }
            }

            k[i] = Sum / QMtrx[i];
         }

         Set_Parameters_Derive(x, y, k[1], k[0]);
      }
   }


   //	additional methods

   private void Set_Parameters(final int x,
                               final int y,
                               double dAspect) {

      if (m_iUnits == UNITS_DEGREES) {
         dAspect = Math.toDegrees(dAspect);
      }

      m_Aspect.setCellValue(x, y, dAspect);

   }


   private void Set_Parameters_Derive(final int x,
                                      final int y,
                                      final double G,
                                      final double H) {

      if (G != 0.0) {
         Set_Parameters(x, y, DEG_180_IN_RAD + Math.atan2(H, G));
      }
      else if (H > 0.0) {
         Set_Parameters(x, y, DEG_270_IN_RAD);
      }
      else if (H < 0.0) {
         Set_Parameters(x, y, DEG_90_IN_RAD);
      }
      else {
         m_Aspect.setNoData(x, y);
      }
   }


   private void Set_Parameters_NoData(final int x,
                                      final int y) {

      m_Aspect.setNoData(x, y);

   }


   //	---------------------------------------------------------
   //	Indexing of the Submatrix:
   //
   //	+-------+    +-------+
   //	| 7 0 1 |    | 2 5 8 |
   //	| 6 * 2 | => | 1 4 7 |
   //	| 5 4 3 |    | 0 3 6 |
   //	+-------+    +-------+
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
               z2 = m_DEM.getCellValueAsDouble(x + m_iOffsetX[(i + 4) % 8], y + m_iOffsetY[(i + 4) % 8]);
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
