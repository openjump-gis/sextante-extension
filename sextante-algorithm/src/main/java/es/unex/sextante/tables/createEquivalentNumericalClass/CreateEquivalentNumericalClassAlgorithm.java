package es.unex.sextante.tables.createEquivalentNumericalClass;

import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class CreateEquivalentNumericalClassAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER          = "LAYER";
   public static final String NEW_FIELD_NAME = "NEW_FIELD_NAME";
   public static final String CLASS_FIELD    = "CLASS_FIELD";
   public static final String RESULT         = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final ArrayList<String> list = new ArrayList<String>();

      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      final int iField = m_Parameters.getParameterValueAsInt(CLASS_FIELD);
      final Class[] types = new Class[layerIn.getFieldCount() + 1];
      System.arraycopy(layerIn.getFieldTypes(), 0, types, 0, layerIn.getFieldCount());
      types[types.length - 1] = Integer.class;
      final String[] sFields = new String[layerIn.getFieldCount() + 1];
      System.arraycopy(layerIn.getFieldNames(), 0, sFields, 0, layerIn.getFieldCount());
      sFields[types.length - 1] = m_Parameters.getParameterValueAsString(NEW_FIELD_NAME);

      final IVectorLayer driver = getNewVectorLayer(RESULT, layerIn.getName(), layerIn.getShapeType(), types, sFields);

      final IFeatureIterator iter = layerIn.iterator();
      final int iTotal = layerIn.getShapesCount();
      int i = 0;
      while (iter.hasNext() && setProgress(i, iTotal)) {
         final IFeature feature = iter.next();
         final IRecord record = feature.getRecord();
         final Object[] values = record.getValues();
         final Object[] newValues = new Object[values.length + 1];
         System.arraycopy(values, 0, newValues, 0, values.length);
         int iClass = list.indexOf(values[iField].toString());
         if (iClass == -1) {
            iClass = list.size();
            list.add(values[iField].toString());
         }
         newValues[values.length] = new Integer(iClass);
         driver.addFeature(feature.getGeometry(), newValues);
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Create_equivalent_numerical_class"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Input_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addString(NEW_FIELD_NAME, Sextante.getText("name_for_new_field"), "CLASS_ID");
         m_Parameters.addTableField(CLASS_FIELD, Sextante.getText("Class_field"), LAYER);

         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_UNDEFINED);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final UndefinedParentParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final OptionalParentParameterException e) {
         Sextante.addErrorToLog(e);
      }
   }

}
