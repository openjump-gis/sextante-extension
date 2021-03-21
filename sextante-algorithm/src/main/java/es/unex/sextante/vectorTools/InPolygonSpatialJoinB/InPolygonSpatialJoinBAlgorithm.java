package es.unex.sextante.vectorTools.InPolygonSpatialJoinB;

import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.outputs.OutputVectorLayer;


public class InPolygonSpatialJoinBAlgorithm
         extends
            GeoAlgorithm {

   public static final String     RESULT    = "RESULT";
   public static final String     MAIN      = "MAIN";
   public static final String     SECONDARY = "SECONDARY";
   public static final String     FIELD     = "FIELD";
   //public static final String     FIELDNAME = "FIELDNAME";


   private IVectorLayer           m_MainLayer;
   private IVectorLayer           m_SecondaryLayer;

   private IVectorLayer           m_Output;

   private NearestNeighbourFinder m_NNF;
   private int                    m_iField;
   private Class                  m_Type;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("InPolygonSpatialJoinB"));
      setGroup(Sextante.getText("Tools_for_polygon_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(MAIN, Sextante.getText("Polygons"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         m_Parameters.addInputVectorLayer(SECONDARY, Sextante.getText("Secondary_layer"),
                  AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), MAIN);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POLYGON, SECONDARY);
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
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

      m_iField = m_Parameters.getParameterValueAsInt(FIELD);

      final String[] sFields = new String[m_SecondaryLayer.getFieldCount() + 1];
      final Class[] types = new Class[m_SecondaryLayer.getFieldCount() + 1];
      for (i = 0; i < m_SecondaryLayer.getFieldCount(); i++) {
         sFields[i] = m_SecondaryLayer.getFieldName(i);
         types[i] = m_SecondaryLayer.getFieldType(i);
      }
      sFields[m_SecondaryLayer.getFieldCount()] = m_MainLayer.getFieldName(m_iField);
      m_Type = m_MainLayer.getFieldType(m_iField);
      types[m_SecondaryLayer.getFieldCount()] = m_Type;

      m_Output = getNewVectorLayer(RESULT, m_SecondaryLayer.getName(), m_SecondaryLayer.getShapeType(), types, sFields);

      m_NNF = new NearestNeighbourFinder(m_MainLayer, this.m_Task);

      iShapeCount = m_SecondaryLayer.getShapesCount();
      i = 0;
      setProgressText(Sextante.getText("Processing"));
      final IFeatureIterator iter = m_SecondaryLayer.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         performSpatialJoin(feature);
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   private void performSpatialJoin(final IFeature feature) {

      final Geometry geom = feature.getGeometry();
      final IFeature[] polygons = m_NNF.getClosestPoints(geom);

      for (final IFeature polygon : polygons) {
         if (polygon.getGeometry().contains(geom)) {
            m_Output.addFeature(feature.getGeometry(), calculateRecord(polygon.getRecord().getValue(m_iField),
                     feature.getRecord().getValues()));
            return;
         }
      }

      Object value;

      if (Integer.class.isAssignableFrom(m_Type)) {
         value = new Integer(-99999);
      }
      else if (Double.class.isAssignableFrom(m_Type)) {
         value = new Integer(-99999);

      }
      else {
         value = "";
      }

      m_Output.addFeature(feature.getGeometry(), calculateRecord(value, feature.getRecord().getValues()));

   }


   private Object[] calculateRecord(final Object value,
                                    final Object[] record) {

      final Object[] values = new Object[record.length + 1];
      System.arraycopy(record, 0, values, 0, record.length);
      values[values.length - 1] = value;
      return values;

   }


}
