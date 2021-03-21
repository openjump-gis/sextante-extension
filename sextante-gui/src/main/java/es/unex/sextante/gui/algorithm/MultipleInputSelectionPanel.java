//package es.unex.sextante.gui.algorithm;
//
//import info.clearthought.layout.TableLayout;
//import info.clearthought.layout.TableLayoutConstants;
//
//import java.awt.Frame;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.util.ArrayList;
//
//import javax.swing.JButton;
//import javax.swing.JPanel;
//import javax.swing.JTextField;
//
//import es.unex.sextante.core.Sextante;
//
///**
// * A panel with a text field and a button. The text field displays a text representation of a multiple selection. Clicking the
// * button shows a dialog to perform a multiple selection
// *
// * @author volaya
// *
// */
//public class MultipleInputSelectionPanel
//         extends
//            JPanel {
//
//   private final Object[]   m_Values;
//   private ArrayList  m_SelectedIndices = new ArrayList();
//   private JTextField textField;
//   private JButton    button;
//
//
//   /**
//    * Creates a new panel
//    *
//    * @param values
//    *                an array of values to select from
//    */
//   public MultipleInputSelectionPanel(final Object[] values) {
//
//      super();
//
//      m_Values = values;
//
//      initGUI();
//
//   }
//
//
//   private void initGUI() {
//
//      button = new JButton("...");
//      textField = new JTextField(Sextante.getText("0_elements_selected"));
//      textField.setEditable(false);
//
//      button.addActionListener(new ActionListener() {
//         public void actionPerformed(final ActionEvent evt) {
//            btnActionPerformed(evt);
//         }
//      });
//
//      final TableLayout thisLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL, 25.0 }, { TableLayoutConstants.FILL } });
//      this.setLayout(thisLayout);
//      this.add(textField, "0,  0");
//      this.add(button, "1,  0");
//   }
//
//
//   /**
//    * Returns a list of all the objects selected using this panel
//    *
//    * @return a list of selected objects
//    */
//   public ArrayList getSelectedObjects() {
//
//      int iIndex;
//      final ArrayList selected = new ArrayList();
//
//      for (int i = 0; i < m_SelectedIndices.size(); i++) {
//         iIndex = ((Integer) m_SelectedIndices.get(i)).intValue();
//         selected.add(this.m_Values[iIndex]);
//      }
//
//      return selected;
//
//   }
//
//
//   private void btnActionPerformed(final ActionEvent e) {
//
//      int iCount;
//      final StringBuffer sText = new StringBuffer();
//
//      final Frame window = new Frame();
//
//      final MultipleInputSelectionDialog dialog = new MultipleInputSelectionDialog(window, m_Values, m_SelectedIndices);
//
//      dialog.pack();
//      dialog.setVisible(true);
//
//      iCount = m_SelectedIndices.size();
//      sText.append(Integer.toString(iCount));
//      if (iCount == 1) {
//         sText.append(" " + Sextante.getText("element_selected"));
//      }
//      else {
//         sText.append(" " + Sextante.getText("elements_selected"));
//      }
//
//      textField.setText(sText.toString());
//
//   }
//
//
//   public Object[] getValues() {
//
//      return m_Values;
//
//   }
//
//
//   public void setSelectedIndices(final ArrayList selectedIndices) {
//
//      m_SelectedIndices = selectedIndices;
//      final StringBuffer sText = new StringBuffer();
//      final int iCount = m_SelectedIndices.size();
//      sText.append(Integer.toString(iCount));
//      if (iCount == 1) {
//         sText.append(" " + Sextante.getText("element_selected"));
//      }
//      else {
//         sText.append(" " + Sextante.getText("elements_selected"));
//      }
//
//      textField.setText(sText.toString());
//
//   }
//
//}
