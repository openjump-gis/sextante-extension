

package es.unex.sextante.gui.modeler;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.gui.core.IAlgorithmProvider;
import es.unex.sextante.gui.core.SextanteGUI;


public class AlgorithmCellRenderer
         extends
            JLabel
         implements
            TreeCellRenderer {

   private final AlgorithmsPanel m_AlgorithmsPanel;


   public AlgorithmCellRenderer(final AlgorithmsPanel algorithmsPanel) {

      m_AlgorithmsPanel = algorithmsPanel;

   }


   protected Icon getCustomIcon(final Object value) {

      final ArrayList<IAlgorithmProvider> providers = SextanteGUI.getAlgorithmProviders();

      final Object obj = ((DefaultMutableTreeNode) value).getUserObject();

      if (obj instanceof GeoAlgorithm) {
         return SextanteGUI.getAlgorithmIcon((GeoAlgorithm) obj);
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
                                                 final boolean leaf,
                                                 final int row,
                                                 final boolean hasFocus) {

      String sName;

      setFont(tree.getFont());
      this.setIcon(getCustomIcon(value));

      sName = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

      setEnabled(tree.isEnabled());
      setText(sName);
      try {
         if (leaf) {
            if (sel) {
               setForeground(Color.blue);
            }
            else {
               final GeoAlgorithm alg = (GeoAlgorithm) ((DefaultMutableTreeNode) value).getUserObject();
               if (!m_AlgorithmsPanel.isAlgorithmEnabled(alg)) {
                  setForeground(Color.gray);
               }
               else {
                  setForeground(Color.black);
               }
            }
         }
         else {
            boolean activeAlgs = false;
            for (int i = 0; !activeAlgs && (i < tree.getModel().getChildCount(value)); i++) {
               final DefaultMutableTreeNode childValue = (DefaultMutableTreeNode) tree.getModel().getChild(value, i);
               final Object childNode = childValue.getUserObject();
               if (childNode instanceof GeoAlgorithm) {
                  final GeoAlgorithm alg = (GeoAlgorithm) childNode;
                  if (m_AlgorithmsPanel.isAlgorithmEnabled(alg)) {
                     activeAlgs = true;
                  }
               }
               else {
                  // If child is not a algorithm
                  activeAlgs = true;
               }
            }
            setFont(new java.awt.Font("Tahoma", 1, 11));
            if (sel) {
               setForeground(Color.blue);
            }
            else {
               if (activeAlgs) {
                  setForeground(Color.black);
               }
               else {
                  setForeground(Color.gray);
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
