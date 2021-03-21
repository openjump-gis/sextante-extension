

package es.unex.sextante.vectorTools.simplifyPolygons;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

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


public class SimplifyPolygonsAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT    = "RESULT";
   public static final String PRESERVE  = "PRESERVE";
   public static final String TOLERANCE = "TOLERANCE";
   public static final String LAYER     = "LAYER";
   private boolean            m_bPreserve;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      final double dTolerance = m_Parameters.getParameterValueAsDouble(TOLERANCE);
      m_bPreserve = m_Parameters.getParameterValueAsBoolean(PRESERVE);

      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final IVectorLayer output = getNewVectorLayer(RESULT, Sextante.getText("Simplified_polygons"),
               IVectorLayer.SHAPE_TYPE_POLYGON, layerIn.getFieldTypes(), layerIn.getFieldNames());
      final IFeatureIterator iter = layerIn.iterator();
      int i = 0;
      final int iCount = layerIn.getShapesCount();
      while (iter.hasNext() && setProgress(i, iCount)) {
         final IFeature feature = iter.next();
         final Geometry simpleGeom = getSimpleLine(feature.getGeometry(), dTolerance);
         output.addFeature(simpleGeom, feature.getRecord().getValues());
         i++;
      }


      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Simplify_polygons"));
      setGroup(Sextante.getText("Tools_for_polygon_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Polygons"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         m_Parameters.addNumericalValue(TOLERANCE, Sextante.getText("Tolerance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 10, 0, Double.MAX_VALUE);
         m_Parameters.addBoolean(PRESERVE, Sextante.getText("Preserve_topology"), false);
         addOutputVectorLayer(RESULT, Sextante.getText("Simplified_polygons"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private Geometry getSimpleLine(final Geometry geometry,
                                  final double dTolerance) {

      Geometry simple;
      if (m_bPreserve) {
         simple = TopologyPreservingSimplifier.simplify(geometry, dTolerance);
      }
      else {
         simple = DouglasPeuckerSimplifier.simplify(geometry, dTolerance);
      }
      return simple;

   }

}
