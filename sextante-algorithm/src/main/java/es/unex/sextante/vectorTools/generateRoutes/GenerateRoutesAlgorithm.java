package es.unex.sextante.vectorTools.generateRoutes;

import java.util.ArrayList;
import java.util.Collections;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class GenerateRoutesAlgorithm
         extends
            GeoAlgorithm {

   public static final String ROUTE        = "ROUTE";
   public static final String NROUTES      = "NROUTES";
   public static final String METHOD       = "METHOD";
   public static final String SINUOSITY    = "SINUOSITY";
   public static final String USESINUOSITY = "USESINUOSITY";
   public static final String RESULT       = "RESULT";

   private IVectorLayer       m_Routes;
   private ArrayList          m_X;
   private ArrayList          m_Y;
   private int                m_iNewRoutes;
   private double             m_dInitX;
   private double             m_dInitY;
   private double             m_dSinuosity;
   private double             m_dFinalX;
   private double             m_dFinalY;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Generate_alternative_routes"));
      setGroup(Sextante.getText("Cost_distances_and_routes"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputVectorLayer(ROUTE, Sextante.getText("Original_route"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE,
                  true);
         m_Parameters.addNumericalValue(NROUTES, Sextante.getText("Number_of_new_routes"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 10, 1, Integer.MAX_VALUE);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), new String[] {
                  Sextante.getText("Constrained_brownian_motion"), Sextante.getText("Recombination") });
         m_Parameters.addNumericalValue(SINUOSITY, Sextante.getText("Sinuosity"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1.3, 1, 10.);
         m_Parameters.addBoolean(USESINUOSITY, Sextante.getText("Use_base_route_sinuosity"), false);
         addOutputVectorLayer(RESULT, Sextante.getText("Routes"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      double x, y;
      double dDifX, dDifY;
      double dRouteDist = 0, dStraightDist;

      final IVectorLayer lines = m_Parameters.getParameterValueAsVectorLayer(ROUTE);
      m_iNewRoutes = m_Parameters.getParameterValueAsInt(NROUTES);
      final int iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      final boolean bUseRouteSinuosity = m_Parameters.getParameterValueAsBoolean(USESINUOSITY);
      m_dSinuosity = m_Parameters.getParameterValueAsDouble(SINUOSITY);

      final int iShapeCount = lines.getShapesCount();

      if (iShapeCount == 0) {
         throw new GeoAlgorithmExecutionException("zero lines in layer");
      }

      m_Routes = getNewVectorLayer(RESULT, Sextante.getText("Alternative_routes"), IVectorLayer.SHAPE_TYPE_LINE,
               new Class[] { Integer.class }, new String[] { "ID" });

      final IFeatureIterator iter = lines.iterator();
      final IFeature feature = iter.next();
      final Geometry geom = feature.getGeometry();
      final Geometry line = geom.getGeometryN(0);
      final Coordinate[] coords = line.getCoordinates();

      m_X = new ArrayList();
      m_Y = new ArrayList();

      m_dInitX = x = coords[0].x;
      m_dInitY = y = coords[0].y;

      for (int i = 1; i < coords.length; i++) {
         dDifX = coords[i].x - x;
         dDifY = coords[i].y - y;
         dRouteDist += Math.sqrt(dDifX * dDifX + dDifY * dDifY);
         m_X.add(new Double(dDifX));
         m_Y.add(new Double(dDifY));
         x = coords[i].x;
         y = coords[i].y;
      }

      m_dFinalX = x;
      m_dFinalY = y;

      dStraightDist = Math.sqrt(Math.pow(m_dFinalX - m_dInitX, 2) + Math.pow(m_dFinalY - m_dInitY, 2));
      if (bUseRouteSinuosity) {
         m_dSinuosity = dRouteDist / dStraightDist;
      }

      if (iMethod == 0) {
         generateRoutesBrownian();
      }
      else {
         generateRoutesRecombine();
      }

      return !m_Task.isCanceled();

   }


   private void generateRoutesRecombine() {

      final Object value[] = new Object[1];
      double dLastX, dLastY;
      double dX, dY;

      for (int i = 0; (i < m_iNewRoutes) && setProgress(i, m_iNewRoutes); i++) {
         Collections.shuffle(m_X);
         Collections.shuffle(m_Y);
         final Coordinate coords[] = new Coordinate[m_X.size() + 1];
         dLastX = m_dInitX;
         dLastY = m_dInitY;
         coords[0] = new Coordinate(m_dInitX, m_dInitY);
         for (int j = 0; j < m_X.size(); j++) {
            dX = dLastX + ((Double) m_X.get(j)).doubleValue();
            dY = dLastY + ((Double) m_Y.get(j)).doubleValue();
            coords[j + 1] = new Coordinate(dX, dY);
            dLastX = dX;
            dLastY = dY;
         }
         value[0] = new Integer(i);
         final GeometryFactory gf = new GeometryFactory();
         final Geometry geom = gf.createLineString(coords);
         m_Routes.addFeature(geom, value);
      }

   }


   private void generateRoutesBrownian() {

      int i;
      int iIter;
      final int SEGMENTS = 50;
      double dMaxDist, dMaxSegmentDist;
      double dPreviousDist = 0;
      double dDist;
      double dAngle;
      double dX, dY;
      double dLastX, dLastY;
      final double dTotalXDif = m_dFinalX - m_dInitX;
      final double dTotalYDif = m_dFinalY - m_dInitY;
      final double dTotalDist = Math.sqrt(dTotalXDif * dTotalXDif + dTotalYDif * dTotalYDif) * m_dSinuosity;
      final Object value[] = new Object[1];

      for (int iRoute = 0; (iRoute < m_iNewRoutes) && setProgress(iRoute, m_iNewRoutes); iRoute++) {
         final Coordinate coords[] = new Coordinate[SEGMENTS + 1];
         coords[0] = new Coordinate(m_dInitX, m_dInitY);
         dLastX = m_dInitX;
         dLastY = m_dInitY;
         dPreviousDist = 0;

         for (i = 1; i < SEGMENTS; i++) {
            dMaxDist = (dTotalDist - dPreviousDist);
            dMaxSegmentDist = dMaxDist / (SEGMENTS - i);
            dMaxDist /= (m_dSinuosity);
            iIter = 0;
            do {
               dDist = Math.random() * dMaxSegmentDist;
               dAngle = Math.random() * Math.PI * 2;
               dX = dLastX + dDist * Math.cos(dAngle);
               dY = dLastY + dDist * Math.sin(dAngle);
               dDist = Math.sqrt(Math.pow(m_dFinalX - dX, 2) + Math.pow(m_dFinalY - dY, 2));
               iIter++;
            }
            while ((dDist > dMaxDist) && (iIter < 1000));
            if (iIter == 1000) {
               dX = dLastX + (m_dFinalX - dLastX) * (dMaxSegmentDist / dMaxDist);
               dY = dLastY + (m_dFinalY - dLastY) * (dMaxSegmentDist / dMaxDist);
            }
            dPreviousDist += Math.sqrt(Math.pow(dLastX - dX, 2) + Math.pow(dLastY - dY, 2));
            coords[i] = new Coordinate(dX, dY);
            dLastX = dX;
            dLastY = dY;
         }
         coords[i] = new Coordinate(m_dFinalX, m_dFinalY);
         value[0] = new Integer(iRoute);
         final GeometryFactory gf = new GeometryFactory();
         final Geometry geom = gf.createLineString(coords);
         m_Routes.addFeature(geom, value);
      }
   }


}
