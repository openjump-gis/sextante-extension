package es.unex.sextante.gui.toolbox;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.gui.core.IAlgorithmProvider;
import es.unex.sextante.gui.core.NameAndIcon;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.core.ToolboxAction;


public class AlgorithmTreeCellRenderer
         extends
            JLabel
         implements
            TreeCellRenderer {


   public AlgorithmTreeCellRenderer() {

      setOpaque(false);
      setBackground(null);

   }


   protected Icon getCustomIcon(final Object value) {

      final ArrayList<IAlgorithmProvider> providers = SextanteGUI.getAlgorithmProviders();
      final HashMap<NameAndIcon, ArrayList<ToolboxAction>> allActions = SextanteGUI.getToolboxActions();

      final Object obj = ((DefaultMutableTreeNode) value).getUserObject();

      if (obj instanceof GeoAlgorithm) {
         return SextanteGUI.getAlgorithmIcon((GeoAlgorithm) obj);
      }
      else if (obj instanceof NameAndIcon) {
         return ((NameAndIcon) obj).getIcon();
      }
      else if (obj instanceof ToolboxAction) {
         final Set<NameAndIcon> set = allActions.keySet();
         final Iterator<NameAndIcon> iter = set.iterator();
         while (iter.hasNext()) {
            final NameAndIcon nai = iter.next();
            final ArrayList<ToolboxAction> actions = allActions.get(nai);
            for (int i = 0; i < actions.size(); i++) {
               if (obj.getClass().equals(actions.get(i).getClass())) {
                  return nai.getIcon();
               }
            }
         }
         return null;
      }
      else {
         for (int i = 0; i < providers.size(); i++) {
            if (value.toString().equals(providers.get(i).getName())) {
               return providers.get(i).getIcon();
            }
         }
         if (value.toString().equals("SEXTANTE")) {
            return SextanteGUI.SEXTANTE_ICON;
         }
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
            // Check if the children algorithms are active
            boolean activeAlg = false;
            final Object[] objs = SextanteGUI.getInputFactory().getDataObjects();
            for (int i = 0; !activeAlg && (i < tree.getModel().getChildCount(value)); i++) {
               final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) tree.getModel().getChild(value, i);
               final Object childValue = childNode.getUserObject();
               if (childValue instanceof GeoAlgorithm) {
                  final GeoAlgorithm alg = (GeoAlgorithm) childValue;
                  if (alg.meetsDataRequirements(objs)) {
                     activeAlg = true;
                  }
               }
               else if (childValue instanceof ToolboxAction) {
                  final ToolboxAction ita = (ToolboxAction) childValue;
                  activeAlg = ita.isActive();
               }
               else {
                  activeAlg = true; //is a top level node. Should be black
               }
            }

            // Check if selected
            setFont(AlgorithmsPanel.TREE_FONT);
            if (sel) {
               setForeground(Color.blue);
            }
            else {
               setFont(AlgorithmsPanel.TREE_FONT);
               if (!activeAlg) {
                  setForeground(Color.gray);
               }
               else {
                  setForeground(Color.black);
               }
            }

         }
         else {
            boolean activeAlg = false;
            final Object obj = ((DefaultMutableTreeNode) value).getUserObject();
            if (obj instanceof ToolboxAction) {
               final ToolboxAction ita = (ToolboxAction) obj;
               activeAlg = ita.isActive();
            }
            else {//geoalgorithm
               final GeoAlgorithm alg = (GeoAlgorithm) obj;
               final Object[] objs = SextanteGUI.getInputFactory().getDataObjects();
               activeAlg = alg.meetsDataRequirements(objs);
            }

            if (sel) {
               setForeground(Color.blue);
            }
            else {
               if (!activeAlg) {
                  setForeground(Color.gray);
               }
               else {
                  setForeground(Color.black);
               }
            }
         }
      }
      catch (final ClassCastException e) {
         setForeground(Color.black);
      }

      return this;

   }
}
