package es.unex.sextante.vectorTools.clipByRectangle;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.index.strtree.STRtree;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.vectorTools.clip.ClipAlgorithm;

// Author Nacho Varela (Based on ClipAlgorithm)
//TODO Checkbox to choose if get whole geometries that touch the rectangle or clip them
//TODO Enable select a full extent of other layer or select by clicking
public class ClipByRectangleAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String YMAX   = "YMAX";
   public static final String XMAX   = "XMAX";
   public static final String YMIN   = "YMIN";
   public static final String XMIN   = "XMIN";
   public static final String RESULT = "RESULT";

   private IVectorLayer       m_Output;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Clip_by_rectangle"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer_to_clip"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY,
                  true);
         /*m_Parameters.addNumericalValue(XMIN, "X Min", -100, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(XMAX, "X Max", 100, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(YMIN, "Y Min", -100, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(YMAX, "Y Max", 100, AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);*/

         addOutputVectorLayer(RESULT, Sextante.getText("Clipped_Layer"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);

      /*final double dXMin = m_Parameters.getParameterValueAsDouble(XMIN);
      final double dXMax = m_Parameters.getParameterValueAsDouble(XMAX);
      final double dYMin = m_Parameters.getParameterValueAsDouble(YMIN);
      final double dYMax = m_Parameters.getParameterValueAsDouble(YMAX);*/

      final STRtree tree = buildClipTree();//dXMin, dXMax, dYMin, dYMax);

      m_Output = getNewVectorLayer(RESULT, Sextante.getText("Clipped_Layer"), layerIn.getShapeType(), layerIn.getFieldTypes(),
               layerIn.getFieldNames());

      final IFeatureIterator iter = layerIn.iterator();

      int i = 0;
      final int iShapeCount = layerIn.getShapesCount();
      final ClipAlgorithm alg = new ClipAlgorithm();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry g = alg.clipGeometry(feature.getGeometry(), tree);
         if (g != null) {
            m_Output.addFeature(g, feature.getRecord().getValues());
         }
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   private STRtree buildClipTree() {

      final STRtree tree = new STRtree();

      final Geometry geom = m_AnalysisExtent.getAsJTSGeometry();

      final PreparedGeometry pg = PreparedGeometryFactory.prepare(geom);
      tree.insert(pg.getGeometry().getEnvelopeInternal(), pg);

      return tree;

   }

}
