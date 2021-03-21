

package es.unex.sextante.vectorTools.removeRepeatedGeometries;

import java.util.HashMap;

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


public class RemoveRepeatedGeometriesAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Remove_repeated_geometries"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Input_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Unique_Geometries"), OutputVectorLayer.SHAPE_TYPE_UNDEFINED, LAYER);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i = 0;
      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(RemoveRepeatedGeometriesAlgorithm.LAYER);
      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final IVectorLayer driver = getNewVectorLayer(RemoveRepeatedGeometriesAlgorithm.RESULT,
               Sextante.getText("Unique_Geometries"), layerIn.getShapeType(), layerIn.getFieldTypes(), layerIn.getFieldNames());

      //Map geometries using WKT format to index
      final HashMap geomHashMap = new HashMap();

      final int iTotal = layerIn.getShapesCount();
      final IFeatureIterator iter = layerIn.iterator();

      while (iter.hasNext() && setProgress(i, iTotal)) {
         final IFeature feat = iter.next();
         final Geometry geom = feat.getGeometry();
         final Object[] values = feat.getRecord().getValues();
         if (geomHashMap.containsKey(geom.toText())) {
            continue;
         }
         else {
            geomHashMap.put(geom.toText(), true);
         }
         driver.addFeature(geom, values);
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }

}
