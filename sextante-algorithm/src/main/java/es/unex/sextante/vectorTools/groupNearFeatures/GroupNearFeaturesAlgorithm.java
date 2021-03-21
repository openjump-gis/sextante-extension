

package es.unex.sextante.vectorTools.groupNearFeatures;

import java.util.ArrayList;
import java.util.HashMap;

import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
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


public class GroupNearFeaturesAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER     = "LAYER";
   public static final String TOLERANCE = "TOLERANCE";
   public static final String RESULT    = "RESULT";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Group_near_features"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);

         m_Parameters.addNumericalValue(TOLERANCE, Sextante.getText("Tolerance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 5.0, 0.0001, Double.MAX_VALUE);

         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_UNDEFINED);

      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final int i = 0;
      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(GroupNearFeaturesAlgorithm.LAYER);
      final double tolerance = m_Parameters.getParameterValueAsDouble(GroupNearFeaturesAlgorithm.TOLERANCE);

      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final Class[] in_ftypes = layerIn.getFieldTypes();
      final Class[] out_ftypes = new Class[in_ftypes.length + 1];
      System.arraycopy(in_ftypes, 0, out_ftypes, 0, in_ftypes.length);
      out_ftypes[out_ftypes.length - 1] = Integer.class;

      final String[] in_fnames = layerIn.getFieldNames();
      final String[] out_fnames = new String[in_ftypes.length + 1];
      System.arraycopy(in_fnames, 0, out_fnames, 0, in_fnames.length);
      out_fnames[out_fnames.length - 1] = "GROUP_ID";

      final IVectorLayer driver = getNewVectorLayer(GroupNearFeaturesAlgorithm.RESULT, Sextante.getText("Grouped_Layer"),
               layerIn.getShapeType(), out_ftypes, out_fnames);

      final int iTotal = layerIn.getShapesCount();
      int groupID = 0;
      final int[] groupedMap = new int[iTotal];
      for (int k = 0; k < iTotal; k++) {
         groupedMap[k] = -1;
      }
      //Array of arrays of groups with all geoms of the group
      final HashMap groupGeoms = new HashMap();

      final IFeatureIterator iter1 = layerIn.iterator();
      final Object[] values = new Object[out_fnames.length];
      for (int featCount1 = 0; iter1.hasNext(); featCount1++) {
         final IFeature feat1 = iter1.next();
         Geometry geom1 = feat1.getGeometry();
         if (groupedMap[featCount1] != -1) {
            continue;
         }
         //A new group is created
         groupedMap[featCount1] = groupID;
         ArrayList geomArray = new ArrayList();
         geomArray.add(geom1);
         groupGeoms.put(groupID, geomArray);
         // Also add this feature on the FeatureCollection
         Object[] aux_values = feat1.getRecord().getValues();
         System.arraycopy(aux_values, 0, values, 0, aux_values.length);
         values[values.length - 1] = groupID;
         driver.addFeature(geom1, values);

         IFeatureIterator iter2 = layerIn.iterator();
         for (int featCount2 = 0; iter2.hasNext(); featCount2++) {
            final IFeature feat2 = iter2.next();
            if (groupedMap[featCount2] != -1) {
               continue;
            }
            final Geometry geom2 = feat2.getGeometry();
            geomArray = (ArrayList) groupGeoms.get(groupID);
            for (int j = 0; j < geomArray.size(); j++) {
               geom1 = (Geometry) geomArray.get(j);
               final double dist = geom1.distance(geom2);
               if (dist < tolerance) {
                  groupedMap[featCount2] = groupID;
                  geomArray = (ArrayList) groupGeoms.get(groupID);
                  geomArray.add(geom2);
                  aux_values = feat2.getRecord().getValues();
                  System.arraycopy(aux_values, 0, values, 0, aux_values.length);
                  values[values.length - 1] = groupID;
                  driver.addFeature(geom2, values);
                  // It needed to reset the iterator to check if any previous feature belongs to this group
                  iter2 = layerIn.iterator();
                  featCount2 = -1;
                  break;
               }
            }
         }
         iter2.close();
         groupID++;
         setProgress(i, iTotal);
      }
      iter1.close();
      return !m_Task.isCanceled();
   }
}
