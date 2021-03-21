

package es.unex.sextante.profiles.crossSections;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;


public class CrossSectionsAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT    = "RESULT";
   public static final String NUMPOINTS = "NUMPOINTS";
   public static final String WIDTH     = "WIDTH";
   public static final String DISTANCE  = "DISTANCE";
   public static final String DEM       = "DEM";
   public static final String ROUTE     = "ROUTE";

   private IVectorLayer       m_Lines;
   private IVectorLayer       m_Result;
   private IRasterLayer       m_DEM;
   private double             m_dStepX, m_dStepY;
   private double             m_dDistance;
   private double             m_dInterval;
   private double             m_dSectionWidth;
   private int                m_iPointsInSection;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iField;
      Class types[];
      String sFieldNames[];

      m_Lines = m_Parameters.getParameterValueAsVectorLayer(ROUTE);
      if (!m_bIsAutoExtent) {
         m_Lines.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      if (m_Lines.getShapesCount() == 0) {
         throw new GeoAlgorithmExecutionException("zero shapes in layer");
      }

      m_dDistance = m_Parameters.getParameterValueAsDouble(DISTANCE);
      m_dSectionWidth = m_Parameters.getParameterValueAsDouble(WIDTH);
      m_iPointsInSection = m_Parameters.getParameterValueAsInt(NUMPOINTS);
      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);
      m_DEM.setFullExtent();

      m_dInterval = m_dSectionWidth / m_iPointsInSection;

      sFieldNames = new String[m_iPointsInSection * 2 + 1];
      types = new Class[m_iPointsInSection * 2 + 1];
      for (i = -m_iPointsInSection, iField = 0; i < m_iPointsInSection + 1; i++, iField++) {
         sFieldNames[iField] = Double.toString(m_dInterval * i);
         types[iField] = Double.class;
      }
      sFieldNames[m_iPointsInSection] = "0";
      types[m_iPointsInSection] = Double.class;

      m_Result = getNewVectorLayer(RESULT, Sextante.getText("Cross_sections"), IVectorLayer.SHAPE_TYPE_LINE, types, sFieldNames);
      final int iCount = m_Lines.getShapesCount();
      final IFeatureIterator iter = m_Lines.iterator();
      i = 0;
      while (iter.hasNext() && setProgress(i, iCount)) {
         final IFeature feature = iter.next();
         final Geometry geometry = feature.getGeometry();
         for (int j = 0; j < geometry.getNumGeometries(); j++) {
            final Geometry line = geometry.getGeometryN(j);
            processLine(line);
         }
         i++;
      }
      iter.close();


      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Cross_sections"));
      setGroup(Sextante.getText("Profiles"));
      setUserCanDefineAnalysisExtent(true);
      try {
         m_Parameters.addInputVectorLayer(ROUTE, Sextante.getText("Route"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);
         m_Parameters.addNumericalValue(DISTANCE, Sextante.getText("Distance_between_sections"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 100, 0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(WIDTH, Sextante.getText("Section_width__to_each_side"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 10, 0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(NUMPOINTS, Sextante.getText("Number_of_points__on_each_side"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 5, 0, Integer.MAX_VALUE);
         addOutputVectorLayer(RESULT, Sextante.getText("Cross_sections"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void processLine(final Geometry line) {

      int i, j;
      int iPoints;
      double dX1, dX2, dY1, dY2;
      double dAddedPointX = 0, dAddedPointY = 0;
      double dDX, dDY;
      double dRemainingDistFromLastSegment = 0;
      double dDistToNextPoint;
      double dDist;

      final Coordinate[] coords = line.getCoordinates();
      if (coords.length < 2) {
         return;
      }

      dX1 = coords[0].x;
      dY1 = coords[0].y;
      dX2 = coords[1].x;
      dY2 = coords[1].y;
      dAddedPointX = dX1;
      dAddedPointY = dY1;
      computeSegmentOrientation(dX1, dY1, dX2, dY2);
      addPoint(dX1, dY1);
      for (i = 0; i < coords.length - 1; i++) {
         dX1 = coords[i].x;
         dY1 = coords[i].y;
         dX2 = coords[i + 1].x;
         dY2 = coords[i + 1].y;
         dDX = dX2 - dX1;
         dDY = dY2 - dY1;
         dDistToNextPoint = Math.sqrt(dDX * dDX + dDY * dDY);
         computeSegmentOrientation(dX1, dY1, dX2, dY2);
         if (dRemainingDistFromLastSegment + dDistToNextPoint > m_dDistance) {
            iPoints = (int) ((dRemainingDistFromLastSegment + dDistToNextPoint) / m_dDistance);
            dDist = m_dDistance - dRemainingDistFromLastSegment;
            for (j = 0; j < iPoints; j++) {
               dDist = m_dDistance - dRemainingDistFromLastSegment;
               dDist += j * m_dDistance;
               dAddedPointX = dX1 + dDist * dDX / dDistToNextPoint;
               dAddedPointY = dY1 + dDist * dDY / dDistToNextPoint;
               addPoint(dAddedPointX, dAddedPointY);
            }
            dDX = dX2 - dAddedPointX;
            dDY = dY2 - dAddedPointY;
            dRemainingDistFromLastSegment = Math.sqrt(dDX * dDX + dDY * dDY);
         }
         else {
            dRemainingDistFromLastSegment += dDistToNextPoint;
         }

      }

   }


   private void computeSegmentOrientation(final double x,
                                          final double y,
                                          final double x2,
                                          final double y2) {

      double dx, dy;
      double dDistance;

      dx = x2 - x;
      dy = y2 - y;

      dDistance = Math.sqrt(dx * dx + dy * dy);
      m_dStepX = dy / dDistance * m_dInterval;
      m_dStepY = -dx / dDistance * m_dInterval;

   }


   private void addPoint(final double x,
                         final double y) {

      int i;
      int iField;
      double x2, y2;
      double dElevation;
      final Object value[] = new Object[m_iPointsInSection * 2 + 1];

      final Coordinate[] coords = new Coordinate[2];
      coords[0] = new Coordinate(x + m_iPointsInSection * m_dStepX, y + m_iPointsInSection * m_dStepY);
      coords[1] = new Coordinate(x - m_iPointsInSection * m_dStepX, y - m_iPointsInSection * m_dStepY);

      for (i = -m_iPointsInSection, iField = 0; i < m_iPointsInSection + 1; i++, iField++) {
         x2 = x - i * m_dStepX;
         y2 = y - i * m_dStepY;
         dElevation = m_DEM.getValueAt(x2, y2);
         value[iField] = new Double(dElevation);
      }

      final GeometryFactory gf = new GeometryFactory();
      m_Result.addFeature(gf.createLineString(coords), value);


   }

}
