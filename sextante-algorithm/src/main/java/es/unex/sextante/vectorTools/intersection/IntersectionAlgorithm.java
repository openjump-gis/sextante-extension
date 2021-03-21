

package es.unex.sextante.vectorTools.intersection;

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
import es.unex.sextante.outputs.OutputVectorLayer;


public class IntersectionAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER1 = "LAYER1";
   public static final String LAYER2 = "LAYER2";
   public static final String RESULT = "RESULT";

   private IVectorLayer       m_Output;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer layer1 = m_Parameters.getParameterValueAsVectorLayer(LAYER1);
      final IVectorLayer layer2 = m_Parameters.getParameterValueAsVectorLayer(LAYER2);

      if (!m_bIsAutoExtent) {
         layer1.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         layer2.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final Class[] fieldTypes = new Class[layer1.getFieldCount() + layer2.getFieldCount()];
      final String[] sFieldNames = new String[fieldTypes.length];

      String layerName1 = layer1.getName();
      String layerName2 = layer2.getName();
      if (layerName1.equalsIgnoreCase(layerName2)) {
         layerName1 = layerName1 + "1";
         layerName2 = layerName2 + "2";
      }

      for (int i = 0; i < layer1.getFieldCount(); i++) {
         fieldTypes[i] = layer1.getFieldType(i);
         sFieldNames[i] = layer1.getFieldName(i) + "_" + layerName1;
      }
      for (int i = 0; i < layer2.getFieldCount(); i++) {
         fieldTypes[i + layer1.getFieldCount()] = layer2.getFieldType(i);
         sFieldNames[i + layer1.getFieldCount()] = layer2.getFieldName(i) + "_" + layerName2;
      }

      m_Output = getNewVectorLayer(RESULT, Sextante.getText("Intersection"), IVectorLayer.SHAPE_TYPE_POLYGON, fieldTypes,
               sFieldNames);

      final IFeatureIterator iter = layer1.iterator();
      IFeatureIterator iter2 = layer2.iterator();

      int i = 0;
      final int iShapeCount = layer1.getShapesCount();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Object[] values = feature.getRecord().getValues();
         final Geometry g = feature.getGeometry();
         iter2 = layer2.iterator();
         while (iter2.hasNext()) {
            final IFeature feature2 = iter2.next();
            final Geometry g2 = feature2.getGeometry();
            if (g2.intersects(g)) {
               final Object[] values2 = feature2.getRecord().getValues();
               final Geometry inter = g.intersection(g2);
               final Object[] resultValues = new Object[values.length + values2.length];
               System.arraycopy(values, 0, resultValues, 0, values.length);
               System.arraycopy(values2, 0, resultValues, values.length, values2.length);
               m_Output.addFeature(inter, resultValues);
            }
         }
         i++;
      }
      iter.close();
      iter2.close();

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Intersection"));
      setGroup(Sextante.getText("Tools_for_polygon_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER1, Sextante.getText("Layer_1"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         m_Parameters.addInputVectorLayer(LAYER2, Sextante.getText("Layer_2"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Clipped_Layer"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
