package es.unex.sextante.gui.algorithm;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A panel with a text field and a button, which pops-up a file selection dialog and puts the selected file in the text field
 * 
 * @author volaya
 * 
 */
public class FileSelectionPanel
         extends
            JPanel {

   private JTextField     textField;
   private JButton        button;
   private final String[] m_sExtensions;
   private boolean        m_bFolder   = false;
   private boolean        m_bOpen     = false;
   private final String   m_sDescription;
   private String         m_sSelected = null;


   /**
    * Creates a new file selection panel
    * 
    * @param bFolder
    *                true if the selection must be a folder instead of a file
    * @param bOpen
    *                true to show a open file dialog. False to show a save file one
    * @param sExts
    *                a list of permitted file extensions
    * @param sDescription
    *                a description of the panel to show in the file dialog
    */
   public FileSelectionPanel(final boolean bFolder,
                             final boolean bOpen,
                             final String[] sExts,
                             final String sDescription) {

      super();

      m_bFolder = bFolder;
      m_bOpen = bOpen;
      m_sExtensions = sExts;
      m_sDescription = sDescription;

      initGUI();

   }


   /**
    * Creates a new file selection panel
    * 
    * @param bFolder
    *                true if the selection must be a folder instead of a file
    * @param bOpen
    *                true to show a open file dialog. False to show a save file one
    * @param sExt
    *                a file extension
    * @param sDescription
    *                a description of the panel to show in the file dialog
    */
   public FileSelectionPanel(final boolean bFolder,
                             final boolean bOpen,
                             final String sExt,
                             final String sDescription) {

      this(bFolder, bOpen, new String[] { sExt }, sDescription);

   }


   /**
    * Creates a new file selection panel
    * 
    * @param bFolder
    *                true if the selection must be a folder instead of a file
    * @param bOpen
    *                true to show a open file dialog. False to show a save file one
    * @param sExt
    *                the permitted file extension
    * @param sDescription
    *                a description of the panel to show in the file dialog
    * @param sSelection
    *                the name of the default selected file or folder
    */
   public FileSelectionPanel(final boolean bFolder,
                             final boolean bOpen,
                             final String sExt,
                             final String sDescription,
                             final String sSelection) {

      this(bFolder, bOpen, new String[] { sExt }, sDescription);
      m_sSelected = sSelection;

   }


   /**
    * Creates a new file selection panel
    * 
    * @param bFolder
    *                true if the selection must be a folder instead of a file
    * @param bOpen
    *                true to show a open file dialog. False to show a save file one
    * @param sExt
    *                a file extensions
    * @param sDescription
    *                a description of the panel to show in the file dialog
    * @param sSelection
    *                the name of the default selected file or folder
    */
   public FileSelectionPanel(final boolean bFolder,
                             final boolean bOpen,
                             final String sExt[],
                             final String sDescription,
                             final String sSelection) {

      this(bFolder, bOpen, sExt, sDescription);
      m_sSelected = sSelection;

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
    * Returns the filepath currently shown in the text field
    * 
    * @return the filepath currently shown in the text field
    */
   public String getFilepath() {

      return textField.getText();

   }


   private void btnActionPerformed() {

      int returnVal;
      final JFileChooser fc = new JFileChooser();

      if (m_bFolder) {
         fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      }

      if (m_sSelected != null) {
         fc.setSelectedFile(new File(m_sSelected));
      }

      if ((m_sExtensions != null) && !m_bFolder) {
         fc.setFileFilter(new GenericFileFilter(m_sExtensions, m_sDescription));
      }

      if (m_bOpen) {
         returnVal = fc.showOpenDialog(this.getParent().getParent());
      }
      else {
         returnVal = fc.showSaveDialog(this.getParent().getParent());
      }

      if (returnVal == JFileChooser.APPROVE_OPTION) {
         textField.setText(fc.getSelectedFile().getAbsolutePath());
      }

   }


   /**
    * Sets the current filepath to be shown in the text field
    * 
    * @param sFilepath
    *                the new filepath to set
    */
   public void setFilepath(final String sFilepath) {

      textField.setText(sFilepath);

   }


   @Override
   public void setToolTipText(final String sText) {

      textField.setToolTipText(sText);

   }
}
