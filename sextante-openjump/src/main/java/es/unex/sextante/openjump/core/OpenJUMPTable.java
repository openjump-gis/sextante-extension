package es.unex.sextante.openjump.core;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.geotools.dbffile.DbfFieldDef;
import org.geotools.dbffile.DbfFile;
import org.geotools.dbffile.DbfFileWriter;

import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;

import es.unex.sextante.dataObjects.AbstractTable;
import es.unex.sextante.dataObjects.IRecordsetIterator;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;

/**
 * A class implementing the ITable interface, based on a OpenJUMP FeatureCollection
 * 
 * @author volaya
 * 
 */
public class OpenJUMPTable extends AbstractTable {

   /**
    * The name of the table
    */
   private String         m_sName;

   /**
    * The filename associated with this table
    */
   private String         m_sFilename;

   private FeatureDataset m_FC;


   public void create(final FeatureCollection featureCollection) {

      m_FC = (FeatureDataset) featureCollection;
      m_sName = "";
      m_sFilename = "";

   }


   public void create(final String name,
                      final Class<?>[] types,
                      final String[] fields,
                      final String filename) {


      final FeatureSchema schema = new FeatureSchema();
      for (int i = 0; i < fields.length; i++) {
         schema.addAttribute(fields[i], AttributeType.toAttributeType(types[i]));
      }
      m_FC = new FeatureDataset(schema);
      m_sFilename = filename;
      m_sName = name;

   }


   public void addRecord(final Object[] attributes) {

      if (m_FC != null) {
         final FeatureDataset fc = m_FC;
         final Feature feature = new BasicFeature(fc.getFeatureSchema());
         feature.setAttributes(attributes.clone());
         fc.add(feature);
      }

   }


   public int getFieldCount() {

      if (m_FC != null) {
         return m_FC.getFeatureSchema().getAttributeCount();
      }
      else {
         return 0;
      }

   }


   public String getFieldName(final int iIndex) {

      if (m_FC != null) {
         return m_FC.getFeatureSchema().getAttributeName(iIndex);
      }
      else {
         return null;
      }

   }


   public Class<?> getFieldType(final int iIndex) {

      if (m_FC != null) {
         return m_FC.getFeatureSchema().getAttributeType(iIndex).toJavaClass();
      }
      else {
         return null;
      }

   }


   public long getRecordCount() {

      if (m_FC != null) {
         return m_FC.size();
      }
      else {
         return 0;
      }

   }


   public OpenJUMPRecordsetIterator iterator() {

      if (m_FC != null) {
         return new OpenJUMPRecordsetIterator(m_FC.iterator());
      }
      else {
         return null;
      }

   }


   public void close() {}


   public String getFilename() {

      return m_sFilename;

   }


   public String getName() {

      return m_sName;

   }


   public void open() {}


   public void postProcess() throws Exception {

      if (m_FC != null) {

         final FeatureSchema fs = m_FC.getFeatureSchema();

         final DbfFieldDef[] fields = new DbfFieldDef[fs.getAttributeCount()];

         // dbf column type and size
         int f = 0;

         for (int t = 0; t < fs.getAttributeCount(); t++) {
            final AttributeType columnType = fs.getAttributeType(t);
            final String columnName = fs.getAttributeName(t);

            if (columnType == AttributeType.INTEGER) {
               fields[f] = new DbfFieldDef(columnName, 'N', 16, 0);
               f++;
            }
            else if (columnType == AttributeType.DOUBLE) {
               fields[f] = new DbfFieldDef(columnName, 'N', 33, 16);
               f++;
            }
            else if (columnType == AttributeType.STRING) {
               final int maxlength = findMaxStringLength(m_FC, t);
               fields[f] = new DbfFieldDef(columnName, 'C', maxlength, 0);
               f++;
            }
            else if (columnType == AttributeType.DATE) {
               fields[f] = new DbfFieldDef(columnName, 'D', 8, 0);
               f++;
            }
         }
         // write header
         final DbfFileWriter dbf = new DbfFileWriter(m_sFilename);
         dbf.writeHeader(fields, m_FC.size());

         //write rows
         final int num = m_FC.size();

         final List<Feature> features = m_FC.getFeatures();

         for (int t = 0; t < num; t++) {
            final Feature feature = features.get(t);
            final Vector<Object> DBFrow = new Vector<>();
            //make data for each column in this feature (row)
            for (int u = 0; u < fs.getAttributeCount(); u++) {
               final AttributeType columnType = fs.getAttributeType(u);

               if (columnType == AttributeType.INTEGER) {
                  final Object a = feature.getAttribute(u);

                  if (a == null) {
                     DBFrow.add(0);
                  }
                  else {
                     DBFrow.add(a);
                  }
               }
               else if (columnType == AttributeType.DOUBLE) {
                  final Object a = feature.getAttribute(u);

                  if (a == null) {
                     DBFrow.add(0.0);
                  }
                  else {
                     DBFrow.add(a);
                  }
               }
               else if (columnType == AttributeType.DATE) {
                  final Object a = feature.getAttribute(u);
                  if (a == null) {
                     DBFrow.add("");
                  }
                  else {
                     DBFrow.add(DbfFile.DATE_PARSER.format((Date) a));
                  }
               }
               else if (columnType == AttributeType.STRING) {
                  final Object a = feature.getAttribute(u);

                  if (a == null) {
                     DBFrow.add("");
                  }
                  else {
                     // MD 16 jan 03 - added some defensive programming
                     if (a instanceof String) {
                        DBFrow.add(a);
                     }
                     else {
                        DBFrow.add(a.toString());
                     }
                  }
               }
            }

            dbf.writeRecord(DBFrow);
         }

         dbf.close();
      }

   }


   /**
    * look at all the data in the column of the featurecollection, and find the largest string!
    * 
    * @param fc features to look at
    * @param attributeNumber
    *                which of the column to test.
    */
   int findMaxStringLength(final FeatureCollection fc,
                           final int attributeNumber) {
      int l;
      int maxlen = 0;

      for (Feature f : fc.getFeatures()) {
         l = f.getString(attributeNumber).length();
         if (l > maxlen) {
            maxlen = l;
         }
      }

      return maxlen;
   }


   public void setName(final String sName) {

      m_sName = sName;

   }


   public void free() {}


   public Object getBaseDataObject() {

      return m_FC;

   }


   public IOutputChannel getOutputChannel() {

      return new FileOutputChannel(m_sFilename);
   }

}
