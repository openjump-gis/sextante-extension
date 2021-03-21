

package es.unex.sextante.vectorTools.smoothLines;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

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


public class SmoothLinesAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT                = "RESULT";
   public static final String INTERMEDIATE_POINTS   = "INTERMEDIATE_POINTS";
   public static final String LAYER                 = "LAYER";
   public static final String CURVE_TYPE            = "CURVE_TYPE";

   public static final int    NATURAL_CUBIC_SPLINES = 0;
   public static final int    BEZIER_CURVES         = 1;
   public static final int    BSPLINES              = 2;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      int iSteps = m_Parameters.getParameterValueAsInt(INTERMEDIATE_POINTS) + 1;
      final int iMethod = m_Parameters.getParameterValueAsInt(CURVE_TYPE);

      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      iSteps = Math.max(1, Math.min(iSteps, 10));

      final IVectorLayer output = getNewVectorLayer(RESULT, layerIn.getName() + "[" + Sextante.getText("smoothed") + "]",
               IVectorLayer.SHAPE_TYPE_LINE, layerIn.getFieldTypes(), layerIn.getFieldNames());
      final IFeatureIterator iter = layerIn.iterator();
      int i = 0;
      final int iCount = layerIn.getShapesCount();
      while (iter.hasNext() && setProgress(i, iCount)) {
         final IFeature feature = iter.next();
         final Geometry simpleGeom = getSmoothedLine(feature.getGeometry(), iSteps, iMethod);
         output.addFeature(simpleGeom, feature.getRecord().getValues());
         i++;
      }


      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Smooth_lines"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Lines"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         m_Parameters.addSelection(INTERMEDIATE_POINTS, Sextante.getText("Intermediate_points"), new String[] { "1", "2", "3",
                  "4", "5", "6", "7", "8", "9" });
         m_Parameters.addSelection(CURVE_TYPE, Sextante.getText("Curve_type"), new String[] { "Natural cubic splines",
                  "Bezier curves", "B splines" });
         addOutputVectorLayer(RESULT, Sextante.getText("Smoothed_lines"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private Geometry getSmoothedLine(final Geometry geometry,
                                    final int iSteps,
                                    final int iMethod) {

      final ArrayList<LineString> geoms = new ArrayList<LineString>();

      ControlCurve curve = null;

      for (int i = 0; i < geometry.getNumGeometries(); i++) {
         switch (iMethod) {
            case NATURAL_CUBIC_SPLINES:
               curve = new NatCubic(geometry.getGeometryN(i));
               break;
            case BEZIER_CURVES:
               curve = new Bezier(geometry.getGeometryN(i));
               break;
            case BSPLINES:
            default:
               curve = new BSpline(geometry.getGeometryN(i));
               break;
         }
         geoms.add(curve.getSmoothedLine(iSteps));
      }

      final GeometryFactory gf = geometry.getFactory();
      return gf.createMultiLineString(geoms.toArray(new LineString[0]));

   }

}
