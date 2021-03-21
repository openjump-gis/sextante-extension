

package es.unex.sextante.vectorTools.dissolveMultiple;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

import org.japura.gui.Anchor;
import org.japura.gui.BatchSelection;
import org.japura.gui.CheckComboBox;
import org.japura.gui.EmbeddedComponent;
import org.japura.gui.model.ListCheckModel;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
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


public class DissolveMultipleParametersPanel
         extends
            GeoAlgorithmParametersPanel {

   private GeoAlgorithm                m_Algorithm;
   private JLabel                      jLabelGroupings;
   private CheckComboBox               fieldsBox;
   private JTable                      jTableGroupings;
   private JComboBox                   jComboBoxLayers;
   private JLabel                      jLabelField;
   private JLabel                      jLabelLayer;
   private JScrollPane                 jScrollPaneFields;
   private OutputChannelSelectionPanel outputChannelSelectionPanel;


   public DissolveMultipleParametersPanel() {

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
         params.getParameter(DissolveMultipleAlgorithm.LAYER).setParameterValue(layer);
         final StringBuffer sb = new StringBuffer();
         final List<Object> selected = fieldsBox.getModel().getCheckeds();
         for (int i = 0; i < selected.size(); i++) {
            if (i == 0) {
               sb.append(selected.get(i).toString());
            }
            else {
               sb.append("," + selected.get(i).toString());
            }
         }
         params.getParameter(DissolveMultipleAlgorithm.GROUPING_FIELDS).setParameterValue(sb.toString());
         final DissolveTableModel model = (DissolveTableModel) jTableGroupings.getModel();
         params.getParameter(DissolveMultipleAlgorithm.GROUPING_FUNCTIONS).setParameterValue(model.getAsString());
      }
      catch (final WrongParameterIDException e) {
         throw new WrongInputException();
      }


      IOutputChannel channel;
      Output out;
      try {
         final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
         out = ooSet.getOutput(DissolveMultipleAlgorithm.RESULT);
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
               jLabelField.setText(Sextante.getText("Fields"));
            }
            {
               fieldsBox = new CheckComboBox();
               fieldsBox.setTextFor(CheckComboBox.NONE, Sextante.getText("no_elements_selected"));
               fieldsBox.setTextFor(CheckComboBox.MULTIPLE, Sextante.getText("multiple_elements_selected"));
               final BatchSelection bs = new BatchSelection.CheckBox();
               final EmbeddedComponent comp = new EmbeddedComponent(bs, Anchor.NORTH);
               fieldsBox.setEmbeddedComponent(comp);
               this.add(fieldsBox, "2, 3");
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
               //fieldsBox.initGUI();
               setTableModel();
            }
            {
               final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
               final Output out = ooSet.getOutput(DissolveMultipleAlgorithm.RESULT);
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

      final String[] fields = getFields();
      final ListCheckModel model = fieldsBox.getModel();
      model.clear();
      for (final Object obj : fields) {
         model.addElement(obj);
      }

      fieldsBox.setModel(model);
      //fieldsBox.setFields(getFields());

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

      final IVectorLayer[] layers = SextanteGUI.getInputFactory().getVectorLayers(AdditionalInfoVectorLayer.SHAPE_TYPE_ANY);
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
