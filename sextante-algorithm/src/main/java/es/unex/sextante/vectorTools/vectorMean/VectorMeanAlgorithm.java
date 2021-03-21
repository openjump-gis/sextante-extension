

package es.unex.sextante.vectorTools.vectorMean;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;


public class VectorMeanAlgorithm
         extends
            GeoAlgorithm {

   public static final String LINES  = "LINES";
   public static final String RESULT = "RESULT";

   private IVectorLayer       m_Driver;
   private double             m_dSumX = 0, m_dSumY = 0;
   private double             m_dSumDifX = 0, m_dSumDifY = 0;
   private double             m_dAbsSumDifX = 0, m_dAbsSumDifY = 0;
   private int                m_iCount      = 0;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      double x, y, x2, y2;
      double dAngle, dLength, dTotalLength;
      final String sFields[] = { Sextante.getText("Mean_distance"), Sextante.getText("Angle"),
               Sextante.getText("Circular_variance") };
      final Class types[] = { Double.class, Double.class, Double.class };

      final IVectorLayer lines = m_Parameters.getParameterValueAsVectorLayer(LINES);
      if (!m_bIsAutoExtent) {
         lines.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      m_Driver = getNewVectorLayer(RESULT, Sextante.getText("Vector_mean"), IVectorLayer.SHAPE_TYPE_LINE, types, sFields);
      i = 0;
      final int iCount = lines.getShapesCount();
      final IFeatureIterator iter = lines.iterator();
      while (iter.hasNext() && setProgress(i, iCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         for (int j = 0; j < geom.getNumGeometries(); j++) {
            final Geometry subgeom = geom.getGeometryN(j);
            processLine(subgeom.getCoordinates());
         }
         i++;
      }

      x = m_dSumX / (2. * m_iCount);
      y = m_dSumY / (2. * m_iCount);
      x2 = x + m_dSumDifX / m_iCount;
      y2 = y + m_dSumDifY / m_iCount;

      final Coordinate[] coords = new Coordinate[2];
      coords[0] = new Coordinate(x, y);
      coords[1] = new Coordinate(x2, y2);
      dLength = Math.sqrt(Math.pow(x2 - x, 2) + Math.pow(y2 - y, 2));
      dTotalLength = Math.sqrt(Math.pow(m_dAbsSumDifX / m_iCount, 2) + Math.pow(m_dAbsSumDifY / m_iCount, 2));
      dAngle = Math.atan2(y2 - y, x2 - x) / Math.PI * 180.;
      final Object[] value = new Object[3];
      value[0] = new Double(dLength);
      value[1] = new Double(dAngle);
      value[2] = new Double(1 - dLength / dTotalLength);

      final GeometryFactory gf = new GeometryFactory();
      m_Driver.addFeature(gf.createLineString(coords), value);

      return !m_Task.isCanceled();

   }


   private void processLine(final Coordinate[] coords) {

      double x, y, x2, y2;

      if (coords.length == 0) {
         return;
      }

      x = coords[0].x;
      y = coords[0].y;
      m_dSumX += x;
      m_dSumY += y;
      x2 = coords[coords.length - 1].x;
      y2 = coords[coords.length - 1].y;
      m_dSumX += x2;
      m_dSumY += y2;
      m_dSumDifX += (x2 - x);
      m_dSumDifY += (y2 - y);
      m_dAbsSumDifX += Math.abs(x2 - x);
      m_dAbsSumDifY += Math.abs(y2 - y);

      m_iCount++;

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Directional_mean"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LINES, Sextante.getText("Lines"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Directional_mean"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
