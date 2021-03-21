package es.unex.sextante.morphometry.fillElevationValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class FillElevationValuesAlgorithm
         extends
            GeoAlgorithm {

   public static final String  CONTOURS_LAYER              = "CONTOURS_LAYER";
   public static final String  CONTOURS_LAYER_FIELD        = "CONTOURS_LAYER_FIELD";
   public static final String  RESULT                      = "RESULT";
   public static final String  DISTANCE                    = "DISTANCE";
   public static final String  UPHILL_LAYER                = "UPHILL_LAYER";
   public static final String  UPHILL_LAYER_FIELD          = "UPHILL_LAYER_FIELD";
   public static final String  UPHILL_LAYER_INTERVAL_FIELD = "UPHILL_LAYER_INTERVAL_FIELD";

   private IVectorLayer        m_Contours_Layer;
   private IVectorLayer        m_Uphill_Layer;
   private IVectorLayer        m_Output;
   private ArrayList<Geometry> m_Geometries;
   private ArrayList<Double>   m_Elevations;
   private double              m_dEquidistance;
   private STRtree             m_Tree;
   private BitSet              m_BitSet;
   private int                 m_iContoursField;
   private int                 m_iUphillField;
   private int                 m_iUphillIntervalField;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Fill_elevation_values"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputVectorLayer(CONTOURS_LAYER, Sextante.getText("Contour_lines"),
                  AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         m_Parameters.addInputVectorLayer(UPHILL_LAYER, Sextante.getText("Uphill_lines"),
                  AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         m_Parameters.addTableField(CONTOURS_LAYER_FIELD, Sextante.getText("Elevation_field"), CONTOURS_LAYER);
         m_Parameters.addTableField(UPHILL_LAYER_FIELD, Sextante.getText("Elevation_field"), UPHILL_LAYER);
         m_Parameters.addTableField(UPHILL_LAYER_INTERVAL_FIELD, Sextante.getText("Interval_field"), UPHILL_LAYER);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_LINE);
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


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;

      m_Contours_Layer = m_Parameters.getParameterValueAsVectorLayer(CONTOURS_LAYER);
      m_Uphill_Layer = m_Parameters.getParameterValueAsVectorLayer(UPHILL_LAYER);
      m_iContoursField = m_Parameters.getParameterValueAsInt(CONTOURS_LAYER_FIELD);
      m_iUphillField = m_Parameters.getParameterValueAsInt(UPHILL_LAYER_FIELD);
      m_iUphillIntervalField = m_Parameters.getParameterValueAsInt(UPHILL_LAYER_FIELD);

      if (!checkFields()) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("Wrong_field_types"));
      }

      m_Output = getNewVectorLayer(RESULT, m_Contours_Layer.getName(), IVectorLayer.SHAPE_TYPE_LINE,
               m_Contours_Layer.getFieldTypes(), m_Contours_Layer.getFieldNames());


      iShapeCount = m_Contours_Layer.getShapesCount();
      m_BitSet = new BitSet(iShapeCount);

      m_Tree = new STRtree();

      setProgressText(Sextante.getText("Reading_input_data"));
      i = 0;
      IFeatureIterator iter = m_Contours_Layer.iterator();
      while (iter.hasNext() && !m_Task.isCanceled()) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Envelope envelope = geom.getEnvelopeInternal();
         final FeatureAndIndex fai = new FeatureAndIndex(feature, i);
         m_Tree.insert(envelope, fai);
         if (i % 50 == 0) {
            m_Task.setProgress(i, iShapeCount);
         }
         i++;
      }
      iter.close();

      if (m_Task.isCanceled()) {
         return false;
      }

      iter = m_Uphill_Layer.iterator();
      setProgressText(Sextante.getText("computing_elevation_values"));
      i = 0;
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         computeElevations(feature);
      }
      iter.close();

      //		i = 0;
      //		iter = m_Contours_Layer.iterator();
      //		while(iter.hasNext() && !m_Task.isCanceled()){
      //			if (!m_BitSet.get(i)){
      //				m_Output.addFeature(iter.next());
      //			}
      //			i++;
      //		}
      //		iter.close();

      return !m_Task.isCanceled();

   }


   private boolean checkFields() {

      final Class contoursField = m_Contours_Layer.getFieldType(m_iContoursField);
      final Class uphillField = m_Uphill_Layer.getFieldType(m_iUphillField);
      final Class intervalField = m_Uphill_Layer.getFieldType(m_iUphillIntervalField);

      return Number.class.isAssignableFrom(contoursField) && Number.class.isAssignableFrom(uphillField)
             && Number.class.isAssignableFrom(intervalField);
   }


   private void computeElevations(final IFeature feature) {

      final Geometry geom = feature.getGeometry();
      final Coordinate initPt = geom.getCoordinates()[0];
      final Object elevation = feature.getRecord().getValue(m_iUphillField);
      final Object interval = feature.getRecord().getValue(m_iUphillIntervalField);
      try {
         final String sElevation = elevation.toString();
         double dElevation = Double.parseDouble(sElevation);
         final String sInterval = interval.toString();
         final double dInterval = Double.parseDouble(sInterval);
         final Envelope envelope = geom.getEnvelopeInternal();
         final List lines = m_Tree.query(envelope);
         final ArrayList<FeatureAndDistance> features = new ArrayList<FeatureAndDistance>();
         for (int i = 0; i < lines.size(); i++) {
            final FeatureAndIndex fai = (FeatureAndIndex) lines.get(i);
            final Geometry line = fai.feature.getGeometry();
            m_BitSet.set(fai.index);
            final Coordinate pt = line.intersection(geom).getCoordinate();
            if (pt != null) {
               final double dDist = initPt.distance(pt);
               final FeatureAndDistance fad = new FeatureAndDistance(fai.feature, dDist);
               features.add(fad);
            }
         }
         final FeatureAndDistance[] featuresArray = features.toArray(new FeatureAndDistance[0]);
         Arrays.sort(featuresArray);
         for (int i = 0; i < featuresArray.length; i++, dElevation += dInterval) {
            final Object[] record = featuresArray[i].getFeature().getRecord().getValues();
            record[m_iContoursField] = new Double(dElevation);
            m_Output.addFeature(featuresArray[i].getFeature().getGeometry(), record);
         }

      }
      catch (final Exception e) {
         e.printStackTrace();
         return;
      }


   }

   private class FeatureAndIndex {

      public IFeature feature;
      public int      index;


      public FeatureAndIndex(final IFeature feature,
                             final int index) {

         this.feature = feature;
         this.index = index;

      }
   }


}
