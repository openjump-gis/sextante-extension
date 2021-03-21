package es.unex.sextante.gui.batch;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import es.unex.sextante.core.Sextante;

/**
 * A panel to define autocompletion parameters in a file chooser
 * 
 * @author volaya
 * 
 */
public class AutoCompletePanel
         extends
            JPanel {

   public static final int NO_AUTOFILL     = 0;
   public static final int AUTOFILL_NUMBER = 1;
   public static final int AUTOFILL_FIELD  = 2;

   private JRadioButton    jAutoFillNumberRB;
   private JRadioButton    jAutoFillFieldRB;
   private JRadioButton    jNoAutoFillRB;
   private JComboBox       m_FieldComboBox;


   public AutoCompletePanel(final String sFields[]) {

      super();

      initGUI(sFields);

   }


   private void initGUI(final String sFields[]) {

      try {
         this.setPreferredSize(new java.awt.Dimension(400, 100));
         {

            final TableLayout layout = new TableLayout(new double[][] {
                     { 5.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 5.0 },
                     { 10.0, 20, TableLayoutConstants.FILL, 20, TableLayoutConstants.FILL, 20, 10.0 } });
            layout.setHGap(5);
            layout.setVGap(5);
            this.setLayout(layout);
            this.setBorder(BorderFactory.createTitledBorder(Sextante.getText("Autofill")));
            jNoAutoFillRB = new JRadioButton(Sextante.getText("Do_not_autofill"));
            jNoAutoFillRB.setSelected(true);
            this.add(jNoAutoFillRB, "1,1");
            jAutoFillNumberRB = new JRadioButton(Sextante.getText("Autofill_with_numbers"));
            this.add(jAutoFillNumberRB, "1,3");
            jAutoFillFieldRB = new JRadioButton(Sextante.getText("Autofill_with_field_values"));
            this.add(jAutoFillFieldRB, "1,5");
            m_FieldComboBox = new JComboBox(sFields);
            this.add(m_FieldComboBox, "2,5");
            final ButtonGroup group = new ButtonGroup();
            group.add(jNoAutoFillRB);
            group.add(jAutoFillNumberRB);
            group.add(jAutoFillFieldRB);
         }
      }
      catch (final Exception e) {}

   }


   public int getAutoFill() {

      if (jAutoFillNumberRB.isSelected()) {
         return AUTOFILL_NUMBER;
      }
      else if (jAutoFillFieldRB.isSelected()) {
         return AUTOFILL_FIELD;
      }
      else {
         return NO_AUTOFILL;
      }

   }


   public int getField() {

      return m_FieldComboBox.getSelectedIndex();

   }

}
