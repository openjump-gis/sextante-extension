package es.unex.sextante.gui.batch.nonFileBased;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.Parameter;


/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI Builder, which is free for non-commercial use. If
 * Jigloo is being used commercially (ie, by a corporation, company or business for any purpose whatever) then you should purchase
 * a license for each developer using Jigloo. Please visit www.cloudgarden.com for details. Use of Jigloo implies acceptance of
 * these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
/**
 * A panel to introduce parameters for a batch process. It features a table where each row represents a single process. The number
 * of columns depends on the algorithm to execute and the number of parameters it needs
 * 
 * @author volaya
 * 
 */
public class ParametersPanel
         extends
            JPanel {

   private final GeoAlgorithm   m_Algorithm;
   private JPanel               jPanelAddRemoveRows;
   private JScrollPane          jScrollPane;
   private JButton              jButtonRemoveRow;
   private JButton              jButtonAddRow;
   private JTable               jTable;
   private ParametersTableModel m_TableModel;
   private JPopupMenu           popupMenu;
   private JMenuItem            menuItemCopy;
   private String[][]           m_sClipboard;
   private JMenuItem            menuItemPaste;


   /**
    * Constructor
    * 
    * @param alg
    *                the algorithm to execute
    */
   public ParametersPanel(final GeoAlgorithm alg) {

      super();

      m_Algorithm = alg;

      defineTableModel();
      initGUI();

   }


   private void defineTableModel() {

      m_TableModel = new ParametersTableModel(m_Algorithm.getParameters(), m_Algorithm.getOutputObjects());

   }


   private void initGUI() {

      int i;
      final ParametersSet ps = m_Algorithm.getParameters();
      final OutputObjectsSet ooset = m_Algorithm.getOutputObjects();

      final TableLayout thisLayout = new TableLayout(new double[][] { { 5.0, TableLayoutConstants.FILL, 5.0, 120.0, 5.0 },
               { 5.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 5.0, 25.0, 5.0 } });
      this.setLayout(thisLayout);
      this.setPreferredSize(new java.awt.Dimension(580, 350));
      this.setSize(new java.awt.Dimension(580, 350));
      {
         jScrollPane = new JScrollPane();
         this.add(jScrollPane, "1,1,1,2");
         jScrollPane.setMinimumSize(new java.awt.Dimension(432, 256));

         jTable = new JTable();
         jScrollPane.setViewportView(jTable);
         initTable();

         int iDataObjectOutputs = 0;
         for (i = 0; i < ooset.getOutputObjectsCount(); i++) {
            final Output out = ooset.getOutput(i);
            if ((out instanceof OutputRasterLayer) || (out instanceof Output3DRasterLayer) || (out instanceof OutputVectorLayer)
                || (out instanceof OutputTable)) {
               final TableColumn col = jTable.getColumnModel().getColumn(iDataObjectOutputs + ps.getNumberOfParameters());
               col.setCellEditor(new ParameterCellPanelEditor(out, jTable));
               col.setCellRenderer(new ParameterCellPanelRenderer(out));
               jTable.getColumnModel().getColumn(iDataObjectOutputs + ps.getNumberOfParameters()).setPreferredWidth(200);
               iDataObjectOutputs++;
            }
         }

         final MouseListener ml = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
               if (e.getButton() == MouseEvent.BUTTON3) {
                  showPopupMenu(e);
               }
            }
         };
         jTable.addMouseListener(ml);

         {
            popupMenu = new JPopupMenu("Menu");

            menuItemCopy = new JMenuItem(Sextante.getText("Copy"));
            menuItemCopy.addActionListener(new ActionListener() {
               public void actionPerformed(final ActionEvent evt) {
                  copyCells();
               }
            });
            popupMenu.add(menuItemCopy);

            menuItemPaste = new JMenuItem(Sextante.getText("Paste"));
            menuItemPaste.addActionListener(new ActionListener() {
               public void actionPerformed(final ActionEvent evt) {
                  pasteCells();
               }
            });
            popupMenu.add(menuItemPaste);
         }

      }
      {
         jPanelAddRemoveRows = new JPanel();
         final TableLayout jPanelAddRemoveRowsLayout = new TableLayout(new double[][] { { 5.0, TableLayoutConstants.FILL, 5.0 },
                  { 5.0, 25.0, 30.0, TableLayoutConstants.FILL } });
         jPanelAddRemoveRowsLayout.setHGap(5);
         jPanelAddRemoveRowsLayout.setVGap(5);
         jPanelAddRemoveRows.setLayout(jPanelAddRemoveRowsLayout);
         this.add(jPanelAddRemoveRows, "3,1,4,1");
      }
      {
         jButtonRemoveRow = new JButton();
         jPanelAddRemoveRows.add(jButtonRemoveRow, "1, 2");
         jButtonRemoveRow.setText(Sextante.getText("Delete_row"));
         jButtonRemoveRow.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               btnRemoveRowActionPerformed(evt);
            }
         });
      }
      {
         jButtonAddRow = new JButton();
         jPanelAddRemoveRows.add(jButtonAddRow, "1,  1");
         jButtonAddRow.setText(Sextante.getText("Add_row"));
         jButtonAddRow.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               btnAddRowActionPerformed(evt);
            }
         });
      }


   }


   protected void showPopupMenu(final MouseEvent e) {

      popupMenu.show(e.getComponent(), e.getX(), e.getY());

   }


   protected void copyCells() {

      final int[] cols = jTable.getSelectedColumns();
      final int[] rows = jTable.getSelectedRows();
      m_sClipboard = new String[rows.length][cols.length];

      for (int j = 0; j < rows.length; j++) {
         for (int i = 0; i < cols.length; i++) {
            m_sClipboard[j][i] = (String) jTable.getValueAt(rows[j], cols[i]);
         }
      }

   }


   protected void pasteCells() {

      if (m_sClipboard == null) {
         return;
      }

      int iCol, iRow;

      final int[] cols = jTable.getSelectedColumns();
      final int[] rows = jTable.getSelectedRows();

      for (final int element : rows) {
         for (final int element2 : cols) {
            iRow = (element - rows[0]) % m_sClipboard.length;
            iCol = (element2 - cols[0]) % m_sClipboard[0].length;
            jTable.setValueAt(m_sClipboard[iRow][iCol], element, element2);
         }
      }


   }


   private void btnAddRowActionPerformed(final ActionEvent evt) {

      m_TableModel.addRow();

   }


   private void btnRemoveRowActionPerformed(final ActionEvent evt) {

      int iRow;

      iRow = jTable.getSelectedRow();
      if (iRow != -1) {
         m_TableModel.removeRow(iRow);
      }

   }


   /**
    * Assign the values introduced by the user. Values for each execution of the algorithm are stored in two maps, one for the
    * inputs and another one for the outputs. Maps are stored in the passed ArrayLists
    * 
    * @param param
    *                an arraylist to store maps containing input parameters values
    * @param output
    *                an arraylist to store maps containing out parameters values
    * @return returns true if all the values in the current table are correct, false otherwise
    */
   public boolean assignParameters(final ArrayList param,
                                   final ArrayList output) {

      int i, j;


      final ParametersSet ps = m_Algorithm.getParameters();
      final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
      for (i = 0; i < m_TableModel.getRowCount(); i++) {
         final HashMap paramMap = new HashMap();
         for (j = 0; j < ps.getNumberOfParameters(); j++) {
            final ParameterCellPanelRenderer rend = (ParameterCellPanelRenderer) jTable.getCellRenderer(i, j);
            if (!rend.isValueOK()) {
               return false;
            }
            paramMap.put(ps.getParameter(j).getParameterName(), m_TableModel.getValueAt(i, j));
         }
         final HashMap outputMap = new HashMap();
         for (j = 0; j < ooSet.getOutputDataObjectsCount(); j++) {
            final Output out = ooSet.getOutput(j);
            if ((out instanceof OutputRasterLayer) || (out instanceof OutputVectorLayer) || (out instanceof OutputTable)) {
               final String sOut = m_TableModel.getValueAt(i, j + ps.getNumberOfParameters()).toString();
               if (sOut.equals(Sextante.getText("[Save_to_temporary_file]"))) {
                  outputMap.put(out.getName(), null);
               }
               else {
                  outputMap.put(out.getName(), sOut);
               }
            }
         }
         param.add(paramMap);
         output.add(outputMap);
      }

      return true;

   }


   public ParametersTableModel getTableModel() {

      return m_TableModel;

   }


   /**
    * Sets a new parameters table
    * 
    * @param table
    *                the new table to set
    */
   public void setTableModel(final ParametersTableModel table) {

      //Warning: we simply change the assignment, but do not check that
      //the table schema is consistent

      m_TableModel = table;
      initTable();
      this.updateUI();

   }


   private void initTable() {

      final ParametersSet ps = m_Algorithm.getParameters();
      jTable.setModel(m_TableModel);
      jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      jTable.getTableHeader().setReorderingAllowed(false);
      jTable.setColumnSelectionAllowed(true);
      jTable.setRowSelectionAllowed(true);
      for (int i = 0; i < ps.getNumberOfParameters(); i++) {
         final Parameter param = ps.getParameter(i);
         final TableColumn col = jTable.getColumnModel().getColumn(i);
         col.setCellEditor(new ParameterCellPanelEditor(param, jTable));
         col.setCellRenderer(new ParameterCellPanelRenderer(param));
         jTable.getColumnModel().getColumn(i).setPreferredWidth(200);
      }

   }
}
