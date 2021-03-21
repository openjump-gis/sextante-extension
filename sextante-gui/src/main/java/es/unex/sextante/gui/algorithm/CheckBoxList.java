package es.unex.sextante.gui.algorithm;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * A list with checkboxes
 * 
 * @author volaya
 * 
 */
public class CheckBoxList
         extends
            JList {
   protected static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);


   /**
    * Creates a new checkbox list
    */
   public CheckBoxList() {
      setCellRenderer(new CellRenderer());

      addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(final MouseEvent e) {
            final int index = locationToIndex(e.getPoint());

            if (index != -1) {
               final JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
               checkbox.setSelected(!checkbox.isSelected());
               repaint();
            }
         }
      });

      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

   }

   protected class CellRenderer
            implements
               ListCellRenderer {
      public Component getListCellRendererComponent(final JList list,
                                                    final Object value,
                                                    final int index,
                                                    final boolean isSelected,
                                                    final boolean cellHasFocus) {
         final JCheckBox checkbox = (JCheckBox) value;
         checkbox.setBackground(getBackground());
         checkbox.setForeground(getForeground());
         checkbox.setEnabled(isEnabled());
         checkbox.setFont(getFont());
         checkbox.setFocusPainted(false);
         checkbox.setBorderPainted(true);
         checkbox.setBorder(noFocusBorder);
         return checkbox;
      }
   }
}
