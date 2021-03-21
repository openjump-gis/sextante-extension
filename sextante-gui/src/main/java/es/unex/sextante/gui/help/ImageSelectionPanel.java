package es.unex.sextante.gui.help;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.algorithm.FileSelectionPanel;

public class ImageSelectionPanel
         extends
            JPanel {

   private JLabel             jLabelDescription;
   private JTextField         jTextFieldDescription;
   private FileSelectionPanel fileSelectionPanel;
   private JLabel             jLabelFilename;
   private final String             m_sHomeDir;


   public ImageSelectionPanel(final String homeDir) {

      super();

      m_sHomeDir = homeDir;

      initGUI();

   }


   private void initGUI() {

      try {
         {
            final TableLayout thisLayout = new TableLayout(new double[][] { { 5.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 5.0 },
                     { 5.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 5.0 } });
            thisLayout.setHGap(5);
            thisLayout.setVGap(5);
            this.setLayout(thisLayout);
            this.setPreferredSize(new java.awt.Dimension(372, 68));
            {
               jLabelFilename = new JLabel();
               this.add(jLabelFilename, "1, 1");
               jLabelFilename.setText(Sextante.getText("File"));
            }
            {
               fileSelectionPanel = new FileSelectionPanel(false, true, new String[] { "jpg", "gif", "png" },
                        Sextante.getText("Images"), m_sHomeDir);
               this.add(fileSelectionPanel, "2, 1");
            }
            {
               jLabelDescription = new JLabel();
               this.add(jLabelDescription, "1, 2");
               jLabelDescription.setText(Sextante.getText("Description"));
            }
            {
               jTextFieldDescription = new JTextField();
               this.add(jTextFieldDescription, "2, 2");
            }
         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }
   }


   public String getFilename() {

      return fileSelectionPanel.getFilepath();

   }


   public String getDescription() {

      return jTextFieldDescription.getText();

   }

}
