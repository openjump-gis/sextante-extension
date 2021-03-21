package es.unex.sextante.gui.algorithm;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jsh.shell.Utils;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.WrongOutputChannelDataException;
import es.unex.sextante.outputs.DatabaseOutputChannel;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.NullOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.outputs.OverwriteOutputChannel;
import es.unex.sextante.parameters.Parameter;

/**
 * A panel with a text field and a button, which pops-up a dialog to select an output channel
 * 
 * @author volaya
 * 
 */
public class OutputChannelSelectionPanel
         extends
            JPanel {

   private JTextField          textField;
   private JButton             button;
   private final Output        m_Output;
   private final ParametersSet m_ParametersSet;


   /**
    * Creates a new output channel selection panel
    * 
    * @param out
    *                the output represented by this panel
    * 
    */
   public OutputChannelSelectionPanel(final Output out,
                                      final ParametersSet paramSet) {

      super();

      m_Output = out;
      m_ParametersSet = paramSet;

      initGUI();

   }


   private void initGUI() {

      button = new JButton("...");

      textField = new JTextField(Sextante.getText("[Save_to_temporary_file]"));
      textField.setMaximumSize(new java.awt.Dimension(340, 18));
      button.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            showDialog();
         }
      });

      final TableLayout thisLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL, 25.0 },
               { TableLayoutConstants.FILL } });
      this.setLayout(thisLayout);
      this.add(textField, "0,  0");
      this.add(button, "1,  0");

   }


   /**
    * Returns the current output channel
    * 
    * @return the current output channel
    * @throws WrongOutputChannelDataException
    *                 if the channel could not be created
    */
   public IOutputChannel getOutputChannel() throws WrongOutputChannelDataException {

      final String s = textField.getText();
      if (s.equals(Sextante.getText("[Save_to_temporary_file]")) || s.trim().equals("")) {
         return new FileOutputChannel(Utils.constructPath(null));
      }
      if (s.equals(Sextante.getText("[Overwrite]"))) {
         try {
            final Parameter param = m_ParametersSet.getParameter(((OutputVectorLayer) m_Output).getInputLayerToOverwrite());
            final IVectorLayer layer = param.getParameterValueAsVectorLayer();
            return new OverwriteOutputChannel(layer);
         }
         catch (final Exception e) {
            throw new WrongOutputChannelDataException();
         }
      }
      if (s.equals(Sextante.getText("[Do_not_create_output]"))) {
         return new NullOutputChannel();
      }

      IOutputChannel channel;
      channel = DatabaseOutputChannel.getFromString(s);

      if (channel == null) {
         channel = new FileOutputChannel(Utils.constructPath(s));
      }

      return channel;

   }


   private void showDialog() {

      final OutputChannelSelectionDialog dialog = new OutputChannelSelectionDialog(m_Output);
      dialog.pack();
      dialog.setVisible(true);

      final String ret = dialog.getOutputChannelString();
      if (ret != null) {
         textField.setText(ret);
      }

   }


   @Override
   public void setToolTipText(final String sText) {

      textField.setToolTipText(sText);

   }


   public void setText(String sValue) {

      if (sValue.equals("#")) {
         textField.setText(Sextante.getText("[Save_to_temporary_file]"));
      }
      else if (sValue.equals("$")) {
         textField.setText(Sextante.getText("[Overwrite]"));
      }
      else if (sValue.equals("!")) {
         textField.setText(Sextante.getText("[Do_not_create_output]"));
      }
      else {
         String sNewValue = sValue.replace("\\\\", "\\");
         while (!sNewValue.equals(sValue)) {
            sValue = sNewValue;
            sNewValue = sValue.replace("\\\\", "\\");
         }
         textField.setText(sNewValue);
      }

   }
}
