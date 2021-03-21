package es.unex.sextante.gui.help;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import es.unex.sextante.core.Sextante;

public class ImageSelectionDialog
         extends
            javax.swing.JDialog {

   private JPanel              jPanelMain;
   private JSeparator          jSeparator;
   private JButton             jButtonCancel;
   private JButton             jButtonOK;
   private boolean             m_bOK = false;
   private ImageSelectionPanel m_ImageSelectionPanel;
   private final ImageAndDescription m_ImageAndDescription;
   private final String              m_sHomeDir;


   public ImageSelectionDialog(final Frame window,
                               final ImageAndDescription iad,
                               final String sHomeDir) {

      super(window, "", true);
      this.setResizable(false);

      m_sHomeDir = sHomeDir;
      m_ImageAndDescription = iad;

      initGUI();

   }


   private void initGUI() {
      try {
         {
            this.setTitle(Sextante.getText("Image"));
            {
               jPanelMain = new JPanel();
               getContentPane().add(jPanelMain, BorderLayout.CENTER);
               jPanelMain.setPreferredSize(new java.awt.Dimension(400, 150));
               {
                  m_ImageSelectionPanel = new ImageSelectionPanel(m_sHomeDir);
                  jPanelMain.add(m_ImageSelectionPanel);

               }
               {
                  jSeparator = new JSeparator();
                  jPanelMain.add(jSeparator);
                  jSeparator.setPreferredSize(new java.awt.Dimension(350, 20));
               }
               {
                  jButtonOK = new JButton();
                  jPanelMain.add(jButtonOK);
                  jButtonOK.setText(Sextante.getText("OK"));
                  jButtonOK.addActionListener(new ActionListener() {
                     public void actionPerformed(final ActionEvent evt) {
                        btnOKActionPerformed(evt);
                     }
                  });
               }
               {
                  jButtonCancel = new JButton();
                  jPanelMain.add(jButtonCancel);
                  jButtonCancel.setText(Sextante.getText("Cancel"));
                  jButtonCancel.addActionListener(new ActionListener() {
                     public void actionPerformed(final ActionEvent evt) {
                        btnCancelActionPerformed(evt);
                     }
                  });
               }
            }
         }
         this.setSize(350, 100);
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }
   }


   private void btnOKActionPerformed(final ActionEvent evt) {

      final File f = new File(m_ImageSelectionPanel.getFilename());
      final File home = new File(m_sHomeDir);
      String sFilename = FileUtilities.getRelativePath(home, f);
      sFilename = sFilename.replaceAll("\\\\", "/");
      m_ImageAndDescription.setFilename(sFilename);
      m_ImageAndDescription.setDescription(m_ImageSelectionPanel.getDescription());

      m_bOK = true;
      dispose();
      setVisible(false);

   }


   private void btnCancelActionPerformed(final ActionEvent evt) {

      m_bOK = false;
      dispose();
      setVisible(false);


   }


   public boolean getOK() {

      return m_bOK;

   }

}
