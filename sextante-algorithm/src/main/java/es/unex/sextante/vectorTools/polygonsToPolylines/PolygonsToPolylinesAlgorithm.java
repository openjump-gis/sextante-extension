

package es.unex.sextante.vectorTools.polygonsToPolylines;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
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


public class PolygonsToPolylinesAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";

   private IVectorLayer       m_Layer;
   private IVectorLayer       m_Output;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Polygons_to_polylines"));
      setGroup(Sextante.getText("Tools_for_polygon_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Polygons"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Lines"), OutputVectorLayer.SHAPE_TYPE_LINE);
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

      m_Output = getNewVectorLayer(RESULT, Sextante.getText("Lines"), IVectorLayer.SHAPE_TYPE_LINE, m_Layer.getFieldTypes(),
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
            if (subgeom instanceof Polygon) {
               final Polygon poly = (Polygon) subgeom;
               array.add(poly.getExteriorRing());
               for (int k = 0; k < poly.getNumInteriorRing(); k++) {
                  array.add(poly.getInteriorRingN(k));
               }
            }
         }
         if (array.size() != 0) {
            final LineString[] lines = new LineString[array.size()];
            for (int j = 0; j < array.size(); j++) {
               lines[j] = (LineString) array.get(j);
            }
            m_Output.addFeature(gf.createMultiLineString(lines), feature.getRecord().getValues());
         }
         i++;
      }

      return !m_Task.isCanceled();

   }


}
