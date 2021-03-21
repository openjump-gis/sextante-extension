

package es.unex.sextante.gui.core;

import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.gui.additionalResults.AdditionalResultsDialog;
import es.unex.sextante.gui.algorithm.AlgorithmDialog;
import es.unex.sextante.gui.algorithm.DefaultParametersPanel;
import es.unex.sextante.gui.algorithm.GeoAlgorithmParametersPanel;
import es.unex.sextante.gui.batch.BatchProcessDialog;
import es.unex.sextante.gui.cmd.BSHDialog;
import es.unex.sextante.gui.dataExplorer.DataExplorerDialog;
import es.unex.sextante.gui.exceptions.WrongViewNameException;
import es.unex.sextante.gui.help.HelpEditionDialog;
import es.unex.sextante.gui.help.HelpIO;
import es.unex.sextante.gui.help.SextanteHelpWindow;
import es.unex.sextante.gui.history.DateAndCommand;
import es.unex.sextante.gui.history.HistoryDialog;
import es.unex.sextante.gui.history.HistoryPanel;
import es.unex.sextante.gui.modeler.DefaultModelerParametersPanel;
import es.unex.sextante.gui.modeler.GeoAlgorithmModelerParametersPanel;
import es.unex.sextante.gui.modeler.ModelAlgorithm;
import es.unex.sextante.gui.modeler.ModelerDialog;
import es.unex.sextante.gui.settings.SextanteConfigurationDialog;
import es.unex.sextante.gui.toolbox.ToolboxDialog;
import es.unex.sextante.gui.toolbox.ToolboxPanel;


/**
 * A default GUIFactory which shows SEXTANTE elements as Swing dialogs
 * 
 * @author volaya
 * 
 */
public class DefaultGUIFactory
         implements
            IGUIFactory {

   protected static ToolboxPanel       m_Toolbox;
   protected static HistoryPanel       m_History;
   private final ArrayList<NamedPoint> m_Coordinates = new ArrayList<NamedPoint>();


   public void showToolBoxDialog() {

      SextanteGUI.getInputFactory().createDataObjects();

      final ToolboxDialog toolbox = new ToolboxDialog(SextanteGUI.getMainFrame());
      m_Toolbox = toolbox.getToolboxPanel();
      toolbox.pack();
      toolbox.setVisible(true);

      SextanteGUI.getInputFactory().clearDataObjects();

      m_Toolbox = null;

   }


   public int showAlgorithmDialog(final GeoAlgorithm alg,
                                  final JDialog parent,
                                  final ArrayList<DateAndCommand> commands) {

      GeoAlgorithmParametersPanel paramPanel = null;
      Class paramPanelClass = SextanteGUI.getParametersPanel(alg.getCommandLineName());

      if (paramPanelClass == null) {
         paramPanelClass = getDefaultParametersPanel();
      }
      try {
         paramPanel = (GeoAlgorithmParametersPanel) paramPanelClass.newInstance();
      }
      catch (final Exception e) {
         try {
            paramPanel = (GeoAlgorithmParametersPanel) getDefaultParametersPanel().newInstance();
         }
         catch (final Exception e1) {
         }
      }

      final Object[] objs = SextanteGUI.getInputFactory().getDataObjects();
      if (alg.meetsDataRequirements(objs)) {
         AlgorithmDialog dialog;
         if (parent != null) {
            dialog = new AlgorithmDialog(alg, parent, paramPanel, commands);
         }
         else {
            dialog = new AlgorithmDialog(alg, paramPanel, commands);
         }
         dialog.pack();
         dialog.setVisible(true);
         return dialog.getDialogReturn();
      }
      else {
         return CANCEL;
      }

   }


   public int showAlgorithmDialogForModeler(final GeoAlgorithm algorithm,
                                            final String sName,
                                            final String sDescription,
                                            final ModelAlgorithm modelAlgorithm,
                                            final HashMap dataObjects,
                                            final JDialog parent) {

      GeoAlgorithmModelerParametersPanel paramPanel = null;
      Class paramPanelClass = SextanteGUI.getModelerParametersPanel(algorithm.getCommandLineName());

      if (paramPanelClass == null) {
         paramPanelClass = getDefaultModelerParametersPanel();
      }
      try {
         paramPanel = (GeoAlgorithmModelerParametersPanel) paramPanelClass.newInstance();
      }
      catch (final Exception e) {
         try {
            paramPanel = (GeoAlgorithmModelerParametersPanel) getDefaultModelerParametersPanel().newInstance();
         }
         catch (final Exception e1) {
         }
      }

      try {
         es.unex.sextante.gui.modeler.AlgorithmDialog dialog;

         if (parent != null) {
            dialog = new es.unex.sextante.gui.modeler.AlgorithmDialog(algorithm.getNewInstance(), sName, sDescription,
                     modelAlgorithm, paramPanel, dataObjects, parent);
         }
         else {
            dialog = new es.unex.sextante.gui.modeler.AlgorithmDialog(algorithm.getNewInstance(), sName, sDescription,
                     modelAlgorithm, paramPanel, dataObjects);
         }

         dialog.pack();
         dialog.setVisible(true);
         return dialog.getDialogReturn();
      }
      catch (final Exception e) {
         return CANCEL;
      }

   }


   public void showSettingsDialog(final ToolboxPanel panel,
                                  final JDialog parent) {

      SextanteConfigurationDialog dialog;

      if (parent != null) {
         dialog = new SextanteConfigurationDialog(panel, parent);
      }
      else {
         dialog = new SextanteConfigurationDialog(panel);
      }

      dialog.pack();
      dialog.setVisible(true);

   }


   public void showModelerDialog() {

      //SextanteGUI.getInputFactory().createDataObjects();

      final ModelerDialog dialog = new ModelerDialog(SextanteGUI.getMainFrame());
      dialog.pack();
      dialog.setVisible(true);

      //SextanteGUI.getInputFactory().clearDataObjects();

   }


   public void showModelerDialog(final ModelAlgorithm alg) {

      final ModelerDialog dialog = new ModelerDialog(SextanteGUI.getMainFrame());
      dialog.getModelerPanel().checkChangesAndOpenModel(alg.getFilename());
      dialog.pack();
      dialog.setVisible(true);

   }


   public void showHelpEditionDialog(final GeoAlgorithm alg) {

      final HelpEditionDialog dialog = new HelpEditionDialog(alg, SextanteGUI.getMainFrame());
      dialog.pack();
      dialog.setVisible(true);

   }


   public void showHelpDialog(final GeoAlgorithm algorithm) {

      final JEditorPane jEditorPane = new JEditorPane();
      jEditorPane.setEditable(false);
      final JScrollPane scrollPane = new JScrollPane();
      scrollPane.setPreferredSize(new Dimension(800, 600));
      scrollPane.setSize(new Dimension(800, 600));
      scrollPane.setMaximumSize(new Dimension(800, 600));
      scrollPane.setViewportView(jEditorPane);
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      jEditorPane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
      jEditorPane.setContentType("text/html");
      final Object help = SextanteGUI.getAlgorithmHelp(algorithm);
      if (help instanceof String) {
         jEditorPane.setText((String) help);
      }
      else if (help instanceof URL) {
         try {
            jEditorPane.setPage((URL) help);
         }
         catch (final Exception e) {
            //will show a blank page
         }
      }
      jEditorPane.setCaretPosition(0);

      SextanteGUI.getGUIFactory().showGenericInfoDialog(scrollPane, Sextante.getText("Help"));

   }


   public void showHelpDialog(final String sTopic) {

      final JEditorPane jEditorPane = new JEditorPane();
      jEditorPane.setEditable(false);
      final JScrollPane scrollPane = new JScrollPane();
      scrollPane.setPreferredSize(new Dimension(800, 600));
      scrollPane.setViewportView(jEditorPane);
      scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      jEditorPane.setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
      jEditorPane.setContentType("text/html");

      try {
         final String sFilepath = HelpIO.getHelpFile(sTopic);
         final URL url = new URL("file:///" + sFilepath);
         jEditorPane.setPage(url);
      }
      catch (final Exception e) {
         //will show a blank page
      }
      jEditorPane.setCaretPosition(0);

      SextanteGUI.getGUIFactory().showGenericInfoDialog(scrollPane, Sextante.getText("Help"));


   }


   public void showAdditionalResultsDialog(final ArrayList components) {

      if (components.size() != 0) {
         final Runnable runnable = new Runnable() {
            public void run() {
               final AdditionalResultsDialog dialog = new AdditionalResultsDialog(components, SextanteGUI.getMainFrame());
               dialog.pack();
               dialog.setVisible(true);
            }
         };

         if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
         }
         else {
            try {
               SwingUtilities.invokeAndWait(runnable);
            }
            catch (final Exception e) {
            }
         }
      }

   }


   public void showHistoryDialog() {

      SextanteGUI.getInputFactory().createDataObjects();

      final HistoryDialog dialog = new HistoryDialog(SextanteGUI.getMainFrame());
      SextanteGUI.setLastCommandOrigin(SextanteGUI.HISTORY);
      SextanteGUI.setLastCommandOriginParentDialog(dialog);
      m_History = dialog.getHistoryPanel();
      dialog.pack();
      dialog.setVisible(true);

      SextanteGUI.getInputFactory().clearDataObjects();

      m_History = null;

   }


   public void showCommandLineDialog() {

      SextanteGUI.getInputFactory().createDataObjects();

      final BSHDialog dialog = new BSHDialog(SextanteGUI.getMainFrame());
      SextanteGUI.setLastCommandOrigin(SextanteGUI.COMMANDLINE);
      SextanteGUI.setLastCommandOriginParentDialog(dialog);
      dialog.pack();
      dialog.setVisible(true);

      SextanteGUI.getInputFactory().clearDataObjects();

   }


   public void showGenericInfoDialog(final Component component,
                                     final String text) {

      final GenericInfoDialog dialog = new GenericInfoDialog(component, text, SextanteGUI.getMainFrame());
      dialog.setVisible(true);

   }


   public void showBatchProcessingDialog(final GeoAlgorithm alg,
                                         final JDialog parent) {

      BatchProcessDialog dialog;

      if (parent != null) {
         dialog = new BatchProcessDialog(alg, parent);
      }
      else {
         dialog = new BatchProcessDialog(alg);
      }

      dialog.pack();
      dialog.setVisible(true);

   }


   public void showBatchProcessingFromGISDialog(final GeoAlgorithm alg,
                                                final JDialog parent) {

      es.unex.sextante.gui.batch.nonFileBased.BatchProcessDialog dialog;

      if (parent != null) {
         dialog = new es.unex.sextante.gui.batch.nonFileBased.BatchProcessDialog(alg, parent);
      }
      else {
         dialog = new es.unex.sextante.gui.batch.nonFileBased.BatchProcessDialog(alg);
      }

      dialog.pack();
      dialog.setVisible(true);

   }


   public void updateToolbox() {

      if (m_Toolbox != null) {
         m_Toolbox.fillTreesWithAllAlgorithms();
      }

   }


   public void updateHistory() {

      if (m_History != null) {
         m_History.updateContent();
      }

   }


   public ArrayList<NamedPoint> getCoordinatesList() {

      return m_Coordinates;

   }


   public void showDataExplorer() {

      SextanteGUI.getInputFactory().createDataObjects();

      final DataExplorerDialog dialog = new DataExplorerDialog(SextanteGUI.getMainFrame());
      dialog.pack();
      dialog.setVisible(true);

      if (m_Toolbox == null) {
         SextanteGUI.getInputFactory().clearDataObjects();
      }

   }


   public Class getDefaultModelerParametersPanel() {

      return DefaultModelerParametersPanel.class;

   }


   public Class getDefaultParametersPanel() {

      return DefaultParametersPanel.class;

   }


   public void showHelpWindow() {

      final SextanteHelpWindow window = new SextanteHelpWindow();
      window.setExtendedState(window.getExtendedState() | JFrame.MAXIMIZED_BOTH);
      window.pack();
      window.setVisible(true);

   }


   public void addToView(final IDataObject obj,
                         final String viewName) throws WrongViewNameException {


   }


   public HashMap<NameAndIcon, ArrayList<ToolboxAction>> getToolboxActions() {

      return new HashMap<NameAndIcon, ArrayList<ToolboxAction>>();

   }


   public void showGenericDialog(final String name,
                                 final Component component) {

      final GenericDialog dialog = new GenericDialog(component, name, SextanteGUI.getMainFrame());
      dialog.pack();
      dialog.setVisible(true);

   }

}
