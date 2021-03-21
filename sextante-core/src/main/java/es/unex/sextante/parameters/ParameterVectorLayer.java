package es.unex.sextante.parameters;

import java.io.IOException;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoDataObject;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterTypeException;

/**
 * A parameter representing a vector layer
 * 
 * @author volaya
 * 
 */
public class ParameterVectorLayer
         extends
            ParameterDataObject {


   private static final String TYPE = "shape_type";


   @Override
   public String getParameterTypeName() {

      return "Vector Layer";

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
   public IVectorLayer getParameterValueAsVectorLayer() throws NullParameterValueException {

      if (m_ParameterValue != null) {
         return (IVectorLayer) m_ParameterValue;
      }
      if (((AdditionalInfoVectorLayer) m_ParameterAdditionalInfo).getIsMandatory()) {
         throw new NullParameterValueException();
      }
      else {
         return null;
      }
   }


   @Override
   public boolean setParameterAdditionalInfo(final AdditionalInfo additionalInfo) {

      if (additionalInfo instanceof AdditionalInfoVectorLayer) {
         m_ParameterAdditionalInfo = additionalInfo;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public boolean setParameterValue(final Object value) {

      if (value instanceof IVectorLayer) {
         final AdditionalInfoVectorLayer ai = (AdditionalInfoVectorLayer) m_ParameterAdditionalInfo;
         final int iType = ai.getShapeType();
         if ((iType == AdditionalInfoVectorLayer.SHAPE_TYPE_ANY) || (iType == ((IVectorLayer) value).getShapeType())) {
            m_ParameterValue = value;
            return true;
         }
         else {
            return false;
         }
      }
      else if (value == null) {
         if (((AdditionalInfoVectorLayer) m_ParameterAdditionalInfo).getIsMandatory()) {
            return false;
         }
         else {
            m_ParameterValue = null;
            return true;
         }
      }
      else {
         return false;
      }

   }


   @Override
   public Class getParameterClass() {

      return IVectorLayer.class;

   }


   @Override
   protected void serializeAttributes(final KXmlSerializer serializer) throws NullParameterAdditionalInfoException, IOException {

      super.serializeAttributes(serializer);

      final AdditionalInfoVectorLayer aivl = (AdditionalInfoVectorLayer) m_ParameterAdditionalInfo;
      if (aivl != null) {
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, TYPE);
         serializer.attribute(null, VALUE, new Integer(aivl.getShapeType()).toString());
         serializer.endTag(null, ATTRIBUTE);
      }
      else {
         throw new NullParameterAdditionalInfoException();
      }

   }


   public static Parameter deserialize(final KXmlParser parser) throws XmlPullParserException, IOException {

      boolean bMandatory = false;
      int iShapeType = 0;

      int tag = parser.nextTag();

      boolean bOver = false;
      while (!bOver) {
         switch (tag) {
            case XmlPullParser.START_TAG:
               if (parser.getName().compareTo(ATTRIBUTE) == 0) {
                  final String sName = parser.getAttributeValue("", NAME);
                  if (sName.compareTo(MANDATORY) == 0) {
                     bMandatory = parser.getAttributeValue("", VALUE).equals("true");
                  }
                  else if (sName.compareTo(TYPE) == 0) {
                     iShapeType = Integer.parseInt(parser.getAttributeValue("", VALUE));
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


      final ParameterVectorLayer param = new ParameterVectorLayer();
      final AdditionalInfoVectorLayer ai = new AdditionalInfoVectorLayer(iShapeType, bMandatory);
      param.setParameterAdditionalInfo(ai);

      return param;

   }


   @Override
   public String getCommandLineParameter() {

      final IVectorLayer layer = (IVectorLayer) m_ParameterValue;

      if (layer == null) {
         return "\"#\"";
      }
      else {
         return "\"" + layer.getName() + "\"";
      }

   }


   @Override
   public boolean isParameterValueCorrect() {

      final AdditionalInfoDataObject aido = (AdditionalInfoDataObject) m_ParameterAdditionalInfo;
      if (m_ParameterValue == null) {
         return !aido.getIsMandatory();
      }
      else {
         return true;
      }

   }

}
