

package es.unex.sextante.vectorTools.pointCoordinates;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.shapesTools.ShapesTools;


public class pointCoordinatesAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER       = "LAYER";
   public static final String GRIDS       = "GRIDS";
   public static final String INTERPOLATE = "INTERPOLATE";
   public static final String RESULT      = "RESULT";

   private IVectorLayer       m_Layer;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Add_coordinates_to_points"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Points_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);
         addOutputVectorLayer(RESULT, Sextante.getText("Points"), OutputVectorLayer.SHAPE_TYPE_POINT, LAYER);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int iProgress = 0;
      int iShapeCount;
      Double[][] values;

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);

      if (!m_bIsAutoExtent) {
         m_Layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      iShapeCount = m_Layer.getShapesCount();
      values = new Double[2][iShapeCount];
      final IFeatureIterator iter = m_Layer.iterator();

      while (iter.hasNext() && setProgress(iProgress, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate[] coords = geom.getCoordinates();
         values[0][iProgress] = new Double(coords[0].x);
         values[1][iProgress] = new Double(coords[0].y);
         iProgress++;
      }

      if (m_Task.isCanceled()) {
         return false;
      }

      final String[] sNames = { "X", "Y" };
      final Class[] types = { Double.class, Double.class };

      final IOutputChannel channel = getOutputChannel(RESULT);
      final OutputVectorLayer out = new OutputVectorLayer();
      out.setName(RESULT);
      out.setOutputChannel(channel);
      out.setDescription(m_Layer.getName());
      out.setOutputObject(ShapesTools.addFields(m_OutputFactory, m_Layer, channel, sNames, values, types));
      addOutputObject(out);

      return !m_Task.isCanceled();

   }

}
