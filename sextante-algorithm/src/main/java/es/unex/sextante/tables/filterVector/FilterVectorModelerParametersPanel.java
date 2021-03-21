package es.unex.sextante.tables.filterVector;

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

import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.WrongOutputIDException;
import es.unex.sextante.gui.modeler.GeoAlgorithmModelerParametersPanel;
import es.unex.sextante.gui.modeler.OutputLayerSettingsPanel;
import es.unex.sextante.modeler.elements.ModelElementNumericalValue;
import es.unex.sextante.modeler.elements.ModelElementTableField;
import es.unex.sextante.modeler.elements.ModelElementVectorLayer;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

public class FilterVectorModelerParametersPanel
         extends
            GeoAlgorithmModelerParametersPanel {

   private JTextArea                jTextExpression;
   private JScrollPane              jScrollPane;
   private JTree                    jTree;
   private JScrollPane              jScrollPane2;
   private CalculatorKeysPanel      m_KeysPanel;
   private HashMap                  m_Constants;

   private OutputLayerSettingsPanel m_OutputLayerSettingsPanel;
   private JPanel                   jPanel;
   private JComboBox                comboBox;


   public FilterVectorModelerParametersPanel() {

      super();

   }


   @Override
   protected void initGUI() {

      this.setPreferredSize(new java.awt.Dimension(570, 350));
      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 10.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
                        TableLayoutConstants.FILL, 10.0 },
               { 10.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 50.0, 5.0, 50.0, 10.0 } });
      thisLayout.setHGap(10);
      thisLayout.setVGap(10);
      this.setLayout(thisLayout);
      this.setSize(new java.awt.Dimension(350, 350));
      {
         jScrollPane = new JScrollPane();
         this.add(jScrollPane, "1, 4, 4, 4");
         jScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         {
            try {
               jTextExpression = new JTextArea();
               jScrollPane.setViewportView(jTextExpression);
               jTextExpression.setPreferredSize(new java.awt.Dimension(0, 0));
               jTextExpression.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
               final Parameter param = m_Algorithm.getParameters().getParameter(FilterVectorAlgorithm.FORMULA);
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
         jPanel = new JPanel();
         final TableLayout panelLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL },
                  { 20, TableLayoutConstants.FILL } });
         jPanel.setLayout(panelLayout);
         this.add(jPanel, "1, 1, 2, 3");
         jScrollPane2 = new JScrollPane();
         jPanel.add(jScrollPane2, "0,1");
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
         jScrollPane2.setViewportView(jTree);
         jScrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
         jTree.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

         comboBox = new JComboBox();
         final ObjectAndDescription[] layers = getElementsOfClass(ModelElementVectorLayer.class, false);
         final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(layers);
         comboBox.setModel(defaultModel);

         jPanel.add(comboBox, "0,0");
         populateTree();
      }
      {
         final OutputObjectsSet oosetGlobal = this.m_GlobalAlgorithm.getOutputObjects();
         m_OutputLayerSettingsPanel = new OutputLayerSettingsPanel();
         String sDescription = Sextante.getText("Result");
         final String sKey = FilterVectorAlgorithm.RESULT;

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

      int i;
      final int j;

      jTree.setModel(null);

      final DefaultMutableTreeNode main = new DefaultMutableTreeNode(Sextante.getText("ELEMENTS"));

      //functions
      final String sFunctions[] = { "sin", "cos", "tan", "asin", "acos", "atan", "atan2", "sinh", "cosh", "tanh", "asinh",
               "acosh", "atanh", "ln", "log", "exp", "abs", "rand", "mod", "sqrt", "if" };
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(Sextante.getText("Functions"));
      DefaultMutableTreeNode child;
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

      //numerical values
      ObjectAndDescription[] values = getElementsOfClass(ModelElementNumericalValue.class, false);
      node = new DefaultMutableTreeNode(Sextante.getText("Numerical_values"));
      for (i = 0; i < values.length; i++) {
         final String sName = values[i].getDescription();
         final String sVariableName = values[i].getObject().toString();
         child = new DefaultMutableTreeNode(new ObjectAndDescription(sName, sVariableName));
         node.add(child);
      }
      main.add(node);

      //fields
      values = getElementsOfClass(ModelElementTableField.class, false);
      node = new DefaultMutableTreeNode(Sextante.getText("Fields"));
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

      map.put(FilterVectorAlgorithm.LAYER, ((ObjectAndDescription) comboBox.getSelectedItem()).getObject());

      final String sFormulaKey = getInnerParameterKey();
      map.put(FilterVectorAlgorithm.FORMULA, sFormulaKey);
      final String sFormula = jTextExpression.getText();
      m_DataObjects.put(sFormulaKey, new ObjectAndDescription("String", sFormula));

      final JEP jep = new JEP();
      jep.addStandardConstants();
      jep.addStandardFunctions();

      ObjectAndDescription[] values = getElementsOfClass(ModelElementNumericalValue.class, false);
      for (int i = 0; i < values.length; i++) {
         if (sFormula.contains(values[i].getObject().toString())) {
            jep.addVariable(values[i].getObject().toString(), 0);
            map.put("DUMMY" + this.getInnerParameterKey(), values[i].getObject().toString());
         }
      }
      values = getElementsOfClass(ModelElementTableField.class, false);
      for (int i = 0; i < values.length; i++) {
         if (sFormula.contains(values[i].getObject().toString())) {
            jep.addVariable(values[i].getObject().toString(), 0);
            map.put("DUMMY" + this.getInnerParameterKey(), values[i].getObject().toString());
         }
      }

      jep.setAllowUndeclared(true);
      jep.parseExpression(sFormula);

      if (jep.hasError()) {
         return false;
      }

      final OutputObjectsSet oosetGlobal = this.m_GlobalAlgorithm.getOutputObjects();
      final OutputObjectsSet ooset = this.m_Algorithm.getOutputObjects();

      final String sName = FilterVectorAlgorithm.RESULT + this.m_sAlgorithmName;
      if (m_OutputLayerSettingsPanel.getKeepAsFinalResult()) {
         try {
            final Output out = ooset.getOutput(FilterVectorAlgorithm.RESULT);
            final Output outToAdd = out.getClass().newInstance();
            outToAdd.setName(sName);
            outToAdd.setDescription(m_OutputLayerSettingsPanel.getName());
            oosetGlobal.add(outToAdd);
         }
         catch (final Exception e) {}
      }
      else {
         oosetGlobal.remove(sName);
      }

      return true;

   }

}
