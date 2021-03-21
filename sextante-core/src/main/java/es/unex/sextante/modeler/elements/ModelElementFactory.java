

package es.unex.sextante.modeler.elements;

import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterBand;
import es.unex.sextante.parameters.ParameterBoolean;
import es.unex.sextante.parameters.ParameterFixedTable;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterNumericalValue;
import es.unex.sextante.parameters.ParameterPoint;
import es.unex.sextante.parameters.ParameterRasterLayer;
import es.unex.sextante.parameters.ParameterSelection;
import es.unex.sextante.parameters.ParameterString;
import es.unex.sextante.parameters.ParameterTable;
import es.unex.sextante.parameters.ParameterTableField;
import es.unex.sextante.parameters.ParameterVectorLayer;


public class ModelElementFactory {

   public static IModelElement getParameterAsModelElement(final Parameter param) {

      IModelElement element = null;

      try {
         if (param instanceof ParameterRasterLayer) {
            element = new ModelElementRasterLayer();
            ((ModelElementRasterLayer) element).setNumberOfBands(ModelElementRasterLayer.NUMBER_OF_BANDS_UNDEFINED);
         }
         else if (param instanceof ParameterVectorLayer) {
            element = new ModelElementVectorLayer();
            final AdditionalInfoVectorLayer ai = (AdditionalInfoVectorLayer) param.getParameterAdditionalInfo();
            ((ModelElementVectorLayer) element).setShapeType(ai.getShapeType());
         }
         else if (param instanceof ParameterTable) {
            element = new ModelElementTable();
         }
         else if (param instanceof ParameterPoint) {
            element = new ModelElementPoint();
         }
         else if (param instanceof ParameterNumericalValue) {
            element = new ModelElementNumericalValue();
         }
         else if (param instanceof ParameterString) {
            element = new ModelElementString();
         }
         else if (param instanceof ParameterFixedTable) {
            element = new ModelElementFixedTable();
            final AdditionalInfoFixedTable aift = (AdditionalInfoFixedTable) param.getParameterAdditionalInfo();
            ((ModelElementFixedTable) element).setRowsCount(aift.getRowsCount());
            ((ModelElementFixedTable) element).setColsCount(aift.getColsCount());
            ((ModelElementFixedTable) element).setIsNumberOfRowsFixed(aift.isNumberOfRowsFixed());
         }
         else if (param instanceof ParameterSelection) {
            element = new ModelElementSelection();
         }
         else if (param instanceof ParameterMultipleInput) {
            element = new ModelElementInputArray();
            final AdditionalInfoMultipleInput ai = (AdditionalInfoMultipleInput) param.getParameterAdditionalInfo();
            ((ModelElementInputArray) element).setType(ai.getDataType());
         }
         else if (param instanceof ParameterBoolean) {
            element = new ModelElementBoolean();
         }
         else if (param instanceof ParameterMultipleInput) {
            element = new ModelElementInputArray();
         }
         else if (param instanceof ParameterTableField) {
            element = new ModelElementTableField();
         }
         else if (param instanceof ParameterBand) {
            element = new ModelElementBand();
         }
      }
      catch (final NullParameterAdditionalInfoException e) {
      }

      return element;

   }

}
