package es.unex.sextante.gui.history;

import java.text.DateFormat;
import java.util.Date;

import es.unex.sextante.core.Sextante;

public class DateAndCommand {

   private static long MILLISECS_PER_DAY = 1000 * 60 * 60 * 24;

   private Date        date;
   private String      command;


   public String getDay() {

      if (date.getTime() > History.getSessionStartingTime()) {
         return Sextante.getText("This_session");
      }

      final int iDayA = (int) ((System.currentTimeMillis()) / MILLISECS_PER_DAY);
      final int iDayB = (int) ((date.getTime()) / MILLISECS_PER_DAY);
      final int iDays = iDayA - iDayB;

      if (iDays == 0) {
         return Sextante.getText("Today");
      }
      else if (iDays == 1) {
         return Sextante.getText("Yesterday");
      }
      else if (iDays < 30) {
         String s = Sextante.getText("XXX_days_ago");
         s = s.replace("XXX", Integer.toString(iDays));
         return s;
      }
      else {
         return Sextante.getText("More_than_one_month_ago");
      }
   }


   @Override
   public String toString() {

      final DateFormat formatter = DateFormat.getDateTimeInstance();
      final String sDate = formatter.format(date);
      final String s = "[" + sDate + "] " + command;

      return s;

   }


   public Date getDate() {

      return date;

   }


   public void setDate(final Date date) {

      this.date = date;

   }


   public String getCommand() {

      return command;

   }


   public void setCommand(final String command) {

      this.command = command;

   }


   public String getAsFullText() {

      return toString();//Change THIS!!!

   }


}
