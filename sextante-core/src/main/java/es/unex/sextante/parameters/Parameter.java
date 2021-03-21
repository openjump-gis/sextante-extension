

package es.unex.sextante.parameters;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParserException;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterTypeException;


/**
 * A parameter in a {@link ParametersSet}, needed to run a {@link GeoAlgorithm}
 * 
 * @author volaya
 * 
 */
public abstract class Parameter {

   private static final String   CLASS                     = "class";
   private static final String   DESCRIPTION               = "description";
   private static final String   TOOLTIP                   = "tooltip";
   private static final String   COORDS                    = "coords";

   protected static final String INPUT                     = "input";
   protected static final String ATTRIBUTE                 = "attribute";
   protected static final String VALUE                     = "value";
   protected static final String NAME                      = "name";

   private String                m_sName                   = "";
   private String                m_sDescription            = "";
   private String                m_sTooltip                = "";
   protected Object              m_ParameterValue          = null;
   protected AdditionalInfo      m_ParameterAdditionalInfo = null;


   /**
    * Returns the name used to identify the parameter in the parameters set
    * 
    * @return the name of the parameter
    */
   public abstract String getParameterTypeName();


   /**
    * Sets the value of the parameter
    * 
    * @param value
    *                the new value of the parameter
    * @return true if the value is a correct one and could be assigned
    */
   public abstract boolean setParameterValue(Object value);


   /**
    * Set the additional information for this parameter
    * 
    * @see AdditionalInfo
    * @param additionalInfo
    *                the additional info for this parameter
    * @return true it the passed additional info can be assigned to this parameter
    */
   public abstract boolean setParameterAdditionalInfo(AdditionalInfo additionalInfo);


   /**
    * Returns the value of the parameter as an integer
    * 
    * @return the value of the parameter as an integer
    * @throws WrongParameterTypeException
    *                 if the value cannot be returned as an integer
    * @throws NullParameterValueException
    *                 if the parameter is null
    */
   public abstract int getParameterValueAsInt() throws WrongParameterTypeException, NullParameterValueException;


   /**
    * Returns the value of the parameter as a double
    * 
    * @return the value of the parameter as a double
    * @throws WrongParameterTypeException
    *                 if the parameter cannot be returned as an double
    * @throws NullParameterValueException
    *                 if the value is null
    */
   public abstract double getParameterValueAsDouble() throws WrongParameterTypeException, NullParameterValueException;


   /**
    * Returns the value of the parameter as a boolean value
    * 
    * @return the value of the parameter as a boolean value
    * @throws WrongParameterTypeException
    *                 if the value cannot be returned as a boolean value
    * @throws NullParameterValueException
    *                 if the parameter is null
    */
   public abstract boolean getParameterValueAsBoolean() throws WrongParameterTypeException, NullParameterValueException;


   /**
    * Returns the value of the parameter as a string
    * 
    * @return the value of the parameter as a string
    * @throws WrongParameterTypeException
    *                 if the parameter cannot be returned as a string
    * @throws NullParameterValueException
    *                 if the value is null
    * @throws NullParameterAdditionalInfoException
    *                 if the additional info of the parameter is null
    */
   public abstract String getParameterValueAsString() throws WrongParameterTypeException, NullParameterValueException,
                                                     NullParameterAdditionalInfoException;


   /**
    * Returns the value of the parameter as a vector layer
    * 
    * @return the value of the parameter as a vector layer
    * @throws WrongParameterTypeException
    *                 if the parameter cannot be returned as a vector layer
    * @throws NullParameterValueException
    *                 if the value is null and it is a mandatory vector layer
    * @throws NullParameterAdditionalInfoException
    *                 if the additional info of the parameter is null
    */
   public abstract IVectorLayer getParameterValueAsVectorLayer() throws WrongParameterTypeException, NullParameterValueException;


   /**
    * Returns the value of the parameter as a 3D raster layer
    * 
    * @return the value of the parameter as a 3D raster layer
    * @throws WrongParameterTypeException
    *                 if the parameter cannot be returned as a 3D raster layer
    * @throws NullParameterValueException
    *                 if the value is null and it is a mandatory 3D raster layer
    * @throws NullParameterAdditionalInfoException
    *                 if the additional info of the parameter is null
    */
   public abstract I3DRasterLayer getParameterValueAs3DRasterLayer() throws WrongParameterTypeException,
                                                                    NullParameterValueException;


   /**
    * Returns the value of the parameter as a raster layer
    * 
    * @return the value of the parameter as a raster layer
    * @throws WrongParameterTypeException
    *                 if the parameter cannot be returned as a raster layer
    * @throws NullParameterValueException
    *                 if the value is null and it is a mandatory raster layer
    * @throws NullParameterAdditionalInfoException
    *                 if the additional info of the parameter is null
    */
   public abstract IRasterLayer getParameterValueAsRasterLayer() throws WrongParameterTypeException, NullParameterValueException;


   /**
    * Returns the value of the parameter as a table
    * 
    * @return the value of the parameter as a table
    * @throws WrongParameterTypeException
    *                 if the parameter cannot be returned as a table
    * @throws NullParameterValueException
    *                 if the value is null and it is a mandatory table
    * @throws NullParameterAdditionalInfoException
    *                 if the additional info of the parameter is null
    */
   public abstract ITable getParameterValueAsTable() throws WrongParameterTypeException, NullParameterValueException;


   /**
    * Returns the value of the parameter as a point
    * 
    * @return the value of the parameter as a point
    * @throws WrongParameterTypeException
    *                 if the parameter cannot be returned as a point
    * @throws NullParameterValueException
    *                 if the parameter is null
    */
   public abstract Point2D getParameterValueAsPoint() throws WrongParameterTypeException, NullParameterValueException;


   /**
    * Returns the class of the value represented by this parameter
    * 
    * @return the class of the value represented by this parameter
    */
   public abstract Class getParameterClass();


   /**
    * Returns the value of the parameter as a command-line parameter
    * 
    * @return then value of the parameter as a command-line parameter
    */
   public abstract String getCommandLineParameter();


   /**
    * Serializes the attributes of the parameter that are specific to this kind of parameter
    * 
    * @param serializer
    *                a serializer
    * @throws NullParameterAdditionalInfoException
    * @throws IOException
    */
   protected abstract void serializeAttributes(KXmlSerializer serializer) throws NullParameterAdditionalInfoException,
                                                                         IOException;


   /**
    * Returns the additional information of this parameter
    * 
    * @return the additional information of this parameter
    * @throws NullParameterAdditionalInfoException
    *                 if there is no additional information
    */
   public AdditionalInfo getParameterAdditionalInfo() throws NullParameterAdditionalInfoException {

      if (m_ParameterAdditionalInfo == null) {
         throw new NullParameterAdditionalInfoException();
      }
      else {
         return m_ParameterAdditionalInfo;
      }

   }


   /**
    * Returns the name of the parameter
    * 
    * @return the name (machine-readable) of the parameter
    */
   public String getParameterName() {

      return m_sName;

   }


   /**
    * Returns the description of the parameter
    * 
    * @return the description(human-readable) of the parameter
    */
   public String getParameterDescription() {

      return m_sDescription;

   }


   /**
    * Sets a new description of the parameter
    * 
    * @param sDescription
    *                the description of the parameter
    */
   public void setParameterDescription(final String sDescription) {

      m_sDescription = sDescription;

   }


   /**
    * Returns a string that can be used as a tooltip.
    * 
    * @return the tooltip string
    */
   public String getParameterTooltip() {

      return m_sTooltip;

   }


   /**
    * Returns a string that can be used as a tooltip.
    * 
    * param the tooltip string
    */
   public void setParameterTooltip(final String sTooltip) {

      m_sTooltip = sTooltip;

   }


   /**
    * Sets a new name for this parameter
    * 
    * @param sName
    *                the name of the parameter
    */
   public void setParameterName(final String sName) {

      m_sName = sName;

   }


   /**
    * Returns the value of the parameter, not casting it to any particular type
    * 
    * @return the value of the parameter
    */
   public Object getParameterValueAsObject() {

      return m_ParameterValue;

   }


   /**
    * Returns the value of the parameter as an array list. This is a generic implementation, which simply returns an ArrayList
    * with a single object
    * 
    * @return the value of the a parameter as an array list
    * @throws NullParameterValueException
    */
   public ArrayList getParameterValueAsArrayList() throws NullParameterValueException {

      if (m_ParameterValue != null) {
         final ArrayList array = new ArrayList();
         array.add(m_ParameterValue);

         return array;
      }
      throw new NullParameterValueException();
   }


   /**
    * Returns a text description of the parameter
    * 
    * @see #getParameterTypeName()
    */
   @Override
   public String toString() {

      return getParameterTypeName();

   }


   /**
    * Creates an instance of a parameter from an XML string
    * 
    * @param parser
    *                a parser to take the parameter data from
    * @return a new instance of a parameter
    * @throws XmlPullParserException
    * @throws IOException
    */
   public static Parameter deserialize(final KXmlParser parser) throws XmlPullParserException, IOException {

      Parameter param = null;

      final String sName = parser.getAttributeValue("", NAME);
      final String sDescription = parser.getAttributeValue("", DESCRIPTION);
      final String sClass = parser.getAttributeValue("", CLASS);

      if (sClass.equals(ParameterRasterLayer.class.toString())) {
         param = ParameterRasterLayer.deserialize(parser);
      }
      else if (sClass.equals(ParameterVectorLayer.class.toString())) {
         param = ParameterVectorLayer.deserialize(parser);
      }
      else if (sClass.equals(ParameterTable.class.toString())) {
         param = ParameterTable.deserialize(parser);
      }
      else if (sClass.equals(ParameterBand.class.toString())) {
         param = ParameterBand.deserialize(parser);
      }
      else if (sClass.equals(ParameterBoolean.class.toString())) {
         param = ParameterBoolean.deserialize(parser);
      }
      else if (sClass.equals(ParameterFilepath.class.toString())) {
         param = ParameterFilepath.deserialize(parser);
      }
      else if (sClass.equals(ParameterNumericalValue.class.toString())) {
         param = ParameterNumericalValue.deserialize(parser);
      }
      else if (sClass.equals(ParameterPoint.class.toString())) {
         param = ParameterPoint.deserialize(parser);
      }
      else if (sClass.equals(ParameterSelection.class.toString())) {
         param = ParameterSelection.deserialize(parser);
      }
      else if (sClass.equals(ParameterString.class.toString())) {
         param = ParameterString.deserialize(parser);
      }
      else if (sClass.equals(ParameterTableField.class.toString())) {
         param = ParameterTableField.deserialize(parser);
      }
      else if (sClass.equals(ParameterMultipleInput.class.toString())) {
         param = ParameterMultipleInput.deserialize(parser);
      }
      else if (sClass.equals(ParameterFixedTable.class.toString())) {
         param = ParameterFixedTable.deserialize(parser);
      }
      else {
         return null;
      }

      param.setParameterDescription(sDescription);

      param.setParameterName(sName);

      return param;

   }


   /**
    * Serializes the description of this parameter (not its value)
    * 
    * @param serializer
    *                a serializer
    * @param sCoords
    *                The coords for representing it if it is part of a model
    * @throws IOException
    * @throws NullParameterAdditionalInfoException
    */
   public void serialize(final KXmlSerializer serializer,
                         final String sCoords) throws IOException, NullParameterAdditionalInfoException {

      serializer.text("\n");
      serializer.text("\t\t");
      serializer.startTag(null, INPUT);
      serializer.attribute(null, NAME, m_sName);
      serializer.attribute(null, DESCRIPTION, m_sDescription);
      serializer.attribute(null, TOOLTIP, m_sTooltip);
      serializer.attribute(null, CLASS, this.getClass().toString());
      serializer.attribute(null, COORDS, sCoords);
      serializeAttributes(serializer);
      serializer.text("\n");
      serializer.text("\t\t");
      serializer.endTag(null, INPUT);

   }


   /**
    * Returns a copy of this parameter
    * 
    * @return a copy of this parameter
    */
   public Parameter getNewInstance() {

      Parameter param;
      try {
         param = this.getClass().newInstance();
         param.setParameterDescription(m_sDescription);
         param.setParameterTooltip(m_sTooltip);
         param.setParameterName(m_sName);
         param.setParameterAdditionalInfo(m_ParameterAdditionalInfo);
         param.setParameterValue(m_ParameterValue);

         return param;
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
         return null;
      }

   }


   /**
    * Returns true if the value assigned to this parameter is valid
    * 
    * @return true if the value assigned to this parameter is valid
    */
   public abstract boolean isParameterValueCorrect();


}
