package es.unex.sextante.parameters;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.ILayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterTypeException;

/**
 * A parameter representing a multiple input
 * 
 * @author volaya
 * 
 */
public class ParameterMultipleInput
         extends
            Parameter {

   private static final String TYPE      = "type";
   private static final String MANDATORY = "mandatory";


   @Override
   public String getParameterTypeName() {

      return "Multiple Input";

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
   public ArrayList getParameterValueAsArrayList() throws NullParameterValueException {

      if (m_ParameterValue != null) {
         return ((ArrayList) m_ParameterValue);
      }
      throw new NullParameterValueException();

   }


   @Override
   public int getParameterValueAsInt() throws NullParameterValueException {

      if (m_ParameterValue != null) {
         return ((ArrayList) m_ParameterValue).size();
      }
      throw new NullParameterValueException();

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
   public String getParameterValueAsString() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public Point2D getParameterValueAsPoint() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public boolean setParameterAdditionalInfo(final AdditionalInfo additionalInfo) {

      if (additionalInfo instanceof AdditionalInfoMultipleInput) {
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


   @Override
   public Class getParameterClass() {

      return ArrayList.class;

   }


   @Override
   protected void serializeAttributes(final KXmlSerializer serializer) throws NullParameterAdditionalInfoException, IOException {

      final AdditionalInfoMultipleInput aimi = (AdditionalInfoMultipleInput) m_ParameterAdditionalInfo;
      if (aimi != null) {
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, TYPE);
         serializer.attribute(null, VALUE, new Integer(aimi.getDataType()).toString());
         serializer.endTag(null, ATTRIBUTE);
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, MANDATORY);
         serializer.attribute(null, VALUE, Boolean.valueOf(aimi.getIsMandatory()).toString());
         serializer.endTag(null, ATTRIBUTE);
      }
      else {
         throw new NullParameterAdditionalInfoException();
      }

   }


   public static Parameter deserialize(final KXmlParser parser) throws XmlPullParserException, IOException {

      int iType = 0;
      boolean bMandatory = false;

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
                  else if (sName.compareTo(MANDATORY) == 0) {
                     bMandatory = parser.getAttributeValue("", VALUE).equals("true");
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

      final ParameterMultipleInput param = new ParameterMultipleInput();
      final AdditionalInfoMultipleInput ai = new AdditionalInfoMultipleInput(iType, bMandatory);
      param.setParameterAdditionalInfo(ai);

      return param;

   }


   @Override
   public String getCommandLineParameter() {

      Object obj;
      final ArrayList list = (ArrayList) m_ParameterValue;

      if (list.size() > 0) {
         final StringBuffer sb = new StringBuffer("\"");
         for (int i = 0; i < list.size(); i++) {
            obj = list.get(i);
            if (obj instanceof ILayer) {
               sb.append(((ILayer) obj).getName());
            }
            else if (obj instanceof ITable) {
               sb.append(((ITable) obj).getName());
            }
            else {
               sb.append(obj.toString());
            }
            if (i < list.size() - 1) {
               sb.append(", ");
            }
         }
         sb.append("\"");

         return sb.toString();
      }
      else {
         return "\"#\"";
      }

   }


   @Override
   public boolean isParameterValueCorrect() {

      final AdditionalInfoMultipleInput aimi = (AdditionalInfoMultipleInput) m_ParameterAdditionalInfo;
      if (m_ParameterValue == null) {
         return !aimi.getIsMandatory();
      }
      else {
         return true;
      }

   }

}
