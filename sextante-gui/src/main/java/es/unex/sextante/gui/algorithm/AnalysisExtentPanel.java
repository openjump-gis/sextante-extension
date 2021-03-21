

package es.unex.sextante.gui.algorithm;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.NamedExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.ILayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.WrongAnalysisExtentException;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.TooLargeGridExtentException;
import es.unex.sextante.gui.grass.GrassAlgorithm;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputRasterLayer;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;


/**
 * A panel to select a extent to be used for generating output layers from a given algorithm
 * 
 * @author volaya
 * 
 */
public class AnalysisExtentPanel
extends
javax.swing.JPanel {

	protected static final int   BIG_SIZE = 10000000;

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
	protected JComboBox          jComboBoxLayers;
	private JComboBox            jComboBoxViews;
	protected JPanel             jPanelAnalysisExtentValues;
	private JPanel               jPanelAnalysisExtentOptions;
	private JRadioButton         jRadioButtonAdjustToInputDataExtent;
	private JRadioButton         jRadioButtonViewExtent;
	private JRadioButton         jRadioButtonUserDefinedExtent;
	private JRadioButton         jRadioButtonExtentFromLayer;
	private ButtonGroup          jButtonGroup;

	protected final GeoAlgorithm m_Algorithm;
	protected final ILayer[]     m_Layers;


	/**
	 * Creates a new panel
	 * 
	 * @param algorithm
	 *                the algorithm
	 */
	public AnalysisExtentPanel(final GeoAlgorithm algorithm) {

		super();

		m_Layers = SextanteGUI.getInputFactory().getLayers();
		m_Algorithm = algorithm;

		initGUI();

	}


	private void initGUI() {

		final TableLayout jPanelParametersRasterLayout = new TableLayout(new double[][] { { 1.0, TableLayoutConstants.FILL, 1.0 },
			{ 1.0, TableLayoutConstants.MINIMUM, TableLayoutConstants.FILL, 1.0 } });
		jPanelParametersRasterLayout.setHGap(5);
		jPanelParametersRasterLayout.setVGap(5);
		this.setLayout(jPanelParametersRasterLayout);

		this.add(getJPanelAnalysisExtentOptions(), "1, 1");
		this.add(getJPanelAnalysisExtentValues(), "1, 2");
		if (m_Algorithm.canDefineOutputExtentFromInput()) {
			getJPanelAnalysisExtentValues().setVisible(false);
		}
		else {
			getJPanelAnalysisExtentValues().setVisible(true);
		}

	}


	/**
	 * Assigns the selected extent to the algorithm
	 * 
	 * @throws TooLargeGridExtentException
	 *                 if the dimensions of the extent are too large (too many cells) and the user should be queried to make sure
	 *                 that the input values are correct
	 * @throws WrongAnalysisExtentException
	 *                 if the input values are not correct
	 */
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
			outputExtent.setXRange(Double.parseDouble(jTextFieldXMin.getText()), Double.parseDouble(jTextFieldXMax.getText()), true);
			outputExtent.setYRange(Double.parseDouble(jTextFieldYMin.getText()), Double.parseDouble(jTextFieldYMax.getText()), true);
			m_Algorithm.setAnalysisExtent(outputExtent);
		}
		catch (final NumberFormatException e) {
			//Sextante.addErrorToLog(e);
			if (m_Algorithm instanceof GrassAlgorithm) {
				//GRASS vector output modules can always fall back to a default extent
				outputExtent.setXRange(0.0, 1.0, true);
				outputExtent.setYRange(0.0, 1.0, true);
				m_Algorithm.setAnalysisExtent(outputExtent);
				if (m_Algorithm.getName().contains("v.in.region")) {
					//we absolutely need a region for these commands!
					throw new WrongAnalysisExtentException(Sextante.getText("Wrong_or_missing_region"));
				}
				for (int i = 0; i < m_Algorithm.getNumberOfOutputObjects(); i++) {
					final Output out = m_Algorithm.getOutputObjects().getOutput(i);
					//Raster import modules also do not need an extent setting
					if ((out instanceof OutputRasterLayer) && !m_Algorithm.getName().contains("r.in.")) {
						//But if we have another raster output, then we need an extent!
						throw new WrongAnalysisExtentException(Sextante.getText("Wrong_or_missing_region"));
					}
				}
			}
			else {
				throw new WrongAnalysisExtentException(Sextante.getText("Wrong_or_missing_raster_extent_definition"));
			}
		}

		final int numCells = outputExtent.getNX() * outputExtent.getNY();
		if ((numCells > BIG_SIZE) || (Integer.MAX_VALUE / outputExtent.getNY() < outputExtent.getNX())) {
			throw new TooLargeGridExtentException(Sextante.getText("The_selected_grid_extent_seems_too_large") + "("
					+ Integer.toString(outputExtent.getNX()) + " X "
					+ Integer.toString(outputExtent.getNY()) + ")\n"
					+ Sextante.getText("Are_you_sure_you_want_to_use_it?"));
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
				@Override
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
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					getJPanelAnalysisExtentValues().setVisible(false);
				}
			});
		}


		return jRadioButtonAdjustToInputDataExtent;
	}


	private JRadioButton getJRadioButtonViewExtent() {
		if (jRadioButtonViewExtent == null) {
			jRadioButtonViewExtent = new JRadioButton();
			jRadioButtonViewExtent.setText(Sextante.getText("Use_extent_from_view"));
			jRadioButtonViewExtent.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent evt) {
					try {
						getJComboBoxViews().setSelectedIndex(getJComboBoxViews().getSelectedIndex());
						setTextFieldsEnabled(false);
					}
					catch (final Exception e) {
					}
				}
			});
		}
		return jRadioButtonViewExtent;
	}


	JRadioButton getJRadioButtonExtentFromLayer() {
		if (jRadioButtonExtentFromLayer == null) {
			jRadioButtonExtentFromLayer = new JRadioButton();
			jRadioButtonExtentFromLayer.setText(Sextante.getText("Use_extent_from_layer"));
			jRadioButtonExtentFromLayer.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent e) {
					getJComboBoxLayers().setSelectedIndex(getJComboBoxLayers().getSelectedIndex());
					setTextFieldsEnabled(false);
				}
			});
		}
		return jRadioButtonExtentFromLayer;
	}


	protected JPanel getJPanelAnalysisExtentOptions() {
		if (jPanelAnalysisExtentOptions == null) {
			jPanelAnalysisExtentOptions = new JPanel();
			final TableLayout jPanelRasterExtentOptionsLayout = new TableLayout(new double[][] {
				{ 253.0, TableLayoutConstants.FILL },
				{ TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM, TableLayoutConstants.MINIMUM,
					TableLayoutConstants.MINIMUM } });
			jPanelRasterExtentOptionsLayout.setHGap(5);
			jPanelRasterExtentOptionsLayout.setVGap(5);
			jPanelAnalysisExtentOptions.setLayout(jPanelRasterExtentOptionsLayout);
			jPanelAnalysisExtentOptions.setPreferredSize(new java.awt.Dimension(660, 200));
			jPanelAnalysisExtentOptions.setBorder(BorderFactory.createTitledBorder(Sextante.getText("Extent_from")));
			jPanelAnalysisExtentOptions.add(getJRadioButtonUserDefinedExtent(), "0, 1");
			jPanelAnalysisExtentOptions.add(getJRadioButtonViewExtent(), "0, 2");
			jPanelAnalysisExtentOptions.add(getJRadioButtonExtentFromLayer(), "0, 3");
			getJButtonGroup().add(getJRadioButtonUserDefinedExtent());
			getJButtonGroup().add(getJRadioButtonViewExtent());
			getJButtonGroup().add(getJRadioButtonExtentFromLayer());
			if (m_Algorithm.canDefineOutputExtentFromInput()) {
				jPanelAnalysisExtentOptions.add(getJRadioButtonAdjustToInputDataExtent(), "0, 0");
				getJButtonGroup().add(getJRadioButtonAdjustToInputDataExtent());
				getJRadioButtonAdjustToInputDataExtent().setSelected(true);
			}
			else {
				getJRadioButtonUserDefinedExtent().setSelected(true);
			}
			jPanelAnalysisExtentOptions.add(getJComboBoxViews(), "1, 2");
			jPanelAnalysisExtentOptions.add(getJComboBoxLayers(), "1, 3");

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
			jPanelAnalysisExtentValues.add(getJButtonExtentFromPoints(), "2,2");
		}
		return jPanelAnalysisExtentValues;
	}


	private JButton getJButtonExtentFromPoints() {

		final JButton button = new JButton("...");
		button.setSize(new Dimension(30, 30));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				final ExtentFromSavedPointsDialog dialog = new ExtentFromSavedPointsDialog();
				dialog.pack();
				dialog.setVisible(true);
				final double[] ret = dialog.getPoints();
				if (ret != null) {
					getJTextFieldXMin().setText(Double.toString(ret[0]));
					getJTextFieldYMin().setText(Double.toString(ret[1]));
					getJTextFieldXMax().setText(Double.toString(ret[2]));
					getJTextFieldYMax().setText(Double.toString(ret[3]));
				}
			}
		});
		return button;
	}


	protected JComboBox getJComboBoxLayers() {

		if (jComboBoxLayers == null) {

			final ComboBoxModel jComboBoxLayersModel = new DefaultComboBoxModel(m_Layers);
			jComboBoxLayers = new JComboBox();

			jComboBoxLayers.setModel(jComboBoxLayersModel);
			jComboBoxLayers.invalidate();
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
							extentHasChanged();
						}
						catch (final Exception e) {
						}
					}
				}
			});
		}
		return jComboBoxLayers;
	}


	private JComboBox getJComboBoxViews() {

		if (jComboBoxViews == null) {
			final ComboBoxModel jComboBoxViewsModel = new DefaultComboBoxModel(SextanteGUI.getInputFactory().getPredefinedExtents());
			jComboBoxViews = new JComboBox();
			jComboBoxViews.setModel(jComboBoxViewsModel);
			jComboBoxViews.addActionListener(new java.awt.event.ActionListener() {
				@Override
				public void actionPerformed(final java.awt.event.ActionEvent evt) {
					if (getJRadioButtonViewExtent().isSelected()) {
						try {
							final NamedExtent ne = (NamedExtent) jComboBoxViews.getSelectedItem();
							if (ne != null) {
								final Rectangle2D extent = ne.getExtent();
								getJTextFieldXMin().setText(new Double(extent.getMinX()).toString());
								getJTextFieldXMax().setText(new Double(extent.getMaxX()).toString());
								getJTextFieldYMin().setText(new Double(extent.getMinY()).toString());
								getJTextFieldYMax().setText(new Double(extent.getMaxY()).toString());
								setTextFieldsEnabled(false);
								extentHasChanged();
							}
						}
						catch (final Exception e) {
							Sextante.addErrorToLog(e);
						}
					}
				}
			});
		}
		return jComboBoxViews;
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


	private void validateKeyTyping(final KeyEvent event) {
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


	public void setExtent(final AnalysisExtent ge) {

		getJRadioButtonUserDefinedExtent().setSelected(true);
		setTextFieldsEnabled(true);

		getJTextFieldCellSize().setText(Double.toString(ge.getCellSize()));
		getJTextFieldXMin().setText(Double.toString(ge.getXMin()));
		getJTextFieldXMax().setText(Double.toString(ge.getXMax()));
		getJTextFieldYMin().setText(Double.toString(ge.getYMin()));
		getJTextFieldYMax().setText(Double.toString(ge.getYMax()));
		extentHasChanged();

	}


	public void setAutoExtent() {

		getJRadioButtonAdjustToInputDataExtent().setSelected(true);
		setTextFieldsEnabled(false);
		getJPanelAnalysisExtentValues().setVisible(false);


	}


}
