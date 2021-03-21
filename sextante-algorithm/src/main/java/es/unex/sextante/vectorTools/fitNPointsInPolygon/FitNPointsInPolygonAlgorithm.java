

package es.unex.sextante.vectorTools.fitNPointsInPolygon;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;


public class FitNPointsInPolygonAlgorithm
         extends
            GeoAlgorithm {

   private static final int   MAX_REP                           = 50;

   public static final String POLYGONS                          = "POLYGONS";
   public static final String NPOINTS                           = "NPOINTS";
   public static final String NPOINTS_METHOD                    = "NPOINTS_METHOD";
   public static final String METHOD                            = "METHOD";
   public static final String RESULT                            = "RESULT";
   public static final String FIELD                             = "FIELD";

   public static final int    NPOINTS_FIXED                     = 0;
   public static final int    NPOINTS_FROM_FIELD                = 1;

   public static final int    METHOD_REGULARLY_SPACED           = 0;
   public static final int    METHOD_RANDOMLY                   = 1;
   public static final int    METHOD_REGULARLY_SPACED_ALTERNATE = 2;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int iPointsIn = 0;
      int iRep = 0;
      int i = 0, j;
      double x, y;
      double dArea;
      double dDist;
      double dDistInf, dDistSup;
      final String[] sFields = { "X", "Y" };
      final Class[] types = { Double.class, Double.class };
      final ArrayList points = new ArrayList();
      final ArrayList allPoints = new ArrayList();
      Envelope extent;
      final Coordinate coord = new Coordinate();
      final Object[] values = new Object[2];

      final IVectorLayer polygons = m_Parameters.getParameterValueAsVectorLayer(POLYGONS);
      int iPoints = m_Parameters.getParameterValueAsInt(NPOINTS);
      final int iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      final int iField = m_Parameters.getParameterValueAsInt(FIELD);
      final int iNPointsMethod = m_Parameters.getParameterValueAsInt(NPOINTS_METHOD);

      if (!m_bIsAutoExtent) {
         polygons.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final GeometryFactory gf = new GeometryFactory();
      final int iShapeCount = polygons.getShapesCount();
      final IFeatureIterator iter = polygons.iterator();
      while (iter.hasNext() && !m_Task.isCanceled()) {
         setProgressText(Integer.toString(i) + "/" + Integer.toString(iShapeCount));
         final IFeature feature = iter.next();
         final Geometry geometry = feature.getGeometry();
         if (iNPointsMethod == NPOINTS_FROM_FIELD) {
            try {
               iPoints = Integer.parseInt(feature.getRecord().getValue(iField).toString());
            }
            catch (final Exception e) {
               iPoints = 0;
            }
         }
         extent = geometry.getEnvelopeInternal();
         if (iMethod == METHOD_REGULARLY_SPACED) {
            iRep = 0;
            dArea = geometry.getArea();
            dDist = Math.sqrt(dArea / iPoints);
            dDistInf = Math.sqrt(dArea / (iPoints + 2));
            dDistSup = Math.sqrt(dArea / (iPoints - Math.min(2, iPoints - 1)));
            if (dDist > 0) {
               do {
                  points.clear();
                  iPointsIn = 0;
                  iRep++;
                  for (x = extent.getMinX(); x < extent.getMaxX(); x = x + dDist) {
                     for (y = extent.getMinY(); y < extent.getMaxY(); y = y + dDist) {
                        coord.x = x;
                        coord.y = y;
                        if (geometry.contains(gf.createPoint(coord))) {
                           points.add(new Point2D.Double(x, y));
                           iPointsIn++;
                           setProgress(iPointsIn, iPoints);
                        }
                     }
                  }
                  if (iPointsIn > iPoints) {
                     dDistInf = dDist;
                     dDist = (dDistInf + dDistSup) / 2.;
                  }
                  else if (iPointsIn < iPoints) {
                     dDistSup = dDist;
                     dDist = (dDistInf + dDistSup) / 2.;
                  }
               }
               while ((iPointsIn != iPoints) && (iRep < MAX_REP) && !m_Task.isCanceled());
               for (j = 0; j < points.size(); j++) {
                  allPoints.add(new Point2D.Double(((Point2D) points.get(j)).getX(), ((Point2D) points.get(j)).getY()));
               }
            }
         }
         else if (iMethod == METHOD_RANDOMLY) {
            iPointsIn = 0;
            do {
               x = Math.random() * extent.getWidth() + extent.getMinX();
               y = Math.random() * extent.getHeight() + extent.getMinY();
               coord.x = x;
               coord.y = y;
               if (geometry.contains(gf.createPoint(coord))) {
                  allPoints.add(new Point2D.Double(x, y));
                  iPointsIn++;
               }
            }
            while ((iPointsIn != iPoints) && setProgress(iPointsIn, iPoints));
         }
         else if (iMethod == METHOD_REGULARLY_SPACED_ALTERNATE) {
            iRep = 0;
            dArea = geometry.getArea();
            dDist = Math.sqrt(dArea / iPoints);
            dDistInf = Math.sqrt(dArea / (iPoints + 2));
            dDistSup = Math.sqrt(dArea / (iPoints - Math.min(2, iPoints - 1)));
            if (dDist > 0) {
               do {
                  points.clear();
                  iPointsIn = 0;
                  iRep++;
                  for (x = extent.getMinX(); x < extent.getMaxX(); x = x + dDist) {
                     boolean bDisplace = false;
                     for (y = extent.getMinY(); y < extent.getMaxY(); y = y + dDist) {
                        coord.x = x;
                        coord.y = y;
                        if (bDisplace) {
                           coord.x = coord.x + dDist / 2;
                        }
                        bDisplace = !bDisplace;
                        if (geometry.contains(gf.createPoint(coord))) {
                           points.add(new Point2D.Double(coord.x, coord.y));
                           iPointsIn++;
                           setProgress(iPointsIn, iPoints);
                        }
                     }
                  }
                  if (iPointsIn > iPoints) {
                     dDistInf = dDist;
                     dDist = (dDistInf + dDistSup) / 2.;
                  }
                  else if (iPointsIn < iPoints) {
                     dDistSup = dDist;
                     dDist = (dDistInf + dDistSup) / 2.;
                  }
               }
               while ((iPointsIn != iPoints) && (iRep < MAX_REP) && !m_Task.isCanceled());
               for (j = 0; j < points.size(); j++) {
                  allPoints.add(new Point2D.Double(((Point2D) points.get(j)).getX(), ((Point2D) points.get(j)).getY()));
               }
            }
         }
         i++;
      }

      if (allPoints.size() != 0) {
         final IVectorLayer outputLayer = getNewVectorLayer(RESULT, Sextante.getText("Points"), IVectorLayer.SHAPE_TYPE_POINT,
                  types, sFields);

         for (i = 0; i < allPoints.size(); i++) {
            x = ((Point2D) allPoints.get(i)).getX();
            y = ((Point2D) allPoints.get(i)).getY();
            values[0] = new Double(x);
            values[1] = new Double(y);
            final Geometry pt = gf.createPoint(new Coordinate(x, y));
            outputLayer.addFeature(pt, values);
         }
      }

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      final String[] sOptions = { Sextante.getText("Regular_grid"), Sextante.getText("Random"),
               Sextante.getText("Regular_grid_alternate") };

      final String[] sOptionsNPoints = { Sextante.getText("Fixed_number"), Sextante.getText("Take_from_table_field") };

      setName(Sextante.getText("Adjust_n_point_to_polygon"));
      setGroup(Sextante.getText("Tools_for_polygon_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(POLYGONS, Sextante.getText("Polygons"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON,
                  true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field_for_number_of_points"), POLYGONS);
         m_Parameters.addNumericalValue(NPOINTS, Sextante.getText("Number_of_points"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 10, 1, Integer.MAX_VALUE);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sOptions);
         m_Parameters.addSelection(NPOINTS_METHOD, Sextante.getText("Number_of_points"), sOptionsNPoints);
         addOutputVectorLayer(RESULT, Sextante.getText("Points"), OutputVectorLayer.SHAPE_TYPE_POINT);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final UndefinedParentParameterNameException e) {
         e.printStackTrace();
      }
      catch (final OptionalParentParameterException e) {
         e.printStackTrace();
      }

   }

}
