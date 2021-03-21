package es.unex.sextante.vectorTools.cleanPointsLayer;

import java.util.Arrays;
import java.util.BitSet;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class CleanPointsLayerAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Clean_points_layer"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Points_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT,
                  true);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POINT);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;

      final IVectorLayer layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);

      final IVectorLayer output = getNewVectorLayer(RESULT, layer.getName(), layer.getShapeType(), layer.getFieldTypes(),
               layer.getFieldNames());


      i = 0;
      iShapeCount = layer.getShapesCount();
      final Coordinate coords[] = new Coordinate[iShapeCount];
      IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate coord = geom.getCoordinate();
         coords[i] = new Coordinate(coord.x, coord.y, i);
         i++;
      }
      iter.close();

      Arrays.sort(coords);

      final BitSet add = new BitSet(iShapeCount);

      add.set((int) coords[0].z);
      for (int j = 1; j < coords.length; j++) {
         if (coords[j].compareTo(coords[j - 1]) != 0) {
            add.set((int) coords[j].z);
         }
      }

      i = 0;
      iter = layer.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         if (add.get(i)) {
            output.addFeature(feature.getGeometry(), feature.getRecord().getValues());
         }
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }

}
