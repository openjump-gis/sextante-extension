package es.unex.sextante.core;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfo3DRasterLayer;
import es.unex.sextante.additionalInfo.AdditionalInfoBand;
import es.unex.sextante.additionalInfo.AdditionalInfoBoolean;
import es.unex.sextante.additionalInfo.AdditionalInfoDataObject;
import es.unex.sextante.additionalInfo.AdditionalInfoFilepath;
import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoPoint;
import es.unex.sextante.additionalInfo.AdditionalInfoRasterLayer;
import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.additionalInfo.AdditionalInfoString;
import es.unex.sextante.additionalInfo.AdditionalInfoTable;
import es.unex.sextante.additionalInfo.AdditionalInfoTableField;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.exceptions.WrongParameterIDException;
import es.unex.sextante.exceptions.WrongParameterTypeException;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.Parameter3DRasterLayer;
import es.unex.sextante.parameters.ParameterBand;
import es.unex.sextante.parameters.ParameterBoolean;
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

/**
 * Set of parameters needed by a GeoAlgorithm. This set is used to specify the requirements of the algorithm and to create a
 * suitable GUI to get the required information from the user, among other tasks.
 * 
 * @author Victor Olaya volaya@unex.es
 * 
 */
public class ParametersSet {

	private final ArrayList m_Parameters;


	public ParametersSet() {

		m_Parameters = new ArrayList();
	}


	/**
	 * 
	 * @return the number of parameters in the set
	 */
	public int getNumberOfParameters() {

		return m_Parameters.size();

	}


	/**
	 * Returns the number of parameters of type raster layer. This does not include multiple inputs of this same type
	 * 
	 * @return the number of parameters requiring raster layers
	 * 
	 */

	public int getNumberOfRasterLayers() {

		return getNumberOfRasterLayers(false);

	}


	/**
	 * 
	 * @return the number of parameters requiring raster layers
	 * 
	 */
	public int getNumberOfRasterLayers(final boolean includeMultipleInputs) {

		int iCount = 0;
		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			final Parameter parameter = (Parameter) m_Parameters.get(i);

			if (parameter.getParameterTypeName().equals("Raster Layer")) {
				iCount++;
				continue;
			}
			// Also counts Multiples Raster Inputs (for GUI representation)
			if (includeMultipleInputs && (parameter instanceof ParameterMultipleInput)) {
				final ParameterMultipleInput param = ((ParameterMultipleInput) parameter);
				try {
					final int ai = ((AdditionalInfoMultipleInput) param.getParameterAdditionalInfo()).getDataType();
					if (ai == AdditionalInfoMultipleInput.DATA_TYPE_RASTER) {
						iCount++;
						continue;
					}
					if (ai == AdditionalInfoMultipleInput.DATA_TYPE_BAND) {
						iCount++;
						continue;
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return iCount;
	}


	/**
	 * Returns the number of parameters of type vector layer. This does not include multiple inputs of this same type
	 * 
	 * @return the number of parameters requiring vector layers
	 * 
	 */
	public int getNumberOfVectorLayers() {

		return getNumberOfVectorLayers(false);

	}


	/**
	 * Returns the number of parameters of type vector layer.
	 * 
	 * @return the number of parameters requiring vector layers
	 * 
	 */
	public int getNumberOfVectorLayers(final boolean includeMultiInputs) {

		int iCount = 0;
		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			final Parameter parameter = (Parameter) m_Parameters.get(i);
			if (parameter.getParameterTypeName().equals("Vector Layer")) {
				iCount++;
				continue;
			}

			if (includeMultiInputs && (parameter instanceof ParameterMultipleInput)) {
				final ParameterMultipleInput param = ((ParameterMultipleInput) parameter);
				try {
					final int ai = ((AdditionalInfoMultipleInput) param.getParameterAdditionalInfo()).getDataType();
					if ((ai == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY)
							|| (ai == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE)
							|| (ai == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT)
							|| (ai == AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON)) {
						iCount++;
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return iCount;
	}


	/**
	 * 
	 * @param shapeType
	 *                the type of shape for single input parameter
	 * @param dataType
	 *                the type of shape for multiple input parameters
	 * @return the number of mandatory parameters including a specific kind of vector layers
	 */
	private int getNumberOfSpecificVectorLayers(final int shapeType,
			final int dataType) {

		int iCount = 0;
		boolean bMandatory;
		int iDataType;
		int iShapeType;
		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			if (((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Vector Layer")) {
				try {
					iShapeType = ((AdditionalInfoVectorLayer) ((Parameter) m_Parameters.get(i)).getParameterAdditionalInfo()).getShapeType();
					bMandatory = ((AdditionalInfoVectorLayer) ((Parameter) m_Parameters.get(i)).getParameterAdditionalInfo()).getIsMandatory();
					if ((iShapeType == shapeType) && bMandatory) {
						iCount++;
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					Sextante.addErrorToLog(e);
				}
			}
			else if (((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Multiple Input")) {
				try {
					iDataType = ((AdditionalInfoMultipleInput) ((Parameter) m_Parameters.get(i)).getParameterAdditionalInfo()).getDataType();
					bMandatory = ((AdditionalInfoMultipleInput) ((Parameter) m_Parameters.get(i)).getParameterAdditionalInfo()).getIsMandatory();
					if ((iDataType == dataType) && bMandatory) {
						iCount++;
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					Sextante.addErrorToLog(e);
				}
			}
		}

		return iCount;
	}


	/**
	 * 
	 * @return the number of mandatory parameters requiring point vector layers
	 */
	public int getNumberOfPointVectorLayers() {

		return getNumberOfSpecificVectorLayers(AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
				AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT);

	}


	/**
	 * 
	 * @return the number of mandatory parameters requiring line vector layers
	 */
	public int getNumberOfLineVectorLayers() {

		return getNumberOfSpecificVectorLayers(AdditionalInfoVectorLayer.SHAPE_TYPE_LINE,
				AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE);

	}


	/**
	 * 
	 * @return the number of mandatory parameters requiring polygon vector layers
	 */
	public int getNumberOfPolygonVectorLayers() {

		return getNumberOfSpecificVectorLayers(AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON,
				AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON);

	}


	/**
	 * Returns the number of parameters of type table. This does not include multiple inputs of this same type
	 * 
	 * @return the number of parameters of type table
	 */
	public int getNumberOfTables() {

		int iCount = 0;
		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			if (((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Table")) {
				iCount++;
			}
		}

		return iCount;
	}


	/**
	 * Returns the number of parameters of type 3D raster layer
	 * 
	 * @return the number of parameters of type 3D raster layer
	 */
	public int getNumberOf3DRasterLayers() {

		int iCount = 0;
		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			if (((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("3D Raster Layer")) {
				iCount++;
			}
		}

		return iCount;
	}


	/**
	 * Returns the number of parameters of type different of Table, Vector or Raster.
	 * 
	 * @return the number of parameters of type different of Table, Vector or Raster.
	 */
	public int getNumberOfNoDataParameters() {

		int iCount = 0;
		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			final Object parameter = m_Parameters.get(i);
			if (parameter instanceof ParameterNumericalValue) {
				iCount++;
				continue;
			}
			if (parameter instanceof ParameterString) {
				iCount++;
				continue;
			}
			if (parameter instanceof ParameterSelection) {
				iCount++;
				continue;
			}
			if (parameter instanceof ParameterFixedTable) {
				iCount++;
				continue;
			}
			if (parameter instanceof ParameterPoint) {
				iCount++;
				continue;
			}
			if (parameter instanceof ParameterBoolean) {
				iCount++;
				continue;
			}
			if (parameter instanceof ParameterFilepath) {
				iCount++;
				continue;
			}
		}
		return iCount;
	}


	/**
	 * Returns the number of TableFields Parameters
	 * 
	 * @return the number of parameters of TableFields.
	 */
	public int getNumberOfTableFieldsParameters() {

		int iCount = 0;
		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			final Object parameter = m_Parameters.get(i);
			if (parameter instanceof ParameterTableField) {
				iCount++;
				continue;
			}
		}
		return iCount;
	}


	/**
	 * Returns the number of Bands Parameters
	 * 
	 * @return the number of parameters of Bands.
	 */
	public int getNumberOfBandsParameters() {

		int iCount = 0;
		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			final Object parameter = m_Parameters.get(i);
			if (parameter instanceof ParameterBand) {
				iCount++;
				continue;
			}
		}
		return iCount;
	}


	/**
	 * Returns true if the set contains parameters other than tables or layers
	 * 
	 * @return true if the set contains parameters other than tables or layers (vector or raster)
	 */
	public boolean requiresNonDataObjects() {

		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			if (((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Table")
					|| ((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Vector Layer")
					|| ((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Raster Layer")
					|| ((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Multiple Input")
					|| ((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Table Field")
					|| ((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("3D Raster Layer")
					|| ((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Band")) {}
			else {
				return true;
			}
		}

		return false;
	}


	/**
	 * Returns true if the set contains parameters requiring raster layers
	 * 
	 * @return true if the set contains parameters requiring raster layers
	 */
	public boolean requiresRasterLayers() {

		if (getNumberOfRasterLayers() > 0) {
			return true;
		}

		if (requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_RASTER, true)
				|| requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_BAND, true)) {
			return true;
		}

		return false;


	}


	/**
	 * Returns true if the set contains parameters requiring 3D raster layers
	 * 
	 * @return true if the set contains parameters requiring 3D raster layers
	 */
	public boolean requires3DRasterLayers() {

		return (getNumberOf3DRasterLayers() > 0);

	}


	/**
	 * Returns true if the set contains parameters requiring multiple raster layers
	 * 
	 * @return true if the set contains parameters requiring multiple raster layers
	 */
	public boolean requiresMultipleRasterLayers() {

		return requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_RASTER, false);

	}


	/**
	 * Returns true if the set contains parameters requiring multiple vector layers
	 * 
	 * @return true if the set contains parameters requiring multiple vector layers
	 */
	public boolean requiresMultipleVectorLayers() {

		return requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY, false)
				|| requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE, false)
				|| requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT, false)
				|| requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON, false);

	}


	/**
	 * Returns true if the set contains parameters requiring multiple tables
	 * 
	 * @return true if the set contains parameters requiring multiple tables
	 */
	public boolean requiresMultipleTables() {

		return requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_TABLE, false);

	}


	/**
	 * Returns true if the set contains parameters requiring multiple raster bands
	 * 
	 * @return true if the set contains parameters requiring multiple raster bands
	 */
	public boolean requiresMultipleRasterBands() {

		return requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_BAND, false);

	}


	/**
	 * 
	 * @param iType
	 *                the type of multiple input to check, as defined in
	 * @see {@link AdditionalInfoMultipleInput}.
	 * @param bOnlyMandatory
	 *                true if should consider only mandatory inputs
	 * @return returns true if the set contain parameters requiring multiple inputs of the specified type.
	 */
	private boolean requiresMultipleInputOfType(final int iType,
			boolean bOnlyMandatory) {

		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			if (((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Multiple Input")) {
				try {
					final AdditionalInfoMultipleInput ai = (AdditionalInfoMultipleInput) ((Parameter) m_Parameters.get(i)).getParameterAdditionalInfo();
					if (ai.getDataType() == iType) {
						if (ai.getIsMandatory() || !bOnlyMandatory) {
							return true;
						}
					}
				}
				catch (final NullParameterAdditionalInfoException e) {
					Sextante.addErrorToLog(e);
				}
			}
		}

		return false;

	}


	/**
	 * Returns true if there is any parameter in the set requiring a single raster band
	 * 
	 * @return true if there is any parameter in the set requiring a single raster band
	 */
	public boolean requiresRasterBands() {

		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			if (((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Band")) {
				return true;
			}
		}

		return false;
	}


	/**
	 * Returns true if the set contains parameters requiring vector layers
	 * 
	 * @return true if the set contains parameters requiring vector layers
	 */
	public boolean requiresVectorLayers() {

		if (getNumberOfVectorLayers() > 0) {
			return true;
		}

		if (requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY, true)) {
			return true;
		}

		return false;

	}


	/**
	 * Returns true if the set contains parameters requiring point vector layers
	 * 
	 * @return true if the set contains parameters requiring point vector layers
	 */
	public boolean requiresPointVectorLayers() {

		if (getNumberOfPointVectorLayers() > 0) {
			return true;
		}

		if (requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POINT, true)) {
			return true;
		}

		return false;

	}


	/**
	 * Returns true if the set contains parameters requiring line vector layers
	 * 
	 * @return true if the set contains parameters requiring line vector layers
	 */
	public boolean requiresLineVectorLayers() {

		if (getNumberOfLineVectorLayers() > 0) {
			return true;
		}

		if (requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_LINE, true)) {
			return true;
		}

		return false;

	}


	/**
	 * Returns true if the set contains parameters requiring polygon vector layers
	 * 
	 * @return true if the set contains parameters requiring polygon vector layers
	 */
	public boolean requiresPolygonVectorLayers() {

		if (getNumberOfPolygonVectorLayers() > 0) {
			return true;
		}

		if (requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_POLYGON, true)) {
			return true;
		}

		return false;

	}


	/**
	 * Returns true if the set contains parameters requiring tables
	 * 
	 * @return true if the set contains parameters requiring tables
	 */
	public boolean requiresTables() {

		if (getNumberOfTables() > 0) {
			return true;
		}

		if (requiresMultipleInputOfType(AdditionalInfoMultipleInput.DATA_TYPE_TABLE, true)) {
			return true;
		}

		return false;

	}


	/**
	 * Returns true if the set contains fixed table parameters
	 * 
	 * @return true if the set contains parameters requiring fixed tables
	 */
	public boolean requiresFixedTables() {

		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			if (((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Fixed Table")) {
				return true;
			}
		}
		return false;

	}


	/**
	 * Returns true if the set contains table fields parameters
	 * 
	 * @return true if the set contains table fields that depend on other fields.
	 */
	public boolean requiresTableFields() {

		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			if (((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Table Field")) {
				return true;
			}
		}

		return false;

	}


	/**
	 * Returns true if the set contains point parameters.
	 * 
	 * @return true if the set contains point parameters.
	 */
	public boolean requiresPoints() {

		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			if (((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Point")) {
				return true;
			}
		}

		return false;

	}


	/**
	 * Adds a new parameter to the set
	 * 
	 * @param parameter
	 *                a parameter
	 * @return true if the parameter was added
	 * @throws RepeatedParameterNameException
	 *                 if already exists a parameter in the set with the same name
	 */
	public boolean addParameter(final Parameter parameter) throws RepeatedParameterNameException {

		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			final Parameter param = (Parameter) m_Parameters.get(i);
			if (param.getParameterName().equals(parameter.getParameterName())) {
				throw new RepeatedParameterNameException();
			}
		}

		return m_Parameters.add(parameter);

	}


	/**
	 * 
	 * @param parameter
	 *                a parameter
	 * @throws WrongParameterIDException
	 *                 if the parameter does not exist in the set
	 */
	public void removeParameter(final Parameter parameter) throws WrongParameterIDException {


		removeParameter(parameter.getParameterName());

	}


	/**
	 * 
	 * @param sParameterName
	 *                the parameter name
	 * @throws WrongParameterIDException
	 *                 if no parameter with the specified name exists in the set
	 */
	public void removeParameter(final String sParameterName) throws WrongParameterIDException {

		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			if (((Parameter) m_Parameters.get(i)).getParameterName().equals(sParameterName)) {
				m_Parameters.remove(i);
				return;
			}
		}

		throw new WrongParameterIDException();

	}


	/**
	 * 
	 * @param paramClass
	 *                the class of the parameters to retrieve
	 * @return an arrayList with all parameters of the given class
	 */
	public ArrayList getParametersOfType(final Class paramClass) {

		int i;
		final ArrayList list = new ArrayList();

		for (i = 0; i < m_Parameters.size(); i++) {
			final Parameter param = (Parameter) m_Parameters.get(i);
			if (param.getClass().equals(paramClass)) {
				list.add(param);
			}
		}

		return list;

	}


	/**
	 * 
	 * @param sParameterName
	 *                the name of the parameter
	 * @return the parameter
	 * @throws WrongParameterIDException
	 *                 if no parameter with the specified name exists in the set
	 */
	public Parameter getParameter(final String sParameterName) throws WrongParameterIDException {

		int i;

		for (i = 0; i < m_Parameters.size(); i++) {
			if (((Parameter) m_Parameters.get(i)).getParameterName().equals(sParameterName)) {
				return ((Parameter) m_Parameters.get(i));
			}
		}

		throw new WrongParameterIDException();

	}


	/**
	 * 
	 * @param iIndex
	 *                the index of the parameter in the set
	 * @return the parameter
	 * @throws ArrayIndexOutOfBoundsException
	 *                 if iIndex is not a valid array index
	 */
	public Parameter getParameter(final int iIndex) throws ArrayIndexOutOfBoundsException {

		if ((iIndex >= 0) && (iIndex < m_Parameters.size())) {
			return (Parameter) m_Parameters.get(iIndex);
		}
		else {
			throw new ArrayIndexOutOfBoundsException();
		}

	}


	/**
	 * 
	 * @param sParameterName
	 *                the name of the parameter
	 * @return the parameter as an object
	 * @throws WrongParameterIDException
	 *                 if no parameter with that name exists in the set
	 * @throws NullParameterValueException
	 *                 if the parameter is null
	 */
	public Object getParameterValueAsObject(final String sParameterName) throws WrongParameterTypeException,
	WrongParameterIDException, NullParameterValueException {

		Parameter parameter;
		parameter = getParameter(sParameterName);

		return parameter.getParameterValueAsObject();

	}


	/**
	 * 
	 * @param sParameterName
	 *                the name of the parameter
	 * @return the int value of the parameter
	 * @throws WrongParameterTypeException
	 *                 if the parameter cannot be cast to an int value
	 * @throws WrongParameterIDException
	 *                 if no parameter with that name exists in the set
	 * @throws NullParameterValueException
	 *                 if the parameter is null
	 */
	public int getParameterValueAsInt(final String sParameterName) throws WrongParameterTypeException, WrongParameterIDException,
	NullParameterValueException {

		Parameter parameter;
		parameter = getParameter(sParameterName);

		return parameter.getParameterValueAsInt();

	}


	/**
	 * 
	 * @param sParameterName
	 *                the name of the parameter
	 * @return the value of the parameter as double
	 * @throws WrongParameterTypeException
	 *                 if the parameter cannot be cast to a double value
	 * @throws WrongParameterIDException
	 *                 if no parameter with that name exists in the set
	 * @throws NullParameterValueException
	 *                 if the parameter is null
	 */
	public double getParameterValueAsDouble(final String sParameterName) throws WrongParameterTypeException,
	WrongParameterIDException, NullParameterValueException {

		Parameter parameter;
		parameter = getParameter(sParameterName);

		return parameter.getParameterValueAsDouble();

	}


	/**
	 * 
	 * @param sParameterName
	 *                the name of the parameter
	 * @return the value of the parameter as a Point2D object
	 * @throws WrongParameterTypeException
	 *                 if the parameter cannot be cast to a Point2D value
	 * @throws WrongParameterIDException
	 *                 if no parameter with that name exists in the set
	 * @throws NullParameterValueException
	 *                 if the parameter is null
	 */
	public Point2D getParameterValueAsPoint(final String sParameterName) throws WrongParameterTypeException,
	WrongParameterIDException, NullParameterValueException {

		Parameter parameter;
		parameter = getParameter(sParameterName);

		return parameter.getParameterValueAsPoint();

	}


	/**
	 * 
	 * @param sParameterName
	 *                the name of the parameter
	 * @return the boolean value of the parameter
	 * @throws WrongParameterTypeException
	 *                 if the parameter cannot be cast to a boolean value
	 * @throws WrongParameterIDException
	 *                 if no parameter with that name exists in the set
	 * @throws NullParameterValueException
	 *                 if the parameter is null
	 */
	public boolean getParameterValueAsBoolean(final String sParameterName) throws WrongParameterTypeException,
	WrongParameterIDException, NullParameterValueException {

		Parameter parameter;
		parameter = getParameter(sParameterName);

		return parameter.getParameterValueAsBoolean();

	}


	/**
	 * 
	 * @param sParameterName
	 *                the name of the parameter
	 * @return an array with the value(s) of the parameter
	 * @throws WrongParameterTypeException
	 *                 if the parameter cannot be cast to an ArrayList
	 * @throws WrongParameterIDException
	 *                 if no parameter with that name exists in the set
	 * @throws NullParameterValueException
	 *                 if the parameter is null
	 */
	public ArrayList getParameterValueAsArrayList(final String sParameterName) throws WrongParameterTypeException,
	WrongParameterIDException,
	NullParameterValueException {

		Parameter parameter;
		parameter = getParameter(sParameterName);

		return parameter.getParameterValueAsArrayList();

	}


	/**
	 * 
	 * @param sParameterName
	 *                the name of the parameter
	 * @return the value of the parameter as a string
	 * @throws WrongParameterTypeException
	 *                 if the parameter cannot be cast to a string
	 * @throws WrongParameterIDException
	 *                 if no parameter with that name exists in the set
	 * @throws NullParameterValueException
	 *                 if the parameter is null
	 */
	public String getParameterValueAsString(final String sParameterName) throws WrongParameterTypeException,
	WrongParameterIDException, NullParameterValueException,
	NullParameterAdditionalInfoException {

		Parameter parameter;
		parameter = getParameter(sParameterName);

		return parameter.getParameterValueAsString();

	}


	/**
	 * 
	 * @param sParameterName
	 *                the name of the parameter
	 * @return the value of the parameter as a vector layer
	 * @throws WrongParameterTypeException
	 *                 if the parameter cannot be converted to a vector layer
	 * @throws WrongParameterIDException
	 *                 if no parameter with that name exists in the set
	 * @throws NullParameterValueException
	 *                 if the parameter is null
	 */
	public IVectorLayer getParameterValueAsVectorLayer(final String sParameterName) throws WrongParameterTypeException,
	WrongParameterIDException,
	NullParameterValueException {

		Parameter parameter;
		parameter = getParameter(sParameterName);

		return parameter.getParameterValueAsVectorLayer();

	}


	/**
	 * 
	 * @param sParameterName
	 *                the name of the parameter
	 * @return the value of the parameter as a raster layer
	 * @throws WrongParameterTypeException
	 *                 if the parameter cannot be converted to a raster layer
	 * @throws WrongParameterIDException
	 *                 if no parameter with that name exists in the set
	 * @throws NullParameterValueException
	 *                 if the parameter is null
	 */
	public IRasterLayer getParameterValueAsRasterLayer(final String sParameterName) throws WrongParameterTypeException,
	WrongParameterIDException,
	NullParameterValueException {

		Parameter parameter;
		parameter = getParameter(sParameterName);

		return parameter.getParameterValueAsRasterLayer();

	}


	/**
	 * 
	 * @param sParameterName
	 *                the name of the parameter
	 * @return the value of the parameter as a 3D raster layer
	 * @throws WrongParameterTypeException
	 *                 if the parameter cannot be converted to a 3D raster layer
	 * @throws WrongParameterIDException
	 *                 if no parameter with that name exists in the set
	 * @throws NullParameterValueException
	 *                 if the parameter is null
	 */
	public I3DRasterLayer getParameterValueAs3DRasterLayer(final String sParameterName) throws WrongParameterTypeException,
	WrongParameterIDException,
	NullParameterValueException {

		Parameter parameter;
		parameter = getParameter(sParameterName);

		return parameter.getParameterValueAs3DRasterLayer();

	}


	/**
	 * 
	 * @param sParameterName
	 *                the name of the parameter
	 * @return the value of the parameter as a table
	 * @throws WrongParameterTypeException
	 *                 if the parameter cannot be converted to a table
	 * @throws WrongParameterIDException
	 *                 if no parameter with that name exists in the set
	 * @throws NullParameterValueException
	 *                 if the parameter is null
	 */
	public ITable getParameterValueAsTable(final String sParameterName) throws WrongParameterTypeException,
	WrongParameterIDException, NullParameterValueException {

		Parameter parameter;
		parameter = getParameter(sParameterName);

		return parameter.getParameterValueAsTable();

	}


	/**
	 * Adds a vector layer to the parameter set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param iShapeType
	 *                the type of shapes
	 * @param bIsMandatory
	 *                true if it is not an optional layer
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addInputVectorLayer(final String sName,
			final String sDescription,
			final int iShapeType,
			final boolean bIsMandatory) throws RepeatedParameterNameException {

		final AdditionalInfoVectorLayer additionalInfo = new AdditionalInfoVectorLayer(iShapeType, bIsMandatory);

		final ParameterVectorLayer parameter = new ParameterVectorLayer();
		parameter.setParameterName(sName);
		parameter.setParameterDescription(sDescription);
		parameter.setParameterAdditionalInfo(additionalInfo);
		addParameter(parameter);

	}


	/*public void addOutputVectorLayer(String sName,
   								 String sDescription,
   								 int iShapeType)
   		throws RepeatedParameterNameException{

   	AdditionalInfoVectorLayer additionalInfo = new AdditionalInfoVectorLayer(iShapeType,
   														 					true,
   														 					true);

   	StdExtParameterVectorLayer parameter = new StdExtParameterVectorLayer();
   	parameter.setParameterName(sName);
   	parameter.setParameterDescription(sDescription);
   	parameter.setParameterAdditionalInfo(additionalInfo);

   	_AddParameter(parameter);

   }*/

	/**
	 * Adds a raster layer to the parameter set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param bIsMandatory
	 *                true if it is not an optional layer
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addInputRasterLayer(final String sName,
			final String sDescription,
			final boolean bIsMandatory) throws RepeatedParameterNameException {

		final AdditionalInfoRasterLayer additionalInfo = new AdditionalInfoRasterLayer(bIsMandatory);

		final ParameterRasterLayer parameter = new ParameterRasterLayer();
		parameter.setParameterName(sName);
		parameter.setParameterDescription(sDescription);
		parameter.setParameterAdditionalInfo(additionalInfo);
		addParameter(parameter);

	}


	/**
	 * Adds a 3D raster layer to the parameter set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param bIsMandatory
	 *                true if it is not an optional layer
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addInput3DRasterLayer(final String sName,
			final String sDescription,
			final boolean bIsMandatory) throws RepeatedParameterNameException {

		final AdditionalInfo3DRasterLayer additionalInfo = new AdditionalInfo3DRasterLayer(bIsMandatory);

		final Parameter3DRasterLayer parameter = new Parameter3DRasterLayer();
		parameter.setParameterName(sName);
		parameter.setParameterDescription(sDescription);
		parameter.setParameterAdditionalInfo(additionalInfo);
		addParameter(parameter);

	}


	/**
	 * Adds a table to the parameter set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param bIsMandatory
	 *                true if it is not an optional table
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addInputTable(final String sName,
			final String sDescription,
			final boolean bIsMandatory) throws RepeatedParameterNameException {

		final AdditionalInfoTable additionalInfo = new AdditionalInfoTable(bIsMandatory);

		final ParameterTable parameter = new ParameterTable();
		parameter.setParameterName(sName);
		parameter.setParameterDescription(sDescription);
		parameter.setParameterAdditionalInfo(additionalInfo);
		addParameter(parameter);

	}


	/**
	 * Adds a multiple input the parameter set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param iDataType
	 *                the type of data required
	 * @param bIsMandatory
	 *                true if the number of selected elements cannot be zero
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addMultipleInput(final String sName,
			final String sDescription,
			final int iDataType,
			final boolean bIsMandatory) throws RepeatedParameterNameException {

		final AdditionalInfoMultipleInput additionalInfo = new AdditionalInfoMultipleInput(iDataType, bIsMandatory);
		final ParameterMultipleInput parameter = new ParameterMultipleInput();
		parameter.setParameterName(sName);
		parameter.setParameterDescription(sDescription);
		parameter.setParameterAdditionalInfo(additionalInfo);
		addParameter(parameter);

	}


	/**
	 * Adds a table field to the set. The table field is set as mandatory by default
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param sParentParameterName
	 *                the name of the parent parameter (vector layer or table) to which this parameter is linked
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addTableField(final String sName,
			final String sDescription,
			final String sParentParameterName) throws RepeatedParameterNameException,
	UndefinedParentParameterNameException,
	OptionalParentParameterException {

		addTableField(sName, sDescription, sParentParameterName, true);

	}


	/**
	 * Adds a table field to the set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param sParentParameterName
	 *                the name of the parent parameter (vector layer or table) to which this parameter is linked
	 * @param bIsMandatory
	 *                true if the user must select a field. False if this is not necessary to execute the corresponding algorithm
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addTableField(final String sName,
			final String sDescription,
			final String sParentParameterName,
			final boolean bIsMandatory) throws RepeatedParameterNameException,
	UndefinedParentParameterNameException, OptionalParentParameterException {

		int i;
		boolean bParentParameterFound = false;
		for (i = 0; i < m_Parameters.size(); i++) {
			if (((Parameter) m_Parameters.get(i)).getParameterName().equals(sParentParameterName)) {
				if (((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Table")
						|| ((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Vector Layer")) {
					try {
						final AdditionalInfoDataObject parentAdditionalInfo = (AdditionalInfoDataObject) ((Parameter) m_Parameters.get(i)).getParameterAdditionalInfo();
						if (parentAdditionalInfo.getIsMandatory()) {
							final AdditionalInfoTableField additionalInfo = new AdditionalInfoTableField(sParentParameterName,
									bIsMandatory);
							final ParameterTableField parameter = new ParameterTableField();
							parameter.setParameterName(sName);
							parameter.setParameterDescription(sDescription);
							parameter.setParameterAdditionalInfo(additionalInfo);
							addParameter(parameter);
							bParentParameterFound = true;
						}
						else {
							throw new OptionalParentParameterException();
						}
					}
					catch (final NullParameterAdditionalInfoException e) {
						Sextante.addErrorToLog(e);
					}
				}
			}
		}

		if (!bParentParameterFound) {
			throw new UndefinedParentParameterNameException();
		}

	}


	/**
	 * Adds a band to the set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param sParentParameterName
	 *                the name of the parent parameter (raster layer) to which this parameter is linked
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addBand(final String sName,
			final String sDescription,
			final String sParentParameterName) throws RepeatedParameterNameException,
	UndefinedParentParameterNameException, OptionalParentParameterException {

		int i;
		boolean bParentParameterFound = false;
		for (i = 0; i < m_Parameters.size(); i++) {
			if (((Parameter) m_Parameters.get(i)).getParameterName().equals(sParentParameterName)) {
				if (((Parameter) m_Parameters.get(i)).getParameterTypeName().equals("Raster Layer")) {
					try {
						final AdditionalInfoDataObject parentAdditionalInfo = (AdditionalInfoDataObject) ((Parameter) m_Parameters.get(i)).getParameterAdditionalInfo();
						if (parentAdditionalInfo.getIsMandatory()) {
							final AdditionalInfoBand additionalInfo = new AdditionalInfoBand(sParentParameterName);
							final ParameterBand parameter = new ParameterBand();
							parameter.setParameterName(sName);
							parameter.setParameterDescription(sDescription);
							parameter.setParameterAdditionalInfo(additionalInfo);
							addParameter(parameter);
							bParentParameterFound = true;
						}
						else {
							throw new OptionalParentParameterException();
						}
					}
					catch (final NullParameterAdditionalInfoException e) {
						Sextante.addErrorToLog(e);
					}
				}
			}
		}

		if (!bParentParameterFound) {
			throw new UndefinedParentParameterNameException();
		}

	}


	/**
	 * Adds a numerical value to the set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param iType
	 *                the type of numerical value (integer or double)
	 * @param dDefaultValue
	 *                the default value to use (or show)
	 * @param dMinValue
	 *                the min value admitted
	 * @param dMaxValue
	 *                the max value admitted
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addNumericalValue(final String sName,
			final String sDescription,
			final int iType,
			final double dDefaultValue,
			final double dMinValue,
			final double dMaxValue) throws RepeatedParameterNameException {

		final AdditionalInfoNumericalValue additionalInfo = new AdditionalInfoNumericalValue(iType, dDefaultValue, dMinValue,
				dMaxValue);

		final ParameterNumericalValue parameter = new ParameterNumericalValue();
		parameter.setParameterName(sName);
		parameter.setParameterDescription(sDescription);
		parameter.setParameterAdditionalInfo(additionalInfo);
		parameter.setParameterValue(new Double(dDefaultValue));
		addParameter(parameter);

	}


	/**
	 * Adds a numerical value to the set. No min or max values are defined, so the parameter can take any value
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param dDefaultValue
	 *                the default value to use (or show)
	 * @param iType
	 *                the type of numerical value (integer or double)
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addNumericalValue(final String sName,
			final String sDescription,
			final double dDefaultValue,
			final int iType) throws RepeatedParameterNameException {

		addNumericalValue(sName, sDescription, iType, dDefaultValue, Double.NEGATIVE_INFINITY, Double.MAX_VALUE);

	}


	/**
	 * Add a string to the set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addString(final String sName,
			final String sDescription) throws RepeatedParameterNameException {

		addString(sName, sDescription, "");

	}


	/**
	 * Add a string to the set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param sDefaultString
	 *                the default value
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addString(final String sName,
			final String sDescription,
			final String sDefaultString) throws RepeatedParameterNameException {

		final AdditionalInfoString additionalInfo = new AdditionalInfoString();
		additionalInfo.setDefaultString(sDefaultString);

		final ParameterString parameter = new ParameterString();
		parameter.setParameterName(sName);
		parameter.setParameterDescription(sDescription);
		parameter.setParameterAdditionalInfo(additionalInfo);
		parameter.setParameterValue(sDefaultString);
		addParameter(parameter);

	}


	/**
	 * Adds a filepath to the set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param bFolder
	 *                true if it is a folder dialog
	 * @param bOpenDialog
	 *                true if it is an open file dialog, false if it's a save file dialog
	 * @param sExt
	 *                the extensions allowed
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 * 
	 */
	public void addFilepath(final String sName,
			final String sDescription,
			final boolean bFolder,
			final boolean bOpenDialog,
			final String[] sExt) throws RepeatedParameterNameException {

		final AdditionalInfoFilepath additionalInfo = new AdditionalInfoFilepath(bFolder, bOpenDialog, sExt);

		final ParameterFilepath parameter = new ParameterFilepath();
		parameter.setParameterName(sName);
		parameter.setParameterDescription(sDescription);
		parameter.setParameterAdditionalInfo(additionalInfo);
		addParameter(parameter);

	}


	/**
	 * Adds a filepath to the set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param bFolder
	 *                true if it is a folder dialog
	 * @param bOpenDialog
	 *                true if it is an open file dialog, false if it's a save file dialog
	 * @param sExt
	 *                the extension allowed
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 * 
	 */
	public void addFilepath(final String sName,
			final String sDescription,
			final boolean bFolder,
			final boolean bOpenDialog,
			final String sExt) throws RepeatedParameterNameException {

		final AdditionalInfoFilepath additionalInfo = new AdditionalInfoFilepath(bFolder, bOpenDialog, new String[] { sExt });

		final ParameterFilepath parameter = new ParameterFilepath();
		parameter.setParameterName(sName);
		parameter.setParameterDescription(sDescription);
		parameter.setParameterAdditionalInfo(additionalInfo);
		addParameter(parameter);

	}


	/**
	 * Adds a boolean value to the set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the descripion of the parameter
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addBoolean(final String sName,
			final String sDescription,
			final boolean bDefault) throws RepeatedParameterNameException {

		final AdditionalInfoBoolean additionalInfo = new AdditionalInfoBoolean(bDefault);

		final ParameterBoolean parameter = new ParameterBoolean();
		parameter.setParameterName(sName);
		parameter.setParameterDescription(sDescription);
		parameter.setParameterAdditionalInfo(additionalInfo);
		parameter.setParameterValue(Boolean.valueOf(bDefault));
		addParameter(parameter);

	}


	/**
	 * Add a selection index (to choose from a list) to the set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param sValues
	 *                the values to choose from
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addSelection(final String sName,
			final String sDescription,
			final String[] sValues) throws RepeatedParameterNameException {

		final AdditionalInfoSelection additionalInfo = new AdditionalInfoSelection(sValues);

		final ParameterSelection parameter = new ParameterSelection();
		parameter.setParameterName(sName);
		parameter.setParameterDescription(sDescription);
		parameter.setParameterAdditionalInfo(additionalInfo);
		parameter.setParameterValue(new Integer(0));
		addParameter(parameter);

	}


	/**
	 * Adds a fixed table to the set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @param sColumnNames
	 *                names of columns(fields) in the table
	 * @param iRows
	 *                number of rows in the table
	 * @param bIsNumberOfRowsFixed
	 *                true if the number of rows cannot be modified
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addFixedTable(final String sName,
			final String sDescription,
			final String[] sColumnNames,
			final int iRows,
			final boolean bIsNumberOfRowsFixed) throws RepeatedParameterNameException {

		final AdditionalInfoFixedTable additionalInfo = new AdditionalInfoFixedTable(sColumnNames, iRows, bIsNumberOfRowsFixed);

		final ParameterFixedTable parameter = new ParameterFixedTable();
		parameter.setParameterName(sName);
		parameter.setParameterDescription(sDescription);
		parameter.setParameterAdditionalInfo(additionalInfo);
		addParameter(parameter);

	}


	/**
	 * Add a point to the set
	 * 
	 * @param sName
	 *                the name of the parameter
	 * @param sDescription
	 *                the description of the parameter
	 * @throws RepeatedParameterNameException
	 *                 if a parameter with the same name already exists in the set
	 */
	public void addPoint(final String sName,
			final String sDescription) throws RepeatedParameterNameException {

		final AdditionalInfoPoint additionalInfo = new AdditionalInfoPoint();

		final ParameterPoint parameter = new ParameterPoint();
		parameter.setParameterName(sName);
		parameter.setParameterDescription(sDescription);
		parameter.setParameterAdditionalInfo(additionalInfo);
		addParameter(parameter);

	}


	/**
	 * Returns a new instance of the parameter set
	 * 
	 * @return a new instance of the parameter set
	 */
	public ParametersSet getNewInstance() {

		final ParametersSet set = new ParametersSet();

		for (int i = 0; i < m_Parameters.size(); i++) {
			try {
				final Parameter param = (Parameter) m_Parameters.get(i);
				set.addParameter(param.getNewInstance());
			}
			catch (final RepeatedParameterNameException e) {}
		}
		return set;


	}


	/**
	 * Returns true if all parameters in the set have valid values
	 * 
	 * @return true if all parameters in the set have valid values
	 */
	public boolean areParameterValuesCorrect() {

		for (int i = 0; i < m_Parameters.size(); i++) {
			final Parameter param = (Parameter) m_Parameters.get(i);
			if (!param.isParameterValueCorrect()) {
				return false;
			}
		}

		return true;

	}



}
