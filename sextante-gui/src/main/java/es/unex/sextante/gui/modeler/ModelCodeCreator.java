package es.unex.sextante.gui.modeler;

import es.unex.sextante.additionalInfo.AdditionalInfoBand;
import es.unex.sextante.additionalInfo.AdditionalInfoBoolean;
import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoRasterLayer;
import es.unex.sextante.additionalInfo.AdditionalInfoString;
import es.unex.sextante.additionalInfo.AdditionalInfoTable;
import es.unex.sextante.additionalInfo.AdditionalInfoTableField;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterBand;
import es.unex.sextante.parameters.ParameterBoolean;
import es.unex.sextante.parameters.ParameterFixedTable;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterNumericalValue;
import es.unex.sextante.parameters.ParameterPoint;
import es.unex.sextante.parameters.ParameterRasterLayer;
import es.unex.sextante.parameters.ParameterString;
import es.unex.sextante.parameters.ParameterTable;
import es.unex.sextante.parameters.ParameterTableField;
import es.unex.sextante.parameters.ParameterVectorLayer;

public class ModelCodeCreator {

   public static String getJavaCode(final ModelAlgorithm alg) {

      final StringBuffer code = new StringBuffer();

      addHeader(code, alg);
      addOpening(code, alg);
      addConstants(code, alg);
      addDefineCharacteristics(code, alg);
      addProcessAlgorithm(code, alg);

      addClosing(code, alg);

      return code.toString();


   }


   private static void addConstants(final StringBuffer code,
                                    final ModelAlgorithm alg) {

      final ParametersSet params = alg.getParameters();
      for (int i = 0; i < params.getNumberOfParameters(); i++) {
         final Parameter param = params.getParameter(i);
         final String sName = getParameterNameFromDescription(param);
         code.append("\tpublic static final String " + sName + "=\"" + sName + "\";\n");
      }

   }


   private static void addOpening(final StringBuffer code,
                                  final ModelAlgorithm alg) {

      code.append("\npublic class UnnamedAlgorithm extends  GeoAlgorithm {\n\n");

   }


   private static void addClosing(final StringBuffer code,
                                  final ModelAlgorithm alg) {

      code.append("}\n");

   }


   private static void addProcessAlgorithm(final StringBuffer code,
                                           final ModelAlgorithm alg) {

      code.append("\n\tpublic boolean processAlgorithm() {\n\n");

      String sAs = null;
      String sClass = null;
      final ParametersSet params = alg.getParameters();
      for (int i = 0; i < params.getNumberOfParameters(); i++) {
         final Parameter param = params.getParameter(i);
         if (param instanceof ParameterRasterLayer) {
            sAs = "RasterLayer";
            sClass = "IRasterLayer";
         }
         else if (param instanceof ParameterMultipleInput) {
            sAs = "ArrayList";
            sClass = "ArrayList";
         }
         else if (param instanceof ParameterVectorLayer) {
            sAs = "VectorLayer";
            sClass = "IVectorLayer";
         }
         else if (param instanceof ParameterTable) {
            sAs = "Table";
            sClass = "ITable";
         }
         else if (param instanceof ParameterNumericalValue) {
            sAs = "Double";
            sClass = "Double";
         }
         else if (param instanceof ParameterString) {
            sAs = "String";
            sClass = "String";

         }
         else if (param instanceof ParameterFixedTable) {
            sAs = "Object";
            sClass = "FixedTableModel";
            final String sName = getParameterNameFromDescription(param);
            code.append("\t\t" + sClass + " " + sName.toLowerCase() + " = (FixedTableModel) m_Parameters.getParameterValueAs"
                        + sAs + "(" + sName + ");\n");
            continue;
         }
         else if (param instanceof ParameterPoint) {
            sAs = "Point";
            sClass = "Point2D";
         }
         else if (param instanceof ParameterBoolean) {
            sAs = "Boolean";
            sClass = "Boolean";
         }

         final String sName = getParameterNameFromDescription(param);
         code.append("\t\t" + sClass + " " + sName.toLowerCase() + " = m_Parameters.getParameterValueAs" + sAs + "(" + sName
                     + ");\n");


      }


      code.append("\n\t\treturn !m_Task.isCanceled();\n");
      code.append("\t}\n\n");


   }


   private static void addParameters(final StringBuffer code,
                                     final ModelAlgorithm alg) {

      final StringBuffer dependentCode = new StringBuffer();
      final ParametersSet params = alg.getParameters();
      for (int i = 0; i < params.getNumberOfParameters(); i++) {
         final Parameter param = params.getParameter(i);
         if (param instanceof ParameterRasterLayer) {
            try {
               final AdditionalInfoRasterLayer ai = (AdditionalInfoRasterLayer) param.getParameterAdditionalInfo();
               code.append("\t\t\tm_Parameters.addInputRasterLayer(" + getParameterNameFromDescription(param) + ", \""
                           + param.getParameterDescription() + "\", " + new Boolean(ai.getIsMandatory()).toString() + ");\n");
            }
            catch (final NullParameterAdditionalInfoException e) {};
         }
         else if (param instanceof ParameterMultipleInput) {
            try {
               final AdditionalInfoMultipleInput ai = (AdditionalInfoMultipleInput) param.getParameterAdditionalInfo();
               String sType;
               switch (ai.getDataType()) {
                  case AdditionalInfoMultipleInput.DATA_TYPE_BAND:
                     sType = "AdditionalInfoMultipleInput.DATA_TYPE_BAND";
                     break;
                  case AdditionalInfoMultipleInput.DATA_TYPE_RASTER:
                     sType = "AdditionalInfoMultipleInput.DATA_TYPE_RASTER";
                     break;
                  case AdditionalInfoMultipleInput.DATA_TYPE_TABLE:
                     sType = "AdditionalInfoMultipleInput.DATA_TYPE_TABLE";
                     break;
                  case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY:
                     sType = "AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY";
                     break;
                  case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE:
                     sType = "AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE";
                     break;
                  case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT:
                     sType = "AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT";
                     break;
                  case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON:
                  default:
                     sType = "AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON";
                     break;
               }
               code.append("\t\t\tm_Parameters.addMultipleInput(" + getParameterNameFromDescription(param) + ", \""
                           + param.getParameterDescription() + "\", " + sType + "," + new Boolean(ai.getIsMandatory()).toString()
                           + ");\n");
            }
            catch (final NullParameterAdditionalInfoException e) {};
         }
         else if (param instanceof ParameterVectorLayer) {
            try {
               final AdditionalInfoVectorLayer ai = (AdditionalInfoVectorLayer) param.getParameterAdditionalInfo();
               String sType;
               switch (ai.getShapeType()) {
                  case AdditionalInfoVectorLayer.SHAPE_TYPE_ANY:
                     sType = "AdditionalInfoVectorLayer.SHAPE_TYPE_ANY";
                     break;
                  case AdditionalInfoVectorLayer.SHAPE_TYPE_LINE:
                     sType = "AdditionalInfoVectorLayer.SHAPE_TYPE_LINE";
                     break;
                  case AdditionalInfoVectorLayer.SHAPE_TYPE_POINT:
                     sType = "AdditionalInfoVectorLayer.SHAPE_TYPE_POINT";
                     break;
                  case AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON:
                  default:
                     sType = "AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON";
                     break;
               }
               code.append("\t\t\tm_Parameters.addInputVectorLayer(" + getParameterNameFromDescription(param) + ", \""
                           + param.getParameterDescription() + "\", " + sType + ", "
                           + new Boolean(ai.getIsMandatory()).toString() + ");\n");
            }
            catch (final NullParameterAdditionalInfoException e) {}

         }
         else if (param instanceof ParameterTable) {
            try {
               final AdditionalInfoTable ai = (AdditionalInfoTable) param.getParameterAdditionalInfo();
               code.append("\t\t\tm_Parameters.addInputTable(" + getParameterNameFromDescription(param) + ", \""
                           + param.getParameterDescription() + "\", " + new Boolean(ai.getIsMandatory()).toString() + ");\n");
            }
            catch (final NullParameterAdditionalInfoException e) {}
         }
         else if (param instanceof ParameterNumericalValue) {
            try {
               final AdditionalInfoNumericalValue ai = (AdditionalInfoNumericalValue) param.getParameterAdditionalInfo();
               String sType;
               switch (ai.getType()) {
                  case AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER:
                     sType = "AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER";
                     break;
                  default:
                     sType = "AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE";
               }
               code.append("\t\t\tm_Parameters.addNumericalValue(" + getParameterNameFromDescription(param) + ", \""
                           + param.getParameterDescription() + "\", " + sType + ", " + Double.toString(ai.getDefaultValue())
                           + ", " + Double.toString(ai.getMinValue()) + ", " + Double.toString(ai.getMaxValue()) + ");\n");
            }
            catch (final NullParameterAdditionalInfoException e) {}
         }
         else if (param instanceof ParameterString) {
            try {
               final AdditionalInfoString ai = (AdditionalInfoString) param.getParameterAdditionalInfo();
               code.append("\t\t\tm_Parameters.addString(" + getParameterNameFromDescription(param) + ", \""
                           + param.getParameterDescription() + "\", " + ai.getDefaultString().toString() + ");\n");
            }
            catch (final NullParameterAdditionalInfoException e) {}
         }
         else if (param instanceof ParameterFixedTable) {
            try {
               final AdditionalInfoFixedTable ai = (AdditionalInfoFixedTable) param.getParameterAdditionalInfo();
               code.append("\t\t\tm_Parameters.addFixedTable(" + getParameterNameFromDescription(param) + ", \""
                           + param.getParameterDescription() + "\", " + ai.getRowsCount() + ", " + ai.getColsCount() + ", "
                           + Boolean.toString(ai.isNumberOfRowsFixed()) + ");\n");
            }
            catch (final NullParameterAdditionalInfoException e) {}
         }
         else if (param instanceof ParameterPoint) {
            code.append("\t\t\tm_Parameters.addString(" + getParameterNameFromDescription(param) + ", \""
                        + param.getParameterDescription() + "\");\n");

         }
         else if (param instanceof ParameterBoolean) {
            try {
               final AdditionalInfoBoolean ai = (AdditionalInfoBoolean) param.getParameterAdditionalInfo();
               code.append("\t\t\tm_Parameters.addBoolean(" + getParameterNameFromDescription(param) + ", \""
                           + param.getParameterDescription() + "\", " + Boolean.toString(ai.getDefaultValue()) + ");\n");
            }
            catch (final NullParameterAdditionalInfoException e) {}
         }
         else if (param instanceof ParameterBand) {
            try {
               final AdditionalInfoBand ai = (AdditionalInfoBand) param.getParameterAdditionalInfo();
               dependentCode.append("\t\t\tm_Parameters.addBand(" + getParameterNameFromDescription(param) + ", \""
                                    + param.getParameterDescription() + "\", " + ai.getParentParameterName() + ");\n");
            }
            catch (final NullParameterAdditionalInfoException e) {}
         }
         else if (param instanceof ParameterTableField) {
            try {
               final AdditionalInfoTableField ai = (AdditionalInfoTableField) param.getParameterAdditionalInfo();
               dependentCode.append("\t\t\tm_Parameters.addTableField(" + getParameterNameFromDescription(param) + ", \""
                                    + param.getParameterDescription() + "\", " + ai.getParentParameterName() + ");\n");
            }
            catch (final NullParameterAdditionalInfoException e) {}
         }

      }

      code.append(dependentCode);

   }


   private static String getParameterNameFromDescription(final Parameter param) {

      final String sDesc = param.getParameterDescription();

      return sDesc.replaceAll(" ", "_").toUpperCase();

   }


   private static void addDefineCharacteristics(final StringBuffer code,
                                                final ModelAlgorithm alg) {

      code.append("\n\tpublic void defineCharacteristics() {\n\n");

      code.append("\t\tsetName(Sextante.getText(\"" + alg.getName() + "\");\n");
      code.append("\t\tsetGroup(Sextante.getText(\"" + alg.getGroup() + "\");\n");
      code.append("\n");
      code.append("\t\ttry{\n");
      addParameters(code, alg);
      code.append("\t\t}\n");
      code.append("\t\tcatch(Exception e){}\n");
      code.append("\t}\n\n");

   }


   private static void addHeader(final StringBuffer code,
                                 final ModelAlgorithm alg) {


   }

}
