package es.unex.sextante.parameters;

import java.io.IOException;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfo3DRasterLayer;
import es.unex.sextante.additionalInfo.AdditionalInfoDataObject;
import es.unex.sextante.additionalInfo.AdditionalInfoRasterLayer;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterTypeException;

/**
 * A parameter representing a 3D raster layer
 * 
 * @author volaya
 * 
 */
public class Parameter3DRasterLayer
         extends
            ParameterDataObject {


   @Override
   public String getParameterTypeName() {

      return "3D Raster Layer";

   }


   @Override
   public I3DRasterLayer getParameterValueAs3DRasterLayer() throws NullParameterValueException {

      if (m_ParameterValue != null) {
         return (I3DRasterLayer) m_ParameterValue;
      }
      if (((AdditionalInfo3DRasterLayer) m_ParameterAdditionalInfo).getIsMandatory()) {
         throw new NullParameterValueException();
      }
      else {
         return null;
      }

   }


   @Override
   public IRasterLayer getParameterValueAsRasterLayer() throws WrongParameterTypeException {

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
   public boolean setParameterAdditionalInfo(final AdditionalInfo additionalInfo) {

      if (additionalInfo instanceof AdditionalInfoRasterLayer) {
         m_ParameterAdditionalInfo = additionalInfo;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public boolean setParameterValue(final Object value) {

      if (value instanceof I3DRasterLayer) {
         m_ParameterValue = value;
         return true;
      }
      else if (value == null) {
         if (((AdditionalInfo3DRasterLayer) m_ParameterAdditionalInfo).getIsMandatory()) {
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

      return I3DRasterLayer.class;

   }


   public static Parameter deserialize(final KXmlParser parser) throws XmlPullParserException, IOException {

      boolean bMandatory = false;

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

      final Parameter3DRasterLayer param = new Parameter3DRasterLayer();
      final AdditionalInfo3DRasterLayer ai = new AdditionalInfo3DRasterLayer(bMandatory);
      param.setParameterAdditionalInfo(ai);

      return param;

   }


   @Override
   public String getCommandLineParameter() {

      final I3DRasterLayer layer = (I3DRasterLayer) m_ParameterValue;

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
