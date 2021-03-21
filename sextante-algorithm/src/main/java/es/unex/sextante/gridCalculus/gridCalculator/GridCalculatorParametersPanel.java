

package es.unex.sextante.gridCalculus.gridCalculator;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.WrongInputException;
import es.unex.sextante.gui.algorithm.GeoAlgorithmParametersPanel;
import es.unex.sextante.gui.algorithm.OutputChannelSelectionPanel;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.RasterLayerAndBand;


public class GridCalculatorParametersPanel
         extends
            GeoAlgorithmParametersPanel {

   private JTextArea                   jTextExpression;
   private JScrollPane                 jScrollPane;
   private JTree                       jTree;
   private JScrollPane                 jScrollPane1;
   private CalculatorKeysPanel         m_KeysPanel;
   private TreeMap                     m_Constants;
   private GeoAlgorithm                m_Algorithm;
   private OutputChannelSelectionPanel outputChannelSelectionPanel;


   public GridCalculatorParametersPanel() {

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
               { 10.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 50.0, 5.0, 20.0, 5, 20,
                        10.0 } });
      thisLayout.setHGap(10);
      thisLayout.setVGap(10);
      this.setLayout(thisLayout);
      this.setSize(new java.awt.Dimension(350, 350));
      {
         jTextExpression = new JTextArea();
         jTextExpression.setLineWrap(true);
         jScrollPane = new JScrollPane(jTextExpression);
         this.add(jScrollPane, "1, 4, 4, 4");
         jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         jScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
         jTextExpression.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      }
      {
         m_KeysPanel = new CalculatorKeysPanel(jTextExpression);
         this.add(m_KeysPanel, "3, 1, 4, 3");
      }
      {
         jScrollPane1 = new JScrollPane();
         this.add(jScrollPane1, "1, 1, 2, 3");
         {
            jTree = new JTree();
            jScrollPane1.setViewportView(jTree);
            jScrollPane1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            jTree.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
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
            populateTree();
         }
      }
      try {
         final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
         final Output out = ooSet.getOutput(GridCalculatorAlgorithm.RESULT);
         outputChannelSelectionPanel = new OutputChannelSelectionPanel(out, m_Algorithm.getParameters());
         this.add(outputChannelSelectionPanel, "3,6,4,6");
         this.add(new JLabel(Sextante.getText("Result")), "1,6,2,6");
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void populateTree() {

      int i, j;
      double dCellsize;
      String sName;

      jTree.setModel(null);

      //layers
      final IRasterLayer[] layers = SextanteGUI.getInputFactory().getRasterLayers();
      final DefaultMutableTreeNode main = new DefaultMutableTreeNode(Sextante.getText("ELEMENTS"));
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(Sextante.getText("Layers"));
      DefaultMutableTreeNode child;
      for (i = 0; i < layers.length; i++) {
         for (j = 0; j < layers[i].getBandsCount(); j++) {
            sName = layers[i].getName() + " Band " + Integer.toString(j + 1);
            child = new DefaultMutableTreeNode(sName);
            node.add(child);
         }
      }
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
      m_Constants = new TreeMap();
      m_Constants.put("e", " " + Double.toString(Math.E) + " ");
      m_Constants.put("Pi", " " + Double.toString(Math.PI) + " ");
      m_Constants.put("NODATA", " " + Double.toString(SextanteGUI.getOutputFactory().getDefaultNoDataValue()) + " ");
      for (i = 0; i < layers.length; i++) {
         sName = Sextante.getText("Cell_size_[") + layers[i].getName() + "]";
         dCellsize = layers[i].getLayerCellSize();
         m_Constants.put(sName, " " + Double.toString(dCellsize) + " ");
      }

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
         String s = path.getLastPathComponent().toString();

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
   public void assignParameters() throws WrongInputException {

      final IRasterLayer[] layers = SextanteGUI.getInputFactory().getRasterLayers();
      final ArrayList layersList = new ArrayList();
      for (final IRasterLayer element : layers) {
         layersList.add(element);
      }
      final ArrayList array = FormulaParser.getBandsFromFormula(jTextExpression.getText(), layersList);

      if (array == null) {
         throw new WrongInputException();
      }

      layersList.clear();

      for (int i = 0; i < array.size(); i++) {
         final RasterLayerAndBand rlab = (RasterLayerAndBand) array.get(i);
         final IRasterLayer layer = rlab.getRasterLayer();
         if (!layersList.contains(layer)) {
            layersList.add(layer);
         }
      }

      try {
         m_Algorithm.getParameters().getParameter(GridCalculatorAlgorithm.LAYERS).setParameterValue(layersList);
         m_Algorithm.getParameters().getParameter(GridCalculatorAlgorithm.FORMULA).setParameterValue(jTextExpression.getText());
         final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
         final Output out = ooSet.getOutput(GridCalculatorAlgorithm.RESULT);
         out.setOutputChannel(outputChannelSelectionPanel.getOutputChannel());
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
         throw new WrongInputException();
      }

   }


   @Override
   public void setOutputValue(final String outputName,
                              final String value) {


      outputChannelSelectionPanel.setText(value);

   }


   @Override
   public void setParameterValue(final String parameterName,
                                 final String value) {

      if (parameterName.equals(GridCalculatorAlgorithm.FORMULA)) {
         jTextExpression.setText(value);
      }

   }

}
