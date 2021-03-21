package es.unex.sextante.gui.help;

import java.io.IOException;
import java.util.ArrayList;

import org.kxml2.io.KXmlSerializer;

public class HelpElement {

   private String             m_sName              = "";
   private ArrayList          m_Images             = new ArrayList();
   private String             m_sDescription       = "";
   private String             m_sText              = "";
   private int                m_iType              = 0;

   public static final int    TYPE_ADDITIONAL_INFO = 0;
   public static final int    TYPE_INPUT           = 1;
   public static final int    TYPE_OUTPUT          = 2;
   public static final int    TYPE_PARAMETER       = 3;

   public static final String ELEMENT              = "element";
   public static final String NAME                 = "name";
   public static final String TEXT                 = "text";
   public static final String TYPE                 = "type";
   public static final String DESCRIPTION          = "description";


   public HelpElement(final String sName,
                      final String sDescription,
                      final int iType) {

      m_sName = sName;
      m_sDescription = sDescription;
      m_iType = iType;

   }


   public HelpElement() {

   }


   public ArrayList getImages() {

      return m_Images;

   }


   public void setImages(final ArrayList images) {

      m_Images = images;

   }


   public String getText() {

      return m_sText;

   }


   public String getTextAsFormattedHTML() {

      boolean bList = false;
      boolean bListElement = false;
      final StringBuffer sb = new StringBuffer();
      for (int i = 0; i < m_sText.length(); i++) {
         final char c = m_sText.charAt(i);
         if (c == '*') {
            if (!bList) {
               sb.append("<ul>");
            }
            sb.append("<li>");
            bListElement = true;
            bList = true;
         }
         else if (c == '\n') {
            sb.append("<br>");
            bListElement = false;
         }
         else {
            if (bList && !bListElement) {
               sb.append("</ul>");
               bList = false;
            }
            sb.append(c);
         }
      }
      sb.append("\n");

      if (bList) {
         sb.append("</ul>");
      }

      return sb.toString();

   }


   public String getTextForTooltip() {

      final int MAX_CHARS = 60;

      int iChars = 0;
      boolean bList = false;
      boolean bListElement = false;
      final StringBuffer sb = new StringBuffer("<html>");
      for (int i = 0; i < m_sText.length(); i++) {
         final char c = m_sText.charAt(i);
         if (c == '*') {
            if (!bList) {
               sb.append("<ul>");
            }
            sb.append("<li>");
            bListElement = true;
            bList = true;
         }
         else if (c == '\n') {
            sb.append("<br>");
            bListElement = false;
         }
         else if (c == ' ') {
            sb.append(c);
            if (iChars > MAX_CHARS) {
               iChars = 0;
               sb.append("<br>");
            }
            else {
               iChars++;
            }
         }
         else {
            if (bList && !bListElement) {
               sb.append("</ul>");
               bList = false;
            }
            sb.append(c);
         }
      }
      sb.append("\n</html>");

      if (bList) {
         sb.append("</ul>");
      }

      return sb.toString();


   }


   public void setText(final String text) {

      m_sText = text;

   }


   public String getDescription() {

      return m_sDescription;

   }


   public void setDescription(final String description) {

      m_sDescription = description;

   }


   public String getName() {

      return m_sName;

   }


   public void setName(final String name) {

      m_sName = name;

   }


   public int getType() {

      return m_iType;

   }


   public void setType(final int type) {

      m_iType = type;

   }


   @Override
   public String toString() {

      return m_sDescription;
   }


   public void serialize(final KXmlSerializer serializer) throws IOException {

      serializer.text("\n");
      serializer.text("\t\t");
      serializer.startTag(null, ELEMENT);
      serializer.attribute(null, NAME, m_sName);
      serializer.attribute(null, TEXT, m_sText);
      serializer.attribute(null, DESCRIPTION, m_sDescription);
      serializer.attribute(null, TYPE, Integer.toString(m_iType));
      for (int i = 0; i < m_Images.size(); i++) {
         ((ImageAndDescription) m_Images.get(i)).serialize(serializer);
      }
      serializer.text("\n");
      serializer.text("\t\t");
      serializer.endTag(null, ELEMENT);

   }


}
