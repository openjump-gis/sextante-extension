

package es.unex.sextante.vectorTools.medialAxis;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
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
import es.unex.sextante.outputs.OutputVectorLayer;
import gishur.core.SimpleList;
import gishur.x.XPoint;
import gishur.x.XPolygon;
import gishur.x.XSegment;
import gishur.x.voronoi.Skeleton;


public class MedialAxisAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i = 0;

      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      final IVectorLayer result = getNewVectorLayer(RESULT, Sextante.getText("Medial_Axis"), IVectorLayer.SHAPE_TYPE_LINE,
               layerIn.getFieldTypes(), layerIn.getFieldNames());

      final IFeatureIterator iter = layerIn.iterator();
      final int iTotal = layerIn.getShapesCount();
      while (iter.hasNext() && setProgress(i, iTotal)) {
         final IFeature feature = iter.next();
         final MultiLineString axis = getMedialAxis(((Polygon) feature.getGeometry().getGeometryN(0)));
         result.addFeature(axis, feature.getRecord().getValues());
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();
   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Medial_Axis"));
      setGroup(Sextante.getText("Tools_for_polygon_layers"));

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Polygons"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Medial_Axis"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private MultiLineString getMedialAxis(final Polygon poly) {

      final ArrayList<LineString> list = new ArrayList<LineString>();
      final GeometryFactory gf = new GeometryFactory();
      final Coordinate[] coords = poly.getExteriorRing().getCoordinates();
      final XPoint xpoints[] = new XPoint[coords.length];
      for (int i = 0; i < coords.length; i++) {
         xpoints[i] = new XPoint(coords[i].x, coords[i].y);
      }
      final XPolygon xpoly = new XPolygon(xpoints);
      final Skeleton skeleton = new Skeleton(xpoly);
      skeleton.execute();
      final SimpleList lines = skeleton.getLines(false);
      final Object[] array = lines.convertValuesToArray();

      for (int i = 0; i < array.length; i++) {
         final XSegment segment = (XSegment) array[i];
         System.out.println(segment.toString());
         final Coordinate[] segmentCoords = new Coordinate[] {
                  new Coordinate(segment.getStartPoint().x, segment.getStartPoint().y),
                  new Coordinate(segment.getEndPoint().x, segment.getEndPoint().y) };
         list.add(gf.createLineString(segmentCoords));
      }

      return gf.createMultiLineString(list.toArray(new LineString[0]));


   }

}
