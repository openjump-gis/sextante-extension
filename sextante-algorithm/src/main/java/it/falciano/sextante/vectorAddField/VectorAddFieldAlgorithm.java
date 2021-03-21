

package it.falciano.sextante.vectorAddField;

import java.math.BigDecimal;

import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;


public class VectorAddFieldAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT                  = "INPUT";
   public static final String FIELD_NAME             = "FIELD_NAME";
   public static final String FIELD_TYPE             = "FIELD_TYPE";
   public static final String FIELD_LENGTH           = "FIELD_LENGTH";
   public static final String FIELD_PRECISION        = "FIELD_PRECISION";
   public static final String DEFAULT_VALUE          = "DEFAULT_VALUE";
   public static final String RESULT                 = "RESULT";

   public static final int    TYPE_INT               = 0;
   public static final int    TYPE_DOUBLE            = 1;
   public static final int    TYPE_STRING            = 2;

   //	check these values on http://en.wikipedia.org/wiki/Shapefile#Data_storage
   //	  * Incapable of storing null values (this is a serious issue for quantitative data, as it may skew representation and statistics as null quantities are often represented with 0)
   //	  * Poor support for Unicode field names or field storage
   //	  * Maximum length of field names is 10 characters
   //	  * Maximum number of fields is 255
   //	  * Supported field types are: floating point (13 character storage), integer (4 or 9 character storage), date (no time storage; 8 character storage), and text (maximum 254 character storage)
   //	  * Floating point numbers may contain rounding errors since they are stored as text

   public static final double DEFAULT_FIELD_LENGTH   = 1;
   public static final double DEFAUL_FIELD_PRECISION = 0;
   public static final double MIN_FIELD_LENGTH       = 1;
   public static final double MIN_FIELD_PRECISION    = 0;
   // TODO: MAX_FIELD_LENGTH depends by field type
   public static final double MAX_FIELD_LENGTH       = 254;
   public static final double MAX_FIELD_PRECISION    = 20;


   @Override
   public void defineCharacteristics() {

      this.setName(Sextante.getText("Add_field"));
      this.setGroup(Sextante.getText("Tools_for_vector_layers"));
      this.setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputVectorLayer(INPUT, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addString(FIELD_NAME, Sextante.getText("Field_name"));
         final String[] sTypes = { "Integer", "Double", "String" };
         m_Parameters.addSelection(FIELD_TYPE, Sextante.getText("Field_type"), sTypes);
         m_Parameters.addNumericalValue(FIELD_LENGTH, "Field length", AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER,
                  DEFAULT_FIELD_LENGTH, MIN_FIELD_LENGTH, MAX_FIELD_LENGTH);
         m_Parameters.addNumericalValue(FIELD_PRECISION, Sextante.getText("Field_precision_Double_only"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, DEFAUL_FIELD_PRECISION, MIN_FIELD_PRECISION,
                  MAX_FIELD_PRECISION);
         m_Parameters.addString(DEFAULT_VALUE, Sextante.getText("Default_value"));
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_UNDEFINED, INPUT);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   @SuppressWarnings("unchecked")
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer layer = m_Parameters.getParameterValueAsVectorLayer(INPUT);
      final String fieldName = m_Parameters.getParameterValueAsString(FIELD_NAME);
      final int iFieldType = m_Parameters.getParameterValueAsInt(FIELD_TYPE);

      final int iFieldLength = m_Parameters.getParameterValueAsInt(FIELD_LENGTH);
      final int iFieldPrecision = m_Parameters.getParameterValueAsInt(FIELD_PRECISION);
      String sDefaultValue;
      try {
         if (iFieldType == TYPE_DOUBLE) {
            sDefaultValue = m_Parameters.getParameterValueAsString(DEFAULT_VALUE).substring(0, iFieldLength + 1);
         }
         else {
            sDefaultValue = m_Parameters.getParameterValueAsString(DEFAULT_VALUE).substring(0, iFieldLength);
         }
      }
      catch (final StringIndexOutOfBoundsException e) {
         sDefaultValue = m_Parameters.getParameterValueAsString(DEFAULT_VALUE);
         Sextante.addErrorToLog(e);
      }

      final Class[] inputFieldTypes = layer.getFieldTypes();
      final String[] inputFieldNames = layer.getFieldNames();
      final Class[] outputFieldTypes = new Class[inputFieldTypes.length + 1];
      final String[] outputFieldNames = new String[inputFieldTypes.length + 1];

      for (int i = 0; i < inputFieldTypes.length; i++) {
         outputFieldTypes[i] = inputFieldTypes[i];
         outputFieldNames[i] = inputFieldNames[i];
      }
      switch (iFieldType) {
         case 0:
            outputFieldTypes[inputFieldTypes.length] = Integer.class;
            break;
         case 1:
            outputFieldTypes[inputFieldTypes.length] = Double.class;
            break;
         case 2:
            outputFieldTypes[inputFieldTypes.length] = String.class;
            break;
         default:
            break;
      }
      outputFieldNames[inputFieldTypes.length] = fieldName;

      final IVectorLayer output = getNewVectorLayer(RESULT, layer.getName(), layer.getShapeType(), outputFieldTypes,
               outputFieldNames);

      int j = 0;
      final int iShapeCount = layer.getShapesCount();
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(j, iShapeCount)) {
         final IFeature feature = iter.next();
         final Object[] inputValues = feature.getRecord().getValues();
         final Object[] outputValues = new Object[inputValues.length + 1];
         for (int i = 0; i < inputFieldTypes.length; i++) {
            if (inputValues[i] == null) {
               outputValues[i] = null;
            }
            else {
               outputValues[i] = inputValues[i];
            }
         }
         // edit the defaultValue
         if (sDefaultValue == null) {
            outputValues[inputFieldTypes.length] = null;
         }
         else {
            try {
               switch (iFieldType) {
                  case 0:
                     final Integer iDefaultValue = new Integer(Integer.parseInt(sDefaultValue));
                     outputValues[inputFieldTypes.length] = iDefaultValue;
                     break;
                  case 1:
                     final BigDecimal roundDefaultValue = new BigDecimal(Double.parseDouble(sDefaultValue)).setScale(
                              iFieldPrecision, BigDecimal.ROUND_HALF_EVEN);
                     final Double dDefaultValue = new Double(roundDefaultValue.doubleValue());
                     outputValues[inputFieldTypes.length] = dDefaultValue;
                     break;
                  case 2:
                     outputValues[inputFieldTypes.length] = sDefaultValue;
                     break;
                  default:
                     break;
               }
            }
            catch (final NumberFormatException e) {
               Sextante.addErrorToLog(e);
            }
         }
         final Geometry geom = feature.getGeometry();
         output.addFeature(geom, outputValues);
         j++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }

}
