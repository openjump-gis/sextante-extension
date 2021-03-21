

package es.unex.sextante.vectorTools.exportVector;


import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.Output;


public class ExportVectorAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      Output out = m_OutputObjects.getOutput(RESULT);
      final String sFilename = ((FileOutputChannel) out.getOutputChannel()).getFilename();

      if (!m_bIsAutoExtent) {
         layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      if (sFilename != null) {
         final IVectorLayer output = getNewVectorLayer(RESULT, Sextante.getText("Result"), layer.getShapeType(),
                  layer.getFieldTypes(), layer.getFieldNames());
         final int iShapeCount = layer.getShapesCount();
         int i = 0;
         final IFeatureIterator iter = layer.iterator();
         while (iter.hasNext() && setProgress(i, iShapeCount)) {
            final IFeature feature = iter.next();
            output.addFeature(feature);
            i++;
         }
         try {
            output.postProcess();
         }
         catch (final Exception e) {
            throw new GeoAlgorithmExecutionException(e.getMessage());
         }
      }

      //We do not want the result to be added to the view
      out = m_OutputObjects.getOutput(RESULT);
      out.setOutputObject(null);

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Export_vector_layer"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean isSuitableForModelling() {

      return false;

   }


}
