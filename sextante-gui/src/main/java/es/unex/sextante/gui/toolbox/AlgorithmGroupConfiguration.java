package es.unex.sextante.gui.toolbox;

public class AlgorithmGroupConfiguration {

   private String  m_sGroup;
   private String  m_sSubgroup;
   private boolean m_bShow;


   public String getGroup() {
      return m_sGroup;
   }


   public void setGroup(final String group) {
      m_sGroup = group;
   }


   public String getSubgroup() {
      return m_sSubgroup;
   }


   public void setSubgroup(final String subgroup) {
      m_sSubgroup = subgroup;
   }


   public boolean isShow() {
      return m_bShow;
   }


   public void setShow(final boolean show) {
      m_bShow = show;
   }


   @Override
   public String toString() {

      return new Boolean(m_bShow).toString() + "|" + m_sGroup + "|" + m_sSubgroup;

   }


   public static AlgorithmGroupConfiguration fromString(final String s) {

      try {
         final AlgorithmGroupConfiguration conf = new AlgorithmGroupConfiguration();
         final String[] sTokens = s.split("|");
         conf.m_bShow = Boolean.parseBoolean(sTokens[0]);
         conf.m_sGroup = sTokens[1];
         conf.m_sSubgroup = sTokens[2];
         return conf;
      }
      catch (final Exception e) {
         return null;
      }

   }

}
