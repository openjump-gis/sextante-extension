

package es.unex.sextante.vectorTools.transform;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.util.AffineTransformation;

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


public class TransformAlgorithm
         extends
            GeoAlgorithm {

   public static final String DISTANCEX = "DISTANCEX";
   public static final String DISTANCEY = "DISTANCEY";
   public static final String ANCHORX   = "ANCHORX";
   public static final String ANCHORY   = "ANCHORY";
   public static final String ANGLE     = "ANGLE";
   public static final String SCALEX    = "SCALEX";
   public static final String LAYER     = "LAYER";
   public static final String SCALEY    = "SCALEY";
   public static final String RESULT    = "RESULT";

   private IVectorLayer       m_Output;
   private double             dDistanceX;
   private double             dDistanceY;
   private double             dAnchorX;
   private double             dAnchorY;
   private double             dAngle;
   private double             dScaleX;
   private double             dScaleY;
   IVectorLayer               layerIn;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;

      dDistanceX = m_Parameters.getParameterValueAsDouble(DISTANCEX);
      dDistanceY = m_Parameters.getParameterValueAsDouble(DISTANCEY);
      dAnchorX = m_Parameters.getParameterValueAsDouble(ANCHORX);
      dAnchorY = m_Parameters.getParameterValueAsDouble(ANCHORY);
      dAngle = m_Parameters.getParameterValueAsDouble(ANGLE);
      dScaleX = m_Parameters.getParameterValueAsDouble(SCALEX);
      dScaleY = m_Parameters.getParameterValueAsDouble(SCALEY);
      layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);

      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      m_Output = getNewVectorLayer(RESULT, Sextante.getText("Capa_transformada"), layerIn.getShapeType(),
               layerIn.getFieldTypes(), layerIn.getFieldNames());

      final AffineTransformation at = new AffineTransformation();
      at.compose(AffineTransformation.translationInstance(-dAnchorX, -dAnchorY));
      at.compose(AffineTransformation.rotationInstance(Math.toRadians(dAngle)));
      at.compose(AffineTransformation.translationInstance(dAnchorX, dAnchorY));
      at.compose(AffineTransformation.scaleInstance(dScaleX, dScaleY));
      at.compose(AffineTransformation.translationInstance(dDistanceX, dDistanceY));
      final IFeatureIterator iter = layerIn.iterator();
      i = 0;
      final int iShapeCount = layerIn.getShapesCount();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = (Geometry) feature.getGeometry().clone();
         geom.apply(at);
         m_Output.addFeature(geom, feature.getRecord().getValues());
      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Transform"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Input_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addNumericalValue(DISTANCEX, Sextante.getText("Translation_X"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 0, Double.NEGATIVE_INFINITY, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(DISTANCEY, Sextante.getText("Translation_Y"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 0, Double.NEGATIVE_INFINITY, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(ANGLE, Sextante.getText("Rotation_angle"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 0, Double.NEGATIVE_INFINITY, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(SCALEX, Sextante.getText("Scale_factor__X"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, Double.NEGATIVE_INFINITY, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(SCALEY, Sextante.getText("Scale_Factor__Y"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, Double.NEGATIVE_INFINITY, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(ANCHORX, Sextante.getText("Anchor_point_X"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 0, Double.NEGATIVE_INFINITY, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(ANCHORY, Sextante.getText("Anchor_point_Y"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 0, Double.NEGATIVE_INFINITY, Double.MAX_VALUE);
         addOutputVectorLayer(RESULT, Sextante.getText("Capa_transformada"), OutputVectorLayer.SHAPE_TYPE_UNDEFINED, LAYER);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
   }

}
