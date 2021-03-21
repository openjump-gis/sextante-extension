/*******************************************************************************
RangeOfSight.java
Copyright (C) Aviad Segev, 2010

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *******************************************************************************/

package es.unex.sextante.lighting.viewshed;

import java.util.LinkedList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.rasterWrappers.GridCell;

public class RangeOfSight {

   public static final double VISIBLE   = 255.0;
   public static final double HIDDEN    = 0.0;
   public static final double UNDEFINED = -1.0;

   private final IRasterLayer       m_dem;
   private final double             m_objectHeights;


   public RangeOfSight(final IRasterLayer dem,
                       final double objectHeights) {
      m_dem = dem;
      m_objectHeights = objectHeights;
   }


   public LinkedList<GridCell> Calculate(final GridCell sourceCell,
                                         final GridCell targetCell,
                                         boolean skipNoData) {
      double dx, dy;
      double ix, iy;
      double dMaxSlope = Double.MAX_VALUE;

      final LinkedList<GridCell> ros = new LinkedList<GridCell>();
      // Source cell always sees itself
      ros.add(new GridCell(sourceCell.getX(), sourceCell.getY(), VISIBLE));

      dx = targetCell.getX() - sourceCell.getX();
      dy = targetCell.getY() - sourceCell.getY();

      double d = Math.abs(dx) > Math.abs(dy) ? Math.abs(dx) : Math.abs(dy);

      if (d == 0) {
         return ros;
      }

      final double dist = Math.sqrt(dx * dx + dy * dy);

      dx /= d;
      dy /= d;

      d = dist / d;

      // Align sampled point to the center of the cell
      ix = sourceCell.getX() + 0.5;
      iy = sourceCell.getY() + 0.5;
      // Source cell should always have value
      if (m_dem.isNoDataValue(sourceCell.getValue())) {
         return ros;
      }

      double id = 0.0;
      while (id < dist) {
         id += d;

         ix += dx;
         iy += dy;

         final GridCell currCell = new GridCell((int) ix, (int) iy, m_dem.getCellValueAsDouble((int) ix, (int) iy));

         // If cell has no value mark it as one
         if (m_dem.isNoDataValue(currCell.getValue())) {
            if (!skipNoData) {
               ros.add(new GridCell(currCell.getX(), currCell.getY(), m_dem.getNoDataValue()));
            }
            continue;
         }

         // Slope between source and current cell (with added height)
         final double heightedSlope = (currCell.getValue() + m_objectHeights - sourceCell.getValue()) / id;

         // First cell is always visible to source. It's slope is the maximum slope
         // for the source to see other cells
         if (dMaxSlope == Double.MAX_VALUE) {
            ros.add(new GridCell(currCell.getX(), currCell.getY(), VISIBLE));
         }
         else if (heightedSlope <= dMaxSlope) {
            // If slope below maximum visible slope than cell is hidden
            ros.add(new GridCell(currCell.getX(), currCell.getY(), HIDDEN));
         }
         else {
            // If slope is above maximum visible slope than cell is visible
            // and its slope  is now the maximum visible slope
            ros.add(new GridCell(currCell.getX(), currCell.getY(), VISIBLE));
         }

         // Actual slope between source and current cell (no added height)
         // Update maximum visible slope
         final double realSlope = (currCell.getValue() - sourceCell.getValue()) / id;
         if (dMaxSlope == Double.MAX_VALUE || realSlope > dMaxSlope) {
            dMaxSlope = realSlope;
         }
      }

      return ros;
   }


   public LinkedList<Coordinate> Calculate(final Coordinate source,
                                           final Coordinate target) {

      double maxSlope = Double.MAX_VALUE;

      final LinkedList<Coordinate> ros = new LinkedList<Coordinate>();

      // Source point always sees itself
      ros.add(new Coordinate(source.x, source.y, VISIBLE));

      // Source cell should always have value
      if (m_dem.isNoDataValue(source.z)) {
         return ros;
      }

      final LineSegment los = new LineSegment(source, target);
      final double rosAngle = los.angle();
      final double losCosAngle = Math.cos(rosAngle);
      final double losSinAngle = Math.sin(rosAngle);
      final double losDistance = los.getLength();

      final int xCellCount = (int) ((target.x - source.x) / m_dem.getLayerCellSize());
      final int yCellCount = (int) ((target.y - source.y) / m_dem.getLayerCellSize());

      final int maxCellCount = Math.max(Math.abs(xCellCount), Math.abs(yCellCount));
      if (maxCellCount == 0) {
         return ros;
      }

      final double cellDistance = losDistance / maxCellCount;

      double currDistance = 0.0;
      while (currDistance < losDistance) {
         currDistance += cellDistance;

         final Coordinate currLOSPoint = new Coordinate(source.x + currDistance * losCosAngle, source.y + currDistance * losSinAngle);
         currLOSPoint.z = m_dem.getValueAt(currLOSPoint.x, currLOSPoint.y);

         // Skip no data points
         if (m_dem.isNoDataValue(currLOSPoint.z)) {
            continue;
         }

         // Slope between source and current point
         final double slope = (currLOSPoint.z - source.z) / currDistance;

         // First point is always visible to source. It's slope is the maximum slope
         // for the source to see other cells
         if (maxSlope == Double.MAX_VALUE) {
            maxSlope = slope;
            ros.add(new Coordinate(currLOSPoint.x, currLOSPoint.y, VISIBLE));
         }
         else if (slope <= maxSlope) {
            // If slope below maximum visible slope than cell is hidden
            ros.add(new Coordinate(currLOSPoint.x, currLOSPoint.y, HIDDEN));
         }
         else {
            // If slope is above maximum visible slope than cell is visible
            // and its slope  is now the maximum visible slope
            maxSlope = slope;
            ros.add(new Coordinate(currLOSPoint.x, currLOSPoint.y, VISIBLE));
         }
      }

      return ros;
   }
}
