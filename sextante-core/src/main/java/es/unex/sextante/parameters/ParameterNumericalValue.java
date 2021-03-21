package es.unex.sextante.parameters;

import java.awt.geom.Point2D;
import java.io.IOException;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterTypeException;

/**
 * A parameter representing a numerical value
 * 
 * @author volaya
 * 
 */
public class ParameterNumericalValue
         extends
            Parameter {

   private static final String MAX     = "max";
   private static final String TYPE    = "type";
   private static final String DEFAULT = "default";
   private static final String MIN     = "min";


   @Override
   public String getParameterTypeName() {

      return "Numerical Value";

   }


   @Override
   public IRasterLayer getParameterValueAsRasterLayer() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public I3DRasterLayer getParameterValueAs3DRasterLayer() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public ITable getParameterValueAsTable() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public IVectorLayer getParameterValueAsVectorLayer() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public int getParameterValueAsInt() throws WrongParameterTypeException, NullParameterValueException {

      if (m_ParameterValue != null) {
         return ((Number) m_ParameterValue).intValue();
      }
      throw new NullParameterValueException();

   }


   @Override
   public double getParameterValueAsDouble() throws WrongParameterTypeException, NullParameterValueException {

      if (m_ParameterValue != null) {
         return ((Number) m_ParameterValue).doubleValue();
      }
      throw new NullParameterValueException();

   }


   @Override
   public boolean getParameterValueAsBoolean() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public String getParameterValueAsString() throws WrongParameterTypeException, NullParameterValueException {

      if (m_ParameterValue != null) {
         final Number n = (Number) m_ParameterValue;
         if (((AdditionalInfoNumericalValue) m_ParameterAdditionalInfo).getType() == AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER) {
            return Integer.toString(n.intValue());
         }
         else {
            return n.toString();
         }
      }
      throw new NullParameterValueException();

   }


   @Override
   public Point2D getParameterValueAsPoint() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public boolean setParameterAdditionalInfo(final AdditionalInfo additionalInfo) {

      if (additionalInfo instanceof AdditionalInfoNumericalValue) {
         m_ParameterAdditionalInfo = additionalInfo;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public boolean setParameterValue(final Object value) {

      if (value instanceof Number) {
         final Number number = (Number) value;
         final double dValue = number.doubleValue();
         final AdditionalInfoNumericalValue ainv = (AdditionalInfoNumericalValue) m_ParameterAdditionalInfo;
         if ((dValue >= ainv.getMinValue()) && (dValue <= ainv.getMaxValue())) {
            m_ParameterValue = value;
            return true;
         }
         else {
            return false;
         }
      }
      else {
         return false;
      }

   }


   @Override
   public Class getParameterClass() {

      return Number.class;
   }


   @Override
   protected void serializeAttributes(final KXmlSerializer serializer) throws NullParameterAdditionalInfoException, IOException {

      final AdditionalInfoNumericalValue ainv = (AdditionalInfoNumericalValue) m_ParameterAdditionalInfo;
      if (ainv != null) {
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, MIN);
         serializer.attribute(null, VALUE, new Double(ainv.getMinValue()).toString());
         serializer.endTag(null, ATTRIBUTE);
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, MAX);
         serializer.attribute(null, VALUE, new Double(ainv.getMaxValue()).toString());
         serializer.endTag(null, ATTRIBUTE);
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, DEFAULT);
         serializer.attribute(null, VALUE, new Double(ainv.getDefaultValue()).toString());
         serializer.endTag(null, ATTRIBUTE);
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, TYPE);
         serializer.attribute(null, VALUE, new Integer(ainv.getType()).toString());
         serializer.endTag(null, ATTRIBUTE);
      }
      else {
         throw new NullParameterAdditionalInfoException();
      }

   }


   public static Parameter deserialize(final KXmlParser parser) throws XmlPullParserException, IOException {

      int iType = 0;
      double dMin = Double.MAX_VALUE;
      double dMax = Double.NEGATIVE_INFINITY;
      double dDefault = 0;

      int tag = parser.nextTag();

      boolean bOver = false;
      while (!bOver) {
         switch (tag) {
            case XmlPullParser.START_TAG:
               if (parser.getName().compareTo(ATTRIBUTE) == 0) {
                  final String sName = parser.getAttributeValue("", NAME);
                  if (sName.compareTo(TYPE) == 0) {
                     iType = Integer.parseInt(parser.getAttributeValue("", VALUE));
                  }
                  if (sName.compareTo(MIN) == 0) {
                     dMin = Double.parseDouble(parser.getAttributeValue("", VALUE));
                  }
                  if (sName.compareTo(MAX) == 0) {
                     dMax = Double.parseDouble(parser.getAttributeValue("", VALUE));
                  }
                  if (sName.compareTo(DEFAULT) == 0) {
                     dDefault = Double.parseDouble(parser.getAttributeValue("", VALUE));
                  }
               }
               break;
            case XmlPullParser.END_TAG:
               if (parser.getName().compareTo(INPUT) == 0) {
                  bOver = true;
               }
               break;
            case XmlPullParser.TEXT:
               break;
         }

         if (!bOver) {
            tag = parser.next();
         }

      }

      final ParameterNumericalValue param = new ParameterNumericalValue();
      final AdditionalInfoNumericalValue ai = new AdditionalInfoNumericalValue(iType, dDefault, dMin, dMax);
      param.setParameterAdditionalInfo(ai);

      return param;

   }


   @Override
   public String getCommandLineParameter() {

      final Number n = (Number) m_ParameterValue;

      if (((AdditionalInfoNumericalValue) m_ParameterAdditionalInfo).getType() == AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER) {
         return "\"" + Integer.toString(n.intValue()) + "\"";
      }
      else {
         return "\"" + n.toString() + "\"";
      }


   }


   @Override
   public boolean isParameterValueCorrect() {

      return true;

   }

}
