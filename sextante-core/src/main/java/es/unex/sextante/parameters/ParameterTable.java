package es.unex.sextante.parameters;

import java.io.IOException;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoDataObject;
import es.unex.sextante.additionalInfo.AdditionalInfoTable;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterTypeException;

/**
 * A parameter representing a table
 * 
 * @author volaya
 * 
 */
public class ParameterTable
         extends
            ParameterDataObject {

   @Override
   public String getParameterTypeName() {

      return "Table";

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
   public ITable getParameterValueAsTable() throws WrongParameterTypeException, NullParameterValueException {

      if (m_ParameterValue != null) {
         return (ITable) m_ParameterValue;
      }
      if (((AdditionalInfoTable) m_ParameterAdditionalInfo).getIsMandatory()) {
         throw new NullParameterValueException();
      }
      else {
         return null;
      }

   }


   @Override
   public IVectorLayer getParameterValueAsVectorLayer() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public boolean setParameterAdditionalInfo(final AdditionalInfo additionalInfo) {

      if (additionalInfo instanceof AdditionalInfoTable) {
         m_ParameterAdditionalInfo = additionalInfo;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public boolean setParameterValue(final Object value) {

      if (value instanceof ITable) {
         m_ParameterValue = value;
         return true;
      }
      else if (value == null) {
         if (((AdditionalInfoTable) m_ParameterAdditionalInfo).getIsMandatory()) {
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

      return ITable.class;
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

      final ParameterTable param = new ParameterTable();
      final AdditionalInfoTable ai = new AdditionalInfoTable(bMandatory);
      param.setParameterAdditionalInfo(ai);

      return param;

   }


   @Override
   public String getCommandLineParameter() {

      final ITable table = (ITable) m_ParameterValue;

      if (table == null) {
         return "\"#\"";
      }
      else {
         return "\"" + table.getName() + "\"";
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
