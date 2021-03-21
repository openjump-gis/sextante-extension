package es.unex.sextante.rasterWrappers;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.dataObjects.IRasterLayer;

/**
 * A grid wrapper that performs interpolation to calculate cell values. This should be used when the window extent does not 'fit'
 * into the structure (coordinates and cellsize) of the grid.
 * 
 * @author Victor Olaya
 * 
 */

public class GridWrapperInterpolated
         extends
            GridWrapper {

   public double m_dCellsize;


   public GridWrapperInterpolated(final IRasterLayer layer,
                                  final AnalysisExtent windowExtent) {

      super(layer, windowExtent);

      m_dCellsize = windowExtent.getCellSize();

   }


   @Override
   public byte getCellValueAsByte(final int x,
                                  final int y) {

      return (byte) getCellValue(x, y, 0);

   }


   @Override
   public byte getCellValueAsByte(final int x,
                                  final int y,
                                  final int band) {

      return (byte) getCellValue(x, y, band);

   }


   @Override
   public short getCellValueAsShort(final int x,
                                    final int y) {

      return (short) getCellValue(x, y, 0);

   }


   @Override
   public short getCellValueAsShort(final int x,
                                    final int y,
                                    final int band) {

      return (short) getCellValue(x, y, band);

   }


   @Override
   public int getCellValueAsInt(final int x,
                                final int y) {

      return (int) getCellValue(x, y, 0);

   }


   @Override
   public int getCellValueAsInt(final int x,
                                final int y,
                                final int band) {

      return (int) getCellValue(x, y, band);

   }


   @Override
   public float getCellValueAsFloat(final int x,
                                    final int y) {

      return (float) getCellValue(x, y, 0);

   }


   @Override
   public float getCellValueAsFloat(final int x,
                                    final int y,
                                    final int band) {

      return (float) getCellValue(x, y, band);

   }


   @Override
   public double getCellValueAsDouble(final int x,
                                      final int y) {

      return getCellValue(x, y, 0);

   }


   @Override
   public double getCellValueAsDouble(final int x,
                                      final int y,
                                      final int band) {

      return getCellValue(x, y, band);

   }


   private double getCellValue(final int x,
                               final int y,
                               final int band) {

      final double dX = m_WindowExtent.getXMin() + m_dCellsize * (x + 0.5);
      final double dY = m_WindowExtent.getYMax() - m_dCellsize * (y + 0.5);

      final double dValue = getValueAt(dX, dY, band);

      return dValue;

   }


}
