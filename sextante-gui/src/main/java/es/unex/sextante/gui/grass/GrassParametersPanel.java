package es.unex.sextante.gui.grass;

import javax.swing.JPanel;

import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.gui.algorithm.DefaultParametersPanel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterBoolean;
import es.unex.sextante.parameters.ParameterFilepath;
import es.unex.sextante.parameters.ParameterFixedTable;
import es.unex.sextante.parameters.ParameterNumericalValue;
import es.unex.sextante.parameters.ParameterPoint;
import es.unex.sextante.parameters.ParameterSelection;
import es.unex.sextante.parameters.ParameterString;

public class GrassParametersPanel
         extends
            DefaultParametersPanel {


   @Override
   protected void addNonDataObjects(final JPanel pane) {

      int i;
      Parameter parameter;
      final ParametersSet parameters = m_Algorithm.getParameters();

      m_iCurrentRow = 0;

      for (i = 0; i < m_Algorithm.getNumberOfParameters(); i++) {
         parameter = parameters.getParameter(i);
         if (parameter.getParameterName().equals(GrassAlgorithm.PARAMETER_RESTRICT_VECTOR_OUTPUT_TYPE)) {
            continue;
         }
         if (parameter instanceof ParameterNumericalValue) {
            addNumericalTextField(pane, (ParameterNumericalValue) parameter);
         }
         else if (parameter instanceof ParameterString) {
            addStringTextField(pane, (ParameterString) parameter);
         }
         else if (parameter instanceof ParameterSelection) {
            addSelection(pane, (ParameterSelection) parameter);
         }
         else if (parameter instanceof ParameterFixedTable) {
            addFixedTable(pane, (ParameterFixedTable) parameter);
         }
         else if (parameter instanceof ParameterPoint) {
            addPoint(pane, (ParameterPoint) parameter);
         }
         else if (parameter instanceof ParameterBoolean) {
            addCheckBox(pane, (ParameterBoolean) parameter);
         }
         else if (parameter instanceof ParameterFilepath) {
            addFilepath(pane, (ParameterFilepath) parameter);
         }
      }
   }

}
