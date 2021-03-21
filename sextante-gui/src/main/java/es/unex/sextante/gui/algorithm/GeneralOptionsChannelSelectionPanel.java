package es.unex.sextante.gui.algorithm;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputVectorLayer;

public class GeneralOptionsChannelSelectionPanel
         extends
            JPanel {

   public static final String   OVERWRITE            = "OVERWRITE";
   public static final String   SAVE_TO_TEMP_FILE    = "SET_TEMP_FILE";
   public static final String   DO_NOT_CREATE_OUTPUT = "DO_NOT_CREATE_OUTPUT";

   private final ActionListener m_Listener;
   private JButton              jButtonSetTempFile;
   private JButton              jButtonSetOverwrite;
   private JButton              jButtonDoNotCreate;
   private final Output         m_Output;


   public GeneralOptionsChannelSelectionPanel(final Output output,
                                              final ActionListener listener) {

      super();

      m_Listener = listener;
      m_Output = output;

      initGUI();

   }


   private void initGUI() {

      this.setLayout(new TableLayout(new double[][] { { 50, TableLayout.FILL, 50 },
               { 50, TableLayout.MINIMUM, 50, TableLayout.MINIMUM, 50, TableLayout.MINIMUM, TableLayout.FILL } }));

      //this.setLayout(new FlowLayout());


      if (m_Output instanceof OutputVectorLayer) {
         if (((OutputVectorLayer) m_Output).canOverwrite()) {
            jButtonSetOverwrite = new JButton(Sextante.getText("Overwrite"));
            jButtonSetOverwrite.addActionListener(m_Listener);
            jButtonSetOverwrite.setActionCommand(OVERWRITE);
            this.add("1,5", jButtonSetOverwrite);
         }
      }
      jButtonSetTempFile = new JButton(Sextante.getText("Save_to_temp_file"));
      jButtonSetTempFile.addActionListener(m_Listener);
      jButtonSetTempFile.setActionCommand(SAVE_TO_TEMP_FILE);
      this.add("1,1", jButtonSetTempFile);

      jButtonDoNotCreate = new JButton(Sextante.getText("Do_not_create_output"));
      jButtonDoNotCreate.addActionListener(m_Listener);
      jButtonDoNotCreate.setActionCommand(DO_NOT_CREATE_OUTPUT);
      this.add("1,3", jButtonDoNotCreate);

   }


}
