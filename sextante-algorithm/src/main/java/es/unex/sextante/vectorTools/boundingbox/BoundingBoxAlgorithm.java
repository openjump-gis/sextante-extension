

package es.unex.sextante.vectorTools.boundingbox;

import org.locationtech.jts.geom.Geometry;

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


public class BoundingBoxAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i = 0;

      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final IVectorLayer result = getNewVectorLayer(RESULT, Sextante.getText("Bounding Box"), IVectorLayer.SHAPE_TYPE_POLYGON,
               layerIn.getFieldTypes(), layerIn.getFieldNames());

      final IFeatureIterator iter = layerIn.iterator();
      final int iTotal = layerIn.getShapesCount();
      while (iter.hasNext() && setProgress(i, iTotal)) {
         final IFeature feature = iter.next();
         final Geometry bbox = getBBox(feature.getGeometry());
         result.addFeature(bbox, feature.getRecord().getValues());
         setProgress(i, iTotal);
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Bounding_Box"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Bounding_Box"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private Geometry getBBox(final Geometry geometry) {

      return geometry.getEnvelope();

   }

}
