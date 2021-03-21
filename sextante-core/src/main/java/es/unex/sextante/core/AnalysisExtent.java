

package es.unex.sextante.core;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.ILayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.rasterWrappers.Grid3DCell;
import es.unex.sextante.rasterWrappers.GridCell;


/**
 * This class defines an output region (extent coordinates and cellsize)
 * 
 * @author Victor Olaya
 * 
 */
public class AnalysisExtent {

   //these values are cell border coordinates, not centered ones
   double m_dXMin      = 0;
   double m_dYMin      = 0;
   double m_dZMin      = 0;
   double m_dXMax      = 0;
   double m_dYMax      = 0;
   double m_dZMax      = 0;
   double m_dCellSize  = 1;
   double m_dCellSizeZ = 1;
   int    m_iNX;
   int    m_iNY;
   int    m_iNZ;


   public AnalysisExtent() {
   }


   /**
    * Creates a new grid extent using the extent of a layer. If it is a raster layer, it will also use its cellsize. If it is a 3d
    * raster layer, it will use its z values
    * 
    * @param layer
    *                a layer
    */
   public AnalysisExtent(final ILayer layer) {

      m_dXMin = layer.getFullExtent().getMinX();
      m_dXMax = layer.getFullExtent().getMaxX();
      m_dYMin = layer.getFullExtent().getMinY();
      m_dYMax = layer.getFullExtent().getMaxY();

      if (layer instanceof IRasterLayer) {
         final IRasterLayer rasterLayer = (IRasterLayer) layer;
         m_dCellSize = rasterLayer.getLayerGridExtent().getCellSize();
         recalculateNXAndNY();
      }
      else if (layer instanceof I3DRasterLayer) {
         final I3DRasterLayer raster3DLayer = (I3DRasterLayer) layer;
         m_dZMin = raster3DLayer.getLayerExtent().getZMin();
         m_dZMax = raster3DLayer.getLayerExtent().getZMax();
         m_dCellSizeZ = raster3DLayer.getCellSizeZ();
         recalculateNZ();
      }

   }


   /**
    * Sets a new range for X coordinates. Coordinates are not center cell ones, but border ones
    * 
    * @param dXMin
    *                the minimum x coordinate of the extent.
    * @param dXMax
    *                the maximum x coordinate of the extent
    * @param bRecalculateWithCellsize
    *                if this parameter is true, the range will be adapted to match the cellsize. Use this when you are working
    *                with raster layers. Make sure that the cellsize has been set in advance.
    */
   public void setXRange(final double dXMin,
                         final double dXMax,
                         final boolean bRecalculateWithCellsize) {

      m_dXMin = Math.min(dXMin, dXMax);
      m_dXMax = Math.max(dXMin, dXMax);
      if (bRecalculateWithCellsize) {
         recalculateNXAndNY();
      }

   }


   /**
    * Sets a new range for Y coordinates. Coordinates are not center cell ones, but border ones
    * 
    * @param dYMin
    *                the minimum Y coordinate of the extent.
    * @param dYMax
    *                the maximum Y coordinate of the extent
    * @param bRecalculateWithCellsize
    *                if this parameter is true, the range will be adapted to match the cellsize. Use this when you are working
    *                with raster layers. Make sure that the cellsize has been set in advance.
    */
   public void setYRange(final double dYMin,
                         final double dYMax,
                         final boolean bRecalculateWithCellsize) {

      m_dYMin = Math.min(dYMin, dYMax);
      m_dYMax = Math.max(dYMin, dYMax);
      if (bRecalculateWithCellsize) {
         recalculateNXAndNY();
      }

   }


   /**
    * Sets a new range for Z coordinates. Coordinates are not center cell ones, but border ones
    * 
    * @param dZMin
    *                the minimum z coordinate of the extent.
    * @param dZMax
    *                the maximum z coordinate of the extent
    * @param bRecalculateWithCellsize
    *                if this parameter is true, the range will be adapted to match the cellsize. Use this when you are working
    *                with raster layers. Make sure that the cellsize has been set in advance.
    */
   public void setZRange(final double dZMin,
                         final double dZMax,
                         final boolean bRecalculateWithCellsize) {

      m_dZMin = Math.min(dZMin, dZMax);
      m_dZMax = Math.max(dZMin, dZMax);
      if (bRecalculateWithCellsize) {
         recalculateNZ();
      }

   }


   /**
    * Returns the cellsize of this extent
    * 
    * @return the cellsize of this extent
    */
   public double getCellSize() {

      return m_dCellSize;

   }


   /**
    * Sets a new cellsize for this extent
    * 
    * @param cellSize
    *                the new cellsize
    */
   public void setCellSize(final double cellSize) {

      m_dCellSize = cellSize;
      recalculateNXAndNY();

   }


   /**
    * Returns the z cellsize of this extent
    * 
    * @return the z cellsize of this extent
    */
   public double getCellSizeZ() {

      return m_dCellSizeZ;

   }


   /**
    * Sets a new z cellsize for this extent
    * 
    * @param cellSize
    *                the new cellsize
    */
   public void setCellSizeZ(final double cellSize) {

      m_dCellSizeZ = cellSize;
      recalculateNZ();

   }


   /**
    * Returns the number of columns in the extent
    * 
    * @return the number of columns
    */
   public int getNX() {

      return m_iNX;

   }


   /**
    * Returns the number of rows in the extent
    * 
    * @return the number of rows
    */
   public int getNY() {

      return m_iNY;

   }


   /**
    * Returns the number of rows in the extent
    * 
    * @return the number of rows
    */
   public int getNZ() {

      return m_iNZ;

   }


   private void recalculateNXAndNY() {

      m_iNY = (int) Math.floor((m_dYMax - m_dYMin) / m_dCellSize);
      m_iNX = (int) Math.floor((m_dXMax - m_dXMin) / m_dCellSize);
      m_dXMax = m_dXMin + m_dCellSize * m_iNX;
      m_dYMax = m_dYMin + m_dCellSize * m_iNY;

   }


   private void recalculateNZ() {

      m_iNZ = (int) Math.floor((m_dZMax - m_dZMin) / m_dCellSizeZ);
      m_dZMax = m_dZMin + m_dCellSizeZ * m_iNZ;

   }


   /**
    * Return the minimum x coordinate of the extent. For raster layers, this is not the coordinate of the center of the left-most
    * cell, but the the coordinate of its left border
    * 
    * @return the minimum x coordinate of the extent
    */
   public double getXMin() {

      return m_dXMin;

   }


   /**
    * Return the maximum x coordinate of the extent. For raster layers, this is not the coordinate of the center of the right-most
    * cell, but the the coordinate of its right border
    * 
    * @return the maximum x coordinate of the extent
    */
   public double getXMax() {

      return m_dXMax;

   }


   /**
    * Return the minimum x coordinate of the extent. For raster layers, this is not the coordinate of the center of the lower
    * cell, but the the coordinate of its lower border
    * 
    * @return the minimum y coordinate of the extent
    */
   public double getYMin() {

      return m_dYMin;

   }


   /**
    * Return the maximum y coordinate of the extent. For raster layers, this is not the coordinate of the center of the upper
    * cell, but the the coordinate of its upper border
    * 
    * @return the maximum y coordinate of the extent
    */
   public double getYMax() {

      return m_dYMax;

   }


   /**
    * Return the minimum z coordinate of the extent. For raster layers, this is not the coordinate of the center of the lower
    * cell, but the the coordinate of its lower border
    * 
    * @return the minimum y coordinate of the extent
    */
   public double getZMin() {

      return m_dZMin;

   }


   /**
    * Return the maximum z coordinate of the extent. For raster layers, this is not the coordinate of the center of the upper
    * cell, but the the coordinate of its upper border
    * 
    * @return the maximum z coordinate of the extent
    */
   public double getZMax() {

      return m_dZMax;

   }


   /**
    * Returns the real X distance spanned by this extent
    * 
    * @return the real X distance spanned by this extent
    */
   public double getWidth() {

      return m_dXMax - m_dXMin;

   }


   /**
    * Returns the real Y distance spanned by this extent
    * 
    * @return the real Y distance spanned by this extent
    */
   public double getLength() {

      return m_dYMax - m_dYMin;

   }


   /**
    * Returns the real Z distance spanned by this extent
    * 
    * @return the real Z distance spanned by this extent
    */
   public double getHeight() {

      return m_dZMax - m_dZMin;

   }


   /**
    * Returns true if the given point falls within the area covered by this extent
    * 
    * @param x
    *                the x coordinate of the point
    * @param y
    *                the y coordinate of the point
    * @return whether the given point falls within the XY area covered by this extent
    */
   public boolean contains(final double x,
                           final double y) {

      return ((x >= m_dXMin) && (x <= m_dXMax) && (y >= m_dYMin) && (y <= m_dYMax));

   }


   /**
    * Returns true if the given point falls within the area covered by this extent
    * 
    * @param x
    *                the x coordinate of the point
    * @param y
    *                the y coordinate of the point
    * @param z
    *                the z coordinate of the point
    * 
    * @return whether the given point falls within the volume covered by this extent
    */
   public boolean contains(final double x,
                           final double y,
                           final double z) {

      return ((x >= m_dXMin) && (x <= m_dXMax) && (y >= m_dYMin) && (y <= m_dYMax) && (x >= m_dZMin) && (x <= m_dZMax));

   }


   /**
    * Returns true if the given extent matches the grid defined by this extent extent (has same size and cell boundaries match)
    * 
    * @param extent
    * @return whether the passed extent fits into this extent
    */
   public boolean fitsIn(final AnalysisExtent extent) {

      boolean bFitsX, bFitsY, bFitsZ;
      double dOffset;
      double dOffsetCols;
      double dOffsetRows;
      double dOffsetZ;
      final double MIN_DIF = 0.00001;

      if (extent.getCellSize() != this.getCellSize()) {
         return false;
      }
      dOffset = Math.abs(extent.getXMin() - this.getXMin());
      dOffsetCols = dOffset / this.getCellSize();
      bFitsX = (dOffsetCols - Math.floor(dOffsetCols + 0.5) < MIN_DIF);

      dOffset = Math.abs(extent.getYMax() - this.getYMax());
      dOffsetRows = dOffset / this.getCellSize();
      bFitsY = (Math.abs(dOffsetRows - Math.floor(dOffsetRows + 0.5)) < MIN_DIF);

      dOffset = Math.abs(extent.getZMax() - this.getZMax());
      dOffsetZ = dOffset / this.getCellSizeZ();
      bFitsZ = (Math.abs(dOffsetZ - Math.floor(dOffsetZ + 0.5)) < MIN_DIF);

      return bFitsX && bFitsY && bFitsZ;

   }


   /**
    * Returns true if this extent has the same characteristics as a given one
    * 
    * @param extent
    * @return whether this extent equals the given extent
    */
   public boolean equals(final AnalysisExtent extent) {

      return (m_dXMin == extent.getXMin()) && (m_dXMax == extent.getXMax()) && (m_dYMin == extent.getYMin())
             && (m_dYMax == extent.getYMax()) && (m_dCellSize == extent.getCellSize()) && (m_dZMin == extent.getZMin())
             && (m_dZMax == extent.getZMax()) && (m_dCellSizeZ == extent.getCellSizeZ());

   }


   /**
    * Modifies this extent to incorporate another one into its boundaries
    * 
    * @param extent
    *                the extent to add
    */
   public void addExtent(final AnalysisExtent extent) {

      m_dXMin = Math.min(extent.getXMin(), m_dXMin);
      m_dXMax = Math.max(extent.getXMax(), m_dXMax);
      m_dYMin = Math.min(extent.getYMin(), m_dYMin);
      m_dYMax = Math.max(extent.getYMax(), m_dYMax);
      m_dZMin = Math.min(extent.getZMin(), m_dZMin);
      m_dZMax = Math.max(extent.getZMax(), m_dZMax);
      m_dCellSize = Math.min(extent.getCellSize(), m_dCellSize);
      m_dCellSizeZ = Math.min(extent.getCellSizeZ(), m_dCellSizeZ);
      recalculateNXAndNY();
      recalculateNZ();

   }


   /**
    * Modifies this extent to incorporate another one into its boundaries
    * 
    * @param extent
    *                the extent to add
    */
   public void addExtent(final Rectangle2D extent) {

      m_dXMin = Math.min(extent.getMinX(), m_dXMin);
      m_dXMax = Math.max(extent.getMaxX(), m_dXMax);
      m_dYMin = Math.min(extent.getMinY(), m_dYMin);
      m_dYMax = Math.max(extent.getMaxY(), m_dYMax);
      recalculateNXAndNY();

   }


   /**
    * Converts a world coordinate to grid coordinates
    * 
    * @param pt
    *                a point in world coordinates
    * @return a grid cell with coordinates of the given point in grid coordinates referred to this grid extent
    */
   public GridCell getGridCoordsFromWorldCoords(final Point2D pt) {

      final int x = (int) Math.floor((pt.getX() - m_dXMin) / m_dCellSize);
      final int y = (int) Math.floor((m_dYMax - pt.getY()) / m_dCellSize);

      final GridCell cell = new GridCell(x, y, 0.0);

      return cell;

   }


   /**
    * Converts a world coordinate to grid coordinates
    * 
    * @param x
    *                the x coordinate of the point
    * @param y
    *                the y coordinate of the point
    * @return a grid cell representing the given point in grid coordinates referred to this grid extent
    */
   public GridCell getGridCoordsFromWorldCoords(final double x,
                                                final double y) {

      return getGridCoordsFromWorldCoords(new Point2D.Double(x, y));

   }


   public Grid3DCell getGridCoordsFromWorldCoords(final double x,
                                                  final double y,
                                                  final double z) {

      final int iX = (int) Math.floor((x - m_dXMin) / m_dCellSize);
      final int iY = (int) Math.floor((m_dYMax - y) / m_dCellSize);
      final int iZ = (int) Math.floor((z - m_dZMin) / m_dCellSizeZ);

      final Grid3DCell cell = new Grid3DCell(iX, iY, iZ, 0.0);

      return cell;

   }


   /**
    * /** Converts a grid cell into a world coordinate representing the center of that cell
    * 
    * @param cell
    *                the cell to convert
    * @return a point representing the given cell in world coordinates
    */
   public Point2D getWorldCoordsFromGridCoords(final GridCell cell) {

      final double x = m_dXMin + (cell.getX() + 0.5) * m_dCellSize;
      final double y = m_dYMax - (cell.getY() + 0.5) * m_dCellSize;

      final Point2D pt = new Point2D.Double(x, y);

      return pt;

   }


   /**
    * Converts a grid cell into a world coordinate representing the center of that cell
    * 
    * @param x
    *                the x coordinate (col) of the cell
    * @param y
    *                the y coordinate (row) of the cell
    * @return a point representing the given cell in world coordinates
    */
   public Point2D getWorldCoordsFromGridCoords(final int x,
                                               final int y) {

      return getWorldCoordsFromGridCoords(new GridCell(x, y, 0));

   }


   /**
    * Converts a grid cell into a world coordinate representing the center of that cell
    * 
    * @param x
    *                the x coordinate of the cell
    * @param y
    *                the y coordinate of the cell
    * @param z
    *                the z coordinate of the cell
    * 
    * @return a coordinate representing the given cell in world coordinates
    */
   public Coordinate getWorldCoordsFromGridCoords(final int x,
                                                  final int y,
                                                  final int z) {

      final double dX = m_dXMin + (x + 0.5) * m_dCellSize;
      final double dY = m_dYMax - (y + 0.5) * m_dCellSize;
      final double dZ = m_dZMin + (y + 0.5) * m_dCellSizeZ;

      final Coordinate coord = new Coordinate(dX, dY, dZ);

      return coord;

   }


   @Override
   public String toString() {

      final String s = Double.toString(m_dXMin) + ", " + Double.toString(m_dYMin) + ", " + Double.toString(m_dZMin) + ", "
                       + Double.toString(m_dXMax) + ", " + Double.toString(m_dYMax) + ", " + Double.toString(m_dZMax) + ", "
                       + Double.toString(m_dCellSize) + ", " + Double.toString(m_dCellSizeZ);

      return s;

   }


   /**
    * Enlarges this grid extent one cell in each direction (only X and Y)
    */
   public void enlargeOneCell() {

      m_dYMin = m_dYMin - m_dCellSize;
      m_dXMin = m_dXMin - m_dCellSize;
      m_dXMax = m_dXMax + m_dCellSize;
      m_dYMax = m_dYMax + m_dCellSize;
      this.recalculateNXAndNY();

   }


   /**
    * Returns this extent as a Java Rectangle2D. Z coordinates are neglected
    * 
    * @return the boundary of this extent a Java Rectangle2D
    */
   public Rectangle2D getAsRectangle2D() {

      final Rectangle2D rect = new Rectangle2D.Double();
      rect.setRect(m_dXMin, m_dYMin, m_dXMax - m_dXMin, m_dYMax - m_dYMin);

      return rect;

   }


   /**
    * Returns true if the cell is within the limits of this grid extent
    * 
    * @param x
    *                the x coordinate (col) of the cell
    * @param y
    *                the y coordinate (row) of the cell
    * @return whether the cell is within the limits of this grid extent
    */
   public boolean containsCell(final int x,
                               final int y) {

      return (x >= 0) && (x < m_iNX) && (y >= 0) && (y < m_iNY);

   }


   /**
    * Return this extent as a JTS Polygon
    * 
    * @return this extent as a JTS polygon
    */
   public Geometry getAsJTSGeometry() {

      final GeometryFactory gf = new GeometryFactory();
      final Coordinate[] coords = new Coordinate[5];
      coords[0] = new Coordinate(m_dXMin, m_dYMin);
      coords[1] = new Coordinate(m_dXMax, m_dYMin);
      coords[2] = new Coordinate(m_dXMax, m_dYMax);
      coords[3] = new Coordinate(m_dXMin, m_dYMax);
      coords[4] = new Coordinate(m_dXMin, m_dYMin);
      final Geometry geom = gf.createPolygon(gf.createLinearRing(coords), null);

      return geom;
   }


}
