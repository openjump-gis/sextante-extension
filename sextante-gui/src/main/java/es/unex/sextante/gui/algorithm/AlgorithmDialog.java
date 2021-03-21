package es.unex.sextante.gui.algorithm;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.WrongAnalysisExtentException;
import es.unex.sextante.exceptions.WrongInputException;
import es.unex.sextante.gui.core.IGUIFactory;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.LayerCannotBeOverwrittenException;
import es.unex.sextante.gui.exceptions.OverwrittingNotAllowedException;
import es.unex.sextante.gui.exceptions.TooLargeGridExtentException;
import es.unex.sextante.gui.history.DateAndCommand;
import es.unex.sextante.gui.history.History;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.Parameter;


/**
 * A dialog used to introduce all the necessary input for a geoalgorithm (input parameters and raster extent if needed. see
 * {@link es.unex.sextante.core.GeoAlgorithm#getUserCanDefineAnalysisExtent()}) for a given algorithm
 * 
 * @author volaya
 * 
 */
public class AlgorithmDialog
         extends
            JDialog {

   protected GeoAlgorithm                  m_Algorithm;
   protected JTabbedPane                   jTabbedPane1;
   private JPanel                          jPanelButtons;
   private JPanel                          jMainPanel;
   protected JButton                       jButtonCancel;
   protected JButton                       jButtonOK;

   private int                             m_iDialogReturn;

   protected GeoAlgorithmParametersPanel   jPanelParametersMain = null;
   private AnalysisExtentPanel             jAnalysisExtentPanel;
   private JButton                         jButtonHelp;
   private String[]                        m_PreviousParameters;
   private String[]                        m_Extents;
   private JTextField                      jLabelCommand;
   private JButton                         jButtonPrevious;
   private JButton                         jButtonNext;
   private int                             m_iPreviousCommandIndex;
   private final ArrayList<DateAndCommand> m_sCommand;


   /**
    * Creates a new dialog for a given algorithm.
    * 
    * @param algorithm
    *                the algorithm
    * @param parent
    *                the parent dialog
    * @param panel
    *                the parameters panel to use. Doesn't have to be initialized using its init(Geoalgorithm) method. This
    *                constructor will initialize it
    * @param commands
    *                a list of DateAndCommand objects to use as previous parameters set. Must include both the "runalg" commands
    *                and the "extent" ones, in case you want them to be used for algorithms generating new raster layers. If null,
    *                all suitable commands from history are used
    */
   public AlgorithmDialog(final GeoAlgorithm algorithm,
                          final JDialog parent,
                          final GeoAlgorithmParametersPanel panel,
                          final ArrayList<DateAndCommand> commands) {

      super(parent, algorithm.getName(), true);

      setResizable(false);

      m_Algorithm = algorithm;
      m_sCommand = commands;

      jPanelParametersMain = panel;
      jPanelParametersMain.init(m_Algorithm);

      initGUI();
      setLocationRelativeTo(null);

   }


   /**
    * Creates a new dialog for a given algorithm. The main frame of the UI is used as the parent component
    * 
    * @param algorithm
    *                the algorithm
    * @param panel
    *                the parameters panel to use. Doesn't have to be initialized using its init(Geoalgorithm) method. This
    *                constructor will initialize it
    * @param comands
    *                a list of DateAndCommand objects to use as previous parameters set. Must include both the "runalg" commands
    *                and the "extent" ones, in case you want them to be used for algorithms generating new raster layers. If null,
    *                all suitable commands from history are used
    */
   public AlgorithmDialog(final GeoAlgorithm algorithm,
                          final GeoAlgorithmParametersPanel panel,
                          final ArrayList<DateAndCommand> commands) {

      super(SextanteGUI.getMainFrame(), algorithm.getName(), true);

      setResizable(false);

      m_sCommand = commands;
      m_Algorithm = algorithm;

      jPanelParametersMain = panel;
      jPanelParametersMain.init(m_Algorithm);

      initGUI();
      setLocationRelativeTo(null);

   }


   private void initGUI() {


      jMainPanel = new JPanel();

      this.add(jMainPanel);

      final TableLayout thisLayout = new TableLayout(new double[][] { { 10.0, TableLayoutConstants.FILL, 10. },
               { 1.0, 338.0, 37.0 } });
      jMainPanel.setLayout(thisLayout);
      this.setSize(696, 446);
      {
         jTabbedPane1 = new JTabbedPane();
         jMainPanel.add(jTabbedPane1, "1, 1");
         {
            jTabbedPane1.addTab(Sextante.getText("Parameters"), null, jPanelParametersMain, null);
         }
         {
            if (m_Algorithm.getUserCanDefineAnalysisExtent()) {
               jTabbedPane1.addTab(Sextante.getText("Raster_output"), null, getAnalysisExtentPanel(), null);
            }
         }
      }
      {
         jPanelButtons = new JPanel();
         final TableLayout jPanelButtonsLayout = new TableLayout(new double[][] {
                  { 5.0, 45.0, 120.0, 120.0, 45.0, TableLayoutConstants.FILL, 90.0, 90.0, 25.0, 15.0 },
                  { TableLayoutConstants.FILL, 25.0, TableLayoutConstants.FILL } });
         jPanelButtonsLayout.setHGap(5);
         jPanelButtonsLayout.setVGap(5);
         jPanelButtons.setLayout(jPanelButtonsLayout);
         jMainPanel.add(jPanelButtons, "1, 2");
         jPanelButtons.setFocusable(false);
         {
            jButtonOK = new JButton();
            jPanelButtons.add(jButtonOK, "6, 1");
            jButtonOK.setText(Sextante.getText("OK"));
            jButtonOK.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(final java.awt.event.ActionEvent e) {
                  executeAlgorithm();
               }
            });
         }
         {
            jButtonHelp = new JButton();
            jPanelButtons.add(jButtonHelp, "8, 1");
            jButtonHelp.setIcon(new ImageIcon(getClass().getClassLoader().getResource("images/info.gif")));
            jButtonHelp.setPreferredSize(new java.awt.Dimension(33, 0));
            jButtonHelp.addActionListener(new ActionListener() {
               public void actionPerformed(final ActionEvent evt) {
                  showHelp();
               }
            });
         }
         {
            jButtonCancel = new JButton();
            jPanelButtons.add(jButtonCancel, "7, 1");
            jButtonCancel.setText(Sextante.getText("Cancel"));
            jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(final java.awt.event.ActionEvent e) {
                  m_iDialogReturn = IGUIFactory.CANCEL;
                  dispose();
                  setVisible(false);
               }
            });
         }

      }

      retrievePreviouslyUsedParametersFromHistory();
      if (m_PreviousParameters != null) {
         try {
            {
               jButtonPrevious = new JButton();
               jPanelButtons.add(jButtonPrevious, "1, 1");
               jButtonPrevious.setText("<");
               jButtonPrevious.addActionListener(new ActionListener() {
                  public void actionPerformed(final ActionEvent evt) {
                     setPreviousSetOfPreviouslyUsedCommand();
                  }
               });
            }
            {
               jButtonNext = new JButton();
               jPanelButtons.add(jButtonNext, "4, 1");
               jButtonNext.setText(">");
               jButtonNext.addActionListener(new ActionListener() {
                  public void actionPerformed(final ActionEvent evt) {
                     setNextSetOfPreviouslyUsedCommand();
                  }
               });
            }
            {
               jLabelCommand = new JTextField();
               jLabelCommand.setFont(new Font("Monospaced", Font.PLAIN, 10));
               jLabelCommand.setEditable(false);
               jPanelButtons.add(jLabelCommand, "2, 1, 3, 1");
            }

            m_iPreviousCommandIndex = m_PreviousParameters.length - 1;
            setPreviouslyUsedParameters(m_iPreviousCommandIndex);
         }
         catch (final Exception e) {
            // do nothing
         }
      }

   }


   private void setPreviouslyUsedParameters(final int iIndex) {

      int i;
      Parameter param;
      String[] args;
      String sArg;

      jLabelCommand.setText("[" + Integer.toString(iIndex + 1) + "] " + m_PreviousParameters[iIndex]);
      args = m_PreviousParameters[iIndex].split("\"");

      for (int j = 0; j < 2; j++) { //twice to handle dependencies
         final ParametersSet ps = m_Algorithm.getParameters();
         for (i = 0; i < m_Algorithm.getNumberOfParameters(); i++) {
            param = ps.getParameter(i);
            sArg = args[i * 2 + 3];
            jPanelParametersMain.setParameterValue(param.getParameterName(), sArg.trim());
         }
         int iOutputIndex = i * 2 + 3;
         final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
         for (i = 0; i < ooSet.getOutputObjectsCount(); i++) {
            final Output out = ooSet.getOutput(i);
            if ((out instanceof OutputRasterLayer) || (out instanceof Output3DRasterLayer) || (out instanceof OutputVectorLayer)
                || (out instanceof OutputTable)) {
               final String sValue = args[iOutputIndex].trim();
               jPanelParametersMain.setOutputValue(out.getName(), sValue);
               iOutputIndex += 2;
            }
         }
      }

      if (m_Algorithm.getUserCanDefineAnalysisExtent()) {
         String sExtent = m_Extents[iIndex];
         if (sExtent != null) {
            sExtent = sExtent.substring(sExtent.indexOf("(") + 1, sExtent.indexOf(")"));
            final AnalysisExtent ae = new AnalysisExtent();
            final String[] sCoords = sExtent.split(",");
            final double dCellSize = Double.parseDouble(sCoords[6]);
            final double dCellSizeZ = Double.parseDouble(sCoords[7]);
            ae.setCellSize(dCellSize);
            ae.setCellSizeZ(dCellSizeZ);
            final double xMin = Double.parseDouble(sCoords[0]);
            final double xMax = Double.parseDouble(sCoords[3]);
            ae.setXRange(xMin, xMax, true);
            final double yMin = Double.parseDouble(sCoords[1]);
            final double yMax = Double.parseDouble(sCoords[4]);
            ae.setYRange(yMin, yMax, true);
            final double zMin = Double.parseDouble(sCoords[2]);
            final double zMax = Double.parseDouble(sCoords[5]);
            ae.setZRange(zMin, zMax, true);
            getAnalysisExtentPanel().setExtent(ae);
         }
         else {
            getAnalysisExtentPanel().setAutoExtent();
         }
      }

   }


   private void retrievePreviouslyUsedParametersFromHistory() {

      ArrayList<DateAndCommand> dac;
      if (m_sCommand != null) {
         dac = m_sCommand;
      }
      else {
         dac = History.getHistory();
      }
      final ArrayList<String> previousParameters = new ArrayList<String>();
      final ArrayList<String> extents = new ArrayList<String>();
      for (int i = 0; i < dac.size(); i++) {
         String command = dac.get(i).getCommand();
         if (command.startsWith("runalg(\"" + m_Algorithm.getCommandLineName() + "\"")) {
            previousParameters.add(command);
            if (m_Algorithm.getUserCanDefineAnalysisExtent() && (i != 0)) {
               command = dac.get(i - 1).getCommand();
               if (command.startsWith("extent")) {
                  extents.add(command);
               }
               else {
                  extents.add(null);
               }
            }
         }
      }

      if (previousParameters.size() == 0) {
         m_PreviousParameters = null;
         m_Extents = null;
      }
      else {
         m_PreviousParameters = previousParameters.toArray(new String[0]);
         m_Extents = extents.toArray(new String[0]);

      }

   }


   public void setPreviousSetOfPreviouslyUsedCommand() {

      if (m_iPreviousCommandIndex <= 0) {
         //m_iPreviousCommandIndex = m_PreviousParameters.length - 1;
      }
      else {
         m_iPreviousCommandIndex--;
         setPreviouslyUsedParameters(m_iPreviousCommandIndex);
      }


   }


   public void setNextSetOfPreviouslyUsedCommand() {

      if (m_iPreviousCommandIndex >= m_PreviousParameters.length - 1) {
         //m_iPreviousCommandIndex = 0;
      }
      else {
         m_iPreviousCommandIndex++;
         setPreviouslyUsedParameters(m_iPreviousCommandIndex);
      }


   }


   protected void showHelp() {

      SextanteGUI.getGUIFactory().showHelpDialog(m_Algorithm);

   }


   protected void executeAlgorithm() {

      try {
         try {
            assignParameters();
         }
         catch (final TooLargeGridExtentException e) {
            final int iRet = JOptionPane.showConfirmDialog(null, e.getMessage(), Sextante.getText("Warning"),
                     JOptionPane.YES_NO_OPTION);
            if (iRet != JOptionPane.YES_OPTION) {
               this.jTabbedPane1.setSelectedIndex(1);
               return;
            }
         }

         m_iDialogReturn = IGUIFactory.OK;

         dispose();
         setVisible(false);

      }
      catch (final WrongInputException e) {
         JOptionPane.showMessageDialog(null, e.getMessage(), Sextante.getText("Warning"), JOptionPane.WARNING_MESSAGE);
         this.jTabbedPane1.setSelectedIndex(0);
      }
      catch (final WrongAnalysisExtentException e) {
         JOptionPane.showMessageDialog(null, e.getMessage(), Sextante.getText("Warning"), JOptionPane.WARNING_MESSAGE);
         this.jTabbedPane1.setSelectedIndex(1);
      }
      catch (final OverwrittingNotAllowedException e) {
         JOptionPane.showMessageDialog(null, e.getMessage(), Sextante.getText("Warning"), JOptionPane.WARNING_MESSAGE);
         this.jTabbedPane1.setSelectedIndex(0);
      }
      catch (final LayerCannotBeOverwrittenException e) {
         JOptionPane.showMessageDialog(null, e.getMessage(), Sextante.getText("Warning"), JOptionPane.WARNING_MESSAGE);
         this.jTabbedPane1.setSelectedIndex(0);
      }

   }


   protected void assignParameters() throws WrongInputException, OverwrittingNotAllowedException,
                                    LayerCannotBeOverwrittenException, WrongAnalysisExtentException, TooLargeGridExtentException {

      jPanelParametersMain.assignParameters();
      if (m_Algorithm.getUserCanDefineAnalysisExtent()) {
         getAnalysisExtentPanel().assignExtent();
      }

   }


   private AnalysisExtentPanel getAnalysisExtentPanel() {

      if (jAnalysisExtentPanel == null) {
         if (m_Algorithm.is3D()) {
            jAnalysisExtentPanel = new TridimensionalAnalysisExtentPanel(m_Algorithm);
         }
         else {
            jAnalysisExtentPanel = new AnalysisExtentPanel(m_Algorithm);
         }
      }
      return jAnalysisExtentPanel;

   }


   /**
    * Returns {@link es.unex.sextante.gui.core.IGUIFactory#OK} if the user selected "OK" and the algorithm should be executed
    * 
    * @return IGUIFactory.OK if the user selected "OK" and the algorithm should be executed
    */
   public int getDialogReturn() {

      return m_iDialogReturn;

   }


}
