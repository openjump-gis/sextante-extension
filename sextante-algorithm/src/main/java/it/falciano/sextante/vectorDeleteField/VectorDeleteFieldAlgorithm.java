package it.falciano.sextante.vectorDeleteField;

import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class VectorDeleteFieldAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT  = "INPUT";
   public static final String FIELD  = "FIELD";
   public static final String RESULT = "RESULT";


   @Override
   public void defineCharacteristics() {

      this.setName(Sextante.getText("Delete_field"));
      this.setGroup(Sextante.getText("Tools_for_vector_layers"));
      this.setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputVectorLayer(INPUT, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), INPUT);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_UNDEFINED, INPUT);
      }
      catch (final RepeatedParameterNameException e) {
         e.printStackTrace();
      }
      catch (final UndefinedParentParameterNameException e) {
         e.printStackTrace();
      }
      catch (final OptionalParentParameterException e) {
         e.printStackTrace();
      }

   }


   @Override
   @SuppressWarnings("unchecked")
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer layer = m_Parameters.getParameterValueAsVectorLayer(INPUT);
      final int iField = m_Parameters.getParameterValueAsInt(FIELD);

      final Class[] inputFieldTypes = layer.getFieldTypes();
      final String[] inputFieldNames = layer.getFieldNames();
      final Class[] outputFieldTypes = new Class[inputFieldTypes.length - 1];
      final String[] outputFieldNames = new String[inputFieldTypes.length - 1];

      int j = 0;
      for (int i = 0; i < inputFieldTypes.length; i++) {
         if (i != iField) {
            outputFieldTypes[j] = inputFieldTypes[i];
            outputFieldNames[j] = inputFieldNames[i];
            j++;
         }
      }

      final IVectorLayer output = getNewVectorLayer(RESULT, layer.getName(), layer.getShapeType(), outputFieldTypes,
               outputFieldNames);

      final int k = 0;
      final int iShapeCount = layer.getShapesCount();
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(k, iShapeCount)) {
         final IFeature feature = iter.next();
         final Object[] inputValues = feature.getRecord().getValues();
         final Object[] outputValues = new Object[inputValues.length - 1];
         j = 0;
         for (int i = 0; i < inputFieldTypes.length; i++) {
            if (i != iField) {
               if (inputValues[i] == null) {
                  outputValues[j] = null;
               }
               else {
                  outputValues[j] = inputValues[i];
               }
               j++;
            }
         }
         final Geometry geom = feature.getGeometry();
         output.addFeature(geom, outputValues);
      }
      iter.close();

      return !m_Task.isCanceled();
   }

}
