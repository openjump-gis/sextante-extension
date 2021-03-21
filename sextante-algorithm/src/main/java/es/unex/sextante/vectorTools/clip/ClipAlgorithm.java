

package es.unex.sextante.vectorTools.clip;

import java.util.Iterator;
import java.util.List;

import org.locationtech.jts.geom.Envelope;
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
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public class ClipAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER     = "LAYER";
   public static final String CLIPLAYER = "CLIPLAYER";
   public static final String RESULT    = "RESULT";

   private IVectorLayer       m_Output;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      final IVectorLayer layerClip = m_Parameters.getParameterValueAsVectorLayer(CLIPLAYER);
      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
         layerClip.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final STRtree tree = buildClipTree(layerClip);

      m_Output = getNewVectorLayer(RESULT, Sextante.getText("Clipped_layer"), layerIn.getShapeType(), layerIn.getFieldTypes(),
               layerIn.getFieldNames());

      final IFeatureIterator iter = layerIn.iterator();

      int i = 0;
      final int iShapeCount = layerIn.getShapesCount();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry g = clipGeometry(feature.getGeometry(), tree);
         if (g != null) {
            m_Output.addFeature(g, feature.getRecord().getValues());
         }
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Clip"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer_to_clip"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY,
                  true);
         m_Parameters.addInputVectorLayer(CLIPLAYER, Sextante.getText("Clipping_layer"),
                  AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Clipped_Layer"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   public Geometry clipGeometry(final Geometry g,
                                final STRtree tree) throws GeoAlgorithmExecutionException {

      if (g == null) {
         return null;
      }

      final Envelope env = g.getEnvelopeInternal();
      if (env == null) {
         return null;
      }

      final List candidates = tree.query(env);
      Geometry clipGeometry = null;
      Geometry result = g;
      boolean intersected = false;
      if ((candidates == null) || (candidates.size() == 0)) {
         return null;
      }
      else {
         try {
            for (final Iterator it = candidates.iterator(); it.hasNext();) {
               final PreparedGeometry pg = (PreparedGeometry) it.next();
               if (pg.intersects(result)) {
                  intersected = true;
                  clipGeometry = pg.getGeometry();
                  result = clipGeometry.intersection(result);
               }
            }
            if (!intersected) {
               return null;
            }
            if (result.getNumGeometries() == 0) {
               return null;
            }
            return result;
         }
         catch (final org.locationtech.jts.geom.TopologyException e) {
            if (!g.isValid()) {
               throw new GeoAlgorithmExecutionException("Wrong input geometry");
            }
            if (!clipGeometry.isValid()) {
               throw new GeoAlgorithmExecutionException("Wrong clipping geometry");
            }
         }

      }
      return null;
   }


   private STRtree buildClipTree(final IVectorLayer layer) throws IteratorException {

      final STRtree tree = new STRtree();

      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext()) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final PreparedGeometry pg = PreparedGeometryFactory.prepare(geom);
         tree.insert(pg.getGeometry().getEnvelopeInternal(), pg);
      }
      iter.close();

      return tree;

   }

}
