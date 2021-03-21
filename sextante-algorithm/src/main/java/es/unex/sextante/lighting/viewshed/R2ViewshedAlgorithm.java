/*******************************************************************************
R2ViewshedAlgorithm.java
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

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.rasterWrappers.GridCell;

public class R2ViewshedAlgorithm
         extends
            GeoAlgorithm {

   public static final String DEM       = "DEM";
   public static final String POINT     = "POINT";
   public static final String HEIGHT    = "HEIGHT";
   public static final String HEIGHTOBS = "HEIGHTOBS";
   public static final String RADIUS    = "RADIUS";
   public static final String RESULT    = "RESULT";

   private int                m_xCellCount, m_yCellCount;
   private IRasterLayer       m_DEM     = null;
   private IRasterLayer       m_visibilityRaster;
   private GridCell           m_watcherCellPoint;
   private double             m_watcherHeight, m_objectsHeight;
   private int                m_searchRadius;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("R2_Viewshed"));
      setGroup(Sextante.getText("Visibility_and_lighting"));
      setUserCanDefineAnalysisExtent(false); // Output defined by problem !

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

         // Resulting view shed
         addOutputRasterLayer(RESULT, Sextante.getText("Viewshed_output"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_DEM = m_Parameters.getParameterValueAsRasterLayer(DEM);

      // Height of watcher and watched objects
      m_watcherHeight = m_Parameters.getParameterValueAsDouble(HEIGHT);
      m_objectsHeight = m_Parameters.getParameterValueAsDouble(HEIGHTOBS);

      // Search radius is the number of cells in each direction to consider
      final double worldSearchRadius = m_Parameters.getParameterValueAsDouble(RADIUS);
      m_searchRadius = (int) (worldSearchRadius / m_DEM.getLayerCellSize());

      // Position of watcher
      final Point2D watcherPoint = m_Parameters.getParameterValueAsPoint(POINT);

      // The output raster has the extent of the search radius
      m_AnalysisExtent = new AnalysisExtent(m_DEM);

      if (m_searchRadius > 0) {
         final double outputXMin = Math.max(watcherPoint.getX() - worldSearchRadius, m_AnalysisExtent.getXMin());
         final double outputXMax = Math.min(watcherPoint.getX() + worldSearchRadius, m_AnalysisExtent.getXMax());
         final double outputYMin = Math.max(watcherPoint.getY() - worldSearchRadius, m_AnalysisExtent.getYMin());
         final double outputYMax = Math.min(watcherPoint.getY() + worldSearchRadius, m_AnalysisExtent.getYMax());
         m_AnalysisExtent.setXRange(outputXMin, outputXMax, true);
         m_AnalysisExtent.setYRange(outputYMin, outputYMax, true);
         m_AnalysisExtent.enlargeOneCell();// TODO: unnecessary ?
      }
      m_DEM.setWindowExtent(m_AnalysisExtent);

      m_visibilityRaster = getNewRasterLayer(RESULT, Sextante.getText("Viewshed_output"), IRasterLayer.RASTER_DATA_TYPE_FLOAT);

      m_xCellCount = m_DEM.getNX();
      m_yCellCount = m_DEM.getNY();

      // Cell coordinate of watcher
      m_watcherCellPoint = m_DEM.getWindowGridExtent().getGridCoordsFromWorldCoords(watcherPoint);

      // Perform the visibility calculation
      calculateVisibility(m_watcherCellPoint.getX(), m_watcherCellPoint.getY());

      return !m_Task.isCanceled();
   }


   private void calculateVisibility(final int x_Pos,
                                    final int y_Pos) {

      int iXMin, iYMin;
      int iXMax, iYMax;

      final double watcherZValue = m_DEM.getCellValueAsDouble(x_Pos, y_Pos);
      if (m_DEM.isNoDataValue(watcherZValue)) {
         return;
      }

      // Set elevation and height of watcher
      m_watcherCellPoint.setValue(watcherZValue + m_watcherHeight);

      final RangeOfSight rangeOfSight = new RangeOfSight(m_DEM, m_objectsHeight);

      // Gather the grid area to check
      if (m_searchRadius > 0) {
         iXMin = Math.max(0, x_Pos - m_searchRadius);
         iYMin = Math.max(0, y_Pos - m_searchRadius);
         iXMax = Math.min(m_xCellCount, x_Pos + m_searchRadius);
         iYMax = Math.min(m_xCellCount, y_Pos + m_searchRadius);
      }
      else {
         iXMin = 0;
         iXMax = m_xCellCount;
         iYMin = 0;
         iYMax = m_yCellCount;
      }

      // Prepare list of the cells at the edges
      final List<GridCell> edgeCells = new ArrayList<GridCell>();
      for (int xIndex = iXMin; xIndex < iXMax; xIndex++) {
         edgeCells.add(new GridCell(xIndex, iYMin, 0));
      }
      for (int yIndex = iYMin + 1; yIndex < iYMax; yIndex++) {
         edgeCells.add(new GridCell(iXMax - 1, yIndex, 0));
      }
      for (int xIndex = iXMax - 2; xIndex >= iXMin; xIndex--) {
         edgeCells.add(new GridCell(xIndex, iYMax - 1, 0));
      }
      for (int yIndex = iYMax - 2; yIndex >= iYMin + 1; yIndex--) {
         edgeCells.add(new GridCell(iXMin, yIndex, 0));
      }

      // Draw a line of sight from watcher to each of the edge cells
      int cellIndex = 0;
      for (final GridCell edgeCell : edgeCells) {
         // Update progress
         setProgress(cellIndex++, edgeCells.size());
         final List<GridCell> ros = rangeOfSight.Calculate(m_watcherCellPoint, edgeCell, false);

         // Update output raster with visibility data from the range of sight
         for (final GridCell visibilityCell : ros) {
            if (Math.pow(visibilityCell.getX() - m_watcherCellPoint.getX(), 2)
                + Math.pow(visibilityCell.getY() - m_watcherCellPoint.getY(), 2) > Math.pow(m_searchRadius, 2)) {
               m_visibilityRaster.setCellValue(visibilityCell.getX(), visibilityCell.getY(), m_visibilityRaster.getNoDataValue());
            }
            else {
               m_visibilityRaster.setCellValue(visibilityCell.getX(), visibilityCell.getY(), visibilityCell.getValue());
            }
         }
      }

   }
}
