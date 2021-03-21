package es.unex.sextante.vectorTools.cleanVectorLayer;


import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class CleanVectorLayerAlgorithm
         extends
            GeoAlgorithm {

   private static final String LAYER  = "LAYER";
   private static final String RESULT = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int iWrongShapes = 0;
      final IVectorLayer layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);

      final IVectorLayer output = getNewVectorLayer(RESULT, Sextante.getText("Result"), layer.getShapeType(),
               layer.getFieldTypes(), layer.getFieldNames());
      final int iShapeCount = layer.getShapesCount();
      int i = 0;
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         try {
            final IFeature feature = iter.next();
            output.addFeature(feature);
         }
         catch (final Exception e) {
            iWrongShapes++;
         }
         i++;
      }

      String sInfo = Sextante.getText("Eliminados_N_Registros_erroneos)");
      sInfo = sInfo.replace("XXX", Integer.toString(iWrongShapes));
      Sextante.addInfoToLog(sInfo);

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Clean_vector_layer"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_UNDEFINED, LAYER);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
