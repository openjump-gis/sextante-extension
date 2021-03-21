/*******************************************************************************
ApproximatedViewshedAlgorithm.java
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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateArrays;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Polygon;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

/**
 * Aproximated Viewshed algorithm. Computes visibility region of a watcher using similarity thresholds and interpolations to
 * reduce the number of computed line-of-sights. Based on the paper "Approximating the visible region of a point on a terrain' by
 * B. Ben-Moshe, P. Carmi, and M.J. Katz (http://cg.scs.carleton.ca/~paz/papers/radar5-1-04.pdf)
 * 
 */
public class ApproximatedViewshedAlgorithm
         extends
            GeoAlgorithm {

   public static final String DEM              = "DEM";
   public static final String POINT            = "POINT";
   public static final String HEIGHT           = "HEIGHT";
   public static final String HEIGHTOBS        = "HEIGHTOBS";
   public static final String RADIUS           = "RADIUS";
   public static final String THRESHOLD        = "THRESHOLD";
   public static final String RESULT           = "RESULT";
   public static final String VISIBLE_FIELD    = "Visible";

   private IRasterLayer       m_DEM            = null;
   private IVectorLayer       m_visibilityLayer;
   private double             m_watcherHeight, m_objectsHeight;
   double                     m_threshold;
   private int                m_searchRadius;
   private double             m_worldSearchRadius;
   private Coordinate         m_watcherPoint;
   private GeometryFactory    m_geoFactory;

   // Angles of interpolated cross sections.
   // Goes from 0.0 (nothing done) to 2*Math.PI (all done)
   private double             m_finishedAngles = 0.0;
   private AtomicInteger      m_taskCount;

   private ExecutorService    m_executor;
   private double             m_minAllowedAngle;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Approximated_Viewshed"));
      setGroup(Sextante.getText("Visibility_and_lighting"));

      try {
         // The DEM
         m_Parameters.addInputRasterLayer(DEM, Sextante.getText("Elevation"), true);

         // Watcher location
         m_Parameters.addPoint(POINT, Sextante.getText("Coordinates_of_emitter-receiver"));

         // Height of watcher
         m_Parameters.addNumericalValue(HEIGHT, Sextante.getText("Height_of_emitter-receiver"), 10,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);

         // Height of watched objects
         m_Parameters.addNumericalValue(HEIGHTOBS, Sextante.getText("Height_of_mobile_receiver-emitter"), 0,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);

         // Viewing radius
         m_Parameters.addNumericalValue(RADIUS, Sextante.getText("Radius"), 0,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);

         // Viewing radius
         m_Parameters.addNumericalValue(THRESHOLD, Sextante.getText("Approximated_viewshed_threshold"), 0.1,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);

         // Resulting view shed
         addOutputVectorLayer(RESULT, Sextante.getText("Viewshed_output"), IVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      boolean result = false;
      try {
         m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);

         // Height of watcher and watched objects
         m_watcherHeight = m_Parameters.getParameterValueAsDouble(HEIGHT);
         m_objectsHeight = m_Parameters.getParameterValueAsDouble(HEIGHTOBS);

         m_threshold = m_Parameters.getParameterValueAsDouble(THRESHOLD);

         m_worldSearchRadius = m_Parameters.getParameterValueAsDouble(RADIUS);
         m_searchRadius = (int) (m_worldSearchRadius / m_DEM.getLayerCellSize());

         final Point2D watcherPoint = m_Parameters.getParameterValueAsPoint(POINT);
         m_watcherPoint = new Coordinate(watcherPoint.getX(), watcherPoint.getY());

         // The output raster has the extent of the search radius
         m_AnalysisExtent = new AnalysisExtent(m_DEM);

         // Set the DEM grid extent to be processed in no search radius is given it is the
         // whole grid extent - that is the entire DEM
         if (m_searchRadius > 0) {
            final double outputXMin = Math.max(m_watcherPoint.x - m_worldSearchRadius, m_AnalysisExtent.getXMin());
            final double outputXMax = Math.min(m_watcherPoint.x + m_worldSearchRadius, m_AnalysisExtent.getXMax());
            final double outputYMin = Math.max(m_watcherPoint.y - m_worldSearchRadius, m_AnalysisExtent.getYMin());
            final double outputYMax = Math.min(m_watcherPoint.y + m_worldSearchRadius, m_AnalysisExtent.getYMax());
            m_AnalysisExtent.setXRange(outputXMin, outputXMax, true);
            m_AnalysisExtent.setYRange(outputYMin, outputYMax, true);
         }

         m_DEM.setWindowExtent(m_AnalysisExtent);

         m_visibilityLayer = getNewVectorLayer(RESULT, Sextante.getText("Viewshed_output"), IVectorLayer.SHAPE_TYPE_POLYGON,
                  new Class[] { Integer.class }, new String[] { VISIBLE_FIELD });

         m_geoFactory = new GeometryFactory();

         // Perform the visibility calculation
         result = calculateVisibility();

         result &= !m_Task.isCanceled();
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
         return false;
      }
      finally {
         ClearData();
      }

      return result;
   }


   private void ClearData() {
      m_DEM = null;
      m_visibilityLayer = null;
      m_watcherPoint = null;
      m_geoFactory = null;
      m_taskCount = null;
      m_executor = null;
   }


   private boolean calculateVisibility() {

      try {
         m_watcherPoint.z = m_DEM.getValueAt(m_watcherPoint.x, m_watcherPoint.y);
         if (m_DEM.isNoDataValue(m_watcherPoint.z)) {
            return false;
         }

         m_minAllowedAngle = new LineSegment(0.0, 0.0, m_worldSearchRadius, m_DEM.getLayerCellSize()).angle();

         // Set elevation and height of watcher
         m_watcherPoint.z += m_watcherHeight;

         final int STARTING_SLICES = 10;
         final double alpha = 2 * Math.PI / STARTING_SLICES;

         m_taskCount = new AtomicInteger(0);
         m_executor = Executors.newFixedThreadPool(java.lang.Runtime.getRuntime().availableProcessors());

         // Add cross section of starting angle size to the working queue
         for (int sliceIndex = 0; sliceIndex < STARTING_SLICES; sliceIndex++) {
            final double angle1 = sliceIndex * alpha;
            final double angle2 = (sliceIndex + 1) * alpha;

            final VisibilityCrossSection crossSection = new VisibilityCrossSection(CreateROS(angle1), CreateROS(angle2), angle1,
                     angle2);
            m_taskCount.getAndIncrement();
            m_executor.execute(new CrossSectionWorker(crossSection));
         }

         // Wait for completion
         while ((m_taskCount.get() > 0) && !m_Task.isCanceled()) {
            synchronized (m_taskCount) {
               m_taskCount.wait(10);
            }
         }

         // Stop the thread pool
         m_executor.shutdownNow();
         return true;

      }
      catch (final Exception e) {
         e.printStackTrace();
         return false;
      }
   }


   /**
    * Create a range of sight - a list of coordinates and their visibility from the watcher.
    * 
    * @param angle -
    *                the angle from the horizon of the range-of-sight
    * @return - calculated ROS from {@link m_watcherPoint} at angle {@link angle} of distance {@link m_worldSearchRadius}
    */
   private List<Coordinate> CreateROS(final double angle) {
      final Coordinate edgePoint = new Coordinate(m_watcherPoint.x + m_worldSearchRadius * Math.cos(angle), m_watcherPoint.y
                                                                                                            + m_worldSearchRadius
                                                                                                            * Math.sin(angle));

      return new RangeOfSight(m_DEM, m_objectsHeight).Calculate(m_watcherPoint, edgePoint);
   }


   /**
    * Add visibility polygons extrapolated from a slice defined by two range of sights The calculation is based on triangulating
    * the slice and assigning each triangle the visibility of its closest ROS segment visibility
    * 
    * @param firstSliceROS
    *                First range of sight of extrapolated slice
    * @param secondSliceROS
    *                Second range of sight of extrapolated slice
    * @param mVisibilityLayer
    *                Polygon layer to add the extrapolated visibility areas
    */
   private void extrapolateSliceVisibility(final List<Coordinate> firstSliceROS,
                                           final List<Coordinate> secondSliceROS) {

      /**
       * The Idea: consider two range of sights and the area slice between them If we represent the ROS as alternating visibility
       * segments we wish to divide the slice between them so areas closer to each segment will have its visibility. We therefore
       * break the range-of-sights to equal length segments and consider each pair of same length: 0. On of the segments is
       * missing. This may happen when the range-of-sights are of different lengths (due to no-data values). In that case, just
       * add a triangle made of the existing segment and the last point of the missing segment. 1. If both segments have the same
       * visibility assign to the whole area between them the same visibility. 2. Otherwise, divide the area to four parts by the
       * two diagonals. The upper and lower parts will be assigned the visibility of the upper and lower segments respectively.
       * The left and right parts will be assigned the visibility assigned to previous right and next left parts respectively. 3.
       * In case of a check board pattern, i.e. (V+NV, NV+V...) instead of cutting be the diagonals we will cut in the middle.
       * 
       */

      final EqualLengthVisibilitySegmentQueue visibilitySegmentQueue = new EqualLengthVisibilitySegmentQueue(firstSliceROS,
               secondSliceROS, m_DEM.getLayerCellSize());

      // The right side polygon of the previous segments area. If not null than it is needed
      // to be set as the current left part visibility
      VisibilityPolygon previousRightSidePolygon = null;

      // Upper and lower parts of a split segment area.
      // We store them so they could be aggregated with the following areas
      VisibilityPolygon previousUpperPart = null, previousLowerPart = null;
      VisibilitySegment previousSegment1 = null, previousSegment2 = null;
      Coordinate previousDiagonalIntersection = null;

      // aggregatedVisiblityPolygon is a union of previous polygons with the same visibility.
      // We aggregate them as much as we can in order to lower the number of polygons in the
      // output.
      VisibilityPolygon aggregatedVisiblityPolygon = null;

      // Go over all segment pairs
      while (!visibilitySegmentQueue.isEmpty()) {
         final VisibilityPair pair = visibilitySegmentQueue.poll();
         final VisibilitySegment segment1 = pair.getFirstSegment();
         final VisibilitySegment segment2 = pair.getSecondSegment();

         // Case 0: one of the segments is null. This happens when the range-of-sights have
         // different overall lengths due to no-data values. In that case, add triangle of
         // the existing segment and the last point of the missing ROS
         if ((segment1 == null) || (segment2 == null)) {

            final int triangleVisibility = segment1 == null ? segment2.getVisibility() : segment1.getVisibility();

            final VisibilityPolygon triangle = segment1 == null ? CreatePolygon(triangleVisibility, previousSegment1.p1,
                     segment2.p1, segment2.p0, previousSegment1.p1) : CreatePolygon(triangleVisibility, previousSegment2.p1,
                     segment1.p1, segment1.p0, previousSegment2.p1);

            if (previousRightSidePolygon != null) {
               triangle.union(previousRightSidePolygon);
               if (aggregatedVisiblityPolygon.visibility() == triangle.visibility()) {
                  aggregatedVisiblityPolygon.union(triangle);
                  if (previousUpperPart.visibility() != aggregatedVisiblityPolygon.visibility()) {
                     AddFeature(previousUpperPart);
                  }
                  else {
                     AddFeature(previousLowerPart);
                  }
               }
               else {
                  AddFeature(aggregatedVisiblityPolygon);
                  if (previousUpperPart.visibility() == triangle.visibility()) {
                     triangle.union(previousUpperPart);
                  }
                  else {
                     triangle.union(previousLowerPart);
                  }
                  aggregatedVisiblityPolygon = triangle;
               }
               previousRightSidePolygon = null;
            }
            else {
               if (aggregatedVisiblityPolygon.visibility() == triangle.visibility()) {
                  aggregatedVisiblityPolygon.union(triangle);
               }
               else {
                  AddFeature(aggregatedVisiblityPolygon);
                  aggregatedVisiblityPolygon = triangle;
               }
            }
         }
         else {

            // Case 1: both segments have the same visibility
            // This is always the first case, as the first points after the watcher are
            // always visible
            if (segment1.getVisibility() == segment2.getVisibility()) {
               final int rectVisibility = segment1.getVisibility();

               // Make a rectangle (or triangle if segments intersect) of the area between
               final VisibilityPolygon rect = segment1.p0.equals2D(segment2.p0) ? CreatePolygon(rectVisibility, segment1.p0,
                        segment1.p1, segment2.p1, segment1.p0) : CreatePolygon(rectVisibility, segment1.p0, segment1.p1,
                        segment2.p1, segment2.p0, segment1.p0);

               if (aggregatedVisiblityPolygon == null) {
                  aggregatedVisiblityPolygon = rect;
               }
               else {
                  // Set the visibility of the undefined previous right part
                  if (previousRightSidePolygon != null) {
                     rect.union(previousRightSidePolygon);
                     if (rect.visibility() != aggregatedVisiblityPolygon.visibility()) {
                        AddFeature(aggregatedVisiblityPolygon);
                        if (previousUpperPart.visibility() == rect.visibility()) {
                           rect.union(previousUpperPart);
                        }
                        else {
                           rect.union(previousLowerPart);
                        }
                        aggregatedVisiblityPolygon = rect;
                     }
                     else {
                        aggregatedVisiblityPolygon.union(rect);
                        if (previousUpperPart.visibility() != aggregatedVisiblityPolygon.visibility()) {
                           AddFeature(previousUpperPart);
                        }
                        else {
                           AddFeature(previousLowerPart);
                        }
                     }

                     previousRightSidePolygon = null;
                  }
                  else {
                     if (rect.visibility() == aggregatedVisiblityPolygon.visibility()) {
                        aggregatedVisiblityPolygon.union(rect);
                     }
                     else {
                        AddFeature(aggregatedVisiblityPolygon);
                        aggregatedVisiblityPolygon = rect;
                     }
                  }
               }
            }
            else {
               // Case 2: segments have different visibility
               // Intersect the area between them by the diagonals
               final LineSegment leftDiagonal = new LineSegment(segment1.p0, segment2.p1);
               final LineSegment rightDiagonal = new LineSegment(segment1.p1, segment2.p0);
               final Coordinate diagonalIntersection = leftDiagonal.intersection(rightDiagonal);

               VisibilityPolygon upperPart = CreatePolygon(segment1.getVisibility(), segment1.p0, segment1.p1,
                        diagonalIntersection, segment1.p0);
               VisibilityPolygon lowerPart = CreatePolygon(segment2.getVisibility(), segment2.p0, segment2.p1,
                        diagonalIntersection, segment2.p0);
               final VisibilityPolygon leftPart = CreatePolygon(-1, segment1.p0, diagonalIntersection, segment2.p0, segment1.p0);

               // No problem setting left part visibility as the previous right side
               // visibility is well defined
               if (previousRightSidePolygon == null) {
                  aggregatedVisiblityPolygon.union(leftPart);
                  if (upperPart.visibility() == aggregatedVisiblityPolygon.visibility()) {
                     aggregatedVisiblityPolygon.union(upperPart);
                     upperPart = aggregatedVisiblityPolygon;
                  }
                  else {
                     aggregatedVisiblityPolygon.union(lowerPart);
                     lowerPart = aggregatedVisiblityPolygon;
                  }
               }
               else {
                  // Now there is a problem. We need to set the previous right part by the current
                  // left part, and the current left part by the previous right part. Note that this
                  // may happen only on two consecutive dissimilar visibility segments. In that case,
                  // Cut both parts in half and color each half by the color of its closest segment
                  // using information of the previous two segments partition
                  final Coordinate middlePoint = new LineSegment(segment1.p0, segment2.p0).midPoint();
                  final VisibilityPolygon previousRightPart1 = CreatePolygon(previousSegment1.getVisibility(), segment1.p0,
                           middlePoint, previousDiagonalIntersection, segment1.p0);
                  final VisibilityPolygon previousRightPart2 = CreatePolygon(previousSegment2.getVisibility(), segment2.p0,
                           middlePoint, previousDiagonalIntersection, segment2.p0);
                  final VisibilityPolygon leftPart1 = CreatePolygon(segment1.getVisibility(), segment1.p0, diagonalIntersection,
                           middlePoint, segment1.p0);
                  final VisibilityPolygon leftPart2 = CreatePolygon(segment2.getVisibility(), segment2.p0, diagonalIntersection,
                           middlePoint, segment2.p0);

                  previousUpperPart.union(previousRightPart1);
                  previousLowerPart.union(previousRightPart2);
                  upperPart.union(leftPart1);
                  lowerPart.union(leftPart2);

                  AddFeature(previousUpperPart);
                  AddFeature(previousLowerPart);

                  aggregatedVisiblityPolygon = upperPart;
               }

               // Mark the right side for addition on next segment pair when we will know the
               // visibility of the left part
               previousRightSidePolygon = CreatePolygon(-1, segment1.p1, diagonalIntersection, segment2.p1, segment1.p1);

               // Update the previous diagonal intersection
               previousDiagonalIntersection = diagonalIntersection;

               previousUpperPart = upperPart;
               previousLowerPart = lowerPart;
            }
         }

         previousSegment1 = segment1 != null ? segment1 : previousSegment1;
         previousSegment2 = segment2 != null ? segment2 : previousSegment2;;
      }

      // Deal with last right side polygon
      if (previousRightSidePolygon != null) {
         aggregatedVisiblityPolygon.union(previousRightSidePolygon);
         if (previousUpperPart.visibility() != aggregatedVisiblityPolygon.visibility()) {
            AddFeature(previousUpperPart);
         }
         else {
            AddFeature(previousLowerPart);
         }
      }

      // Add the aggregated polygon
      AddFeature(aggregatedVisiblityPolygon);
   }


   /**
    * Measure the distance between two range of sights based on XOR (symmetric difference) of visibility segments, normalized to
    * the minimal length of the two range-of-sights. The normalization returns a number between 0.0 (similar) and 1.0 (different).
    * In case the two range-of-sights have different lengths we stop comparing when the shortest reaches its end.
    * 
    * @param firstSliceROS
    *                first range of sight of a slice
    * @param secondSliceROS
    *                second range of sight of a slice
    * @return distance between
    * @param firstSliceROS
    *                and
    * @param secondSliceROS
    */
   private double SliceDistance(final List<Coordinate> firstSliceROS,
                                final List<Coordinate> secondSliceROS) {

      final EqualLengthVisibilitySegmentQueue visibilitySegmentQueue = new EqualLengthVisibilitySegmentQueue(firstSliceROS,
               secondSliceROS, m_DEM.getLayerCellSize());

      double difference = 0.0;
      double totalLength = 0.0;

      while (!visibilitySegmentQueue.isEmpty()) {
         final VisibilityPair pair = visibilitySegmentQueue.poll();
         final VisibilitySegment segment1 = pair.getFirstSegment();
         final VisibilitySegment segment2 = pair.getSecondSegment();

         // If one ROS reached its end stop comparing difference as one has no more data to compare to
         if ((segment1 == null) || (segment2 == null)) {
            break;
         }

         // add segment length to the total length
         // (the iterator ensures that both segments have the same length)
         final double segmentLength = segment1.getLength();
         totalLength += segmentLength;

         // If the two segments have different visibility, sum the length of the differed segments
         if (segment1.getVisibility() != segment2.getVisibility()) {
            difference += segmentLength;
         }
      }

      return difference / totalLength;
   }


   private void AddFeature(final VisibilityPolygon poly) {
      synchronized (m_executor) {
         m_visibilityLayer.addFeature(poly.polygon(), new Object[] { poly.visibility() });
      }
   }


   private void updateProgress(final double sliceAngle) {
      synchronized (m_executor) {
         m_finishedAngles += sliceAngle;
         setProgress((int) (m_finishedAngles * 180 / Math.PI), 360);
      }
   }


   /**
    * Creates a polygon of the given coordinates
    * 
    * @param coordinates
    *                coordinates of the polygon
    * @return Polygon encompassing the area of the coordinates
    */
   private VisibilityPolygon CreatePolygon(final int visiblity,
                                           Coordinate... coordinates) {

      // In case the polygon is not close, add the first point as the last
      if (coordinates[0] != coordinates[coordinates.length - 1]) {
         final List<Coordinate> coordList = new ArrayList<Coordinate>(coordinates.length + 1);
         for (final Coordinate coord : coordinates) {
            coordList.add(coord);
         }
         coordList.add(coordinates[0]);
         coordinates = CoordinateArrays.toCoordinateArray(coordList);
      }

      final Polygon poly = m_geoFactory.createPolygon(m_geoFactory.createLinearRing(coordinates), null);

      return new VisibilityPolygon(poly, visiblity);
   }

   private class CrossSectionWorker
            implements
               Runnable {

      private final VisibilityCrossSection m_crossSection;


      public CrossSectionWorker(final VisibilityCrossSection crossSection) {
         m_crossSection = crossSection;
      }


      //@Override
      public void run() {

         final double sliceAngle = m_crossSection.getAngle2() - m_crossSection.getAngle1();

         // If distance between ROS is acceptable or cutting the slice angle will make it
         // below minimal data angle interpolate the visibility of the slice
         final double crossSectionDistance = SliceDistance(m_crossSection.getRangeOfSight1(), m_crossSection.getRangeOfSight2());
         if ((sliceAngle < 2 * m_minAllowedAngle) || (sliceAngle / Math.PI * crossSectionDistance < m_threshold)) {
            extrapolateSliceVisibility(m_crossSection.getRangeOfSight1(), m_crossSection.getRangeOfSight2());

            // Update progress
            updateProgress(sliceAngle);

            // Decrement the task count. If this was the last task (all others have
            // finished and there are no more tasks in the queue) notify the main thread
            // to finish
            final int previousWorkCount = m_taskCount.getAndDecrement();
            if (previousWorkCount == 1) {
               synchronized (m_taskCount) {
                  m_taskCount.notify();
               }
            }
         }
         else {
            // Otherwise, the cross section's angle is too large. Cut it in half to two
            // cross sections and add them to working queue
            final double middleSectionAngle = (m_crossSection.getAngle1() + m_crossSection.getAngle2()) / 2.0;
            final List<Coordinate> middleROS = CreateROS(middleSectionAngle);

            // increment the work counter to reflect that this task has been split in two
            m_taskCount.getAndIncrement();
            final VisibilityCrossSection firstHalf = new VisibilityCrossSection(m_crossSection.getRangeOfSight1(), middleROS,
                     m_crossSection.getAngle1(), middleSectionAngle);
            m_executor.execute(new CrossSectionWorker(firstHalf));
            final VisibilityCrossSection secondHalf = new VisibilityCrossSection(middleROS, m_crossSection.getRangeOfSight2(),
                     middleSectionAngle, m_crossSection.getAngle2());
            m_executor.execute(new CrossSectionWorker(secondHalf));
         }
      }
   }
}
