package es.unex.sextante.rasterWrappers;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.dataObjects.IRasterLayer;

/**
 * A grid wrapper that does not perform interpolation to calculate cell values. This should be used when the window extent 'fits'
 * into the structure (coordinates and cellsize) of the grid, so it is faster than using a grid wrapper with interpolation
 * 
 * Upon construction, cellsizes are not checked, so they are assumed to be equal. Use a QueryableGridWindow to safely create a
 * GridWrapper better than instantiating this class directly.
 * 
 * @author Victor Olaya
 * 
 */

public class GridWrapperNotInterpolated
         extends
            GridWrapper {


   public GridWrapperNotInterpolated(final IRasterLayer layer,
                                     final AnalysisExtent windowExtent) {

      super(layer, windowExtent);

      calculateOffsets();

   }


   protected void calculateOffsets() {

      //		double dMinX, dMaxY;
      //		int iWindowMinX, iWindowMinY;

      final AnalysisExtent layerExtent = m_Layer.getLayerGridExtent();

      m_iOffsetX = (int) ((m_WindowExtent.getXMin() - layerExtent.getXMin()) / m_WindowExtent.getCellSize());
      m_iOffsetY = (int) ((layerExtent.getYMax() - m_WindowExtent.getYMax()) / m_WindowExtent.getCellSize());

      //		dMinX = Math.min(Math.max(m_WindowExtent.getXMin(), layerExtent.getXMin()), layerExtent.getXMax());
      //		//dMinY = Math.min(Math.max(m_WindowExtent.getYMin(), layerExtent.getYMin()), layerExtent.getYMax());
      //		dMaxY = Math.max(Math.min(m_WindowExtent.getYMax(), layerExtent.getYMax()), layerExtent.getYMin());
      //
      //		m_iMinX = (int) Math.floor((dMinX - layerExtent.getXMin()) / m_WindowExtent.getCellSize());
      //		m_iMinY = (int) Math.floor((layerExtent.getYMax() - dMaxY) / m_WindowExtent.getCellSize());
      //
      //		m_iOffsetX = m_iMinX - iWindowMinX;
      //		m_iOffsetY = m_iMinY - iWindowMinY;

   }


   @Override
   public byte getCellValueAsByte(final int x,
                                  final int y) {

      return (byte) getCellValueInLayerCoords(x + m_iOffsetX, y + m_iOffsetY, 0);

   }


   @Override
   public byte getCellValueAsByte(final int x,
                                  final int y,
                                  final int band) {

      return (byte) getCellValueInLayerCoords(x + m_iOffsetX, y + m_iOffsetY, band);

   }


   @Override
   public short getCellValueAsShort(final int x,
                                    final int y) {

      return (short) getCellValueInLayerCoords(x + m_iOffsetX, y + m_iOffsetY, 0);

   }


   @Override
   public short getCellValueAsShort(final int x,
                                    final int y,
                                    final int band) {

      return (short) getCellValueInLayerCoords(x + m_iOffsetX, y + m_iOffsetY, band);

   }


   @Override
   public int getCellValueAsInt(final int x,
                                final int y) {

      return (int) getCellValueInLayerCoords(x + m_iOffsetX, y + m_iOffsetY, 0);

   }


   @Override
   public int getCellValueAsInt(final int x,
                                final int y,
                                final int band) {

      return (int) getCellValueInLayerCoords(x + m_iOffsetX, y + m_iOffsetY, band);

   }


   @Override
   public float getCellValueAsFloat(final int x,
                                    final int y) {

      return (float) getCellValueInLayerCoords(x + m_iOffsetX, y + m_iOffsetY, 0);

   }


   @Override
   public float getCellValueAsFloat(final int x,
                                    final int y,
                                    final int band) {

      return (float) getCellValueInLayerCoords(x + m_iOffsetX, y + m_iOffsetY, band);

   }


   @Override
   public double getCellValueAsDouble(final int x,
                                      final int y) {

      return getCellValueInLayerCoords(x + m_iOffsetX, y + m_iOffsetY, 0);

   }


   @Override
   public double getCellValueAsDouble(final int x,
                                      final int y,
                                      final int band) {

      return getCellValueInLayerCoords(x + m_iOffsetX, y + m_iOffsetY, band);

   }

}
