package es.unex.sextante.gui.dataExplorer;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.gui.additionalResults.TableTools;
import es.unex.sextante.gui.core.SextanteGUI;

public class TableDialog
         extends
            JDialog {

   private final IDataObject m_Object;


   public TableDialog(final IDataObject dataObject) {

      super(SextanteGUI.getMainFrame(), getDataObjectName(dataObject), true);

      m_Object = dataObject;

      initGUI();

   }


   private static String getDataObjectName(final Object dataObject) {

      if (dataObject instanceof ITable) {
         return ((ITable) dataObject).getName();
      }
      else if (dataObject instanceof IVectorLayer) {
         return ((IVectorLayer) dataObject).getName();
      }
      return null;

   }


   private void initGUI() {

      final JScrollPane table = TableTools.getScrollableTablePanelFromITable(m_Object);

      getContentPane().add(table, BorderLayout.CENTER);

      pack();
      this.setSize(623, 330);

      setLocationRelativeTo(null);

   }

}
