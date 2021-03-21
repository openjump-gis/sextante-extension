package es.unex.sextante.gui.cmd;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.algorithm.GenericFileFilter;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.settings.SextanteScriptsSettings;

public class ScriptEditingPanel
         extends
            JPanel {

   private String      m_sFilename;
   private JButton     jButtonOpen;
   private JTextArea   jScriptTextArea;
   private JScrollPane jScrollPaneTextArea;
   private JButton     jButtonSave;


   public ScriptEditingPanel() {

      super();

      initGUI();

   }


   public ScriptEditingPanel(final String sScriptFilename) {

      super();

      m_sFilename = sScriptFilename;

      initGUI();

      openFromFile(new File(sScriptFilename));

   }


   private void initGUI() {

      final TableLayout layout = new TableLayout(new double[][] { { TableLayoutConstants.FILL, 70.0, 5.0, 70.0, 5 },
               { TableLayoutConstants.FILL, 5.0, 25.0, 5.0 } });
      layout.setHGap(5);
      layout.setVGap(5);

      this.setLayout(layout);

      this.setPreferredSize(new Dimension(600, 500));
      this.setSize(new Dimension(600, 500));
      jScriptTextArea = new JTextArea();
      jScriptTextArea.setEditable(true);
      jScriptTextArea.setLineWrap(false);
      jScrollPaneTextArea = new JScrollPane(jScriptTextArea, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      this.add(jScrollPaneTextArea, "0,0,4,0");
      jScriptTextArea.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

      jButtonOpen = new JButton(Sextante.getText("Open"));
      this.add(jButtonOpen, "1, 2");
      jButtonOpen.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent e) {
            open();
         }
      });

      jButtonSave = new JButton(Sextante.getText("Save"));
      this.add(jButtonSave, "3, 2");
      jButtonSave.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent e) {
            save();
         }
      });


   }


   protected void save() {

      final JFileChooser fc = new JFileChooser();
      final GenericFileFilter filter = new GenericFileFilter("bsh", "SEXTANTE BeanShell Script");
      fc.setFileFilter(filter);
      if (m_sFilename != null) {
         fc.setSelectedFile(new File(m_sFilename));
      }
      else {
         final String sFolder = SextanteGUI.getSettingParameterValue(SextanteScriptsSettings.SCRIPTS_FOLDER);
         fc.setCurrentDirectory(new File(sFolder));
      }
      final int returnVal = fc.showSaveDialog(this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
         final File file = fc.getSelectedFile();
         m_sFilename = file.getAbsolutePath();
         saveToFile();
         SextanteGUI.updateAlgorithmProvider(ScriptAlgorithmProvider.class);
         SextanteGUI.getGUIFactory().updateToolbox();
      }

   }


   private void saveToFile() {

      Writer output = null;
      try {
         output = new BufferedWriter(new FileWriter(m_sFilename));
         output.write(jScriptTextArea.getText());

      }
      catch (final IOException e) {
         Sextante.addErrorToLog(e);
      }
      finally {
         if (output != null) {
            try {
               output.close();
            }
            catch (final IOException e) {
               Sextante.addErrorToLog(e);
            }
         }
      }


   }


   protected void open() {

      final JFileChooser fc = new JFileChooser();

      final String sFolder = SextanteGUI.getSettingParameterValue(SextanteScriptsSettings.SCRIPTS_FOLDER);
      fc.setFileFilter(new GenericFileFilter("bsh", "SEXTANTE BeanShell Script"));
      fc.setCurrentDirectory(new File(sFolder));
      final int returnVal = fc.showOpenDialog(this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
         final File file = fc.getSelectedFile();
         openFromFile(file);

      }


   }


   private void openFromFile(final File file) {


      BufferedReader input = null;
      try {
         input = new BufferedReader(new FileReader(file));
         final StringBuffer sText = new StringBuffer();
         String sLine = null;
         while ((sLine = input.readLine()) != null) {
            sText.append(sLine + "\n");
         }
         jScriptTextArea.setText(sText.toString());
      }
      catch (final FileNotFoundException e) {}
      catch (final IOException e) {}
      finally {
         try {
            if (input != null) {
               input.close();
            }
         }
         catch (final IOException e) {
            Sextante.addErrorToLog(e);
         }
      }


   }

}
