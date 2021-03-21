package es.unex.sextante.vectorTools.pointsToLine;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class PointsToLineAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";

   private IVectorLayer       m_Layer;
   private IVectorLayer       m_Output;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Points_to_line"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Points"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Line"), OutputVectorLayer.SHAPE_TYPE_LINE);
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

      m_Output = getNewVectorLayer(RESULT, m_Layer.getName(), IVectorLayer.SHAPE_TYPE_LINE, m_Layer.getFieldTypes(),
               m_Layer.getFieldNames());

      final ArrayList<Coordinate> array = new ArrayList<Coordinate>();
      final GeometryFactory gf = new GeometryFactory();
      iShapeCount = m_Layer.getShapesCount();
      if (iShapeCount < 2) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Cannot_construct_line_with_less_than_2_points"));
      }

      final IFeatureIterator iter = m_Layer.iterator();
      IFeature feature = iter.next();
      Geometry geom = feature.getGeometry();
      for (int j = 0; j < geom.getNumGeometries(); j++) {
         final Geometry subgeom = geom.getGeometryN(j);
         final Coordinate coord = subgeom.getCoordinate();
         array.add(coord);
      }
      final Object[] values = feature.getRecord().getValues();
      i = 1;
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         feature = iter.next();
         geom = feature.getGeometry();
         for (int j = 0; j < geom.getNumGeometries(); j++) {
            final Geometry subgeom = geom.getGeometryN(j);
            final Coordinate coord = subgeom.getCoordinate();
            array.add(coord);
         }
         i++;
      }

      m_Output.addFeature(gf.createLineString(array.toArray(new Coordinate[0])), values);

      return !m_Task.isCanceled();

   }


}
