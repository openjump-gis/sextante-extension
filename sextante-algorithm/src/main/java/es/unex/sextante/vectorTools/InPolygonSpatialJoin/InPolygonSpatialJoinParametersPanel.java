package es.unex.sextante.vectorTools.InPolygonSpatialJoin;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.WrongInputException;
import es.unex.sextante.gui.algorithm.GeoAlgorithmParametersPanel;
import es.unex.sextante.gui.algorithm.OutputChannelSelectionPanel;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.LayerCannotBeOverwrittenException;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OverwriteOutputChannel;


public class InPolygonSpatialJoinParametersPanel
         extends
            GeoAlgorithmParametersPanel {

   private GeoAlgorithm                m_Algorithm;
   private JLabel                      jLabelGroupings;
   private JTable                      jTableGroupings;
   private JComboBox                   jComboBoxPolygonLayers;
   private JComboBox                   jComboBoxPointsLayers;
   private JLabel                      jLabelPointsLayer;
   private JLabel                      jLabelPolygonsLayer;
   private JScrollPane                 jScrollPaneGroupings;
   private OutputChannelSelectionPanel outputChannelSelectionPanel;


   public InPolygonSpatialJoinParametersPanel() {

      super();

   }


   @Override
   public void init(final GeoAlgorithm algorithm) {

      m_Algorithm = algorithm;

      initGUI();

   }


   @Override
   public void assignParameters() throws WrongInputException, LayerCannotBeOverwrittenException {

      ObjectAndDescription oad = (ObjectAndDescription) jComboBoxPolygonLayers.getSelectedItem();
      final IVectorLayer polygonLayer = (IVectorLayer) oad.getObject();
      final ParametersSet params = m_Algorithm.getParameters();
      try {
         params.getParameter(InPolygonSpatialJoinAlgorithm.POLYGONS).setParameterValue(polygonLayer);
         oad = (ObjectAndDescription) jComboBoxPointsLayers.getSelectedItem();
         final IVectorLayer pointsLayer = (IVectorLayer) oad.getObject();
         params.getParameter(InPolygonSpatialJoinAlgorithm.POINTS).setParameterValue(pointsLayer);
         final SpatialJoinTableModel model = (SpatialJoinTableModel) jTableGroupings.getModel();
         params.getParameter(InPolygonSpatialJoinAlgorithm.GROUPING_FUNCTIONS).setParameterValue(model.getAsString());
      }
      catch (final Exception e) {
         throw new WrongInputException();
      }

      Output out;
      IOutputChannel channel;
      final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
      try {
         out = ooSet.getOutput(InPolygonSpatialJoinAlgorithm.RESULT);
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
               jScrollPaneGroupings = new JScrollPane();
               jScrollPaneGroupings.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
               this.add(jScrollPaneGroupings, "1, 6, 2, 6");
               {
                  jTableGroupings = new JTable();
                  jScrollPaneGroupings.setViewportView(jTableGroupings);
               }
            }
            {
               jLabelPolygonsLayer = new JLabel();
               this.add(jLabelPolygonsLayer, "1, 1");
               jLabelPolygonsLayer.setText(Sextante.getText("Polygons"));
            }
            {
               jLabelPointsLayer = new JLabel();
               this.add(jLabelPointsLayer, "1, 3");
               jLabelPointsLayer.setText(Sextante.getText("Secondary_layer"));
            }
            {
               final ComboBoxModel jComboBoxPointsLayersModel = new DefaultComboBoxModel(getVectorLayers());
               jComboBoxPointsLayers = new JComboBox();
               this.add(jComboBoxPointsLayers, "2, 3");
               jComboBoxPointsLayers.setModel(jComboBoxPointsLayersModel);
               jComboBoxPointsLayers.addItemListener(new java.awt.event.ItemListener() {
                  public void itemStateChanged(final java.awt.event.ItemEvent e) {
                     setTableModel();
                  }
               });
               jComboBoxPointsLayers.setSelectedIndex(0);
               setTableModel();
            }
            {
               final ComboBoxModel jComboBoxPolygonLayersModel = new DefaultComboBoxModel(getPolygonLayers());
               jComboBoxPolygonLayers = new JComboBox();
               this.add(jComboBoxPolygonLayers, "2, 1");
               jComboBoxPolygonLayers.setModel(jComboBoxPolygonLayersModel);
            }
            {
               final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
               final Output out = ooSet.getOutput(InPolygonSpatialJoinAlgorithm.RESULT);
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


   private void setTableModel() {

      final ObjectAndDescription oad = (ObjectAndDescription) jComboBoxPointsLayers.getSelectedItem();
      final IVectorLayer layer = (IVectorLayer) oad.getObject();
      final SpatialJoinTableModel tableModel = new SpatialJoinTableModel(layer);
      jTableGroupings.setModel(tableModel);

   }


   private ObjectAndDescription[] getPolygonLayers() {

      final IVectorLayer[] layers = SextanteGUI.getInputFactory().getVectorLayers(IVectorLayer.SHAPE_TYPE_POLYGON);
      final ObjectAndDescription[] oad = new ObjectAndDescription[layers.length];
      for (int i = 0; i < layers.length; i++) {
         oad[i] = new ObjectAndDescription(layers[i].getName(), layers[i]);
      }
      return oad;

   }


   private ObjectAndDescription[] getVectorLayers() {

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
