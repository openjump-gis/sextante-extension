/*******************************************************************************
EqualLengthVisibilitySegmentQueue.java
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

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;

/**
 * EqualLengthVisibilitySegmentQueue acts as a queue which holds two line of sights of a cross section. Upon requesting the next
 * segments, the queue breaks the two LOSs to equal length segments such that each LOS segment has a uniform visibility. This
 * class is used therefore to compare the XOR difference between LOSs and interpolate thier cross section visibility polygons.
 *
 */
class EqualLengthVisibilitySegmentQueue
         extends
            LinkedList<VisibilityPair> {
   private static final long        serialVersionUID = 728269644204953666L;
   private final Deque<VisibilitySegment> m_firstROS;
   private final Deque<VisibilitySegment> m_secondROS;
   private final double                   m_cellSize;


   public EqualLengthVisibilitySegmentQueue(final List<Coordinate> firstROS,
                                            final List<Coordinate> secondROS,
                                            final double cellSize) {
      m_cellSize = cellSize;

      // Convert range of sights from grid cells to visibility segments
      m_firstROS = new VisibilitySegmentQueue(firstROS);
      m_secondROS = new VisibilitySegmentQueue(secondROS);
   }


   @Override
   public boolean isEmpty() {
      // Empty if both LOSs are empty
      return m_firstROS.isEmpty() && m_secondROS.isEmpty();
   }


   @Override
   public VisibilityPair poll() {
      // Get the two next LOSs segments. If they are of different length break them to same
      // length and insert back the leftover part

      final VisibilitySegment firstROSSegment = m_firstROS.poll();
      final VisibilitySegment secondROSSegment = m_secondROS.poll();

      // If the range-of-sights have different overall length (due to no-data values,
      // for example) returned the missing segment as null
      if (firstROSSegment == null || secondROSSegment == null) {
         return new VisibilityPair(firstROSSegment, secondROSSegment);
      }

      // Lengths of segments
      final double firstSegmentLength = firstROSSegment.getLength();
      final double secondSegmentLength = secondROSSegment.getLength();

      final double segmentComparison = firstSegmentLength - secondSegmentLength;

      // Case 1: the two segments are of the same length or they are the last segments
      if (Math.abs(segmentComparison) < m_cellSize || (m_firstROS.isEmpty() && m_secondROS.isEmpty())) {
         return new VisibilityPair(firstROSSegment, secondROSSegment);
      }

      // Case 2: segments are of different length
      VisibilitySegment longSegment;
      double lengthRatio;
      Deque<VisibilitySegment> longSegmentQueue;
      boolean isFirstROSShorter;

      // Find shorter segment
      if (segmentComparison < 0) {
         isFirstROSShorter = true;
         lengthRatio = firstSegmentLength / secondSegmentLength;
         longSegment = secondROSSegment;
         longSegmentQueue = m_secondROS;
      }
      else {
         isFirstROSShorter = false;
         lengthRatio = secondSegmentLength / firstSegmentLength;
         longSegment = firstROSSegment;
         longSegmentQueue = m_firstROS;
      }

      // Divide the longer segment to two parts, the first being the same length as the
      // shorter segment
      final Coordinate dividingPoint = longSegment.pointAlong(lengthRatio);
      final VisibilitySegment longSegmentFirstPart = new VisibilitySegment(longSegment.p0, dividingPoint, longSegment.getVisibility());
      final VisibilitySegment longSegmentSecondPart = new VisibilitySegment(dividingPoint, longSegment.p1, longSegment.getVisibility());

      // Add to the longer segment iterator the part of the segment not covered
      // by the shorter segment to be processed on next iteration
      longSegmentQueue.push(longSegmentSecondPart);

      // Return the ROS pair of equal lengths
      if (isFirstROSShorter) {
         return new VisibilityPair(firstROSSegment, longSegmentFirstPart);
      }
      else {
         return new VisibilityPair(longSegmentFirstPart, secondROSSegment);
      }
   }

   private class VisibilitySegmentQueue
            extends
               LinkedList<VisibilitySegment> {
      private static final long        serialVersionUID = 6999971040518772075L;
      private final Iterator<Coordinate>     m_inputPoints;
      private final Deque<VisibilitySegment> m_addedSegments  = new LinkedList<VisibilitySegment>();
      private Coordinate               m_startPoint;


      public VisibilitySegmentQueue(final List<Coordinate> input) {
         m_inputPoints = input.iterator();
         m_startPoint = m_inputPoints.next();
      }


      @Override
      public boolean isEmpty() {
         // There is a next segment if there is a next point or segments were added
         return m_addedSegments.isEmpty() && !m_inputPoints.hasNext();
      }


      @Override
      public int size() {
         // Unsupported
         return 0;
      }


      @Override
      public void push(final VisibilitySegment segment) {
         m_addedSegments.push(segment);
      }


      @Override
      public VisibilitySegment poll() {

         // Return added segments if any
         if (!m_addedSegments.isEmpty()) {
            final VisibilitySegment segment = m_addedSegments.poll();
            return segment;
         }

         if (!m_inputPoints.hasNext()) {
            return null;
         }

         // Find the next point with visibility different from the last point
         // We assume there is a next point (at least one), as isEmpty returned false
         // and the ROS filtered no data cells
         Coordinate nextPoint;
         do {
            nextPoint = m_inputPoints.next();
         }
         while (m_inputPoints.hasNext() && nextPoint.z == m_startPoint.z);

         // Construct segment between start and end points
         final Coordinate[] lineSegmentCoords = new Coordinate[2];
         lineSegmentCoords[0] = m_startPoint;
         lineSegmentCoords[1] = nextPoint;

         final int segmentVisibility = (int) m_startPoint.z;

         // Update last cell to point to the start of the next segment
         m_startPoint = nextPoint;

         return new VisibilitySegment(lineSegmentCoords[0], lineSegmentCoords[1], segmentVisibility);
      }
   }
}
