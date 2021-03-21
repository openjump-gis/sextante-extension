package es.unex.sextante.gui.batch;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class RasterFilePanelEditor
         extends
            AbstractCellEditor
         implements
            TableCellEditor {

   RasterFilePanel panel;


   public RasterFilePanelEditor(final JTable table) {

      panel = new RasterFilePanel(table);

   }


   public Object getCellEditorValue() {

      return panel.getValue();

   }


   public Component getTableCellEditorComponent(final JTable table,
                                                final Object value,
                                                final boolean isSelected,
                                                final int row,
                                                final int column) {

      panel.setValue((String) value);

      return panel;

   }


   @Override
   public boolean isCellEditable(final EventObject anEvent) {

      if (anEvent instanceof MouseEvent) {
         return ((MouseEvent) anEvent).getClickCount() >= 2;
      }
      return true;

   }


}
