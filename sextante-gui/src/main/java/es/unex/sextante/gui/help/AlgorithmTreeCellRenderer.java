package es.unex.sextante.gui.help;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.gui.core.SextanteGUI;

public class AlgorithmTreeCellRenderer
         extends
            JLabel
         implements
            TreeCellRenderer {

   //   ImageIcon m_ModuleIcon      = new ImageIcon(getClass().getClassLoader().getResource("images/module2.png"));
   //   ImageIcon m_ModuleIconWrong = new ImageIcon(getClass().getClassLoader().getResource("images/module_wrong.png"));


   protected Icon getCustomIcon(final Object value) {

      if (((DefaultMutableTreeNode) value).getUserObject() instanceof GeoAlgorithm) {
         final GeoAlgorithm alg = (GeoAlgorithm) ((DefaultMutableTreeNode) value).getUserObject();
         return SextanteGUI.getAlgorithmIcon(alg);
         //         final String sFile = HelpIO.getHelpFilename(alg, true);
         //         final File file = new File(sFile);
         //
         //         if (file.exists()) {
         //            return m_ModuleIcon;
         //         }
         //         else {
         //            return m_ModuleIconWrong;
         //         }
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

      final String sName = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);

      setFont(tree.getFont());
      setEnabled(tree.isEnabled());
      setText(sName);
      setIcon(getCustomIcon(value));

      if (!leaf) {
         setFont(new java.awt.Font("Tahoma", 1, 11));
      }

      if (sel) {
         setForeground(Color.blue);
      }
      else {
         setForeground(Color.black);
      }

      return this;

   }

}
