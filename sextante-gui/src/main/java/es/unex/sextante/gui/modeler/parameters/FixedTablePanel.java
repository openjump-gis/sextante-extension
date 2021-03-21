

package es.unex.sextante.gui.modeler.parameters;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;

import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.gui.modeler.ModelerPanel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterFixedTable;


public class FixedTablePanel
         extends
            ParameterPanel {

   private JSpinner  jSpinnerRows;
   private JLabel    jLabelRows;
   private JCheckBox jCheckBox;
   private JSpinner  jSpinnerCols;
   private JLabel    jLabelCols;


   public FixedTablePanel(final JDialog parent,
                          final ModelerPanel panel) {

      super(parent, panel);

   }


   public FixedTablePanel(final ModelerPanel panel) {

      super(panel);

   }


   @Override
   public String getParameterDescription() {

      return Sextante.getText("Fixed_table");

   }


   @Override
   protected void initGUI() {

      final Integer values[] = new Integer[15];
      super.initGUI();

      try {
         {
            final TableLayout thisLayout = new TableLayout(
                     new double[][] {
                              { TableLayoutConstants.FILL, 5.0, TableLayoutConstants.FILL },
                              { TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
                                       TableLayoutConstants.FILL } });
            thisLayout.setHGap(5);
            thisLayout.setVGap(5);
            jPanelMiddle.setLayout(thisLayout);
            for (int i = 0; i < 15; i++) {
               values[i] = new Integer(i + 1);
            }
            {
               jCheckBox = new JCheckBox();
               jPanelMiddle.add(jCheckBox, "2, 1");
               jCheckBox.setText(Sextante.getText("Fixed_number_of_rows"));
            }
            {
               jLabelRows = new JLabel();
               jPanelMiddle.add(jLabelRows, "0, 0");
               jLabelRows.setText(Sextante.getText("Number_of_rows"));
            }
            {
               jSpinnerRows = new JSpinner(new SpinnerListModel(values));
               jPanelMiddle.add(jSpinnerRows, "2, 0");
            }
            {
               jLabelCols = new JLabel();
               jPanelMiddle.add(jLabelCols, "0, 3");
               jLabelCols.setText(Sextante.getText("Number_of_columns"));
            }
            {
               jSpinnerCols = new JSpinner(new SpinnerListModel(values));
               jPanelMiddle.add(jSpinnerCols, "2, 3");
            }
         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   protected boolean prepareParameter() {

      int iRows, iCols;
      boolean bIsNumberOfRowsFixed;

      try {
         bIsNumberOfRowsFixed = jCheckBox.isSelected();
         iRows = ((Integer) jSpinnerRows.getValue()).intValue();
         iCols = ((Integer) jSpinnerCols.getValue()).intValue();
      }
      catch (final Exception e) {
         JOptionPane.showMessageDialog(null, Sextante.getText("Invalid_parameters"), Sextante.getText("Warning"),
                  JOptionPane.WARNING_MESSAGE);
         return false;
      }

      final String sDescription = jTextFieldDescription.getText();

      if (sDescription.length() != 0) {

         final String sCols[] = new String[iCols];
         for (int i = 0; i < iCols; i++) {
            sCols[i] = Integer.toString(i + 1);
         }
         final AdditionalInfoFixedTable addInfo = new AdditionalInfoFixedTable(sCols, iRows, bIsNumberOfRowsFixed);
         m_Parameter = new ParameterFixedTable();
         m_Parameter.setParameterAdditionalInfo(addInfo);
         m_Parameter.setParameterDescription(jTextFieldDescription.getText());
         return true;
      }
      else {
         JOptionPane.showMessageDialog(null, Sextante.getText("Invalid_description"), Sextante.getText("Warning"),
                  JOptionPane.WARNING_MESSAGE);
         return false;
      }

   }


   @Override
   public void setParameter(final Parameter param) {

      super.setParameter(param);

      try {
         final AdditionalInfoFixedTable ai = (AdditionalInfoFixedTable) param.getParameterAdditionalInfo();
         jSpinnerCols.setValue(new Integer(ai.getColsCount()));
         jSpinnerRows.setValue(new Integer(ai.getRowsCount()));
      }
      catch (final NullParameterAdditionalInfoException e) {
         e.printStackTrace();
      }

   }


   @Override
   public boolean parameterCanBeAdded() {

      return true;

   }


}
