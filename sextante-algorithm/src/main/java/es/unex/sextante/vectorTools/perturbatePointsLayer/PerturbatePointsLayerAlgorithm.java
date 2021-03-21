

package es.unex.sextante.vectorTools.perturbatePointsLayer;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
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


public class PerturbatePointsLayerAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String MEAN   = "MEAN";
   public static final String STDDEV = "STDDEV";
   public static final String RESULT = "RESULT";

   private IVectorLayer       m_Layer;
   private IVectorLayer       m_Output;
   private double             m_dMean;
   private double             m_dStdDev;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Perturbate_points_layer"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Points_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);
         m_Parameters.addNumericalValue(MEAN, Sextante.getText("Mean"), AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 10,
                  0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(STDDEV, Sextante.getText("Standard_deviation"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, 0, Double.MAX_VALUE);

         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POINT);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      m_dMean = m_Parameters.getParameterValueAsDouble(MEAN);
      m_dStdDev = m_Parameters.getParameterValueAsDouble(STDDEV);

      if (!m_bIsAutoExtent) {
         m_Layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      m_Output = getNewVectorLayer(RESULT, m_Layer.getName(), m_Layer.getShapeType(), m_Layer.getFieldTypes(),
               m_Layer.getFieldNames());

      i = 0;
      iShapeCount = m_Layer.getShapesCount();
      final IFeatureIterator iter = m_Layer.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate coord = geom.getCoordinate();
         addPoint(coord.x, coord.y, feature.getRecord().getValues());
         i++;
      }

      iter.close();

      return !m_Task.isCanceled();

   }


   private void addPoint(final double x,
                         final double y,
                         final Object[] values) {

      final Point pt = getAlteredPoint(x, y);
      m_Output.addFeature(pt, values);

   }


   private Point getAlteredPoint(final double x,
                                 final double y) {

      final GeometryFactory gf = new GeometryFactory();

      final double dDist = getDist();
      final double dAngle = Math.random() * Math.PI * 2.;

      return gf.createPoint(new Coordinate(x + Math.cos(dAngle) * dDist, y + Math.sin(dAngle) * dDist));

   }


   double getDist() {


      double x1, x2, w, y1;

      do {
         x1 = 2.0 * Math.random() - 1.0;
         x2 = 2.0 * Math.random() - 1.0;

         w = x1 * x1 + x2 * x2;
      }
      while (w >= 1.0);

      w = Math.sqrt((-2.0 * Math.log(w)) / w);

      y1 = x1 * w;

      return (m_dMean + m_dStdDev * y1);

   }

}
