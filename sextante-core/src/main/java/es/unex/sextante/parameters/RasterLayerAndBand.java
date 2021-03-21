package es.unex.sextante.parameters;

import es.unex.sextante.dataObjects.IRasterLayer;

/**
 * This class represents a raster layer and one of its bands
 *
 * @author volaya
 *
 */
public class RasterLayerAndBand {

   private IRasterLayer m_RasterLayer;
   private int          m_iBand;


   public RasterLayerAndBand(final IRasterLayer layer,
                             final int iBand) {

      m_RasterLayer = layer;
      m_iBand = iBand;

   }


   public int getBand() {

      return m_iBand;

   }


   public void setBand(final int band) {

      m_iBand = band;

   }


   public IRasterLayer getRasterLayer() {

      return m_RasterLayer;
   }


   public void setRasterLayer(final IRasterLayer rasterLayer) {

      m_RasterLayer = rasterLayer;

   }


   @Override
   public String toString() {

      return m_RasterLayer.getName() + "," + Integer.toString(m_iBand + 1);

   }

}
