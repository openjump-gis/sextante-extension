

package es.unex.sextante.parameters;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleTableField;
import es.unex.sextante.additionalInfo.AdditionalInfoTableField;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterTypeException;


/**
 * A parameter representing a field in a table or in the attributes table of a vector layer
 * 
 * @author volaya
 * 
 */
public class ParameterMultipleTableField
         extends
            Parameter {

   private static final String PARENT    = "parent";
   private static final String MANDATORY = "mandatory";


   @Override
   public String getParameterTypeName() {

      return "Multiple Table Field";

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


   public ArrayList getParameterValueAsMultipleRasterLayer() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   public ArrayList getParameterValueAsMultipleVectorLayer() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   public ArrayList getParameterValueAsMultipleTable() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public int getParameterValueAsInt() throws WrongParameterTypeException, NullParameterValueException {

      throw new WrongParameterTypeException();

   }


   @Override
   public double getParameterValueAsDouble() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public boolean getParameterValueAsBoolean() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public String getParameterValueAsString() throws WrongParameterTypeException, NullParameterValueException {

      if (m_ParameterValue != null) {
         return ((Integer) m_ParameterValue).toString();
      }
      throw new NullParameterValueException();

   }


   @Override
   public Point2D getParameterValueAsPoint() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public boolean setParameterAdditionalInfo(final AdditionalInfo additionalInfo) {

      if (additionalInfo instanceof AdditionalInfoMultipleTableField) {
         m_ParameterAdditionalInfo = additionalInfo;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public boolean setParameterValue(final Object value) {

      if (value instanceof ArrayList) {
         m_ParameterValue = value;
         return true;
      }
      else {
         return false;
      }

   }


   public boolean setParameterValue(final int iValue) {

      final Integer Value = new Integer(iValue);
      m_ParameterValue = Value;
      return true;

   }


   @Override
   public Class getParameterClass() {

      return Integer.class;

   }


   @Override
   protected void serializeAttributes(final KXmlSerializer serializer) throws NullParameterAdditionalInfoException, IOException {

      final AdditionalInfoTableField aitf = (AdditionalInfoTableField) m_ParameterAdditionalInfo;
      if (aitf != null) {
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, PARENT);
         serializer.attribute(null, VALUE, aitf.getParentParameterName());
         serializer.endTag(null, ATTRIBUTE);
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, MANDATORY);
         serializer.attribute(null, VALUE, Boolean.valueOf(aitf.getIsMandatory()).toString());
         serializer.endTag(null, ATTRIBUTE);
      }
      else {
         throw new NullParameterAdditionalInfoException();
      }

   }


   public static Parameter deserialize(final KXmlParser parser) throws XmlPullParserException, IOException {

      String sParent = null;
      boolean bMandatory = true;

      int tag = parser.nextTag();

      boolean bOver = false;
      while (!bOver) {
         switch (tag) {
            case XmlPullParser.START_TAG:
               if (parser.getName().compareTo(ATTRIBUTE) == 0) {
                  final String sName = parser.getAttributeValue("", NAME);
                  if (sName.compareTo(PARENT) == 0) {
                     sParent = parser.getAttributeValue("", VALUE);
                  }
                  else if (sName.compareTo(PARENT) == 0) {
                     final String sMandatory = parser.getAttributeValue("", VALUE);
                     bMandatory = Boolean.parseBoolean(sMandatory);
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

      final ParameterMultipleTableField param = new ParameterMultipleTableField();
      final AdditionalInfoTableField ai = new AdditionalInfoTableField(sParent, bMandatory);
      param.setParameterAdditionalInfo(ai);

      return param;

   }


   @Override
   public String getCommandLineParameter() {

      final Integer i = (Integer) m_ParameterValue;

      return "\"" + i.toString() + "\"";

   }


   @Override
   public boolean isParameterValueCorrect() {

      return m_ParameterValue != null;

   }

}
