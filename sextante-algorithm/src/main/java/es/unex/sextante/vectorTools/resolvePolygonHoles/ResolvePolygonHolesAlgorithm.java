package es.unex.sextante.vectorTools.resolvePolygonHoles;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class ResolvePolygonHolesAlgorithm
         extends
            GeoAlgorithm {

   public static final String     RESULT   = "RESULT";
   public static final String     POLYGONS = "POLYGONS";

   private IVectorLayer           m_Polygons;

   private IVectorLayer           m_Output;

   private NearestNeighbourFinder m_NNF;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("ResolvePolygonHoles"));
      setGroup(Sextante.getText("Tools_for_polygon_layers"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputVectorLayer(POLYGONS, Sextante.getText("Polygons_layer"),
                  AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {

      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;

      m_Polygons = m_Parameters.getParameterValueAsVectorLayer(POLYGONS);

      m_Output = getNewVectorLayer(RESULT, m_Polygons.getName(), m_Polygons.getShapeType(), m_Polygons.getFieldTypes(),
               m_Polygons.getFieldNames());

      m_NNF = new NearestNeighbourFinder(m_Polygons, this.m_Task);

      iShapeCount = m_Polygons.getShapesCount();
      i = 0;
      final IFeatureIterator iter = m_Polygons.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         resolveHoles(feature);
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   private void resolveHoles(final IFeature feature) {

      final ArrayList<Polygon> polygons = new ArrayList<Polygon>();
      final Geometry geom = feature.getGeometry();
      for (int i = 0; i < geom.getNumGeometries(); i++) {
         final Polygon poly = resolveHolesInSimplePolygon((Polygon) geom.getGeometryN(i));
         if (poly != null) {
            polygons.add(poly);
         }
      }

      if (!polygons.isEmpty()) {
         final GeometryFactory gf = new GeometryFactory();
         final MultiPolygon polygonWithHoles = gf.createMultiPolygon(polygons.toArray(new Polygon[0]));
         m_Output.addFeature(polygonWithHoles, feature.getRecord().getValues());
      }

   }


   private Polygon resolveHolesInSimplePolygon(final Polygon polygon) {

      final GeometryFactory gf = new GeometryFactory();
      final ArrayList<LinearRing> holes = new ArrayList<LinearRing>();
      for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
         holes.add(gf.createLinearRing(polygon.getInteriorRingN(i).getCoordinates()));
      }

      final IFeature[] holeCandidates = m_NNF.getClosestFeatures(polygon);

      for (int i = 0; i < holeCandidates.length; i++) {
         final Geometry geom = holeCandidates[i].getGeometry();
         for (int j = 0; j < geom.getNumGeometries(); j++) {
            final Geometry subgeom = geom.getGeometryN(j);
            if (!polygon.equals(subgeom)) {
               if (polygon.contains(subgeom)) {
                  holes.add(gf.createLinearRing(subgeom.getCoordinates()));
               }
            }
         }
      }

      //check that the polygon is not a hole itself
      //      for (int i = 0; i < holeCandidates.length; i++) {
      //         final Geometry geom = holeCandidates[i].getGeometry();
      //         for (int j = 0; j < geom.getNumGeometries(); j++) {
      //            final Geometry subgeom = geom.getGeometryN(j);
      //            if (!polygon.equals(subgeom)) {
      //               if (subgeom.contains(polygon)) {
      //                  return null;
      //               }
      //            }
      //         }
      //      }

      return gf.createPolygon(gf.createLinearRing(polygon.getExteriorRing().getCoordinates()), holes.toArray(new LinearRing[0]));

   }

}
