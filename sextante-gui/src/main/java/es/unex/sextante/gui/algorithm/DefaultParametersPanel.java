

package es.unex.sextante.gui.algorithm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.japura.gui.Anchor;
import org.japura.gui.BatchSelection;
import org.japura.gui.CheckComboBox;
import org.japura.gui.EmbeddedComponent;
import org.japura.gui.model.ListCheckModel;

import es.unex.sextante.additionalInfo.AdditionalInfoBand;
import es.unex.sextante.additionalInfo.AdditionalInfoBoolean;
import es.unex.sextante.additionalInfo.AdditionalInfoFilepath;
import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoRasterLayer;
import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.additionalInfo.AdditionalInfoString;
import es.unex.sextante.additionalInfo.AdditionalInfoTable;
import es.unex.sextante.additionalInfo.AdditionalInfoTableField;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.WrongInputException;
import es.unex.sextante.exceptions.WrongOutputChannelDataException;
import es.unex.sextante.exceptions.WrongOutputIDException;
import es.unex.sextante.exceptions.WrongParameterIDException;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.LayerCannotBeOverwrittenException;
import es.unex.sextante.gui.grass.GrassAlgorithm;
import es.unex.sextante.gui.help.HelpElement;
import es.unex.sextante.gui.help.HelpIO;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.outputs.OverwriteOutputChannel;
import es.unex.sextante.parameters.FixedTableModel;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.Parameter3DRasterLayer;
import es.unex.sextante.parameters.ParameterBand;
import es.unex.sextante.parameters.ParameterBoolean;
import es.unex.sextante.parameters.ParameterDataObject;
import es.unex.sextante.parameters.ParameterFilepath;
import es.unex.sextante.parameters.ParameterFixedTable;
import es.unex.sextante.parameters.ParameterMultipleInput;
import es.unex.sextante.parameters.ParameterNumericalValue;
import es.unex.sextante.parameters.ParameterPoint;
import es.unex.sextante.parameters.ParameterRasterLayer;
import es.unex.sextante.parameters.ParameterSelection;
import es.unex.sextante.parameters.ParameterString;
import es.unex.sextante.parameters.ParameterTable;
import es.unex.sextante.parameters.ParameterTableField;
import es.unex.sextante.parameters.ParameterVectorLayer;
import es.unex.sextante.parameters.RasterLayerAndBand;
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;


/**
 * A panel to introduce parameters for a geoalgorithm, created automatically based on the parameters needed by that geoalgorithm
 * If no additional panel is provided for an algorithm, SEXTANTE will use this panel as the default one.
 * 
 * @author volaya
 * 
 */
public class DefaultParametersPanel
extends
GeoAlgorithmParametersPanel {

	protected static int   CELL_HEIGHT                = 18;
	protected static int   COLUMN_WIDTH               = 250;

	protected int          m_iCurrentRow              = 0;
	protected JScrollPane  jScrollPanelParameters;
	protected JPanel       jPanelParameters;
	protected ArrayList    m_ParameterContainer       = new ArrayList();
	protected ArrayList    m_ComboBox                 = new ArrayList();
	protected GeoAlgorithm m_Algorithm;
	protected ArrayList    m_OutputParameterContainer = new ArrayList();
	protected HashMap      m_HelpElements;


	/**
	 * Constructor.
	 */
	public DefaultParametersPanel() {

		super();

	}


	@Override
	public void init(final GeoAlgorithm alg) {

		m_Algorithm = alg;

		try {
			final ArrayList list = HelpIO.open(SextanteGUI.getAlgorithmHelpFilename(alg, false));

			if (list != null) {
				m_HelpElements = HelpIO.createMap(list);
			}
			else {
				m_HelpElements = new HashMap();
			}
		}
		catch (final Exception e) {
			m_HelpElements = new HashMap();
		}

		initGUI();

	}


	protected void initGUI() {

		m_ComboBox = new ArrayList();

		{
			jScrollPanelParameters = new JScrollPane();
			jPanelParameters = new JPanel();

			int guiRows = 0;

			final JPanel jInputs = new JPanel();
			final JPanel jOptions = new JPanel();
			final JPanel jOutputs = new JPanel();

			final double[][] inputs_matrix = getInputsTableLayoutMatrix();
			if (inputs_matrix[1].length > 1) {
				final TableLayout jInputsLayout = new TableLayout(inputs_matrix);
				jInputsLayout.setHGap(5);
				jInputsLayout.setVGap(5);
				jInputs.setLayout(jInputsLayout);

				jInputs.setBorder(BorderFactory.createTitledBorder(Sextante.getText("Inputs")));

				jPanelParameters.add(jInputs, BorderLayout.NORTH);
				guiRows += inputs_matrix[1].length;

			}

			final double[][] options_matrix = getOptionsTableLayoutMatrix();
			if (options_matrix[1].length > 1) {
				final TableLayout jOptionsLayout = new TableLayout(options_matrix);
				jOptionsLayout.setHGap(5);
				jOptionsLayout.setVGap(5);
				jOptions.setLayout(jOptionsLayout);

				jOptions.setBorder(BorderFactory.createTitledBorder(Sextante.getText("Options")));

				jPanelParameters.add(jOptions, BorderLayout.CENTER);
				guiRows += options_matrix[1].length;
			}

			final double[][] outputs_matrix = getOutputsTableLayoutMatrix();
			if (outputs_matrix[1].length > 1) {

				final TableLayout jOutputsLayout = new TableLayout(getOutputsTableLayoutMatrix());
				jOutputsLayout.setHGap(5);
				jOutputsLayout.setVGap(5);
				jOutputs.setLayout(jOutputsLayout);

				jOutputs.setBorder(BorderFactory.createTitledBorder(Sextante.getText("Outputs")));

				jPanelParameters.add(jOutputs, BorderLayout.SOUTH);
				guiRows += outputs_matrix[1].length;
			}

			if (m_Algorithm.requiresRasterLayers() || m_Algorithm.requiresMultipleRasterLayers()
					|| m_Algorithm.requiresMultipleRasterBands()) {
				addRasterLayers(jInputs);
			}
			if (m_Algorithm.requires3DRasterLayers()) {
				add3DRasterLayers(jInputs);
			}
			if (m_Algorithm.requiresVectorLayers() || m_Algorithm.requiresMultipleVectorLayers()) {
				addVectorLayers(jInputs);
			}
			if (m_Algorithm.requiresTables() || m_Algorithm.requiresMultipleTables()) {
				addTables(jInputs);
			}
			if (m_Algorithm.requiresNonDataObjects()) {
				addNonDataObjects(jOptions);
			}
			if (m_Algorithm.generatesLayersOrTables()) {
				addOutputObjects(jOutputs);
			}

			final int gui_height = (int) (CELL_HEIGHT * guiRows * 1.5);
			jPanelParameters.setPreferredSize(new Dimension(660, gui_height));
			jScrollPanelParameters.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			jScrollPanelParameters.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			jScrollPanelParameters.setPreferredSize(new java.awt.Dimension(680, 300));
			jScrollPanelParameters.setViewportView(jPanelParameters);
			this.add(jScrollPanelParameters);

			for (int i = 0; i < m_ParameterContainer.size(); i++) {
				final ParameterContainer pc = (ParameterContainer) m_ParameterContainer.get(i);
				final HelpElement help = (HelpElement) m_HelpElements.get(pc.getParameter().getParameterName());
				if (m_Algorithm instanceof GrassAlgorithm) {
					if (pc.getParameter().getParameterTooltip() != null) {
						pc.getContainer().setToolTipText(pc.getParameter().getParameterTooltip());
					}
				}
				if (help != null) {
					pc.getContainer().setToolTipText("<html>" + help.getTextAsFormattedHTML() + "</html>");
				}
			}

		}
	}


	protected void addOutputObjects(final JPanel pane) {

		String sDescription;
		final OutputObjectsSet ooset = m_Algorithm.getOutputObjects();

		m_iCurrentRow = 0;

		for (int i = 0; i < ooset.getOutputObjectsCount(); i++) {
			final Output out = ooset.getOutput(i);
			if ((out instanceof OutputRasterLayer) || (out instanceof OutputVectorLayer) || (out instanceof OutputTable)
					|| (out instanceof Output3DRasterLayer)) {
				sDescription = out.getDescription() + "[" + out.getTypeDescription() + "]";
				addTitleLabel(pane, sDescription, m_iCurrentRow, false);
				final OutputChannelSelectionPanel panel = new OutputChannelSelectionPanel(out, m_Algorithm.getParameters());
				final HelpElement help = (HelpElement) m_HelpElements.get(out.getName());
				if (help != null) {
					panel.setToolTipText("<html>" + help.getTextAsFormattedHTML() + "</html>");
				}
				pane.add(panel, getStringTableCoords(2, m_iCurrentRow));

				m_iCurrentRow++;
				m_OutputParameterContainer.add(new OutputParameterContainer(out.getName(), panel));
			}
		}

	}


	protected void addRasterLayers(final JPanel pane) {

		int i, j;
		boolean bAddNotSetField = false;
		final ArrayList childComboBoxIndex = new ArrayList();
		Parameter parameter;
		Parameter subParameter;
		final ParametersSet parameters = m_Algorithm.getParameters();
		JComboBox comboBox;
		String sParameterName;

		addTitleLabel(pane, Sextante.getText("Raster_layers"), m_iCurrentRow, true);
		m_iCurrentRow++;

		for (i = 0; i < m_Algorithm.getNumberOfParameters(); i++) {
			parameter = parameters.getParameter(i);
			if (parameter instanceof ParameterRasterLayer) {
				//check for bands that depend on this raster layer
				sParameterName = parameter.getParameterName();
				childComboBoxIndex.clear();
				for (j = 0; j < m_Algorithm.getNumberOfParameters(); j++) {
					subParameter = parameters.getParameter(j);
					if (subParameter instanceof ParameterBand) {
						try {
							if (((AdditionalInfoBand) subParameter.getParameterAdditionalInfo()).getParentParameterName().equals(
									sParameterName)) {
								m_iCurrentRow++;
								addTitleLabel(pane, "      " + subParameter.getParameterDescription(), m_iCurrentRow, false);
								comboBox = getBandSelectionComboBox();
								childComboBoxIndex.add(new Integer(m_ComboBox.size()));
								m_ComboBox.add(comboBox);
								pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow));
								m_ParameterContainer.add(new ParameterContainer(subParameter, comboBox));
							}
						}
						catch (final NullParameterAdditionalInfoException e) {
							Sextante.addErrorToLog(e);
						}
					}
				}
				try {
					if (((AdditionalInfoRasterLayer) parameter.getParameterAdditionalInfo()).getIsMandatory()) {
						bAddNotSetField = false;
						addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow - childComboBoxIndex.size(), false);
					}
					else {
						bAddNotSetField = true;
						addTitleLabel(pane, parameter.getParameterDescription() + Sextante.getText("[optional]"),
								m_iCurrentRow - childComboBoxIndex.size(), false);
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					Sextante.addErrorToLog(e);
				}
				comboBox = getRasterLayerSelectionComboBox(childComboBoxIndex, bAddNotSetField);
				if (bAddNotSetField) {
					comboBox.setSelectedIndex(0);
				}
				pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow - childComboBoxIndex.size()));
				m_iCurrentRow++;
				m_ParameterContainer.add(new ParameterContainer(parameter, comboBox));
			}
			else if (parameter instanceof ParameterMultipleInput) {
				try {
					final AdditionalInfoMultipleInput additionalInfo = (AdditionalInfoMultipleInput) parameter.getParameterAdditionalInfo();

					if ((additionalInfo.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_RASTER)
							|| (additionalInfo.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_BAND)) {
						addMultipleInput(pane, (ParameterMultipleInput) parameter);
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					Sextante.addErrorToLog(e);
				}
			}
		}
	}


	protected void add3DRasterLayers(final JPanel pane) {

		int i;
		boolean bAddNotSetField = false;
		Parameter parameter;
		final ParametersSet parameters = m_Algorithm.getParameters();
		JComboBox comboBox;

		addTitleLabel(pane, Sextante.getText("3DRaster_layers"), m_iCurrentRow, true);
		m_iCurrentRow++;

		for (i = 0; i < m_Algorithm.getNumberOfParameters(); i++) {
			parameter = parameters.getParameter(i);
			if (parameter instanceof Parameter3DRasterLayer) {
				try {
					if (((AdditionalInfoRasterLayer) parameter.getParameterAdditionalInfo()).getIsMandatory()) {
						bAddNotSetField = false;
						addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);
					}
					else {
						bAddNotSetField = true;
						addTitleLabel(pane, parameter.getParameterDescription() + Sextante.getText("[optional]"), m_iCurrentRow, false);
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					Sextante.addErrorToLog(e);
				}
				comboBox = get3DRasterLayerSelectionComboBox(bAddNotSetField);
				if (bAddNotSetField) {
					comboBox.setSelectedIndex(0);
				}
				pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow));
				m_iCurrentRow++;
				m_ParameterContainer.add(new ParameterContainer(parameter, comboBox));
			}
		}
	}


	protected void addVectorLayers(final JPanel pane) {

		boolean bAddNotSetField = false;
		int i, j;
		int iShapeType;
		String sParameterName;
		final ArrayList childComboBoxIndex = new ArrayList();
		Parameter parameter;
		Parameter subParameter;
		final ParametersSet parameters = m_Algorithm.getParameters();
		JComboBox comboBox;

		addTitleLabel(pane, Sextante.getText("Vector_layer"), m_iCurrentRow, true);
		m_iCurrentRow++;

		for (i = 0; i < m_Algorithm.getNumberOfParameters(); i++) {
			parameter = parameters.getParameter(i);
			if (parameter instanceof ParameterVectorLayer) {
				try {
					iShapeType = ((AdditionalInfoVectorLayer) parameter.getParameterAdditionalInfo()).getShapeType();
					//check for table fields that depend on this vector layer
					sParameterName = parameter.getParameterName();
					childComboBoxIndex.clear();
					for (j = 0; j < m_Algorithm.getNumberOfParameters(); j++) {
						subParameter = parameters.getParameter(j);
						if (subParameter instanceof ParameterTableField) {
							try {
								if (((AdditionalInfoTableField) subParameter.getParameterAdditionalInfo()).getParentParameterName().equals(
										sParameterName)) {
									m_iCurrentRow++;
									addTitleLabel(pane, "      " + subParameter.getParameterDescription(), m_iCurrentRow, false);
									comboBox = getVectorLayerFieldSelectionComboBox(iShapeType);
									childComboBoxIndex.add(new Integer(m_ComboBox.size()));
									m_ComboBox.add(comboBox);
									pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow));
									m_ParameterContainer.add(new ParameterContainer(subParameter, comboBox));
								}
							}
							catch (final NullParameterAdditionalInfoException e) {
								Sextante.addErrorToLog(e);
							}
						}
					}
				}
				catch (final NullParameterAdditionalInfoException e1) {
					e1.printStackTrace();
				}
				// add vector layer
				try {
					iShapeType = ((AdditionalInfoVectorLayer) parameter.getParameterAdditionalInfo()).getShapeType();
					if (((AdditionalInfoVectorLayer) parameter.getParameterAdditionalInfo()).getIsMandatory()) {
						bAddNotSetField = false;
						addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow - childComboBoxIndex.size(), false);
					}
					else {
						bAddNotSetField = true;
						addTitleLabel(pane, parameter.getParameterDescription() + Sextante.getText("[optional]"),
								m_iCurrentRow - childComboBoxIndex.size(), false);
					}
					comboBox = getVectorLayerSelectionComboBox(iShapeType, childComboBoxIndex, bAddNotSetField);
					if (bAddNotSetField) {
						comboBox.setSelectedIndex(0);
					}
					pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow - childComboBoxIndex.size()));
					m_iCurrentRow++;
					m_ParameterContainer.add(new ParameterContainer(parameter, comboBox));
				}
				catch (final NullParameterAdditionalInfoException e) {
					Sextante.addErrorToLog(e);
				}
			}
			else if (parameter instanceof ParameterMultipleInput) {
				try {
					final AdditionalInfoMultipleInput additionalInfo = (AdditionalInfoMultipleInput) parameter.getParameterAdditionalInfo();

					if ((additionalInfo.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT)
							|| (additionalInfo.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE)
							|| (additionalInfo.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON)
							|| (additionalInfo.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY)) {
						addMultipleInput(pane, (ParameterMultipleInput) parameter);
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					Sextante.addErrorToLog(e);
				}
			}
		}
	}


	protected void addTables(final JPanel pane) {

		boolean bAddNotSetField = false;
		int i, j;
		String sParameterName;
		final ArrayList childComboBoxIndex = new ArrayList();
		Parameter parameter;
		Parameter subParameter;
		final ParametersSet parameters = m_Algorithm.getParameters();
		JComboBox comboBox;

		addTitleLabel(pane, Sextante.getText("Tables"), m_iCurrentRow, true);
		m_iCurrentRow++;

		for (i = 0; i < m_Algorithm.getNumberOfParameters(); i++) {
			parameter = parameters.getParameter(i);
			if (parameter instanceof ParameterTable) {
				//check for table fields that depend on this table
				sParameterName = parameter.getParameterName();
				childComboBoxIndex.clear();
				for (j = 0; j < m_Algorithm.getNumberOfParameters(); j++) {
					subParameter = parameters.getParameter(j);
					if (subParameter instanceof ParameterTableField) {
						try {
							if (((AdditionalInfoTableField) subParameter.getParameterAdditionalInfo()).getParentParameterName().equals(
									sParameterName)) {
								m_iCurrentRow++;
								addTitleLabel(pane, "      " + subParameter.getParameterDescription(), m_iCurrentRow, false);
								comboBox = getTableFieldSelectionComboBox();
								childComboBoxIndex.add(new Integer(m_ComboBox.size()));
								m_ComboBox.add(comboBox);
								pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow));
								m_ParameterContainer.add(new ParameterContainer(subParameter, comboBox));
							}
						}
						catch (final NullParameterAdditionalInfoException e) {
							Sextante.addErrorToLog(e);
						}
					}
				}
				// add table
				try {
					if (((AdditionalInfoTable) parameter.getParameterAdditionalInfo()).getIsMandatory()) {
						bAddNotSetField = false;
						addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow - childComboBoxIndex.size(), false);
					}
					else {
						bAddNotSetField = true;
						addTitleLabel(pane, parameter.getParameterDescription() + Sextante.getText("[optional]"),
								m_iCurrentRow - childComboBoxIndex.size(), false);
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					Sextante.addErrorToLog(e);
				}
				comboBox = getTableSelectionComboBox(childComboBoxIndex, bAddNotSetField);
				if (bAddNotSetField) {
					comboBox.setSelectedIndex(comboBox.getItemCount() - 1);
				}
				pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow - childComboBoxIndex.size()));
				m_iCurrentRow++;
				m_ParameterContainer.add(new ParameterContainer(parameter, comboBox));
			}
			else if (parameter instanceof ParameterMultipleInput) {
				try {
					final AdditionalInfoMultipleInput additionalInfo = (AdditionalInfoMultipleInput) parameter.getParameterAdditionalInfo();

					if (additionalInfo.getDataType() == AdditionalInfoMultipleInput.DATA_TYPE_TABLE) {
						addMultipleInput(pane, (ParameterMultipleInput) parameter);
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					Sextante.addErrorToLog(e);
				}
			}
		}

	}


	protected void addNonDataObjects(final JPanel pane) {

		int i;
		Parameter parameter;
		final ParametersSet parameters = m_Algorithm.getParameters();

		m_iCurrentRow = 0;

		for (i = 0; i < m_Algorithm.getNumberOfParameters(); i++) {
			parameter = parameters.getParameter(i);
			if (parameter instanceof ParameterNumericalValue) {
				addNumericalTextField(pane, (ParameterNumericalValue) parameter);
			}
			else if (parameter instanceof ParameterString) {
				addStringTextField(pane, (ParameterString) parameter);
			}
			else if (parameter instanceof ParameterSelection) {
				addSelection(pane, (ParameterSelection) parameter);
			}
			else if (parameter instanceof ParameterFixedTable) {
				addFixedTable(pane, (ParameterFixedTable) parameter);
			}
			else if (parameter instanceof ParameterPoint) {
				addPoint(pane, (ParameterPoint) parameter);
			}
			else if (parameter instanceof ParameterBoolean) {
				addCheckBox(pane, (ParameterBoolean) parameter);
			}
			else if (parameter instanceof ParameterFilepath) {
				addFilepath(pane, (ParameterFilepath) parameter);
			}
		}
	}


	protected void addMultipleInput(final JPanel pane,
			final ParameterMultipleInput parameter) {

		Object[] objs;

		try {
			final AdditionalInfoMultipleInput additionalInfo = (AdditionalInfoMultipleInput) parameter.getParameterAdditionalInfo();

			switch (additionalInfo.getDataType()) {
			case AdditionalInfoMultipleInput.DATA_TYPE_RASTER:
				objs = SextanteGUI.getInputFactory().getRasterLayers();
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY:
				objs = SextanteGUI.getInputFactory().getVectorLayers(AdditionalInfoVectorLayer.SHAPE_TYPE_ANY);
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT:
				objs = SextanteGUI.getInputFactory().getVectorLayers(AdditionalInfoVectorLayer.SHAPE_TYPE_POINT);
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE:
				objs = SextanteGUI.getInputFactory().getVectorLayers(AdditionalInfoVectorLayer.SHAPE_TYPE_POINT);
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON:
				objs = SextanteGUI.getInputFactory().getVectorLayers(AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON);
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_TABLE:
				objs = SextanteGUI.getInputFactory().getTables();
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_BAND:
				objs = SextanteGUI.getInputFactory().getBands();
				break;
			default:
				return;
			}

			String sDescription = parameter.getParameterDescription();
			if (!additionalInfo.getIsMandatory()) {
				sDescription = sDescription + Sextante.getText("[optional]");
			}

			addTitleLabel(pane, sDescription, m_iCurrentRow, false);

			final CheckComboBox ccb = new CheckComboBox();
			ccb.setTextFor(CheckComboBox.NONE, Sextante.getText("no_elements_selected"));
			ccb.setTextFor(CheckComboBox.MULTIPLE, Sextante.getText("multiple_elements_selected"));
			final ListCheckModel model = ccb.getModel();
			for (final Object obj : objs) {
				model.addElement(obj);
			}
			final BatchSelection bs = new BatchSelection.CheckBox();
			final EmbeddedComponent comp = new EmbeddedComponent(bs, Anchor.NORTH);
			ccb.setEmbeddedComponent(comp);

			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

			pane.add(ccb, getStringTableCoords(2, m_iCurrentRow));
			m_ParameterContainer.add(new ParameterContainer(parameter, ccb));
			m_iCurrentRow++;
		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
		}
	}


	protected void addFixedTable(final JPanel pane,
			final ParameterFixedTable parameter) {

		boolean bIsNumberOfRowsFixed;
		int iRows;
		String[] sCols;

		try {
			final AdditionalInfoFixedTable additionalInfo = (AdditionalInfoFixedTable) parameter.getParameterAdditionalInfo();

			iRows = additionalInfo.getRowsCount();
			sCols = additionalInfo.getCols();
			bIsNumberOfRowsFixed = additionalInfo.isNumberOfRowsFixed();

			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

			final FixedTablePanel fixedTablePanel = new FixedTablePanel(sCols, iRows, bIsNumberOfRowsFixed);
			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

			pane.add(fixedTablePanel, getStringTableCoords(2, m_iCurrentRow));
			m_ParameterContainer.add(new ParameterContainer(parameter, fixedTablePanel));
			m_iCurrentRow++;
		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
		}

	}


	protected void addFilepath(final JPanel pane,
			final ParameterFilepath parameter) {

		try {
			final AdditionalInfoFilepath additionalInfo = (AdditionalInfoFilepath) parameter.getParameterAdditionalInfo();

			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

			String sExtension = "*.*";
			final String[] sExtensions = additionalInfo.getExtensions();
			if (sExtensions != null) {
				final StringBuffer sb = new StringBuffer();
				for (int i = 0; i < sExtensions.length; i++) {
					sb.append(sExtensions[i]);
					if (i < sExtensions.length - 1) {
						sb.append(",");
					}
				}
				sExtension = sb.toString();
			}
			final FileSelectionPanel fileSelectionPanel = new FileSelectionPanel(additionalInfo.isFolder(),
					additionalInfo.isOpenDialog(), additionalInfo.getExtensions(), Sextante.getText("Files") + " " + sExtension);
			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

			pane.add(fileSelectionPanel, getStringTableCoords(2, m_iCurrentRow));
			m_ParameterContainer.add(new ParameterContainer(parameter, fileSelectionPanel));
			m_iCurrentRow++;
		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
		}

	}


	protected void addPoint(final JPanel pane,
			final ParameterPoint parameter) {

		addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

		final PointSelectionPanel pointSelectionPanel = new PointSelectionPanel();
		addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

		pane.add(pointSelectionPanel, getStringTableCoords(2, m_iCurrentRow));
		m_ParameterContainer.add(new ParameterContainer(parameter, pointSelectionPanel));
		m_iCurrentRow++;

	}


	protected void addSelection(final JPanel pane,
			final ParameterSelection parameter) {

		try {
			final JComboBox comboBox = new JComboBox();
			final String[] sValues = ((AdditionalInfoSelection) parameter.getParameterAdditionalInfo()).getValues();
			final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(sValues);
			comboBox.setModel(defaultModel);
			comboBox.setSelectedIndex(0);
			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);
			pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow));
			m_iCurrentRow++;
			m_ParameterContainer.add(new ParameterContainer(parameter, comboBox));
		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
		}

	}


	protected void addNumericalTextField(final JPanel pane,
			final ParameterNumericalValue parameter) {

		final JTextField textField = new JTextField();
		final double dMinValue, dMaxValue;
		final int iType;

		try {
			final AdditionalInfoNumericalValue additionalInfo = (AdditionalInfoNumericalValue) parameter.getParameterAdditionalInfo();
			dMinValue = additionalInfo.getMinValue();
			dMaxValue = additionalInfo.getMaxValue();
			iType = additionalInfo.getType();

			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

			if (iType == AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE) {
				textField.setText(Double.toString(additionalInfo.getDefaultValue()));
			}
			else {
				textField.setText(Integer.toString((int) additionalInfo.getDefaultValue()));
			}
			textField.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(final FocusEvent e) {
					final JTextField textField = (JTextField) e.getSource();
					final String content = textField.getText();
					if (content.length() != 0) {
						try {
							if (iType == AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE) {
								final double d = Double.parseDouble(content);
								if (d > dMaxValue) {
									textField.setText(Double.toString(dMaxValue));
								}
								if (d < dMinValue) {
									textField.setText(Double.toString(dMinValue));
								}
							}
							else {
								final int i = (int) Double.parseDouble(content);
								if (i > dMaxValue) {
									textField.setText(Integer.toString((int) dMaxValue));
								}
								if (i < dMinValue) {
									textField.setText(Integer.toString((int) dMinValue));
								}
								textField.setText(Integer.toString(i));
							}
						}
						catch (final NumberFormatException nfe) {
							getToolkit().beep();
							textField.requestFocus();
						}
					}
				}
			});

			pane.add(textField, getStringTableCoords(2, m_iCurrentRow));
			m_ParameterContainer.add(new ParameterContainer(parameter, textField));
			m_iCurrentRow++;
		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
		}

	}


	protected void addStringTextField(final JPanel pane,
			final ParameterString parameter) {

		try {
			final AdditionalInfoString ai = (AdditionalInfoString) parameter.getParameterAdditionalInfo();
			final JTextField textField = new JTextField(ai.getDefaultString());

			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

			pane.add(textField, getStringTableCoords(2, m_iCurrentRow));
			m_ParameterContainer.add(new ParameterContainer(parameter, textField));
			m_iCurrentRow++;
		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
		}

	}


	protected void addCheckBox(final JPanel pane,
			final ParameterBoolean parameter) {

		final JCheckBox checkBox = new JCheckBox();
		try {
			final AdditionalInfoBoolean ai = (AdditionalInfoBoolean) parameter.getParameterAdditionalInfo();
			checkBox.setSelected(ai.getDefaultValue());
			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);
			pane.add(checkBox, getStringTableCoords(2, m_iCurrentRow));
			m_ParameterContainer.add(new ParameterContainer(parameter, checkBox));
			m_iCurrentRow++;
		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
		}


	}


	protected void addTitleLabel(final JPanel pane,
			final String sText,
			final int iRow,
			final boolean isSectionTitleLabel) {

		JLabel label;
		label = new JLabel();
		label.setText(sText);
		label.setPreferredSize(new java.awt.Dimension(COLUMN_WIDTH, CELL_HEIGHT));

		if (isSectionTitleLabel) {
			label.setFont(new java.awt.Font("Tahoma", 1, 11));
			label.setForeground(java.awt.Color.BLUE);
		}

		pane.add(label, getStringTableCoords(1, iRow));

	}


	protected String getStringTableCoords(final int iCol,
			final int iRow) {

		final StringBuffer sCellCoords = new StringBuffer();

		sCellCoords.append(Integer.toString(iCol));
		sCellCoords.append(",");
		sCellCoords.append(Integer.toString(iRow));

		return sCellCoords.toString();

	}


	protected double[][] getInputsTableLayoutMatrix() {

		int iRows = 1;

		if (m_Algorithm.requiresRasterLayers() || m_Algorithm.requiresMultipleRasterLayers()
				|| m_Algorithm.requiresMultipleRasterBands()) {
			//Adds 1 for label
			iRows += 1;
			iRows += m_Algorithm.getNumberOfRasterLayers(true);
			final int tf_num = m_Algorithm.getNumberOfBandsParameters();
			if (tf_num > 0) {
				iRows += tf_num;
			}
		}
		if (m_Algorithm.requiresVectorLayers() || m_Algorithm.requiresMultipleVectorLayers()) {
			//Adds 1 for label
			iRows += 1;
			iRows += m_Algorithm.getNumberOfVectorLayers(true);
			final int tf_num = m_Algorithm.getNumberOfTableFieldsParameters();
			if (tf_num > 0) {
				iRows += tf_num;
			}
		}
		if (m_Algorithm.requiresTables() || m_Algorithm.requiresMultipleTables()) {
			//Adds 1 for label
			iRows += 1;
			iRows += m_Algorithm.getNumberOfTables();
			final int tf_num = m_Algorithm.getNumberOfTableFieldsParameters();
			if (tf_num > 0) {
				iRows += tf_num;
			}
		}
		if (m_Algorithm.requires3DRasterLayers()) {
			//Adds 1 for label
			iRows += 1;
			iRows += m_Algorithm.getParameters().getNumberOf3DRasterLayers();
		}

		return getTableMatrixSize(iRows);

	}


	protected double[][] getOptionsTableLayoutMatrix() {

		int iRows = 1;

		if (m_Algorithm.requiresNonDataObjects()) {
			iRows += m_Algorithm.getNumberOfNoDataParameters();
		}

		return getTableMatrixSize(iRows);

	}


	protected double[][] getOutputsTableLayoutMatrix() {

		int iRows = 0;

		final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
		final int iOutput = ooSet.getOutputDataObjectsCount();
		iRows += iOutput;

		if (m_Algorithm.generatesLayersOrTables()) {
			iRows += 1;
		}

		return getTableMatrixSize(iRows);

	}


	protected double[][] getTableMatrixSize(final int iRows) {

		final double iSizeColumns[] = { 10, TableLayoutConstants.FILL, 360, 10 };
		final double iSizeRows[] = new double[iRows];

		for (int i = 0; i < (iRows - 1); i++) {
			iSizeRows[i] = CELL_HEIGHT;
		}
		if (iRows > 0) {
			// Last row is smaller on the interface
			iSizeRows[iRows - 1] = 5;
		}

		final double iSize[][] = new double[2][];
		iSize[0] = iSizeColumns;
		iSize[1] = iSizeRows;

		return iSize;

	}


	protected JComboBox getRasterLayerSelectionComboBox(final ArrayList childComboBoxes,
			final boolean bAddNotSetField) {

		int i;

		final Integer[] childsArray = new Integer[childComboBoxes.size()];
		for (i = 0; i < childsArray.length; i++) {
			childsArray[i] = (Integer) childComboBoxes.get(i);
		}

		final IRasterLayer[] layers = SextanteGUI.getInputFactory().getRasterLayers();
		final ObjectAndDescription[] oad = new ObjectAndDescription[layers.length];
		for (i = 0; i < layers.length; i++) {
			oad[i] = new ObjectAndDescription(layers[i].getName(), layers[i]);
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
	 	final JComboBox cb = new JComboBox(oad);
		 

		if (bAddNotSetField) {
			final DefaultComboBoxModel model = (DefaultComboBoxModel) cb.getModel();
			model.insertElementAt(new ObjectAndDescription(Sextante.getText("[Not_selected]"), null), 0);
		}

		cb.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(final java.awt.event.ItemEvent e) {
				int i;
				int iIndex;
				String sNames[] = null;
				DefaultComboBoxModel defaultModel;
				for (i = 0; i < childsArray.length; i++) {
					iIndex = (childsArray[i]).intValue();
					final ObjectAndDescription ob = (ObjectAndDescription) cb.getSelectedItem();
					final IRasterLayer layer = (IRasterLayer) ob.getObject();
					if (layer != null) {
						sNames = getBandNames(layer);
						if (sNames != null) {
							defaultModel = new DefaultComboBoxModel(sNames);
							((JComboBox) m_ComboBox.get(iIndex)).setModel(defaultModel);
						}
					}
				}
			}
		});

		return cb;

	}


	protected JComboBox get3DRasterLayerSelectionComboBox(final boolean bAddNotSetField) {

		int i;

		final I3DRasterLayer[] layers = SextanteGUI.getInputFactory().get3DRasterLayers();
		final ObjectAndDescription[] oad = new ObjectAndDescription[layers.length];
		for (i = 0; i < layers.length; i++) {
			oad[i] = new ObjectAndDescription(layers[i].getName(), layers[i]);
		}

		final JComboBox cb = new JComboBox(oad);


		if (bAddNotSetField) {
			final DefaultComboBoxModel model = (DefaultComboBoxModel) cb.getModel();
			model.insertElementAt(new ObjectAndDescription(Sextante.getText("[Not_selected]"), null), 0);
		}

		return cb;

	}


	protected JComboBox getVectorLayerSelectionComboBox(final int iShapeType,
			final ArrayList childComboBoxes,
			final boolean bAddNotSetField) {

		int i;

		final Integer[] childsArray = new Integer[childComboBoxes.size()];
		for (i = 0; i < childsArray.length; i++) {
			childsArray[i] = (Integer) childComboBoxes.get(i);
		}

		final IVectorLayer[] layers = SextanteGUI.getInputFactory().getVectorLayers(iShapeType);
		final ObjectAndDescription[] oad = new ObjectAndDescription[layers.length];
		for (i = 0; i < layers.length; i++) {
			oad[i] = new ObjectAndDescription(layers[i].getName(), layers[i]);
		}
		final JComboBox cb = new JComboBox(oad);


		if (bAddNotSetField) {
			final DefaultComboBoxModel model = (DefaultComboBoxModel) cb.getModel();
			model.insertElementAt(new ObjectAndDescription(Sextante.getText("[Not_selected]"), null), 0);
		}

		cb.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(final java.awt.event.ItemEvent e) {
				int i;
				int iIndex;
				String sNames[] = null;
				DefaultComboBoxModel defaultModel;
				for (i = 0; i < childsArray.length; i++) {
					iIndex = (childsArray[i]).intValue();
					final ObjectAndDescription ob = (ObjectAndDescription) cb.getSelectedItem();
					final IVectorLayer layer = (IVectorLayer) ob.getObject();
					if (layer != null) {
						sNames = getVectorLayerFieldNames(layer);
						if (sNames != null) {
							defaultModel = new DefaultComboBoxModel(sNames);
							((JComboBox) m_ComboBox.get(iIndex)).setModel(defaultModel);
						}
					}
				}
			}
		});

		return cb;

	}


	protected JComboBox getTableSelectionComboBox(final ArrayList childComboBoxes,
			final boolean bAddNotSetField) {

		int i;
		final JComboBox comboBox = new JComboBox();

		final Integer[] childsArray = new Integer[childComboBoxes.size()];
		for (i = 0; i < childsArray.length; i++) {
			childsArray[i] = (Integer) childComboBoxes.get(i);
		}

		final ITable[] tables = SextanteGUI.getInputFactory().getTables();
		final ObjectAndDescription[] oad = new ObjectAndDescription[tables.length];
		for (i = 0; i < tables.length; i++) {
			oad[i] = new ObjectAndDescription(tables[i].getName(), tables[i]);
		}
		final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(oad);
		comboBox.setModel(defaultModel);

		if (bAddNotSetField) {
			defaultModel.insertElementAt(new ObjectAndDescription(Sextante.getText("[Not_selected]"), null), 0);
		}

		comboBox.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(final java.awt.event.ItemEvent e) {
				int i;
				int iIndex;
				String sNames[] = null;
				DefaultComboBoxModel defaultModel;
				for (i = 0; i < childsArray.length; i++) {
					iIndex = (childsArray[i]).intValue();
					final ObjectAndDescription ob = (ObjectAndDescription) comboBox.getSelectedItem();
					final ITable table = (ITable) ob.getObject();
					if (table != null) {
						sNames = getTableFieldNames(table);
						if (sNames != null) {
							defaultModel = new DefaultComboBoxModel(sNames);
							((JComboBox) m_ComboBox.get(iIndex)).setModel(defaultModel);
						}
					}
				}
			}
		});

		return comboBox;

	}


	protected JComboBox getTableFieldSelectionComboBox() {

		final ITable[] tables = SextanteGUI.getInputFactory().getTables();

		if (tables.length > 0) {
			final JComboBox comboBox = new JComboBox();

			final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(getTableFieldNames(tables[0]));
			comboBox.setModel(defaultModel);
			return comboBox;
		}
		else {
			return new JComboBox();
		}

	}


	protected JComboBox getBandSelectionComboBox() {

		final IRasterLayer[] layers = SextanteGUI.getInputFactory().getRasterLayers();

		if (layers.length > 0) {
			final JComboBox comboBox = new JComboBox();
			final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(getBandNames(layers[0]));
			comboBox.setModel(defaultModel);
			return comboBox;
		}
		else {
			return new JComboBox();
		}

	}


	protected JComboBox getVectorLayerFieldSelectionComboBox(final int iShapeType) {

		final IVectorLayer[] layers = SextanteGUI.getInputFactory().getVectorLayers(iShapeType);

		if (layers.length > 0) {
			final JComboBox comboBox = new JComboBox();
			final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(getVectorLayerFieldNames(layers[0]));
			comboBox.setModel(defaultModel);
			return comboBox;
		}
		else {
			return new JComboBox();
		}

	}


	protected String[] getTableFieldNames(final ITable table) {

		return table.getFieldNames();

	}


	protected String[] getVectorLayerFieldNames(final IVectorLayer vectorLayer) {

		return vectorLayer.getFieldNames();

	}


	protected String[] getBandNames(final IRasterLayer layer) {

		int j;
		int iBands;
		String[] bands = null;

		iBands = layer.getBandsCount();
		bands = new String[iBands];
		for (j = 0; j < iBands; j++) {
			bands[j] = Integer.toString(j + 1);
		}

		return bands;

	}


	@Override
	public void assignParameters() throws WrongInputException, LayerCannotBeOverwrittenException {

		boolean bAssigningOK = true;
		int i;
		ParameterContainer parameterContainer;
		String sType;

		for (i = 0; i < m_ParameterContainer.size(); i++) {
			parameterContainer = (ParameterContainer) m_ParameterContainer.get(i);
			sType = parameterContainer.getType();
			if (sType.equals("Table")) {
				bAssigningOK = assignInputTable(parameterContainer);
			}
			else if (sType.equals("Vector Layer") || sType.equals("Raster Layer") || sType.equals("3D Raster Layer")) {
				bAssigningOK = assignInputLayer(parameterContainer);
			}
			else if (sType.equals("Numerical Value")) {
				bAssigningOK = assignInputNumericalValue(parameterContainer);
			}
			else if (sType.equals("String")) {
				bAssigningOK = assignInputString(parameterContainer);
			}
			else if (sType.equals("Boolean")) {
				bAssigningOK = assignInputBoolean(parameterContainer);
			}
			else if (sType.equals("Fixed Table")) {
				bAssigningOK = assignInputFixedTable(parameterContainer);
			}
			else if (sType.equals("Multiple Input")) {
				bAssigningOK = assignInputMultipleInput(parameterContainer);
			}
			else if (sType.equals("Point")) {
				bAssigningOK = assignInputPoint(parameterContainer);
			}
			else if (parameterContainer.getType().equals("Filepath")) {
				bAssigningOK = assignInputFilepath(parameterContainer);
			}
			else if (sType.equals("Table Field") || sType.equals("Selection") || sType.equals("Band")) {
				bAssigningOK = assignInputSelection(parameterContainer);
			}

			if (!bAssigningOK) {
				throw new WrongInputException();
			}

		}

		final OutputObjectsSet ooset = m_Algorithm.getOutputObjects();
		for (i = 0; i < m_OutputParameterContainer.size(); i++) {
			final OutputParameterContainer opc = (OutputParameterContainer) m_OutputParameterContainer.get(i);
			Output out;

			try {
				out = ooset.getOutput(opc.getName());
			}
			catch (final WrongOutputIDException e) {
				throw new WrongInputException();
			}

			final OutputChannelSelectionPanel ocsp = (OutputChannelSelectionPanel) opc.getContainer();
			IOutputChannel channel;
			try {
				channel = ocsp.getOutputChannel();
			}
			catch (final WrongOutputChannelDataException e) {
				throw new WrongInputException();
			}

			if (channel instanceof OverwriteOutputChannel) {
				final OverwriteOutputChannel ooc = (OverwriteOutputChannel) channel;
				if (!ooc.getLayer().canBeEdited()) {
					throw new LayerCannotBeOverwrittenException();
				}
			}
			out.setOutputChannel(channel);
			//TODO Check that output channels do not match with input layers

		}


	}


	protected boolean assignInputTable(final ParameterContainer parameterContainer) {

		boolean bReturn = true;

		try {
			final JComboBox comboBox = (JComboBox) parameterContainer.getContainer();
			final Parameter parameter = m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			final ObjectAndDescription oad = (ObjectAndDescription) comboBox.getSelectedItem();
			bReturn = parameter.setParameterValue(oad.getObject());
		}
		catch (final Exception e) {
			Sextante.addErrorToLog(e);
			return false;
		}

		return bReturn;

	}


	protected boolean assignInputLayer(final ParameterContainer parameterContainer) {

		boolean bReturn = true;

		try {
			final JComboBox comboBox = (JComboBox) parameterContainer.getContainer();
			final Parameter parameter = m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			final ObjectAndDescription oad = (ObjectAndDescription) comboBox.getSelectedItem();
			bReturn = parameter.setParameterValue(oad.getObject());
		}
		catch (final Exception e) {
			Sextante.addErrorToLog(e);
			return false;
		}

		return bReturn;

	}


	protected boolean assignInputMultipleInput(final ParameterContainer parameterContainer) {

		boolean bReturn;

		try {
			//final MultipleInputSelectionPanel panel = (MultipleInputSelectionPanel) parameterContainer.getContainer();
			final CheckComboBox ccb = (CheckComboBox) parameterContainer.getContainer();
			final ParameterMultipleInput parameter = (ParameterMultipleInput) m_Algorithm.getParameters().getParameter(
					parameterContainer.getName());
			final List<Object> list = ccb.getModel().getCheckeds();
			//final ArrayList list = panel.getSelectedObjects();
			final AdditionalInfoMultipleInput ai = (AdditionalInfoMultipleInput) parameter.getParameterAdditionalInfo();
			final boolean bMandatory = ai.getIsMandatory();
			if (bMandatory && (list.size() == 0)) {
				return false;
			}
			bReturn = parameter.setParameterValue(list);
		}
		catch (final Exception e) {
			Sextante.addErrorToLog(e);
			return false;
		}

		return bReturn;

	}


	protected boolean assignInputSelection(final ParameterContainer parameterContainer) {

		int iIndex;
		boolean bReturn = true;

		try {
			final JComboBox comboBox = (JComboBox) parameterContainer.getContainer();
			final Parameter parameter = m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			iIndex = comboBox.getSelectedIndex();
			bReturn = parameter.setParameterValue(new Integer(iIndex));

		}
		catch (final WrongParameterIDException e) {
			Sextante.addErrorToLog(e);
			return false;
		}

		return bReturn;

	}


	protected boolean assignInputNumericalValue(final ParameterContainer parameterContainer) {

		boolean bReturn;

		try {
			final JTextField txtField = (JTextField) parameterContainer.getContainer();
			final Parameter parameter = m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			bReturn = parameter.setParameterValue(new Double(Double.parseDouble(txtField.getText())));
		}
		catch (final Exception e) {
			Sextante.addErrorToLog(e);
			return false;
		}

		return bReturn;

	}


	protected boolean assignInputFixedTable(final ParameterContainer parameterContainer) {

		boolean bReturn;

		try {
			final FixedTablePanel panel = (FixedTablePanel) parameterContainer.getContainer();
			final Parameter parameter = m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			bReturn = parameter.setParameterValue(panel.getTableModel());
		}
		catch (final WrongParameterIDException e) {
			Sextante.addErrorToLog(e);
			return false;
		}

		return bReturn;

	}


	protected boolean assignInputString(final ParameterContainer parameterContainer) {

		boolean bReturn;

		try {
			final JTextField txtField = (JTextField) parameterContainer.getContainer();
			final Parameter parameter = m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			bReturn = parameter.setParameterValue(txtField.getText());
		}
		catch (final WrongParameterIDException e) {
			Sextante.addErrorToLog(e);
			return false;
		}

		return bReturn;

	}


	protected boolean assignInputFilepath(final ParameterContainer parameterContainer) {

		boolean bReturn;

		try {
			final FileSelectionPanel fileSelectionPanel = (FileSelectionPanel) parameterContainer.getContainer();
			final Parameter parameter = m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			bReturn = parameter.setParameterValue(fileSelectionPanel.getFilepath());
		}
		catch (final WrongParameterIDException e) {
			Sextante.addErrorToLog(e);
			return false;
		}

		return bReturn;

	}


	protected boolean assignInputPoint(final ParameterContainer parameterContainer) {

		boolean bReturn;

		try {
			final PointSelectionPanel pointSelectionPanel = (PointSelectionPanel) parameterContainer.getContainer();
			final Parameter parameter = m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			bReturn = parameter.setParameterValue(pointSelectionPanel.getPoint());
		}
		catch (final Exception e) {
			Sextante.addErrorToLog(e);
			return false;
		}

		return bReturn;

	}


	protected boolean assignInputBoolean(final ParameterContainer parameterContainer) {

		boolean bReturn;

		try {
			final JCheckBox checkBox = (JCheckBox) parameterContainer.getContainer();
			final Parameter parameter = m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			bReturn = parameter.setParameterValue(new Boolean(checkBox.isSelected()));
		}
		catch (final WrongParameterIDException e) {
			Sextante.addErrorToLog(e);
			return false;
		}

		return bReturn;

	}


	@Override
	public void setOutputValue(final String sOutputName,
			final String sValue) {

		for (int i = 0; i < m_OutputParameterContainer.size(); i++) {
			final OutputParameterContainer container = (OutputParameterContainer) m_OutputParameterContainer.get(i);
			if (sOutputName.equals(container.getName())) {
				final OutputChannelSelectionPanel ocsp = (OutputChannelSelectionPanel) container.getContainer();
				ocsp.setText(sValue);
			}
		}

	}


	@Override
	public void setParameterValue(final String sParameterName,
			final String sValue) {

		final ParametersSet parameters = m_Algorithm.getParameters();
		Parameter param;
		try {
			param = parameters.getParameter(sParameterName);
		}
		catch (final WrongParameterIDException e1) {
			return;
		}
		Object container = null;
		for (int i = 0; i < m_ParameterContainer.size(); i++) {
			final ParameterContainer pc = (ParameterContainer) m_ParameterContainer.get(i);
			if (pc.getName().equals(sParameterName)) {
				container = pc.getContainer();
				break;
			}
		}

		if (container == null) {
			return;
		}

		try {
			if (param instanceof ParameterDataObject) {
				final JComboBox cb = (JComboBox) container;
				final ComboBoxModel model = cb.getModel();
				for (int i = 0; i < model.getSize(); i++) {
					final ObjectAndDescription oad = (ObjectAndDescription) model.getElementAt(i);
					if (oad.getDescription().equals(sValue)) {
						cb.setSelectedIndex(i);
						break;
					}
				}
			}
			else if ((param instanceof ParameterNumericalValue) || (param instanceof ParameterString)) {
				if (!sValue.equals("#")) {
					final JTextField tf = (JTextField) container;
					tf.setText(sValue);
				}
			}
			else if (param instanceof ParameterFilepath) {
				final FileSelectionPanel fsp = (FileSelectionPanel) container;
				fsp.setFilepath(sValue);
			}
			else if (param instanceof ParameterFixedTable) {
				this.setFixedTableValue(param, (FixedTablePanel) container, sValue);
			}
			else if (param instanceof ParameterBoolean) {
				if (!sValue.equals("#")) {
					final JCheckBox cb = (JCheckBox) container;
					boolean bValue;
					if (sValue.equals("true")) {
						bValue = true;
					}
					else {
						bValue = false;
					}
					cb.setSelected(bValue);

				}
			}
			else if (param instanceof ParameterSelection) {
				if (!sValue.equals("#")) {
					final int iIndex = Integer.parseInt(sValue);
					final AdditionalInfoSelection ai = (AdditionalInfoSelection) param.getParameterAdditionalInfo();
					if ((iIndex >= 0) && (iIndex < ai.getValues().length)) {
						final JComboBox cb = (JComboBox) container;
						cb.setSelectedIndex(iIndex);
					}
				}
			}
			else if (param instanceof ParameterMultipleInput) {

				final ArrayList selectedIndices = new ArrayList();
				final String[] sObjects = sValue.split(",");
				//final MultipleInputSelectionPanel msp = (MultipleInputSelectionPanel) container;
				final CheckComboBox ccb = (CheckComboBox) container;
				final Object[] objs = new Object[ccb.getModel().getSize()];
				for (int j = 0; j < objs.length; j++) {
					objs[j] = ccb.getModel().getElementAt(j);
				}
				for (int j = 0; j < objs.length; j++) {
					for (int i = 0; i < sObjects.length; i++) {
						if (objs[j] instanceof IDataObject) {
							final IDataObject ido = (IDataObject) objs[j];
							if (ido.getName().equals(sObjects[i].trim())) {
								ccb.getModel().addCheck(objs[j]);
								//selectedIndices.add(new Integer(j));
								break;
							}
						}
						else if (objs[j] instanceof RasterLayerAndBand) {
							try {
								final RasterLayerAndBand rlab = (RasterLayerAndBand) objs[j];
								if (rlab.getRasterLayer().getName().equals(sObjects[i].trim())) {
									if (Integer.parseInt(sObjects[i + 1].trim()) - 1 == rlab.getBand()) {
										ccb.getModel().addCheck(objs[j]);
										//selectedIndices.add(new Integer(j));
										break;
									}
								}
							}
							catch (final Exception e) {
							}

						}
					}
				}
				//msp.setSelectedIndices(selectedIndices);
			}
			else if (param instanceof ParameterPoint) {
				final StringTokenizer st = new StringTokenizer(sValue, ",");
				if (st.countTokens() == 2) {
					final double x = Double.parseDouble(st.nextToken());
					final double y = Double.parseDouble(st.nextToken());
					final PointSelectionPanel psp = (PointSelectionPanel) container;
					psp.setPoint(new Point2D.Double(x, y));
				}
			}
			else if ((param instanceof ParameterTableField) || (param instanceof ParameterBand)) {
				final JComboBox cb = (JComboBox) container;
				try {
					cb.setSelectedIndex(Integer.parseInt(sValue));
				}
				catch (final Exception e) {
				}
			}

		}
		catch (final Exception e) {
			Sextante.addErrorToLog(e);
		}

	}


	protected void setFixedTableValue(final Parameter param,
			final FixedTablePanel container,
			final String sValue) {

		boolean bIsNumberOfRowsFixed;
		int iCols, iRows;
		int iCol, iRow;
		int iToken = 0;
		FixedTableModel tableModel;
		final StringTokenizer st = new StringTokenizer(sValue, ",");
		String sToken;
		AdditionalInfoFixedTable ai;
		try {
			ai = (AdditionalInfoFixedTable) param.getParameterAdditionalInfo();
			iCols = ai.getColsCount();
			final int iTokens = st.countTokens();
			iRows = (st.countTokens() / iCols);
			bIsNumberOfRowsFixed = ai.isNumberOfRowsFixed();
			tableModel = new FixedTableModel(ai.getCols(), iRows, bIsNumberOfRowsFixed);

			if (bIsNumberOfRowsFixed) {
				if (iRows != ai.getRowsCount()) {
					return;
				}
			}
			else {
				if (st.countTokens() % iCols != 0) {
					return;
				}
			}

			while (st.hasMoreTokens()) {
				iRow = (int) Math.floor(iToken / (double) iCols);
				iCol = iToken % iCols;
				sToken = st.nextToken().trim();
				tableModel.setValueAt(sToken, iRow, iCol);
				iToken++;
			}

			container.setTableModel(tableModel);

		}
		catch (final Exception e) {
			return;
		}

	}




	public Object getParameterContainer(final String sParameterName) {

		final ParametersSet parameters = m_Algorithm.getParameters();
		Parameter param;
		try {
			param = parameters.getParameter(sParameterName);
		}
		catch (final WrongParameterIDException e1) {
			//return;
		}
		Object container = null;
		for (int i = 0; i < m_ParameterContainer.size(); i++) {
			final ParameterContainer pc = (ParameterContainer) m_ParameterContainer.get(i);
			if (pc.getName().equals(sParameterName)) {
				return container;

			}
		}
		return container;

	}

}
