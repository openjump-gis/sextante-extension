package es.unex.sextante.gui.modeler;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

public class CustomCellRenderer
         extends
            JLabel
         implements
            TreeCellRenderer {

   ImageIcon m_Icon;


   public CustomCellRenderer(final ImageIcon icon) {

      setOpaque(false);
      setBackground(null);
      m_Icon = icon;

   }


   protected Icon getCustomIcon(final Object value) {

      if (((DefaultMutableTreeNode) value).getUserObject() instanceof String) {
         return null;
      }
      else {
         return m_Icon;
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
