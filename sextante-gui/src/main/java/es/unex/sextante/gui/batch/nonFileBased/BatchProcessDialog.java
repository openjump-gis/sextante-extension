package es.unex.sextante.gui.batch.nonFileBased;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.WrongAnalysisExtentException;
import es.unex.sextante.exceptions.WrongInputException;
import es.unex.sextante.gui.batch.AnalysisExtentPanel;
import es.unex.sextante.gui.batch.TridimensionalAnalysisExtentPanel;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.TooLargeGridExtentException;

/**
 * A dialog to enter parameter values for a batch process that takes input layers from the GIS, not from files
 */
public class BatchProcessDialog
         extends
            JDialog {

   private final GeoAlgorithm  m_Algorithm;
   private JButton             jButtonCancel;
   private JButton             jButtonOK;
   private JButton             jButtonHelp;
   private ParametersPanel     jParametersPanel;
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
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }
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
