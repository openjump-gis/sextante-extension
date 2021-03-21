package es.unex.sextante.core;

import java.awt.geom.Rectangle2D;

/**
 * A class representing an extent with an associated description
 *
 * @author volaya
 *
 */
public class NamedExtent {

   private Rectangle2D m_Extent;
   private String      m_sName;


   public NamedExtent(final String sName,
                      final Rectangle2D extent) {

      m_sName = sName;
      m_Extent = extent;

   }


   public Rectangle2D getExtent() {

      return m_Extent;

   }


   public void setExtent(final Rectangle2D extent) {

      m_Extent = extent;

   }


   public String getsName() {

      return m_sName;

   }


   public void setName(final String sName) {

      m_sName = sName;

   }


   @Override
   public String toString() {

      return m_sName;

   }

}
