package es.unex.sextante.gui.batch;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.WrongAnalysisExtentException;
import es.unex.sextante.gui.exceptions.TooLargeGridExtentException;

public class TridimensionalAnalysisExtentPanel
         extends
            AnalysisExtentPanel {

   private JTextField jTextFieldZMin;
   private JTextField jTextFieldZMax;
   private JLabel     jLabelRangeZ;
   private JLabel     jLabelCellSizeZ;
   private JLabel     jLabelColsZ;
   private JTextField jTextFieldColsZ;
   private JTextField jTextFieldCellSizeZ;


   public TridimensionalAnalysisExtentPanel(final GeoAlgorithm algorithm) {

      super(algorithm);

   }


   @Override
   public void adjustExtent() throws TooLargeGridExtentException, WrongAnalysisExtentException {

      m_Algorithm.setAnalysisExtent((AnalysisExtent) null);
      m_AnalysisExtent = null;
      if (!getJRadioButtonAdjustToInputDataExtent().isSelected()) {
         m_AnalysisExtent = new AnalysisExtent();
         try {
            m_AnalysisExtent.setCellSize(Double.parseDouble(jTextFieldCellSize.getText()));
            m_AnalysisExtent.setCellSizeZ(Double.parseDouble(jTextFieldCellSizeZ.getText()));
            m_AnalysisExtent.setXRange(Double.parseDouble(jTextFieldXMin.getText()),
                     Double.parseDouble(jTextFieldXMax.getText()), true);
            m_AnalysisExtent.setYRange(Double.parseDouble(jTextFieldYMin.getText()),
                     Double.parseDouble(jTextFieldYMax.getText()), true);
            m_AnalysisExtent.setZRange(Double.parseDouble(jTextFieldZMin.getText()),
                     Double.parseDouble(jTextFieldZMax.getText()), true);
         }
         catch (final NumberFormatException e) {
            throw new WrongAnalysisExtentException(Sextante.getText("Wrong_or_missing_region"));
         }

         if (m_AnalysisExtent.getNX() * m_AnalysisExtent.getNY() > BIG_SIZE) {
            throw new TooLargeGridExtentException(Sextante.getText("The_selected_grid_extent_seems_too_large") + "("
                                                  + Integer.toString(m_AnalysisExtent.getNX()) + " X "
                                                  + Integer.toString(m_AnalysisExtent.getNY()) + ")\n"
                                                  + Sextante.getText("Are_you_sure_you_want_to_use_it?"));
         }
      }


   }


   @Override
   protected JPanel getJPanelAnalysisExtentValues() {

      if (jPanelAnalysisExtentValues == null) {
         jPanelAnalysisExtentValues = new JPanel();
         final TableLayout jPanelRasterExtentValuesLayout = new TableLayout(new double[][] {
                  { 255.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL },
                  { TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM,
                           TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM,
                           TableLayoutConstants.MINIMUM } });
         jPanelRasterExtentValuesLayout.setHGap(5);
         jPanelRasterExtentValuesLayout.setVGap(5);
         jPanelAnalysisExtentValues.setLayout(jPanelRasterExtentValuesLayout);
         jPanelAnalysisExtentValues.setBorder(BorderFactory.createTitledBorder(Sextante.getText("Extent__values")));
         jPanelAnalysisExtentValues.setPreferredSize(new java.awt.Dimension(660, 145));
         jPanelAnalysisExtentValues.add(getJLabelRangeX(), "0, 0");
         jPanelAnalysisExtentValues.add(getJTextFieldXMin(), "1, 0");
         jPanelAnalysisExtentValues.add(getJTextFieldXMax(), "2, 0");
         jPanelAnalysisExtentValues.add(getJLabelRangeY(), "0, 1");
         jPanelAnalysisExtentValues.add(getJTextFieldYMin(), "1, 1");
         jPanelAnalysisExtentValues.add(getJTextFieldYMax(), "2, 1");
         jPanelAnalysisExtentValues.add(getJLabelCellSize(), "0, 2");
         jPanelAnalysisExtentValues.add(getJTextFieldCellSize(), "1, 2");
         jPanelAnalysisExtentValues.add(getJLabelRowsCols(), "0, 3");
         jPanelAnalysisExtentValues.add(getJTextFieldRows(), "1, 3");
         jPanelAnalysisExtentValues.add(getJTextFieldCols(), "2, 3");

         jPanelAnalysisExtentValues.add(getJLabelRangeZ(), "0, 4");
         jPanelAnalysisExtentValues.add(getJTextFieldZMin(), "1, 4");
         jPanelAnalysisExtentValues.add(getJTextFieldZMax(), "2, 4");
         jPanelAnalysisExtentValues.add(getJLabelCellSizeZ(), "0, 5");
         jPanelAnalysisExtentValues.add(getJTextFieldCellSizeZ(), "1, 5");
         jPanelAnalysisExtentValues.add(getJLabelColsZ(), "0, 6");
         jPanelAnalysisExtentValues.add(getJTextFieldColsZ(), "1, 6");
      }
      return jPanelAnalysisExtentValues;

   }


   protected JTextField getJTextFieldZMin() {
      if (jTextFieldZMin == null) {
         jTextFieldZMin = getNewJTextFieldZ();
      }
      return jTextFieldZMin;
   }


   protected JTextField getJTextFieldZMax() {
      if (jTextFieldZMax == null) {
         jTextFieldZMax = getNewJTextFieldZ();
      }
      return jTextFieldZMax;
   }


   protected JLabel getJLabelRangeZ() {
      if (jLabelRangeZ == null) {
         jLabelRangeZ = new JLabel();
         jLabelRangeZ.setText(Sextante.getText("Range_Z"));
      }
      return jLabelRangeZ;
   }


   protected JLabel getJLabelCellSizeZ() {
      if (jLabelCellSizeZ == null) {
         jLabelCellSizeZ = new JLabel();
         jLabelCellSizeZ.setText(Sextante.getText("Cell_sizeZ"));
      }
      return jLabelCellSizeZ;
   }


   protected JTextField getJTextFieldCellSizeZ() {
      if (jTextFieldCellSizeZ == null) {
         jTextFieldCellSizeZ = new JTextField();
         jTextFieldCellSizeZ.setText("1");
         jTextFieldCellSizeZ.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(final FocusEvent e) {
               final JTextField textField = (JTextField) e.getSource();
               final String content = textField.getText();
               if (content.length() != 0) {
                  try {
                     final double d = Double.parseDouble(content);
                     if (d <= 0) {
                        throw new NumberFormatException();
                     }
                     else {
                        extentZHasChanged();
                     }
                  }
                  catch (final NumberFormatException nfe) {
                     getToolkit().beep();
                     textField.requestFocus();
                  }
               }

            }
         });
         jTextFieldCellSizeZ.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(final KeyEvent event) {
               validateKeyTypingZ(event);
            }
         });
      }
      return jTextFieldCellSizeZ;

   }


   protected JLabel getJLabelColsZ() {
      if (jLabelColsZ == null) {
         jLabelColsZ = new JLabel();
         jLabelColsZ.setText(Sextante.getText("Number_of_cols_z"));
      }
      return jLabelColsZ;
   }


   protected JTextField getJTextFieldColsZ() {
      if (jTextFieldColsZ == null) {
         jTextFieldColsZ = new JTextField();
         jTextFieldColsZ.setEnabled(false);
      }
      return jTextFieldColsZ;
   }


   private JTextField getNewJTextFieldZ() {

      final JTextField jTextField = new JTextField();
      jTextField.addFocusListener(new FocusAdapter() {
         @Override
         public void focusLost(final FocusEvent e) {
            final JTextField textField = (JTextField) e.getSource();
            final String content = textField.getText();
            if (content.length() != 0) {
               try {
                  final double d = Double.parseDouble(content);
                  extentZHasChanged();
               }
               catch (final NumberFormatException nfe) {
                  getToolkit().beep();
                  textField.requestFocus();
               }
            }
         }
      });
      jTextField.addKeyListener(new KeyAdapter() {
         @Override
         public void keyTyped(final KeyEvent event) {
            validateKeyTypingZ(event);
         }
      });

      return jTextField;
   }


   private void validateKeyTypingZ(final KeyEvent event) {

      String text = ((JTextField) event.getSource()).getText();
      switch (event.getKeyChar()) {
         case KeyEvent.VK_ENTER:
            extentZHasChanged();
            break;
         default:
            text += event.getKeyChar();
            break;
      }
   }


   @Override
   protected void setTextFieldsEnabled(final boolean bEnabled) {

      getJPanelAnalysisExtentValues().setVisible(true);
      getJTextFieldXMin().setEnabled(bEnabled);
      getJTextFieldXMax().setEnabled(bEnabled);
      getJTextFieldYMin().setEnabled(bEnabled);
      getJTextFieldYMax().setEnabled(bEnabled);
      /*getJTextFieldZMin().setEnabled(bEnabled);
      getJTextFieldZMax().setEnabled(bEnabled);*/

   }


   private void extentZHasChanged() {

      double dRange;
      double dCellSize;
      int iCols;

      try {
         dRange = Math.abs(Double.parseDouble(getJTextFieldZMax().getText()) - Double.parseDouble(getJTextFieldZMin().getText()));
         dCellSize = Double.parseDouble(getJTextFieldCellSizeZ().getText());
         iCols = (int) Math.floor(dRange / dCellSize);
         getJTextFieldColsZ().setText(Integer.toString(iCols));
      }
      catch (final NumberFormatException e) {
         return;
      }

   }


}
