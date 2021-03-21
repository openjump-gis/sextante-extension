package es.unex.sextante.gui.modeler.parameters;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.text.Collator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.gui.modeler.SelectionAndChoices;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterSelection;

public class SelectionTreePanel
         extends
            JPanel {

   private JTree       jTree;
   private JScrollPane jScrollPane;


   public SelectionTreePanel() {

      super();

      initGUI();

   }


   private void initGUI() {

      final BorderLayout thisLayout = new BorderLayout();
      this.setLayout(thisLayout);
      jTree = new JTree();
      jTree.setCellRenderer(new MyCellRenderer());

      fillTree();
      jScrollPane = new JScrollPane(jTree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      jScrollPane.setMinimumSize(new Dimension(300, 400));

      this.add(jScrollPane, BorderLayout.CENTER);

   }


   private void fillTree() {

      int j;
      boolean bHasSelection;
      DefaultMutableTreeNode node;
      final DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(Sextante.getText("Algorithms"));
      DefaultMutableTreeNode child, subchild;
      String sGroup;
      final HashMap<String, HashMap<String, GeoAlgorithm>> algs = Sextante.getAlgorithms();
      final Set<String> setGroups = algs.keySet();
      final Iterator<String> iterGroups = setGroups.iterator();
      while (iterGroups.hasNext()) {
         final HashMap<String, DefaultMutableTreeNode> map = new HashMap<String, DefaultMutableTreeNode>();
         final String sKeyGroups = iterGroups.next();
         final DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(sKeyGroups);

         final HashMap<String, GeoAlgorithm> group = algs.get(sKeyGroups);
         Set<String> set = group.keySet();
         Iterator<String> iter = set.iterator();
         while (iter.hasNext()) {
            final String sKey = iter.next();
            final GeoAlgorithm alg = group.get(sKey);
            if (alg.isSuitableForModelling()) {
               bHasSelection = false;
               child = new DefaultMutableTreeNode(alg);
               final ParametersSet ps = alg.getParameters();
               final int iParCount = ps.getNumberOfParameters();
               for (j = 0; j < iParCount; j++) {
                  final Parameter parameter = ps.getParameter(j);
                  if (parameter instanceof ParameterSelection) {
                     bHasSelection = true;
                     AdditionalInfoSelection ai;
                     try {
                        ai = (AdditionalInfoSelection) parameter.getParameterAdditionalInfo();
                        subchild = new DefaultMutableTreeNode(new SelectionAndChoices(parameter.getParameterDescription(),
                                 ai.getValues()));
                        addNodeInSortedOrder(child, subchild);
                     }
                     catch (final NullParameterAdditionalInfoException e) {}
                  }
               }
               if (bHasSelection) {
                  sGroup = (alg.getGroup());
                  if (map.containsKey(sGroup)) {
                     node = map.get(sGroup);
                  }
                  else {
                     node = new DefaultMutableTreeNode(sGroup);
                     map.put(sGroup, node);
                  }
                  addNodeInSortedOrder(node, child);
               }

            }
         }
         set = map.keySet();
         iter = set.iterator();

         while (iter.hasNext()) {
            final String sKey = iter.next();
            node = map.get(sKey);
            addNodeInSortedOrder(groupNode, node);
         }

         if (map.size() != 0) {
            addNodeInSortedOrder(mainNode, groupNode);
         }
      }

      jTree.setModel(new DefaultTreeModel(mainNode));

   }


   private void addNodeInSortedOrder(final DefaultMutableTreeNode parent,
                                     final DefaultMutableTreeNode child) {

      final int n = parent.getChildCount();
      if (n == 0) {
         parent.add(child);
         return;
      }
      final Collator collator = Collator.getInstance();
      collator.setStrength(Collator.PRIMARY);
      DefaultMutableTreeNode node = null;
      for (int i = 0; i < n; i++) {
         node = (DefaultMutableTreeNode) parent.getChildAt(i);
         try {
            if (collator.compare(node.toString(), child.toString()) > 0) {
               parent.insert(child, i);
               return;
            }
         }
         catch (final Exception e) {}
      }
      parent.add(child);

   }


   public SelectionAndChoices getSelectedList() {

      final TreePath path = jTree.getSelectionPath();

      if (path == null) {
         return null;
      }

      try {
         final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
         final SelectionAndChoices sac = (SelectionAndChoices) node.getUserObject();
         return sac;
      }
      catch (final Exception e) {
         return null;
      }

   }

   static class MyCellRenderer
            extends
               JLabel
            implements
               TreeCellRenderer {

      ImageIcon m_ModuleIcon = new ImageIcon(getClass().getClassLoader().getResource("images/module2.png"));


      protected Icon getCustomIcon(final Object value) {

         if (((DefaultMutableTreeNode) value).getUserObject() instanceof GeoAlgorithm) {
            return m_ModuleIcon;
         }
         else {
            return null;
         }

      }


      public Component getTreeCellRendererComponent(final JTree tree,
                                                    final Object value,
                                                    final boolean sel,
                                                    final boolean expanded,
                                                    boolean leaf,
                                                    final int row,
                                                    final boolean hasFocus) {

         String sName;

         setFont(tree.getFont());
         this.setIcon(getCustomIcon(value));

         sName = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);


         setEnabled(tree.isEnabled());
         setText(sName);
         try {
            if (!leaf) {
               setFont(new java.awt.Font("Tahoma", 1, 11));
               setForeground(Color.black);
            }
            else {
               if (sel) {
                  setForeground(Color.blue);
               }
               else {
                  setForeground(Color.black);
               }
            }
         }
         catch (final ClassCastException e) {
            setForeground(Color.black);
         }

         return this;

      }

   }


}
