package es.unex.sextante.gui.core;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * A simple dialog to show a java component with which the user might interact
 * 
 * @author volaya
 * 
 */
public class GenericDialog
         extends
            JDialog {

   /**
    * constructor
    * 
    * @param component
    *                the component to show in the dialog
    * @param sTitle
    *                the title of the dialog
    * @param parent
    *                the parent frame
    */
   public GenericDialog(final Component component,
                        final String sTitle,
                        final Frame parent) {

      super(parent, sTitle, true);

      initGUI(component);

      pack();
      setLocationRelativeTo(null);

   }


   protected void initGUI(final Component component) {

      setPreferredSize(component.getPreferredSize());
      setSize(component.getPreferredSize());
      final BorderLayout thisLayout = new BorderLayout();
      final JPanel pane = new JPanel();
      pane.setLayout(thisLayout);
      setContentPane(pane);
      pane.add(component, BorderLayout.CENTER);

   }


}
