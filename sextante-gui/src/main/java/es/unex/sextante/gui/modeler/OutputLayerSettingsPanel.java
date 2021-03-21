package es.unex.sextante.gui.modeler;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.unex.sextante.core.Sextante;

public class OutputLayerSettingsPanel
         extends
            JPanel {

   private JCheckBox  jCheckBoxAdd;
   private JTextField jTextFieldName;
   private JLabel     jLabel1;


   public OutputLayerSettingsPanel() {

      super();
      initGUI();

   }


   private void initGUI() {
      try {
         {
            final TableLayout thisLayout = new TableLayout(new double[][] {
                     { TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL },
                     { TableLayoutConstants.FILL, 1.0, TableLayoutConstants.FILL } });
            thisLayout.setHGap(5);
            thisLayout.setVGap(5);
            this.setLayout(thisLayout);
            this.setPreferredSize(new java.awt.Dimension(271, 48));
            {
               jLabel1 = new JLabel();
               this.add(jLabel1, "0, 2");
               jLabel1.setText(Sextante.getText("Name"));
            }
            {
               jCheckBoxAdd = new JCheckBox(Sextante.getText("Keep_as_final_result"));
               jCheckBoxAdd.addItemListener(new ItemListener() {
                  public void itemStateChanged(final ItemEvent e) {
                     jTextFieldName.setEnabled(jCheckBoxAdd.isSelected());
                  }
               });
               this.add(jCheckBoxAdd, "0, 0, 2, 0");
            }
            {
               jTextFieldName = new JTextField();
               jTextFieldName.setEnabled(false);
               this.add(jTextFieldName, "1, 2, 2, 2");
            }
         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }
   }


   public void setKeepAsFinalResult(final boolean bKeep) {

      jCheckBoxAdd.setSelected(bKeep);

   }


   public boolean getKeepAsFinalResult() {

      return this.jCheckBoxAdd.isSelected();

   }


   @Override
   public void setName(final String sName) {

      jTextFieldName.setText(sName);

   }


   @Override
   public String getName() {

      return jTextFieldName.getText();

   }


}
