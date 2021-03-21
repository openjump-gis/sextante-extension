package es.unex.sextante.vectorTools.graticuleBuilder;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class GraticuleBuilderAlgorithm
         extends
            GeoAlgorithm {

   private static final int   TYPE_RECTANGLES = 0;
   private static final int   TYPE_LINES      = 1;
   private static final int   TYPE_POINTS     = 2;

   public static final String GRATICULE       = "GRATICULE";
   public static final String TYPE            = "TYPE";
   public static final String INTERVALY       = "INTERVALY";
   public static final String INTERVALX       = "INTERVALX";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Create_graticule"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      final String[] sOptions = { Sextante.getText("Rectangles"), Sextante.getText("Lines"), Sextante.getText("Points") };

      try {
         m_Parameters.addNumericalValue(INTERVALX, Sextante.getText("X_interval"), 1,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(INTERVALY, Sextante.getText("Y_interval"), 1,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addSelection(TYPE, Sextante.getText("Type"), sOptions);
         addOutputVectorLayer(GRATICULE, Sextante.getText("Graticule"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      double x, y;
      int i = 0;
      int iCountX, iCountY;
      int iID = 0;
      int iShapeType;
      final String[] sNames = { "ID", "X", "Y", "BORDER" };
      final Class[] types = { Integer.class, Double.class, Double.class, Integer.class };

      final double dXMax = m_AnalysisExtent.getXMax();
      final double dXMin = m_AnalysisExtent.getXMin();
      final double dYMax = m_AnalysisExtent.getYMax();
      final double dYMin = m_AnalysisExtent.getYMin();

      final double dIntervalX = m_Parameters.getParameterValueAsDouble(INTERVALX);
      final double dIntervalY = m_Parameters.getParameterValueAsDouble(INTERVALY);
      final int iType = m_Parameters.getParameterValueAsInt(TYPE);

      iCountX = (int) ((dXMax - dXMin) / dIntervalX);
      iCountY = (int) ((dYMax - dYMin) / dIntervalY);

      switch (iType) {
         case TYPE_RECTANGLES:
            iShapeType = IVectorLayer.SHAPE_TYPE_POLYGON;
            break;
         case TYPE_LINES:
            iShapeType = IVectorLayer.SHAPE_TYPE_LINE;
            iCountX *= 2;
            break;
         case TYPE_POINTS:
         default:
            iShapeType = IVectorLayer.SHAPE_TYPE_POINT;
            break;
      }

      final IVectorLayer output = getNewVectorLayer(GRATICULE, Sextante.getText("Graticule"), iShapeType, types, sNames);
      final Object[] value = new Object[4];
      final GeometryFactory gf = new GeometryFactory();
      switch (iType) {
         case TYPE_RECTANGLES:
            Geometry geom;
            for (x = dXMin; (x < dXMax - dIntervalX) & setProgress(i++, iCountX); x = x + dIntervalX) {
               for (y = dYMin; y < dYMax - dIntervalY; y = y + dIntervalY) {
                  final Coordinate[] coords = new Coordinate[5];
                  coords[0] = new Coordinate(x, y);
                  coords[1] = new Coordinate(x, y + dIntervalY);
                  coords[2] = new Coordinate(x + dIntervalX, y + dIntervalY);
                  coords[3] = new Coordinate(x + dIntervalX, y);
                  coords[4] = new Coordinate(x, y);
                  value[0] = new Integer(iID++);
                  value[1] = new Double(x);
                  value[2] = new Double(y);
                  if ((x == dXMin) || (x + dIntervalX >= dXMax - dIntervalX) || (y == dYMin)
                      || (y + dIntervalY >= dYMax - dIntervalY)) {
                     value[3] = new Integer(1);
                  }
                  else {
                     value[3] = new Integer(0);
                  }
                  final LinearRing ring = gf.createLinearRing(coords);
                  geom = gf.createPolygon(ring, null);
                  output.addFeature(geom, value);
               }
            }
            break;
         case TYPE_LINES:
            Geometry line;

            for (x = dXMin; (x <= dXMax) && setProgress(i++, iCountX); x = x + dIntervalX) {
               final Coordinate[] coords = new Coordinate[2];
               coords[0] = new Coordinate(x, dYMin);
               coords[1] = new Coordinate(x, dYMax);
               line = gf.createLineString(coords);
               value[0] = new Integer(iID++);
               value[1] = new Double(x);
               value[2] = new Double(dYMin);
               if ((x == dXMin) || (x + dIntervalX > dXMax - dIntervalX)) {
                  value[3] = new Integer(1);
               }
               else {
                  value[3] = new Integer(0);
               }
               output.addFeature(line, value);
            }
            i = 0;
            for (y = dYMin; (y <= dYMax) && setProgress(i++, iCountY); y = y + dIntervalY) {
               final Coordinate[] coords = new Coordinate[2];
               coords[0] = new Coordinate(dXMin, y);
               coords[1] = new Coordinate(dXMax, y);
               line = gf.createLineString(coords);
               value[0] = new Integer(iID++);
               value[1] = new Double(dXMin);
               value[2] = new Double(y);
               if ((y == dYMin) || (y + dIntervalY > dYMax - dIntervalY)) {
                  value[3] = new Integer(1);
               }
               else {
                  value[3] = new Integer(0);
               }
               output.addFeature(line, value);
            }
            break;
         case TYPE_POINTS:
            Geometry point;
            for (x = dXMin; (x <= dXMax) && setProgress(i++, iCountX); x = x + dIntervalX) {
               for (y = dYMin; y <= dYMax; y = y + dIntervalY) {
                  point = gf.createPoint(new Coordinate(x, y));
                  value[0] = new Integer(iID++);
                  value[1] = new Double(x);
                  value[2] = new Double(y);
                  if ((x == dXMin) || (x + dIntervalX > dXMax) || (y == dYMin) || (y + dIntervalY > dYMax)) {
                     value[3] = new Integer(1);
                  }
                  else {
                     value[3] = new Integer(0);
                  }
                  output.addFeature(point, value);
               }
            }
            break;
      }

      return !m_Task.isCanceled();

   }


}
