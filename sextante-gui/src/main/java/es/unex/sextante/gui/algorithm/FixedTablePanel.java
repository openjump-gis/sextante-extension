package es.unex.sextante.gui.algorithm;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.parameters.FixedTableModel;

/**
 * A panel with a text field and a button. The text field shows a text representation of a fixed table. Clicking on the button
 * shows a dialog to fill the data of the fixed table
 *
 * @author volaya
 *
 */
public class FixedTablePanel
         extends
            JPanel {

   private FixedTableModel m_FixedTableModel;
   private JTextField      textField;
   private JButton         button;


   /**
    * Creates a new fixed table panel
    *
    * @param sColumnNames
    *                the names of the columns in the table
    * @param iNumRows
    *                the number of rows
    * @param bIsNumberOfRowsFixed
    *                true if the number of rows cannot be modified
    */
   public FixedTablePanel(final String[] sColumnNames,
                          final int iNumRows,
                          final boolean bIsNumberOfRowsFixed) {

      super();

      m_FixedTableModel = new FixedTableModel(sColumnNames, iNumRows, bIsNumberOfRowsFixed);

      InitGUI();

   }


   private void InitGUI() {

      button = new JButton("...");

      final StringBuffer sText = new StringBuffer(Sextante.getText("Fixed_table") + " (");
      sText.append(m_FixedTableModel.getDimensionsAsString());
      sText.append(")");
      textField = new JTextField(sText.toString());
      textField.setEditable(false);

      button.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            btnActionPerformed(evt);
         }
      });

      final TableLayout thisLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL, 25.0 }, { TableLayoutConstants.FILL } });
      this.setLayout(thisLayout);
      this.add(textField, "0,  0");
      this.add(button, "1,  0");

   }


   /**
    * Gets the table model defined using this panel
    *
    * @return the fixed table model
    */
   public FixedTableModel getTableModel() {

      return m_FixedTableModel;

   }


   /**
    * Sets the table model
    *
    * @param model
    *                the model to set
    */
   public void setTableModel(final FixedTableModel model) {

      m_FixedTableModel = model;

      final StringBuffer sText = new StringBuffer(Sextante.getText("Fixed_table") + " (");
      sText.append(m_FixedTableModel.getDimensionsAsString());
      sText.append(")");

      textField.setText(sText.toString());

   }


   private void btnActionPerformed(final ActionEvent e) {

      final StringBuffer sText = new StringBuffer(Sextante.getText("Fixed_table") + " (");

      final FixedTableDialog dialog = new FixedTableDialog(SextanteGUI.getMainFrame(), m_FixedTableModel);

      dialog.pack();
      dialog.setVisible(true);

      sText.append(m_FixedTableModel.getDimensionsAsString());
      sText.append(")");

      textField.setText(sText.toString());

   }

}
