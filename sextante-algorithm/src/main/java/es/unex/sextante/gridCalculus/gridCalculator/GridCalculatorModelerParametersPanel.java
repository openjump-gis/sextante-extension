

package es.unex.sextante.gridCalculus.gridCalculator;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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

import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.WrongOutputIDException;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.modeler.GeoAlgorithmModelerParametersPanel;
import es.unex.sextante.gui.modeler.OutputLayerSettingsPanel;
import es.unex.sextante.modeler.elements.ModelElementNumericalValue;
import es.unex.sextante.modeler.elements.ModelElementRasterLayer;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;


public class GridCalculatorModelerParametersPanel
         extends
            GeoAlgorithmModelerParametersPanel {

   private static final int         MAX_BANDS = 3;

   private JTextArea                jTextExpression;
   private JScrollPane              jScrollPane;
   private JTree                    jTree;
   private JScrollPane              jScrollPane1;
   private CalculatorKeysPanel      m_KeysPanel;
   private HashMap                  m_Constants;

   private OutputLayerSettingsPanel m_OutputLayerSettingsPanel;


   public GridCalculatorModelerParametersPanel() {

      super();

   }


   @Override
   protected void initGUI() {

      this.setPreferredSize(new java.awt.Dimension(570, 350));
      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 10.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
                        TableLayoutConstants.FILL, 10.0 },
               { 10.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 50.0, 5.0, 50.0, 5.0,
                        10.0 } });
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
            try {
               final Parameter param = m_Algorithm.getParameters().getParameter(GridCalculatorAlgorithm.FORMULA);
               final ObjectAndDescription oad = ((ObjectAndDescription) getParameterValue(param));
               if (oad != null) {
                  final String sFormula = (String) oad.getObject();
                  jTextExpression.setText(sFormula);
               }
            }
            catch (final Exception e) {
               Sextante.addErrorToLog(e);
            }
         }
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
            try {
               populateTree();
            }
            catch (final Exception e) {
               e.printStackTrace();
            }
         }
      }
      {
         final OutputObjectsSet oosetGlobal = this.m_GlobalAlgorithm.getOutputObjects();
         m_OutputLayerSettingsPanel = new OutputLayerSettingsPanel();
         String sDescription = Sextante.getText("Result");
         final String sKey = GridCalculatorAlgorithm.RESULT;

         if (!oosetGlobal.containsKey(sKey + this.m_sAlgorithmName)) {
            sDescription = "\"" + sDescription + "\" " + Sextante.getText("from") + " " + m_sAlgorithmDescription;
            m_OutputLayerSettingsPanel.setKeepAsFinalResult(false);
         }
         else {
            Output out;
            try {
               out = oosetGlobal.getOutput(sKey + this.m_sAlgorithmName);
               sDescription = out.getDescription();
               m_OutputLayerSettingsPanel.setKeepAsFinalResult(true);
            }
            catch (final WrongOutputIDException e) {
               //cannot reach this
            }

         }
         m_OutputLayerSettingsPanel.setName(sDescription);

         this.add(m_OutputLayerSettingsPanel, "2, 6, 4, 6");
         this.add(new JLabel(Sextante.getText("Result")), "1,6,2,6");
      }

   }


   private void populateTree() {

      int i, j;

      jTree.setModel(null);

      //layers
      final ObjectAndDescription[] layers = getElementsOfClass(ModelElementRasterLayer.class, false);
      final DefaultMutableTreeNode main = new DefaultMutableTreeNode(Sextante.getText("ELEMENTS"));
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(Sextante.getText("Layers"));
      DefaultMutableTreeNode child;
      for (i = 0; i < layers.length; i++) {
         final ObjectAndDescription oad = (ObjectAndDescription) m_DataObjects.get(layers[i].getObject());
         final ModelElementRasterLayer merl = (ModelElementRasterLayer) oad.getObject();
         int iBands = merl.getNumberOfBands();
         if (iBands == ModelElementRasterLayer.NUMBER_OF_BANDS_UNDEFINED) {
            iBands = MAX_BANDS;
         }
         for (j = 0; j < iBands; j++) {
            final String sName = layers[i].getDescription() + " Band " + Integer.toString(j + 1);
            final String sVariableName = layers[i].getObject() + " Band " + Integer.toString(j + 1);
            if ((iBands == MAX_BANDS) && (j != 0)) {
               child = new DefaultMutableTreeNode(new ObjectAndDescription(sName + "[" + Sextante.getText("unchecked") + "]",
                        sVariableName));
            }
            else {
               child = new DefaultMutableTreeNode(new ObjectAndDescription(sName, sVariableName));
            }
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
         final String s = " " + sOperators[i] + " ";
         child = new DefaultMutableTreeNode(new ObjectAndDescription(s, s));
         node.add(child);
      }
      main.add(node);

      final ObjectAndDescription[] values = getElementsOfClass(ModelElementNumericalValue.class, false);
      node = new DefaultMutableTreeNode(Sextante.getText("Numerical_values"));
      for (i = 0; i < values.length; i++) {
         final String sName = values[i].getDescription();
         final String sVariableName = values[i].getObject().toString();
         child = new DefaultMutableTreeNode(new ObjectAndDescription(sName, sVariableName));
         node.add(child);
      }
      main.add(node);

      //constants
      node = new DefaultMutableTreeNode(Sextante.getText("Constants"));
      m_Constants = new HashMap();
      m_Constants.put("e", " " + Double.toString(Math.E) + " ");
      m_Constants.put("Pi", " " + Double.toString(Math.PI) + " ");
      m_Constants.put("NODATA", " " + Double.toString(SextanteGUI.getOutputFactory().getDefaultNoDataValue()) + " ");
      final Set set = m_Constants.keySet();
      final Iterator iter = set.iterator();
      while (iter.hasNext()) {
         final String s = (String) iter.next();
         child = new DefaultMutableTreeNode(new ObjectAndDescription(s, s));
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
         final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
         String s = (String) ((ObjectAndDescription) node.getUserObject()).getObject();

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
   public boolean assignParameters(final HashMap map) {

      final ObjectAndDescription[] layers = getElementsOfClass(ModelElementRasterLayer.class, false);
      final ObjectAndDescription[] numerical = getElementsOfClass(ModelElementNumericalValue.class, false);
      final ArrayList layersList = new ArrayList();

      final String sFormula = jTextExpression.getText();
      final ArrayList array = FormulaParser.getBandsFromFormulaForModeler(sFormula, layers, numerical);

      if (array == null) {
         return false;
      }

      layersList.clear();

      for (int i = 0; i < array.size(); i++) {
         final String sKey = (String) array.get(i);
         if (!layersList.contains(sKey)) {
            layersList.add(sKey);
         }
      }

      final ObjectAndDescription[] values = getElementsOfClass(ModelElementNumericalValue.class, false);
      for (int i = 0; i < values.length; i++) {
         if (sFormula.contains(values[i].getObject().toString())) {
            map.put("DUMMY" + this.getInnerParameterKey(), values[i].getObject().toString());
         }
      }

      final String sArrayKey = getInnerParameterKey();
      map.put(GridCalculatorAlgorithm.LAYERS, sArrayKey);
      m_DataObjects.put(sArrayKey, new ObjectAndDescription("Multiple Input", layersList));

      final String sFormulaKey = getInnerParameterKey();
      map.put(GridCalculatorAlgorithm.FORMULA, sFormulaKey);
      m_DataObjects.put(sFormulaKey, new ObjectAndDescription("String", jTextExpression.getText()));

      final OutputObjectsSet oosetGlobal = this.m_GlobalAlgorithm.getOutputObjects();
      final OutputObjectsSet ooset = this.m_Algorithm.getOutputObjects();

      final String sName = GridCalculatorAlgorithm.RESULT + this.m_sAlgorithmName;
      if (m_OutputLayerSettingsPanel.getKeepAsFinalResult()) {
         try {
            final Output out = ooset.getOutput(GridCalculatorAlgorithm.RESULT);
            final Output outToAdd = out.getClass().newInstance();
            outToAdd.setName(sName);
            outToAdd.setDescription(m_OutputLayerSettingsPanel.getName());
            oosetGlobal.add(outToAdd);
         }
         catch (final Exception e) {
         }
      }
      else {
         oosetGlobal.remove(sName);
      }

      return true;

   }

}
