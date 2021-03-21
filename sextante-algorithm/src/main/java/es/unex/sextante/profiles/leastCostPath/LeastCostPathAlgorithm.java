package es.unex.sextante.profiles.leastCostPath;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.profiles.profile.ProfileAlgorithm;
import es.unex.sextante.rasterWrappers.GridCell;

public class LeastCostPathAlgorithm
         extends
            GeoAlgorithm {

   private final static int   m_iOffsetX[]  = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int   m_iOffsetY[]  = { 1, 1, 0, -1, -1, -1, 0, 1 };

   public static final String GRAPH         = "GRAPH";
   public static final String PROFILEPOINTS = "PROFILEPOINTS";
   public static final String POINT         = "POINT";
   public static final String LAYERS        = "LAYERS";
   public static final String ACCCOST       = "ACCCOST";
   public static final String PROFILELINE   = "PROFILELINE";

   private IRasterLayer       m_Cost;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int iDirection;
      boolean bContinue = true;
      final String sNames[] = { "ID" };
      final Class types[] = { Integer.class };
      final Object[] value = new Object[1];

      final ArrayList layers = m_Parameters.getParameterValueAsArrayList(LAYERS);
      m_Cost = m_Parameters.getParameterValueAsRasterLayer(ACCCOST);
      Point2D pt = m_Parameters.getParameterValueAsPoint(POINT);
      m_Cost.setFullExtent();
      final AnalysisExtent extent = m_Cost.getWindowGridExtent();
      final GridCell cell = extent.getGridCoordsFromWorldCoords(pt);
      final ArrayList coords = new ArrayList();
      coords.add(new Coordinate(pt.getX(), pt.getY()));

      do {
         iDirection = getDirToNextDownslopeCell(cell.getX(), cell.getY());
         if (iDirection >= 0) {
            cell.setX(cell.getX() + m_iOffsetX[iDirection]);
            cell.setY(cell.getY() + m_iOffsetY[iDirection]);
            pt = extent.getWorldCoordsFromGridCoords(cell);
            coords.add(new Coordinate(pt.getX(), pt.getY()));
         }
         else {
            bContinue = false;
         }
      }
      while (bContinue && !m_Task.isCanceled());

      if (m_Task.isCanceled()) {
         return false;
      }

      if (coords.size() > 1) {
         final IVectorLayer lines = getNewVectorLayer(PROFILELINE, Sextante.getText("Profile"), IVectorLayer.SHAPE_TYPE_LINE,
                  types, sNames);
         value[0] = new Double(1);
         final GeometryFactory gf = new GeometryFactory();
         final Coordinate[] coordinates = new Coordinate[coords.size()];
         for (int i = 0; i < coordinates.length; i++) {
            coordinates[i] = (Coordinate) coords.get(i);
         }
         final LineString line = gf.createLineString(coordinates);
         lines.addFeature(line, value);
         try {
            lines.postProcess();//we have to do this to use this as input
         }
         catch (final Exception e) {
            throw new GeoAlgorithmExecutionException(e.getMessage());
         }
         final ProfileAlgorithm profile = new ProfileAlgorithm();
         profile.getParameters().getParameter(ProfileAlgorithm.DEM).setParameterValue(m_Cost);
         profile.getParameters().getParameter(ProfileAlgorithm.LAYERS).setParameterValue(layers);
         profile.getParameters().getParameter(ProfileAlgorithm.ROUTE).setParameterValue(lines);
         final OutputObjectsSet outputs = profile.getOutputObjects();
         outputs.getOutput(ProfileAlgorithm.PROFILEPOINTS).setOutputChannel(getOutputChannel(PROFILEPOINTS));
         if (profile.execute(m_Task, m_OutputFactory)) {
            final IVectorLayer profilePts = (IVectorLayer) outputs.getOutput(ProfileAlgorithm.PROFILEPOINTS).getOutputObject();
            m_OutputObjects.getOutput(PROFILEPOINTS).setOutputObject(profilePts);
         }
         else {
            return false;
         }
      }
      else {
         throw new GeoAlgorithmExecutionException("zero lines in layer");
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Least_cost_path"));
      setGroup(Sextante.getText("Cost_distances_and_routes"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(ACCCOST, Sextante.getText("Accumulated_cost"), true);
         m_Parameters.addMultipleInput(LAYERS, Sextante.getText("Additional_layers"),
                  AdditionalInfoMultipleInput.DATA_TYPE_RASTER, false);
         m_Parameters.addPoint(POINT, Sextante.getText("Starting_point"));
         addOutputVectorLayer(PROFILEPOINTS, Sextante.getText("Route_[points]"), OutputVectorLayer.SHAPE_TYPE_POINT);
         addOutputVectorLayer(PROFILELINE, Sextante.getText("Route_[line]"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   public int getDirToNextDownslopeCell(final int x,
                                        final int y) {

      int i, iDir;
      double z, z2, dSlope, dMaxSlope;

      z = m_Cost.getCellValueAsDouble(x, y);

      if (m_Cost.isNoDataValue(z)) {
         return -1;
      }

      dMaxSlope = 0.0;
      for (iDir = -1, i = 0; i < 8; i++) {
         z2 = m_Cost.getCellValueAsDouble(x + m_iOffsetX[i], y + m_iOffsetY[i]);
         if (!m_Cost.isNoDataValue(z2)) {
            dSlope = (z - z2) / m_Cost.getDistToNeighborInDir(i);
            if (dSlope > dMaxSlope) {
               iDir = i;
               dMaxSlope = dSlope;
            }
         }
      }

      return iDir;

   }


}
