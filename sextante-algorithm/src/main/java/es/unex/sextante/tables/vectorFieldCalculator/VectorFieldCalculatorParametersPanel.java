package es.unex.sextante.tables.vectorFieldCalculator;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.nfunk.jep.JEP;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
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
import es.unex.sextante.tables.filterVector.FilterVectorAlgorithm;

public class VectorFieldCalculatorParametersPanel
         extends
            GeoAlgorithmParametersPanel {

   private JTextArea                   jTextExpression;
   private JScrollPane                 jScrollPane;
   private JTree                       jTree;
   private JScrollPane                 jScrollPane1;
   private CalculatorKeysPanel         m_KeysPanel;
   private JPanel                      jPanel;
   private HashMap                     m_Constants;
   private GeoAlgorithm                m_Algorithm;
   private OutputChannelSelectionPanel outputChannelSelectionPanel;
   private JComboBox                   comboBox;


   public VectorFieldCalculatorParametersPanel() {

      super();

   }


   @Override
   public void init(final GeoAlgorithm algorithm) {

      m_Algorithm = algorithm;

      initGUI();

   }


   private void initGUI() {

      this.setPreferredSize(new java.awt.Dimension(570, 350));
      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 10.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
                        TableLayoutConstants.FILL, 10.0 },
               { 10.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 50.0, 5.0, 20.0, 10.0 } });
      thisLayout.setHGap(10);
      thisLayout.setVGap(10);
      this.setLayout(thisLayout);
      this.setSize(new java.awt.Dimension(350, 350));
      {
         jScrollPane = new JScrollPane();
         this.add(jScrollPane, "1, 4, 4, 4");
         jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         {
            jTextExpression = new JTextArea();
            jScrollPane.setViewportView(jTextExpression);
            jTextExpression.setPreferredSize(new java.awt.Dimension(0, 0));
            jTextExpression.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
         }
      }
      {
         m_KeysPanel = new CalculatorKeysPanel(jTextExpression);
         this.add(m_KeysPanel, "3, 1, 4, 3");
      }
      {
         jPanel = new JPanel();
         final TableLayout panelLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL },
                  { 20, TableLayoutConstants.FILL } });
         jPanel.setLayout(panelLayout);
         this.add(jPanel, "1, 1, 2, 3");
         jScrollPane1 = new JScrollPane();
         jPanel.add(jScrollPane1, "0,1");
         jTree = new JTree();
         final MouseListener ml = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               int iRow = jTree.getRowForLocation(e.getX(), e.getY());
               TreePath path = jTree.getPathForLocation(e.getX(), e.getY());
               if ((iRow != -1) && (e.getClickCount() == 2)) {
                  insertTextFromTree(path);
               }
            }
         };
         jTree.addMouseListener(ml);
         jScrollPane1.setViewportView(jTree);
         jScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         jTree.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

         comboBox = new JComboBox();
         final IVectorLayer[] layers = SextanteGUI.getInputFactory().getVectorLayers(AdditionalInfoVectorLayer.SHAPE_TYPE_ANY);
         final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(layers);
         comboBox.setModel(defaultModel);
         comboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(final java.awt.event.ItemEvent e) {
               populateTree();
            }
         });
         jPanel.add(comboBox, "0,0");
         populateTree();
      }
      try {
         final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
         final Output out = ooSet.getOutput(VectorFieldCalculatorAlgorithm.RESULT);
         outputChannelSelectionPanel = new OutputChannelSelectionPanel(out, m_Algorithm.getParameters());
         this.add(outputChannelSelectionPanel, "3,6,4,6");
         this.add(new JLabel(Sextante.getText("Result")), "1,6,2,6");
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void populateTree() {

      int i;

      jTree.setModel(null);

      //fields
      final DefaultMutableTreeNode main = new DefaultMutableTreeNode(Sextante.getText("ELEMENTS"));
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(Sextante.getText("Fields"));
      DefaultMutableTreeNode child;
      final IVectorLayer layer = (IVectorLayer) comboBox.getSelectedItem();
      try {
         final String[] fieldNames = layer.getFieldNames();
         for (i = 0; i < fieldNames.length; i++) {
            child = new DefaultMutableTreeNode(fieldNames[i]);
            node.add(child);
         }
      }
      catch (final Exception e1) {}

      main.add(node);

      //functions
      final String sFunctions[] = { "sin", "cos", "tan", "asin", "acos", "atan", "atan2", "sinh", "cosh", "tanh", "asinh",
               "acosh", "atanh", "ln", "log", "exp", "abs", "rand", "mod", "sqrt", "if" };
      node = new DefaultMutableTreeNode(Sextante.getText("Functions"));
      for (i = 0; i < sFunctions.length; i++) {
         child = new DefaultMutableTreeNode(" " + sFunctions[i] + "() ");
         node.add(child);
      }
      main.add(node);

      //operators
      final String sOperators[] = { "+", "-", "*", "/", "%", "!", "^", "&&", "||", "<", ">", "<=", ">=", "==", "!=" };
      node = new DefaultMutableTreeNode(Sextante.getText("Operators"));
      for (i = 0; i < sOperators.length; i++) {
         child = new DefaultMutableTreeNode(" " + sOperators[i] + " ");
         node.add(child);
      }
      main.add(node);

      //constants
      node = new DefaultMutableTreeNode(Sextante.getText("Constants"));
      m_Constants = new HashMap();
      m_Constants.put("e", " " + Double.toString(Math.E) + " ");
      m_Constants.put("Pi", " " + Double.toString(Math.PI) + " ");

      final Set set = m_Constants.keySet();
      final Iterator iter = set.iterator();
      while (iter.hasNext()) {
         child = new DefaultMutableTreeNode(iter.next());
         node.add(child);
      }

      main.add(node);

      jTree.setModel(new DefaultTreeModel(main));
      final DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) jTree.getCellRenderer();
      renderer.setOpenIcon(null);
      renderer.setClosedIcon(null);
      renderer.setLeafIcon(null);

   }


   private void insertTextFromTree(final TreePath path) {

      final TreePath parent = path.getParentPath();

      if ((parent != null) && !parent.toString().equals("[" + Sextante.getText("ELEMENTS") + "]")) {
         final String sParentName = parent.toString();
         String s = path.getLastPathComponent().toString();;

         if (sParentName.equals("[" + Sextante.getText("ELEMENTS") + ", " + Sextante.getText("Constants]"))) {
            if (m_Constants.containsKey(s)) {
               s = (String) m_Constants.get(s);
            }
            else {
               s = "";
            }
         }

         jTextExpression.insert(s, jTextExpression.getCaretPosition());

         if (sParentName.equals("[" + Sextante.getText("ELEMENTS") + ", " + Sextante.getText("Functions]"))) {
            jTextExpression.setCaretPosition(jTextExpression.getCaretPosition() - 2);
         }
      }

   }


   @Override
   public void assignParameters() throws WrongInputException, LayerCannotBeOverwrittenException {

      int i;;
      IVectorLayer layer;
      String sFormula;
      String sFieldName;
      //ArrayList array = new ArrayList();
      final JEP jep = new JEP();
      jep.addStandardConstants();
      jep.addStandardFunctions();

      sFormula = jTextExpression.getText().toLowerCase();
      sFormula = sFormula.toLowerCase().replaceAll(" ", "");
      sFormula = replaceDots(sFormula);
      layer = (IVectorLayer) comboBox.getSelectedItem();
      try {
         final String[] fieldNames = layer.getFieldNames();
         for (i = 0; i < fieldNames.length; i++) {
            sFieldName = fieldNames[i].toLowerCase();
            sFieldName = sFieldName.replaceAll(" ", "");
            sFieldName = sFieldName.replaceAll("\\.", "");
            sFieldName = replaceDots(sFieldName);
            if (sFormula.lastIndexOf(sFieldName) != -1) {
               jep.addVariable(sFieldName, 0.0);
            }
         }
      }
      catch (final Exception e1) {}

      jep.parseExpression(sFormula);

      if (jep.hasError()) {
         throw new WrongInputException();
      }


      try {
         m_Algorithm.getParameters().getParameter(VectorFieldCalculatorAlgorithm.LAYER).setParameterValue(layer);
         m_Algorithm.getParameters().getParameter(VectorFieldCalculatorAlgorithm.FORMULA).setParameterValue(sFormula);
      }
      catch (final Exception e) {
         throw new WrongInputException();
      }

      final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
      Output out;
      IOutputChannel channel;

      try {
         out = ooSet.getOutput(VectorFieldCalculatorAlgorithm.RESULT);
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


   private String replaceDots(final String s) {

      char c, c2;
      StringBuffer sb = new StringBuffer(s);
      for (int i = 0; i < sb.length() - 1; i++) {
         c = sb.charAt(i);
         c2 = sb.charAt(i + 1);
         if ((c == '.') && !Character.isDigit(c2)) {
            sb = sb.deleteCharAt(i);
         }
      }

      return sb.toString();

   }


   @Override
   public void setOutputValue(final String outputName,
                              final String sValue) {


      outputChannelSelectionPanel.setText(sValue);

   }


   @Override
   public void setParameterValue(final String parameterName,
                                 final String value) {

      if (parameterName.equals(FilterVectorAlgorithm.FORMULA)) {
         jTextExpression.setText(value);
      }

   }

}
