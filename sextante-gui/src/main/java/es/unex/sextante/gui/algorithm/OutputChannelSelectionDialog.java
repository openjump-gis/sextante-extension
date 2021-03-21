package es.unex.sextante.gui.algorithm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JTabbedPane;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputVectorLayer;

public class OutputChannelSelectionDialog
         extends
            JDialog
         implements
            ActionListener {

   public static final int                     TYPE_RASTER   = 0;
   public static final int                     TYPE_VECTOR   = 1;
   public static final int                     TYPE_TABLE    = 2;
   public static final int                     TYPE_RASTER3D = 3;

   private String                              m_sOutputChannel;
   private GeneralOptionsChannelSelectionPanel m_GeneralOptionsChannelSelectionPanel;
   private FileOutputChannelSelectionPanel     m_FileOutputChannelSelectionPanel;
   //private DatabaseOutputChannelSelectionPanel m_DatabaseOutputChannelSelectionPanel;
   private final Output                        m_Output;


   public OutputChannelSelectionDialog(final Output out) {

      super(SextanteGUI.getMainFrame(), true);

      m_Output = out;

      initGUI();

      this.pack();
      setLocationRelativeTo(null);

   }


   private void initGUI() {

      this.setResizable(false);
      this.setPreferredSize(new Dimension(500, 400));
      this.getContentPane().setLayout(new BorderLayout());

      final JTabbedPane tabbedPane = new JTabbedPane();

      m_GeneralOptionsChannelSelectionPanel = new GeneralOptionsChannelSelectionPanel(m_Output, this);
      tabbedPane.addTab(Sextante.getText("General"), m_GeneralOptionsChannelSelectionPanel);

      String[] sExt;
      String sDesc;
      int iType;
      if (m_Output instanceof OutputRasterLayer) {
         sExt = SextanteGUI.getOutputFactory().getRasterLayerOutputExtensions();
         sDesc = Sextante.getText(Sextante.getText("Raster_layers"));
         iType = TYPE_RASTER;
      }
      else if (m_Output instanceof Output3DRasterLayer) {
         sExt = SextanteGUI.getOutputFactory().get3DRasterLayerOutputExtensions();
         sDesc = Sextante.getText(Sextante.getText("3D_Raster_layers"));
         iType = TYPE_RASTER3D;
      }
      else if (m_Output instanceof OutputVectorLayer) {
         sExt = SextanteGUI.getOutputFactory().getVectorLayerOutputExtensions();
         sDesc = Sextante.getText(Sextante.getText("Vector_layer"));
         iType = TYPE_VECTOR;
      }
      else {
         sExt = SextanteGUI.getOutputFactory().getTableOutputExtensions();
         sDesc = Sextante.getText(Sextante.getText("Tables"));
         iType = TYPE_TABLE;
      }
      m_FileOutputChannelSelectionPanel = new FileOutputChannelSelectionPanel(sDesc, sExt, this);
      tabbedPane.addTab(Sextante.getText("File"), m_FileOutputChannelSelectionPanel);

      if (iType == TYPE_VECTOR) {
         //m_DatabaseOutputChannelSelectionPanel = new DatabaseOutputChannelSelectionPanel(sDesc, this);
         //tabbedPane.addTab(Sextante.getText("Database"), m_DatabaseOutputChannelSelectionPanel);
      }

      this.getContentPane().add(tabbedPane);

   }


   public String getOutputChannelString() {

      return m_sOutputChannel;

   }


   public void actionPerformed(final ActionEvent action) {

      if (action.getActionCommand().equals("CancelSelection")) {
         m_sOutputChannel = null;
         this.setVisible(false);
         this.dispose();
      }
      else if (action.getActionCommand().equals("ApproveSelection")) {
         m_sOutputChannel = m_FileOutputChannelSelectionPanel.getSelectedFile();
         this.setVisible(false);
         this.dispose();
      }
      else if (action.getActionCommand().equals(GeneralOptionsChannelSelectionPanel.OVERWRITE)) {
         m_sOutputChannel = Sextante.getText("[Overwrite]");
         this.setVisible(false);
         this.dispose();
      }
      else if (action.getActionCommand().equals(GeneralOptionsChannelSelectionPanel.SAVE_TO_TEMP_FILE)) {
         m_sOutputChannel = Sextante.getText("[Save_to_temporary_file]");
         this.setVisible(false);
         this.dispose();
      }
      else if (action.getActionCommand().equals(GeneralOptionsChannelSelectionPanel.DO_NOT_CREATE_OUTPUT)) {
         m_sOutputChannel = Sextante.getText("[Do_not_create_output]");
         this.setVisible(false);
         this.dispose();
      }

   }
}
