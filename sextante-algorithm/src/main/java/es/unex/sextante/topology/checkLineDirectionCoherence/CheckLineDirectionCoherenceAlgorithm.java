

package es.unex.sextante.topology.checkLineDirectionCoherence;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

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


public class CheckLineDirectionCoherenceAlgorithm
         extends
            GeoAlgorithm {

   public static final String  RESULT    = "RESULT";
   public static final String  LINES     = "LINES";
   public static final String  FIELD     = "FIELD";
   private static final String TOLERANCE = "TOLERANCE";

   private IVectorLayer        m_Lines;
   private IVectorLayer        m_Output;

   private double              m_dTolerance;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("CheckLineDirectionCoherence"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LINES, Sextante.getText("Lines_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         m_Parameters.addNumericalValue(TOLERANCE, Sextante.getText("Tolerance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, 0, Double.MAX_VALUE);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final RepeatedParameterNameException e) {

      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i = 0;
      int iShapesCount;
      final HashMap map = new HashMap();
      final HashMap<String, ArrayList<IFeature>> classes = new HashMap<String, ArrayList<IFeature>>();

      m_Lines = m_Parameters.getParameterValueAsVectorLayer(LINES);
      if (!m_bIsAutoExtent) {
         m_Lines.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final int iField = m_Parameters.getParameterValueAsInt(FIELD);
      m_dTolerance = m_Parameters.getParameterValueAsDouble(TOLERANCE);

      m_Output = getNewVectorLayer(RESULT, m_Lines.getName(), m_Lines.getShapeType(), m_Lines.getFieldTypes(),
               m_Lines.getFieldNames());

      //this is a naive approach. Might not work with very large layers...
      iShapesCount = m_Lines.getShapesCount();
      final IFeatureIterator iter = m_Lines.iterator();
      while (iter.hasNext() && setProgress(i, iShapesCount)) {
         final IFeature feature = iter.next();
         final String sClass = feature.getRecord().getValue(iField).toString();
         ArrayList<IFeature> features = classes.get(sClass);
         if (features == null) {
            features = new ArrayList<IFeature>();
            classes.put(sClass, features);
         }
         features.add(feature);
         i++;
      }
      iter.close();

      final Set set = classes.keySet();
      final Iterator keys = set.iterator();
      i = 0;
      while (keys.hasNext() && setProgress(i, classes.size())) {
         final Object sClass = keys.next();
         final ArrayList<IFeature> lines = classes.get(sClass);
         processLines(lines);
      }

      return !m_Task.isCanceled();

   }


   private void processLines(final ArrayList<IFeature> lines) {

      final BitSet bitset = new BitSet(lines.size());
      while (bitset.cardinality() != bitset.length()) {
         final IFeature line = getExtremeLine(lines, bitset);
         m_Output.addFeature(line);
         followLine(line, lines, bitset);
      }


   }


   private void followLine(final IFeature feature,
                           final ArrayList<IFeature> lines,
                           final BitSet bitset) {


      final Geometry line = feature.getGeometry();
      for (int i = 0; i < lines.size(); i++) {
         if (!bitset.get(i)) {
            final Coordinate[] coords = line.getCoordinates();
            final Geometry line2 = lines.get(i).getGeometry();
            for (int j = 0; j < lines.size(); j++) {
               final Coordinate[] coords2 = line2.getCoordinates();
               if (coords2[0].distance(coords[coords.length - 1]) < m_dTolerance) {
                  m_Output.addFeature(feature);
                  bitset.set(i);
                  followLine(lines.get(i), lines, bitset);
               }
            }
         }
      }
      for (int i = 0; i < lines.size(); i++) {
         if (!bitset.get(i)) {
            final Coordinate[] coords = line.getCoordinates();
            final Geometry line2 = lines.get(i).getGeometry();
            for (int j = 0; j < lines.size(); j++) {
               final Coordinate[] coords2 = line2.getCoordinates();
               if (coords2[coords2.length - 1].distance(coords[coords.length - 1]) < m_dTolerance) {
                  final Geometry inverted = invertDirection(feature.getGeometry());
                  m_Output.addFeature(inverted, feature.getRecord().getValues());
                  bitset.set(i);
                  followLine(lines.get(i), lines, bitset);
               }
            }
         }
      }


   }


   private Geometry invertDirection(final Geometry geometry) {

      final Coordinate[] coords = geometry.getCoordinates();
      final Coordinate[] newCoords = new Coordinate[coords.length];
      for (int i = 0; i < coords.length; i++) {
         newCoords[i] = coords[coords.length - i - 1];
      }
      final GeometryFactory gf = new GeometryFactory();
      return gf.createLineString(newCoords);

   }


   private IFeature getExtremeLine(final ArrayList<IFeature> lines,
                                   final BitSet bitset) {

      for (int i = 0; i < lines.size(); i++) {
         if (!bitset.get(i)) {
            final Geometry line = lines.get(i).getGeometry();
            final Coordinate[] coords = line.getCoordinates();
            boolean bHasContiguousLineOnStart = false;
            for (int j = 0; j < lines.size(); j++) {
               final Coordinate[] coords2 = line.getCoordinates();
               if ((coords2[0].distance(coords[0]) < m_dTolerance)
                   || (coords2[coords2.length - 1].distance(coords[0]) < m_dTolerance)) {
                  bHasContiguousLineOnStart = true;
                  break;
               }
            }
            boolean bHasContiguousLineOnEnd = false;
            for (int j = 0; j < lines.size(); j++) {
               final Coordinate[] coords2 = line.getCoordinates();
               if ((coords2[0].distance(coords[coords.length - 1]) < m_dTolerance)
                   || (coords2[coords2.length - 1].distance(coords[coords.length - 1]) < m_dTolerance)) {
                  bHasContiguousLineOnEnd = true;
                  break;
               }
            }
            if (bHasContiguousLineOnEnd != bHasContiguousLineOnStart) {
               bitset.set(i);
               return lines.get(i);
            }
         }
      }

      for (int i = 0; i < lines.size(); i++) {
         if (!bitset.get(i)) {
            bitset.set(i);
            return lines.get(i);
         }
      }

      return null;

   }

}
