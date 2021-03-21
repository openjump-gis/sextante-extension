

package es.unex.sextante.gui.core;

import javax.swing.ImageIcon;


public class NameAndIcon {

   private String    m_sName;
   private ImageIcon m_Icon;


   public NameAndIcon(final String sName,
                      final ImageIcon icon) {

      m_sName = sName;
      m_Icon = icon;

   }


   public String getName() {
      return m_sName;
   }


   public void setName(final String name) {
      m_sName = name;
   }


   public ImageIcon getIcon() {
      return m_Icon;
   }


   public void setIcon(final ImageIcon icon) {
      m_Icon = icon;
   }


   @Override
   public String toString() {
      return m_sName;
   }


}
