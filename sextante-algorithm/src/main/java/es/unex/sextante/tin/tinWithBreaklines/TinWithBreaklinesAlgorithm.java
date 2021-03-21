

package es.unex.sextante.tin.tinWithBreaklines;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
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
import es.unex.sextante.outputs.OutputVectorLayer;


public class TinWithBreaklinesAlgorithm
         extends
            GeoAlgorithm {

   public static final String POINTS          = "POINTS";
   public static final String LINES           = "LINES";
   public static final String TRIANGLES       = "TRIANGLES";
   public static final String ELEVATION_FIELD = "ELEVATION_FIELD";

   private IVectorLayer       m_Points        = null;
   private IVectorLayer       m_Lines         = null;
   private IVectorLayer       m_Triangles     = null;
   private int                m_iElevationField;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Create_tin_with_breaklines"));
      setGroup(Sextante.getText("TIN"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);
         m_Parameters.addInputVectorLayer(LINES, Sextante.getText("Breaklines"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);

         m_Parameters.addTableField(ELEVATION_FIELD, Sextante.getText("Height_field"), "POINTS");

         addOutputVectorLayer(TRIANGLES, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;
      final GeometryFactory gf = new GeometryFactory();

      m_Points = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      m_Lines = m_Parameters.getParameterValueAsVectorLayer(LINES);
      if (!m_bIsAutoExtent) {
         m_Points.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         m_Lines.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      m_iElevationField = m_Parameters.getParameterValueAsInt(ELEVATION_FIELD);

      final Class types[] = { Integer.class, String.class, Integer.class, Double.class };
      final String sNames[] = { "ID", "HardLines", "type", "elevation" };
      m_Triangles = getNewVectorLayer(TRIANGLES, "TIN_" + m_Points.getName(), IVectorLayer.SHAPE_TYPE_POLYGON, types, sNames);

      i = 0;
      final ArrayList<Geometry> pointsList = new ArrayList<Geometry>();
      iShapeCount = m_Points.getShapesCount();
      IFeatureIterator iter = m_Points.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Coordinate coord = feature.getGeometry().getCoordinate();
         try {
            final double dElevation = Double.parseDouble(feature.getRecord().getValue(m_iElevationField).toString());
            coord.z = dElevation;
         }
         catch (final Exception e) {
            throw new GeoAlgorithmExecutionException(Sextante.getText("Wrong_values_in_elevation_field"));
         }
         pointsList.add(gf.createPoint(coord));
         i++;
      }
      iter.close();

      final ArrayList<Geometry> linesList = new ArrayList<Geometry>();
      iShapeCount = m_Lines.getShapesCount();
      iter = m_Lines.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         linesList.add(feature.getGeometry());
         i++;
      }
      iter.close();
      m_Task.setDeterminate(false);
      final Geometry pointsGeomcol = gf.createGeometryCollection(pointsList.toArray(new Geometry[0]));
      final Geometry linesGeomcol = gf.createGeometryCollection(linesList.toArray(new Geometry[0]));
      final ConformingDelaunayTriangulationBuilder delaunay = new ConformingDelaunayTriangulationBuilder();
      delaunay.setSites(pointsGeomcol);
      delaunay.setConstraints(linesGeomcol);
      final Geometry triangles = delaunay.getTriangles(gf);

      final int iTriangles = triangles.getNumGeometries();
      for (int iTriangle = 0; iTriangle < iTriangles; iTriangle++) {
         final Geometry triangle = triangles.getGeometryN(iTriangle);
         final Object[] record = { new Integer(iTriangle), "", -1, getElevation(triangle) };
         m_Triangles.addFeature(triangle, record);
      }

      return !m_Task.isCanceled();

   }


   /* return elevation as average of all 3 triangle vertices */
   private double getElevation(final Geometry triangle) {

      final Coordinate[] coords = triangle.getCoordinates();
      return (coords[0].z + coords[1].z + coords[2].z) / 3.;

   }

}
