

package es.unex.sextante.vectorTools.dissolveMultiple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

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
import es.unex.sextante.math.simpleStats.SimpleStats;
import es.unex.sextante.outputs.OutputVectorLayer;


public class DissolveMultipleAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT             = "RESULT";
   public static final String LAYER              = "LAYER";
   public static final String GROUPING_FIELDS    = "GROUPING_FIELD";
   public static final String GROUPING_FUNCTIONS = "GROUPING_FUNCTIONS";

   public static final int    SUM                = 0;
   public static final int    MIN                = 1;
   public static final int    MAX                = 2;
   public static final int    AVG                = 3;
   public static final String FUNCTIONS[]        = { "SUM", "MIN", "MAX", "AVG" };

   private IVectorLayer       m_LayerIn;
   private int[]              m_iFields;
   private Grouping[]         m_Groupings;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_LayerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         m_LayerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      m_iFields = getFields(m_Parameters.getParameterValueAsString(GROUPING_FIELDS));
      final String sGroupings = m_Parameters.getParameterValueAsString(GROUPING_FUNCTIONS);
      m_Groupings = getGroupings(sGroupings);

      final String[] sFields = new String[m_iFields.length + m_Groupings.length];
      final Class[] types = new Class[m_iFields.length + m_Groupings.length];
      for (int i = 0; i < types.length; i++) {
         sFields[i] = m_LayerIn.getFieldName(m_iFields[i]);
         types[i] = m_LayerIn.getFieldType(m_iFields[i]);
      }
      for (int i = 0; i < m_Groupings.length; i++) {
         sFields[i + m_iFields.length] = m_LayerIn.getFieldName(m_Groupings[i].field) + "_" + FUNCTIONS[m_Groupings[i].function];
         types[i + m_iFields.length] = Double.class;
      }

      final IVectorLayer output = getNewVectorLayer(RESULT, m_LayerIn.getName(), m_LayerIn.getShapeType(), types, sFields);

      final int iShapesCount = m_LayerIn.getShapesCount();

      final HashMap<ArrayList, Geometry> geoms = new HashMap<ArrayList, Geometry>();
      final HashMap[] stats = new HashMap[m_LayerIn.getFieldCount()];
      for (int i = 0; i < stats.length; i++) {
         stats[i] = new HashMap();
      }
      final IFeatureIterator iter = m_LayerIn.iterator();
      int i = 0;
      while (iter.hasNext() && setProgress(i, iShapesCount)) {
         final IFeature feature = iter.next();
         //String sClass = "";
         final ArrayList clazz = new ArrayList();
         final IRecord record = feature.getRecord();
         //sClass = record.getValue(m_iFields[0]).toString();
         for (int j = 0; j < m_iFields.length; j++) {
            clazz.add(record.getValue(m_iFields[j]));
            //sClass = sClass + "@" + record.getValue(m_iFields[j]).toString();
         }

         final Geometry geom = geoms.get(clazz);
         if (geom == null) {
            geoms.put(clazz, feature.getGeometry());
            for (final HashMap element : stats) {
               element.put(clazz, new SimpleStats());
            }
         }
         else {
            final Geometry[] geomsToUnion = new Geometry[2];
            geomsToUnion[0] = geom;
            geomsToUnion[1] = feature.getGeometry();
            final GeometryFactory fact = geom.getFactory();
            final Geometry geomColl = fact.createGeometryCollection(geomsToUnion);
            geoms.put(clazz, geomColl);
         }
         for (int j = 0; j < stats.length; j++) {
            try {
               final double dValue = Double.parseDouble(record.getValue(j).toString());
               ((SimpleStats) stats[j].get(clazz)).addValue(dValue);
            }
            catch (final Exception e) {
            }
         }

         i++;
      }

      if (m_Task.isCanceled()) {
         return false;
      }

      final Set set = geoms.keySet();
      final Iterator keys = set.iterator();
      i = 0;
      while (keys.hasNext() && setProgress(i, geoms.size())) {
         final ArrayList clazz = (ArrayList) keys.next();
         final Geometry geom = geoms.get(clazz).union();
         final SimpleStats stat[] = new SimpleStats[m_LayerIn.getFieldCount()];
         for (int j = 0; j < stat.length; j++) {
            stat[j] = (SimpleStats) stats[j].get(clazz);
         }
         final Object[] values = calculateRecord(stat, clazz);
         output.addFeature(geom, values);
         i++;
      }

      return !m_Task.isCanceled();

   }


   private int[] getFields(final String sFields) throws GeoAlgorithmExecutionException {

      final String[] fields = sFields.split(",");

      if (fields.length == 0) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("At_Least_One_Field_Needed"));
      }
      final int[] iFields = new int[fields.length];
      for (int i = 0; i < fields.length; i++) {
         iFields[i] = m_LayerIn.getFieldIndexByName(fields[i]);
         if (iFields[i] == -1) {
            throw new GeoAlgorithmExecutionException(Sextante.getText("Wrong_Field_Name") + ":" + fields[i]);
         }
      }
      return iFields;

   }


   private Object[] calculateRecord(final SimpleStats[] stats,
                                    final ArrayList clazz) {

      double dValue;
      final Object[] values = new Object[m_Groupings.length + m_iFields.length];
      //final String[] sClasses = clazz.split("@");
      for (int i = 0; i < m_iFields.length; i++) {
         values[i] = clazz.get(i);//sClasses[i];
      }
      for (int i = 0; i < m_Groupings.length; i++) {
         final int iField = m_Groupings[i].field;
         final int iFunction = m_Groupings[i].function;
         switch (iFunction) {
            case MIN:
               dValue = stats[iField].getMin();
               break;
            case MAX:
               dValue = stats[iField].getMax();
               break;
            case SUM:
               dValue = stats[iField].getSum();
               break;
            case AVG:
            default:
               dValue = stats[iField].getMean();
               break;
         }
         if (stats[iField].getCount() > 0) {
            values[i + m_iFields.length] = new Double(dValue);
         }
         else {
            values[i + m_iFields.length] = null;
         }
      }

      return values;

   }


   private Grouping[] getGroupings(final String sGroupings) throws GeoAlgorithmExecutionException {

      if (sGroupings.trim().equals("")) {
         return new Grouping[0];
      }
      try {
         final String[] sTokens = sGroupings.split(",");
         if (sTokens.length % 2 != 0) {
            throw new GeoAlgorithmExecutionException("Wrong groupings");
         }
         final Grouping[] groupings = new Grouping[sTokens.length / 2];

         final int iGrouping = 0;
         for (int i = 0; i < sTokens.length; i++) {
            groupings[iGrouping] = new Grouping();
            groupings[iGrouping].field = Integer.parseInt(sTokens[i]);
            if ((groupings[iGrouping].field >= m_LayerIn.getFieldCount()) || (groupings[iGrouping].field < 0)) {
               throw new GeoAlgorithmExecutionException("Wrong groupings");
            }
            i++;
            groupings[iGrouping].function = Integer.parseInt(sTokens[i]);
            if ((groupings[iGrouping].function > 4) || (groupings[iGrouping].function < 0)) {
               throw new GeoAlgorithmExecutionException("Wrong groupings");
            }
         }
         return groupings;
      }
      catch (final Exception e) {
         throw new GeoAlgorithmExecutionException("Wrong groupings");
      }


   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Dissolve_multiple"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addString(GROUPING_FIELDS, Sextante.getText("Fields"));
         m_Parameters.addString(GROUPING_FUNCTIONS, Sextante.getText("Summary_statistics"));
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_UNDEFINED, LAYER);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }


   }

   private class Grouping {

      public int field;
      public int function;


      public Grouping() {
      }

   }

}
