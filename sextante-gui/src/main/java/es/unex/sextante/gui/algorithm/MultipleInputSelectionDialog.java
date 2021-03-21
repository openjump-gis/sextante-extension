package es.unex.sextante.gui.algorithm;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import es.unex.sextante.core.Sextante;

/**
 * A dialog for selecting multiples elements (raster layers, vector layers or tables)
 *
 * @author volaya
 *
 */
public class MultipleInputSelectionDialog
         extends
            JDialog {

   private final ArrayList    m_SelectedIndices;
   private final ArrayList    m_SelectedIndicesOrg;

   private JPanel       jPanelMain;
   private CheckBoxList jList;
   private JButton      jButtonCancel;
   private JButton      jButtonOK;
   private JScrollPane  jScrollPane;
   private JButton      jButtonSelectAll;
   private JButton      jButtonDeselectAll;


   /**
    * Creates a new dialog
    *
    * @param window
    *                the parent window
    * @param values
    *                an array of possible values
    * @param selectedIndices
    *                a list the indices of the values to show as selected when opening this dialog. If the user selects ok, the
    *                new selection will be stored in this arraylist, clearing the previous selection. If not, it will be left
    *                unchanged
    */
   public MultipleInputSelectionDialog(final Frame window,
                                       final Object[] values,
                                       final ArrayList selectedIndices) {

      super(window, "", true);

      this.setResizable(false);

      m_SelectedIndicesOrg = selectedIndices;
      m_SelectedIndices = (ArrayList) selectedIndices.clone();
      initGUI(values);

   }


   private void initGUI(final Object[] values) {

      int i;
      int iIndex;
      final JCheckBox[] checkBox = new JCheckBox[values.length];

      try {
         this.setTitle(Sextante.getText("Multiple_selection"));
         for (i = 0; i < values.length; i++) {
            checkBox[i] = new JCheckBox(values[i].toString());
         }
         {
            jPanelMain = new JPanel();
            final TableLayout jPanelMainLayout = new TableLayout(new double[][] { { 5, TableLayoutConstants.FILL, 5, TableLayoutConstants.FILL, 5.0 },
                     { 5, TableLayoutConstants.FILL, 5, 25, 5, 25, 5 } });
            jPanelMain.setLayout(jPanelMainLayout);
            getContentPane().add(jPanelMain, BorderLayout.CENTER);
            jPanelMain.setPreferredSize(new java.awt.Dimension(357, 335));
            {

               jList = new CheckBoxList();
               jList.setListData(checkBox);
               jList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
               jScrollPane = new JScrollPane();
               jScrollPane.setViewportView(jList);
               jPanelMain.add(jScrollPane, "1,1,3,1");
            }
            {
               jButtonOK = new JButton();
               jPanelMain.add(jButtonOK, "1,5");
               jButtonOK.setText(Sextante.getText("OK"));
               jButtonOK.addActionListener(new ActionListener() {
                  public void actionPerformed(final ActionEvent evt) {
                     btnOKActionPerformed(evt);
                  }
               });
            }
            {
               jButtonCancel = new JButton();
               jPanelMain.add(jButtonCancel, "3,5");
               jButtonCancel.setText(Sextante.getText("Cancel"));
               jButtonCancel.addActionListener(new ActionListener() {
                  public void actionPerformed(final ActionEvent evt) {
                     btnCancelActionPerformed(evt);
                  }
               });
            }
            {
               jButtonSelectAll = new JButton();
               jPanelMain.add(jButtonSelectAll, "1,3");
               jButtonSelectAll.setText(Sextante.getText("Select_all"));
               jButtonSelectAll.addActionListener(new ActionListener() {
                  public void actionPerformed(final ActionEvent evt) {
                     selectAll(true);
                  }
               });
            }
            {
               jButtonDeselectAll = new JButton();
               jPanelMain.add(jButtonDeselectAll, "3,3");
               jButtonDeselectAll.setText(Sextante.getText("Deselect_all"));
               jButtonDeselectAll.addActionListener(new ActionListener() {
                  public void actionPerformed(final ActionEvent evt) {
                     selectAll(false);
                  }
               });
            }
         }
         for (i = 0; i < m_SelectedIndices.size(); i++) {
            iIndex = ((Integer) m_SelectedIndices.get(i)).intValue();
            checkBox[iIndex].setSelected(true);
         }
         {
            this.setSize(365, 369);
         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }
   }


   protected void selectAll(final boolean bSelect) {

      int i;

      for (i = 0; i < jList.getModel().getSize(); i++) {
         final JCheckBox checkBox = (JCheckBox) jList.getModel().getElementAt(i);
         checkBox.setSelected(bSelect);
      }
      jList.updateUI();

   }


   private void btnOKActionPerformed(final ActionEvent evt) {

      int i;
      m_SelectedIndicesOrg.clear();

      for (i = 0; i < jList.getModel().getSize(); i++) {
         final JCheckBox checkBox = (JCheckBox) jList.getModel().getElementAt(i);
         if (checkBox.isSelected()) {
            m_SelectedIndicesOrg.add(new Integer(i));
         }
      }

      dispose();
      setVisible(false);

   }


   private void btnCancelActionPerformed(final ActionEvent evt) {

      dispose();
      setVisible(false);


   }
}
