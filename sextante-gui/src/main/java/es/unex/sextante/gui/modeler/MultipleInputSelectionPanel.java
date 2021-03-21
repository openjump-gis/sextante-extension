//package es.unex.sextante.gui.modeler;
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
//public class MultipleInputSelectionPanel
//         extends
//            JPanel {
//
//   private Object[]   m_Objects;
//   private ArrayList  m_SelectedObjects = new ArrayList();
//   private JTextField textField;
//   private JButton    button;
//
//
//   public MultipleInputSelectionPanel(final Object[] objects) {
//
//      super();
//
//      m_Objects = objects;
//
//      InitGUI();
//
//   }
//
//
//   private void InitGUI() {
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
//   public ArrayList getSelectedObjects() {
//
//      return m_SelectedObjects;
//
//   }
//
//
//   public void setSelectedObjects(final ArrayList selObjects) {
//
//      m_SelectedObjects = selObjects;
//      setFieldText();
//
//   }
//
//
//   public void setObjects(final Object[] objects) {
//
//      m_Objects = objects;
//
//   }
//
//
//   public void clearSelection() {
//
//      m_SelectedObjects.clear();
//      textField.setText(Sextante.getText("0_elements_selected"));
//
//   }
//
//
//   private void btnActionPerformed(final ActionEvent e) {
//
//      final Frame window = new Frame();
//
//      final MultipleInputSelectionDialog dialog = new MultipleInputSelectionDialog(window, m_Objects, m_SelectedObjects);
//
//      dialog.pack();
//      dialog.setVisible(true);
//
//      setFieldText();
//
//
//   }
//
//
//   private void setFieldText() {
//
//
//      int iCount;
//      final StringBuffer sText = new StringBuffer();
//
//      iCount = m_SelectedObjects.size();
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
