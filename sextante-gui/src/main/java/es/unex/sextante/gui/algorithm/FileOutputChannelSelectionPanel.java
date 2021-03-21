package es.unex.sextante.gui.algorithm;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

public class FileOutputChannelSelectionPanel
         extends
            JPanel {

   private final ActionListener m_Listener;
   private final String         m_sDescription;
   private final String[]       m_sExtensions;
   private JFileChooser         m_Chooser;


   public FileOutputChannelSelectionPanel(final String sDescription,
                                          final String[] sExtensions,
                                          final ActionListener listener) {

      m_sDescription = sDescription;
      m_sExtensions = sExtensions;
      m_Listener = listener;
      initGUI();

   }


   private void initGUI() {

      this.setLayout(new BorderLayout());
      m_Chooser = new JFileChooser();
      m_Chooser.setDialogType(JFileChooser.SAVE_DIALOG);
      m_Chooser.setFileFilter(new GenericFileFilter(m_sExtensions, m_sDescription));
      m_Chooser.addActionListener(m_Listener);
      this.add(m_Chooser);

   }


   public String getSelectedFile() {

      return m_Chooser.getSelectedFile().getAbsolutePath();

   }


}
