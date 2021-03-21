package es.unex.sextante.gui.modeler;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JPanel;

import es.unex.sextante.core.Sextante;

public class ModelerDialog
         extends
            JDialog {

   private ModelerPanel modelerPanel;


   public ModelerDialog(final Frame parent) {

      super(parent, Sextante.getText("Modeler"), false);

      initialize();
      setLocationRelativeTo(null);

   }


   public void initialize() {

      final JPanel pane = new JPanel();
      this.setContentPane(pane);
      this.setSize(new java.awt.Dimension(600, 500));
      {

         final BorderLayout thisLayout = new BorderLayout();
         this.getContentPane().setLayout(thisLayout);

         modelerPanel = new ModelerPanel(this);
         this.getContentPane().setLayout(thisLayout);
         this.getContentPane().add(modelerPanel);

      }

   }


   public ModelerPanel getModelerPanel() {

      return modelerPanel;

   }

}
