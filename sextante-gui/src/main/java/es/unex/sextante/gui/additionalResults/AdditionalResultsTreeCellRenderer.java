package es.unex.sextante.gui.additionalResults;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

public class AdditionalResultsTreeCellRenderer
         extends
            JLabel
         implements
            TreeCellRenderer {

   public Component getTreeCellRendererComponent(final JTree tree,
                                                 final Object value,
                                                 final boolean sel,
                                                 final boolean expanded,
                                                 boolean leaf,
                                                 final int row,
                                                 final boolean hasFocus) {

      final String sName = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

      setFont(tree.getFont());
      setEnabled(tree.isEnabled());
      setText(sName);

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
      return this;

   }

}
