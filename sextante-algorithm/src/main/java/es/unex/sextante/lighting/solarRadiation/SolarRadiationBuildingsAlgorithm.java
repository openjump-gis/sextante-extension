

package es.unex.sextante.lighting.solarRadiation;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;


public class SolarRadiationBuildingsAlgorithm
         extends
            GeoAlgorithm {

   public static final String  DEM           = "DEM";
   public static final String  BUILDINGS     = "BUILDING";
   public static final String  SOLARCONST    = "SOLARCONST";
   public static final String  METHOD        = "METHOD";
   public static final String  TRANSMITTANCE = "TRANSMITTANCE";
   public static final String  PRESSURE      = "PRESSURE";
   public static final String  HOURSTEP      = "HOURSTEP";
   public static final String  DAILYSTEP     = "DAILYSTEP";
   public static final String  WATER         = "WATER";
   public static final String  DUST          = "DUST";
   public static final String  LATITUDE      = "LATITUDE";
   public static final String  INITMONTH     = "INITMONTH";
   public static final String  INITDAY       = "INITDAY";
   public static final String  ENDMONTH      = "ENDMONTH";
   public static final String  ENDDAY        = "ENDDAY";
   public static final String  RADIATION     = "RADIATION";
   public static final String  TIME          = "TIME";
   public static final String  OUTPUT        = "OUTPUT";

   private static final double DEG_90_IN_RAD = Math.toRadians(90);
   private static final String FIELD_NAME    = "HEIGHT";
   private static final double SHADED        = -1;

   int                         m_iNX, m_iNY;
   int                         m_iMethod;
   double                      m_SolarConstant;
   double                      m_Transmittance;
   double                      m_Water;
   double                      m_Dust;
   double                      m_Pressure;
   double                      m_dAzimuth, m_dHeight;
   double                      m_dRDIRN, m_dRDIFN;
   double                      m_dRadiation, m_dRadiationDaily;
   double                      m_dDuration, m_dDurationDaily;
   IRasterLayer                m_DEM         = null;
   private Coordinate[]        m_Coords;
   private IVectorLayer        m_Output;
   private double              m_dBuildingHeight;
   private IRasterLayer        m_ModifiedDEM;
   private IVectorLayer        m_Buildings;


   @Override
   public void defineCharacteristics() {

      final String[] sMethod = { Sextante.getText("Global_atmospheric_transmittance"),
               Sextante.getText("Calculate_components_separately") };
      final String[] sMonths = { Sextante.getText("January"), Sextante.getText("February"), Sextante.getText("March"),
               Sextante.getText("April"), Sextante.getText("May"), Sextante.getText("June"), Sextante.getText("July"),
               Sextante.getText("August"), Sextante.getText("September"), Sextante.getText("October"),
               Sextante.getText("November"), Sextante.getText("December") };
      String[] sDays;

      int i;

      sDays = new String[31];
      for (i = 1; i < 32; i++) {
         sDays[i - 1] = Integer.toString(i);
      }

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Visibility_and_lighting"));
      setName(Sextante.getText("Solar_radiation_buildings"));

      try {

         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         m_Parameters.addInputVectorLayer(BUILDINGS, Sextante.getText("Buildings"), IVectorLayer.SHAPE_TYPE_POLYGON, true);
         m_Parameters.addNumericalValue(SOLARCONST, Sextante.getText("Solar_constant_[W-m\u00b2]"), 1367,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         m_Parameters.addNumericalValue(TRANSMITTANCE, Sextante.getText("Global_transmittance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 70.0, 0.0, 100.0);
         m_Parameters.addNumericalValue(PRESSURE, Sextante.getText("Atmospheric_pressure_[mb]"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1013.0, 0.0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(WATER, Sextante.getText("Atmospheric_water_content_[cm]"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1.68, 0.0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(DUST, Sextante.getText("Dust_[ppm]"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 100.0, 0.0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(LATITUDE, Sextante.getText("Latitude"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 41.0, -90.0, 90.0);
         m_Parameters.addNumericalValue(HOURSTEP, Sextante.getText("Interval_for_daily_insolation_[h]"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1.0, 0.001, 12.0);
         m_Parameters.addNumericalValue(DAILYSTEP, Sextante.getText("Interval_for_global_insolation_[days]"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1.0, 1, 100);
         m_Parameters.addSelection(INITMONTH, Sextante.getText("Starting_month"), sMonths);
         m_Parameters.addSelection(INITDAY, Sextante.getText("Starting_day"), sDays);
         m_Parameters.addSelection(ENDMONTH, Sextante.getText("Ending_month"), sMonths);
         m_Parameters.addSelection(ENDDAY, Sextante.getText("Ending_day"), sDays);
         addOutputVectorLayer(OUTPUT, Sextante.getText("Result"));

      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final int Month2Day[] = { 0, 31, 49, 80, 109, 140, 170, 201, 232, 272, 303, 333 };
      int Day_Step, Day_Start, Day_Stop;
      double Latitude;
      double Hour_Step, Hour_Start, Hour_Stop;

      m_iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      m_SolarConstant = m_Parameters.getParameterValueAsDouble(SOLARCONST) / 1000.0;
      m_Transmittance = m_Parameters.getParameterValueAsDouble(TRANSMITTANCE) / 100.0;
      m_Pressure = m_Parameters.getParameterValueAsDouble(PRESSURE);
      m_Water = m_Parameters.getParameterValueAsDouble(WATER);
      m_Dust = m_Parameters.getParameterValueAsDouble(DUST);
      m_Buildings = m_Parameters.getParameterValueAsVectorLayer(BUILDINGS);

      Latitude = Math.toRadians(m_Parameters.getParameterValueAsDouble(LATITUDE));
      Day_Step = m_Parameters.getParameterValueAsInt(DAILYSTEP);
      Hour_Step = m_Parameters.getParameterValueAsDouble(HOURSTEP);

      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
      m_DEM.setFullExtent();

      m_Coords = new Coordinate[2];
      m_Output = getNewVectorLayer(OUTPUT, Sextante.getText("Result"), IVectorLayer.SHAPE_TYPE_LINE, new Class[] { Double.class,
               Double.class }, new String[] { "RAD", "TIME" });

      Hour_Start = 0;
      Hour_Stop = 24;
      Day_Start = m_Parameters.getParameterValueAsInt(INITDAY) + Month2Day[m_Parameters.getParameterValueAsInt(INITMONTH)];
      Day_Stop = m_Parameters.getParameterValueAsInt(ENDDAY) + Month2Day[m_Parameters.getParameterValueAsInt(ENDMONTH)];

      modifyDEM();

      final IFeatureIterator iter = m_Buildings.iterator();
      while (iter.hasNext()) {
         final IFeature feature = iter.next();
         final Geometry geometry = feature.getGeometry();
         for (int i = 0; i < geometry.getNumGeometries(); i++) {
            final Geometry subgeom = geometry.getGeometryN(i);
            final Coordinate[] coords = subgeom.getCoordinates();
            for (int j = 0; j < coords.length - 1; j++) {
               m_Coords[0] = coords[j];
               m_Coords[1] = coords[j + 1];
               if (m_Coords[0].equals2D(m_Coords[1])) {
                  continue;
               }
               final String sBuildingHeight = feature.getRecord().getValue(m_Buildings.getFieldIndexByName(FIELD_NAME)).toString();
               try {
                  m_dBuildingHeight = Double.parseDouble(sBuildingHeight);
               }
               catch (final NumberFormatException e) {
                  m_dBuildingHeight = 0;
               }
               executeSumOfDays(Latitude, Hour_Step, Hour_Start, Hour_Stop, Day_Step, Day_Start, Day_Stop);
               createFacet();
            }
         }
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   private void modifyDEM() {

      try {
         m_ModifiedDEM = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_DOUBLE, m_DEM.getWindowGridExtent());
         m_ModifiedDEM.assign(m_DEM);
         final IFeatureIterator iter = m_Buildings.iterator();
         while (iter.hasNext()) {
            IFeature feature;
            try {
               feature = iter.next();
               final Geometry geometry = feature.getGeometry();
               modifyDEMWithPolygon(geometry);
            }
            catch (final IteratorException e) {
            }
         }
         iter.close();
      }
      catch (final UnsupportedOutputChannelException e) {
      }

   }


   private void modifyDEMWithPolygon(final Geometry geometry) {
      // TODO Auto-generated method stub

   }


   private void createFacet() {

      final GeometryFactory gf = new GeometryFactory();
      final LineString line = gf.createLineString(m_Coords);
      m_Output.addFeature(line, new Object[] { new Double(m_dRadiation), new Double(m_dDuration) });

   }


   private void executeSumOfDays(final double Latitude_RAD,
                                 final double Hour_Step,
                                 final double Hour_Start,
                                 final double Hour_Stop,
                                 int Day_Step,
                                 final int Day_Start,
                                 final int Day_Stop) {

      int Day, nDays;

      nDays = Day_Stop - Day_Start;

      m_dRadiation = m_dDuration = 0;

      if ((Day_Step < 1) || (Day_Step > nDays)) {
         Day_Step = 1;
      }

      for (Day = Day_Start; (Day < Day_Stop) && !m_Task.isCanceled(); Day += Day_Step) {

         setProgressText(Sextante.getText("Calculating_day") + Integer.toString(Day - Day_Start + 1) + Sextante.getText("de")
                         + Integer.toString(nDays));

         getDailySum(Latitude_RAD, Hour_Step, Hour_Start, Hour_Stop, Day);

         m_dRadiationDaily *= Day_Step;
         m_dRadiation += m_dRadiationDaily;

         m_dDurationDaily *= Day_Step;
         m_dDuration += m_dDurationDaily;

      }

      if ((Day_Step = Day_Stop - Day) > 0) {

         getDailySum(Latitude_RAD, Hour_Step, Hour_Start, Hour_Stop, Day);

         m_dRadiationDaily *= Day_Step;
         m_dRadiation += m_dRadiationDaily;

         m_dDurationDaily *= Day_Step;
         m_dDuration += m_dDurationDaily;

      }
   }


   private void getDailySum(final double Latitude_RAD,
                            final double Hour_Step,
                            double Hour_Start,
                            final double Hour_Stop,
                            int Day) {

      double time, Solar_Angle, d;

      Day %= 366;

      if (Day < 0) {
         Day += 366;
      }

      final Coordinate midPoint = new Coordinate((m_Coords[0].x + m_Coords[1].x) / 2., (m_Coords[0].y + m_Coords[1].y) / 2.);

      Hour_Start += Hour_Step / 2.0;

      m_dRadiationDaily = 0.0;
      m_dDurationDaily = 0.0;

      for (time = Hour_Start; (time < Hour_Stop) && setProgress((int) (time - Hour_Start), (int) (Hour_Stop - Hour_Start)); time += Hour_Step) {
         if (getSolarPosition(Day, time, Latitude_RAD, 0.0, false)) {
            Solar_Angle = rayTrace(midPoint);
            if ((Solar_Angle < DEG_90_IN_RAD) && (Solar_Angle != SHADED)) {
               getSolarCorrection(DEG_90_IN_RAD - m_dHeight, m_DEM.getValueAt(midPoint.x, midPoint.y));
               d = m_SolarConstant * Hour_Step * m_dRDIRN * Math.cos(Solar_Angle);
               m_dRadiationDaily += d;
               m_dDurationDaily += Hour_Step;
            }
         }
      }
   }


   void getSolarCorrection(final double ZenithAngle,
                           final double Elevation) {

      final double AM[] = // AM    Optical air mass in 1 degree increments for zenith angles >=60 [after LIST, 1968; p. 422]
      { 2.00, 2.06, 2.12, 2.19, 2.27, 2.36, 2.45, 2.55, 2.65, 2.77, 2.90, 3.05, 3.21, 3.39, 3.59, 3.82, 4.07, 4.37, 4.72, 5.12,
               5.60, 6.18, 6.88, 7.77, 8.90, 10.39, 12.44, 15.36, 19.79, 26.96, 26.96, 26.96 };

      final double Pressure_0 = 1013.0; // Standard atmospheric pressure = 1013 mb

      int i;
      double z, AMASS, AMASS2, AW, TW, TD, TDC;

      z = Math.toDegrees(ZenithAngle);

      if (z <= 60.0) {
         AMASS = 1.0 / Math.cos(ZenithAngle);
      }
      else {
         z -= 60.0;
         i = (int) z;
         AMASS = AM[i] + (z - i) * (AM[i + 1] - AM[i]);
      }

      z = m_Pressure / Math.pow(10.0, Elevation * 5.4667E-05); // P     Barometric pressure in mb
      AMASS2 = AMASS * z / Pressure_0;

      switch (m_iMethod) {
         case 0:
         default:
            m_dRDIRN = Math.pow(m_Transmittance, AMASS2);
            m_dRDIFN = 0.271 - 0.294 * m_dRDIRN;
            break;

         case 1:
            AW = 1.0 - 0.077 * Math.pow(m_Water * AMASS2, 0.3); // AW    Accounts for absorption by water vapour
            TW = Math.pow(0.975, m_Water * AMASS); // TW    Accounts for scattering by water vapour
            TD = Math.pow(0.950, m_Water * m_Dust / 100.0); // TD    Accounts for scattering by dust
            TDC = Math.pow(0.900, AMASS2) + 0.026 * (AMASS2 - 1.0); // TDC   Accounts for scattering by a dust free atmosphere

            m_dRDIRN = AW * TW * TD * TDC;
            m_dRDIFN = 0.5 * (AW - m_dRDIRN);
            break;
      }

   }


   //////////////////////////////////////////////////////////   /
   //                                                                                                                    //
   //                                           Solar Position                                           //
   //                                                                                                                    //
   //////////////////////////////////////////////////////////   /

   //   ---------------------------------------------------------
   private boolean getSolarPosition(int Day,
                                    final double Time,
                                    final double LAT,
                                    final double LON,
                                    final boolean bDegree) {

      final int Day2Month[] = { 0, 31, 49, 80, 109, 140, 170, 201, 232, 272, 303, 333, 366 };

      final double ECLIPTIC_OBL = Math.toRadians(23.43999); // obliquity of ecliptic

      int i;

      double JD, T, M, L, X, Y, Z, R, UTime, DEC, RA, theta, tau, Month, Year = 2000;

      for (Month = 1, i = 0; i <= 12; i++) {
         if (Day < Day2Month[i]) {
            Month = i;
            Day -= Day2Month[i - 1];
            break;
         }
      }

      if ((Month < 1) || (Month > 12)) {
         Month = 1;
      }

      if (Month <= 2) {
         Month += 12;
         Year -= 1;
      }

      UTime = Time - LON * 12.0 / Math.PI;

      //-----------------------------------------------------
      // 1. Julian Date...

      JD = (int) (365.25 * Year) + (int) (30.6001 * (Month + 1)) - 15 + 1720996.5 + Day + UTime / 24.0;
      T = (JD - 2451545.0) / 36525.0; // number of Julian centuries since 2000/01/01 at 12 UT (JD = 2451545.0)

      //-----------------------------------------------------
      // 2. Solar Coordinates (according to: Jean Meeus: Astronomical Algorithms), accuracy of 0.01 degree

      M = Math.toRadians(357.52910 + 35999.05030 * T - 0.0001559 * T * T - 0.00000048 * T * T * T); // mean anomaly
      L = Math.toRadians((280.46645 + 36000.76983 * T + 0.0003032 * T * T) // mean longitude
                         + ((1.914600 - 0.004817 * T - 0.000014 * T * T) * Math.sin(M) + (0.019993 - 0.000101 * T)
                            * Math.sin(2 * M) + 0.000290 * Math.sin(3 * M) // true longitude
                         ));

      //-----------------------------------------------------
      // 3. convert ecliptic longitude to right ascension RA and declination delta

      X = Math.cos(L);
      Y = Math.cos(ECLIPTIC_OBL) * Math.sin(L);
      Z = Math.sin(ECLIPTIC_OBL) * Math.sin(L);
      R = Math.sqrt(1.0 - Z * Z);

      DEC = Math.atan2(Z, R);
      RA = 2 * Math.atan2(Y, (X + R));

      //-----------------------------------------------------
      // 4. compute sidereal time (degree) at Greenwich local sidereal time at longitude (�E)

      theta = LON + Math.toRadians(280.46061837 + 360.98564736629 * (JD - 2451545.0) + T * T * (0.000387933 - T / 38710000.0));

      //-----------------------------------------------------
      // 5. compute local hour angle (degree)

      tau = theta - RA;

      //-----------------------------------------------------
      // 6. convert (tau, delta) to horizon coordinates (h, az) of the observer

      m_dHeight = Math.asin(Math.sin(LAT) * Math.sin(DEC) + Math.cos(LAT) * Math.cos(DEC) * Math.cos(tau));
      m_dAzimuth = Math.atan2(-Math.sin(tau) * Math.cos(DEC), Math.cos(LAT) * Math.sin(DEC) - Math.sin(LAT) * Math.cos(DEC)
                                                              * Math.cos(tau));

      if (bDegree) {
         m_dHeight = Math.toDegrees(m_dHeight);
         m_dAzimuth = Math.toDegrees(m_dAzimuth);
      }

      return (m_dHeight >= 0.0);
   }


   private double rayTrace(final Coordinate midPoint) {

      //final double dSlope = DEG_90_IN_RAD;
      final double dDifX = m_Coords[1].x - m_Coords[0].x;
      final double dDifY = m_Coords[1].y - m_Coords[0].y;
      final double dAspect = Math.atan2(dDifX, dDifY);
      final double dElevation = m_DEM.getValueAt(midPoint.x, midPoint.y) + m_dBuildingHeight / 2.;

      double dx, dy, d = 0;
      final AnalysisExtent ext = m_DEM.getWindowGridExtent();

      dx = Math.abs(Math.sin(m_dAzimuth));
      dy = Math.abs(Math.cos(m_dAzimuth));

      if ((dx > 0.0) || (dy > 0.0)) {
         if (dx > dy) {
            dx /= ext.getCellSize();
            dy /= dx;
            dx = ext.getCellSize();
         }
         else {
            dy /= ext.getCellSize();
            dx /= dy;
            dy = ext.getCellSize();
         }

         final double dDeltaD = Math.sqrt(dx * dx + dy * dy);

         dx = dx * Math.signum(dDifX);
         dy = dy * Math.signum(dDifY);

         double x = midPoint.x;
         double y = midPoint.y;

         for (d = 0.0;; d += dDeltaD, x += dx, y += dy) {
            if (ext.contains(x, y)) {
               final double dElevation2 = m_DEM.getValueAt(x, y);
               final double dAngle = Math.atan2(dElevation2 - dElevation, d);
               if (dAngle > m_dHeight) {
                  return SHADED;
               }
            }
            else {
               final double dSinDec = Math.sin(m_dHeight);
               final double dCosDec = Math.cos(m_dHeight);
               /*dSlope = Math.tan(dSlope);
               dShading = DEG_90_IN_RAD - Math.atan(dSlope);*/
               double dShading = 0;
               dShading = Math.acos(Math.sin(dShading) * dSinDec + Math.cos(dShading) * dCosDec * Math.cos(dAspect - m_dAzimuth));
               if (dShading > DEG_90_IN_RAD) {
                  dShading = DEG_90_IN_RAD;
               }
               return dShading;
            }
         }
      }

      return -1;

   }
}
