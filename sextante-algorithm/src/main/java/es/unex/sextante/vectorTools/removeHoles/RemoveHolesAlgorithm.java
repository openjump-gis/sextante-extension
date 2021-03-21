

package es.unex.sextante.vectorTools.removeHoles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public class RemoveHolesAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final IVectorLayer driver = getNewVectorLayer(RESULT, Sextante.getText("Result"), layer.getShapeType(),
               layer.getFieldTypes(), layer.getFieldNames());
      final int iTotal = layer.getShapesCount();
      int i = 0;
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(i, iTotal)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final ArrayList<Polygon> list = new ArrayList<Polygon>();
         for (int j = 0; j < geom.getNumGeometries(); j++) {
            list.add(removeHoles(geom.getGeometryN(j)));
         }
         final Geometry resultingGeom = geom.getFactory().createMultiPolygon(list.toArray(new Polygon[0]));
         driver.addFeature(resultingGeom, feature.getRecord().getValues());
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Remove_holes"));
      setGroup(Sextante.getText("Tools_for_polygon_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, LAYER);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   /**
    * Remove holes from a polygon (based on org.geotools.renderer.lite.LabelCacheDefault)
    * 
    * @param Geometry
    * @return Polygon
    */
   private Polygon removeHoles(final Geometry geom) {
      if (!(geom instanceof Polygon)) {
         return null;
      }
      final Polygon polygon = (Polygon) geom;
      LineString outer = polygon.getExteriorRing();
      if (outer.getStartPoint().distance(outer.getEndPoint()) != 0) {
         final List clist = new ArrayList(Arrays.asList(outer.getCoordinates()));
         clist.add(outer.getStartPoint().getCoordinate());
         outer = outer.getFactory().createLinearRing((Coordinate[]) clist.toArray(new Coordinate[clist.size()]));
      }
      final LinearRing r = (LinearRing) outer;
      return outer.getFactory().createPolygon(r, null);
   }
}
