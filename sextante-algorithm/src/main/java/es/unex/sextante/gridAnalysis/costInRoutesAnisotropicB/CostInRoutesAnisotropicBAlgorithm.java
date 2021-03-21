package es.unex.sextante.gridAnalysis.costInRoutesAnisotropicB;

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
import es.unex.sextante.parameters.FixedTableModel;
import es.unex.sextante.rasterWrappers.GridCell;
import es.unex.sextante.shapesTools.ShapesTools;

public class CostInRoutesAnisotropicBAlgorithm
         extends
            GeoAlgorithm {

   public static final String   ROUTES     = "ROUTES";
   public static final String   COST       = "COST";
   public static final String   COSTDIR    = "COSTDIR";
   public static final String   RESULT     = "RESULT";
   public static final String   FACTORS    = "FACTORS";

   private static final double  ANGLES[][] = { { 135, 180, 225 }, { 90, 0, 270 }, { 45, 0, 315 } };
   private static final double  NO_DATA    = -99999;

   private IRasterLayer         m_Cost;
   private double[]             m_dDist;
   private double[]             m_dCost;
   private int                  m_iLastX, m_iLastY;
   private int                  m_iPoints  = 0;
   private int                  m_iCurrentRoute;
   private IRasterLayer         m_CostDir;
   private DeviationAndFactor[] m_Factors;


   @Override
   public void defineCharacteristics() {

      final String sColumnNames[] = new String[] { Sextante.getText("Difference"), Sextante.getText("Factor") };

      setName(Sextante.getText("Cost_for_predefined_routes__anisotropic") + "(B)");
      setGroup(Sextante.getText("Cost_distances_and_routes"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(ROUTES, Sextante.getText("Routes"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         m_Parameters.addInputRasterLayer(COST, Sextante.getText("Maximum_unitary_cost"), true);
         m_Parameters.addInputRasterLayer(COSTDIR, Sextante.getText("Direction_of_maximum_cost_[degrees]"), true);
         m_Parameters.addFixedTable(FACTORS, Sextante.getText("Cost_variation_factors"), sColumnNames, 5, false);
         addOutputVectorLayer(RESULT, Sextante.getText("Routes_and_cost"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_Cost = m_Parameters.getParameterValueAsRasterLayer(COST);
      m_CostDir = m_Parameters.getParameterValueAsRasterLayer(COSTDIR);

      final FixedTableModel factors = (FixedTableModel) m_Parameters.getParameterValueAsObject(FACTORS);

      m_Factors = new DeviationAndFactor[factors.getRowCount()];
      for (int i = 0; i < factors.getRowCount(); i++) {
         final double dDeviation = Double.parseDouble(factors.getValueAt(i, 0).toString());
         final double dFactor = Double.parseDouble(factors.getValueAt(i, 1).toString());
         m_Factors[i] = new DeviationAndFactor(dDeviation, dFactor);
      }

      m_Cost.setFullExtent();

      m_CostDir.setWindowExtent(m_Cost.getWindowGridExtent());

      final IVectorLayer routes = m_Parameters.getParameterValueAsVectorLayer(ROUTES);

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

      int iDX, iDY;
      final GridCell cell = m_Cost.getWindowGridExtent().getGridCoordsFromWorldCoords(x, y);
      final int iX = cell.getX();
      final int iY = cell.getY();

      if (m_iPoints == 0) {
         m_dDist[m_iCurrentRoute] = 0.0;
         m_dCost[m_iCurrentRoute] = 0.0;
         m_iLastX = cell.getX();
         m_iLastY = cell.getY();
      }
      else {
         iDX = iX - m_iLastX;
         iDY = iY - m_iLastY;
         m_dDist[m_iCurrentRoute] += Math.sqrt(iDX * iDX + iDY * iDY) * m_Cost.getWindowCellSize();
         final double dCost = getCostInDir(m_iLastX, m_iLastY, iDX, iDY);
         if (dCost != NO_DATA) {
            m_dCost[m_iCurrentRoute] += dCost;
         }
         m_iLastX = iX;
         m_iLastY = iY;
      }

      m_iLastX = iX;
      m_iLastY = iY;
      m_iPoints++;

   }


   private double getCostInDir(final int x,
                               final int y,
                               final int iH,
                               final int iV) {

      final double dAngle = ANGLES[iV + 1][iH + 1];

      final int x2 = x + iH;
      final int y2 = y + iV;

      final double dCostDir1 = m_CostDir.getCellValueAsDouble(x, y);
      final double dCostDir2 = m_CostDir.getCellValueAsDouble(x2, y2);
      double dCost1 = m_Cost.getCellValueAsDouble(x, y);
      double dCost2 = m_Cost.getCellValueAsDouble(x2, y2);

      if (m_Cost.isNoDataValue(dCost1) || m_Cost.isNoDataValue(dCost1) || m_CostDir.isNoDataValue(dCostDir1)
          || m_CostDir.isNoDataValue(dCostDir1)) {
         return NO_DATA;
      }
      else {
         final double dDifAngle1 = Math.abs(dCostDir1 - dAngle);
         final double dDifAngle2 = Math.abs(dCostDir2 - dAngle);

         dCost1 = getWeigthedCost(dCost1, dDifAngle1);
         dCost2 = getWeigthedCost(dCost2, dDifAngle2);

         return dCost1 + dCost2;
      }

   }


   private double getWeigthedCost(final double cost,
                                  final double difAngle) {

      for (int i = 0; i < m_Factors.length - 1; i++) {
         if ((m_Factors[i].deviation < difAngle) || (m_Factors[i + 1].deviation >= difAngle)) {
            return m_Factors[i].factor * cost / 2.;
         }
      }

      return cost / 2.;

   }

   private class DeviationAndFactor
            implements
               Comparable {

      public double factor;
      public double deviation;


      DeviationAndFactor(final double dDeviation,
                         final double dFactor) {

         deviation = dDeviation;
         factor = dFactor;
      }


      public int compareTo(final Object ob) throws ClassCastException {

         if (!(ob instanceof DeviationAndFactor)) {
            throw new ClassCastException();
         }

         final double dDeviation = ((DeviationAndFactor) ob).deviation;
         final double dDif = this.deviation - dDeviation;

         if (dDif > 0.0) {
            return 1;
         }
         else if (dDif < 0.0) {
            return -1;
         }
         else {
            return 0;
         }

      }
   }

}
