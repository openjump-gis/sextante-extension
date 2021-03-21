package es.unex.sextante.vectorTools.autoincrementValue;

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

public class AutoincrementValueAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String FIELD  = "FIELD";
   public static final String RESULT = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i = 0;

      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      final int iField = m_Parameters.getParameterValueAsInt(FIELD);
      final IVectorLayer result = getNewVectorLayer(RESULT, Sextante.getText("Autoincrement_Value"), layerIn.getShapeType(),
               layerIn.getFieldTypes(), layerIn.getFieldNames());

      final IFeatureIterator iter = layerIn.iterator();
      final int iTotal = layerIn.getShapesCount();
      while (iter.hasNext() && setProgress(i, iTotal)) {
         final IFeature feature = iter.next();
         final IRecord record = feature.getRecord();
         final Object[] values = record.getValues();
         values[iField] = i;
         result.addFeature(feature.getGeometry(), values);
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Autoincrement_Value"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Input_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Attribute"), LAYER);
         addOutputVectorLayer(RESULT, Sextante.getText("Autoincrement_Value"), OutputVectorLayer.SHAPE_TYPE_UNDEFINED);
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
