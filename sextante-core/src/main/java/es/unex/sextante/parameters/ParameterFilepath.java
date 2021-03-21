package es.unex.sextante.parameters;

import java.awt.geom.Point2D;
import java.io.IOException;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoFilepath;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.NullParameterValueException;
import es.unex.sextante.exceptions.WrongParameterTypeException;

/**
 * A parameter representing a filepath
 * 
 * @author volaya
 * 
 */
public class ParameterFilepath
         extends
            Parameter {

   private static final String OPENDIALOG = "isopendialog";
   private static final String FOLDER     = "folder";
   private static final String EXTENSION  = "extension";


   @Override
   public String getParameterTypeName() {

      return "Filepath";

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
   public int getParameterValueAsInt() throws WrongParameterTypeException {

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
         return (String) m_ParameterValue;
      }
      throw new NullParameterValueException();

   }


   @Override
   public Point2D getParameterValueAsPoint() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public boolean setParameterAdditionalInfo(final AdditionalInfo additionalInfo) {

      if (additionalInfo instanceof AdditionalInfoFilepath) {
         m_ParameterAdditionalInfo = additionalInfo;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public boolean setParameterValue(final Object value) {

      if (value instanceof String) {
         m_ParameterValue = value;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public Class getParameterClass() {

      return String.class;

   }


   @Override
   protected void serializeAttributes(final KXmlSerializer serializer) throws NullParameterAdditionalInfoException, IOException {

      final AdditionalInfoFilepath aifp = (AdditionalInfoFilepath) m_ParameterAdditionalInfo;
      if (aifp != null) {
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, FOLDER);
         serializer.attribute(null, VALUE, (Boolean.valueOf(aifp.isFolder())).toString());
         serializer.endTag(null, ATTRIBUTE);
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, OPENDIALOG);
         serializer.attribute(null, VALUE, (new Boolean(aifp.isOpenDialog())).toString());
         serializer.endTag(null, ATTRIBUTE);
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, EXTENSION);
         final StringBuffer exts = new StringBuffer();
         final String[] sExts = aifp.getExtensions();
         for (int i = 0; i < sExts.length; i++) {
            exts.append(sExts[i]);
            if (i < sExts.length - 1) {
               exts.append(",");
            }
         }
         serializer.attribute(null, VALUE, exts.toString());
         serializer.endTag(null, ATTRIBUTE);
      }
      else {
         throw new NullParameterAdditionalInfoException();
      }

   }


   public static Parameter deserialize(final KXmlParser parser) throws XmlPullParserException, IOException {

      boolean bFolder = true;
      boolean bOpenDialog = true;
      String sExt[] = null;

      int tag = parser.nextTag();

      boolean bOver = false;
      while (!bOver) {
         switch (tag) {
            case XmlPullParser.START_TAG:
               if (parser.getName().compareTo(ATTRIBUTE) == 0) {
                  final String sName = parser.getAttributeValue("", NAME);
                  if (sName.compareTo(EXTENSION) == 0) {
                     sExt = parser.getAttributeValue("", VALUE).split(",");
                  }
                  else if (sName.compareTo(FOLDER) == 0) {
                     bFolder = parser.getAttributeValue("", VALUE).equals("true");
                  }
                  else if (sName.compareTo(OPENDIALOG) == 0) {
                     bOpenDialog = parser.getAttributeValue("", VALUE).equals("true");
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

      final ParameterFilepath param = new ParameterFilepath();
      final AdditionalInfoFilepath ai = new AdditionalInfoFilepath(bFolder, bOpenDialog, sExt);
      param.setParameterAdditionalInfo(ai);

      return param;

   }


   @Override
   public String getCommandLineParameter() {

      final String s = (String) m_ParameterValue;

      return "\"" + s + "\"";

   }


   @Override
   public boolean isParameterValueCorrect() {

      return true;

   }

}
