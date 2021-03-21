package es.unex.sextante.gui.batch.nonFileBased;

import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ITaskMonitor;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.additionalResults.AdditionalResults;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.outputs.OutputNumericalValue;

/**
 * A task to execute a batch process.
 * 
 * @author volaya
 * 
 */
public class BatchProcessMonitorableTask
         implements
            Runnable {

   private StringBuffer                                     m_sLog;
   private final ArrayList                                  m_Parameters;
   private final ArrayList                                  m_Outputs;
   private final AnalysisExtent                             m_AnalysisExtent;
   private final GeoAlgorithm                               m_Algorithm;
   private final ITaskMonitor                               m_ProgressMonitor;
   private final ArrayList<ArrayList<OutputNumericalValue>> m_NumericalOutputs;


   /**
    * Creates a new task
    * 
    * @param algorithm
    *                the algorithm to execute
    * @param parameters
    *                a list of maps, each one of them containing parameter values for each execution of the algorithm
    * @param outputs
    *                a list of maps, each one of them containing output settings for each execution of the algorithm
    * @param analysisExtent
    *                the output extent to use
    */
   public BatchProcessMonitorableTask(final GeoAlgorithm algorithm,
                                      final ArrayList parameters,
                                      final ArrayList outputs,
                                      final AnalysisExtent analysisExtent,
                                      final JDialog parent) {

      m_Parameters = parameters;
      m_Outputs = outputs;
      m_NumericalOutputs = new ArrayList<ArrayList<OutputNumericalValue>>();
      m_AnalysisExtent = analysisExtent;
      m_Algorithm = algorithm;
      m_ProgressMonitor = SextanteGUI.getOutputFactory().getTaskMonitor(algorithm.getName(), algorithm.isDeterminatedProcess(),
               parent);

   }


   /**
    * Runs the batch process
    */
   public void run() {

      final String LINE = "----------------------------------------\n";

      BatchProcessSingleUnit unit = null;

      m_sLog = new StringBuffer();

      for (int i = 0; i < m_Parameters.size(); i++) {

         m_sLog.append(Sextante.getText("Process") + " " + Integer.toString(i + 1) + "\n");
         m_sLog.append(LINE);

         m_ProgressMonitor.setDescriptionPrefix("[" + Integer.toString(i + 1) + "/" + Integer.toString(m_Parameters.size()) + "]");

         try {
            final HashMap param = (HashMap) m_Parameters.get(i);
            final HashMap output = (HashMap) m_Outputs.get(i);
            final GeoAlgorithm alg = m_Algorithm.getNewInstance();
            unit = new BatchProcessSingleUnit(alg, param, output, m_AnalysisExtent, m_ProgressMonitor, m_NumericalOutputs);

            final ExecutorService pool = Executors.newFixedThreadPool(2);
            final Future<Boolean> future = pool.submit(unit);
            Boolean success = null;
            success = future.get();
            if (!success.booleanValue() || m_ProgressMonitor.isCanceled()) {
               m_sLog.append(unit.getLog());
               m_sLog.append(Sextante.getText("Error_or_process_canceled_by_user") + "\n");
               break;
            }
            //getAdditionalResults(alg);
            m_sLog.append(unit.getLog().toString());
            m_sLog.append("OK!\n");
         }
         catch (final InterruptedException e) {
            m_sLog.append(unit.getLog().toString());
         }
         catch (final ExecutionException e) {
            m_sLog.append(unit.getLog().toString());
         }
         catch (final Exception e) {
            Sextante.addErrorToLog(e);
         }

         m_sLog.append(LINE + "\n");

         SextanteGUI.getInputFactory().clearDataObjects();
         SextanteGUI.getInputFactory().createDataObjects();

      }

      final JScrollPane table = createNumericalOutputsTable();

      if (table != null) {
         AdditionalResults.addComponent(new ObjectAndDescription(m_Algorithm.getName() + "["
                                                                 + Sextante.getText("Batch_processing") + "]", table));
      }

      m_ProgressMonitor.close();

      final JTextArea jTextArea = new JTextArea();
      jTextArea.setEditable(false);
      jTextArea.setText(m_sLog.toString());
      jTextArea.setFont(new Font("Courier", Font.PLAIN, 11));
      final JScrollPane scrollPane = new JScrollPane();
      scrollPane.setPreferredSize(new Dimension(600, 400));
      scrollPane.setViewportView(jTextArea);
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      jTextArea.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));

      SextanteGUI.getGUIFactory().showGenericInfoDialog(scrollPane, Sextante.getText("Batch_processing"));

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
