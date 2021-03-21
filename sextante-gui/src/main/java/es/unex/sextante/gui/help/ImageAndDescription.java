package es.unex.sextante.gui.help;

import java.io.IOException;

import org.kxml2.io.KXmlSerializer;

public class ImageAndDescription {

   private String             m_sFilename;
   private String             m_sDescription;

   public static final String DESCRIPTION = "description";
   public static final String FILE        = "file";
   public static final String IMAGE       = "image";


   public String getDescription() {

      return m_sDescription;

   }


   public void setDescription(final String description) {

      m_sDescription = description;

   }


   public String getFilename() {

      return m_sFilename;

   }


   public void setFilename(final String filename) {

      m_sFilename = filename;

   }


   @Override
   public String toString() {

      return m_sFilename;

   }


   public void serialize(final KXmlSerializer serializer) throws IOException {

      serializer.text("\n");
      serializer.text("\t\t\t");
      serializer.startTag(null, IMAGE);
      serializer.attribute(null, DESCRIPTION, m_sDescription);
      serializer.attribute(null, FILE, m_sFilename);
      serializer.text("\n");
      serializer.text("\t\t\t");
      serializer.endTag(null, IMAGE);

   }

}
