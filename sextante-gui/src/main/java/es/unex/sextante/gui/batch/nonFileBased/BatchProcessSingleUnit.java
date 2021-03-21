package es.unex.sextante.gui.batch.nonFileBased;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.additionalInfo.AdditionalInfoTableField;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ITaskMonitor;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.ProcessTask;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.WrongParameterIDException;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputNumericalValue;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.FixedTableModel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterBand;
import es.unex.sextante.parameters.ParameterBoolean;
import es.unex.sextante.parameters.ParameterDataObject;
import es.unex.sextante.parameters.ParameterFilepath;
import es.unex.sextante.parameters.ParameterFixedTable;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterNumericalValue;
import es.unex.sextante.parameters.ParameterPoint;
import es.unex.sextante.parameters.ParameterSelection;
import es.unex.sextante.parameters.ParameterString;
import es.unex.sextante.parameters.ParameterTableField;
import es.unex.sextante.parameters.ParameterVectorLayer;

/**
 * A task representing a single algorithm execution as a part of a batch process. The process is comprised of several single units
 * like the one represented by this class
 * 
 * @author volaya
 * 
 */
public class BatchProcessSingleUnit
         implements
            Callable<Boolean> {

   private final HashMap                                    m_Parameters;
   private final GeoAlgorithm                               m_Algorithm;
   private final HashMap                                    m_DataObjects;
   private final HashMap                                    m_OutputSettings;
   private StringBuffer                                     m_sLog;
   private final AnalysisExtent                             m_AnalysisExtent;
   private final ITaskMonitor                               m_TaskMonitor;
   private final ArrayList<ArrayList<OutputNumericalValue>> m_NumericalOutputs;


   /**
    * 
    * @param alg
    *                the algorithm to execute
    * @param parameters
    *                a map with parameter values, to be used to fill the ParametersSet of the algorithm
    * @param output
    *                a map with output settings, to be used to fill the OutputObjectsSet of the algorithm
    * @param analysisExtent
    *                the output extent to use
    * @param numericalOutputs
    *                an array list to collect numerical outputs produced by the algorithm
    */
   public BatchProcessSingleUnit(final GeoAlgorithm alg,
                                 final HashMap parameters,
                                 final HashMap output,
                                 final AnalysisExtent analysisExtent,
                                 final ITaskMonitor monitor,
                                 final ArrayList<ArrayList<OutputNumericalValue>> numericalOutputs) {

      m_Algorithm = alg;
      m_Parameters = parameters;
      m_OutputSettings = output;
      m_DataObjects = new HashMap();
      m_AnalysisExtent = analysisExtent;
      m_TaskMonitor = monitor;
      m_NumericalOutputs = numericalOutputs;

   }


   /**
    * Executes the algorithm
    * 
    * @return true if the algorithm was not canceled
    * @throws GeoAlgorithmExecutionException
    */
   public Boolean call() throws GeoAlgorithmExecutionException {

      m_sLog = new StringBuffer();

      if (assignParameters() && assignOutputSettings() && assignExtent()) {
         final ExecutorService pool = Executors.newFixedThreadPool(1);
         final Future<Boolean> p = pool.submit(new ProcessTask(m_Algorithm, SextanteGUI.getOutputFactory(), m_TaskMonitor));
         try {
            final Boolean success = p.get();
            if ((success != null) && success.booleanValue()) {
               final Runnable postProcess = SextanteGUI.getPostProcessTask(m_Algorithm, false);
               if (postProcess != null) {
                  postProcess.run();
               }
               collectIndividualNumericalOutputs();
               m_DataObjects.clear();
               return true;
            }
            else {
               m_DataObjects.clear();
               return false;
            }
         }
         catch (final Exception e) {
            m_DataObjects.clear();
            Sextante.addErrorToLog(e);
            throw new GeoAlgorithmExecutionException(e.getMessage());
         }

      }
      else {
         m_DataObjects.clear();
         return Boolean.valueOf(false);
      }

   }


   private void collectIndividualNumericalOutputs() {

      final ArrayList<OutputNumericalValue> numericalOutputs = new ArrayList<OutputNumericalValue>();
      final OutputObjectsSet ooset = m_Algorithm.getOutputObjects();
      for (int i = 0; i < ooset.getOutputObjectsCount(); i++) {
         final Output out = ooset.getOutput(i);
         if (out instanceof OutputNumericalValue) {
            numericalOutputs.add((OutputNumericalValue) out);
         }
      }

      m_NumericalOutputs.add(numericalOutputs);

   }


   private boolean assignExtent() {

      if (m_Algorithm.getUserCanDefineAnalysisExtent()) {
         m_Algorithm.setAnalysisExtent(m_AnalysisExtent);
         return m_Algorithm.adjustOutputExtent();
      }
      else {
         return true;
      }

   }


   /**
    * Returns a string with information about the executed process
    * 
    * @return a string with information about the executed process
    */
   public String getLog() {

      return m_sLog.toString();

   }


   private boolean assignOutputSettings() {

      final Set set = m_OutputSettings.keySet();
      final Iterator iter = set.iterator();
      try {
         while (iter.hasNext()) {
            final String sOutputID = (String) iter.next();
            final String sValue = (String) m_OutputSettings.get(sOutputID);
            final Output out = m_Algorithm.getOutputObjects().getOutput(sOutputID);
            if ((out instanceof OutputRasterLayer) || (out instanceof OutputVectorLayer) || (out instanceof OutputTable)) {
               m_sLog.append(out.getDescription() + ":");
               m_sLog.append(sValue + "\n");
               out.setOutputChannel(new FileOutputChannel(sValue));
            }
         }

         return true;
      }
      catch (final Exception e) {
         return false;
      }

   }


   private boolean assignParameters() {

      final Set set = m_Parameters.keySet();
      Iterator iter = set.iterator();

      while (iter.hasNext()) {
         final String sParamID = (String) iter.next();
         final Object value = m_Parameters.get(sParamID);
         Parameter parameter;
         try {
            parameter = m_Algorithm.getParameters().getParameter(sParamID);
         }
         catch (final WrongParameterIDException e) {
            return false;
         }
         if ((parameter instanceof ParameterDataObject) || (parameter instanceof ParameterMultipleInput)) { //data objects first, to avoid problems with dependencies
            if (!setValue(value, parameter)) {
               m_sLog.append("[error]\n");
               return false;
            }
            else {
               m_sLog.append("[ok]\n");
            }
         }
      }

      iter = set.iterator();
      while (iter.hasNext()) {
         final String sParamID = (String) iter.next();
         final Object value = m_Parameters.get(sParamID);
         Parameter parameter;
         try {
            parameter = m_Algorithm.getParameters().getParameter(sParamID);
         }
         catch (final WrongParameterIDException e) {
            return false;
         }
         if (!(parameter instanceof ParameterDataObject) && !(parameter instanceof ParameterMultipleInput)) {
            if (!setValueFromString((String) value, parameter)) {
               m_sLog.append("[error]\n");
               return false;
            }
            else {
               m_sLog.append("[ok]\n");
            }
         }
      }

      return true;

   }


   private boolean setValue(final Object value,
                            final Parameter parameter) {

      m_sLog.append(parameter.getParameterDescription());
      m_sLog.append(":");
      m_sLog.append(value.toString());

      return parameter.setParameterValue(value);

   }


   private boolean setValueFromString(final String sValue,
                                      final Parameter parameter) {

      m_sLog.append(parameter.getParameterDescription());
      m_sLog.append(":");
      m_sLog.append(sValue);

      if (parameter instanceof ParameterNumericalValue) {
         return assignNumericalValue(sValue, (ParameterNumericalValue) parameter);
      }
      else if (parameter instanceof ParameterString) {
         return assignString(sValue, (ParameterString) parameter);
      }
      else if (parameter instanceof ParameterSelection) {
         return assignSelection(sValue, (ParameterSelection) parameter);
      }
      else if (parameter instanceof ParameterFixedTable) {
         return assignFixedTable(sValue, (ParameterFixedTable) parameter);
      }
      else if (parameter instanceof ParameterPoint) {
         return assignPoint(sValue, (ParameterPoint) parameter);
      }
      else if (parameter instanceof ParameterBoolean) {
         return assignBoolean(sValue, parameter);
      }
      else if (parameter instanceof ParameterFilepath) {
         return assignFilepath(sValue, (ParameterFilepath) parameter);
      }
      else if (parameter instanceof ParameterTableField) {
         return assignTableField(sValue, (ParameterTableField) parameter);
      }
      else if (parameter instanceof ParameterBand) {
         return assignBand(sValue, (ParameterBand) parameter);
      }
      else {
         return false;
      }

   }


   private boolean assignBand(final String sValue,
                              final ParameterBand parameter) {

      try {
         final Integer value = new Integer(Integer.parseInt(sValue) - 1);
         parameter.setParameterValue(value);
         return true;
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
         return false;
      }

   }


   private boolean assignTableField(final String sValue,
                                    final ParameterTableField parameter) {

      final int iField;
      try {
         final AdditionalInfoTableField aivl = (AdditionalInfoTableField) parameter.getParameterAdditionalInfo();
         final ParametersSet ps = m_Algorithm.getParameters();
         final String sParent = aivl.getParentParameterName();
         final Parameter parentParam = ps.getParameter(sParent);
         if (parentParam instanceof ParameterVectorLayer) {
            final IVectorLayer layer = ps.getParameterValueAsVectorLayer(sParent);
            final String[] sNames = layer.getFieldNames();
            for (int i = 0; i < sNames.length; i++) {
               if (layer.getFieldName(i).equals(sValue)) {
                  parameter.setParameterValue(new Integer(i));
                  return true;
               }
            }
            return false;
         }
         else {//table
            final ITable table = ps.getParameterValueAsTable(sParent);
            final String[] sNames = table.getFieldNames();
            for (int i = 0; i < sNames.length; i++) {
               if (table.getFieldName(i).equals(sValue)) {
                  parameter.setParameterValue(new Integer(i));
                  return true;
               }
            }
            return false;
         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
         return false;
      }

   }


   private boolean assignFixedTable(final String sValue,
                                    final ParameterFixedTable parameter) {

      boolean bIsNumberOfRowsFixed;
      int iCols, iRows;
      int iCol, iRow;
      int iToken = 0;
      FixedTableModel tableModel;
      final StringTokenizer st = new StringTokenizer(sValue, ",");
      String sToken;
      AdditionalInfoFixedTable ai;
      try {
         ai = (AdditionalInfoFixedTable) parameter.getParameterAdditionalInfo();
         iCols = ai.getColsCount();
         iRows = ai.getRowsCount();
         bIsNumberOfRowsFixed = ai.isNumberOfRowsFixed();
         tableModel = new FixedTableModel(ai.getCols(), iCols, bIsNumberOfRowsFixed);

         if (bIsNumberOfRowsFixed) {
            if (st.countTokens() != iCols * iRows) {
               return false;
            }
         }
         else {
            if (st.countTokens() % iCols != 0) {
               return false;
            }
         }

         while (st.hasMoreTokens()) {
            iRow = (int) Math.floor(iToken / (double) iCols);
            iCol = iToken % iCols;
            sToken = st.nextToken().trim();
            tableModel.setValueAt(sToken, iRow, iCol);
            iToken++;
         }

         parameter.setParameterValue(tableModel);
         return true;
      }
      catch (final Exception e) {
         return false;
      }
   }


   private boolean assignFilepath(final String sValue,
                                  final ParameterFilepath parameter) {

      parameter.setParameterValue(sValue);

      return true;

   }


   private boolean assignPoint(final String sValue,
                               final ParameterPoint parameter) {

      try {
         final String[] sCoords = sValue.split(",");
         final double dX = Double.parseDouble(sCoords[0]);
         final double dY = Double.parseDouble(sCoords[1]);
         final Point2D pt = new Point2D.Double(dX, dY);
         parameter.setParameterValue(pt);
         return true;
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
         return false;
      }

   }


   private boolean assignNumericalValue(final String sValue,
                                        final ParameterNumericalValue parameter) {

      try {
         final Double value = new Double(Double.parseDouble(sValue));
         parameter.setParameterValue(value);
         return true;
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
         return false;
      }

   }


   private boolean assignSelection(final String sValue,
                                   final ParameterSelection parameter) {

      try {
         final AdditionalInfoSelection ai = (AdditionalInfoSelection) parameter.getParameterAdditionalInfo();

         final String[] sValues = ai.getValues();
         for (int i = 0; i < sValues.length; i++) {
            if (sValue.equals(sValues[i])) {
               parameter.setParameterValue(new Integer(i));
               return true;
            }
         }


      }
      catch (final NullParameterAdditionalInfoException e) {
         return false;
      }

      return false;

   }


   private boolean assignBoolean(final String sValue,
                                 final Parameter parameter) {

      if (sValue.toLowerCase().equals("true")) {
         parameter.setParameterValue(new Boolean(true));
      }
      else {
         parameter.setParameterValue(new Boolean(false));
      }

      return true;

   }


   private boolean assignString(final String sValue,
                                final ParameterString parameter) {

      parameter.setParameterValue(sValue);

      return true;

   }

}
