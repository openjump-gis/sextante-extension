

package es.unex.sextante.vectorTools.extendLinesLayerWithGrids;

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


/**
 * Sample the values of Grids on the first and last vertex of each line on a linestring layer. It creates a new Vector Layer
 * adding two attributes: <i>'1_'rasterlayer</i> for first vertex and <i>'2_'rasterlayer</i> for last vertex.
 * 
 * Based on ExtendPointsLayerWithGridsAlgorithm
 * 
 * @author Nacho Varela
 * 
 */

//TODO Calculate slope, differences between values, etc...
public class ExtendLinesLayerWithGridsAlgorithm
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

      setName(Sextante.getText("Sample_lines_extreme_points"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Input_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         m_Parameters.addMultipleInput(GRIDS, Sextante.getText("Raster_layers"), AdditionalInfoMultipleInput.DATA_TYPE_BAND, true);
         m_Parameters.addBoolean(INTERPOLATE, Sextante.getText("Use_interpolation"), true);
         addOutputVectorLayer(RESULT, Sextante.getText("Sampled_lines"), OutputVectorLayer.SHAPE_TYPE_LINE, LAYER);
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

      dValues = new double[2 * m_Grids.size()][iShapeCount];
      iTotalProgress = 2 * iShapeCount * m_Grids.size();
      final IFeatureIterator iter = m_Layer.iterator();
      for (iLayer = 0; iLayer < m_Grids.size(); iLayer++) {
         i = 0;
         final RasterLayerAndBand rab = (RasterLayerAndBand) m_Grids.get(iLayer);
         final IRasterLayer raster = rab.getRasterLayer();
         final int iBand = rab.getBand();
         raster.setFullExtent();
         if (!bInterpolate) {
            raster.setInterpolationMethod(GridWrapper.INTERPOLATION_NearestNeighbour);
         }
         while (iter.hasNext() && setProgress(iProgress, iTotalProgress)) {
            final IFeature feature = iter.next();
            final Geometry geom = feature.getGeometry();
            final Coordinate[] coords = geom.getCoordinates();
            dValues[iLayer][i] = raster.getValueAt(coords[0].x, coords[0].y, iBand);
            dValues[iLayer + 1][i] = raster.getValueAt(coords[coords.length - 1].x, coords[coords.length - 1].y, iBand);
            iProgress++;
            i++;
         }
      }

      if (m_Task.isCanceled()) {
         return false;
      }
      // 2 * m_Grids.size() because first and last vertex has its own attribute
      final Object[][] values = new Object[2 * m_Grids.size()][iShapeCount];
      final String[] sNames = new String[2 * m_Grids.size()];
      final Class[] types = new Class[2 * m_Grids.size()];
      for (i = 0; i < m_Grids.size(); i++) {
         final RasterLayerAndBand rab = (RasterLayerAndBand) m_Grids.get(i);
         grid = rab.getRasterLayer();
         sNames[i] = "1_" + grid.getName();

         types[i] = Double.class;
         for (j = 0; j < iShapeCount; j++) {
            values[i][j] = new Double(dValues[i][j]);
         }
         sNames[(i + 1)] = "2_" + grid.getName();

         types[(i + 1)] = Double.class;
         for (j = 0; j < (iShapeCount); j++) {
            values[i][j] = new Double(dValues[i][j]);
            values[(i + 1)][j] = new Double(dValues[(i + 1)][j]);
         }
      }
      final IOutputChannel channel = getOutputChannel(RESULT);
      final OutputVectorLayer out = new OutputVectorLayer();
      out.setName(RESULT);
      out.setOutputChannel(channel);
      out.setDescription(m_Layer.getName() + "[" + Sextante.getText("sampled") + "]");
      out.setOutputObject(ShapesTools.addFields(m_OutputFactory, m_Layer, channel, sNames, values, types));
      addOutputObject(out);

      return !m_Task.isCanceled();

   }

}
