package es.unex.sextante.gui.batch;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.WrongAnalysisExtentException;
import es.unex.sextante.gui.exceptions.TooLargeGridExtentException;

public class AnalysisExtentPanel
         extends
            javax.swing.JPanel {

   protected static final int   BIG_SIZE = 5000000;

   private JTextField           jTextFieldCols;
   private JTextField           jTextFieldRows;
   protected JTextField         jTextFieldCellSize;
   protected JTextField         jTextFieldXMax;
   protected JTextField         jTextFieldXMin;
   protected JTextField         jTextFieldYMax;
   protected JTextField         jTextFieldYMin;
   private JLabel               jLabelRowsCols;
   private JLabel               jLabelCellSize;
   private JLabel               jLabelRangeY;
   private JLabel               jLabelRangeX;
   protected JPanel             jPanelAnalysisExtentValues;
   private JPanel               jPanelAnalysisExtentOptions;
   private JRadioButton         jRadioButtonAdjustToInputDataExtent;
   private JRadioButton         jRadioButtonUserDefinedExtent;
   private ButtonGroup          jButtonGroup;

   protected final GeoAlgorithm m_Algorithm;
   protected AnalysisExtent     m_AnalysisExtent;


   public AnalysisExtentPanel(final GeoAlgorithm algorithm) {

      super();

      m_Algorithm = algorithm;

      initGUI();

   }


   private void initGUI() {

      /*final TableLayout jPanelParametersRasterLayout = new TableLayout(new double[][] { { 5.0, TableLayoutConstants.FILL, 10.0 },
               { TableLayoutConstants.FILL, 140.0, 120.0, TableLayoutConstants.FILL } });*/
      final TableLayout jPanelAnalysisExtentLayout = new TableLayout(new double[][] { { 1.0, TableLayoutConstants.FILL, 1.0 },
               { 1.0,/*TableLayoutConstants.FILL,*/TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL, /*140.0, 120.0,*/
               1.0 /*TableLayoutConstants.FILL*/} });
      jPanelAnalysisExtentLayout.setHGap(5);
      jPanelAnalysisExtentLayout.setVGap(5);
      this.setLayout(jPanelAnalysisExtentLayout);

      this.add(getJPanelAnalysisExtentOptions(), "1, 1");
      this.add(getJPanelAnalysisExtentValues(), "1, 2");
      if (m_Algorithm.canDefineOutputExtentFromInput()) {
         getJPanelAnalysisExtentValues().setVisible(false);
      }
      else {
         getJPanelAnalysisExtentValues().setVisible(true);
      }

   }


   public void adjustExtent() throws TooLargeGridExtentException, WrongAnalysisExtentException {

      m_Algorithm.setAnalysisExtent((AnalysisExtent) null);
      m_AnalysisExtent = null;
      if (!getJRadioButtonAdjustToInputDataExtent().isSelected()) {
         m_AnalysisExtent = new AnalysisExtent();
         try {
            m_AnalysisExtent.setCellSize(Double.parseDouble(jTextFieldCellSize.getText()));
            m_AnalysisExtent.setXRange(Double.parseDouble(jTextFieldXMin.getText()),
                     Double.parseDouble(jTextFieldXMax.getText()), true);
            m_AnalysisExtent.setYRange(Double.parseDouble(jTextFieldYMin.getText()),
                     Double.parseDouble(jTextFieldYMax.getText()), true);
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


   private ButtonGroup getJButtonGroup() {
      if (jButtonGroup == null) {
         jButtonGroup = new ButtonGroup();
      }
      return jButtonGroup;
   }


   protected JRadioButton getJRadioButtonUserDefinedExtent() {

      if (jRadioButtonUserDefinedExtent == null) {
         jRadioButtonUserDefinedExtent = new JRadioButton();
         jRadioButtonUserDefinedExtent.setText(Sextante.getText("User_defined"));
         jRadioButtonUserDefinedExtent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final java.awt.event.ActionEvent e) {
               setTextFieldsEnabled(true);
               extentHasChanged();
            }
         });
      }


      return jRadioButtonUserDefinedExtent;
   }


   protected JRadioButton getJRadioButtonAdjustToInputDataExtent() {

      if (jRadioButtonAdjustToInputDataExtent == null) {
         jRadioButtonAdjustToInputDataExtent = new JRadioButton();
         jRadioButtonAdjustToInputDataExtent.setText(Sextante.getText("Fit_to_input_layers"));
         jRadioButtonAdjustToInputDataExtent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(final java.awt.event.ActionEvent e) {
               getJPanelAnalysisExtentValues().setVisible(false);
            }
         });
      }


      return jRadioButtonAdjustToInputDataExtent;
   }


   private JPanel getJPanelAnalysisExtentOptions() {
      if (jPanelAnalysisExtentOptions == null) {
         jPanelAnalysisExtentOptions = new JPanel();
         final TableLayout jPanelRasterExtentOptionsLayout = new TableLayout(
                  new double[][] {
                           { 5., TableLayoutConstants.FILL, 5. },
                           { TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
                                    TableLayoutConstants.FILL } });
         jPanelRasterExtentOptionsLayout.setHGap(5);
         jPanelRasterExtentOptionsLayout.setVGap(5);
         jPanelAnalysisExtentOptions.setLayout(jPanelRasterExtentOptionsLayout);
         jPanelAnalysisExtentOptions.setPreferredSize(new java.awt.Dimension(660, 150));
         jPanelAnalysisExtentOptions.setBorder(BorderFactory.createTitledBorder(Sextante.getText("Extent_from")));
         jPanelAnalysisExtentOptions.add(getJRadioButtonUserDefinedExtent(), "1, 2");
         getJButtonGroup().add(getJRadioButtonUserDefinedExtent());
         if (m_Algorithm.canDefineOutputExtentFromInput()) {
            jPanelAnalysisExtentOptions.add(getJRadioButtonAdjustToInputDataExtent(), "1, 1");
            getJButtonGroup().add(getJRadioButtonAdjustToInputDataExtent());
            getJRadioButtonAdjustToInputDataExtent().setSelected(true);
         }
         else {
            getJRadioButtonUserDefinedExtent().setSelected(true);
         }

      }
      return jPanelAnalysisExtentOptions;
   }


   protected JPanel getJPanelAnalysisExtentValues() {

      if (jPanelAnalysisExtentValues == null) {
         jPanelAnalysisExtentValues = new JPanel();
         final TableLayout jPanelRasterExtentValuesLayout = new TableLayout(new double[][] {
                  { 255.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL },
                  { TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM,
                           TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL } });
         jPanelRasterExtentValuesLayout.setHGap(5);
         jPanelRasterExtentValuesLayout.setVGap(5);
         jPanelAnalysisExtentValues.setLayout(jPanelRasterExtentValuesLayout);
         jPanelAnalysisExtentValues.setBorder(BorderFactory.createTitledBorder(Sextante.getText("Extent__values")));
         jPanelAnalysisExtentValues.setPreferredSize(new java.awt.Dimension(660, 145));
         jPanelAnalysisExtentValues.add(getJLabelRangeX(), "0, 0");
         jPanelAnalysisExtentValues.add(getJLabelCellSize(), "0, 2");
         jPanelAnalysisExtentValues.add(getJTextFieldCellSize(), "1, 2");
         jPanelAnalysisExtentValues.add(getJTextFieldXMin(), "1, 0");
         jPanelAnalysisExtentValues.add(getJTextFieldXMax(), "2, 0");
         jPanelAnalysisExtentValues.add(getJTextFieldYMin(), "1, 1");
         jPanelAnalysisExtentValues.add(getJTextFieldYMax(), "2, 1");
         jPanelAnalysisExtentValues.add(getJLabelRangeY(), "0, 1");
         jPanelAnalysisExtentValues.add(getJLabelRowsCols(), "0, 3");
         jPanelAnalysisExtentValues.add(getJTextFieldRows(), "1, 3");
         jPanelAnalysisExtentValues.add(getJTextFieldCols(), "2, 3");
      }
      return jPanelAnalysisExtentValues;
   }


   protected JLabel getJLabelRangeX() {

      if (jLabelRangeX == null) {
         jLabelRangeX = new JLabel();
         jLabelRangeX.setText(Sextante.getText("Range_X"));
      }
      return jLabelRangeX;

   }


   protected JTextField getNewJTextField() {

      final JTextField jTextField = new JTextField();
      jTextField.addFocusListener(new FocusAdapter() {
         @Override
         public void focusLost(final FocusEvent e) {
            final JTextField textField = (JTextField) e.getSource();
            final String content = textField.getText();
            if (content.length() != 0) {
               try {
                  final double d = Double.parseDouble(content);
                  extentHasChanged();
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
            validateKeyTyping(event);
         }
      });

      return jTextField;
   }


   protected JTextField getJTextFieldXMin() {
      if (jTextFieldXMin == null) {
         jTextFieldXMin = getNewJTextField();
      }
      return jTextFieldXMin;
   }


   protected JTextField getJTextFieldXMax() {
      if (jTextFieldXMax == null) {
         jTextFieldXMax = getNewJTextField();
      }
      return jTextFieldXMax;
   }


   protected JTextField getJTextFieldYMin() {
      if (jTextFieldYMin == null) {
         jTextFieldYMin = getNewJTextField();
      }
      return jTextFieldYMin;
   }


   protected JTextField getJTextFieldYMax() {
      if (jTextFieldYMax == null) {
         jTextFieldYMax = getNewJTextField();
      }
      return jTextFieldYMax;
   }


   protected JLabel getJLabelRangeY() {
      if (jLabelRangeY == null) {
         jLabelRangeY = new JLabel();
         jLabelRangeY.setText(Sextante.getText("Range_Y"));
      }
      return jLabelRangeY;
   }


   protected JLabel getJLabelCellSize() {
      if (jLabelCellSize == null) {
         jLabelCellSize = new JLabel();
         jLabelCellSize.setText(Sextante.getText("Cell_size"));
      }
      return jLabelCellSize;
   }


   protected JTextField getJTextFieldCellSize() {
      if (jTextFieldCellSize == null) {
         jTextFieldCellSize = new JTextField();
         jTextFieldCellSize.setText("1");
         jTextFieldCellSize.addFocusListener(new FocusAdapter() {
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
                        extentHasChanged();
                     }
                  }
                  catch (final NumberFormatException nfe) {
                     getToolkit().beep();
                     textField.requestFocus();
                  }
               }

            }
         });
         jTextFieldCellSize.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(final KeyEvent event) {
               validateKeyTyping(event);
            }
         });
      }
      return jTextFieldCellSize;
   }


   protected JLabel getJLabelRowsCols() {
      if (jLabelRowsCols == null) {
         jLabelRowsCols = new JLabel();
         jLabelRowsCols.setText(Sextante.getText("Number_of_rows-cols"));
      }
      return jLabelRowsCols;
   }


   protected JTextField getJTextFieldRows() {
      if (jTextFieldRows == null) {
         jTextFieldRows = new JTextField();
         jTextFieldRows.setEnabled(false);
      }
      return jTextFieldRows;
   }


   protected JTextField getJTextFieldCols() {
      if (jTextFieldCols == null) {
         jTextFieldCols = new JTextField();
         jTextFieldCols.setEnabled(false);
      }
      return jTextFieldCols;
   }


   protected void validateKeyTyping(final KeyEvent event) {
      String text = ((JTextField) event.getSource()).getText();
      switch (event.getKeyChar()) {
         case KeyEvent.VK_ENTER:
            extentHasChanged();
            break;
         default:
            text += event.getKeyChar();
            break;
      }
   }


   protected void setTextFieldsEnabled(final boolean bEnabled) {

      getJPanelAnalysisExtentValues().setVisible(true);
      getJTextFieldXMin().setEnabled(bEnabled);
      getJTextFieldXMax().setEnabled(bEnabled);
      getJTextFieldYMin().setEnabled(bEnabled);
      getJTextFieldYMax().setEnabled(bEnabled);

   }


   protected void extentHasChanged() {

      double dRangeX;
      double dRangeY;
      double dCellSize;
      int iRows;
      int iCols;

      try {
         dRangeX = Math.abs(Double.parseDouble(getJTextFieldXMax().getText()) - Double.parseDouble(getJTextFieldXMin().getText()));
         dRangeY = Math.abs(Double.parseDouble(getJTextFieldYMax().getText()) - Double.parseDouble(getJTextFieldYMin().getText()));
         dCellSize = Double.parseDouble(getJTextFieldCellSize().getText());
         iRows = (int) Math.floor(dRangeY / dCellSize);
         iCols = (int) Math.floor(dRangeX / dCellSize);
         getJTextFieldRows().setText(Integer.toString(iRows));
         getJTextFieldCols().setText(Integer.toString(iCols));
      }
      catch (final NumberFormatException e) {
         return;
      }

   }


   public AnalysisExtent getExtent() {

      return m_AnalysisExtent;

   }

}
