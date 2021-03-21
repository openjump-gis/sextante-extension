package es.unex.sextante.docEngines.html;

/**
 * A class to create simple HTML-formatted texts. Use this to create HTML results from SEXTANTE algorithms
 * 
 * @author volaya
 * 
 */
public class HTMLDoc {

   private final String OPENING_HTML_CODE_1 = "<html>\n" + "<head>" + "<title>";

   private final String OPENING_HTML_CODE_2 = "</title>\n" + "</head>\n" + "<body bgcolor=\"#FFFFFF\" text=\"#000000\">\n";

   private final String CLOSING_HTML_CODE   = "</body>\n" + "</html>";

   private StringBuffer m_sHTMLCode;


   /**
    * Opens the HTML page with the given title
    * 
    * @param sTitle
    *                the title of the page
    */
   public void open(final String sTitle) {

      m_sHTMLCode = new StringBuffer("");

      m_sHTMLCode.append(OPENING_HTML_CODE_1);
      m_sHTMLCode.append(sTitle);
      m_sHTMLCode.append(OPENING_HTML_CODE_2);

   }


   /**
    * Closes the HTML page
    */
   public void close() {

      m_sHTMLCode.append(CLOSING_HTML_CODE);

   }


   /**
    * Returns the page as a HTML-formatted string
    * 
    * @return the page as a HTML-formatted string
    */
   public String getHTMLCode() {

      return m_sHTMLCode.toString();

   }


   /**
    * adds the given text. HTML tags can be included
    * 
    * @param sText
    *                the text to add
    */
   public void addText(final String sText) {

      m_sHTMLCode.append(sText);

   }


   /**
    * Adds the given text in bold font
    * 
    * @param sText
    *                the text to add
    */
   public void addBoldText(final String sText) {

      m_sHTMLCode.append("<b>");
      m_sHTMLCode.append(sText);
      m_sHTMLCode.append("</b>");

   }


   /**
    * Adds a new paragraph
    * 
    * @param sText
    *                the text of the paragraph
    */
   public void addParagraph(final String sText) {

      m_sHTMLCode.append("<p align=\"left\">");
      m_sHTMLCode.append(sText);
      m_sHTMLCode.append("</p>\n");

   }


   /**
    * Adds a line break
    */
   public void addLineBreak() {

      m_sHTMLCode.append("</br>");

   }


   /**
    * Adds a header
    * 
    * @param sText
    *                the text of the header
    * @param iOrder
    *                the order (importance) of the header (1,2,3...)
    */
   public void addHeader(final String sText,
                         final int iOrder) {

      m_sHTMLCode.append("<h");
      m_sHTMLCode.append(iOrder);
      m_sHTMLCode.append(" align=\"left\">");
      m_sHTMLCode.append(sText);
      m_sHTMLCode.append("</h");
      m_sHTMLCode.append(iOrder);
      m_sHTMLCode.append(">\n");

   }


   /**
    * Adds a hyperlink
    * 
    * @param sText
    *                the text of the link
    * @param sURL
    *                the URL to link to
    */
   public void addHyperlink(final String sText,
                            final String sURL) {

      m_sHTMLCode.append("<a href=\"");
      m_sHTMLCode.append(sURL);
      m_sHTMLCode.append("\">\n");
      m_sHTMLCode.append(sText);
      m_sHTMLCode.append("\n</a>");

   }


   /**
    * Adds a horizontal separator
    */
   public void addHorizontalSeparator() {

      m_sHTMLCode.append("<p><hr width=\"80%\"></p>");

   }


   /**
    * Adds an image
    * 
    * @param sFilename
    *                the filename of the image
    */
   public void addImage(final String sFilename) {

      m_sHTMLCode.append("<img src=\"");
      m_sHTMLCode.append(sFilename);
      m_sHTMLCode.append("\">\n");

   }


   public void addThumbnail(final String sFilename,
                            final int iWidth,
                            final boolean bIsPercent) {

      m_sHTMLCode.append("<a href=\"");
      m_sHTMLCode.append(sFilename);
      m_sHTMLCode.append("\">\n");
      m_sHTMLCode.append("<img src=\"");
      m_sHTMLCode.append(sFilename);
      m_sHTMLCode.append("\" width=");
      m_sHTMLCode.append(iWidth);
      if (bIsPercent) {
         m_sHTMLCode.append("%");
      }
      m_sHTMLCode.append("></a><br><br>\n");

   }


   /**
    * Starts an unordered list
    */
   public void startUnorderedList() {

      m_sHTMLCode.append("<ul>\n");

   }


   /**
    * Starts an ordered list
    */
   public void startOrderedList() {

      m_sHTMLCode.append("<ol>\n");

   }


   /**
    * Closes an unordered list
    */
   public void closeUnorderedList() {

      m_sHTMLCode.append("</ul>\n");

   }


   /**
    * Closes an ordered list
    */
   public void closeOrderedList() {

      m_sHTMLCode.append("</ol>\n");

   }


   /**
    * Adds an element to the last opened list (ordered or unordered)
    * 
    * @param sText
    *                the text of the element
    */
   public void addListElement(final String sText) {

      m_sHTMLCode.append("<li>");
      m_sHTMLCode.append(sText);
      m_sHTMLCode.append("</li>\n");

   }


   /**
    * Creates an ordered list from a set of elements
    * 
    * @param sText
    *                an array of string, each of them representing one element of the list
    */
   public void addOrderedList(final String[] sText) {

      startOrderedList();
      for (int i = 0; i < sText.length; i++) {
         addListElement(sText[i]);
      }
      closeOrderedList();

   }


   /**
    * Creates an unordered list from a set of elements
    * 
    * @param sText
    *                an array of string, each of them representing one element of the list
    */
   public void addUnorderedList(final String[] sText) {

      startUnorderedList();
      for (int i = 0; i < sText.length; i++) {
         addListElement(sText[i]);
      }
      closeUnorderedList();

   }


   /**
    * Adds a table
    * 
    * @param Table
    *                The table as a 2D array of strings
    * @param sDescription
    *                The description of the table
    * @param bColorFirstRow
    *                true if should give a different color to the first row
    * @param bColorFirstCol
    *                true if should give a different color to the first columns
    */
   public void addTable(final String[][] Table,
                        final String sDescription,
                        final boolean bColorFirstRow,
                        final boolean bColorFirstCol) {

      int i, j;
      int iRows, iCols;
      int iWidth;

      iRows = Table.length;
      if (iRows > 0) {
         iCols = Table[0].length;
      }
      else {
         return;
      }

      iWidth = (100 / iCols);

      m_sHTMLCode.append("<table width=\"99%\" style=\"background-color:transparent;\" "
                         + "border=0 cellspacing=0 cellpadding=2 >\n");

      for (i = 0; i < iRows; i++) {
         if ((i == 0) && bColorFirstRow) {
            m_sHTMLCode.append("<tr bgcolor=\"#CCCCCC\">\n");
         }
         else {
            m_sHTMLCode.append("<tr>\n");
         }
         for (j = 0; j < iCols; j++) {
            m_sHTMLCode.append("<td width=\"");
            m_sHTMLCode.append(iWidth);
            m_sHTMLCode.append("%\"");
            if ((j == 0) & bColorFirstCol) {
               m_sHTMLCode.append("bgcolor=\"#CCCCCC\"");
            }
            m_sHTMLCode.append("align=\"center\">");
            try {
               m_sHTMLCode.append(Table[i][j]);
            }
            catch (final Exception e) {}
            m_sHTMLCode.append("</td>");
         }
         m_sHTMLCode.append("\n</tr>\n");
      }

      m_sHTMLCode.append("\n</table>\n");
      m_sHTMLCode.append("<p align=\"center\"><i>");
      m_sHTMLCode.append(sDescription);
      m_sHTMLCode.append("</i></p>\n");

   }


   /**
    * Adds an image an a caption describing it
    * 
    * @param sImageFile
    *                the image file
    * @param sDescription
    *                the caption
    */
   public void addImageAndDescription(final String sImageFile,
                                      final String sDescription) {

      m_sHTMLCode.append("<p><center>\n");
      m_sHTMLCode.append("<img src=\"");
      m_sHTMLCode.append(sImageFile);
      m_sHTMLCode.append("\"><br>\n");
      m_sHTMLCode.append(sDescription);
      m_sHTMLCode.append("\n</center></p>\n");

   }


   /**
    * Adds text in courier font
    * 
    * @param sText
    *                the text to add
    */
   public void addCourierText(final String sText) {

      m_sHTMLCode.append("<font face=\"courier\">");
      m_sHTMLCode.append(sText);
      m_sHTMLCode.append("</font>");

   }


}
