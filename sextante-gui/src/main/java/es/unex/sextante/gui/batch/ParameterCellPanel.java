package es.unex.sextante.gui.batch;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.gui.algorithm.FixedTableDialog;
import es.unex.sextante.gui.algorithm.GenericFileFilter;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.FixedTableModel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.Parameter3DRasterLayer;
import es.unex.sextante.parameters.ParameterBand;
import es.unex.sextante.parameters.ParameterBoolean;
import es.unex.sextante.parameters.ParameterDataObject;
import es.unex.sextante.parameters.ParameterFixedTable;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterNumericalValue;
import es.unex.sextante.parameters.ParameterPoint;
import es.unex.sextante.parameters.ParameterRasterLayer;
import es.unex.sextante.parameters.ParameterSelection;
import es.unex.sextante.parameters.ParameterString;
import es.unex.sextante.parameters.ParameterTable;
import es.unex.sextante.parameters.ParameterTableField;
import es.unex.sextante.parameters.ParameterVectorLayer;

public class ParameterCellPanel
         extends
            JPanel {

   private JTextField   textField;
   private JButton      button;
   private final Object m_Object;
   private final JTable m_Table;


   public ParameterCellPanel(final Object obj,
                             final JTable table) {

      super();

      m_Object = obj;
      m_Table = table;

      initGUI();

   }


   private void initGUI() {

      button = new JButton("...");

      textField = new JTextField("");
      textField.setMaximumSize(new java.awt.Dimension(340, 18));
      button.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            btnActionPerformed(evt);
         }
      });

      final TableLayout thisLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL, 25.0 },
               { TableLayoutConstants.FILL } });
      this.setLayout(thisLayout);

      if ((m_Object instanceof ParameterString) || (m_Object instanceof ParameterNumericalValue)
          || (m_Object instanceof ParameterTableField)) {
         this.add(textField, "0, 0, 1, 0");
      }
      else {
         this.add(textField, "0,  0");
         this.add(button, "1,  0");
      }

   }


   private void btnActionPerformed(final ActionEvent e) {

      if (m_Object instanceof ParameterBand) {
         assignBand();
      }
      else if (m_Object instanceof ParameterPoint) {
         assignPoint();
      }
      else if (m_Object instanceof ParameterBoolean) {
         assignBoolean();
      }
      else if (m_Object instanceof ParameterDataObject) {
         assignDataObject();
      }
      else if (m_Object instanceof ParameterFixedTable) {
         assignFixedTable();
      }
      else if (m_Object instanceof ParameterSelection) {
         assignSelection();
      }
      else if (m_Object instanceof ParameterMultipleInput) {
         assignMultipleInput();
      }
      else if (m_Object instanceof Output) {
         assignOutput();
      }

      m_Table.setValueAt(textField.getText(), m_Table.getSelectedRow(), m_Table.getSelectedColumn());

   }


   private void assignOutput() {

      int returnVal;
      String sExt[];
      String sDesc;
      final JFileChooser fc = new JFileChooser();

      if (m_Object instanceof OutputRasterLayer) {
         sExt = SextanteGUI.getOutputFactory().getRasterLayerOutputExtensions();
         sDesc = Sextante.getText("Raster_layers");
      }
      else if (m_Object instanceof Output3DRasterLayer) {
         sExt = SextanteGUI.getOutputFactory().get3DRasterLayerOutputExtensions();
         sDesc = Sextante.getText(Sextante.getText("3D_Raster_layers"));
      }
      else if (m_Object instanceof OutputVectorLayer) {
         sExt = SextanteGUI.getOutputFactory().getVectorLayerOutputExtensions();
         sDesc = Sextante.getText("Vector_layer");
      }
      else if (m_Object instanceof OutputTable) {
         sExt = SextanteGUI.getOutputFactory().getTableOutputExtensions();
         sDesc = Sextante.getText("Tables");
      }
      else {
         return;
      }

      fc.setFileFilter(new GenericFileFilter(sExt, sDesc));

      final String sColumnNames[] = new String[m_Table.getColumnCount() - 1];
      for (int i = 0, j = 0; i < sColumnNames.length; i++) {
         if (i != m_Table.getSelectedColumn()) {
            sColumnNames[j] = m_Table.getColumnName(i);
            j++;
         }
      }
      fc.setAccessory(new AutoCompletePanel(sColumnNames));
      returnVal = fc.showSaveDialog(this.getParent().getParent());

      if (returnVal == JFileChooser.APPROVE_OPTION) {
         String sFile = fc.getSelectedFile().getAbsolutePath();
         int i;
         for (i = 0; i < sExt.length; i++) {
            if (sFile.endsWith(sExt[i])) {
               break;
            }
         }
         if (i == sExt.length) {
            sFile = sFile + "." + sExt[0];
         }

         String sFile2;
         final int iRow = m_Table.getSelectedRow();
         final int iCol = m_Table.getSelectedColumn();

         final int iAutoFill = ((AutoCompletePanel) fc.getAccessory()).getAutoFill();
         if (iAutoFill == AutoCompletePanel.AUTOFILL_NUMBER) {
            int iCount = 1;
            for (i = iRow; i < m_Table.getRowCount(); i++) {
               sFile2 = sFile.substring(0, sFile.lastIndexOf(".")) + Integer.toString(iCount)
                        + sFile.substring(sFile.lastIndexOf("."), sFile.length());
               m_Table.setValueAt(sFile2, i, iCol);
               iCount++;
               if (i == iRow) {
                  textField.setText(sFile2);
               }
            }
         }
         else if (iAutoFill == AutoCompletePanel.AUTOFILL_FIELD) {
            final int iField = ((AutoCompletePanel) fc.getAccessory()).getField();
            final ParameterCellPanelEditor ce = (ParameterCellPanelEditor) m_Table.getCellEditor(0, iField);
            for (i = iRow; i < m_Table.getRowCount(); i++) {
               String sCellValue = m_Table.getValueAt(i, iField).toString();
               if (ce.isFilenameCell()) {
                  if (!sCellValue.trim().equals("")) {
                     final File f = new File(sCellValue);
                     sCellValue = f.getName().substring(0, f.getName().lastIndexOf("."));
                  }
               }
               sFile2 = sFile.substring(0, sFile.lastIndexOf(".")) + sCellValue
                        + sFile.substring(sFile.lastIndexOf("."), sFile.length());
               m_Table.setValueAt(sFile2, i, iCol);
               if (i == iRow) {
                  textField.setText(sFile2);
               }
            }
         }
         else {
            textField.setText(sFile);
         }
      }

   }


   private void assignMultipleInput() {

      AdditionalInfoMultipleInput ai;
      final ParameterMultipleInput parameter = (ParameterMultipleInput) m_Object;
      try {
         ai = (AdditionalInfoMultipleInput) parameter.getParameterAdditionalInfo();
         if (ai.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_BAND) {
            final MultipleBandSelectionDialog dialog = new MultipleBandSelectionDialog(textField.getText());
            dialog.pack();
            dialog.setVisible(true);
            final String sSel = dialog.getSelectionAsString();
            if (sSel != null) {
               textField.setText(sSel);
            }
         }
         else {
            int returnVal;
            String sExt[];
            String sDesc;
            final JFileChooser fc = new JFileChooser();

            if (ai.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_RASTER) {
               sExt = SextanteGUI.getInputFactory().getRasterLayerInputExtensions();
               sDesc = Sextante.getText("Raster_layers");
            }
            else if ((ai.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY)
                     || (ai.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT)
                     || (ai.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE)
                     || (ai.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON)) {
               sExt = SextanteGUI.getInputFactory().getVectorLayerInputExtensions();
               sDesc = Sextante.getText("Vector_layer");
            }
            else if (ai.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_TABLE) {
               sExt = SextanteGUI.getInputFactory().getTableInputExtensions();
               sDesc = Sextante.getText("Tables");
            }
            else {
               return;
            }

            fc.setFileFilter(new GenericFileFilter(sExt, sDesc));
            fc.setMultiSelectionEnabled(true);
            returnVal = fc.showOpenDialog(this.getParent().getParent());

            if (returnVal == JFileChooser.APPROVE_OPTION) {
               final StringBuffer sValue = new StringBuffer();
               final File[] files = fc.getSelectedFiles();
               for (int i = 0; i < files.length; i++) {
                  sValue.append(files[i].getAbsolutePath());
                  if (i < files.length - 1) {
                     sValue.append(",");
                  }
               }
               m_Table.getModel().setValueAt(sValue.toString(), m_Table.getSelectedRow(), m_Table.getSelectedColumn());
               textField.setText(sValue.toString());
            }
         }
      }
      catch (final Exception e) {}

   }


   private void assignFixedTable() {

      final FixedTableModel model = getTableModelFromString();


      final FixedTableDialog dialog = new FixedTableDialog(SextanteGUI.getMainFrame(), model);

      dialog.pack();
      dialog.setVisible(true);

      textField.setText(model.getAsCSV());

   }


   public FixedTableModel getTableModelFromString() {

      boolean bIsNumberOfRowsFixed;
      int iCols, iRows;
      int iCol, iRow;
      int iToken = 0;
      FixedTableModel tableModel = null;
      final StringTokenizer st = new StringTokenizer(textField.getText(), ",");
      String sToken;
      AdditionalInfoFixedTable ai;
      final Parameter parameter = (Parameter) m_Object;
      try {
         ai = (AdditionalInfoFixedTable) parameter.getParameterAdditionalInfo();
         iCols = ai.getColsCount();
         iRows = ai.getRowsCount();
         bIsNumberOfRowsFixed = ai.isNumberOfRowsFixed();
         tableModel = new FixedTableModel(ai.getCols(), iCols, bIsNumberOfRowsFixed);

         if (bIsNumberOfRowsFixed) {
            if (st.countTokens() != iCols * iRows) {
               return tableModel;
            }
         }
         else {
            if (iToken % iCols != 0) {
               return tableModel;
            }
         }

         while (st.hasMoreTokens()) {
            iCol = (int) Math.floor(iToken / (double) iCols);
            iRow = iToken % iCols;
            sToken = st.nextToken().trim();
            tableModel.setValueAt(sToken, iCol, iRow);
            iToken++;
         }

         return tableModel;
      }
      catch (final Exception e) {
         return tableModel;
      }

   }


   private void assignBand() {

      final int MAX_BANDS = 250;

      final String[] possibilities = new String[MAX_BANDS];
      for (int i = 0; i < possibilities.length; i++) {
         possibilities[i] = Integer.toString(i + 1);
      }

      final String s = (String) JOptionPane.showInputDialog(null, Sextante.getText("Band"), Sextante.getText("Band"),
               JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0]);

      if (s != null) {
         textField.setText(s);
      }

   }


   private void assignPoint() {

      final Frame window = new Frame();
      final Point2D pt = new Point2D.Double();
      final PointSelectionDialog dialog = new PointSelectionDialog(window, pt);

      dialog.pack();
      dialog.setVisible(true);

      if (dialog.getOK()) {
         textField.setText(Double.toString(pt.getX()) + "," + Double.toString(pt.getY()));
      }

   }


   private void assignSelection() {

      final Parameter parameter = (Parameter) m_Object;
      try {
         final AdditionalInfoSelection ai = (AdditionalInfoSelection) parameter.getParameterAdditionalInfo();

         final String[] possibilities = ai.getValues();
         final String s = (String) JOptionPane.showInputDialog(null, parameter.getParameterDescription(),
                  parameter.getParameterDescription(), JOptionPane.PLAIN_MESSAGE, null, possibilities, possibilities[0]);

         if (s != null) {
            textField.setText(s);
         }


      }
      catch (final NullParameterAdditionalInfoException e) {}


   }


   private void assignBoolean() {

      final Parameter parameter = (Parameter) m_Object;
      final String[] possibilities = { Sextante.getText("Yes"), Sextante.getText("No") };
      final String s = (String) JOptionPane.showInputDialog(null, parameter.getParameterDescription(),
               parameter.getParameterDescription(), JOptionPane.PLAIN_MESSAGE, null, possibilities, Sextante.getText("Yes"));

      if (s != null) {
         if (s.equals(Sextante.getText("Yes"))) {
            textField.setText("true");
         }
         else {
            textField.setText("false");
         }
      }

   }


   private void assignDataObject() {

      int returnVal;
      String sExt[];
      String sDesc;
      final JFileChooser fc = new JFileChooser();

      if (m_Object instanceof ParameterRasterLayer) {
         sExt = SextanteGUI.getInputFactory().getRasterLayerInputExtensions();
         sDesc = Sextante.getText("Raster_layers");
      }
      else if (m_Object instanceof ParameterVectorLayer) {
         sExt = SextanteGUI.getInputFactory().getVectorLayerInputExtensions();
         sDesc = Sextante.getText("Vector_layers");
      }
      else if (m_Object instanceof ParameterTable) {
         sExt = SextanteGUI.getInputFactory().getTableInputExtensions();
         sDesc = Sextante.getText("Tables");
      }
      else if (m_Object instanceof Parameter3DRasterLayer) {
         sExt = SextanteGUI.getInputFactory().get3DRasterLayerInputExtensions();
         sDesc = Sextante.getText("3D_Raster_layers");
      }
      else {
         return;
      }

      fc.setFileFilter(new GenericFileFilter(sExt, sDesc));
      fc.setMultiSelectionEnabled(true);
      returnVal = fc.showOpenDialog(this.getParent().getParent());

      if (returnVal == JFileChooser.APPROVE_OPTION) {
         final StringBuffer sValue = new StringBuffer();
         final File[] files = fc.getSelectedFiles();
         for (int i = 0; i < files.length; i++) {
            sValue.append(files[i].getAbsolutePath());
            if (i < files.length - 1) {
               sValue.append(",");
            }
         }
         m_Table.getModel().setValueAt(sValue.toString(), m_Table.getSelectedRow(), m_Table.getSelectedColumn());
         textField.setText(fc.getSelectedFile().getAbsolutePath());
      }

   }


   public String getValue() {

      return textField.getText();

   }


   public void setValue(final String sText) {

      textField.setText(sText);

   }


   public boolean isFilenameCell() {

      return ((m_Object instanceof ParameterDataObject) || !(m_Object instanceof Parameter));

   }

}
