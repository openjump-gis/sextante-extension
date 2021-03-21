

package es.unex.sextante.parameters;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import es.unex.sextante.additionalInfo.AdditionalInfo;
import es.unex.sextante.additionalInfo.AdditionalInfoFixedTable;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.WrongParameterTypeException;


/**
 * A parameter representing a fixed table
 * 
 * @author volaya
 * 
 */
public class ParameterFixedTable
         extends
            Parameter {

   private static final String ROWS      = "rows";
   private static final String FIXED     = "fixed";
   private static final String COL_NAMES = "col_names";


   @Override
   public String getParameterTypeName() {

      return "Fixed Table";

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
   public String getParameterValueAsString() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public Point2D getParameterValueAsPoint() throws WrongParameterTypeException {

      throw new WrongParameterTypeException();

   }


   @Override
   public boolean setParameterAdditionalInfo(final AdditionalInfo additionalInfo) {

      if (additionalInfo instanceof AdditionalInfoFixedTable) {
         m_ParameterAdditionalInfo = additionalInfo;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public boolean setParameterValue(final Object value) {

      if (value instanceof FixedTableModel) {
         m_ParameterValue = value;
         return true;
      }
      else if (value instanceof String) {
         final AdditionalInfoFixedTable ai = (AdditionalInfoFixedTable) m_ParameterAdditionalInfo;
         final int iCols = ai.getColsCount();
         final String string = (String) value;
         final String[] tokens = string.split(",");
         final boolean bFixed = ai.isNumberOfRowsFixed();
         final int iRows = tokens.length / iCols;
         if (tokens.length % iCols != 0) {
            return false;
         }
         if (bFixed && (tokens.length != iCols * iRows)) {
            return false;
         }
         final ArrayList[] list = new ArrayList[iCols];
         for (int i = 0; i < list.length; i++) {
            list[i] = new ArrayList();
         }
         for (int i = 0; i < iRows; i++) {
            for (int j = 0; j < iCols; j++) {
               list[j].add(new Double(tokens[j + i * iCols].trim()));
            }
         }
         final FixedTableModel ftm = new FixedTableModel(ai.getCols(), iRows, bFixed);
         ftm.setData(list);
         m_ParameterValue = ftm;
         return true;
      }
      else {
         return false;
      }

   }


   @Override
   public Class getParameterClass() {

      return FixedTableModel.class;

   }


   @Override
   protected void serializeAttributes(final KXmlSerializer serializer) throws NullParameterAdditionalInfoException, IOException {

      final AdditionalInfoFixedTable aift = (AdditionalInfoFixedTable) m_ParameterAdditionalInfo;
      if (aift != null) {
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, ROWS);
         serializer.attribute(null, VALUE, new Integer(aift.getRowsCount()).toString());
         serializer.endTag(null, ATTRIBUTE);
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.text("\n");
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         serializer.attribute(null, NAME, FIXED);
         serializer.attribute(null, VALUE, Boolean.valueOf(aift.isNumberOfRowsFixed()).toString());
         serializer.endTag(null, ATTRIBUTE);
         serializer.text("\t\t\t");
         serializer.startTag(null, ATTRIBUTE);
         final String[] sFields = aift.getCols();
         String s = sFields[0];
         for (int i = 1; i < sFields.length; i++) {
            s = s + ";" + sFields[i];
         }
         serializer.attribute(null, NAME, COL_NAMES);
         serializer.attribute(null, VALUE, s);
         serializer.text("\n");
         serializer.endTag(null, ATTRIBUTE);
      }
      else {
         throw new NullParameterAdditionalInfoException();
      }

   }


   public static Parameter deserialize(final KXmlParser parser) throws XmlPullParserException, IOException {

      boolean bFixed = false;
      int iRows = 0;
      String sColNames = null;

      int tag = parser.nextTag();

      boolean bOver = false;
      while (!bOver) {
         switch (tag) {
            case XmlPullParser.START_TAG:
               if (parser.getName().compareTo(ATTRIBUTE) == 0) {
                  final String sName = parser.getAttributeValue("", NAME);
                  if (sName.compareTo(ROWS) == 0) {
                     iRows = Integer.parseInt(parser.getAttributeValue("", VALUE));
                  }
                  if (sName.compareTo(FIXED) == 0) {
                     bFixed = parser.getAttributeValue("", VALUE).equals("true");
                  }
                  if (sName.compareTo(COL_NAMES) == 0) {
                     sColNames = parser.getAttributeValue("", VALUE);
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

      final ParameterFixedTable param = new ParameterFixedTable();
      final AdditionalInfoFixedTable ai = new AdditionalInfoFixedTable(sColNames.split("\\;"), iRows, bFixed);
      param.setParameterAdditionalInfo(ai);

      return param;

   }


   @Override
   public String getCommandLineParameter() {

      final FixedTableModel model = (FixedTableModel) m_ParameterValue;

      int i, j;
      final StringBuffer sb = new StringBuffer("\"");

      for (i = 0; i < model.getRowCount(); i++) {
         for (j = 0; j < model.getColumnCount(); j++) {
            sb.append(model.getValueAt(i, j) + ",");
         }
      }
      sb.deleteCharAt(sb.length() - 1);
      sb.append("\"");

      return sb.toString();

   }


   @Override
   public boolean isParameterValueCorrect() {

      return (m_ParameterValue != null);

   }

}
