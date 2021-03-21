package es.unex.sextante.gui.dataExplorer;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

import es.unex.sextante.core.Sextante;

public class DataExplorerTreeCellRenderer
         extends
            JLabel
         implements
            TreeCellRenderer {

   private final ImageIcon m_RasterIcon;
   private final ImageIcon m_VectorIcon;
   private final ImageIcon m_TableIcon;
   private final ImageIcon m_LayerIcon;


   public DataExplorerTreeCellRenderer() {

      m_RasterIcon = new ImageIcon(getClass().getClassLoader().getResource("images/raster_layer.gif"));
      m_VectorIcon = new ImageIcon(getClass().getClassLoader().getResource("images/vector_layer.gif"));
      m_TableIcon = new ImageIcon(getClass().getClassLoader().getResource("images/table_icon.png"));
      m_LayerIcon = new ImageIcon(getClass().getClassLoader().getResource("images/layers.gif"));

      setOpaque(false);
      setBackground(null);

   }


   public Component getTreeCellRendererComponent(final JTree tree,
                                                 final Object value,
                                                 final boolean selected,
                                                 final boolean expanded,
                                                 final boolean leaf,
                                                 final int row,
                                                 final boolean hasFocus) {

      String sName;

      setFont(tree.getFont());
      this.setIcon(getCustomIcon(value));

      sName = tree.convertValueToText(value, selected, expanded, leaf, row, hasFocus);


      setEnabled(tree.isEnabled());
      setText(sName);

      if (leaf) {
         if (selected) {
            setForeground(Color.blue);
         }
         else {
            setForeground(Color.black);
         }
      }

      return this;
   }


   private Icon getCustomIcon(final Object value) {

      final Object obj = ((DefaultMutableTreeNode) value).getUserObject();
      if (obj instanceof String) {
         if (obj.equals(Sextante.getText("Raster_layers"))) {
            return m_RasterIcon;
         }
         else if (obj.equals(Sextante.getText("Vector_layers"))) {
            return m_VectorIcon;
         }
         else if (obj.equals(Sextante.getText("Tables"))) {
            return m_TableIcon;
         }
         else if (obj.equals(Sextante.getText("Data"))) {
            return m_LayerIcon;
         }
         else {
            return null;
         }
      }
      else {
         return null;
      }

   }


}
