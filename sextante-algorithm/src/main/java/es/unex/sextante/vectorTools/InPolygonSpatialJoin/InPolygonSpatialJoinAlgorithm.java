package es.unex.sextante.vectorTools.InPolygonSpatialJoin;

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
import es.unex.sextante.math.simpleStats.SimpleStats;
import es.unex.sextante.outputs.OutputVectorLayer;


public class InPolygonSpatialJoinAlgorithm
         extends
            GeoAlgorithm {

   public static final String     RESULT             = "RESULT";
   public static final String     POLYGONS           = "POLYGONS";
   public static final String     POINTS             = "POINTS";
   public static final String     GROUPING_FUNCTIONS = "GROUPING_FUNCTIONS";

   public static final int        SUM                = 0;
   public static final int        MIN                = 1;
   public static final int        MAX                = 2;
   public static final int        AVG                = 3;
   public static final int        COUNT              = 4;
   public static final String     FUNCTIONS[]        = { "SUM", "MIN", "MAX", "AVG", "COUNT" };


   private IVectorLayer           m_MainLayer;
   private IVectorLayer           m_SecondaryLayer;

   private IVectorLayer           m_Output;

   private NearestNeighbourFinder m_NNF;
   private Grouping[]             m_Groupings;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("InPolygonSpatialJoin"));
      setGroup(Sextante.getText("Tools_for_polygon_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(POLYGONS, Sextante.getText("Polygons_layer"),
                  AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Secondary_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY,
                  true);
         m_Parameters.addString(GROUPING_FUNCTIONS, Sextante.getText("Summary_statistics"));
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POLYGON, POLYGONS);

      }
      catch (final RepeatedParameterNameException e) {

      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;

      m_MainLayer = m_Parameters.getParameterValueAsVectorLayer(POLYGONS);
      m_SecondaryLayer = m_Parameters.getParameterValueAsVectorLayer(POINTS);

      if (!m_bIsAutoExtent) {
         m_MainLayer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         m_SecondaryLayer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final String sGroupings = m_Parameters.getParameterValueAsString(GROUPING_FUNCTIONS);
      m_Groupings = getGroupings(sGroupings);

      final String[] sFields = new String[m_MainLayer.getFieldCount() + m_Groupings.length];
      final Class[] types = new Class[m_MainLayer.getFieldCount() + m_Groupings.length];
      for (i = 0; i < m_MainLayer.getFieldCount(); i++) {
         sFields[i] = m_MainLayer.getFieldName(i);
         types[i] = m_MainLayer.getFieldType(i);
      }
      for (i = 0; i < m_Groupings.length; i++) {
         sFields[m_MainLayer.getFieldCount() + i] = m_SecondaryLayer.getFieldName(m_Groupings[i].field) + "_"
                                                    + FUNCTIONS[m_Groupings[i].function];
         types[m_MainLayer.getFieldCount() + i] = Double.class;
      }

      m_Output = getNewVectorLayer(RESULT, m_MainLayer.getName(), m_MainLayer.getShapeType(), types, sFields);

      m_NNF = new NearestNeighbourFinder(m_SecondaryLayer, this.m_Task);

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

      final Geometry polygon = feature.getGeometry();
      final IFeature[] pts = m_NNF.getClosestPoints(polygon);

      final SimpleStats[] stats = new SimpleStats[m_SecondaryLayer.getFieldCount()];
      for (int i = 0; i < stats.length; i++) {
         stats[i] = new SimpleStats();
      }

      for (final IFeature element : pts) {
         if (polygon.contains(element.getGeometry())) {
            for (int j = 0; j < stats.length; j++) {
               final String sValue = element.getRecord().getValue(j).toString();
               try {
                  final double dValue = Double.parseDouble(sValue);
                  stats[j].addValue(dValue);
               }
               catch (final Exception e) {
                  e.printStackTrace();
               }
            }
         }
      }

      m_Output.addFeature(polygon, calculateRecord(stats, feature.getRecord().getValues()));

   }


   private Object[] calculateRecord(final SimpleStats[] stats,
                                    final Object[] record) {

      double dValue;
      final Object[] values = new Object[m_Groupings.length + record.length];
      System.arraycopy(record, 0, values, 0, record.length);
      for (int i = 0; i < m_Groupings.length; i++) {
         final int iField = m_Groupings[i].field;
         final int iFunction = m_Groupings[i].function;
         switch (iFunction) {
            case COUNT:
               dValue = stats[iField].getCount();
               break;
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
            values[i + record.length] = new Double(dValue);
         }
         else {
            values[i + record.length] = null;
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
         for (int i = 0; i < sTokens.length; i++) {
            String sToken = sTokens[i];
            groupings[iGrouping] = new Grouping();
            groupings[iGrouping].field = Integer.parseInt(sToken);
            if ((groupings[iGrouping].field >= m_SecondaryLayer.getFieldCount()) || (groupings[iGrouping].field < 0)) {
               throw new GeoAlgorithmExecutionException("Wrong groupings");
            }
            i++;
            sToken = sTokens[i];
            groupings[iGrouping].function = Integer.parseInt(sToken);
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


   private class Grouping {

      public int field;
      public int function;


      public Grouping() {}

   }


}
