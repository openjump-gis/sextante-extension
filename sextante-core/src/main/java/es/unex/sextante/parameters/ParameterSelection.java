package es.unex.sextante.parameters;

import java.awt.geom.Point2D;
import java.io.IOException;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoSelection;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterTypeException;

/**
 * A parameter representing a selection from a list of possible values
 * 
 * @author volaya
 * 
 */
public class ParameterSelection
         extends
            Parameter {

   private static final String OPTIONS = "options";


   @Override
   public String getParameterTypeName() {

      return "Selection";

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
         return ((Integer) m_ParameterValue).intValue();
      }
      throw new NullParameterValueException();

   }


   @Override
   public double getParameterValueAsDouble() throws WrongParameterTypeException, NullParameterValueException {

      if (m_ParameterValue != null) {
         return ((Integer) m_ParameterValue).doubleValue();
      }
      throw new NullParameterValueException();

   }


   @Override
   public boolean getParameterValueAsBoolean() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public String getParameterValueAsString() throws WrongParameterTypeException, NullParameterValueException,
                                            NullParameterAdditionalInfoException {

      if (m_ParameterValue != null) {
         if (m_ParameterAdditionalInfo != null) {
            final String[] sValues = ((AdditionalInfoSelection) m_ParameterAdditionalInfo).getValues();
            return (sValues[((Integer) m_ParameterValue).intValue()]);
         }
         else {
            throw new NullParameterAdditionalInfoException();
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

      if (additionalInfo instanceof AdditionalInfoSelection) {
         m_ParameterAdditionalInfo = additionalInfo;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public boolean setParameterValue(final Object value) {

      if (value instanceof Integer) {
         final String[] sValues = ((AdditionalInfoSelection) m_ParameterAdditionalInfo).getValues();
         if (((Integer) value).intValue() < sValues.length) {
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

      return Integer.class;

   }


   @Override
   protected void serializeAttributes(final KXmlSerializer serializer) throws NullParameterAdditionalInfoException, IOException {

      final AdditionalInfoSelection ais = (AdditionalInfoSelection) m_ParameterAdditionalInfo;
      if (ais != null) {
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         final String[] sFields = ais.getValues();
         String s = sFields[0];
         for (int i = 1; i < sFields.length; i++) {
            s = s + ";" + sFields[i];
         }
         serializer.attribute(null, NAME, OPTIONS);
         serializer.attribute(null, VALUE, s);
         serializer.endTag(null, ATTRIBUTE);
         serializer.text("\n");
      }
      else {
         throw new NullParameterAdditionalInfoException();
      }

   }


   public static Parameter deserialize(final KXmlParser parser) throws XmlPullParserException, IOException {

      String sValues = null;

      int tag = parser.nextTag();

      boolean bOver = false;
      while (!bOver) {
         switch (tag) {
            case XmlPullParser.START_TAG:
               if (parser.getName().compareTo(ATTRIBUTE) == 0) {
                  final String sName = parser.getAttributeValue("", NAME);
                  if (sName.compareTo(OPTIONS) == 0) {
                     sValues = parser.getAttributeValue("", VALUE);
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

      final ParameterSelection param = new ParameterSelection();
      final AdditionalInfoSelection ai = new AdditionalInfoSelection(sValues.split("\\;"));
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
