package es.unex.sextante.vectorTools.dissolve;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.WrongInputException;
import es.unex.sextante.exceptions.WrongParameterIDException;
import es.unex.sextante.gui.algorithm.GeoAlgorithmParametersPanel;
import es.unex.sextante.gui.algorithm.OutputChannelSelectionPanel;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.LayerCannotBeOverwrittenException;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OverwriteOutputChannel;

public class DissolveParametersPanel
         extends
            GeoAlgorithmParametersPanel {

   private GeoAlgorithm                m_Algorithm;
   private JLabel                      jLabelGroupings;
   private JComboBox                   jComboBoxField;
   private JTable                      jTableGroupings;
   private JComboBox                   jComboBoxLayers;
   private JLabel                      jLabelField;
   private JLabel                      jLabelLayer;
   private JScrollPane                 jScrollPaneFields;
   private OutputChannelSelectionPanel outputChannelSelectionPanel;


   public DissolveParametersPanel() {

      super();

   }


   @Override
   public void init(final GeoAlgorithm algorithm) {

      m_Algorithm = algorithm;

      initGUI();

   }


   @Override
   public void assignParameters() throws WrongInputException, LayerCannotBeOverwrittenException {

      final ObjectAndDescription oad = (ObjectAndDescription) jComboBoxLayers.getSelectedItem();
      final IVectorLayer layer = (IVectorLayer) oad.getObject();
      final ParametersSet params = m_Algorithm.getParameters();
      try {
         params.getParameter(DissolveAlgorithm.LAYER).setParameterValue(layer);
         params.getParameter(DissolveAlgorithm.GROUPING_FIELD).setParameterValue(jComboBoxField.getSelectedIndex());
         final DissolveTableModel model = (DissolveTableModel) jTableGroupings.getModel();
         params.getParameter(DissolveAlgorithm.GROUPING_FUNCTIONS).setParameterValue(model.getAsString());
      }
      catch (final WrongParameterIDException e) {
         throw new WrongInputException();
      }


      final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
      Output out;
      IOutputChannel channel;
      try {
         out = ooSet.getOutput(DissolveAlgorithm.RESULT);
         channel = outputChannelSelectionPanel.getOutputChannel();
      }
      catch (final Exception e) {
         throw new WrongInputException();
      }

      if (channel instanceof OverwriteOutputChannel) {
         final OverwriteOutputChannel ooc = (OverwriteOutputChannel) channel;
         if (!ooc.getLayer().canBeEdited()) {
            throw new LayerCannotBeOverwrittenException();
         }
      }

      out.setOutputChannel(channel);


   }


   private void initGUI() {
      try {
         {
            final TableLayout thisLayout = new TableLayout(new double[][] {
                     { 5.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 5.0 },
                     { 5.0, 25.0, 5.0, 25.0, 5.0, 25.0, TableLayoutConstants.FILL, 5.0, 20.0, 5.0 } });
            thisLayout.setHGap(5);
            thisLayout.setVGap(5);
            this.setLayout(thisLayout);
            this.setPreferredSize(new java.awt.Dimension(469, 257));
            {
               jLabelGroupings = new JLabel();
               this.add(jLabelGroupings, "1, 5");
               jLabelGroupings.setText(Sextante.getText("Summary_statistics"));
            }
            {
               jScrollPaneFields = new JScrollPane();
               jScrollPaneFields.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
               this.add(jScrollPaneFields, "1, 6, 2, 6");
               {
                  jTableGroupings = new JTable();
                  jScrollPaneFields.setViewportView(jTableGroupings);
               }
            }
            {
               jLabelLayer = new JLabel();
               this.add(jLabelLayer, "1, 1");
               jLabelLayer.setText(Sextante.getText("Layer"));
            }
            {
               jLabelField = new JLabel();
               this.add(jLabelField, "1, 3");
               jLabelField.setText(Sextante.getText("Field"));
            }
            {
               jComboBoxField = new JComboBox();
               this.add(jComboBoxField, "2, 3");
            }
            {
               final ComboBoxModel jComboBoxLayersModel = new DefaultComboBoxModel(getLayers());
               jComboBoxLayers = new JComboBox();
               this.add(jComboBoxLayers, "2, 1");
               jComboBoxLayers.setModel(jComboBoxLayersModel);
               jComboBoxLayers.addItemListener(new java.awt.event.ItemListener() {
                  public void itemStateChanged(final java.awt.event.ItemEvent e) {
                     setFields();
                     setTableModel();
                  }
               });
               jComboBoxLayers.setSelectedIndex(0);
               setFields();
               setTableModel();
            }
            {
               final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
               final Output out = ooSet.getOutput(DissolveAlgorithm.RESULT);
               outputChannelSelectionPanel = new OutputChannelSelectionPanel(out, m_Algorithm.getParameters());
               this.add(outputChannelSelectionPanel, "2,8");
               this.add(new JLabel(Sextante.getText("Result")), "1,8");
            }
         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }
   }


   protected void setFields() {

      final ComboBoxModel jComboBoxFieldModel = new DefaultComboBoxModel(getFields());
      jComboBoxField.setModel(jComboBoxFieldModel);

   }


   private void setTableModel() {

      final ObjectAndDescription oad = (ObjectAndDescription) jComboBoxLayers.getSelectedItem();
      final IVectorLayer layer = (IVectorLayer) oad.getObject();
      final DissolveTableModel tableModel = new DissolveTableModel(layer);
      jTableGroupings.setModel(tableModel);

   }


   private String[] getFields() {

      final ObjectAndDescription oad = (ObjectAndDescription) jComboBoxLayers.getSelectedItem();
      final IVectorLayer layer = (IVectorLayer) oad.getObject();
      return getFields(layer);

   }


   private String[] getFields(final IVectorLayer layer) {

      return layer.getFieldNames();

   }


   private ObjectAndDescription[] getLayers() {

      final IVectorLayer[] layers = SextanteGUI.getInputFactory().getVectorLayers(IVectorLayer.SHAPE_TYPE_POLYGON);
      final ObjectAndDescription[] oad = new ObjectAndDescription[layers.length];
      for (int i = 0; i < layers.length; i++) {
         oad[i] = new ObjectAndDescription(layers[i].getName(), layers[i]);
      }
      return oad;

   }


   @Override
   public void setOutputValue(final String sOutputName,
                              final String sValue) {

      outputChannelSelectionPanel.setText(sValue);

   }


   @Override
   public void setParameterValue(final String parameterName,
                                 final String value) {
   // TODO Auto-generated method stub

   }

}
