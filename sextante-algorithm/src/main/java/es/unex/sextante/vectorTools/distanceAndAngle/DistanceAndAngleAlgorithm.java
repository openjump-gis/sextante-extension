

package es.unex.sextante.vectorTools.distanceAndAngle;

//import org.locationtech.jcs.algorithm.VertexHausdorffDistance;
import org.locationtech.jts.algorithm.distance.DiscreteHausdorffDistance;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

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


public class DistanceAndAngleAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER1        = "LAYER1";
   public static final String FIELD1        = "ID1";
   public static final String CONVEXHULL1   = "CONVEXHULL1";
   public static final String LAYER2        = "LAYER2";
   public static final String FIELD2        = "ID2";
   public static final String CONVEXHULL2   = "CONVEXHULL2";
   public static final String RESULT_POINTS = "POINTS";
   public static final String RESULT_LINES  = "LINES";


   /**
    * LINES -- UID_A, UID_B, MIN_DISTANCE, CENTROID_DISTANCE, HAUSDORFF_DISTANCE, ANGLE_CENTROIDS, BEARING_CENTROIDS
    */
   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i = 0, j = 0;
      double x1, x2, y1, y2;
      int iCount1;
      String[] linesFields;
      Class[] linesTypes;
      Object[] linesValues;

      final IVectorLayer layer1 = m_Parameters.getParameterValueAsVectorLayer(LAYER1);
      final int id1_idx = m_Parameters.getParameterValueAsInt(FIELD1);
      final boolean useConvexHull1 = m_Parameters.getParameterValueAsBoolean(CONVEXHULL1);
      final IVectorLayer layer2 = m_Parameters.getParameterValueAsVectorLayer(LAYER2);
      final int id2_idx = m_Parameters.getParameterValueAsInt(FIELD2);
      final boolean useConvexHull2 = m_Parameters.getParameterValueAsBoolean(CONVEXHULL2);

      if (!m_bIsAutoExtent) {
         layer1.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         layer2.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      iCount1 = layer1.getShapesCount();

      linesFields = new String[] { "UID_A", "UID_B", "MIN_DIST", "CENTR_DIST", "HAUSD_DIST", "BEARING" };
      linesTypes = new Class[] { String.class, String.class, Double.class, Double.class, Double.class, Double.class };

      final IVectorLayer driver = getNewVectorLayer(RESULT_LINES, Sextante.getText("Lines_Dist_Angles"),
               OutputVectorLayer.SHAPE_TYPE_LINE, linesTypes, linesFields);

      final IFeatureIterator iter1 = layer1.iterator();
      while (iter1.hasNext() && setProgress(i, iCount1)) {
         linesValues = new Object[linesFields.length];
         final IFeature feature1 = iter1.next();
         Geometry geom1 = feature1.getGeometry();
         if (useConvexHull1) {
            geom1 = geom1.convexHull();
         }
         final Point geom1_c = geom1.getCentroid();
         final Coordinate coord1 = geom1_c.getCoordinate();
         x1 = coord1.x;
         y1 = coord1.y;

         final IFeatureIterator iter2 = layer2.iterator();
         //uid_A
         linesValues[0] = feature1.getRecord().getValue(id1_idx).toString();

         j = 0;
         final GeometryFactory gf = new GeometryFactory();
         while (iter2.hasNext()) {
            final IFeature feature2 = iter2.next();
            Geometry geom2 = feature2.getGeometry();
            if (useConvexHull2) {
               geom2 = geom2.convexHull();
            }
            final Point geom2_c = geom2.getCentroid();
            final Coordinate coord2 = geom2_c.getCoordinate();
            x2 = coord2.x;
            y2 = coord2.y;
            final double dX = (x2 - x1);
            final double dY = (y2 - y1);

            //uid_B
            linesValues[1] = feature2.getRecord().getValue(id2_idx).toString();
            //min_dist
            final double min_dist = geom1.distance(geom2);
            linesValues[2] = min_dist;
            //centroid_dist
            final double centroid_dist = geom1_c.distance(geom2_c);
            linesValues[3] = centroid_dist;
            //hausdorff_dist (approximation)
            final DiscreteHausdorffDistance hausdorff = new DiscreteHausdorffDistance(geom1, geom2);
            final double hausd_dist = hausdorff.distance();
            linesValues[4] = hausd_dist;

            Double slope = (dY) / (dX);
            if (Double.isNaN(slope)) {
               slope = 0.0;
            }
            if (Double.isInfinite(slope)) {
               if (dY < 0) {
                  slope = Double.MIN_VALUE;
               }
               else {
                  slope = Double.MAX_VALUE;
               }
            }
            // To retrieve angles between -180 and 180 degrees
            // Quadrants:
            //     	0 = 0-90 grades
            //     	1 = 90-180 grades
            // 		2 = 180-270 grades (0(-90) degrees)
            // 		3 = 270-360 grades ((-90)-(-180) degrees)
            int quadrant = 0;
            if (dX < 0) {
               if (slope < 0) {
                  quadrant = 1;
               }
               else {
                  quadrant = 2;
               }
            }
            else {
               if (slope < 0) {
                  quadrant = 3;
               }
               else {
                  quadrant = 0;
               }
            }


            //angle[5] <-- It was removed
            //bearing[6] <-- Changed to [5]
            if (centroid_dist == 0) {
               //linesValues[5] = -1;
               linesValues[5] = -1;
            }
            else {
               if (Double.MIN_VALUE == slope) {
                  //linesValues[5] = -90;
                  linesValues[5] = 180;
               }
               else {

                  final double angle_aux = Math.toDegrees((Math.atan(slope)));
                  switch (quadrant) {
                     case 0: {
                        //linesValues[5] = angle_aux;
                        linesValues[5] = 90 - angle_aux;
                        break;
                     }
                     case 1: {
                        //linesValues[5] = angle_aux + 180;
                        linesValues[5] = 270 - angle_aux;
                        break;
                     }
                     case 2: {
                        //linesValues[5] = angle_aux - 180;
                        linesValues[5] = 270 - angle_aux;
                        break;
                     }
                     case 3: {
                        //linesValues[5] = angle_aux;
                        linesValues[5] = Math.abs(angle_aux) + 90;
                        break;
                     }
                  }
               }
            }
            driver.addFeature(gf.createLineString(new Coordinate[] { coord1, coord2 }), linesValues);
            j++;
         }
         iter2.close();
         i++;
      }
      iter1.close();
      driver.close();

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Distances_and_Angles"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));

      try {
         m_Parameters.addInputVectorLayer(LAYER1, Sextante.getText("Layer_1"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD1, Sextante.getText("ID1_FIELDNAME"), LAYER1);
         m_Parameters.addBoolean(CONVEXHULL1, Sextante.getText("ConvexHull_of_layer_1"), true);

         m_Parameters.addInputVectorLayer(LAYER2, Sextante.getText("Layer_2"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD2, Sextante.getText("ID2_FIELDNAME"), LAYER2);
         m_Parameters.addBoolean(CONVEXHULL2, Sextante.getText("ConvexHull_of_Layer_2"), true);
         addOutputVectorLayer(RESULT_LINES, Sextante.getText("Lines_Dist_Angles"));

      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final UndefinedParentParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final OptionalParentParameterException e) {
         Sextante.addErrorToLog(e);
      }

   }
}
