package es.unex.sextante.gui.modeler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import es.unex.sextante.additionalInfo.AdditionalInfoBand;
import es.unex.sextante.additionalInfo.AdditionalInfoDataObject;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoTableField;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.WrongParameterIDException;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.modeler.elements.IModelElement;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterBand;
import es.unex.sextante.parameters.ParameterDataObject;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterNumericalValue;
import es.unex.sextante.parameters.ParameterTable;
import es.unex.sextante.parameters.ParameterTableField;
import es.unex.sextante.parameters.ParameterVectorLayer;
import es.unex.sextante.parameters.RasterLayerAndBand;

/**
 * An algorithm representing a model (a set of other geoalgorithms linked together in a workflow)
 * 
 * @author Victor Olaya volaya@unex.es
 * 
 */

public class ModelAlgorithm
         extends
            GeoAlgorithm {

   /**
    * A map of all inputs used along the execution of the model, not only the ones set by the user, but also the input to
    * intermediate algorithms, which are created from other intermediate algorithms.
    */
   private HashMap   m_Inputs;

   /**
    * A list of all used algorithms in the model
    */
   private ArrayList m_Algorithms;

   /**
    * A list of keys to identify the algorithms
    */
   private ArrayList m_AlgorithmKeys;

   private ArrayList m_InputAssignments;

   /**
    * The filename associated with this model
    */
   private String    m_sFilename;

   private int       m_iGeometryTypeRestriction;


   /**
    * Creates a new model algorithm
    */
   public ModelAlgorithm() {

      super();

      m_Algorithms = new ArrayList();
      m_AlgorithmKeys = new ArrayList();
      m_InputAssignments = new ArrayList();
      m_Inputs = new HashMap();

      setName(Sextante.getText("[New_model]"));

   }


   @Override
   public void defineCharacteristics() {}


   /**
    * Sets the values used by the model algorithm
    * 
    * @param algs
    *                The list of algorithm used by the model
    * @param algKeys
    *                a list of keys to identify the used algorithms
    * @param assignments
    *                a list of assignments
    * @param inputs
    *                the list of inputs of the algorithm
    * @param outputs
    *                The output objects that the algorithm generates
    */
   public void setValues(final ArrayList algs,
                         final ArrayList algKeys,
                         final ArrayList assignments,
                         final HashMap inputs,
                         final OutputObjectsSet outputs) {

      m_Algorithms = algs;
      m_AlgorithmKeys = algKeys;
      m_InputAssignments = assignments;
      m_Inputs = inputs;
      m_OutputObjects = outputs;

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iExecuted = 0;
      boolean bHasChanged;
      final boolean bExecuted[] = new boolean[m_Algorithms.size()];
      String sKey, sAlgKey;
      GeoAlgorithm alg;
      OutputObjectsSet oldOOSet;
      Object object;

      setInputs();

      do {
         bHasChanged = false;
         for (i = 0; (i < m_Algorithms.size()) && !m_Task.isCanceled(); i++) {
            if (!bExecuted[i]) {
               if (prepareAlgorithm(i)) {
                  alg = (GeoAlgorithm) m_Algorithms.get(i);
                  if (!alg.preprocessForModeller(this)) {
                     continue;
                  }
                  bExecuted[i] = true;
                  iExecuted++;
                  sAlgKey = (String) m_AlgorithmKeys.get(i);
                  if (m_bIsAutoExtent) {
                     alg.setAnalysisExtent(null);
                  }
                  else {
                     alg.setAnalysisExtent(this.getAnalysisExtent());
                  }
                  oldOOSet = alg.getOutputObjects().getNewInstance();
                  m_Task.setProcessDescription(alg.getName());
                  m_Task.setProgress(0);
                  m_Task.setDeterminate(alg.isDeterminatedProcess());
                  boolean bSuccess;
                  try {
                     bSuccess = alg.execute(m_Task, m_OutputFactory);
                  }
                  catch (final GeoAlgorithmExecutionException e) {
                     final GeoAlgorithm wrongAlg = (GeoAlgorithm) m_Algorithms.get(i);
                     Sextante.addErrorToLog(Sextante.getText("error_en_algoritmo" + " ") + Integer.toString(i + 1) + ":"
                                            + wrongAlg.getName()); //
                     throw e;
                  }
                  if (bSuccess) {
                     bHasChanged = true;
                     final OutputObjectsSet ooset = alg.getOutputObjects();
                     for (int j = 0; j < ooset.getOutputObjectsCount(); j++) {
                        final Output output = ooset.getOutput(j);
                        object = output.getOutputObject();
                        //if the output has been created by an external application, object might be null
                        //We have to create it from the output channel
                        if (object == null) {
                           object = getDataObjectFromOutputChannel(output);
                           ((IDataObject) object).setName(output.getName());

                        }
                        sKey = output.getName();
                        sKey += sAlgKey;
                        if (m_OutputObjects.containsKey(sKey)) {
                           final Output out = m_OutputObjects.getOutput(sKey);
                           out.setOutputObject(object);
                        }
                        if (!(output instanceof OutputRasterLayer) && !(output instanceof OutputVectorLayer)
                            && !(output instanceof OutputTable) && !(output instanceof Output3DRasterLayer)) {
                           m_OutputObjects.add(output.getNewInstance());
                        }
                        m_Inputs.put(sKey, object);
                     }
                     alg.setOutputObjects(oldOOSet);
                  }
                  else {
                     return false;
                  }
               }
            }
         }
      }
      while (bHasChanged && !m_Task.isCanceled());

      return !m_Task.isCanceled();

   }


   private IDataObject getDataObjectFromOutputChannel(final Output output) throws GeoAlgorithmExecutionException {

      final String sFilename = ((FileOutputChannel) output.getOutputChannel()).getFilename();
      final IDataObject obj = SextanteGUI.getInputFactory().openDataObjectFromFile(sFilename);
      if (obj == null) {
         throw new GeoAlgorithmExecutionException("Could not open intermediate layer");
      }

      return obj;

   }


   /**
    * Initiates the list of inputs with the inputs selected by the user.
    */
   private void setInputs() {

      int i;
      int iCount;
      int iField;
      AdditionalInfoTableField aitf;
      Parameter param, parent;
      String sParent;
      String sFieldName = "";

      iCount = m_Parameters.getNumberOfParameters();

      for (i = 0; i < iCount; i++) {
         param = m_Parameters.getParameter(i);
         m_Inputs.put(param.getParameterName(), param.getParameterValueAsObject());
      }

      for (i = 0; i < iCount; i++) {
         param = m_Parameters.getParameter(i);
         if (param instanceof ParameterTableField) {
            try {
               aitf = (AdditionalInfoTableField) param.getParameterAdditionalInfo();
               sParent = aitf.getParentParameterName();
               parent = m_Parameters.getParameter(sParent);
               iField = param.getParameterValueAsInt();
               if (parent instanceof ParameterVectorLayer) {
                  final IVectorLayer layer = parent.getParameterValueAsVectorLayer();
                  sFieldName = layer.getFieldName(iField);
               }
               else if (parent instanceof ParameterTable) {
                  final ITable table = parent.getParameterValueAsTable();
                  sFieldName = table.getFieldName(iField);

               }
               m_Inputs.put(param.getParameterName(), sFieldName);
            }
            catch (final Exception e) {
               Sextante.addErrorToLog(e);
            }
         }
      }

   }


   /**
    * Prepares an algorithm to be executed.
    * 
    * @param iAlgorithm
    *                the index of the algorithm in the list of them kept by the model
    * @return true if the algorithm can be executed. False if some of the inputs needed are not yet available (other algorithms
    *         have to be run before)
    * @throws GeoAlgorithmExecutionException
    */
   private boolean prepareAlgorithm(final int iAlgorithm) throws GeoAlgorithmExecutionException {

      int i;
      int iCount;
      boolean bMandatory;
      final GeoAlgorithm alg = (GeoAlgorithm) m_Algorithms.get(iAlgorithm);
      final ParametersSet ps = alg.getParameters();
      Parameter param;
      Object asignment;
      Object input;
      HashMap map;
      ArrayList inputArray;

      iCount = ps.getNumberOfParameters();
      map = (HashMap) m_InputAssignments.get(iAlgorithm);

      for (i = 0; i < iCount; i++) {
         param = ps.getParameter(i);
         asignment = map.get(param.getParameterName());
         if (param instanceof ParameterMultipleInput) {
            if (asignment != null) {
               inputArray = (ArrayList) m_Inputs.get(asignment);
               if (inputArray == null) {
                  return false;
               }
               else {
                  input = new ArrayList();
                  if (createMultipleInputFromArray(inputArray, (ArrayList) input, param)) {
                     param.setParameterValue(input);
                     bMandatory = ((AdditionalInfoMultipleInput) param.getParameterAdditionalInfo()).getIsMandatory();
                     if (bMandatory && (((ArrayList) input).size() == 0)) {
                        return false;
                     }
                  }
                  else {
                     return false;
                  }

               }
            }
         }
         else {
            if (param instanceof ParameterDataObject) {
               try {
                  bMandatory = ((AdditionalInfoDataObject) param.getParameterAdditionalInfo()).getIsMandatory();
               }
               catch (final NullParameterAdditionalInfoException e) {
                  Sextante.addErrorToLog(e);
                  return false;
               }
            }
            else {
               bMandatory = false;
            }

            if (asignment == null) {
               if (bMandatory) {
                  return false;
               }
            }
            else {
               input = m_Inputs.get(asignment);
               if (input instanceof IModelElement) {
                  return false;
               }
               else {
                  if (!(param instanceof ParameterTableField)) {
                     param.setParameterValue(input);
                  }

               }
            }
         }

      }

      setOutputs(iAlgorithm);
      checkVectorLayers(iAlgorithm);
      checkTableFields(iAlgorithm);
      checkRasterBands(iAlgorithm);
      checkNumericalValues(iAlgorithm);

      return true;

   }


   /**
    * Sets output filenames for a given algorithm. If outputs of this algorithm are also outputs of the model (i.e. they are not
    * intermediate results), they are assigned the corresponding filenames
    * 
    * @param iIndex
    *                the index of the algorithm in the model
    */
   private void setOutputs(final int iIndex) {

      final GeoAlgorithm alg = (GeoAlgorithm) m_Algorithms.get(iIndex);
      final String sAlgKey = (String) m_AlgorithmKeys.get(iIndex);
      final OutputObjectsSet ooset = alg.getOutputObjects();
      for (int j = 0; j < ooset.getOutputObjectsCount(); j++) {
         final Output output = ooset.getOutput(j);
         String sKey = output.getName();
         sKey += sAlgKey;
         if (m_OutputObjects.containsKey(sKey)) {
            output.setOutputChannel(this.getOutputChannel(sKey));
         }
      }

   }


   /**
    * Checks that numerical inputs for a given algorithm are correct
    * 
    * @param iAlgorithm
    *                the index of the algorithm in the list of them kept by the model
    * @throws GeoAlgorithmExecutionException
    *                 if numerical inputs of the algorithm are incorrect
    */
   private void checkNumericalValues(final int iAlgorithm) throws GeoAlgorithmExecutionException {

      int i;
      int iCount;
      double dValue;
      double dMin, dMax;
      Object asignment;
      AdditionalInfoNumericalValue ainv;
      Parameter param;
      final GeoAlgorithm alg = (GeoAlgorithm) m_Algorithms.get(iAlgorithm);
      final ParametersSet ps = alg.getParameters();
      final HashMap map = (HashMap) m_InputAssignments.get(iAlgorithm);

      iCount = ps.getNumberOfParameters();

      for (i = 0; i < iCount; i++) {
         try {
            param = ps.getParameter(i);
            asignment = map.get(param.getParameterName());
            if (param instanceof ParameterNumericalValue) {
               dValue = ((Double) m_Inputs.get(asignment)).doubleValue();
               ainv = (AdditionalInfoNumericalValue) param.getParameterAdditionalInfo();
               dMax = ainv.getMaxValue();
               dMin = ainv.getMinValue();
               if ((dMax < dValue) || (dValue < dMin)) {
                  final String sError = "Valor_fuera_rango" + Double.toString(dValue);
                  throw new GeoAlgorithmExecutionException(sError);
               }
            }

         }
         catch (final Exception e) {
            throw new GeoAlgorithmExecutionException(e.getMessage());
         }
      }

   }


   /**
    * Checks that vector layer inputs for a given algorithm are correct
    * 
    * @param iAlgorithm
    *                the index of the algorithm in the list of them kept by the model
    * @throws GeoAlgorithmExecutionException
    *                 if vector layer inputs of the algorithm are incorrect
    */
   private void checkVectorLayers(final int iAlgorithm) throws GeoAlgorithmExecutionException {

      int i;
      int iCount;
      int iType, iTypeLayer;
      Object asignment;
      AdditionalInfoVectorLayer aivl;
      IVectorLayer layer;
      Parameter param;
      final GeoAlgorithm alg = (GeoAlgorithm) m_Algorithms.get(iAlgorithm);
      final ParametersSet ps = alg.getParameters();
      final HashMap map = (HashMap) m_InputAssignments.get(iAlgorithm);

      iCount = ps.getNumberOfParameters();

      for (i = 0; i < iCount; i++) {
         try {
            param = ps.getParameter(i);
            asignment = map.get(param.getParameterName());
            if (param instanceof ParameterVectorLayer) {
               layer = (IVectorLayer) m_Inputs.get(asignment);
               aivl = (AdditionalInfoVectorLayer) param.getParameterAdditionalInfo();
               iType = aivl.getShapeType();
               iTypeLayer = layer.getShapeType();
               if ((iType == iTypeLayer) || (iType == AdditionalInfoVectorLayer.SHAPE_TYPE_ANY)) {}
               else {
                  final String sError = "ERROR: Layer " + layer.getName() + ". Wrong layer type";
                  throw new GeoAlgorithmExecutionException(sError);
               }
            }
         }
         catch (final Exception e) {
            throw new GeoAlgorithmExecutionException(e.getMessage());
         }
      }

   }


   /**
    * Checks that table field inputs for a given algorithm are correct
    * 
    * @param iAlgorithm
    *                the index of the algorithm in the list of them kept by the model
    * @throws GeoAlgorithmExecutionException
    *                 if table field inputs of the algorithm are incorrect
    */
   private void checkTableFields(final int iAlgorithm) throws GeoAlgorithmExecutionException {

      int i;
      int iCount;
      int iField;
      Object asignment;
      AdditionalInfoTableField aitf;
      IVectorLayer layer;
      ITable table;
      Parameter param, parentParam;
      String sParent;
      String sFieldName;
      final GeoAlgorithm alg = (GeoAlgorithm) m_Algorithms.get(iAlgorithm);
      final ParametersSet ps = alg.getParameters();
      final HashMap map = (HashMap) m_InputAssignments.get(iAlgorithm);

      iCount = ps.getNumberOfParameters();

      for (i = 0; i < iCount; i++) {
         try {
            param = ps.getParameter(i);
            asignment = map.get(param.getParameterName());
            if (param instanceof ParameterTableField) {
               sFieldName = (String) m_Inputs.get(asignment);
               aitf = (AdditionalInfoTableField) param.getParameterAdditionalInfo();
               if (sFieldName.trim().equals("") && !aitf.getIsMandatory()) {
                  continue;
               }
               sParent = aitf.getParentParameterName();
               parentParam = ps.getParameter(sParent);
               if (parentParam instanceof ParameterVectorLayer) {
                  layer = ps.getParameterValueAsVectorLayer(sParent);
                  iField = layer.getFieldIndexByName(sFieldName);
                  if (iField != -1) {
                     param.setParameterValue(new Integer(iField));
                  }
                  else {
                     final String sError = "ERROR: Field " + sFieldName + " not found in layer " + layer.getName();
                     throw new GeoAlgorithmExecutionException(sError);
                  }
               }
               else {//table
                  table = ps.getParameterValueAsTable(sParent);
                  iField = table.getFieldIndexByName(sFieldName);
                  if (iField != -1) {
                     param.setParameterValue(new Integer(iField));
                  }
                  else {
                     final String sError = "ERROR: Field " + sFieldName + " not found in layer " + table.getName();
                     throw new GeoAlgorithmExecutionException(sError);
                  }
               }

            }
         }
         catch (final Exception e) {
            throw new GeoAlgorithmExecutionException(e.getMessage());
         }
      }

   }


   /**
    * Checks that raster band inputs for a given algorithm are correct
    * 
    * @param iAlgorithm
    *                the index of the algorithm in the list of them kept by the model
    * @throws GeoAlgorithmExecutionException
    *                 if raster band inputs of the algorithm are incorrect
    */
   private void checkRasterBands(final int iAlgorithm) throws GeoAlgorithmExecutionException {

      int i;
      int iCount;
      int iBand;
      Object asignment;
      AdditionalInfoBand aib;
      IRasterLayer layer;
      Parameter param;
      String sParent;
      final GeoAlgorithm alg = (GeoAlgorithm) m_Algorithms.get(iAlgorithm);
      final ParametersSet ps = alg.getParameters();
      final HashMap map = (HashMap) m_InputAssignments.get(iAlgorithm);

      iCount = ps.getNumberOfParameters();

      for (i = 0; i < iCount; i++) {
         try {
            param = ps.getParameter(i);
            asignment = map.get(param.getParameterName());
            if (param instanceof ParameterBand) {
               iBand = ((Integer) m_Inputs.get(asignment)).intValue();
               aib = (AdditionalInfoBand) param.getParameterAdditionalInfo();
               sParent = aib.getParentParameterName();
               layer = ps.getParameterValueAsRasterLayer(sParent);
               if (layer.getBandsCount() > iBand) {
                  param.setParameterValue(new Integer(iBand));
               }
               else {
                  final String sError = "ERROR: Band " + Integer.toString(iBand + 1) + " not found in layer " + layer.getName();
                  throw new GeoAlgorithmExecutionException(sError);
               }
            }
         }
         catch (final Exception e) {
            throw new GeoAlgorithmExecutionException(e.getMessage());
         }
      }

   }


   private boolean createMultipleInputFromArray(final ArrayList array,
                                                final ArrayList paramInput,
                                                final Parameter param) {

      final ArrayList paramInputUnprocessed = new ArrayList();
      final boolean bReturn = createMultipleInputFromArray(array, paramInputUnprocessed);

      if (bReturn) {
         AdditionalInfoMultipleInput ai;
         try {
            ai = (AdditionalInfoMultipleInput) param.getParameterAdditionalInfo();
            if (ai.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_BAND) {
               createArrayOfBands(paramInput, paramInputUnprocessed);
            }
            else {
               for (int i = 0; i < paramInputUnprocessed.size(); i++) {
                  paramInput.add(paramInputUnprocessed.get(i));
               }
            }
            return true;
         }
         catch (final NullParameterAdditionalInfoException e) {
            Sextante.addErrorToLog(e);
            return false;
         }

      }
      else {
         return false;
      }


   }


   private void createArrayOfBands(final ArrayList array,
                                   final ArrayList unprocessed) {


      for (int i = 0; i < unprocessed.size(); i++) {
         final Object obj = unprocessed.get(i);
         if (obj instanceof RasterLayerAndBand) {
            array.add(obj);
         }
         else {
            final IRasterLayer lyr = (IRasterLayer) obj;
            for (int j = 0; j < lyr.getBandsCount(); j++) {
               array.add(new RasterLayerAndBand(lyr, j));
            }
         }
      }

   }


   private boolean createMultipleInputFromArray(final ArrayList array,
                                                final ArrayList paramInput) {

      String sName;
      Object item;
      int i;
      final int iParams = array.size();

      for (i = 0; i < iParams; i++) {
         sName = (String) array.get(i);
         item = m_Inputs.get(sName);
         if (item == null) {
            return false;
         }
         else if (item instanceof ArrayList) {
            if (!createMultipleInputFromArray((ArrayList) item, paramInput)) {
               return false;
            }
         }
         else {
            paramInput.add(item);
         }
      }

      return true;


   }


   /**
    * Adds a new algorithm to the model
    * 
    * @param alg
    *                the algorithm to add
    * @param sName
    *                the name to identify the algorithm
    */
   public void addAlgorithm(final GeoAlgorithm alg,
                            final String sName) {

      if (!m_AlgorithmKeys.contains(sName)) {
         m_Algorithms.add(alg);
         m_AlgorithmKeys.add(sName);
         m_InputAssignments.add(new HashMap());
         checkUserCanDefineOutputExtent();
      }

   }


   /**
    * Removes an algorithm from the model
    * 
    * @param sKey
    *                the name to identify the algorithm to remove
    */
   public void removeAlgorithm(final String sKey) {

      String sAlgKey;

      for (int i = 0; i < m_AlgorithmKeys.size(); i++) {
         sAlgKey = (String) m_AlgorithmKeys.get(i);
         if (sAlgKey.equals(sKey)) {
            m_Algorithms.remove(i);
            m_AlgorithmKeys.remove(i);
            m_InputAssignments.remove(i);
            checkUserCanDefineOutputExtent();
            return;
         }
      }

   }


   /**
    * Checks if the algorithm generates user-defined output and updates the corresponding property
    * 
    */
   private void checkUserCanDefineOutputExtent() {

      setUserCanDefineAnalysisExtent(false);

      for (int i = 0; i < m_Algorithms.size(); i++) {
         if (((GeoAlgorithm) m_Algorithms.get(i)).getUserCanDefineAnalysisExtent()) {
            setUserCanDefineAnalysisExtent(true);
            return;
         }
      }

   }


   /**
    * Adds an input assignment
    * 
    * @param sParamName
    *                the name of the parameter
    * @param sInputName
    *                the name of the input
    * @param alg
    *                the algorithm
    * @return true if could make the assignment
    */
   public boolean addInputAsignment(final String sParamName,
                                    final String sInputName,
                                    final String alg) {

      int i;
      String alg2;
      HashMap map;

      for (i = 0; i < m_Algorithms.size(); i++) {
         alg2 = (String) m_AlgorithmKeys.get(i);
         if (alg.equals(alg2)) {
            map = (HashMap) m_InputAssignments.get(i);
            map.put(sParamName, sInputName);
            return true;
         }
      }

      return false;

   }


   /**
    * Adds an input to the model
    * 
    * @param param
    *                the input parameter to add
    * @throws RepeatedParameterNameException
    */
   public void addInput(final Parameter param) throws RepeatedParameterNameException {

      m_Parameters.addParameter(param);

   }


   /**
    * Removes a parameter input
    * 
    * @param param
    *                the parameter to remove
    */
   public void removeInput(final Parameter param) {

      try {
         m_Parameters.removeParameter(param);
      }
      catch (final WrongParameterIDException e) {}

   }


   /**
    * Removes a parameter input
    * 
    * @param sKey
    *                the name of the parameter to remove
    */
   public void removeInput(final String sKey) {

      try {
         unassign(sKey);
         m_Parameters.removeParameter(sKey);
         m_InputAssignments.remove(sKey);
      }
      catch (final WrongParameterIDException e) {
         Sextante.addErrorToLog(e);
      }

   }


   /**
    * Removes an assignment
    * 
    * @param sAssignmentKey
    *                the key of the assignment to remove
    */
   public void unassign(final String sAssignmentKey) {

      int i, j;
      Set set;
      Iterator iter;
      String sKey;
      String sInput;
      final ArrayList toRemove = new ArrayList();

      for (i = 0; i < m_InputAssignments.size(); i++) {
         final HashMap assignments = (HashMap) m_InputAssignments.get(i);
         set = assignments.keySet();
         iter = set.iterator();
         while (iter.hasNext()) {
            sKey = (String) iter.next();
            sInput = (String) assignments.get(sKey);
            if ((sInput != null) && sInput.equals(sAssignmentKey)) {
               toRemove.add(sKey);
            }

         }
         for (j = 0; j < toRemove.size(); j++) {
            sKey = (String) toRemove.get(j);
            assignments.remove(sKey);
         }
      }

   }


   /**
    * Returns an arraylist with algorithm keys
    * 
    * @return an arraylist with algorithm keys
    */
   public ArrayList getAlgorithmKeys() {

      return m_AlgorithmKeys;

   }


   /**
    * Returns an arraylist with algorithms used in this model
    * 
    * @return an arraylist with algorithms used in this model
    */
   public ArrayList getAlgorithms() {

      return m_Algorithms;

   }


   public ArrayList getInputAssignments() {

      return m_InputAssignments;

   }


   public HashMap getInputs() {

      return m_Inputs;

   }


   /**
    * Returns an algorithm from the ones used in this model
    * 
    * @param sKey
    *                the key used to identify the algorithm to get
    * @return an algorithm from the ones used in this model. Returns null if no algorithm with that key was found
    */
   public GeoAlgorithm getAlgorithm(final String sKey) {

      String sAlgKey;

      for (int i = 0; i < m_AlgorithmKeys.size(); i++) {
         sAlgKey = (String) m_AlgorithmKeys.get(i);
         if (sAlgKey.equals(sKey)) {
            return (GeoAlgorithm) m_Algorithms.get(i);
         }
      }

      return null;

   }


   public HashMap getInputAssignments(final String sKey) {

      String sAlgKey;

      for (int i = 0; i < m_AlgorithmKeys.size(); i++) {
         sAlgKey = (String) m_AlgorithmKeys.get(i);
         if (sAlgKey.equals(sKey)) {
            return (HashMap) m_InputAssignments.get(i);
         }
      }

      return null;

   }


   public String getInputAsignment(final String sParamName,
                                   final GeoAlgorithm alg) {

      int i;
      GeoAlgorithm alg2;
      HashMap map;

      for (i = 0; i < m_Algorithms.size(); i++) {
         alg2 = (GeoAlgorithm) m_Algorithms.get(i);
         if (alg.equals(alg2)) {
            map = (HashMap) m_InputAssignments.get(i);
            return (String) map.get(sParamName);
         }
      }

      return null;

   }


   @Override
   public GeoAlgorithm getNewInstance() throws InstantiationException, IllegalAccessException {

      final ModelAlgorithm alg = this.getClass().newInstance();

      final OutputObjectsSet ooSet = m_OutputObjects.getNewInstance();

      alg.setOutputObjects(ooSet);
      alg.setName(this.getName());
      alg.setValues(m_Algorithms, m_AlgorithmKeys, m_InputAssignments, m_Inputs, ooSet);
      alg.setParameters(m_Parameters);
      alg.setIsDeterminatedProcess(false);
      alg.checkUserCanDefineOutputExtent();
      alg.setAnalysisExtent(m_AnalysisExtent);
      alg.setFilename(m_sFilename);

      if (m_bIsAutoExtent) {
         alg.setAnalysisExtent(null);
         alg.m_bIsAutoExtent = true;
      }

      return alg;

   }


   /**
    * Returns the filename associated with this model
    * 
    * @return the filename associated with this model
    */
   public String getFilename() {

      return m_sFilename;

   }


   /**
    * Sets the filename associated with this model
    * 
    * @param sFilename
    *                the filename where this model is stored
    */
   public void setFilename(final String sFilename) {

      m_sFilename = sFilename;

   }


   @Override
   public String getCommandLineName() {

      return "model:" + new File(m_sFilename).getName();

   }


   @Override
   public boolean canDefineOutputExtentFromInput() {


      for (int i = 0; i < m_Algorithms.size(); i++) {
         if (!((GeoAlgorithm) m_Algorithms.get(i)).canDefineOutputExtentFromInput()) {
            return false;
         }
      }

      return true;

   }


   public void setGeometryTypeRestriction(final int iRestriction) {

      m_iGeometryTypeRestriction = iRestriction;

   }


   public int getGeometryTypeRestriction() {

      return m_iGeometryTypeRestriction;

   }


}
