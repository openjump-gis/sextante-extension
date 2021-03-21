package es.unex.sextante.gui.algorithm.iterative;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ITaskMonitor;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterIDException;
import es.unex.sextante.exceptions.WrongParameterTypeException;
import es.unex.sextante.gui.additionalResults.AdditionalResults;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputNumericalValue;

/**
 * A task to execute an algorithm iteratively over all the features of a vector layer
 * 
 * @author volaya
 * 
 */
public class IterativeAlgorithmMonitorableTask
         implements
            Runnable {

   private String                                           m_sErrorMessage;
   private final GeoAlgorithm                               m_Algorithm;
   private ITaskMonitor                                     m_ProgressMonitor = null;
   private final String                                     m_sParameterToIterateOver;
   private final SingleFeatureVectorLayer                   m_SFVL;
   private final IFeatureIterator                           m_Iterator;
   private final int                                        m_iShapesCount;
   private String                                           m_sLayerName;
   private final ArrayList<ArrayList<OutputNumericalValue>> m_NumericalOutputs;


   /**
    * Creates a new task
    * 
    * @param algorithm
    *                the algorithm to execute
    * @param parent
    *                the dialog from which it has been called, to be used as parent for the monitor dialog
    * @param sParameterToIterateOver
    *                the name of the parameter to iterate over
    * @throws WrongParameterIDException
    * @throws NullParameterValueException
    * @throws WrongParameterTypeException
    */
   public IterativeAlgorithmMonitorableTask(final GeoAlgorithm algorithm,
                                            final JDialog parent,
                                            final String sParameterToIterateOver) throws GeoAlgorithmExecutionException {

      try {
         m_Algorithm = algorithm;
         m_NumericalOutputs = new ArrayList<ArrayList<OutputNumericalValue>>();
         m_sParameterToIterateOver = sParameterToIterateOver;
         m_ProgressMonitor = SextanteGUI.getOutputFactory().getTaskMonitor(algorithm.getName(),
                  algorithm.isDeterminatedProcess(), parent);
         final IVectorLayer layer = m_Algorithm.getParameters().getParameter(m_sParameterToIterateOver).getParameterValueAsVectorLayer();
         m_Iterator = layer.iterator();
         m_iShapesCount = layer.getShapesCount();
         m_SFVL = new SingleFeatureVectorLayer(layer);
         m_Algorithm.getParameters().getParameter(m_sParameterToIterateOver).setParameterValue(m_SFVL);
         m_sLayerName = layer.getName();
      }
      catch (final Exception e) {
         if (m_ProgressMonitor != null) {
            m_ProgressMonitor.close();
         }
         throw new GeoAlgorithmExecutionException(e.getMessage());
      }

   }


   /**
    * Runs the batch process
    */
   public void run() {

      final OutputObjectsSet ooset = m_Algorithm.getOutputObjects();
      final HashMap<String, String> filenames = new HashMap<String, String>();
      for (int i = 0; i < ooset.getOutputObjectsCount(); i++) {
         final Output output = ooset.getOutput(i);
         final IOutputChannel channel = output.getOutputChannel();
         if (channel != null) {
            if (channel instanceof FileOutputChannel) {
               final FileOutputChannel foc = (FileOutputChannel) channel;
               filenames.put(output.getName(), foc.getFilename());
            }
            else {
               m_sErrorMessage = Sextante.getText("Unsupported output channel:") + channel.toString();
               JOptionPane.showMessageDialog(null, m_sErrorMessage, Sextante.getText("Error"), JOptionPane.ERROR_MESSAGE);
            }
         }

      }

      int iIteration = 1;

      while (m_Iterator.hasNext()) {
         try {
            final IFeature feature = m_Iterator.next();
            m_SFVL.setFeature(feature.getGeometry(), feature.getRecord().getValues());
            m_SFVL.setName(m_sLayerName + "[" + Integer.toString(iIteration) + "]");
            final Set<String> names = filenames.keySet();
            for (final Iterator iter = names.iterator(); iter.hasNext();) {
               final String sName = (String) iter.next();
               String sFilename = filenames.get(sName);
               if (sFilename == null) {
                  ooset.getOutput(sName).setOutputChannel(new FileOutputChannel(null));
               }
               else {
                  sFilename = sFilename + "_" + Integer.toString(iIteration);
                  ooset.getOutput(sName).setOutputChannel(new FileOutputChannel(sFilename));
               }
            }

            m_ProgressMonitor.setDescriptionPrefix("[" + Integer.toString(iIteration) + "/" + Integer.toString(m_iShapesCount)
                                                   + "] ");

            IterativeAlgorithmSingleUnit unit;
            final GeoAlgorithm alg = m_Algorithm.getNewInstance();
            unit = new IterativeAlgorithmSingleUnit(alg, m_ProgressMonitor, Integer.toString(iIteration), m_NumericalOutputs);

            final ExecutorService pool = Executors.newFixedThreadPool(2);
            final Future<Boolean> future = pool.submit(unit);
            Boolean success = null;
            success = future.get();
            if (!success.booleanValue() || m_ProgressMonitor.isCanceled()) {
               break;
            }
            iIteration++;
         }
         catch (final Exception e) {
            Sextante.addErrorToLog(e);
            m_sErrorMessage = e.getMessage();
            break;
         }
      }

      m_ProgressMonitor.close();

      if (m_sErrorMessage != null) {
         JOptionPane.showMessageDialog(null, m_sErrorMessage, Sextante.getText("Error"), JOptionPane.ERROR_MESSAGE);
      }

      final JScrollPane table = createNumericalOutputsTable();

      SextanteGUI.getInputFactory().clearDataObjects();
      SextanteGUI.getInputFactory().createDataObjects();

      if (table != null) {
         AdditionalResults.addComponent(new ObjectAndDescription(m_Algorithm.getName() + "[" + m_sLayerName + "]", table));
      }

   }


   private JScrollPane createNumericalOutputsTable() {

      if (m_NumericalOutputs.size() == 0) {
         return null;
      }

      final JScrollPane jScrollPane = new JScrollPane();
      final JTable jTable = new JTable();
      jScrollPane.setViewportView(jTable);


      final int iRecordsCount = m_NumericalOutputs.size();
      final int iFieldsCount = m_NumericalOutputs.get(0).size();
      final String[] fields = new String[iFieldsCount];
      for (int i = 0; i < fields.length; i++) {
         fields[i] = m_NumericalOutputs.get(0).get(i).getDescription();
      }

      final String[][] data = new String[iRecordsCount][fields.length];

      for (int i = 0; i < iRecordsCount; i++) {
         for (int j = 0; j < iFieldsCount; j++) {
            data[i][j] = m_NumericalOutputs.get(i).get(j).getOutputObject().toString();
         }
      }

      final DefaultTableModel model = new DefaultTableModel();
      model.setDataVector(data, fields);
      jTable.setModel(model);
      jTable.setEnabled(false);

      return jScrollPane;

   }

}
