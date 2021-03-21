package es.unex.sextante.vectorTools.NNSpatialJoin;

import org.locationtech.jts.geom.Coordinate;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;


public class NNSpatialJoinAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT    = "RESULT";
   public static final String MAIN      = "MAIN";
   public static final String SECONDARY = "SECONDARY";

   private IVectorLayer       m_MainLayer;
   private IVectorLayer       m_SecondaryLayer;

   private IVectorLayer       m_Output;
   private SextanteRTree      m_SearchEngine;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("NN_Spatial_Join"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(MAIN, Sextante.getText("Main_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addInputVectorLayer(SECONDARY, Sextante.getText("Additional_layer"),
                  AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_UNDEFINED);
      }
      catch (final RepeatedParameterNameException e) {

      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;

      m_MainLayer = m_Parameters.getParameterValueAsVectorLayer(MAIN);
      m_SecondaryLayer = m_Parameters.getParameterValueAsVectorLayer(SECONDARY);

      if (!m_bIsAutoExtent) {
         m_MainLayer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         m_SecondaryLayer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      m_SearchEngine = new SextanteRTree(m_SecondaryLayer, m_Task);

      final Class[] types = new Class[m_SecondaryLayer.getFieldCount() + m_MainLayer.getFieldCount()];
      final String[] sFields = new String[m_SecondaryLayer.getFieldCount() + m_MainLayer.getFieldCount()];
      for (i = 0; i < m_MainLayer.getFieldCount(); i++) {
         sFields[i] = m_MainLayer.getFieldName(i);
         types[i] = m_MainLayer.getFieldType(i);
      }
      for (i = 0; i < m_SecondaryLayer.getFieldCount(); i++) {
         types[i + m_MainLayer.getFieldCount()] = m_SecondaryLayer.getFieldType(i);
         sFields[i + m_MainLayer.getFieldCount()] = m_SecondaryLayer.getFieldName(i);
      }


      m_Output = getNewVectorLayer(RESULT, m_MainLayer.getName(), m_MainLayer.getShapeType(), types, sFields);

      iShapeCount = m_MainLayer.getShapesCount();
      i = 0;
      final IFeatureIterator iter = m_MainLayer.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         performSpatialJoin(feature);
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   private void performSpatialJoin(final IFeature feature) {

      final Coordinate coord = feature.getGeometry().getCoordinate();
      final IFeature closestFeature = m_SearchEngine.getClosestFeature(coord.x, coord.y);
      final Object[] record = new Object[m_SecondaryLayer.getFieldCount() + m_MainLayer.getFieldCount()];

      final IRecord closestFeatureRecord = closestFeature.getRecord();
      for (int i = 0; i < m_SecondaryLayer.getFieldCount(); i++) {
         record[i + m_MainLayer.getFieldCount()] = closestFeatureRecord.getValue(i);
      }
      final IRecord mainRecord = feature.getRecord();
      for (int i = 0; i < m_MainLayer.getFieldCount(); i++) {
         record[i] = mainRecord.getValue(i);
      }

      m_Output.addFeature(feature.getGeometry(), record);


   }

}
