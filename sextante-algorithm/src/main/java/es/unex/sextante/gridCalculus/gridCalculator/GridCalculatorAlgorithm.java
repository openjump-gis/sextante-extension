package es.unex.sextante.gridCalculus.gridCalculator;

import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.nfunk.jep.JEP;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.gui.modeler.ModelAlgorithm;
import es.unex.sextante.modeler.elements.ModelElementNumericalValue;
import es.unex.sextante.parameters.RasterLayerAndBand;
import es.unex.sextante.rasterWrappers.GridWrapper;


public class GridCalculatorAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYERS  = "LAYERS";
   public static final String FORMULA = "FORMULA";
   public static final String RESULT  = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int x, y;
      double dValue;

      IRasterLayer layer;
      RasterLayerAndBand layerAndBand;
      final ArrayList names = new ArrayList();
      final ArrayList layers = m_Parameters.getParameterValueAsArrayList(LAYERS);
      String sFormula = m_Parameters.getParameterValueAsString(FORMULA).toLowerCase();
      final ArrayList bands = FormulaParser.getBandsFromFormula(sFormula, layers);

      if (bands == null) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Syntax_error"));
      }

      final IRasterLayer m_Result = getNewRasterLayer(RESULT, Sextante.getText("Result"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      final IRasterLayer window[] = new IRasterLayer[bands.size()];
      final String sVariable[] = new String[bands.size()];
      final int iBands[] = new int[bands.size()];
      final AnalysisExtent extent = m_Result.getWindowGridExtent();
      final int iNX = m_Result.getWindowGridExtent().getNX();
      final int iNY = m_Result.getWindowGridExtent().getNY();

      final JEP jep = new JEP();
      jep.addStandardConstants();
      jep.addStandardFunctions();

      for (i = 0; i < bands.size(); i++) {
         layerAndBand = (RasterLayerAndBand) bands.get(i);
         layer = layerAndBand.getRasterLayer();
         iBands[i] = layerAndBand.getBand();
         names.add(layer.getName());
         window[i] = layer;
         window[i].setWindowExtent(extent);
         if ((layer.getDataType() == DataBuffer.TYPE_FLOAT) || (layer.getDataType() == DataBuffer.TYPE_DOUBLE)) {
            window[i].setInterpolationMethod(GridWrapper.INTERPOLATION_BSpline);
         }
         else {
            window[i].setInterpolationMethod(GridWrapper.INTERPOLATION_NearestNeighbour);
         }

         sVariable[i] = layer.getName() + " Band " + Integer.toString(iBands[i] + 1);
         sVariable[i] = sVariable[i].toLowerCase();
         sVariable[i] = sVariable[i].replaceAll(" ", "");
         sVariable[i] = sVariable[i].replaceAll("\\[", "_");
         sVariable[i] = sVariable[i].replaceAll("\\]", "_");
         sVariable[i] = FormulaParser.replaceDots(sVariable[i]);

         jep.addVariable(sVariable[i], 0.0);

      }

      sFormula = FormulaParser.prepareFormula(sFormula, names);
      sFormula = sFormula.toLowerCase().replaceAll(" ", "");
      sFormula = sFormula.replaceAll("\\[", "_");
      sFormula = sFormula.replaceAll("\\]", "_");
      sFormula = FormulaParser.replaceDots(sFormula);
      jep.parseExpression(sFormula);

      if (!jep.hasError()) {
         for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
            for (x = 0; x < iNX; x++) {
               for (i = 0; i < bands.size(); i++) {
                  dValue = window[i].getCellValueAsDouble(x, y, iBands[i]);
                  if (!window[i].isNoDataValue(dValue)) {
                     jep.addVariable(sVariable[i], dValue);
                  }
                  else {
                     m_Result.setNoData(x, y);
                     break;
                  }
               }
               if (i == bands.size()) {
                  dValue = jep.getValue();
                  if (!Double.isNaN(dValue)) {
                     m_Result.setCellValue(x, y, dValue);
                  }
                  else {
                     m_Result.setNoData(x, y);
                  }
               }
            }
         }
      }
      else {
         throw new GeoAlgorithmExecutionException(jep.getErrorInfo());
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Raster_calculator"));
      setGroup(Sextante.getText("Calculus_tools_for_raster_layer"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addMultipleInput(LAYERS, Sextante.getText("Layers"), AdditionalInfoMultipleInput.DATA_TYPE_RASTER, true);
         m_Parameters.addString(FORMULA, Sextante.getText("Formula"));
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean preprocessForModeller(final Object obj) throws GeoAlgorithmExecutionException {

      final ModelAlgorithm model = (ModelAlgorithm) obj;
      try {
         String sFormula = m_Parameters.getParameterValueAsString(FORMULA).toLowerCase();
         String sKey = model.getInputAsignment(LAYERS, this);
         final HashMap inputs = model.getInputs();
         final ArrayList array = (ArrayList) inputs.get(sKey);
         final ArrayList<String> variables = new ArrayList<String>();
         for (int i = 0; i < array.size(); i++) {
            sKey = (String) array.get(i);
            final IRasterLayer layer = (IRasterLayer) inputs.get(sKey);
            String sVariableName = layer.getName();
            if (variables.contains(sVariableName)) {
               final char c = (char) ('a' + i);
               sVariableName = Character.toString(c) + "_" + sVariableName;
               layer.setName(sVariableName);
            }
            variables.add(sVariableName);
            sFormula = sFormula.replace(sKey.toLowerCase(), sVariableName);
         }
         final Set set = inputs.keySet();
         final Iterator iter = set.iterator();
         while (iter.hasNext()) {
            final Object key = iter.next();
            final Object input = inputs.get(key);
            if (input instanceof Double) {
               if (sFormula.contains(((String) key).toLowerCase())) {
                  sFormula = sFormula.replace(((String) key).toLowerCase(), input.toString());
               }
            }
            if (input instanceof ModelElementNumericalValue) {
               if (sFormula.contains(((String) key).toLowerCase())) {
                  return false;
               }
            }
         }
         m_Parameters.getParameter(FORMULA).setParameterValue(sFormula);
      }
      catch (final Exception e) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Syntax_error"));
      }

      return true;

   }


}
