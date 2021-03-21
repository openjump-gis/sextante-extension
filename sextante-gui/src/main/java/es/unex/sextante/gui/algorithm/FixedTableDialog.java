package es.unex.sextante.gui.algorithm;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.parameters.FixedTableModel;

/**
 * A dialog to fill a fixed table for a ParameterFixedTable
 * 
 * @author volaya
 * 
 */
public class FixedTableDialog
         extends
            javax.swing.JDialog {

   private final FixedTableModel m_TableModel;
   private final FixedTableModel m_TableModelOrg;
   private JPanel                jPanelMain;
   private JPanel                jPanelAddRemoveRows;
   private JSeparator            jSeparator;
   private JScrollPane           jScrollPane;
   private JButton               jButtonCancel;
   private JButton               jButtonOK;
   private JButton               jButtonRemoveRow;
   private JButton               jButtonAddRow;
   private JTable                jTable;
   private boolean               m_bAccepted;
   private JPanel                jPanelOpenSave;
   private JButton               jButtonOpen;
   private JButton               jButtonSave;


   /**
    * Creates a new dialog
    * 
    * @param window
    *                the parent window
    * @param tableModel
    *                the table model of the parameter
    */
   public FixedTableDialog(final Frame window,
                           final FixedTableModel tableModel) {


      super(window, "", true);

      this.setResizable(false);

      m_TableModelOrg = tableModel;
      m_TableModel = FixedTableModel.newInstance(tableModel);

      initGUI();
   }


   private void initGUI() {
      try {
         {
            this.setTitle(Sextante.getText("Fixed_table"));
            {
               jPanelMain = new JPanel();
               getContentPane().add(jPanelMain, BorderLayout.CENTER);
               final TableLayout jPanelMainLayout = new TableLayout(
                        new double[][] {
                                 { 3.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
                                          TableLayoutConstants.FILL, 3.0 },
                                 { 3.0, 75.0, TableLayoutConstants.FILL, 75.0, 5.0, 30.0, 3.0 } });
               jPanelMainLayout.setHGap(5);
               jPanelMainLayout.setVGap(5);
               jPanelMain.setLayout(jPanelMainLayout);
               jPanelMain.setPreferredSize(new java.awt.Dimension(564, 322));
               {
                  jScrollPane = new JScrollPane();
                  jPanelMain.add(jScrollPane, "1, 1, 3, 3");
                  jScrollPane.setPreferredSize(new java.awt.Dimension(432, 256));
                  {
                     jTable = new JTable();
                     jScrollPane.setViewportView(jTable);
                     jTable.setModel(m_TableModel);
                  }
               }
               {
                  jPanelOpenSave = new JPanel();
                  jPanelMain.add(jPanelOpenSave, "4, 3");
                  final TableLayout jPanelAddOpenSaveLayout = new TableLayout(new double[][] {
                           { TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL, TableLayoutConstants.MINIMUM },
                           { TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
                                    TableLayoutConstants.MINIMUM } });
                  jPanelAddOpenSaveLayout.setHGap(5);
                  jPanelAddOpenSaveLayout.setVGap(5);
                  jPanelOpenSave.setLayout(jPanelAddOpenSaveLayout);
                  jPanelOpenSave.setPreferredSize(new java.awt.Dimension(116, 56));
               }
               {
                  jPanelAddRemoveRows = new JPanel();
                  final TableLayout jPanelAddRemoveRowsLayout = new TableLayout(new double[][] {
                           { TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL, TableLayoutConstants.MINIMUM },
                           { TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
                                    TableLayoutConstants.MINIMUM } });
                  jPanelAddRemoveRowsLayout.setHGap(5);
                  jPanelAddRemoveRowsLayout.setVGap(5);
                  jPanelAddRemoveRows.setLayout(jPanelAddRemoveRowsLayout);
                  jPanelMain.add(jPanelAddRemoveRows, "4, 1");
                  jPanelAddRemoveRows.setPreferredSize(new java.awt.Dimension(116, 56));
               }
               {
                  jButtonRemoveRow = new JButton();
                  jPanelAddRemoveRows.add(jButtonRemoveRow, "1, 2");
                  jButtonRemoveRow.setText(Sextante.getText("Delete_row"));
                  jButtonRemoveRow.addActionListener(new ActionListener() {
                     public void actionPerformed(final ActionEvent evt) {
                        removeRow();
                     }
                  });
                  if (m_TableModel.isNumberOfRowsFixed()) {
                     jButtonRemoveRow.setEnabled(false);
                     jButtonRemoveRow.setSize(100, 22);//
                  }
               }
               {
                  jButtonAddRow = new JButton();
                  jPanelAddRemoveRows.add(jButtonAddRow, "1, 1");
                  jButtonAddRow.setText(Sextante.getText("Add_row"));
                  jButtonAddRow.addActionListener(new ActionListener() {
                     public void actionPerformed(final ActionEvent evt) {
                        addRow();
                     }
                  });
                  if (m_TableModel.isNumberOfRowsFixed()) {
                     jButtonAddRow.setEnabled(false);
                     jButtonAddRow.setSize(100, 22);
                  }
               }

               {
                  jButtonOpen = new JButton();
                  jPanelOpenSave.add(jButtonOpen, "1, 2");
                  jButtonOpen.setText(Sextante.getText("Open"));
                  jButtonOpen.addActionListener(new ActionListener() {
                     public void actionPerformed(final ActionEvent evt) {
                        open();
                     }
                  });
               }
               {
                  jButtonSave = new JButton();
                  jPanelOpenSave.add(jButtonSave, "1, 1");
                  jButtonSave.setText(Sextante.getText("Save"));
                  jButtonSave.addActionListener(new ActionListener() {
                     public void actionPerformed(final ActionEvent evt) {
                        save();
                     }
                  });
               }
               {
                  jSeparator = new JSeparator();
                  jPanelMain.add(jSeparator, "1, 4, 4, 4");
                  jSeparator.setPreferredSize(new java.awt.Dimension(565, 20));
               }
               {
                  jButtonOK = new JButton();
                  jPanelMain.add(jButtonOK, "3, 5");
                  jButtonOK.setText(Sextante.getText("OK"));
                  jButtonOK.addActionListener(new ActionListener() {
                     public void actionPerformed(final ActionEvent evt) {
                        btnOKActionPerformed();
                     }
                  });
               }
               {
                  jButtonCancel = new JButton();
                  jPanelMain.add(jButtonCancel, "4, 5");
                  jButtonCancel.setText(Sextante.getText("Cancel"));
                  jButtonCancel.addActionListener(new ActionListener() {
                     public void actionPerformed(final ActionEvent evt) {
                        btnCancelActionPerformed();
                     }
                  });
               }
            }
         }
         this.setSize(580, 350);
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }
   }


   private void save() {

      final JFileChooser fc = new JFileChooser();
      final GenericFileFilter filter = new GenericFileFilter("csv", "Comma-Separated Values");

      fc.setFileFilter(filter);
      final int returnVal = fc.showSaveDialog(this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
         final File file = fc.getSelectedFile();
         Writer output = null;
         try {
            output = new BufferedWriter(new FileWriter(file));
            final int iRows = m_TableModel.getRowCount();
            final int iCols = m_TableModel.getColumnCount();
            for (int iRow = 0; iRow < iRows; iRow++) {
               for (int iCol = 0; iCol < iCols; iCol++) {
                  final String s = m_TableModel.getValueAt(iRow, iCol).toString();
                  output.write(s);
                  if (iCol < (iCols - 1)) {
                     output.write("|");
                  }
               }
               output.write("\n");
            }
         }
         catch (final IOException e) {
            Sextante.addErrorToLog(e);
         }
         finally {
            if (output != null) {
               try {
                  output.close();
               }
               catch (final IOException e) {
                  Sextante.addErrorToLog(e);
               }
            }
         }
      }

   }


   private void open() {

      try {
         final JFileChooser fc = new JFileChooser();
         final GenericFileFilter filter = new GenericFileFilter("csv", "Comma-Separated Values");

         fc.setFileFilter(filter);
         final int returnVal = fc.showOpenDialog(this);

         if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = fc.getSelectedFile();
            BufferedReader input = null;

            final int iRows = m_TableModel.getRowCount();

            input = new BufferedReader(new FileReader(file));
            String sLine = null;
            int i = 1;
            while ((sLine = input.readLine()) != null) {
               if ((i > iRows) && !m_TableModel.isNumberOfRowsFixed()) {
                  m_TableModel.addRow();
               }
               processLine(sLine, m_TableModel, i - 1);
               i++;
            }
            input.close();
         }
      }
      catch (final Exception e) {
         JOptionPane.showMessageDialog(this, Sextante.getText("Could_not_open_selected_file"), Sextante.getText("Error"),
                  JOptionPane.ERROR_MESSAGE);
      }


   }


   private void processLine(final String line,
                            final FixedTableModel table,
                            final int iRow) {

      try {
         final String[] tokens = line.split("\\|");
         for (int i = 0; i < tokens.length; i++) {
            table.setValueAt(tokens[i], iRow, i);
         }
      }
      catch (final Exception e) {}

   }


   private void btnOKActionPerformed() {

      if (checkData()) {
         m_TableModelOrg.setAttributes(m_TableModel.getColumnNames(), m_TableModel.getData(), m_TableModel.isNumberOfRowsFixed());

         m_bAccepted = true;
         dispose();
         setVisible(false);
      }
      else {
         JOptionPane.showMessageDialog(this, Sextante.getText("Wrong_values_in_table"), Sextante.getText("Error"),
                  JOptionPane.ERROR_MESSAGE);
      }

   }


   private boolean checkData() {

      try {
         for (int i = 0; i < m_TableModel.getData().length; i++) {
            final ArrayList list = m_TableModel.getData()[i];
            for (int j = 0; j < list.size(); j++) {
               if (list.get(j).toString().isEmpty()) { //just check that every cell contains something
                  return false;
               }
            }
         }
         return true;
      }
      catch (final Exception e) {
         return false;
      }
   }


   private void btnCancelActionPerformed() {

      m_bAccepted = false;
      dispose();
      setVisible(false);


   }


   private void addRow() {

      m_TableModel.addRow();

   }


   private void removeRow() {

      int iRow;

      iRow = jTable.getSelectedRow();
      if (iRow != -1) {
         m_TableModel.removeRow(iRow);
      }

   }


   /**
    * Returns true if the user accepted the introduced data.
    * 
    * @return true if the user accepted the introduced data.
    */
   public boolean accepted() {

      return m_bAccepted;

   }
}
