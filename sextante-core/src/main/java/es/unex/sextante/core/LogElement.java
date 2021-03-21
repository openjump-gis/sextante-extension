package es.unex.sextante.core;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogElement {

   public static final String ERROR   = "ERROR";
   public static final String WARNING = "WARNING";
   public static final String INFO    = "INFO";

   private Date               date;
   private String             text;
   private String             type;
   private final String       shortText;


   public LogElement(final Date date,
                     final String text,
                     final String type) {

      this.date = date;
      this.text = text;
      this.type = type;
      this.shortText = text;

   }


   public LogElement(final Date date,
                     final String text,
                     final String shortText,
                     final String type) {

      this.date = date;
      this.text = text;
      this.type = type;
      this.shortText = shortText;

   }


   public Date getDate() {
      return date;
   }


   public void setDate(final Date date) {
      this.date = date;
   }


   public String getText() {
      return text;
   }


   public void setText(final String text) {
      this.text = text;
   }


   public String getType() {
      return type;
   }


   public void setType(final String type) {
      this.type = type;
   }


   public String getAsText() {

      final SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
      final String sFormattedDate = formatter.format(date);
      return "[" + sFormattedDate + "]-" + type + ":\n" + text + "\n\n";

   }


   @Override
   public String toString() {

      final SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss");
      final String sFormattedDate = formatter.format(date);
      return "[" + sFormattedDate + "]-" + type + ":" + shortText;

   }

}
