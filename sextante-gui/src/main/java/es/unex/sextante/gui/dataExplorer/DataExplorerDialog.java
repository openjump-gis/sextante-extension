package es.unex.sextante.gui.dataExplorer;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;

import es.unex.sextante.core.Sextante;

public class DataExplorerDialog
         extends
            JDialog {

   private DataExplorerPanel m_DataExplorerPanel;


   public DataExplorerDialog(final Frame parent) {

      super(parent, Sextante.getText("Data"), true);

      this.setResizable(true);

      init();
      setLocationRelativeTo(null);

   }


   private void init() {

      this.setPreferredSize(new java.awt.Dimension(350, 380));
      this.setSize(new java.awt.Dimension(350, 380));

      final BorderLayout thisLayout = new BorderLayout();
      this.setLayout(thisLayout);

      m_DataExplorerPanel = new DataExplorerPanel();
      this.add(m_DataExplorerPanel, BorderLayout.CENTER);

   }

}
