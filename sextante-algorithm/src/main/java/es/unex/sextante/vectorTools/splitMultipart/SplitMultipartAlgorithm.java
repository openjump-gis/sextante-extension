

package es.unex.sextante.vectorTools.splitMultipart;

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


public class SplitMultipartAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT  = "INPUT";
   public static final String RESULT = "RESULT";

   private IVectorLayer       m_Output;
   private IVectorLayer       m_Input;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Separate_multi-part_features"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(INPUT, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_UNDEFINED, INPUT);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;

      m_Input = m_Parameters.getParameterValueAsVectorLayer(INPUT);
      if (!m_bIsAutoExtent) {
         m_Input.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      m_Output = getNewVectorLayer(RESULT, Sextante.getText("Result"), m_Input.getShapeType(), m_Input.getFieldTypes(),
               m_Input.getFieldNames());

      if (m_Input.getShapesCount() == 0) {
         return false;
      }

      i = 0;
      final int iShapeCount = m_Input.getShapesCount();
      final IFeatureIterator iter = m_Input.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Object[] values = feature.getRecord().getValues();
         for (int j = 0; j < geom.getNumGeometries(); j++) {
            final Geometry subgeom = geom.getGeometryN(j);
            m_Output.addFeature(subgeom, values);
         }
      }

      return !m_Task.isCanceled();

   }

}
