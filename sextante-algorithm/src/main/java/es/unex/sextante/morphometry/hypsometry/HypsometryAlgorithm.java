package es.unex.sextante.morphometry.hypsometry;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class HypsometryAlgorithm
         extends
            GeoAlgorithm {

   private static final int   CLASS_COUNT = 100;

   public static final String DEM         = "DEM";
   public static final String RESULT      = "RESULT";

   IRasterLayer               m_DEM       = null;
   private double             _DX2;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int i;
      int A, a;
      int iNX, iNY;
      double z;
      double dMin = 0, dMax = 0;
      double dDz, dZa;
      long Count[];
      Object[] values;
      final String sFields[] = { Sextante.getText("Relative_elevation"), Sextante.getText("Relative_area"),
               Sextante.getText("Absolute_elevation"), Sextante.getText("Absolute_area") };
      final Class iTypes[] = { Double.class, Double.class, Double.class, Double.class };

      Count = new long[CLASS_COUNT + 1];
      values = new Object[4];

      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
      m_DEM.setFullExtent();

      iNX = m_DEM.getNX();
      iNY = m_DEM.getNY();

      _DX2 = m_DEM.getWindowCellSize() * m_DEM.getWindowCellSize();

      A = 0;

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            z = m_DEM.getCellValueAsFloat(x, y);
            if (!m_DEM.isNoDataValue(z)) {
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

      }

      if ((A > 0) && (dMin < dMax)) {

         for (y = 0; y < iNY; y++) {
            for (x = 0; x < iNX; x++) {
               z = m_DEM.getCellValueAsFloat(x, y);
               if (!m_DEM.isNoDataValue(z)) {
                  i = (int) (CLASS_COUNT * (dMax - z) / (dMax - dMin));
                  Count[i]++;
               }
            }
         }

         dDz = (dMax - dMin) / CLASS_COUNT;
         a = A;

         final String sTableName = Sextante.getText("Hypsometry_[") + m_DEM.getName() + "]";
         final ITable table = getNewTable(RESULT, sTableName, iTypes, sFields);

         for (i = CLASS_COUNT; i >= 0; i--) {
            dZa = (double) a / (double) A;
            a -= Count[i];

            values[0] = new Double(100.0 * i * dDz / (dMax - dMin)); // Relative Height;
            values[1] = new Double(100.0 * dZa); // Relative Area
            values[2] = new Double(dMin + i * dDz); // Absolute Height
            values[3] = new Double(a * _DX2); // Absolute Area
            table.addRecord(values);

         }


         return !m_Task.isCanceled();

      }
      else {
         return false;
      }


   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Hypsometry"));
      setGroup(Sextante.getText("Geomorphometry_and_terrain_analysis"));

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         this.setUserCanDefineAnalysisExtent(false);
         addOutputTable(RESULT, Sextante.getText("Hypsometry"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
