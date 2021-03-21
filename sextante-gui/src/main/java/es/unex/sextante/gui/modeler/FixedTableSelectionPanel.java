

package es.unex.sextante.gui.modeler;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.WrongParameterIDException;
import es.unex.sextante.gui.algorithm.FixedTableDialog;
import es.unex.sextante.modeler.elements.ModelElementFixedTable;
import es.unex.sextante.parameters.FixedTableModel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterFixedTable;


public class FixedTableSelectionPanel
         extends
            JPanel {

   private FixedTableModel              m_FixedTableModel;
   private JComboBox                    jComboBox;
   private JButton                      button;
   private final ObjectAndDescription[] m_Tables;
   private final HashMap                m_DataObjects;
   private final ModelAlgorithm         m_Algorithm;


   public FixedTableSelectionPanel(final AdditionalInfoFixedTable addInfo,
                                   final ObjectAndDescription[] tables,
                                   final HashMap dataObjects,
                                   final ModelAlgorithm algorithm) {

      super();

      m_Tables = tables;
      m_DataObjects = dataObjects;
      m_FixedTableModel = new FixedTableModel(addInfo.getCols(), addInfo.getRowsCount(), addInfo.isNumberOfRowsFixed());
      m_Algorithm = algorithm;

      initGUI(tables);

   }


   private void initGUI(final ObjectAndDescription[] tables) {

      button = new JButton("...");

      button.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            btnActionPerformed(evt);
         }
      });

      jComboBox = new JComboBox(getValidTables());

      final TableLayout thisLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL, 25.0 },
               { TableLayoutConstants.FILL } });
      this.setLayout(thisLayout);
      this.add(jComboBox, "0,  0");
      this.add(button, "1,  0");

   }


   private ObjectAndDescription[] getValidTables() {

      int i;
      boolean bValid;
      final ArrayList array = new ArrayList();
      String sKey;
      String sDescription;
      ObjectAndDescription[] validTables;
      Object obj;
      ParametersSet ps;
      Parameter param;
      AdditionalInfoFixedTable addInfo;

      for (i = 0; i < m_Tables.length; i++) {
         sKey = (String) m_Tables[i].getObject();
         sDescription = m_Tables[i].getDescription();
         obj = ((ObjectAndDescription) m_DataObjects.get(sKey)).getObject();
         if ((obj != null) && (obj instanceof ModelElementFixedTable)) {
            final ModelElementFixedTable meft = (ModelElementFixedTable) obj;
            ps = m_Algorithm.getParameters();
            try {
               param = ps.getParameter(sKey);
               if (param instanceof ParameterFixedTable) {
                  addInfo = (AdditionalInfoFixedTable) ((ParameterFixedTable) param).getParameterAdditionalInfo();
                  bValid = (m_FixedTableModel.getColumnCount() == meft.getColsCount());
                  if (m_FixedTableModel.isNumberOfRowsFixed()) {
                     bValid = bValid && (m_FixedTableModel.getRowCount() == addInfo.getRowsCount());
                  }
                  if (bValid) {
                     array.add(new ObjectAndDescription(sDescription, sKey));
                  }
               }
            }
            catch (final WrongParameterIDException e) {
               Sextante.addErrorToLog(e);
            }
            catch (final NullParameterAdditionalInfoException e) {
               Sextante.addErrorToLog(e);
            }
         }
      }

      final StringBuffer sText = new StringBuffer(Sextante.getText("Fixed_table") + "(");
      sText.append(m_FixedTableModel.getDimensionsAsString());
      sText.append(")");

      validTables = new ObjectAndDescription[array.size() + 1];
      validTables[0] = new ObjectAndDescription(sText.toString(), null);

      for (i = 0; i < array.size(); i++) {
         validTables[i + 1] = (ObjectAndDescription) array.get(i);
      }

      return validTables;

   }


   public FixedTableModel getTableModel() {

      return m_FixedTableModel;

   }


   private void btnActionPerformed(final ActionEvent e) {

      final Frame window = new Frame();

      final FixedTableDialog dialog = new FixedTableDialog(window, m_FixedTableModel);
      dialog.pack();
      dialog.setVisible(true);

      if (dialog.accepted()) {
         jComboBox.setSelectedIndex(0);
      }

   }


   public String getTableKey() {

      final ObjectAndDescription oad = (ObjectAndDescription) jComboBox.getSelectedItem();
      final String sKey = (String) oad.getObject();
      return sKey;

   }


   public void setTable(final Object ob) {

      if (ob instanceof FixedTableModel) {
         m_FixedTableModel = (FixedTableModel) ob;
      }
      else if (ob instanceof String) {
         final String sKey = (String) ob;
         for (int i = 1; i < jComboBox.getModel().getSize(); i++) { //1, because the first one is the user-defined table (=null)
            final ObjectAndDescription oad = (ObjectAndDescription) jComboBox.getModel().getElementAt(i);
            if (oad.getObject().equals(sKey)) {
               jComboBox.setSelectedIndex(i);
            }
         }
      }

   }

}
