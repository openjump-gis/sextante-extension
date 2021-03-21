

package es.unex.sextante.vectorTools.dissolve;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.precision.EnhancedPrecisionOp;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.math.simpleStats.SimpleStats;
import es.unex.sextante.outputs.OutputVectorLayer;


public class DissolveAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT             = "RESULT";
   public static final String LAYER              = "LAYER";
   public static final String GROUPING_FIELD     = "GROUPING_FIELD";
   public static final String GROUPING_FUNCTIONS = "GROUPING_FUNCTIONS";

   public static final int    SUM                = 0;
   public static final int    MIN                = 1;
   public static final int    MAX                = 2;
   public static final int    AVG                = 3;
   public static final String FUNCTIONS[]        = { "SUM", "MIN", "MAX", "AVG" };

   private IVectorLayer       m_LayerIn;
   private int                m_iField;
   private Grouping[]         m_Groupings;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_LayerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         m_LayerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      m_iField = m_Parameters.getParameterValueAsInt(GROUPING_FIELD);
      final String sGroupings = m_Parameters.getParameterValueAsString(GROUPING_FUNCTIONS);
      m_Groupings = getGroupings(sGroupings);

      final String[] sFields = new String[1 + m_Groupings.length];
      final Class[] types = new Class[1 + m_Groupings.length];
      sFields[0] = Sextante.getText("Class");
      types[0] = String.class;
      for (int i = 0; i < m_Groupings.length; i++) {
         sFields[i + 1] = m_LayerIn.getFieldName(m_Groupings[i].field) + "_" + FUNCTIONS[m_Groupings[i].function];
         types[i + 1] = Double.class;
      }

      final IVectorLayer output = getNewVectorLayer(RESULT, m_LayerIn.getName(), m_LayerIn.getShapeType(), types, sFields);

      final int iShapesCount = m_LayerIn.getShapesCount();

      final HashMap<String, Geometry> geoms = new HashMap<String, Geometry>();
      final HashMap[] stats = new HashMap[m_LayerIn.getFieldCount()];
      for (int i = 0; i < stats.length; i++) {
         stats[i] = new HashMap();
      }
      final IFeatureIterator iter = m_LayerIn.iterator();
      int i = 0;
      while (iter.hasNext() && setProgress(i, iShapesCount)) {
         final IFeature feature = iter.next();
         final String sClass = feature.getRecord().getValue(m_iField).toString();
         Geometry geom = geoms.get(sClass);
         if (geom == null) {
            geoms.put(sClass, feature.getGeometry());
            for (final HashMap element : stats) {
               element.put(sClass, new SimpleStats());
            }
         }
         else {
            try {
               geom = EnhancedPrecisionOp.union(geom, feature.getGeometry());
            }
            catch (final Exception e) {
               //try this if JTS complains
               geom = EnhancedPrecisionOp.union(geom.buffer(0), feature.getGeometry());
            }
            geoms.put(sClass, geom);
         }
         final IRecord record = feature.getRecord();
         for (int j = 0; j < stats.length; j++) {
            try {
               final double dValue = Double.parseDouble(record.getValue(j).toString());
               ((SimpleStats) stats[j].get(sClass)).addValue(dValue);
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
         final String sClass = (String) keys.next();
         final SimpleStats stat[] = new SimpleStats[m_LayerIn.getFieldCount()];
         for (int j = 0; j < stat.length; j++) {
            stat[j] = (SimpleStats) stats[j].get(sClass);
         }
         final Object[] values = calculateRecord(stat, sClass);
         output.addFeature(geoms.get(sClass), values);
         i++;
      }

      return !m_Task.isCanceled();

   }


   private Object[] calculateRecord(final SimpleStats[] stats,
                                    final String sClass) {

      double dValue;
      final Object[] values = new Object[m_Groupings.length + 1];
      values[0] = sClass;
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
            values[i + 1] = new Double(dValue);
         }
         else {
            values[i + 1] = null;
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

         int iGrouping = 0;
         for (int i = 0; i < sTokens.length - 1; i++) {
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
            iGrouping++;
         }
         return groupings;
      }
      catch (final Exception e) {
         throw new GeoAlgorithmExecutionException("Wrong groupings");
      }


   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Dissolve"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(GROUPING_FIELD, Sextante.getText("Field_with_class_name"), LAYER);
         m_Parameters.addString(GROUPING_FUNCTIONS, Sextante.getText("Summary_statistics"));
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_UNDEFINED, LAYER);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final UndefinedParentParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final OptionalParentParameterException e) {
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
