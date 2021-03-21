package es.unex.sextante.gridTools.gradientLines;

import java.awt.geom.Point2D;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class GradientLinesAlgorithm
         extends
            GeoAlgorithm {

   public static final String LINES = "LINES";
   public static final String MAX   = "MAX";
   public static final String MIN   = "MIN";
   public static final String SKIP  = "SKIP";
   public static final String INPUT = "INPUT";

   private int                m_iNX, m_iNY;
   private IRasterLayer       m_Input;
   private IVectorLayer       m_Lines;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Gradient_lines"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         m_Parameters.addNumericalValue(MIN, Sextante.getText("Minimum_size"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1.0, 0.0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(MAX, Sextante.getText("Maximum_size"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 10., 0.0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(SKIP, Sextante.getText("Interval"), AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER,
                  1, 0.0, Integer.MAX_VALUE);
         addOutputVectorLayer(LINES, Sextante.getText("Gradient_lines"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iSkip;
      double dSlope, dAspect;
      double dMinSize;
      double dMaxSize;
      double dRange;
      double dMin = Double.MAX_VALUE;
      double dMax = Double.NEGATIVE_INFINITY;
      final Object[] values = new Object[2];
      final String sFields[] = { Sextante.getText("Slope"), Sextante.getText("Aspect") };
      final Class[] types = { Double.class, Double.class };

      m_Input = m_Parameters.getParameterValueAsRasterLayer(INPUT);
      m_Input.setFullExtent();

      iSkip = m_Parameters.getParameterValueAsInt(SKIP);
      dMinSize = m_Parameters.getParameterValueAsDouble(MIN);
      dMaxSize = m_Parameters.getParameterValueAsDouble(MAX);
      dRange = dMaxSize - dMinSize;
      m_Lines = getNewVectorLayer(LINES, m_Input.getName() + Sextante.getText("[gradient]"), IVectorLayer.SHAPE_TYPE_LINE, types,
               sFields);

      m_iNX = m_Input.getNX();
      m_iNY = m_Input.getNY();


      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y += iSkip) {
         for (x = 0; x < m_iNX; x += iSkip) {
            dSlope = m_Input.getSlope(x, y);
            if (!m_Input.isNoDataValue(dSlope)) {
               dMin = Math.min(dSlope, dMin);
               dMax = Math.max(dSlope, dMax);
            }
         }
      }

      if (dMin < dMax) {
         dRange = dRange / (dMax - dMin);
      }
      final GeometryFactory gf = new GeometryFactory();
      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y += iSkip) {
         for (x = 0; x < m_iNX; x += iSkip) {
            dSlope = m_Input.getSlope(x, y);
            dAspect = m_Input.getAspect(x, y);
            if (!m_Input.isNoDataValue(dSlope) && !m_Input.isNoDataValue(dAspect)) {
               values[0] = new Double(dSlope);
               values[1] = new Double(dAspect);
               dSlope = dMin + dRange * (dSlope - dMin);
               final Coordinate coords[] = new Coordinate[2];
               final Point2D pt = m_Input.getWindowGridExtent().getWorldCoordsFromGridCoords(x, y);
               coords[0] = new Coordinate(pt.getX(), pt.getY());
               final double dX = pt.getX() + Math.sin(dAspect) * dSlope;
               final double dY = pt.getY() + Math.cos(dAspect) * dSlope;
               coords[1] = new Coordinate(dX, dY);
               final Geometry line = gf.createLineString(coords);
               m_Lines.addFeature(line, values);
            }

         }
      }
      return !m_Task.isCanceled();

   }

}
