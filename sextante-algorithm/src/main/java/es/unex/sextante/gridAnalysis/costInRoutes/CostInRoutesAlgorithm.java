package es.unex.sextante.gridAnalysis.costInRoutes;


import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.shapesTools.ShapesTools;

public class CostInRoutesAlgorithm
         extends
            GeoAlgorithm {

   public static final String ROUTES    = "ROUTES";
   public static final String COST      = "COST";
   public static final String RESULT    = "RESULT";

   private IRasterLayer       m_Cost;
   private double[]           m_dDist;
   private double[]           m_dCost;
   private double             m_dLastX, m_dLastY;
   private int                m_iPoints = 0;
   private int                m_iCurrentRoute;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Cost_for_predefined_routes"));
      setGroup(Sextante.getText("Cost_distances_and_routes"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(ROUTES, Sextante.getText("Routes"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         m_Parameters.addInputRasterLayer(COST, Sextante.getText("Cost"), true);
         addOutputVectorLayer(RESULT, Sextante.getText("Routes_and_cost"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer routes = m_Parameters.getParameterValueAsVectorLayer(ROUTES);
      m_Cost = m_Parameters.getParameterValueAsRasterLayer(COST);

      if (routes.getShapesCount() == 0) {
         return false;
      }

      final int iShapesCount = routes.getShapesCount();
      m_iCurrentRoute = 0;
      m_Cost.setFullExtent();
      m_dCost = new double[routes.getShapesCount()];
      m_dDist = new double[routes.getShapesCount()];
      final IFeatureIterator iter = routes.iterator();
      while (iter.hasNext() && setProgress(m_iCurrentRoute, iShapesCount)) {
         m_iPoints = 0;
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         processLine(geom.getCoordinates());
         m_iCurrentRoute++;
      }
      iter.close();

      if (m_Task.isCanceled()) {
         return false;
      }

      final Object[][] values = new Object[2][routes.getShapesCount()];
      final String[] sFields = { Sextante.getText("Distance"), Sextante.getText("Cost") };
      final Class[] types = { Double.class, Double.class };
      for (int i = 0; i < routes.getShapesCount(); i++) {
         values[0][i] = new Double(m_dDist[i]);
         values[1][i] = new Double(m_dCost[i]);
      }
      final IOutputChannel channel = getOutputChannel(RESULT);
      final Output out = new OutputVectorLayer();
      out.setName(RESULT);
      out.setDescription(Sextante.getText("Routes"));
      out.setOutputChannel(channel);
      out.setOutputObject(ShapesTools.addFields(m_OutputFactory, routes, channel, sFields, values, types));
      addOutputObject(out);

      return true;

   }


   private void processLine(final Coordinate coords[]) {

      int i;
      double x, y, x2, y2;

      for (i = 0; i < coords.length - 1; i++) {
         x = coords[i].x;
         y = coords[i].y;
         x2 = coords[i + 1].x;
         y2 = coords[i + 1].y;
         processSegment(x, y, x2, y2);
      }

   }


   private void processSegment(double x,
                               double y,
                               final double x2,
                               final double y2) {

      double dx, dy, d, n;

      dx = Math.abs(x2 - x);
      dy = Math.abs(y2 - y);

      if ((dx > 0.0) || (dy > 0.0)) {
         if (dx > dy) {
            dx /= m_Cost.getWindowCellSize();
            n = dx;
            dy /= dx;
            dx = m_Cost.getWindowCellSize();
         }
         else {
            dy /= m_Cost.getWindowCellSize();
            n = dy;
            dx /= dy;
            dy = m_Cost.getWindowCellSize();
         }

         if (x2 < x) {
            dx = -dx;
         }

         if (y2 < y) {
            dy = -dy;
         }

         for (d = 0.0; d <= n; d++, x += dx, y += dy) {
            addPoint(x, y);
         }
      }

   }


   private void addPoint(final double x,
                         final double y) {

      double dDX, dDY;

      final double z = m_Cost.getValueAt(x, y);

      if (m_iPoints == 0) {
         m_dDist[m_iCurrentRoute] = 0.0;
         m_dCost[m_iCurrentRoute] = 0.0;
      }
      else {
         dDX = x - m_dLastX;
         dDY = y - m_dLastY;
         m_dDist[m_iCurrentRoute] += Math.sqrt(dDX * dDX + dDY * dDY);
         if (!m_Cost.isNoDataValue(z)) {
            m_dCost[m_iCurrentRoute] += z;
         }
      }

      m_dLastX = x;
      m_dLastY = y;
      m_iPoints++;

   }

}
