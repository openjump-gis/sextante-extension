package es.unex.sextante.gui.batch;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.WrongAnalysisExtentException;
import es.unex.sextante.exceptions.WrongInputException;
import es.unex.sextante.gui.algorithm.GenericFileFilter;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.TooLargeGridExtentException;

/**
 * A dialog to enter parameter values for a batch process
 */
public class BatchProcessDialog
         extends
            JDialog {

   private final GeoAlgorithm  m_Algorithm;
   private JButton             jButtonCancel;
   private JButton             jButtonOK;
   private JButton             jButtonHelp;
   private ParametersPanel     jParametersPanel;
   private JButton             jButtonSave;
   private JButton             jButtonOpen;
   private AnalysisExtentPanel jRasterExtentPanel;
   private JTabbedPane         jTabbedPanel;
   private JLabel              jLabelName;
   private final JDialog       m_Parent;


   /**
    * Constructor
    * 
    * @param alg
    *                the algorithm to execute in the batch process.
    * @param parent
    *                the parent dialog
    */
   public BatchProcessDialog(final GeoAlgorithm alg,
                             final JDialog parent) {

      super(parent, Sextante.getText("Batch_processing"), true);

      m_Algorithm = alg;
      m_Parent = parent;

      initGUI();

      this.setLocationRelativeTo(null);

   }


   /**
    * Constructor
    * 
    * @param alg
    *                the algorithm to execute in the batch process.
    */
   public BatchProcessDialog(final GeoAlgorithm alg) {

      super(SextanteGUI.getMainFrame(), Sextante.getText("Batch_processing"), true);
      this.setLocationRelativeTo(null);

      m_Algorithm = alg;
      m_Parent = null;

      initGUI();

   }


   private void initGUI() {

      try {
         final TableLayout thisLayout = new TableLayout(new double[][] {
                  { 5.0, 70.0, TableLayoutConstants.FILL, 100.0, 100.0, TableLayoutConstants.FILL, 5.0, 100.0, 100.0, 5.0 },
                  { 5.0, 20.0, 5.0, TableLayoutConstants.FILL, 5.0, 25.0, 5.0 } });
         getContentPane().setLayout(thisLayout);
         this.setPreferredSize(new java.awt.Dimension(800, 600));
         this.setSize(new java.awt.Dimension(800, 600));
         {
            jButtonHelp = new JButton();
            getContentPane().add(jButtonHelp, "1, 5");
            jButtonHelp.setText(Sextante.getText("Help"));
            jButtonHelp.addActionListener(new ActionListener() {
               public void actionPerformed(final ActionEvent evt) {
                  showHelp();
               }
            });
         }
         {
            jTabbedPanel = new JTabbedPane();
            getContentPane().add(jTabbedPanel, "1, 3, 8, 3");
            {
               jParametersPanel = new ParametersPanel(m_Algorithm);
               jTabbedPanel.addTab(Sextante.getText("Parameters"), null, jParametersPanel, null);
               jParametersPanel.setPreferredSize(new java.awt.Dimension(446, 225));
            }
            {
               if (m_Algorithm.getUserCanDefineAnalysisExtent()) {
                  if (m_Algorithm.is3D()) {
                     jRasterExtentPanel = new TridimensionalAnalysisExtentPanel(m_Algorithm);
                  }
                  else {
                     jRasterExtentPanel = new AnalysisExtentPanel(m_Algorithm);
                  }
                  jTabbedPanel.addTab(Sextante.getText("Raster_output"), null, jRasterExtentPanel, null);
               }
            }

         }
         {
            jButtonOK = new JButton();
            getContentPane().add(jButtonOK, "7, 5");
            jButtonOK.setText(Sextante.getText("OK"));
            jButtonOK.addActionListener(new ActionListener() {
               public void actionPerformed(final ActionEvent evt) {
                  executeBatchProcess();
               }
            });
         }
         {
            jButtonCancel = new JButton();
            getContentPane().add(jButtonCancel, "8, 5");
            jButtonCancel.setText(Sextante.getText("Cancel"));
            jButtonCancel.addActionListener(new ActionListener() {
               public void actionPerformed(final ActionEvent evt) {
                  cancel();
               }
            });
         }
         {
            jLabelName = new JLabel(m_Algorithm.getName().toUpperCase());
            jLabelName.setFont(new java.awt.Font("Tahoma", 1, 12));
            getContentPane().add(jLabelName, "1, 1, 7, 1");
         }
         {
            jButtonOpen = new JButton();
            getContentPane().add(jButtonOpen, "3, 5");
            jButtonOpen.setText(Sextante.getText("Open"));
            jButtonOpen.addActionListener(new ActionListener() {
               public void actionPerformed(final ActionEvent evt) {
                  open();
               }
            });
         }
         {
            jButtonSave = new JButton();
            getContentPane().add(jButtonSave, "4, 5");
            jButtonSave.setText(Sextante.getText("Save"));
            jButtonSave.addActionListener(new ActionListener() {
               public void actionPerformed(final ActionEvent evt) {
                  save();
               }
            });
         }

      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }
   }


   private void save() {

      final ParametersTableModel table = jParametersPanel.getTableModel();

      final JFileChooser fc = new JFileChooser();
      final GenericFileFilter filter = new GenericFileFilter("csv", "Comma-Separated Values");

      fc.setFileFilter(filter);
      final int returnVal = fc.showSaveDialog(this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
         final File file = fc.getSelectedFile();
         Writer output = null;
         try {
            output = new BufferedWriter(new FileWriter(file));
            final int iRows = table.getRowCount();
            final int iCols = table.getColumnCount();
            for (int iRow = 0; iRow < iRows; iRow++) {
               for (int iCol = 0; iCol < iCols; iCol++) {
                  final String s = table.getValueAt(iRow, iCol).toString();
                  output.write(s);
                  if (iCol < (iCols - 1)) {
                     output.write("|");
                  }
               }
               output.write("\n");
            }
         }
         catch (final IOException e) {
            Sextante.addErrorToLog(e);
         }
         finally {
            if (output != null) {
               try {
                  output.close();
               }
               catch (final IOException e) {
                  Sextante.addErrorToLog(e);
               }
            }
         }
      }

   }


   private void open() {

      try {
         final JFileChooser fc = new JFileChooser();
         final GenericFileFilter filter = new GenericFileFilter("csv", "Comma-Separated Values");

         fc.setFileFilter(filter);
         final int returnVal = fc.showOpenDialog(this);

         if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();
            BufferedReader input = null;

            final ParametersTableModel table = jParametersPanel.getTableModel();
            final int iRows = table.getRowCount();

            input = new BufferedReader(new FileReader(file));
            String sLine = null;
            int i = 1;
            while ((sLine = input.readLine()) != null) {
               if (i > iRows) {
                  table.addRow();
               }
               processLine(sLine, table, i - 1);
               i++;
            }
            input.close();
         }
      }
      catch (final Exception e) {
         JOptionPane.showMessageDialog(this, Sextante.getText("Could_not_open_selected_file"), Sextante.getText("Error"),
                  JOptionPane.ERROR_MESSAGE);
      }


   }


   private void processLine(final String line,
                            final ParametersTableModel table,
                            final int iRow) {

      try {
         final String[] tokens = line.split("\\|");
         for (int i = 0; i < tokens.length; i++) {
            table.setValueAt(tokens[i], iRow, i);
         }
      }
      catch (final Exception e) {}

   }


   private void showHelp() {

      SextanteGUI.getGUIFactory().showHelpDialog("batch");

   }


   private void executeBatchProcess() {

      final ArrayList param = new ArrayList();
      final ArrayList output = new ArrayList();

      try {
         try {
            assignParameters(param, output);
         }
         catch (final TooLargeGridExtentException e) {
            final int iRet = JOptionPane.showConfirmDialog(null, e.getMessage(), Sextante.getText("Warning"),
                     JOptionPane.YES_NO_OPTION);
            if (iRet != JOptionPane.YES_OPTION) {
               this.jTabbedPanel.setSelectedIndex(1);
               return;
            }
         }
         AnalysisExtent extent = null;
         if (jRasterExtentPanel != null) {
            extent = jRasterExtentPanel.getExtent();
         }

         final BatchProcessMonitorableTask task = new BatchProcessMonitorableTask(m_Algorithm, param, output, extent, m_Parent);

         final Thread th = new Thread(task);
         th.start();

         cancel();

      }
      catch (final WrongInputException e) {
         JOptionPane.showMessageDialog(null, e.getMessage(), Sextante.getText("Warning"), JOptionPane.WARNING_MESSAGE);
         this.jTabbedPanel.setSelectedIndex(0);
      }
      catch (final WrongAnalysisExtentException e) {
         JOptionPane.showMessageDialog(null, e.getMessage(), Sextante.getText("Warning"), JOptionPane.WARNING_MESSAGE);
         this.jTabbedPanel.setSelectedIndex(1);
      }

   }


   private void assignParameters(final ArrayList param,
                                 final ArrayList output) throws WrongInputException, WrongAnalysisExtentException,
                                                        TooLargeGridExtentException {

      if (!jParametersPanel.assignParameters(param, output)) {
         throw new WrongInputException(Sextante.getText("Wrong_or_missing_parameters_definition"));
      }

      if (m_Algorithm.getUserCanDefineAnalysisExtent()) {
         jRasterExtentPanel.adjustExtent();
      }


   }


   private void cancel() {

      dispose();
      setVisible(false);

   }

}
