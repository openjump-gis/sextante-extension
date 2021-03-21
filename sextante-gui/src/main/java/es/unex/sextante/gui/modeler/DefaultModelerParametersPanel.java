

package es.unex.sextante.gui.modeler;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
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

import es.unex.sextante.additionalInfo.AdditionalInfo3DRasterLayer;
import es.unex.sextante.additionalInfo.AdditionalInfoBand;
import es.unex.sextante.additionalInfo.AdditionalInfoBoolean;
import es.unex.sextante.additionalInfo.AdditionalInfoDataObject;
import es.unex.sextante.additionalInfo.AdditionalInfoFilepath;
import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoRasterLayer;
import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.additionalInfo.AdditionalInfoTable;
import es.unex.sextante.additionalInfo.AdditionalInfoTableField;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.WrongParameterIDException;
import es.unex.sextante.gui.algorithm.FileSelectionPanel;
import es.unex.sextante.gui.algorithm.ParameterContainer;
import es.unex.sextante.modeler.elements.ModelElementBand;
import es.unex.sextante.modeler.elements.ModelElementBoolean;
import es.unex.sextante.modeler.elements.ModelElementFixedTable;
import es.unex.sextante.modeler.elements.ModelElementInputArray;
import es.unex.sextante.modeler.elements.ModelElementNumericalValue;
import es.unex.sextante.modeler.elements.ModelElementPoint;
import es.unex.sextante.modeler.elements.ModelElementRasterLayer;
import es.unex.sextante.modeler.elements.ModelElementSelection;
import es.unex.sextante.modeler.elements.ModelElementString;
import es.unex.sextante.modeler.elements.ModelElementTable;
import es.unex.sextante.modeler.elements.ModelElementTableField;
import es.unex.sextante.modeler.elements.ModelElementVectorLayer;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;
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
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;


public class DefaultModelerParametersPanel
extends
GeoAlgorithmModelerParametersPanel {

	public static int        CELL_HEIGHT                          = 18;
	private static int       COLUMN_WIDTH                         = 250;
	private static final int MAX_BANDS                            = 10;

	protected int            m_iCurrentRow                        = 0;
	private JScrollPane      jScrollPanelParameters;
	private JPanel           jPanelParameters;
	private final ArrayList  m_InputParameterContainer            = new ArrayList();
	private final ArrayList  m_OutputParameterDefinitionContainer = new ArrayList();
	private ArrayList        m_ComboBox                           = new ArrayList();


	public DefaultModelerParametersPanel() {

		super();

	}


	@Override
	protected void initGUI() {

		m_ComboBox = new ArrayList();

		jScrollPanelParameters = new JScrollPane();
		this.add(jScrollPanelParameters);
		jScrollPanelParameters.setPreferredSize(new java.awt.Dimension(650, 300));
		jScrollPanelParameters.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPanelParameters.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		{
			jPanelParameters = new JPanel();
			final TableLayout jPanelParametersLayout = new TableLayout(getTableLayoutMatrix());
			jPanelParametersLayout.setHGap(5);
			jPanelParametersLayout.setVGap(5);
			jPanelParameters.setLayout(jPanelParametersLayout);
			jScrollPanelParameters.setViewportView(jPanelParameters);
			jPanelParameters.setSize(new java.awt.Dimension(630, 300));
			{
				if (m_Algorithm.requiresRasterLayers() || m_Algorithm.requiresMultipleRasterLayers()
						|| m_Algorithm.requiresMultipleRasterBands()) {
					addRasterLayers(jPanelParameters);
				}
				if (m_Algorithm.requires3DRasterLayers()) {
					add3DRasterLayers(jPanelParameters);
				}
				if (m_Algorithm.requiresVectorLayers() || m_Algorithm.requiresMultipleVectorLayers()) {
					addVectorLayers(jPanelParameters);
				}
				if (m_Algorithm.requiresTables() || m_Algorithm.requiresMultipleTables()) {
					addTables(jPanelParameters);
				}
				if (m_Algorithm.requiresNonDataObjects()) {
					addNonDataObjects(jPanelParameters);
				}
				if (m_Algorithm.generatesLayersOrTables()) {
					addOutputObjects(jPanelParameters);
				}
			}
		}
	}


	protected void addOutputObjects(final JPanel pane) {

		try {
			String sKey;
			String sDescription;
			final OutputObjectsSet ooset = m_Algorithm.getOutputObjects();
			final OutputObjectsSet oosetGlobal = this.m_GlobalAlgorithm.getOutputObjects();

			addTitleLabel(pane, Sextante.getText("Output_objects"), m_iCurrentRow, true);
			m_iCurrentRow++;

			for (int i = 0; i < ooset.getOutputObjectsCount(); i++) {
				Output out = ooset.getOutput(i);
				if ((out instanceof OutputRasterLayer) || (out instanceof OutputVectorLayer) || (out instanceof OutputTable)
						|| (out instanceof Output3DRasterLayer)) {
					final OutputLayerSettingsPanel olsp = new OutputLayerSettingsPanel();
					sDescription = out.getDescription();
					sKey = out.getName();
					addTitleLabel(pane, sDescription + "[" + out.getTypeDescription() + "]", m_iCurrentRow, false);
					if (!oosetGlobal.containsKey(sKey + this.m_sAlgorithmName)) {
						sDescription = "\"" + sDescription + "\" " + Sextante.getText("from") + " " + m_sAlgorithmDescription;
						olsp.setKeepAsFinalResult(false);
					}
					else {
						out = oosetGlobal.getOutput(sKey + this.m_sAlgorithmName);
						sDescription = out.getDescription();
						olsp.setKeepAsFinalResult(true);
					}
					olsp.setName(sDescription);

					pane.add(olsp, getStringTableCoords(2, m_iCurrentRow));
					m_iCurrentRow++;
					m_OutputParameterDefinitionContainer.add(new OutputParameterContainer(sKey, olsp));
				}
			}
		}
		catch (final Exception e) {
			Sextante.addErrorToLog(e);
		}


	}


	private void addRasterLayers(final JPanel pane) {

		int i, j;
		final ArrayList childComboBoxIndex = new ArrayList();
		boolean bIsOptional = false;
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
				childComboBoxIndex.clear();
				//check for bands that depend on this raster layer
				sParameterName = parameter.getParameterName();
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
								m_InputParameterContainer.add(new ParameterContainer(subParameter, comboBox));
								final ObjectAndDescription oad = ((ObjectAndDescription) getParameterValue(subParameter));
								if (oad != null) {
									final Object value = oad.getObject();
									if (value instanceof Integer) {
										final int iValue = ((Integer) value).intValue();
										comboBox.setSelectedIndex(comboBox.getItemCount() - 250 + iValue);
									}
									else if (value instanceof String) {
										final String sDesc = ((ObjectAndDescription) m_DataObjects.get(value)).getDescription();
										comboBox.setSelectedItem(sDesc);
									}
								}
							}
						}
						catch (final Exception e) {
							Sextante.addErrorToLog(e);
						}
					}
				}
				try {
					if (((AdditionalInfoRasterLayer) parameter.getParameterAdditionalInfo()).getIsMandatory()) {
						bIsOptional = false;
						addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow - childComboBoxIndex.size(), false);
					}
					else {
						bIsOptional = true;
						addTitleLabel(pane, parameter.getParameterDescription() + Sextante.getText("[optional]"),
								m_iCurrentRow - childComboBoxIndex.size(), false);
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					Sextante.addErrorToLog(e);
				}
				comboBox = getRasterLayerSelectionComboBox(childComboBoxIndex, bIsOptional);

				if (bIsOptional) {
					comboBox.setSelectedIndex(0);
				}
				final String sKey = m_GlobalAlgorithm.getInputAsignment(sParameterName, m_Algorithm);
				if (sKey != null) {
					for (int k = 0; k < comboBox.getModel().getSize(); k++) {
						final ObjectAndDescription oad = (ObjectAndDescription) comboBox.getModel().getElementAt(k);
						final Object ob = oad.getObject();
						if (ob != null) {
							if (oad.getObject().equals(sKey)) {
								comboBox.setSelectedIndex(k);
								break;
							}
						}
					}
				}
				pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow - childComboBoxIndex.size()));
				m_iCurrentRow++;
				m_InputParameterContainer.add(new ParameterContainer(parameter, comboBox));
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


	private void add3DRasterLayers(final JPanel pane) {

		int i;
		final ArrayList childComboBoxIndex = new ArrayList();
		boolean bIsOptional = false;
		Parameter parameter;
		final ParametersSet parameters = m_Algorithm.getParameters();
		JComboBox comboBox;
		String sParameterName;

		addTitleLabel(pane, Sextante.getText("3D_Raster_layers"), m_iCurrentRow, true);
		m_iCurrentRow++;

		for (i = 0; i < m_Algorithm.getNumberOfParameters(); i++) {
			parameter = parameters.getParameter(i);
			sParameterName = parameter.getParameterName();
			if (parameter instanceof Parameter3DRasterLayer) {
				try {
					if (((AdditionalInfo3DRasterLayer) parameter.getParameterAdditionalInfo()).getIsMandatory()) {
						bIsOptional = false;
						addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);
					}
					else {
						bIsOptional = true;
						addTitleLabel(pane, parameter.getParameterDescription() + Sextante.getText("[optional]"), m_iCurrentRow, false);
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					Sextante.addErrorToLog(e);
				}
				comboBox = getRasterLayerSelectionComboBox(childComboBoxIndex, bIsOptional);

				if (bIsOptional) {
					comboBox.setSelectedIndex(0);
				}
				final String sKey = m_GlobalAlgorithm.getInputAsignment(sParameterName, m_Algorithm);
				if (sKey != null) {
					for (int k = 0; k < comboBox.getModel().getSize(); k++) {
						final ObjectAndDescription oad = (ObjectAndDescription) comboBox.getModel().getElementAt(k);
						final Object ob = oad.getObject();
						if (ob != null) {
							if (oad.getObject().equals(sKey)) {
								comboBox.setSelectedIndex(k);
								break;
							}
						}
					}
				}
				pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow));
				m_iCurrentRow++;
				m_InputParameterContainer.add(new ParameterContainer(parameter, comboBox));
			}
		}
	}


	private void addVectorLayers(final JPanel pane) {

		boolean bAddNotSetField = false;
		int i, j;
		int iChildFields = 0;
		String sParameterName;
		Parameter parameter;
		Parameter subParameter;
		final ParametersSet parameters = m_Algorithm.getParameters();
		JComboBox comboBox;
		JComboBox childComboBox;

		addTitleLabel(pane, Sextante.getText("Vector_layer"), m_iCurrentRow, true);
		m_iCurrentRow++;

		for (i = 0; i < m_Algorithm.getNumberOfParameters(); i++) {
			parameter = parameters.getParameter(i);
			if (parameter instanceof ParameterVectorLayer) {
				iChildFields = 0;
				//check for table fields that depend on this vector layer
				sParameterName = parameter.getParameterName();
				for (j = 0; j < m_Algorithm.getNumberOfParameters(); j++) {
					subParameter = parameters.getParameter(j);
					if (subParameter instanceof ParameterTableField) {
						try {
							if (((AdditionalInfoTableField) subParameter.getParameterAdditionalInfo()).getParentParameterName().equals(
									sParameterName)) {
								m_iCurrentRow++;
								iChildFields++;
								addTitleLabel(pane, "      " + subParameter.getParameterDescription(), m_iCurrentRow, false);
								childComboBox = getVectorLayerFieldSelectionTextField();
								final ObjectAndDescription oad = ((ObjectAndDescription) getParameterValue(subParameter));
								if (oad != null) {
									final Object value = oad.getObject();
									if (value instanceof Integer) {
										final int iValue = ((Integer) value).intValue();
										childComboBox.setSelectedIndex(iValue);
									}
									else if (value instanceof String) {
										final JTextField textField = (JTextField) childComboBox.getEditor().getEditorComponent();
										textField.setText((String) value);
									}
								}
								pane.add(childComboBox, getStringTableCoords(2, m_iCurrentRow));
								m_InputParameterContainer.add(new ParameterContainer(subParameter, childComboBox));
							}
						}
						catch (final NullParameterAdditionalInfoException e) {
							Sextante.addErrorToLog(e);
						}
					}
				}
				try {
					final AdditionalInfoVectorLayer ai = (AdditionalInfoVectorLayer) parameter.getParameterAdditionalInfo();
					if (ai.getIsMandatory()) {
						bAddNotSetField = false;
						addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow - iChildFields, false);
					}
					else {
						bAddNotSetField = true;
						addTitleLabel(pane, parameter.getParameterDescription() + Sextante.getText("[optional]"), m_iCurrentRow
								- iChildFields, false);
					}
					comboBox = getVectorLayerSelectionComboBox(bAddNotSetField, ai.getShapeType());
					if (bAddNotSetField) {
						comboBox.setSelectedIndex(0);
					}
					final String sKey = m_GlobalAlgorithm.getInputAsignment(sParameterName, m_Algorithm);
					if (sKey != null) {
						for (int k = 0; k < comboBox.getModel().getSize(); k++) {
							final ObjectAndDescription oad = (ObjectAndDescription) comboBox.getModel().getElementAt(k);
							final Object ob = oad.getObject();
							if (ob != null) {
								if (oad.getObject().equals(sKey)) {
									comboBox.setSelectedIndex(k);
									break;
								}
							}
						}
					}
					pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow - iChildFields));
					m_iCurrentRow++;
					m_InputParameterContainer.add(new ParameterContainer(parameter, comboBox));
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


	private void addTables(final JPanel pane) {

		boolean bIsOptional = false;
		int i, j;
		Parameter parameter;
		final ParametersSet parameters = m_Algorithm.getParameters();
		JComboBox comboBox;
		int iChildFields = 0;
		String sParameterName;
		Parameter subParameter;
		JComboBox childComboBox;

		addTitleLabel(pane, Sextante.getText("Tables"), m_iCurrentRow, true);
		m_iCurrentRow++;

		for (i = 0; i < m_Algorithm.getNumberOfParameters(); i++) {
			parameter = parameters.getParameter(i);
			if (parameter instanceof ParameterTable) {
				iChildFields = 0;
				sParameterName = parameter.getParameterName();
				for (j = 0; j < m_Algorithm.getNumberOfParameters(); j++) {
					subParameter = parameters.getParameter(j);
					if (subParameter instanceof ParameterTableField) {
						try {
							if (((AdditionalInfoTableField) subParameter.getParameterAdditionalInfo()).getParentParameterName().equals(
									sParameterName)) {
								m_iCurrentRow++;
								iChildFields++;
								addTitleLabel(pane, "      " + subParameter.getParameterDescription(), m_iCurrentRow, false);
								childComboBox = getTableFieldSelectionTextField();
								final ObjectAndDescription oad = ((ObjectAndDescription) getParameterValue(subParameter));
								if (oad != null) {
									final Object value = oad.getObject();
									if (value instanceof Integer) {
										final int iValue = ((Integer) value).intValue();
										childComboBox.setSelectedIndex(iValue);
									}
									else if (value instanceof String) {
										final JTextField textField = (JTextField) childComboBox.getEditor().getEditorComponent();
										textField.setText((String) value);
									}
								}
								pane.add(childComboBox, getStringTableCoords(2, m_iCurrentRow));
								m_InputParameterContainer.add(new ParameterContainer(subParameter, childComboBox));
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
						bIsOptional = false;
						addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow - iChildFields, false);
					}
					else {
						bIsOptional = true;
						addTitleLabel(pane, parameter.getParameterDescription() + Sextante.getText("[optional]"), m_iCurrentRow
								- iChildFields, false);
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					Sextante.addErrorToLog(e);
				}
				comboBox = getTableSelectionComboBox(bIsOptional);
				if (bIsOptional) {
					comboBox.setSelectedIndex(0);
				}
				final String sKey = m_GlobalAlgorithm.getInputAsignment(sParameterName, m_Algorithm);
				if (sKey != null) {
					for (int k = 0; k < comboBox.getModel().getSize(); k++) {
						final ObjectAndDescription oad = (ObjectAndDescription) comboBox.getModel().getElementAt(k);
						final Object ob = oad.getObject();
						if (ob != null) {
							if (oad.getObject().equals(sKey)) {
								comboBox.setSelectedIndex(k);
								break;
							}
						}
					}
				}
				pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow - iChildFields));
				m_iCurrentRow++;
				m_InputParameterContainer.add(new ParameterContainer(parameter, comboBox));
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


	private void addNonDataObjects(final JPanel pane) {

		int i;
		Parameter parameter;
		final ParametersSet parameters = m_Algorithm.getParameters();

		addTitleLabel(pane, Sextante.getText("Options"), m_iCurrentRow, true);
		m_iCurrentRow++;

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


	private void addMultipleInput(final JPanel pane,
			final ParameterMultipleInput parameter) {

		int i;
		int iDataTypes[];
		String sKey;
		ObjectAndDescription[] objects;
		ObjectAndDescription[] allObjects;
		ObjectAndDescription[] arrays;
		final ArrayList validArrays = new ArrayList();
		Parameter param;
		AdditionalInfoMultipleInput ainfo;

		try {
			final AdditionalInfoMultipleInput additionalInfo = (AdditionalInfoMultipleInput) parameter.getParameterAdditionalInfo();

			switch (additionalInfo.getDataType()) {
			case AdditionalInfoMultipleInput.DATA_TYPE_RASTER:
				objects = getElementsOfClass(ModelElementRasterLayer.class, true);
				iDataTypes = new int[] { AdditionalInfoMultipleInput.DATA_TYPE_RASTER };
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY:
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT:
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE:
			case AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON:
				objects = getElementsOfClass(ModelElementVectorLayer.class, true);
				iDataTypes = new int[] { additionalInfo.getDataType() };
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_TABLE:
				objects = getElementsOfClass(ModelElementTable.class, true);
				iDataTypes = new int[] { AdditionalInfoMultipleInput.DATA_TYPE_TABLE };
				break;
			case AdditionalInfoMultipleInput.DATA_TYPE_BAND:
				objects = getElementsOfClass(ModelElementRasterLayer.class, true);
				//               final ArrayList<ObjectAndDescription> list = new ArrayList<ObjectAndDescription>();
				//               for (int j = 0; j < layers.length; j++) {
				//                  final ObjectAndDescription oad = (ObjectAndDescription) m_DataObjects.get(layers[j].getObject());
				//                  final ModelElementRasterLayer merl = (ModelElementRasterLayer) oad.getObject();
				//                  int iBands = merl.getNumberOfBands();
				//                  if (iBands == ModelElementRasterLayer.NUMBER_OF_BANDS_UNDEFINED) {
				//                     iBands = MAX_BANDS;
				//                  }
				//                  for (int iBand = 0; iBand < iBands; iBand++) {
				//                     String sName;
				//                     if ((iBands == MAX_BANDS) && (iBand != 0)) {
				//                        sName = layers[j].getDescription() + " Band " + Integer.toString(iBand + 1) + "["
				//                                + Sextante.getText("unchecked") + "]";
				//
				//                     }
				//                     else {
				//                        sName = layers[j].getDescription() + " Band " + Integer.toString(iBand + 1);
				//                     }
				//                     list.add(new ObjectAndDescription(sName, layers[j].getObject() + "BAND" + Integer.toString(iBand)));
				//                  }
				//               }
				//               objects = list.toArray(new ObjectAndDescription[0]);
				iDataTypes = new int[] { AdditionalInfoMultipleInput.DATA_TYPE_RASTER, AdditionalInfoMultipleInput.DATA_TYPE_BAND };
				break;
			default:
				return;
			}

			arrays = getElementsOfClass(ModelElementInputArray.class, true);

			final ParametersSet ps = m_GlobalAlgorithm.getParameters();
			for (i = 0; i < arrays.length; i++) {
				sKey = (String) arrays[i].getObject();
				try {
					param = ps.getParameter(sKey);
					ainfo = (AdditionalInfoMultipleInput) param.getParameterAdditionalInfo();
					for (final int element : iDataTypes) {
						if (ainfo.getDataType() == element) {
							validArrays.add(arrays[i]);
							break;
						}
					}
				}
				catch (final WrongParameterIDException e) {
					Sextante.addErrorToLog(e);
				}

			}

			allObjects = new ObjectAndDescription[validArrays.size() + objects.length];
			for (i = 0; i < objects.length; i++) {
				allObjects[i] = objects[i];
			}
			for (i = 0; i < validArrays.size(); i++) {
				allObjects[i + objects.length] = (ObjectAndDescription) validArrays.get(i);
			}

			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

			//final MultipleInputSelectionPanel multipleInputPanel = new MultipleInputSelectionPanel(allObjects);
			final CheckComboBox ccb = new CheckComboBox();
			ccb.setTextFor(CheckComboBox.NONE, Sextante.getText("no_elements_selected"));
			ccb.setTextFor(CheckComboBox.MULTIPLE, Sextante.getText("multiple_elements_selected"));

			final BatchSelection bs = new BatchSelection.CheckBox();
			final EmbeddedComponent comp = new EmbeddedComponent(bs, Anchor.NORTH);
			ccb.setEmbeddedComponent(comp);

			for (int j = 0; j < allObjects.length; j++) {
				ccb.getModel().addElement(allObjects[j]);
			}
			final ObjectAndDescription oad = (ObjectAndDescription) getParameterValue(parameter);
			if (oad != null) {
				final ArrayList list = (ArrayList) oad.getObject();
				for (int j = 0; j < list.size(); j++) {
					for (int k = 0; k < allObjects.length; k++) {
						if (allObjects[k].getObject().equals(list.get(j))) {
							ccb.getModel().addCheck(allObjects[k]);
						}
					}
				}
				//multipleInputPanel.setSelectedObjects((ArrayList) oad.getObject());
			}
			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

			pane.add(ccb, getStringTableCoords(2, m_iCurrentRow));
			m_InputParameterContainer.add(new ParameterContainer(parameter, ccb));
			m_iCurrentRow++;

		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
		}
	}


	private void addFixedTable(final JPanel pane,
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

			final ObjectAndDescription[] tables = getElementsOfClass(ModelElementFixedTable.class, true);
			final AdditionalInfoFixedTable addInfo = new AdditionalInfoFixedTable(sCols, iRows, bIsNumberOfRowsFixed);
			final FixedTableSelectionPanel fixedTablePanel = new FixedTableSelectionPanel(addInfo, tables, this.m_DataObjects,
					this.m_GlobalAlgorithm);
			final String sKey = m_GlobalAlgorithm.getInputAsignment(parameter.getParameterName(), m_Algorithm);
			final ObjectAndDescription oad = (ObjectAndDescription) m_DataObjects.get(sKey);
			if (oad != null) {
				if (oad.getObject() instanceof FixedTableModel) {
					fixedTablePanel.setTable(oad.getObject());
				}
				else {
					fixedTablePanel.setTable(sKey);
				}
			}
			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

			pane.add(fixedTablePanel, getStringTableCoords(2, m_iCurrentRow));
			m_InputParameterContainer.add(new ParameterContainer(parameter, fixedTablePanel));
			m_iCurrentRow++;
		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
		}

	}


	private void addFilepath(final JPanel pane,
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
			m_InputParameterContainer.add(new ParameterContainer(parameter, fileSelectionPanel));
			m_iCurrentRow++;
		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
		}

	}


	private void addPoint(final JPanel pane,
			final ParameterPoint parameter) {

		String sKey;
		ObjectAndDescription oad;

		final JComboBox comboBox = new JComboBox();
		final ObjectAndDescription values[] = getElementsOfClass(ModelElementPoint.class, true);
		final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(values);
		sKey = m_GlobalAlgorithm.getInputAsignment(parameter.getParameterName(), m_Algorithm);
		if (sKey != null) {
			for (int k = 0; k < comboBox.getModel().getSize(); k++) {
				oad = (ObjectAndDescription) comboBox.getModel().getElementAt(k);
				if (oad.getObject().equals(sKey)) {
					comboBox.setSelectedIndex(k);
					break;
				}
			}
		}
		comboBox.setModel(defaultModel);
		addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);
		pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow));
		m_iCurrentRow++;
		m_InputParameterContainer.add(new ParameterContainer(parameter, comboBox));

	}


	private void addSelection(final JPanel pane,
			final ParameterSelection parameter) {

		int i;

		try {
			final JComboBox comboBox = new JComboBox();
			final String[] sValues = ((AdditionalInfoSelection) parameter.getParameterAdditionalInfo()).getValues();
			final ObjectAndDescription values[] = getElementsOfClass(ModelElementSelection.class, true);
			final String sValuesForCombo[] = new String[sValues.length + values.length];
			for (i = 0; i < sValues.length; i++) {
				sValuesForCombo[i] = sValues[i];
			}
			for (i = 0; i < values.length; i++) {
				sValuesForCombo[i + sValues.length] = values[i].getDescription();
			}
			final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(sValuesForCombo);
			comboBox.setModel(defaultModel);
			comboBox.setSelectedIndex(0);
			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);
			pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow));
			final String sKey = m_GlobalAlgorithm.getInputAsignment(parameter.getParameterName(), m_Algorithm);
			final ObjectAndDescription oad = (ObjectAndDescription) m_DataObjects.get(sKey);
			if (oad != null) {
				if (oad.getObject() instanceof Integer) {
					final int iValue = ((Integer) oad.getObject()).intValue();
					comboBox.setSelectedIndex(iValue);
				}
				else {
					for (i = 0; i < values.length; i++) {
						if (values[i].getObject().equals(sKey)) {
							comboBox.setSelectedIndex(sValues.length + i);
							break;
						}
					}
				}
			}
			m_iCurrentRow++;
			m_InputParameterContainer.add(new ParameterContainer(parameter, comboBox));
		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
		}

	}


	private void addNumericalTextField(final JPanel pane,
			final ParameterNumericalValue parameter) {

		JComboBox comboBox;
		JTextField textField;

		final int iType;

		try {
			final AdditionalInfoNumericalValue additionalInfo = (AdditionalInfoNumericalValue) parameter.getParameterAdditionalInfo();
			iType = additionalInfo.getType();

			addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

			comboBox = new JComboBox();
			final ObjectAndDescription values[] = getElementsOfClass(ModelElementNumericalValue.class, true);
			final ComboBoxModel jComboBoxNumericalInputModel = new DefaultComboBoxModel(values);
			comboBox.setModel(jComboBoxNumericalInputModel);
			comboBox.setSelectedIndex(-1);
			comboBox.setEditable(true);

			textField = (JTextField) comboBox.getEditor().getEditorComponent();

			if (iType == AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE) {
				textField.setText(Double.toString(additionalInfo.getDefaultValue()));
			}
			else {
				textField.setText(Integer.toString((int) additionalInfo.getDefaultValue()));
			}

			final String sKey = m_GlobalAlgorithm.getInputAsignment(parameter.getParameterName(), m_Algorithm);
			final ObjectAndDescription oad = (ObjectAndDescription) m_DataObjects.get(sKey);
			if (oad != null) {
				if (oad.getObject() instanceof Number) {
					final double dValue = ((Number) oad.getObject()).doubleValue();
					textField.setText(Double.toString(dValue));
				}
				else {
					for (int i = 0; i < values.length; i++) {
						if (values[i].getObject().equals(sKey)) {
							comboBox.setSelectedIndex(i);
							comboBox.updateUI();
							break;
						}
					}
				}
			}
			pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow));
			m_InputParameterContainer.add(new ParameterContainer(parameter, comboBox));
			m_iCurrentRow++;
		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
		}

	}


	protected void validateKeyTyping(final KeyEvent event) {

		final JTextField textField = (JTextField) event.getSource();
		final String text = textField.getText() + event.getKeyChar();
		try {
			Double.parseDouble(text);
			textField.setText(text);
		}
		catch (final NumberFormatException e) {
		}

	}


	private void addStringTextField(final JPanel pane,
			final ParameterString parameter) {

		JComboBox comboBox;

		addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);

		comboBox = new JComboBox();
		final ObjectAndDescription values[] = getElementsOfClass(ModelElementString.class, true);
		final ComboBoxModel jComboBoxModel = new DefaultComboBoxModel(values);
		comboBox.setModel(jComboBoxModel);
		comboBox.setSelectedIndex(-1);
		comboBox.setEditable(true);
		final String sKey = m_GlobalAlgorithm.getInputAsignment(parameter.getParameterName(), m_Algorithm);
		final ObjectAndDescription oad = (ObjectAndDescription) m_DataObjects.get(sKey);
		if (oad != null) {
			if (oad.getObject() instanceof String) {
				final JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
				textField.setText((String) oad.getObject());
			}
			else {
				for (int i = 0; i < values.length; i++) {
					if (values[i].getObject().equals(sKey)) {
						comboBox.setSelectedIndex(i);
						comboBox.updateUI();
						break;
					}
				}
			}
		}
		pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow));
		m_InputParameterContainer.add(new ParameterContainer(parameter, comboBox));
		m_iCurrentRow++;


	}


	private void addCheckBox(final JPanel pane,
			final ParameterBoolean parameter) {

		int i;

		final JComboBox comboBox = new JComboBox();
		final ObjectAndDescription values[] = getElementsOfClass(ModelElementBoolean.class, true);
		final String sValuesForCombo[] = new String[2 + values.length];
		sValuesForCombo[0] = Sextante.getText("Yes");
		sValuesForCombo[1] = Sextante.getText("No");
		for (i = 0; i < values.length; i++) {
			sValuesForCombo[i + 2] = values[i].getDescription();
		}

		final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(sValuesForCombo);
		comboBox.setModel(defaultModel);
		AdditionalInfoBoolean ai;
		try {
			ai = (AdditionalInfoBoolean) parameter.getParameterAdditionalInfo();
			if (ai.getDefaultValue()) {
				comboBox.setSelectedIndex(0);
			}
			else {
				comboBox.setSelectedIndex(1);
			}
		}
		catch (final NullParameterAdditionalInfoException e) {
			comboBox.setSelectedIndex(0);
		}

		addTitleLabel(pane, parameter.getParameterDescription(), m_iCurrentRow, false);
		final String sKey = m_GlobalAlgorithm.getInputAsignment(parameter.getParameterName(), m_Algorithm);
		final ObjectAndDescription oad = (ObjectAndDescription) m_DataObjects.get(sKey);
		if (oad != null) {
			if (oad.getObject() instanceof Boolean) {
				final Boolean b = (Boolean) oad.getObject();
				if (b.booleanValue()) {
					comboBox.setSelectedIndex(0);

				}
				else {
					comboBox.setSelectedIndex(1);
				}
				comboBox.updateUI();
			}
			else {
				for (i = 0; i < values.length; i++) {
					if (values[i].getObject().equals(sKey)) {
						comboBox.setSelectedIndex(i + 2);
						break;
					}
				}
			}
		}
		pane.add(comboBox, getStringTableCoords(2, m_iCurrentRow));
		m_iCurrentRow++;
		m_InputParameterContainer.add(new ParameterContainer(parameter, comboBox));

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


	protected double[][] getTableLayoutMatrix() {

		int i;
		int iRows = 0;

		final double iSizeColumns[] = { 10, TableLayoutConstants.FILL, 360, 10 };

		iRows += m_Algorithm.getNumberOfParameters();

		if (m_Algorithm.requiresRasterLayers() || m_Algorithm.requiresMultipleRasterLayers()
				|| m_Algorithm.requiresMultipleRasterBands()) {
			iRows++;
		}
		if (m_Algorithm.requiresVectorLayers() || m_Algorithm.requiresMultipleVectorLayers()) {
			iRows++;
		}
		if (m_Algorithm.requiresTables() || m_Algorithm.requiresMultipleTables()) {
			iRows++;
		}
		if (m_Algorithm.requires3DRasterLayers()) {
			iRows++;
		}
		if (m_Algorithm.requiresNonDataObjects()) {
			iRows++;
		}

		final OutputObjectsSet ooSet = m_Algorithm.getOutputObjects();
		final int iOutput = ooSet.getOutputObjectsCount();

		if (iOutput != 0) {
			iRows += (iOutput + 1);
		}

		final double iSizeRows[] = new double[iRows];
		for (i = 0; i < iRows - iOutput; i++) {
			iSizeRows[i] = CELL_HEIGHT;
		}
		for (i = iRows - iOutput; i < iRows; i++) {
			iSizeRows[i] = CELL_HEIGHT * 2.5;
		}

		final double iSize[][] = new double[2][];
		iSize[0] = iSizeColumns;
		iSize[1] = iSizeRows;

		return iSize;

	}


	private JComboBox getRasterLayerSelectionComboBox(final ArrayList childComboBoxes,
			final boolean bIsOptional) {

		final JComboBox comboBox = new JComboBox();

		final ObjectAndDescription[] sObjects = getElementsOfClass(ModelElementRasterLayer.class, bIsOptional);

		final Integer[] childsArray = new Integer[childComboBoxes.size()];
		for (int i = 0; i < childsArray.length; i++) {
			childsArray[i] = (Integer) childComboBoxes.get(i);
		}
		final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(sObjects);
		if (bIsOptional) {
			defaultModel.insertElementAt(new ObjectAndDescription(Sextante.getText("[Not_selected]"), null), 0);
		}
		comboBox.setModel(defaultModel);

		comboBox.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(final java.awt.event.ItemEvent e) {
				int i;
				int iIndex;
				String sNames[] = null;
				DefaultComboBoxModel defaultModel;
				final ObjectAndDescription ob = (ObjectAndDescription) comboBox.getSelectedItem();
				final ObjectAndDescription oad = (ObjectAndDescription) m_DataObjects.get(ob.getObject());
				if (oad != null) {
					final int iBands = ((ModelElementRasterLayer) oad.getObject()).getNumberOfBands();
					for (i = 0; i < childsArray.length; i++) {
						iIndex = (childsArray[i]).intValue();
						sNames = getBandNames(iBands);
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


	private JComboBox getVectorLayerSelectionComboBox(final boolean bIsOptional,
			final int iShapeType) {

		final JComboBox comboBox = new JComboBox();

		final ObjectAndDescription[] allLayers = getElementsOfClass(ModelElementVectorLayer.class, bIsOptional);

		final ArrayList list = new ArrayList();

		for (final ObjectAndDescription element : allLayers) {
			final String sKey = (String) element.getObject();
			final ObjectAndDescription oad = (ObjectAndDescription) m_DataObjects.get(sKey);
			final ModelElementVectorLayer mevl = (ModelElementVectorLayer) oad.getObject();
			if ((mevl.getShapeType() == iShapeType) || (mevl.getShapeType() == ModelElementVectorLayer.SHAPE_TYPE_UNDEFINED)
					|| (iShapeType == AdditionalInfoVectorLayer.SHAPE_TYPE_ANY)) {
				list.add(element);
			}
		}

		final ObjectAndDescription[] layers = new ObjectAndDescription[list.size()];
		for (int i = 0; i < list.size(); i++) {
			layers[i] = (ObjectAndDescription) list.get(i);
		}

		final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(layers);
		if (bIsOptional) {
			defaultModel.insertElementAt(new ObjectAndDescription(Sextante.getText("[Not_selected]"), null), 0);
		}
		comboBox.setModel(defaultModel);

		return comboBox;

	}


	private JComboBox getTableFieldSelectionTextField() {

		JComboBox comboBox;

		comboBox = new JComboBox();

		ParameterTableField param;
		Parameter parentParam;
		final ObjectAndDescription oadArray[] = getElementsOfClass(ModelElementTableField.class, true);
		ObjectAndDescription oad;
		ObjectAndDescription values[];
		String sKey;
		final ArrayList valuesArrayList = new ArrayList();
		for (final ObjectAndDescription element : oadArray) {
			try {
				oad = element;
				sKey = (String) oad.getObject();
				param = (ParameterTableField) m_GlobalAlgorithm.getParameters().getParameter(sKey);
				AdditionalInfoTableField ai;
				ai = (AdditionalInfoTableField) param.getParameterAdditionalInfo();
				sKey = ai.getParentParameterName();
				parentParam = m_GlobalAlgorithm.getParameters().getParameter(sKey);
				if (parentParam != null /*&&
                                                                                                                                                                                                                                                                                                                                					parentParam.getClass().equals(ParameterTable.class)*/) {
					valuesArrayList.add(oad);
				}
			}
			catch (final NullParameterAdditionalInfoException e) {
				Sextante.addErrorToLog(e);
			}
			catch (final WrongParameterIDException e) {
				Sextante.addErrorToLog(e);
			}
		}

		values = new ObjectAndDescription[valuesArrayList.size()];
		for (int i = 0; i < valuesArrayList.size(); i++) {
			values[i] = (ObjectAndDescription) valuesArrayList.get(i);
		}
		final ComboBoxModel jComboBoxModel = new DefaultComboBoxModel(values);
		comboBox.setModel(jComboBoxModel);
		comboBox.setEditable(true);

		return comboBox;

	}


	private JComboBox getVectorLayerFieldSelectionTextField() {

		JComboBox comboBox;

		comboBox = new JComboBox();

		ParameterTableField param;
		Parameter parentParam;
		final ObjectAndDescription oadArray[] = getElementsOfClass(ModelElementTableField.class, true);
		ObjectAndDescription oad;
		ObjectAndDescription values[];
		String sKey;
		final ArrayList valuesArrayList = new ArrayList();
		for (final ObjectAndDescription element : oadArray) {
			try {
				oad = element;
				sKey = (String) oad.getObject();
				param = (ParameterTableField) m_GlobalAlgorithm.getParameters().getParameter(sKey);
				AdditionalInfoTableField ai;
				ai = (AdditionalInfoTableField) param.getParameterAdditionalInfo();
				sKey = ai.getParentParameterName();
				parentParam = m_GlobalAlgorithm.getParameters().getParameter(sKey);
				if (parentParam != null) {
					valuesArrayList.add(oad);
				}
			}
			catch (final NullParameterAdditionalInfoException e) {
				Sextante.addErrorToLog(e);
			}
			catch (final WrongParameterIDException e) {
				Sextante.addErrorToLog(e);
			}
		}

		values = new ObjectAndDescription[valuesArrayList.size()];
		for (int i = 0; i < valuesArrayList.size(); i++) {
			values[i] = (ObjectAndDescription) valuesArrayList.get(i);
		}
		final ComboBoxModel jComboBoxModel = new DefaultComboBoxModel(values);
		comboBox.setModel(jComboBoxModel);
		comboBox.setSelectedIndex(-1);
		comboBox.setEditable(true);

		return comboBox;

	}


	private JComboBox getBandSelectionComboBox() {

		final ObjectAndDescription layers[] = getElementsOfClass(ModelElementRasterLayer.class, true);

		final ObjectAndDescription oad = (ObjectAndDescription) m_DataObjects.get(layers[0].getObject());
		final int iBands = ((ModelElementRasterLayer) oad.getObject()).getNumberOfBands();

		if (layers.length > 0) {
			final JComboBox comboBox = new JComboBox();
			final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(getBandNames(iBands));
			comboBox.setModel(defaultModel);
			return comboBox;
		}
		else {
			return new JComboBox();
		}

	}


	private String[] getBandNames(int iBands) {

		int i;
		final ObjectAndDescription values[] = getElementsOfClass(ModelElementBand.class, true);

		if (iBands == ModelElementRasterLayer.NUMBER_OF_BANDS_UNDEFINED) {
			iBands = MAX_BANDS;
		}

		final String sValues[] = new String[values.length + iBands];
		for (i = 0; i < values.length; i++) {
			sValues[i] = values[i].getDescription();
		}
		for (i = 0; i < iBands; i++) {
			if ((iBands == MAX_BANDS) && (i != 0)) {
				sValues[i + values.length] = Integer.toString(i + 1) + "[" + Sextante.getText("unchecked") + "]";
			}
			else {
				sValues[i + values.length] = Integer.toString(i + 1);
			}
		}

		return sValues;

	}


	private JComboBox getTableSelectionComboBox(final boolean bIsOptional) {

		final JComboBox comboBox = new JComboBox();
		final ObjectAndDescription[] sObjects = getElementsOfClass(ModelElementTable.class, bIsOptional);

		final DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(sObjects);
		if (bIsOptional) {
			defaultModel.insertElementAt(new ObjectAndDescription(Sextante.getText("[Not_selected]"), null), 0);
		}
		comboBox.setModel(defaultModel);

		return comboBox;

	}


	@Override
	public boolean assignParameters(final HashMap map) {

		boolean bAssigningOK = true;
		int i;
		ParameterContainer parameterContainer;

		for (i = 0; i < m_InputParameterContainer.size(); i++) {
			parameterContainer = (ParameterContainer) m_InputParameterContainer.get(i);
			if (parameterContainer.getType().equals("Table") || parameterContainer.getType().equals("Vector Layer")
					|| parameterContainer.getType().equals("Raster Layer") || parameterContainer.getType().equals("Point")) {
				makeDataObjectAssignment(map, parameterContainer);
			}
			else if (parameterContainer.getType().equals("Multiple Input")) {
				makeMultipleInputAssignment(map, parameterContainer);
			}
			else if (parameterContainer.getType().equals("Numerical Value")) {
				bAssigningOK = makeNumericalValueAssignment(map, parameterContainer);
			}
			else if (parameterContainer.getType().equals("String")) {
				bAssigningOK = makeStringAssignment(map, parameterContainer);
			}
			else if (parameterContainer.getType().equals("Boolean")) {
				bAssigningOK = makeBooleanAssignment(map, parameterContainer);
			}
			else if (parameterContainer.getType().equals("Fixed Table")) {
				bAssigningOK = makeFixedTableAssignment(map, parameterContainer);
			}
			else if (parameterContainer.getType().equals("Filepath")) {
				bAssigningOK = makeFilepathAssignment(parameterContainer);
			}
			else if (parameterContainer.getType().equals("Band")) {
				bAssigningOK = makeRasterBandAssignment(map, parameterContainer);
			}
			else if (parameterContainer.getType().equals("Table Field")) {
				bAssigningOK = makeTableFieldAssignment(map, parameterContainer);
			}
			else if (parameterContainer.getType().equals("Selection")) {
				bAssigningOK = makeSelectionAssignment(map, parameterContainer);
			}

			if (!bAssigningOK) {
				return false;
			}
		}

		final OutputObjectsSet oosetGlobal = this.m_GlobalAlgorithm.getOutputObjects();
		final OutputObjectsSet ooset = this.m_Algorithm.getOutputObjects();

		for (i = 0; i < m_OutputParameterDefinitionContainer.size(); i++) {
			final OutputParameterContainer opc = (OutputParameterContainer) m_OutputParameterDefinitionContainer.get(i);
			final OutputLayerSettingsPanel olsp = (OutputLayerSettingsPanel) opc.getContainer();
			final String sName = opc.getName() + this.m_sAlgorithmName;
			if (olsp.getKeepAsFinalResult()) {
				try {
					final Output out = ooset.getOutput(opc.getName());
					final Output outToAdd = out.getClass().newInstance();
					outToAdd.setName(sName);
					outToAdd.setDescription(olsp.getName());
					oosetGlobal.add(outToAdd);
				}
				catch (final Exception e) {
				}
			}
			else {
				oosetGlobal.remove(sName);
			}
		}

		return true;

	}


	private void makeDataObjectAssignment(final HashMap map,
			final ParameterContainer pc) {

		Parameter parameter;
		JComboBox container;
		String sKey;
		String sAssignment;
		ObjectAndDescription oad;
		parameter = pc.getParameter();
		container = (JComboBox) pc.getContainer();
		sKey = parameter.getParameterName();
		oad = (ObjectAndDescription) container.getSelectedItem();
		sAssignment = (String) oad.getObject();
		map.put(sKey, sAssignment);

	}


	private boolean makeMultipleInputAssignment(final HashMap map,
			final ParameterContainer pc) {

		int i;
		boolean bMandatory;
		boolean bContainsOnlyOptionalObjects = true;
		Parameter parameter;
		String sKey, sArrayKey;
		Object object;
		List selectedObjects;
		final ArrayList array = new ArrayList();
		//      MultipleInputSelectionPanel panel;
		//
		//      panel = (MultipleInputSelectionPanel) pc.getContainer();

		final CheckComboBox ccb = (CheckComboBox) pc.getContainer();

		//selectedObjects = panel.getSelectedObjects();
		selectedObjects = ccb.getModel().getCheckeds();
		for (i = 0; i < selectedObjects.size(); i++) {

			try {
				final ObjectAndDescription oad = (ObjectAndDescription) selectedObjects.get(i);
				array.add(oad.getObject());
				object = this.m_GlobalAlgorithm.getParameters().getParameter((String) oad.getObject());
				if (object instanceof ParameterDataObject) {
					parameter = (ParameterDataObject) object;
					bMandatory = ((AdditionalInfoDataObject) parameter.getParameterAdditionalInfo()).getIsMandatory();
					if (bMandatory) {
						bContainsOnlyOptionalObjects = false;
					}
				}
				else if (object instanceof ParameterMultipleInput) {
					parameter = (ParameterMultipleInput) object;
					bMandatory = ((AdditionalInfoMultipleInput) parameter.getParameterAdditionalInfo()).getIsMandatory();
					if (bMandatory) {
						bContainsOnlyOptionalObjects = false;
					}
				}
			}
			catch (final WrongParameterIDException e) {
				bContainsOnlyOptionalObjects = false;
			}
			catch (final Exception e) {
				Sextante.addErrorToLog(e);
				return false;
			}

		}

		parameter = pc.getParameter();
		try {
			bMandatory = ((AdditionalInfoMultipleInput) parameter.getParameterAdditionalInfo()).getIsMandatory();
			if (bMandatory && bContainsOnlyOptionalObjects) {
				return false;
			}
		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
			return false;
		}
		sKey = parameter.getParameterName();
		sArrayKey = getInnerParameterKey();
		map.put(sKey, sArrayKey);
		m_DataObjects.put(sArrayKey, new ObjectAndDescription("Multiple Input", array));

		return true;

	}


	private boolean makeTableFieldAssignment(final HashMap map,
			final ParameterContainer parameterContainer) {

		ObjectAndDescription oad;
		Parameter parameter = null;
		String sKey = null, sInnerKey;
		String sAssignment;

		final JComboBox comboBox = (JComboBox) parameterContainer.getContainer();
		final JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();

		try {
			parameter = m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			sKey = parameter.getParameterName();
			final Object selItem = comboBox.getSelectedItem();
			oad = (ObjectAndDescription) selItem;
			sAssignment = (String) oad.getObject();
			map.put(sKey, sAssignment);
			return true;
		}
		catch (final ClassCastException e) {
			sInnerKey = getInnerParameterKey();
			map.put(sKey, sInnerKey);
			m_DataObjects.put(sInnerKey, new ObjectAndDescription("Field", textField.getText()));
			return true;
		}
		catch (final Exception e) {
			return false;
		}

	}


	private boolean makeSelectionAssignment(final HashMap map,
			final ParameterContainer parameterContainer) {

		int iIndex;
		int iMaxIndex;
		String sAssignment;
		String sKey, sInnerKey;
		Integer selection;

		try {
			final JComboBox comboBox = (JComboBox) parameterContainer.getContainer();
			final ParameterSelection parameter = (ParameterSelection) m_Algorithm.getParameters().getParameter(
					parameterContainer.getName());
			final AdditionalInfoSelection ai = (AdditionalInfoSelection) parameter.getParameterAdditionalInfo();
			iMaxIndex = ai.getValues().length;
			iIndex = comboBox.getSelectedIndex();
			sKey = parameter.getParameterName();
			if (iIndex >= iMaxIndex) {
				final ObjectAndDescription values[] = getElementsOfClass(ModelElementSelection.class, true);
				sAssignment = (String) values[iIndex - iMaxIndex].getObject();
				map.put(sKey, sAssignment);
				return true;
			}
			else {
				sInnerKey = getInnerParameterKey();
				map.put(sKey, sInnerKey);
				selection = new Integer(iIndex);
				m_DataObjects.put(sInnerKey, new ObjectAndDescription("Selection", selection));
				return true;
			}
		}
		catch (final NullParameterAdditionalInfoException e) {
			Sextante.addErrorToLog(e);
		}
		catch (final WrongParameterIDException e) {
			Sextante.addErrorToLog(e);
		}

		return false;


	}


	private boolean makeRasterBandAssignment(final HashMap map,
			final ParameterContainer parameterContainer) {

		int iIndex;
		int iMaxIndex;
		String sAssignment;
		String sKey, sInnerKey;
		Integer selection;

		try {
			final ParameterBand parameter = (ParameterBand) m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			final JComboBox comboBox = (JComboBox) parameterContainer.getContainer();
			iMaxIndex = comboBox.getModel().getSize() - MAX_BANDS;
			iIndex = comboBox.getSelectedIndex();
			sKey = parameter.getParameterName();
			if (iIndex < iMaxIndex) {
				final ObjectAndDescription values[] = getElementsOfClass(ModelElementBand.class, true);
				sAssignment = (String) values[iIndex].getObject();
				map.put(sKey, sAssignment);
				return true;
			}
			else {
				sInnerKey = getInnerParameterKey();
				map.put(sKey, sInnerKey);
				selection = new Integer(iIndex - iMaxIndex);
				m_DataObjects.put(sInnerKey, new ObjectAndDescription("Band", selection));
				return true;
			}
		}
		catch (final WrongParameterIDException e) {
			Sextante.addErrorToLog(e);
		}

		return false;

	}


	private boolean makeNumericalValueAssignment(final HashMap map,
			final ParameterContainer parameterContainer) {

		double dValue;
		ObjectAndDescription oad;
		Parameter parameter = null;
		String sKey = null, sInnerKey;
		Double value;
		String sAssignment;

		final JComboBox comboBox = (JComboBox) parameterContainer.getContainer();
		final JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
		final String sValue = textField.getText();

		try {
			parameter = m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			final AdditionalInfoNumericalValue ai = (AdditionalInfoNumericalValue) parameter.getParameterAdditionalInfo();
			final double dMin = ai.getMinValue();
			final double dMax = ai.getMaxValue();
			sKey = parameter.getParameterName();
			final Object ob = comboBox.getSelectedItem();
			if (ob instanceof ObjectAndDescription) {
				oad = (ObjectAndDescription) ob;
				sAssignment = (String) oad.getObject();
				map.put(sKey, sAssignment);
				return true;
			}
			else {
				try {
					dValue = Double.parseDouble(sValue);
					if ((dValue < dMin) || (dValue > dMax)) {
						return false;
					}
					sInnerKey = getInnerParameterKey();
					map.put(sKey, sInnerKey);
					value = new Double(dValue);
					m_DataObjects.put(sInnerKey, new ObjectAndDescription("Numerical Value", value));
					return true;
				}
				catch (final NumberFormatException e) {
					return false;
				}
			}
		}
		catch (final Exception e) {
			Sextante.addErrorToLog(e);
			return false;
		}

	}


	private boolean makeFixedTableAssignment(final HashMap map,
			final ParameterContainer parameterContainer) {

		String sKey, sInnerKey;
		String sAssignment;
		FixedTableModel table;

		try {

			final FixedTableSelectionPanel panel = (FixedTableSelectionPanel) parameterContainer.getContainer();
			final Parameter parameter = m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			sKey = parameter.getParameterName();
			sAssignment = panel.getTableKey();
			if (sAssignment == null) {
				table = panel.getTableModel();
				sInnerKey = getInnerParameterKey();
				map.put(sKey, sInnerKey);
				m_DataObjects.put(sInnerKey, new ObjectAndDescription("Fixed Table", table));
			}
			else {
				map.put(sKey, sAssignment);
			}

			return true;

		}
		catch (final Exception e) {
			Sextante.addErrorToLog(e);
			return false;
		}
	}


	private boolean makeStringAssignment(final HashMap map,
			final ParameterContainer parameterContainer) {

		ObjectAndDescription oad;
		Parameter parameter = null;
		String sKey = null, sInnerKey;
		String sAssignment;

		final JComboBox comboBox = (JComboBox) parameterContainer.getContainer();
		final JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();

		try {
			parameter = m_Algorithm.getParameters().getParameter(parameterContainer.getName());
			sKey = parameter.getParameterName();
			final Object selItem = comboBox.getSelectedItem();
			oad = (ObjectAndDescription) selItem;
			sAssignment = (String) oad.getObject();
			map.put(sKey, sAssignment);
			return true;
		}
		catch (final ClassCastException e) {
			sInnerKey = getInnerParameterKey();
			map.put(sKey, sInnerKey);
			m_DataObjects.put(sInnerKey, new ObjectAndDescription("String", textField.getText()));
			return true;
		}
		catch (final Exception e) {
			return false;
		}

	}


	private boolean makeFilepathAssignment(final ParameterContainer parameterContainer) {

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


	private boolean makeBooleanAssignment(final HashMap map,
			final ParameterContainer parameterContainer) {

		int iIndex;
		Boolean bool;
		String sKey, sInnerKey;
		String sAssignment;

		try {
			final JComboBox comboBox = (JComboBox) parameterContainer.getContainer();
			final ParameterBoolean parameter = (ParameterBoolean) m_Algorithm.getParameters().getParameter(
					parameterContainer.getName());
			iIndex = comboBox.getSelectedIndex();
			sKey = parameter.getParameterName();
			if (iIndex >= 2) {
				final ObjectAndDescription values[] = getElementsOfClass(ModelElementBoolean.class, true);
				sAssignment = (String) values[iIndex - 2].getObject();
				map.put(sKey, sAssignment);
				return true;
			}
			else {
				sInnerKey = getInnerParameterKey();
				map.put(sKey, sInnerKey);
				bool = new Boolean(iIndex == 0);
				m_DataObjects.put(sInnerKey, new ObjectAndDescription("Boolean", bool));
				return true;
			}
		}
		catch (final WrongParameterIDException e) {
			Sextante.addErrorToLog(e);
			return false;
		}

	}

}
