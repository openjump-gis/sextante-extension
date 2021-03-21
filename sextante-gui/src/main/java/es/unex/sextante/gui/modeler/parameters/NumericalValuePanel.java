package es.unex.sextante.gui.modeler.parameters;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.gui.modeler.ModelerPanel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterNumericalValue;

public class NumericalValuePanel
         extends
            ParameterPanel {

   private JCheckBox  jCheckBoxMin;
   private JCheckBox  jCheckBoxMax;
   private JLabel     jLabelType;
   private JTextField jTextFieldMin;
   private JTextField jTextFieldMax;
   private JTextField jTextFieldDefault;
   private JComboBox  jComboBoxType;
   private JLabel     jLabelDefault;


   public NumericalValuePanel(final JDialog parent,
                              final ModelerPanel panel) {

      super(parent, panel);

   }


   public NumericalValuePanel(final ModelerPanel panel) {

      super(panel);

   }


   @Override
   protected void initGUI() {

      super.initGUI();

      try {
         {
            final TableLayout thisLayout = new TableLayout(new double[][] {
                     { TableLayoutConstants.FILL, 5.0, TableLayoutConstants.FILL },
                     { TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL, TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL, TableLayoutConstants.MINIMUM,
                              TableLayoutConstants.FILL, TableLayoutConstants.MINIMUM } });
            thisLayout.setHGap(5);
            thisLayout.setVGap(5);
            jPanelMiddle.setLayout(thisLayout);
            {
               jCheckBoxMin = new JCheckBox();
               jPanelMiddle.add(jCheckBoxMin, "0, 0");
               jCheckBoxMin.setText(Sextante.getText("Min_value"));
            }
            {
               jCheckBoxMax = new JCheckBox();
               jPanelMiddle.add(jCheckBoxMax, "0, 2");
               jCheckBoxMax.setText(Sextante.getText("Max_value"));
            }
            {
               jLabelType = new JLabel();
               jPanelMiddle.add(jLabelType, "0, 6");
               jLabelType.setText(Sextante.getText("Value_type"));
            }
            {
               final ComboBoxModel jComboBoxTypeModel = new DefaultComboBoxModel(new String[] { Sextante.getText("Integer"),
                        Sextante.getText("Float") });
               jComboBoxType = new JComboBox();
               jPanelMiddle.add(jComboBoxType, "2, 6");
               jComboBoxType.setModel(jComboBoxTypeModel);
            }
            {
               jLabelDefault = new JLabel();
               jPanelMiddle.add(jLabelDefault, "0, 4");
               jLabelDefault.setText(Sextante.getText("Default_value"));

               jTextFieldDefault = new JTextField();
               jPanelMiddle.add(jTextFieldDefault, "2, 4");
               jTextFieldDefault.addFocusListener(new FocusAdapter() {
                  @Override
                  public void focusLost(final FocusEvent e) {
                     checkTextFieldContent((JTextField) e.getSource());
                  }
               });
            }
            {
               jTextFieldMax = new JTextField();
               jPanelMiddle.add(jTextFieldMax, "2, 2");
               jTextFieldMax.addFocusListener(new FocusAdapter() {
                  @Override
                  public void focusLost(final FocusEvent e) {
                     checkTextFieldContent((JTextField) e.getSource());
                  }
               });
            }
            {
               jTextFieldMin = new JTextField();
               jPanelMiddle.add(jTextFieldMin, "2, 0");
               jTextFieldMin.addFocusListener(new FocusAdapter() {
                  @Override
                  public void focusLost(final FocusEvent e) {
                     checkTextFieldContent((JTextField) e.getSource());
                  }
               });
            }
         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   protected boolean prepareParameter() {

      double dMax, dMin;
      double dAbsoluteMax, dAbsoluteMin;
      double dDefault;
      int iType;

      try {
         if (jComboBoxType.getSelectedIndex() == 0) {
            iType = AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER;
            dAbsoluteMin = Integer.MIN_VALUE;
            dAbsoluteMax = Integer.MAX_VALUE;
         }
         else {
            iType = AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE;
            dAbsoluteMin = Double.NEGATIVE_INFINITY;
            dAbsoluteMax = Double.MAX_VALUE;
         }
         dDefault = Double.parseDouble(jTextFieldDefault.getText());
         if (jCheckBoxMin.isSelected()) {
            dMin = Double.parseDouble(jTextFieldMin.getText());
         }
         else {
            dMin = dAbsoluteMin;
         }
         if (jCheckBoxMax.isSelected()) {
            dMax = Double.parseDouble(jTextFieldMax.getText());
         }
         else {
            dMax = dAbsoluteMax;
         }
      }
      catch (final Exception e) {
         JOptionPane.showMessageDialog(null, Sextante.getText("Invalid_parameters"), Sextante.getText("Warning"),
                  JOptionPane.WARNING_MESSAGE);
         return false;
      }

      final String sDescription = jTextFieldDescription.getText();

      if (sDescription.length() != 0) {
         final AdditionalInfoNumericalValue addInfo = new AdditionalInfoNumericalValue(iType, dDefault, Math.min(dMin, dMax), Math.max(
                  dMin, dMax));
         m_Parameter = new ParameterNumericalValue();
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
         final AdditionalInfoNumericalValue ai = (AdditionalInfoNumericalValue) param.getParameterAdditionalInfo();
         if (ai.getMaxValue() != Double.MAX_VALUE && ai.getMaxValue() != Integer.MAX_VALUE) {
            jCheckBoxMax.setSelected(true);
            jTextFieldMax.setText(Double.toString(ai.getMaxValue()));
         }
         if (ai.getMinValue() != Double.NEGATIVE_INFINITY && ai.getMinValue() != Integer.MIN_VALUE) {
            jCheckBoxMin.setSelected(true);
            jTextFieldMin.setText(Double.toString(ai.getMinValue()));
         }
         jTextFieldDefault.setText(Double.toString(ai.getDefaultValue()));
         if (ai.getType() == AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER) {
            jComboBoxType.setSelectedIndex(0);
         }
         else {
            jComboBoxType.setSelectedIndex(1);
         }
      }
      catch (final NullParameterAdditionalInfoException e) {
         e.printStackTrace();
      }

   }


   @Override
   public String getParameterDescription() {

      return Sextante.getText("Numerical_value");

   }


   private void checkTextFieldContent(final JTextField textField) {

      final String content = textField.getText();
      if (content.length() != 0) {
         try {
            Double.parseDouble(content);
            return;
         }
         catch (final NumberFormatException nfe) {
            getToolkit().beep();
            textField.requestFocus();
         }
      }


   }


   @Override
   public boolean parameterCanBeAdded() {

      return true;

   }

}
