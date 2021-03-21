package es.unex.sextante.gui.batch;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A panel with a text field and a button. Extend this class to define it behaviour when the button is cliked
 * 
 * @author volaya
 * 
 */
public abstract class TextFieldAndButton
         extends
            JPanel {

   protected JTextField textField;
   private JButton      button;


   public TextFieldAndButton() {

      super();

      initGUI();

   }


   private void initGUI() {

      button = new JButton("...");

      textField = new JTextField("");
      textField.setMaximumSize(new java.awt.Dimension(340, 18));
      button.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            btnActionPerformed();
         }
      });

      final TableLayout thisLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL, 25.0 },
               { TableLayoutConstants.FILL } });
      this.setLayout(thisLayout);
      this.add(textField, "0,  0");
      this.add(button, "1,  0");

   }


   /**
    * This method is called when the button is pressed. Override this method to implement the desired behaviour
    * 
    */
   protected abstract void btnActionPerformed();


   public String getValue() {

      return textField.getText();

   }


   public void setValue(final String sText) {

      textField.setText(sText);

   }


}
