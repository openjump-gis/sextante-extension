

package es.unex.sextante.vectorTools.polylinesToPolygons;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
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
import es.unex.sextante.outputs.OutputVectorLayer;


public class PolylinesToPolygonsAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";

   private IVectorLayer       m_Layer;
   private IVectorLayer       m_Output;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Polylines_to_polygons"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Polylines"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Polygons"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         m_Layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      m_Output = getNewVectorLayer(RESULT, m_Layer.getName(), IVectorLayer.SHAPE_TYPE_POLYGON, m_Layer.getFieldTypes(),
               m_Layer.getFieldNames());


      final ArrayList array = new ArrayList();
      final GeometryFactory gf = new GeometryFactory();
      iShapeCount = m_Layer.getShapesCount();
      final IFeatureIterator iter = m_Layer.iterator();
      i = 0;
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         array.clear();
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         for (int j = 0; j < geom.getNumGeometries(); j++) {
            final Geometry subgeom = geom.getGeometryN(j);
            final Coordinate[] lineCoords = subgeom.getCoordinates();
            final Coordinate[] ringCoords = new Coordinate[lineCoords.length + 1];
            System.arraycopy(lineCoords, 0, ringCoords, 0, lineCoords.length);
            ringCoords[lineCoords.length] = new Coordinate(lineCoords[0].x, lineCoords[0].y);
            final LinearRing ring = gf.createLinearRing(ringCoords);
            final Polygon polygon = gf.createPolygon(ring, null);
            array.add(polygon);
         }
         final Polygon[] polygons = new Polygon[array.size()];
         for (int j = 0; j < array.size(); j++) {
            polygons[j] = (Polygon) array.get(j);
         }
         m_Output.addFeature(gf.createMultiPolygon(polygons), feature.getRecord().getValues());
         i++;
      }

      return !m_Task.isCanceled();

   }


}
