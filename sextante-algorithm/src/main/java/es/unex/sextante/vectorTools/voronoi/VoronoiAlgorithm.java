

package es.unex.sextante.vectorTools.voronoi;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;

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


public class VoronoiAlgorithm
         extends
            GeoAlgorithm {

   public static final String POINTS    = "POINTS";
   public static final String TRIANGLES = "TRIANGLES";

   private IVectorLayer       m_Points;
   private IVectorLayer       m_Voronoi;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Voronoi_polygons"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);

         addOutputVectorLayer(TRIANGLES, Sextante.getText("Voronoi_polygons"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      final int iShapeCount;

      m_Points = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      if (!m_bIsAutoExtent) {
         m_Points.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final Class types[] = { Integer.class };
      final String sNames[] = { "ID" };
      m_Voronoi = getNewVectorLayer(TRIANGLES, Sextante.getText("Voronoi_polygons"), IVectorLayer.SHAPE_TYPE_POLYGON, types,
               sNames);

      i = 0;
      final ArrayList<Geometry> list = new ArrayList<Geometry>();
      iShapeCount = m_Points.getShapesCount();
      final IFeatureIterator iter = m_Points.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         list.add(feature.getGeometry());
         i++;
      }
      iter.close();

      m_Task.setDeterminate(false);
      final GeometryFactory gf = new GeometryFactory();
      final Geometry geomcol = gf.createGeometryCollection(list.toArray(new Geometry[0]));
      final VoronoiDiagramBuilder voronoi = new VoronoiDiagramBuilder();
      voronoi.setSites(geomcol);
      final Geometry polygons = voronoi.getDiagram(gf);

      final int iPolygons = polygons.getNumGeometries();
      for (int iPolygon = 0; iPolygon < iPolygons; iPolygon++) {
         final Geometry triangle = polygons.getGeometryN(iPolygon);
         final Object[] record = { new Integer(iPolygon) };
         m_Voronoi.addFeature(triangle, record);
      }

      return !m_Task.isCanceled();

   }

}
