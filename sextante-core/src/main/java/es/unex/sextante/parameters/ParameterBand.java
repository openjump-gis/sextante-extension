package es.unex.sextante.parameters;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoBand;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterTypeException;

/**
 * A parameter representing a band in a raster layer
 * 
 * @author volaya
 * 
 */
public class ParameterBand
         extends
            Parameter {

   private static final String PARENT = "parent";


   @Override
   public String getParameterTypeName() {

      return "Band";

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

      if (m_ParameterValue != null) {
         return ((Integer) m_ParameterValue).intValue();
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

      if (additionalInfo instanceof AdditionalInfoBand) {
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

      final AdditionalInfoBand aib = (AdditionalInfoBand) m_ParameterAdditionalInfo;
      if (aib != null) {
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, PARENT);
         serializer.attribute(null, VALUE, aib.getParentParameterName());
         serializer.endTag(null, ATTRIBUTE);
      }
      else {
         throw new NullParameterAdditionalInfoException();
      }

   }


   public static Parameter deserialize(final KXmlParser parser) throws XmlPullParserException, IOException {

      String sParent = null;

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

      final ParameterBand param = new ParameterBand();
      final AdditionalInfoBand ai = new AdditionalInfoBand(sParent);
      param.setParameterAdditionalInfo(ai);

      return param;

   }


   @Override
   public String getCommandLineParameter() {

      final Integer I = (Integer) m_ParameterValue;

      final int i = I.intValue() + 1;

      return Integer.toString(i);

   }


   @Override
   public boolean isParameterValueCorrect() {

      return (m_ParameterValue != null);

   }

}
