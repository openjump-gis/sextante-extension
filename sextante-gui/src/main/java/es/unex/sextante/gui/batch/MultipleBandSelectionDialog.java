package es.unex.sextante.gui.batch;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import es.unex.sextante.core.Sextante;

/**
 * A dialog to select a set of bands for a batch process
 * 
 * @author volaya
 * 
 */
public class MultipleBandSelectionDialog
         extends
            JDialog {

   private JPanel                           jPanelAddRemoveRows;
   private JPanel                           jPanelOKCancel;
   private JScrollPane                      jScrollPane;
   private JButton                          jButtonRemoveRow;
   private JButton                          jButtonAddRow;
   private JButton                          jButtonOK;
   private JButton                          jButtonCancel;
   private JTable                           jTable;
   private MultipleBandsSelectionTableModel m_TableModel;
   private String                           m_sSel;


   public MultipleBandSelectionDialog(final String sValue) {

      super((Frame) null, Sextante.getText("Selection"), true);

      initGUI();
      fillTable(sValue);

   }


   private void fillTable(final String sValue) {

      int i;
      ArrayList list;
      HashMap map = new HashMap();
      String sFile = null;
      final String[] sTokens = sValue.split(",");
      for (i = 0; i < sTokens.length; i++) {
         if (isPositiveInteger(sTokens[i])) {
            if (sFile == null) {
               map = null;
            }
            list = (ArrayList) map.get(sFile);
            if (list == null) {
               map = null;
               break;
            }
            list.add(new Integer(sTokens[i]));
         }
         else {
            sFile = sTokens[i];
            if (!map.containsKey(sFile)) {
               map.put(sFile, new ArrayList());
            }
         }
      }

      if (map != null) {
         final int iSize = map.size() - MultipleBandsSelectionTableModel.INIT_ROWS;
         for (i = 0; i < iSize; i++) {
            m_TableModel.addRow();
         }
         i = 0;
         final Set set = map.keySet();
         final Iterator iter = set.iterator();
         while (iter.hasNext()) {
            final String sKey = (String) iter.next();
            final StringBuffer sb = new StringBuffer();
            list = (ArrayList) map.get(sKey);
            m_TableModel.setValueAt(sKey, i, 0);
            for (int j = 0; j < list.size(); j++) {
               final Integer band = (Integer) list.get(j);
               sb.append(band.toString());
               if (j < list.size() - 1) {
                  sb.append(",");
               }
            }
            m_TableModel.setValueAt(sb.toString(), i, 1);
            i++;
         }

      }

   }


   private void initGUI() {

      final TableLayout thisLayout = new TableLayout(new double[][] { { 5.0, TableLayoutConstants.FILL, 5., 200., 5.0 },
               { 5., TableLayoutConstants.FILL, TableLayoutConstants.FILL, 5, 35, 5 } });
      this.setLayout(thisLayout);
      this.setPreferredSize(new java.awt.Dimension(580, 350));
      this.setSize(new java.awt.Dimension(580, 350));
      {
         jScrollPane = new JScrollPane();
         this.add(jScrollPane, "1,1,1,2");
         jScrollPane.setMinimumSize(new java.awt.Dimension(432, 256));

         jTable = new JTable();
         jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         jScrollPane.setViewportView(jTable);
         m_TableModel = new MultipleBandsSelectionTableModel();
         jTable.setModel(m_TableModel);
         jTable.getTableHeader().setReorderingAllowed(false);
         jTable.getColumnModel().getColumn(0).setCellEditor(new RasterFilePanelEditor(jTable));
         jTable.getColumnModel().getColumn(1).setCellEditor(new RasterBandPanelEditor(jTable));
         jTable.getColumnModel().getColumn(0).setPreferredWidth(200);
         jTable.getColumnModel().getColumn(1).setPreferredWidth(200);
      }
      {
         jPanelAddRemoveRows = new JPanel();
         this.add(jPanelAddRemoveRows, "3,1");
      }
      {
         jButtonRemoveRow = new JButton();
         jPanelAddRemoveRows.add(jButtonRemoveRow);
         jButtonRemoveRow.setText(Sextante.getText("Delete_row"));
         jButtonRemoveRow.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               btnRemoveRowActionPerformed();
            }
         });
      }
      {
         jButtonAddRow = new JButton();
         jPanelAddRemoveRows.add(jButtonAddRow);
         jButtonAddRow.setText(Sextante.getText("Add_row"));
         jButtonAddRow.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               btnAddRowActionPerformed();
            }
         });
      }
      {
         jPanelOKCancel = new JPanel();
         this.add(jPanelOKCancel, "3,4");
      }
      {
         jButtonOK = new JButton();
         jPanelOKCancel.add(jButtonOK);
         jButtonOK.setText(Sextante.getText("OK"));
         jButtonOK.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               btnOkActionPerformed();
            }
         });
      }
      {
         jButtonCancel = new JButton();
         jPanelOKCancel.add(jButtonCancel);
         jButtonCancel.setText(Sextante.getText("Cancel"));
         jButtonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               btnCancelActionPerformed();
            }
         });
      }


   }


   protected void btnOkActionPerformed() {

      setSelection();
      cancel();

   }


   protected void btnCancelActionPerformed() {

      m_sSel = null;
      cancel();

   }


   private void btnAddRowActionPerformed() {

      m_TableModel.addRow();

   }


   private void btnRemoveRowActionPerformed() {

      int iRow;

      iRow = jTable.getSelectedRow();
      if (iRow != -1) {
         m_TableModel.removeRow(iRow);
      }

   }


   public String getSelectionAsString() {

      return m_sSel;

   }


   private void setSelection() {

      final StringBuffer sSel = new StringBuffer();
      String sBands[];

      for (int i = 0; i < jTable.getRowCount(); i++) {
         sBands = ((String) jTable.getValueAt(i, 1)).split(",");
         for (final String element : sBands) {
            sSel.append(jTable.getValueAt(i, 0));
            sSel.append(",");
            sSel.append(element);
            sSel.append(",");
         }
      }

      sSel.deleteCharAt(sSel.length() - 1);

      m_sSel = sSel.toString();

   }


   public void cancel() {

      dispose();
      setVisible(false);

   }


   private boolean isPositiveInteger(final String s) {

      try {
         final int i = Integer.parseInt(s);
         return i > 0;
      }
      catch (final Exception e) {
         return false;
      }

   }

}
