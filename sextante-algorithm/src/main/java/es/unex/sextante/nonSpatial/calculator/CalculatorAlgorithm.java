

package es.unex.sextante.nonSpatial.calculator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.nfunk.jep.JEP;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.gui.modeler.ModelAlgorithm;
import es.unex.sextante.modeler.elements.ModelElementNumericalValue;


public class CalculatorAlgorithm
         extends
            GeoAlgorithm {

   public static final String FORMULA = "FORMULA";
   public static final String RESULT  = "RESULT";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Calculator"));
      setGroup(Sextante.getText("nonSpatial"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addString(FORMULA, Sextante.getText("Formula"));
         addOutputNumericalValue(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final String sFormula = m_Parameters.getParameterValueAsString(FORMULA);

      final JEP jep = new JEP();

      jep.addStandardConstants();
      jep.addStandardFunctions();

      jep.parseExpression(sFormula);

      if (jep.hasError()) {
         Sextante.addErrorToLog(jep.getErrorInfo());
         throw new GeoAlgorithmExecutionException(Sextante.getText("Syntax_error"));
      }

      addOutputNumericalValue(RESULT, jep.getValue());

      return true;

   }


   @Override
   public boolean preprocessForModeller(final Object obj) throws GeoAlgorithmExecutionException {

      final ModelAlgorithm model = (ModelAlgorithm) obj;

      try {
         String sFormula = m_Parameters.getParameterValueAsString(FORMULA);
         final HashMap inputs = model.getInputs();
         final Set set = inputs.keySet();
         final Iterator iter = set.iterator();
         while (iter.hasNext()) {
            final Object key = iter.next();
            final Object input = inputs.get(key);
            if (input instanceof Double) {
               if (sFormula.contains((String) key)) {
                  sFormula = sFormula.replace(key.toString(), input.toString());
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
