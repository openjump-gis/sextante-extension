

package es.unex.sextante.vectorTools.variableDistanceBuffer;

import java.util.ArrayList;
import java.util.Stack;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;


public class VariableDistanceBufferAlgorithm
         extends
            GeoAlgorithm {

   public static final byte   BUFFER_INSIDE_POLY         = 1;
   public static final byte   BUFFER_OUTSIDE_POLY        = 0;
   public static final byte   BUFFER_INSIDE_OUTSIDE_POLY = 2;

   public static final String RESULT                     = "RESULT";
   public static final String NOTROUNDED                 = "NOTROUNDED";
   public static final String RINGS                      = "RINGS";
   public static final String TYPE                       = "TYPES";
   public static final String FIELD                      = "FIELD";
   public static final String LAYER                      = "LAYER";

   private IVectorLayer       m_Output;
   private boolean            m_bRounded;
   private int                m_iRings;
   private int                m_iType;
   private int                numProcessed               = 0;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final int iField = m_Parameters.getParameterValueAsInt(FIELD);
      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      m_bRounded = !m_Parameters.getParameterValueAsBoolean(NOTROUNDED);
      m_iRings = m_Parameters.getParameterValueAsInt(RINGS) + 1;
      m_iType = m_Parameters.getParameterValueAsInt(TYPE);

      final int iFieldCount = layerIn.getFieldCount();
      Class types[];
      String[] sFieldNames;

      if (layerIn.getShapeType() != IVectorLayer.SHAPE_TYPE_POLYGON) {
         m_iType = BUFFER_OUTSIDE_POLY;
      }

      if (m_iType == BUFFER_INSIDE_OUTSIDE_POLY) {
         types = new Class[iFieldCount + 3];
         types[0] = Long.class;
         types[1] = Double.class;
         sFieldNames = new String[iFieldCount + 3];
         sFieldNames[0] = "ID";
         sFieldNames[1] = "FROM";
         sFieldNames[1] = "TO";
         for (int i = 0; i < iFieldCount; i++) {
            sFieldNames[i + 3] = layerIn.getFieldName(i);
            types[i + 3] = layerIn.getFieldType(i);
         }
      }
      else {
         types = new Class[iFieldCount + 2];
         types[0] = Long.class;
         types[1] = Double.class;
         sFieldNames = new String[iFieldCount + 2];
         sFieldNames[0] = "ID";
         sFieldNames[1] = "DIST";
         for (int i = 0; i < iFieldCount; i++) {
            sFieldNames[i + 2] = layerIn.getFieldName(i);
            types[i + 2] = layerIn.getFieldType(i);
         }
      }

      m_Output = getNewVectorLayer("RESULT", "Buffer", IVectorLayer.SHAPE_TYPE_POLYGON, types, sFieldNames);

      int i = 0;
      final int iTotal = layerIn.getShapesCount();
      double dDistance;
      final IFeatureIterator iter = layerIn.iterator();
      while (iter.hasNext() && setProgress(i, iTotal)) {
         final IFeature feature = iter.next();
         try {

            final Number num = (Number) feature.getRecord().getValue(iField);
            dDistance = num.doubleValue();
         }
         catch (final Exception e) {
            continue;
         }
         computeBuffer(feature.getGeometry(), dDistance, feature.getRecord().getValues());
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      final String[] sRings = { Integer.toString(1), Integer.toString(2), Integer.toString(3) };
      final String[] sType = { Sextante.getText("Outer_buffer"), Sextante.getText("Inner_buffer"), Sextante.getText("Both"), };

      setName(Sextante.getText("Variable_distance_buffer"));
      setGroup(Sextante.getText("Buffers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Input_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Distance_field"), "LAYER");
         m_Parameters.addSelection(TYPE, Sextante.getText("Buffer_type"), sType);
         m_Parameters.addSelection(RINGS, Sextante.getText("Number_of_concentric_rings"), sRings);
         m_Parameters.addBoolean(NOTROUNDED, Sextante.getText("Do_not_round_resulting_polygons"), false);

         addOutputVectorLayer(RESULT, Sextante.getText("Buffer"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
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


   //*********** code adapted from BufferVisitor class, by Alvaro Zabala*******************//

   public void computeBuffer(final Geometry originalGeometry,
                             final double bufferDistance,
                             final Object[] record) {
      Geometry solution = null;
      Geometry inputParam = originalGeometry;
      /*
       * When we try to apply large buffer distances, we could get OutOfMemoryError
       * exceptions. Explanation in
       * http://lists.jump-project.org/pipermail/jts-devel/2005-February/000991.html
       * http://lists.jump-project.org/pipermail/jts-devel/2005-September/001292.html
       * This problems hasnt been resolved in JTS 1.7.
       */
      if (originalGeometry.getDimension() != 0) {
         inputParam = TopologyPreservingSimplifier.simplify(originalGeometry, bufferDistance / 10d);
      }
      int cap = BufferOp.CAP_ROUND;
      if (!m_bRounded) {
         cap = BufferOp.CAP_SQUARE;
      }

      //this two references are necessary to compute radial rings
      Geometry previousExteriorRing = null;
      Geometry previousInteriorRing = null;
      if (m_iType == BUFFER_INSIDE_POLY) {
         //if we have radial internal buffers, we start by
         //most interior buffer
         for (int i = m_iRings; i >= 1; i--) {
            final double distRing = i * bufferDistance;
            final BufferOp bufOp = new BufferOp(inputParam);
            bufOp.setEndCapStyle(cap);
            final Geometry newGeometry = bufOp.getResultGeometry(-1 * distRing);
            if (verifyNilGeometry(newGeometry)) {
               //we have collapsed original geometry
               return;
            }
            if (previousInteriorRing != null) {
               solution = newGeometry.difference(previousInteriorRing);
            }
            else {
               solution = newGeometry;
            }
            numProcessed++;
            addFeature(solution, distRing, record);
            previousInteriorRing = newGeometry;
         }
      }
      else if (m_iType == BUFFER_OUTSIDE_POLY) {
         for (int i = 1; i <= m_iRings; i++) {
            final double distRing = i * bufferDistance;
            final BufferOp bufOp = new BufferOp(inputParam);
            bufOp.setEndCapStyle(cap);
            final Geometry newGeometry = bufOp.getResultGeometry(distRing);
            if (previousExteriorRing != null) {
               solution = newGeometry.difference(previousExteriorRing);
            }
            else {
               solution = newGeometry;
            }
            numProcessed++;
            addFeature(solution, distRing, record);
            previousExteriorRing = newGeometry;
         }
      }
      else if (m_iType == BUFFER_INSIDE_OUTSIDE_POLY) {
         final GeometryFactory geomFact = new GeometryFactory();
         for (int i = 1; i <= m_iRings; i++) {
            final double distRing = i * bufferDistance;
            final BufferOp bufOp = new BufferOp(inputParam);
            bufOp.setEndCapStyle(cap);
            final Geometry out = bufOp.getResultGeometry(distRing);
            final Geometry in = bufOp.getResultGeometry(-1 * distRing);
            boolean collapsedInterior = verifyNilGeometry(in);
            if ((previousExteriorRing == null) || (previousInteriorRing == null)) {
               if (collapsedInterior) {
                  solution = out;
               }
               else {
                  solution = out.difference(in);
               }
            }
            else {
               if (collapsedInterior) {
                  solution = out.difference(previousExteriorRing);
               }
               else {
                  final Geometry outRing = out.difference(previousExteriorRing);
                  final Geometry inRing = previousInteriorRing.difference(in);
                  final Geometry[] geomArray = new Geometry[] { outRing, inRing };
                  solution = geomFact.createGeometryCollection(geomArray);
                  final ArrayList polygons = new ArrayList();
                  final Stack stack = new Stack();
                  stack.push(solution);
                  while (stack.size() != 0) {
                     final GeometryCollection geCol = (GeometryCollection) stack.pop();
                     for (int j = 0; j < geCol.getNumGeometries(); j++) {
                        final Geometry geometry = geCol.getGeometryN(j);
                        if (geometry instanceof GeometryCollection) {
                           stack.push(geometry);
                        }
                        if (geometry instanceof Polygon) {
                           polygons.add(geometry);
                        }
                     }
                  }
                  final Polygon[] pols = new Polygon[polygons.size()];
                  polygons.toArray(pols);
                  final MultiPolygon newSolution = geomFact.createMultiPolygon(pols);
                  solution = newSolution;
               }
            }
            numProcessed++;
            addFeature(solution, -1 * distRing, distRing, record);
            previousExteriorRing = out;
            if (!collapsedInterior) {
               previousInteriorRing = in;
            }
         }
      }

   }


   protected void addFeature(final Geometry geom,
                             final double distance,
                             final Object[] record) {

      final Object[] values = new Object[2 + record.length];
      values[0] = new Long(numProcessed);
      values[1] = new Double(distance);
      for (int i = 0; i < record.length; i++) {
         values[i + 2] = record[i];
      }
      m_Output.addFeature(geom, values);

   }


   protected void addFeature(final Geometry geom,
                             final double distanceFrom,
                             final double distanceTo,
                             final Object[] record) {

      final Object[] values = new Object[3 + record.length];
      values[0] = new Long(numProcessed);
      values[1] = new Double(distanceFrom);
      values[2] = new Double(distanceTo);
      for (int i = 0; i < record.length; i++) {
         values[i + 3] = record[i];
      }
      m_Output.addFeature(geom, values);

   }


   public boolean verifyNilGeometry(final Geometry newGeometry) {

      if (newGeometry instanceof GeometryCollection) {
         if (((GeometryCollection) newGeometry).getNumGeometries() == 0) {
            //we have collapsed initial geometry
            return true;
         }
      }
      return false;
   }


}
