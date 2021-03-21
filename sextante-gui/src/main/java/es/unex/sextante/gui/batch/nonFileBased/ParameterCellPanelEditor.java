package es.unex.sextante.gui.batch.nonFileBased;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class ParameterCellPanelEditor
         extends
            AbstractCellEditor
         implements
            TableCellEditor {

   ParameterCellPanel panel;


   public ParameterCellPanelEditor(final Object obj,
                                   final JTable table) {

      panel = new ParameterCellPanel(obj, table);

   }


   public Object getCellEditorValue() {

      return panel.getValue();

   }


   public Component getTableCellEditorComponent(final JTable table,
                                                final Object value,
                                                final boolean isSelected,
                                                final int row,
                                                final int column) {

      panel.setValue(value);

      return panel;

   }


   public ParameterCellPanel getPanel() {

      return panel;

   }


   @Override
   public boolean isCellEditable(final EventObject anEvent) {

      if (anEvent instanceof MouseEvent) {
         return ((MouseEvent) anEvent).getClickCount() >= 2;
      }
      return true;

   }


   public boolean isFilenameCell() {

      return panel.isFilenameCell();

   }

}
