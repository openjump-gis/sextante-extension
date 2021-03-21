package es.unex.sextante.fire.simulation;

import java.util.ArrayList;

import wf.rothermel.Behave;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterWrappers.GridCell;

public class FireSimulationAlgorithm
         extends
            GeoAlgorithm {

   private static final int    NO_DATA    = -1;
   private static final double ANGLES[][] = { { 315, 0, 45 }, { 270, 0, 90 }, { 225, 180, 135 } };

   public static final String  DEM        = "DEM";
   public static final String  MODEL      = "MODEL";
   public static final String  INITPOINTS = "INITPOINTS";
   public static final String  WINDSPEED  = "WINDSPEED";
   public static final String  WINDDIR    = "WINDDIR";
   public static final String  DEAD1      = "DEAD1";
   public static final String  DEAD10     = "DEAD10";
   public static final String  DEAD100    = "DEAD100";
   public static final String  HERB       = "HERB";
   public static final String  WOODY      = "WOODY";
   public static final String  TIME       = "TIME";

   private int                 m_iNX, m_iNY;
   private IRasterLayer        m_Dead1, m_Dead10, m_Dead100;
   private IRasterLayer        m_WindDir, m_WindSpeed;
   private IRasterLayer        m_Herb, m_Woody;
   private IRasterLayer        m_Points;
   private IRasterLayer        m_Model;
   private IRasterLayer        m_DEM;
   private IRasterLayer        m_Time;
   private IRasterLayer        m_SpreadRate;
   private IRasterLayer        m_Dir;
   private IRasterLayer        m_Eccentricity;
   private ArrayList           m_CentralPoints, m_AdjPoints;
   private Behave              m_Behave;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Fire_simulation"));
      setGroup(Sextante.getText("Fire_modeling"));

      setUserCanDefineAnalysisExtent(true);
      setIsDeterminatedProcess(false);

      try {
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         m_Parameters.addInputRasterLayer(MODEL, Sextante.getText("Fuel_model"), true);
         m_Parameters.addInputRasterLayer(INITPOINTS, Sextante.getText("Ignition_points"), true);
         m_Parameters.addInputRasterLayer(WINDSPEED, Sextante.getText("Wind_speed_m-s"), true);
         m_Parameters.addInputRasterLayer(WINDDIR, Sextante.getText("Wind_direction"), true);
         m_Parameters.addInputRasterLayer(DEAD1, Sextante.getText("Dead_fuel_moisture_1H"), true);
         m_Parameters.addInputRasterLayer(DEAD10, Sextante.getText("Dead_fuel_moisture_10H"), true);
         m_Parameters.addInputRasterLayer(DEAD100, Sextante.getText("Dead_fuel_moisture_100"), true);
         m_Parameters.addInputRasterLayer(HERB, Sextante.getText("Herb_fuel_moisture"), true);
         m_Parameters.addInputRasterLayer(WOODY, Sextante.getText("Woody_fuel_moisture"), true);
         addOutputRasterLayer(TIME, Sextante.getText("Time"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      double dValue;

      m_CentralPoints = new ArrayList();
      m_AdjPoints = new ArrayList();

      m_Behave = new Behave();

      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
      m_Woody = m_Parameters.getParameterValueAsRasterLayer(WOODY);
      m_Herb = m_Parameters.getParameterValueAsRasterLayer(HERB);
      m_Dead1 = m_Parameters.getParameterValueAsRasterLayer(DEAD1);
      m_Dead10 = m_Parameters.getParameterValueAsRasterLayer(DEAD10);
      m_Dead100 = m_Parameters.getParameterValueAsRasterLayer(DEAD100);
      m_WindSpeed = m_Parameters.getParameterValueAsRasterLayer(WINDSPEED);
      m_WindDir = m_Parameters.getParameterValueAsRasterLayer(WINDDIR);
      m_Model = m_Parameters.getParameterValueAsRasterLayer(MODEL);
      m_Points = m_Parameters.getParameterValueAsRasterLayer(INITPOINTS);

      m_Time = getNewRasterLayer(TIME, Sextante.getText("Time"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      final AnalysisExtent extent = m_Time.getLayerGridExtent();

      m_SpreadRate = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_DOUBLE, extent);
      m_Eccentricity = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_DOUBLE, extent);
      m_Dir = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_DOUBLE, extent);

      m_DEM.setWindowExtent(extent);
      m_Herb.setWindowExtent(extent);
      m_Woody.setWindowExtent(extent);
      m_Dead1.setWindowExtent(extent);
      m_Dead10.setWindowExtent(extent);
      m_Dead100.setWindowExtent(extent);
      m_WindSpeed.setWindowExtent(extent);
      m_WindDir.setWindowExtent(extent);
      m_Model.setWindowExtent(extent);
      m_Points.setWindowExtent(extent);
      m_Points.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

      m_iNX = extent.getNX();
      m_iNY = extent.getNY();

      m_Time.setNoDataValue(NO_DATA);
      m_Time.assignNoData();

      setProgressText(Sextante.getText("Calculando_velocidades_propagacion"));
      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            calculateCostValues(x, y);
            dValue = m_Points.getCellValueAsDouble(x, y);
            if ((dValue != 0.0) && !m_Points.isNoDataValue(dValue)) {
               m_CentralPoints.add(new GridCell(x, y, 0));
               m_Time.setCellValue(x, y, 0.0);
            }
         }

      }

      simulateFire();

      return !m_Task.isCanceled();

   }


   private void calculateCostValues(final int x,
                                    final int y) {

      final double dDead1 = m_Dead1.getCellValueAsDouble(x, y);
      final double dDead10 = m_Dead10.getCellValueAsDouble(x, y);
      final double dDead100 = m_Dead100.getCellValueAsDouble(x, y);
      final double dWoody = m_Woody.getCellValueAsDouble(x, y);
      final double dHerb = m_Herb.getCellValueAsDouble(x, y);
      final double dWindSpeed = m_WindSpeed.getCellValueAsDouble(x, y);
      final double dWindDir = m_WindDir.getCellValueAsDouble(x, y);
      final double dAspect = m_DEM.getAspect(x, y);
      final double dSlope = m_DEM.getSlope(x, y);
      final int iModel = m_Model.getCellValueAsInt(x, y);

      if (m_Dead1.isNoDataValue(dDead1) || m_Dead10.isNoDataValue(dDead10) || m_Dead100.isNoDataValue(dDead100)
          || m_Herb.isNoDataValue(dHerb) || m_Woody.isNoDataValue(dWoody) || m_WindSpeed.isNoDataValue(dWindSpeed)
          || m_WindDir.isNoDataValue(dWindDir) || m_Model.isNoDataValue(iModel) || m_DEM.isNoDataValue(dAspect)
          || m_DEM.isNoDataValue(dSlope)) {}
      else {
         if (m_Behave.setFuelModel(iModel)) {
            m_Behave.setAspect(Math.toDegrees(dAspect));
            m_Behave.setSlope(Math.toDegrees(dSlope));
            m_Behave.setWindSpeed(dWindSpeed);
            m_Behave.setWindDir(dWindDir);
            m_Behave.setWindSpeed(dWindSpeed);
            m_Behave.setDeadMoisture1(dDead1);
            m_Behave.setDeadMoisture10(dDead10);
            m_Behave.setDeadMoisture100(dDead100);
            m_Behave.setWoodyMoisture(dWoody);
            m_Behave.setHerbMoisture(dHerb);
            m_Behave.calc();
            m_SpreadRate.setCellValue(x, y, m_Behave.ros);
            m_Dir.setCellValue(x, y, m_Behave.sdr);
            m_Eccentricity.setCellValue(x, y, m_Behave.ecc);
         }
         else {
            m_SpreadRate.setNoData(x, y);
            m_Eccentricity.setNoData(x, y);
            m_Dir.setNoData(x, y);
         }
      }

   }


   private void simulateFire() {

      int i, j;
      int iPt;
      int x, y, x2, y2;
      double dAccTime;
      double dSpeed1, dSpeed2;
      double dPrevTime;
      GridCell cell;
      final double dCellSize = m_Time.getWindowCellSize();

      final double dDist[][] = new double[3][3];

      for (i = -1; i < 2; i++) {
         for (j = -1; j < 2; j++) {
            dDist[i + 1][j + 1] = Math.sqrt(i * i + j * j);
         }
      }

      setProgressText(Sextante.getText("Simulando_propagacion_fuego"));
      while ((m_CentralPoints.size() != 0) && !m_Task.isCanceled()) {
         for (iPt = 0; iPt < m_CentralPoints.size(); iPt++) {
            cell = (GridCell) m_CentralPoints.get(iPt);
            x = cell.getX();
            y = cell.getY();
            dSpeed1 = m_SpreadRate.getCellValueAsDouble(x, y);
            for (i = -1; i < 2; i++) {
               for (j = -1; j < 2; j++) {
                  x2 = x + i;
                  y2 = y + j;
                  dSpeed2 = m_SpreadRate.getCellValueAsDouble(x2, y2);
                  if (!m_SpreadRate.isNoDataValue(dSpeed1) && !m_SpreadRate.isNoDataValue(dSpeed2)) {
                     dAccTime = m_Time.getCellValueAsDouble(x, y);
                     dAccTime += ((dCellSize * dDist[i + 1][j + 1]) / (getSpreadRateAt(x, y, i, j)));
                     dPrevTime = m_Time.getCellValueAsDouble(x2, y2);
                     if (m_Time.isNoDataValue(dPrevTime) || (dPrevTime > dAccTime)) {
                        m_Time.setCellValue(x2, y2, dAccTime);
                        m_AdjPoints.add(new GridCell(x2, y2, 0));
                     }
                  }
               }
            }
         }

         m_CentralPoints = m_AdjPoints;
         m_AdjPoints = new ArrayList();

         setProgressText(Integer.toString(m_CentralPoints.size()));

      }
   }


   private double getSpreadRateAt(final int x,
                                  final int y,
                                  final int iH,
                                  final int iV) {

      final int x2 = x + iH;
      final int y2 = y + iV;

      final double dAngle = ANGLES[iV + 1][iH + 1];
      final double dAngle1 = m_Dir.getCellValueAsDouble(x, y);
      final double dAngle2 = m_Dir.getCellValueAsDouble(x2, y2);
      final double dEcc1 = m_Eccentricity.getCellValueAsDouble(x, y);
      final double dEcc2 = m_Eccentricity.getCellValueAsDouble(x2, y2);
      final double dRos1 = m_SpreadRate.getCellValueAsDouble(x, y);
      final double dRos2 = m_SpreadRate.getCellValueAsDouble(x2, y2);

      final double dCost1 = Behave.getRosInDir(dRos1, dAngle1, dAngle, dEcc1) / 2.;
      final double dCost2 = Behave.getRosInDir(dRos2, dAngle2, dAngle, dEcc2) / 2.;

      return dCost1 + dCost2;

   }

}
