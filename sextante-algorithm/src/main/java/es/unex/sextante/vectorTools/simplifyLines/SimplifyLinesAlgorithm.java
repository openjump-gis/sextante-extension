

package es.unex.sextante.vectorTools.simplifyLines;

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


public class SimplifyLinesAlgorithm
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

      final IVectorLayer output = getNewVectorLayer(RESULT, Sextante.getText("Generalized_lines"), IVectorLayer.SHAPE_TYPE_LINE,
               layerIn.getFieldTypes(), layerIn.getFieldNames());
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

      setName(Sextante.getText("Simplify_lines"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Lines"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         m_Parameters.addNumericalValue(TOLERANCE, Sextante.getText("Tolerance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 10, 0, Double.MAX_VALUE);
         m_Parameters.addBoolean(PRESERVE, Sextante.getText("Preserve_topology"), false);
         addOutputVectorLayer(RESULT, Sextante.getText("Generalized_lines"), OutputVectorLayer.SHAPE_TYPE_LINE);
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
