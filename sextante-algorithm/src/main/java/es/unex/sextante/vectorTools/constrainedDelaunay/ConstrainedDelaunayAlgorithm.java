

package es.unex.sextante.vectorTools.constrainedDelaunay;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.ConformingDelaunayTriangulationBuilder;

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


public class ConstrainedDelaunayAlgorithm
         extends
            GeoAlgorithm {

   public static final String POINTS    = "POINTS";
   public static final String LINES     = "LINES";
   public static final String TRIANGLES = "TRIANGLES";

   private IVectorLayer       m_Points;
   private IVectorLayer       m_Lines;
   private IVectorLayer       m_Triangles;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Constrained_Delaunay_triangulation"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);
         m_Parameters.addInputVectorLayer(LINES, Sextante.getText("Lines_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);

         addOutputVectorLayer(TRIANGLES, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;

      m_Points = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      m_Lines = m_Parameters.getParameterValueAsVectorLayer(LINES);
      if (!m_bIsAutoExtent) {
         m_Points.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         m_Lines.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final Class types[] = { Integer.class };
      final String sNames[] = { "ID" };
      m_Triangles = getNewVectorLayer(TRIANGLES, m_Points.getName() + "[" + Sextante.getText("triangulated") + "]",
               IVectorLayer.SHAPE_TYPE_POLYGON, types, sNames);

      i = 0;
      final ArrayList<Geometry> list = new ArrayList<Geometry>();
      iShapeCount = m_Points.getShapesCount();
      IFeatureIterator iter = m_Points.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         list.add(feature.getGeometry());
         i++;
      }
      iter.close();


      final GeometryFactory gf = new GeometryFactory();
      final Geometry geomcolPoints = gf.createGeometryCollection(list.toArray(new Geometry[0]));

      i = 0;
      iShapeCount = m_Lines.getShapesCount();
      list.clear();
      iter = m_Lines.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         list.add(feature.getGeometry());
         i++;
      }
      iter.close();

      final Geometry geomcolLines = gf.createGeometryCollection(list.toArray(new Geometry[0]));

      m_Task.setDeterminate(false);
      final ConformingDelaunayTriangulationBuilder delaunay = new ConformingDelaunayTriangulationBuilder();
      delaunay.setSites(geomcolPoints);
      delaunay.setConstraints(geomcolLines);
      final Geometry triangles = delaunay.getTriangles(gf);

      final int iTriangles = triangles.getNumGeometries();
      for (int iTriangle = 0; iTriangle < iTriangles; iTriangle++) {
         final Geometry triangle = triangles.getGeometryN(iTriangle);
         final Object[] record = { new Integer(iTriangle) };
         m_Triangles.addFeature(triangle, record);
      }

      return !m_Task.isCanceled();

   }
}
