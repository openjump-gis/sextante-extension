package es.unex.sextante.outputs;

public class FileOutputChannel
         implements
            IOutputChannel {

   private String m_sFilename;


   public FileOutputChannel(final String sFilename) {

      m_sFilename = sFilename;

   }


   public String getAsCommandLineParameter() {

      if (m_sFilename == null) {
         return "#";
      }
      else {
         return m_sFilename;
      }

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

}
