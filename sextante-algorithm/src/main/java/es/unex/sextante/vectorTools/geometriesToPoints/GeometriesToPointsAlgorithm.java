

package es.unex.sextante.vectorTools.geometriesToPoints;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

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


public class GeometriesToPointsAlgorithm
         extends
            GeoAlgorithm {

   public static final String POINTS = "POINTS";
   public static final String INPUT  = "INPUT";

   private IVectorLayer       m_Output;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Geometries_to_points"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(INPUT, Sextante.getText("Input_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         addOutputVectorLayer(POINTS, Sextante.getText("Points"), OutputVectorLayer.SHAPE_TYPE_POINT);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer input = m_Parameters.getParameterValueAsVectorLayer(INPUT);
      if (!m_bIsAutoExtent) {
         input.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      m_Output = getNewVectorLayer(POINTS, input.getName() + "[" + Sextante.getText("Points") + "]",
               IVectorLayer.SHAPE_TYPE_POINT, input.getFieldTypes(), input.getFieldNames());

      int i = 0;
      final int iShapeCount = input.getShapesCount();
      final IFeatureIterator iter = input.iterator();
      final GeometryFactory gf = new GeometryFactory();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate[] coords = geom.getCoordinates();
         for (final Coordinate element : coords) {
            m_Output.addFeature(gf.createPoint(element), feature.getRecord().getValues());
         }
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }

}
