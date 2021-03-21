

package es.unex.sextante.tables.vectorFieldCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.nfunk.jep.JEP;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.gui.modeler.ModelAlgorithm;
import es.unex.sextante.modeler.elements.ModelElementNumericalValue;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.shapesTools.ShapesTools;


public class VectorFieldCalculatorAlgorithm
         extends
            GeoAlgorithm {

   private static final Object NO_DATA = new Double(-99999.);

   public static final String  FORMULA = "FORMULA";
   public static final String  LAYER   = "LAYER";
   public static final String  RESULT  = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i, j;
      double dValue;
      boolean bOK;
      String sVariable;

      final Class[] types = { Double.class };
      final String[] sFields = { Sextante.getText("Result") };
      final IVectorLayer layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final JEP jep = new JEP();
      jep.addStandardConstants();
      jep.addStandardFunctions();

      String sFormula = m_Parameters.getParameterValueAsString(FORMULA).toLowerCase();
      sFormula = sFormula.replaceAll(" ", "");
      sFormula = replaceDots(sFormula);

      final String[] fieldNames = layer.getFieldNames();
      final ArrayList variables = new ArrayList();
      final ArrayList variableIndex = new ArrayList();
      for (i = 0; i < fieldNames.length; i++) {
         sVariable = fieldNames[i].toLowerCase();
         sVariable = sVariable.replaceAll(" ", "");
         sVariable = replaceDots(sVariable);
         if (sFormula.lastIndexOf(sVariable) != -1) {
            jep.addVariable(sVariable, 0.0);
            variables.add(sVariable);
            variableIndex.add(new Integer(i));
         }
      }
      final int[] iVariables = new int[variables.size()];
      final String[] sVariables = new String[variables.size()];
      for (i = 0; i < variableIndex.size(); i++) {
         iVariables[i] = ((Integer) variableIndex.get(i)).intValue();
         sVariables[i] = (String) variables.get(i);
      }

      jep.parseExpression(sFormula);

      final int iTotalCount = layer.getShapesCount();
      final Object[][] values = new Object[1][iTotalCount];
      if (!jep.hasError()) {
         final IFeatureIterator iter = layer.iterator();
         IFeature feat;
         int iCount = 0;
         while (iter.hasNext() && setProgress(iCount, iTotalCount)) {
            feat = iter.next();
            bOK = true;
            for (j = 0; (j < iVariables.length) && bOK; j++) {
               try {
                  dValue = Double.parseDouble(feat.getRecord().getValue(iVariables[j]).toString());
                  jep.addVariable(sVariables[j], dValue);
               }
               catch (final NumberFormatException nfe) {
                  bOK = false;
               }

            }
            if (bOK) {
               dValue = jep.getValue();
               values[0][iCount] = new Double(dValue);
            }
            else {
               values[0][iCount] = NO_DATA;
            }
            iCount++;
         }

         final IOutputChannel channel = getOutputChannel(RESULT);
         final OutputVectorLayer out = new OutputVectorLayer();
         out.setName(RESULT);
         out.setOutputChannel(channel);
         out.setDescription(Sextante.getText("Result"));
         out.setOutputObject(ShapesTools.addFields(m_OutputFactory, layer, channel, sFields, values, types));
         addOutputObject(out);

      }
      else {
         throw new GeoAlgorithmExecutionException(jep.getErrorInfo());
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Field_calculator"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addString(FORMULA, Sextante.getText("Formula"));
         addOutputVectorLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private String replaceDots(final String s) {

      char c, c2;
      StringBuffer sb = new StringBuffer(s);
      for (int i = 0; i < sb.length() - 1; i++) {
         c = sb.charAt(i);
         c2 = sb.charAt(i + 1);
         if ((c == '.') && !Character.isDigit(c2)) {
            sb = sb.deleteCharAt(i);
         }
      }

      return sb.toString();

   }


   @Override
   public boolean preprocessForModeller(final Object obj) throws GeoAlgorithmExecutionException {

      final ModelAlgorithm model = (ModelAlgorithm) obj;

      String sFormula = m_Parameters.getParameterValueAsString(FORMULA);

      final HashMap inputs = model.getInputs();
      final Set set = inputs.keySet();
      final Iterator iter = set.iterator();
      while (iter.hasNext()) {
         final Object key = iter.next();
         final Object input = inputs.get(key);
         if (input instanceof ModelElementNumericalValue) {
            if (sFormula.contains(((String) key))) {
               return false;
            }
         }
         if (sFormula.contains((String) key)) {
            sFormula = sFormula.replace(key.toString(), input.toString());
         }
      }

      m_Parameters.getParameter(FORMULA).setParameterValue(sFormula);

      return true;

   }

}
