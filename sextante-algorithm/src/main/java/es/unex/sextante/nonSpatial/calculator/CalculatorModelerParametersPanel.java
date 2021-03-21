package es.unex.sextante.nonSpatial.calculator;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
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
import es.unex.sextante.gui.modeler.GeoAlgorithmModelerParametersPanel;
import es.unex.sextante.modeler.elements.ModelElementNumericalValue;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

public class CalculatorModelerParametersPanel
         extends
            GeoAlgorithmModelerParametersPanel {


   private JTextArea           jTextExpression;
   private JScrollPane         jScrollPane;
   private JTree               jTree;
   private JScrollPane         jScrollPane1;
   private CalculatorKeysPanel m_KeysPanel;
   private HashMap             m_Constants;


   public CalculatorModelerParametersPanel() {

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
               final Parameter param = m_Algorithm.getParameters().getParameter(CalculatorAlgorithm.FORMULA);
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

   }


   private void populateTree() {

      int i;
      final int j;

      jTree.setModel(null);

      //numerical values
      final ObjectAndDescription[] values = getElementsOfClass(ModelElementNumericalValue.class, false);
      final DefaultMutableTreeNode main = new DefaultMutableTreeNode(Sextante.getText("ELEMENTS"));
      DefaultMutableTreeNode node = new DefaultMutableTreeNode(Sextante.getText("Numerical_values"));
      DefaultMutableTreeNode child;
      for (i = 0; i < values.length; i++) {
         final String sName = values[i].getDescription();
         final String sVariableName = values[i].getObject().toString();
         child = new DefaultMutableTreeNode(new ObjectAndDescription(sName, sVariableName));
         node.add(child);
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

      final ObjectAndDescription[] values = getElementsOfClass(ModelElementNumericalValue.class, false);

      final JEP jep = new JEP();

      jep.addStandardConstants();
      jep.addStandardFunctions();

      for (int i = 0; i < values.length; i++) {
         if (jTextExpression.getText().contains(values[i].getObject().toString())) {
            jep.addVariable(values[i].getObject().toString(), 0);
            map.put("DUMMY" + this.getInnerParameterKey(), values[i].getObject().toString());
         }
      }

      jep.parseExpression(jTextExpression.getText());

      if (jep.hasError()) {
         return false;
      }


      final String sFormulaKey = getInnerParameterKey();
      map.put(CalculatorAlgorithm.FORMULA, sFormulaKey);
      m_DataObjects.put(sFormulaKey, new ObjectAndDescription("String", jTextExpression.getText()));

      final OutputObjectsSet oosetGlobal = this.m_GlobalAlgorithm.getOutputObjects();
      final OutputObjectsSet ooset = this.m_Algorithm.getOutputObjects();

      final String sName = CalculatorAlgorithm.RESULT + this.m_sAlgorithmName;
      try {
         final Output out = ooset.getOutput(CalculatorAlgorithm.RESULT);
         final Output outToAdd = out.getClass().newInstance();
         outToAdd.setName(sName);
         outToAdd.setDescription(sName); //TODO!!!!!!!!!!!!!!!
         oosetGlobal.add(outToAdd);
      }
      catch (final Exception e) {}

      return true;

   }

}
