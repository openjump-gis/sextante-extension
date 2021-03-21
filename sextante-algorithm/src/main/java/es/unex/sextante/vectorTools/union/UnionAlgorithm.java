

package es.unex.sextante.vectorTools.union;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;


public class UnionAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER1 = "LAYER1";
   public static final String LAYER2 = "LAYER2";
   public static final String RESULT = "RESULT";

   private Geometry           m_ClipGeometry;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer layer1 = m_Parameters.getParameterValueAsVectorLayer(LAYER1);
      final IVectorLayer layer2 = m_Parameters.getParameterValueAsVectorLayer(LAYER2);

      if (!m_bIsAutoExtent) {
         layer1.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         layer2.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final Class[] fieldTypes = new Class[layer1.getFieldCount() + layer2.getFieldCount()];
      final String[] sFieldNames = new String[fieldTypes.length];

      for (int i = 0; i < layer1.getFieldCount(); i++) {
         fieldTypes[i] = layer1.getFieldType(i);
         sFieldNames[i] = layer1.getFieldName(i);
      }
      for (int i = 0; i < layer2.getFieldCount(); i++) {
         fieldTypes[i + layer1.getFieldCount()] = layer2.getFieldType(i);
         sFieldNames[i + layer1.getFieldCount()] = layer2.getFieldName(i);
      }

      final IVectorLayer intersection = getTempVectorLayer(IVectorLayer.SHAPE_TYPE_POLYGON, fieldTypes, sFieldNames);

      final IVectorLayer output = getNewVectorLayer(RESULT, Sextante.getText("Union"), IVectorLayer.SHAPE_TYPE_POLYGON,
               fieldTypes, sFieldNames);

      //first we do an intersection

      setProgressText(Sextante.getText("Intersection") + "(1/4)");
      IFeatureIterator iter = layer1.iterator();

      int i = 0;
      int iShapeCount = layer1.getShapesCount();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Object[] values = feature.getRecord().getValues();
         final Geometry g = feature.getGeometry();
         final IFeatureIterator iter2 = layer2.iterator();
         while (iter2.hasNext()) {
            final IFeature feature2 = iter2.next();
            final Geometry g2 = feature2.getGeometry();
            if (g2.intersects(g)) {
               final Object[] values2 = feature2.getRecord().getValues();
               final Geometry inter = g.intersection(g2);
               final Object[] resultValues = new Object[values.length + values2.length];
               System.arraycopy(values, 0, resultValues, 0, values.length);
               System.arraycopy(values2, 0, resultValues, values.length, values2.length);
               intersection.addFeature(inter, resultValues);
            }
         }
         iter2.close();
         i++;
      }
      iter.close();


      if (m_Task.isCanceled()) {
         return false;
      }

      //copy the resulting features to the final layer
      setProgressText(Sextante.getText("Copying_entities") + "(2/4)");
      intersection.close();
      try {
         intersection.postProcess();
      }
      catch (final Exception e) {
         throw new GeoAlgorithmExecutionException(e.getMessage());
      }
      intersection.open();
      iter = intersection.iterator();

      i = 0;
      iShapeCount = intersection.getShapesCount();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         output.addFeature(feature);
      }
      iter.close();

      //Now we calculate difference between layer 1 and intersection
      //and add the resulting entities to the final layer
      m_ClipGeometry = computeJtsClippingPoly(intersection);
      setProgressText(Sextante.getText("Copying_entities") + "(3/4)");
      iter = layer1.iterator();
      i = 0;
      iShapeCount = layer1.getShapesCount();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry g = difference(feature.getGeometry());
         if (g != null) {
            final Object[] values = feature.getRecord().getValues();
            final Object[] resultValues = new Object[output.getFieldCount()];
            System.arraycopy(values, 0, resultValues, 0, values.length);
            output.addFeature(g, resultValues);
         }
         i++;
      }
      iter.close();

      if (m_Task.isCanceled()) {
         return false;
      }

      //And now the same difference but with layer2
      setProgressText(Sextante.getText("Copying_entities") + "(4/4)");
      iter = layer2.iterator();
      i = 0;
      iShapeCount = layer1.getShapesCount();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry g = difference(feature.getGeometry());
         if (g != null) {
            final Object[] values = feature.getRecord().getValues();
            final Object[] resultValues = new Object[output.getFieldCount()];
            System.arraycopy(values, 0, resultValues, layer1.getFieldCount(), values.length);
            output.addFeature(g, resultValues);
         }
         i++;
      }
      iter.close();

      intersection.close();

      return !m_Task.isCanceled();

   }


   public Geometry difference(final Geometry g) throws GeoAlgorithmExecutionException {

      if (g == null) {
         return null;
      }

      final Geometry env = g.getEnvelope();
      if (env == null) {
         return null;
      }
      if (!env.intersects(m_ClipGeometry.getEnvelope())) {
         return null;
      }
      if (g.intersects(m_ClipGeometry)) {
         try {
            final Geometry newGeom = g.difference(m_ClipGeometry);
            return newGeom;
         }
         catch (final org.locationtech.jts.geom.TopologyException e) {
            if (!g.isValid()) {
               throw new GeoAlgorithmExecutionException("Wrong input geometry");
            }
            if (!m_ClipGeometry.isValid()) {
               throw new GeoAlgorithmExecutionException("Wrong clipping geometry");
            }
         }
      }
      return null;
   }


   private Geometry computeJtsClippingPoly(final IVectorLayer layer) throws IteratorException {

      Geometry currentGeometry;
      Geometry geometry = null;
      final GeometryFactory geomFact = new GeometryFactory();

      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext()) {
         final IFeature feature = iter.next();
         currentGeometry = feature.getGeometry();
         if (geometry == null) {
            geometry = currentGeometry;
         }
         else {
            final Geometry[] geoms = new Geometry[2];
            geoms[0] = geometry;
            geoms[1] = currentGeometry;
            final GeometryCollection gc = geomFact.createGeometryCollection(geoms);
            geometry = gc.buffer(0d);
         }
      }
      iter.close();

      return geometry;

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Union"));
      setGroup(Sextante.getText("Tools_for_polygon_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER1, Sextante.getText("Layer_1"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         m_Parameters.addInputVectorLayer(LAYER2, Sextante.getText("Layer_2"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Capa_union"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   public Geometry clipGeometry(final Geometry g) throws GeoAlgorithmExecutionException {

      if (g == null) {
         return null;
      }

      final Geometry env = g.getEnvelope();
      if (env == null) {
         return null;
      }
      if (!env.intersects(m_ClipGeometry.getEnvelope())) {
         return null;
      }
      if (g.intersects(m_ClipGeometry)) {
         try {
            final Geometry newGeom = g.symDifference(m_ClipGeometry);
            return newGeom;
         }
         catch (final org.locationtech.jts.geom.TopologyException e) {
            if (!g.isValid()) {
               throw new GeoAlgorithmExecutionException("Wrong input geometry");
            }
            if (!m_ClipGeometry.isValid()) {
               throw new GeoAlgorithmExecutionException("Wrong clipping geometry");
            }
         }
      }
      return null;
   }

}
