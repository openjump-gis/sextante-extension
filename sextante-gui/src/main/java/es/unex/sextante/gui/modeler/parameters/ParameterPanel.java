package es.unex.sextante.gui.modeler.parameters;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.modeler.ModelerPanel;
import es.unex.sextante.parameters.Parameter;

public abstract class ParameterPanel
         extends
            JDialog {

   protected JPanel       jPanelName;
   protected JButton      jButtonOk;
   protected JButton      jButtonCancel;
   protected JLabel       jLabelDescription;
   protected JPanel       jPanelMiddle;
   protected JPanel       jPanelButtons;
   protected JTextField   jTextFieldDescription;
   protected Parameter    m_Parameter;
   protected ModelerPanel m_ModelerPanel;


   public abstract String getParameterDescription();


   protected abstract boolean prepareParameter();


   public abstract boolean parameterCanBeAdded();


   public ParameterPanel(final JDialog dialog,
                         final ModelerPanel modelerPanel) {

      super(dialog, "", true);
      this.setLocationRelativeTo(null);

      m_ModelerPanel = modelerPanel;

      initGUI();

   }


   public ParameterPanel(final ModelerPanel modelerPanel) {

      super(SextanteGUI.getMainFrame(), "", true);
      this.setLocationRelativeTo(null);

      m_ModelerPanel = modelerPanel;

      initGUI();

   }


   protected void initGUI() {

      this.setSize(new java.awt.Dimension(390, 300));
      this.setPreferredSize(new java.awt.Dimension(390, 300));
      {
         final TableLayout thisLayout = new TableLayout(new double[][] { { 5.0, TableLayoutConstants.FILL, 5.0 },
                  { 5.0, 45.0, TableLayoutConstants.FILL, 29.0, 5.0 } });
         thisLayout.setHGap(5);
         thisLayout.setVGap(5);
         this.setLayout(thisLayout);
         {
            jPanelName = new JPanel();
            final TableLayout jPanelNameLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL, 5.0, TableLayoutConstants.FILL },
                     { TableLayoutConstants.FILL, 20.0, TableLayoutConstants.FILL } });
            jPanelNameLayout.setHGap(5);
            jPanelNameLayout.setVGap(5);
            jPanelName.setLayout(jPanelNameLayout);
            this.add(jPanelName, "1, 1");
            {
               jTextFieldDescription = new JTextField();
               jTextFieldDescription.setText(getDefaultName());
               jPanelName.add(jTextFieldDescription, "2, 1");
            }
            {
               jLabelDescription = new JLabel();
               jPanelName.add(jLabelDescription, "0, 1");
               jLabelDescription.setText(Sextante.getText("Description"));
            }
         }
         {
            jPanelButtons = new JPanel();
            final FlowLayout jPanelButtonsLayout = new FlowLayout();
            jPanelButtonsLayout.setAlignment(FlowLayout.RIGHT);
            jPanelButtons.setLayout(jPanelButtonsLayout);
            this.add(jPanelButtons, "1, 3");
            {
               jButtonOk = new JButton();
               jPanelButtons.add(jButtonOk);
               jButtonOk.setText(Sextante.getText("OK"));
               jButtonOk.addActionListener(new ActionListener() {
                  public void actionPerformed(final ActionEvent evt) {
                     if (prepareParameter()) {
                        cancel();
                     }
                  }
               });
            }
            {
               jButtonCancel = new JButton();
               jPanelButtons.add(jButtonCancel);
               jButtonCancel.setText(Sextante.getText("Cancel"));
               jButtonCancel.addActionListener(new ActionListener() {
                  public void actionPerformed(final ActionEvent evt) {
                     m_Parameter = null;
                     cancel();
                  }
               });
            }
         }
         {
            jPanelMiddle = new JPanel();
            this.add(jPanelMiddle, "1, 2");
         }
      }

   }


   protected void cancel() {

      dispose();
      setVisible(false);

   }


   /**
    * Returns the parameter created using this panel
    *
    * @return the paramter created using this panel
    */
   public Parameter getParameter() {

      return m_Parameter;

   }


   @Override
   public String toString() {

      return getParameterDescription();

   }


   private String getDefaultName() {

      boolean bNameFound;
      String sName;
      int i = 1;
      final ParametersSet ps = m_ModelerPanel.getAlgorithm().getParameters();
      final int iCount = ps.getNumberOfParameters();
      Parameter param;

      do {
         bNameFound = true;
         sName = getParameterDescription() + Integer.toString(i);
         for (int j = 0; j < iCount; j++) {
            param = ps.getParameter(j);
            if (param.getParameterDescription().equals(sName)) {
               bNameFound = false;
               break;
            }
         }
         i++;
      }
      while (!bNameFound);

      return sName;

   }


   /**
    * Fills the fields in the panel with default values
    */
   public void updateOptions() {

      jTextFieldDescription.setText(getDefaultName());

   };


   /**
    * Fills the fields in the panel with the characteristics of an already created parameter
    *
    * @param param
    *                a parameter
    */
   public void setParameter(final Parameter param) {

      jTextFieldDescription.setText(param.getParameterDescription());

   }


}
