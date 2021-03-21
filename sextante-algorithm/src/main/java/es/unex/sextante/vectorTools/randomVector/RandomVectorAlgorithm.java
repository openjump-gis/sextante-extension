package es.unex.sextante.vectorTools.randomVector;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class RandomVectorAlgorithm
         extends
            GeoAlgorithm {

   private static final int   TYPE_POLYGONS = 0;
   private static final int   TYPE_LINES    = 1;
   private static final int   TYPE_POINTS   = 2;

   public static final String RESULT        = "RESULT";
   public static final String TYPE          = "TYPE";
   public static final String COUNT         = "COUNT";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Create_random_vector_layer"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      final String[] sOptions = { Sextante.getText("Polygons"), Sextante.getText("Lines"), Sextante.getText("Points") };

      try {
         m_Parameters.addNumericalValue(COUNT, Sextante.getText("Number_of_features_to_create"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 100, 1, Integer.MAX_VALUE);
         m_Parameters.addSelection(TYPE, Sextante.getText("Type"), sOptions);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int iShapeType;
      final String[] sNames = { "ID" };
      final Class[] types = { Integer.class };

      final int iCount = m_Parameters.getParameterValueAsInt(COUNT);
      final int iType = m_Parameters.getParameterValueAsInt(TYPE);

      switch (iType) {
         case TYPE_POLYGONS:
            iShapeType = IVectorLayer.SHAPE_TYPE_POLYGON;
            break;
         case TYPE_LINES:
            iShapeType = IVectorLayer.SHAPE_TYPE_LINE;
            break;
         case TYPE_POINTS:
         default:
            iShapeType = IVectorLayer.SHAPE_TYPE_POINT;
            break;
      }

      final IVectorLayer output = getNewVectorLayer(RESULT, Sextante.getText("Result"), iShapeType, types, sNames);
      final RandomGeometryUtilities rgu = new RandomGeometryUtilities();
      final Envelope env = m_AnalysisExtent.getAsJTSGeometry().getEnvelopeInternal();
      Geometry geom;
      for (int i = 0; (i < iCount) && setProgress(i, iCount); i++) {
         switch (iType) {
            case TYPE_POLYGONS:
               geom = rgu.nextNoHolePolygon(env);
               break;
            case TYPE_LINES:
               geom = rgu.nextLineString(env);
               break;
            case TYPE_POINTS:
            default:
               geom = rgu.nextPoint(env);
               break;
         }
         output.addFeature(geom, new Object[] { new Integer(i) });
      }
      return !m_Task.isCanceled();

   }


}
