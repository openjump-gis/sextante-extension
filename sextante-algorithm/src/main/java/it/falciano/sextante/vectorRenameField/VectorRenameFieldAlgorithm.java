package it.falciano.sextante.vectorRenameField;

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

public class VectorRenameFieldAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT           = "INPUT";
   public static final String FIELD_TO_RENAME = "FIELD_TO_RENAME";
   public static final String NEW_FIELD_NAME  = "NEW_FIELD_NAME";
   public static final String RESULT          = "RESULT";


   @Override
   public void defineCharacteristics() {

      this.setName(Sextante.getText("Rename_field"));
      this.setGroup(Sextante.getText("Tools_for_vector_layers"));
      this.setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputVectorLayer(INPUT, Sextante.getText("Vector layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD_TO_RENAME, Sextante.getText("field_to_rename"), "INPUT");
         m_Parameters.addString(NEW_FIELD_NAME, Sextante.getText("name_for_new_field"), "");
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
      final int iField = m_Parameters.getParameterValueAsInt(FIELD_TO_RENAME);
      final String sNewFieldName = m_Parameters.getParameterValueAsString(NEW_FIELD_NAME);

      if (sNewFieldName.length() == 0) {
         final String s = Sextante.getText("New_field_name_is_empty_Processing_aborted!");
         Sextante.addErrorToLog(s);
         throw new GeoAlgorithmExecutionException(s);
      }
      else if (sNewFieldName.length() > 10) {
         final String s = Sextante.getText("The_length_of_new_field_name_is_over_10_characters_Processing_aborted!");
         Sextante.addErrorToLog(s);
         throw new GeoAlgorithmExecutionException(s);
      }

      final Class[] inputFieldTypes = layer.getFieldTypes();
      final String[] inputFieldNames = layer.getFieldNames();
      final Class[] outputFieldTypes = new Class[inputFieldTypes.length];
      final String[] outputFieldNames = new String[inputFieldTypes.length];

      for (int i = 0; i < inputFieldTypes.length; i++) {
         outputFieldTypes[i] = inputFieldTypes[i];
         if (i != iField) {
            outputFieldNames[i] = inputFieldNames[i];
         }
         else {
            outputFieldNames[i] = sNewFieldName;
         }
      }

      final IVectorLayer output = getNewVectorLayer("RESULT", layer.getName(), layer.getShapeType(), outputFieldTypes,
               outputFieldNames);

      final int j = 0;
      final int iShapeCount = layer.getShapesCount();
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(j, iShapeCount)) {
         final IFeature feature = iter.next();
         final Object[] inputValues = feature.getRecord().getValues();
         final Object[] outputValues = new Object[inputValues.length];
         for (int i = 0; i < inputFieldTypes.length; i++) {
            outputValues[i] = inputValues[i];
         }
         final Geometry geom = feature.getGeometry();
         output.addFeature(geom, outputValues);
      }
      iter.close();

      return !m_Task.isCanceled();
   }

}
