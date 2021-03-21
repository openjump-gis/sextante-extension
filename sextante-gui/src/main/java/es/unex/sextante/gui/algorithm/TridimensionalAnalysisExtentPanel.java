package es.unex.sextante.gui.algorithm;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.ILayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.WrongAnalysisExtentException;
import es.unex.sextante.gui.exceptions.TooLargeGridExtentException;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

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
	public void assignExtent() throws TooLargeGridExtentException, WrongAnalysisExtentException {

		m_Algorithm.setAnalysisExtent((AnalysisExtent) null);

		if (getJRadioButtonAdjustToInputDataExtent().isSelected()) {
			if (!m_Algorithm.adjustOutputExtent()) {
				throw new WrongAnalysisExtentException(Sextante.getText("Wrong_or_missing_region"));
			}
			else {
				return;
			}

		}

		final AnalysisExtent outputExtent = new AnalysisExtent();
		try {
			outputExtent.setCellSize(Double.parseDouble(jTextFieldCellSize.getText()));
			outputExtent.setCellSizeZ(Double.parseDouble(jTextFieldCellSizeZ.getText()));
			outputExtent.setXRange(Double.parseDouble(jTextFieldXMin.getText()), Double.parseDouble(jTextFieldXMax.getText()), true);
			outputExtent.setYRange(Double.parseDouble(jTextFieldYMin.getText()), Double.parseDouble(jTextFieldYMax.getText()), true);
			outputExtent.setZRange(Double.parseDouble(jTextFieldZMin.getText()), Double.parseDouble(jTextFieldZMax.getText()), true);
			m_Algorithm.setAnalysisExtent(outputExtent);
		}
		catch (final NumberFormatException e) {
			throw new WrongAnalysisExtentException(Sextante.getText("Wrong_or_missing_raster_extent_definition"));
		}

		final int numCells = outputExtent.getNX() * outputExtent.getNY();
		if ((numCells > BIG_SIZE) || (Integer.MAX_VALUE / outputExtent.getNY() < outputExtent.getNX())) {
			throw new TooLargeGridExtentException(Sextante.getText("The_selected_grid_extent_seems_too_large") + "("
					+ Integer.toString(outputExtent.getNX()) + " X "
					+ Integer.toString(outputExtent.getNY()) + ")\n"
					+ Sextante.getText("Are_you_sure_you_want_to_use_it?"));
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


	@Override
	protected void setTextFieldsEnabled(final boolean bEnabled) {

		getJPanelAnalysisExtentValues().setVisible(true);
		getJTextFieldXMin().setEnabled(bEnabled);
		getJTextFieldXMax().setEnabled(bEnabled);
		getJTextFieldYMin().setEnabled(bEnabled);
		getJTextFieldYMax().setEnabled(bEnabled);
		//getJTextFieldZMin().setEnabled(bEnabled);
		//getJTextFieldZMax().setEnabled(bEnabled);

	}


	@Override
	protected JComboBox getJComboBoxLayers() {

		if (jComboBoxLayers == null) {
			final ComboBoxModel jComboBoxLayersModel = new DefaultComboBoxModel(m_Layers);
			jComboBoxLayers = new JComboBox();
			jComboBoxLayers.setModel(jComboBoxLayersModel);

			jComboBoxLayers.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent evt) {
					double dCoord;
					if (getJRadioButtonExtentFromLayer().isSelected()) {
						try {
							final ILayer layer = (ILayer) jComboBoxLayers.getSelectedItem();
							dCoord = layer.getFullExtent().getMinX();
							getJTextFieldXMin().setText(new Double(dCoord).toString());
							dCoord = layer.getFullExtent().getMaxX();
							getJTextFieldXMax().setText(new Double(dCoord).toString());
							dCoord = layer.getFullExtent().getMinY();
							getJTextFieldYMin().setText(new Double(dCoord).toString());
							dCoord = layer.getFullExtent().getMaxY();
							getJTextFieldYMax().setText(new Double(dCoord).toString());
							if (layer instanceof IRasterLayer) {
								getJTextFieldCellSize().setText(new Double(((IRasterLayer) layer).getLayerCellSize()).toString());
							}
							else if (layer instanceof I3DRasterLayer) {
								final I3DRasterLayer raster3d = (I3DRasterLayer) layer;
								getJTextFieldCellSizeZ().setText(new Double(raster3d.getCellSizeZ()).toString());
								dCoord = raster3d.getLayerExtent().getZMin();
								getJTextFieldZMin().setText(new Double(dCoord).toString());
								dCoord = raster3d.getLayerExtent().getZMax();
								getJTextFieldZMax().setText(new Double(dCoord).toString());
								extentZHasChanged();
							}
							extentHasChanged();
						}
						catch (final Exception e) {}
					}
				}
			});
		}
		return jComboBoxLayers;
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


	@Override
	public void setExtent(final AnalysisExtent ge) {

		getJRadioButtonUserDefinedExtent().setSelected(true);
		setTextFieldsEnabled(true);

		getJTextFieldCellSize().setText(Double.toString(ge.getCellSize()));
		getJTextFieldXMin().setText(Double.toString(ge.getXMin()));
		getJTextFieldXMax().setText(Double.toString(ge.getXMax()));
		getJTextFieldYMin().setText(Double.toString(ge.getYMin()));
		getJTextFieldYMax().setText(Double.toString(ge.getYMax()));
		extentHasChanged();
		getJTextFieldCellSizeZ().setText(Double.toString(ge.getCellSizeZ()));
		getJTextFieldZMin().setText(Double.toString(ge.getZMin()));
		getJTextFieldZMax().setText(Double.toString(ge.getZMax()));
		extentZHasChanged();

	}

}
