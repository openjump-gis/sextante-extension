package es.unex.sextante.gui.dataExplorer;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IDataObject;

public class ShowTable
         implements
            Action {

   private final IDataObject m_DataObject;


   public ShowTable(final IDataObject obj) {

      m_DataObject = obj;

   }


   public boolean execute() {

      final TableDialog dialog = new TableDialog(m_DataObject);
      dialog.setVisible(true);

      return false;

   }


   public String getDescription() {

      return Sextante.getText("Show_table");
   }

}
