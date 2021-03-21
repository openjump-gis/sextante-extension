

package es.unex.sextante.vectorTools.distanceTableBuffer;

import java.util.ArrayList;
import java.util.Arrays;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.operation.buffer.BufferOp;

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
import es.unex.sextante.parameters.FixedTableModel;


public class DistanceTableBufferAlgorithm
         extends
            GeoAlgorithm {


   public static final String RESULT          = "RESULT";
   public static final String NOTROUNDED      = "NOTROUNDED";
   public static final String LAYER           = "LAYER";
   public static final String DISTANCES       = "DISTANCES";

   private IVectorLayer       m_Output;
   private boolean            m_bRounded;
   private int                m_iNumProcessed = 0;
   private Double[]           m_Distances;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final FixedTableModel distances = (FixedTableModel) m_Parameters.getParameterValueAsObject(DISTANCES);
      final ArrayList<Double> distancesList = new ArrayList<Double>();
      for (int i = 0; i < distances.getRowCount(); i++) {
         try {
            final double dDistance = Double.parseDouble(distances.getValueAt(i, 0).toString());
            if (dDistance > 0) {
               distancesList.add(new Double(dDistance));
            }
         }
         catch (final Exception e) {
            //ignore wrong values in table
         }
      }

      if (distancesList.size() == 0) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("No_Elements_In_Distance_List"));
      }

      m_Distances = distancesList.toArray(new Double[0]);
      Arrays.sort(m_Distances);

      m_bRounded = !m_Parameters.getParameterValueAsBoolean(NOTROUNDED);

      final int iFieldCount = layerIn.getFieldCount();
      Class types[];
      String[] sFieldNames;

      types = new Class[iFieldCount + 2];
      types[0] = Integer.class;
      types[1] = Double.class;
      sFieldNames = new String[iFieldCount + 2];
      sFieldNames[0] = "ID";
      sFieldNames[1] = "DIST";
      for (int i = 0; i < iFieldCount; i++) {
         sFieldNames[i + 2] = layerIn.getFieldName(i);
         types[i + 2] = layerIn.getFieldType(i);
      }

      m_Output = getNewVectorLayer(RESULT, "Buffer", IVectorLayer.SHAPE_TYPE_POLYGON, types, sFieldNames);

      int i = 0;
      final int iTotal = layerIn.getShapesCount();
      final IFeatureIterator iter = layerIn.iterator();
      while (iter.hasNext() && setProgress(i, iTotal)) {
         final IFeature feature = iter.next();
         computeBuffer(feature.getGeometry(), feature.getRecord().getValues());
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Multiple_buffer"));
      setGroup(Sextante.getText("Buffers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Input_layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addFixedTable(DISTANCES, Sextante.getText("Distances_table"),
                  new String[] { Sextante.getText("Distance") }, 3, false);
         m_Parameters.addBoolean(NOTROUNDED, Sextante.getText("Do_not_round_resulting_polygons"), false);

         addOutputVectorLayer(RESULT, Sextante.getText("Buffer"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   public void computeBuffer(final Geometry originalGeometry,
                             final Object[] record) {

      Geometry solution = null;
      final Geometry inputParam = originalGeometry;

      int cap = BufferOp.CAP_ROUND;
      if (!m_bRounded) {
         cap = BufferOp.CAP_SQUARE;
      }

      Geometry previousExteriorRing = null;
      for (int i = 0; i < m_Distances.length; i++) {
         final double dDistRing = m_Distances[i].doubleValue();
         final BufferOp bufOp = new BufferOp(inputParam);
         bufOp.setEndCapStyle(cap);
         final Geometry newGeometry = bufOp.getResultGeometry(dDistRing);
         if (previousExteriorRing != null) {
            solution = newGeometry.difference(previousExteriorRing);
         }
         else {
            solution = newGeometry;
         }
         m_iNumProcessed++;
         addFeature(solution, dDistRing, record);
         previousExteriorRing = newGeometry;
      }

   }


   protected void addFeature(final Geometry geom,
                             final double dDistance,
                             final Object[] record) {

      final Object[] values = new Object[2 + record.length];
      values[0] = new Long(m_iNumProcessed);
      values[1] = new Double(dDistance);
      for (int i = 0; i < record.length; i++) {
         values[i + 2] = record[i];
      }
      m_Output.addFeature(geom, values);

   }

}
