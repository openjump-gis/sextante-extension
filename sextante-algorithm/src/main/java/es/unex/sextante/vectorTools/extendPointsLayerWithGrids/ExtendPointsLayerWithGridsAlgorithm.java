

package es.unex.sextante.vectorTools.extendPointsLayerWithGrids;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.RasterLayerAndBand;
import es.unex.sextante.rasterWrappers.GridWrapper;
import es.unex.sextante.shapesTools.ShapesTools;


public class ExtendPointsLayerWithGridsAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER       = "LAYER";
   public static final String GRIDS       = "GRIDS";
   public static final String INTERPOLATE = "INTERPOLATE";
   public static final String RESULT      = "RESULT";

   private IVectorLayer       m_Layer;
   private ArrayList          m_Grids;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Sample_raster_layers"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Points_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);
         m_Parameters.addMultipleInput(GRIDS, Sextante.getText("Raster_layers"), AdditionalInfoMultipleInput.DATA_TYPE_BAND, true);
         m_Parameters.addBoolean(INTERPOLATE, Sextante.getText("Use_interpolation"), true);
         addOutputVectorLayer(RESULT, Sextante.getText("Points"), OutputVectorLayer.SHAPE_TYPE_POINT, LAYER);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i, j;
      int iTotalProgress;
      int iProgress = 0;
      int iLayer;
      int iShapeCount;
      double[][] dValues;
      boolean bInterpolate;
      IRasterLayer grid;

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      m_Grids = (ArrayList) m_Parameters.getParameterValueAsObject(GRIDS);
      bInterpolate = m_Parameters.getParameterValueAsBoolean(INTERPOLATE);
      if (!m_bIsAutoExtent) {
         m_Layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      iShapeCount = m_Layer.getShapesCount();
      dValues = new double[m_Grids.size()][iShapeCount];
      iTotalProgress = iShapeCount * m_Grids.size();

      for (iLayer = 0; iLayer < m_Grids.size(); iLayer++) {
         final RasterLayerAndBand rab = (RasterLayerAndBand) m_Grids.get(iLayer);
         final IRasterLayer layer = rab.getRasterLayer();
         final int iBand = rab.getBand();
         layer.setFullExtent();
         if (bInterpolate == false) {
            layer.setInterpolationMethod(GridWrapper.INTERPOLATION_NearestNeighbour);
         }
         final IFeatureIterator iter = m_Layer.iterator();
         i = 0;
         while (iter.hasNext() && setProgress(iProgress, iTotalProgress)) {
            final IFeature feature = iter.next();
            final Geometry geom = feature.getGeometry();
            final Coordinate[] coords = geom.getCoordinates();
            dValues[iLayer][i] = layer.getValueAt(coords[0].x, coords[0].y, iBand);
            iProgress++;
            i++;
         }
         iter.close();
      }

      if (m_Task.isCanceled()) {
         return false;
      }

      final Object[][] values = new Object[m_Grids.size()][iShapeCount];
      final String[] sNames = new String[m_Grids.size()];
      final Class[] types = new Class[m_Grids.size()];
      for (i = 0; i < m_Grids.size(); i++) {
         final RasterLayerAndBand rab = (RasterLayerAndBand) m_Grids.get(i);
         grid = rab.getRasterLayer();
         sNames[i] = grid.getName();
         types[i] = Double.class;
         for (j = 0; j < iShapeCount; j++) {
            values[i][j] = new Double(dValues[i][j]);
         }
      }
      final IOutputChannel channel = getOutputChannel(RESULT);
      final OutputVectorLayer out = new OutputVectorLayer();
      out.setName(RESULT);
      out.setOutputChannel(channel);
      out.setDescription(Sextante.getText("Points"));
      out.setOutputObject(ShapesTools.addFields(m_OutputFactory, m_Layer, channel, sNames, values, types));
      addOutputObject(out);

      return !m_Task.isCanceled();

   }

}
