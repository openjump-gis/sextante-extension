

package es.unex.sextante.vectorTools.merge;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public class MergeAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYERMAIN = "LAYERMAIN";
   public static final String LAYERS    = "LAYERS";
   public static final String RESULT    = "RESULT";

   private IVectorLayer       m_Output;
   private IVectorLayer       layerMain;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;

      layerMain = m_Parameters.getParameterValueAsVectorLayer(LAYERMAIN);
      final ArrayList layers = m_Parameters.getParameterValueAsArrayList(LAYERS);

      if (!m_bIsAutoExtent) {
         layerMain.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      m_Output = getNewVectorLayer(RESULT, Sextante.getText("Result"), layerMain.getShapeType(), layerMain.getFieldTypes(),
               layerMain.getFieldNames());

      merge(layerMain);
      for (i = 0; (i < layers.size()) && !m_Task.isCanceled(); i++) {
         final IVectorLayer vect = (IVectorLayer) layers.get(i);
         vect.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         merge(vect);
      }

      return !m_Task.isCanceled();
   }


   private void merge(final IVectorLayer vect) {

      int i = 0;

      try {
         if (vect.getShapeType() == layerMain.getShapeType()) {
            final int iShapeCount = vect.getShapesCount();
            final int iFieldsMain = layerMain.getFieldCount();
            final Class[] types = layerMain.getFieldTypes();
            final int iFieldsMerge = vect.getFieldCount();
            final IFeatureIterator iter = vect.iterator();
            while (iter.hasNext() && setProgress(i, iShapeCount)) {
               final IFeature feature = iter.next();
               final Object[] values = new Object[iFieldsMain];
               final Geometry shape = feature.getGeometry();
               for (int j = 0; j < values.length; j++) {
                  final Class valueTypeMain = types[j];
                  final String sFieldNameMain = layerMain.getFieldName(j);
                  for (int k = 0; k < iFieldsMerge; k++) {
                     final Class valueTypeMerge = vect.getFieldType(k);
                     final String sFieldNameMerge = vect.getFieldName(k);
                     if (sFieldNameMerge.equalsIgnoreCase(sFieldNameMain) && valueTypeMain.equals(valueTypeMerge)) {
                        values[j] = feature.getRecord().getValue(k);
                     }
                  }
               }
               m_Output.addFeature(shape, values);
               i++;
            }
            iter.close();
         }

      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }


   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Merge"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYERMAIN, Sextante.getText("Main_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY,
                  true);
         m_Parameters.addMultipleInput(LAYERS, Sextante.getText("Additional_layers"),
                  AdditionalInfoMultipleInput.DATA_TYPE_VECTOR_ANY, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


}
