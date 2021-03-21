package es.unex.sextante.vectorTools.InPolygonSpatialJoinB;

import java.util.List;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.index.strtree.STRtree;

import es.unex.sextante.core.ITaskMonitor;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.IteratorException;

public class NearestNeighbourFinder {

   private final STRtree m_Tree;
   private final int     m_iShapes;


   public NearestNeighbourFinder(final IVectorLayer layer,
                                 final ITaskMonitor task) throws IteratorException {

      int i;
      final float x, y;

      task.setProgressText(Sextante.getText("Creating_index"));

      m_Tree = new STRtree();

      m_iShapes = layer.getShapesCount();
      i = 0;
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && !task.isCanceled()) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Envelope bounds = geom.getEnvelopeInternal();
         m_Tree.insert(bounds, feature);
         if (i % 100 == 0) {
            task.setProgress(i, m_iShapes);
         }
         i++;
      }
      iter.close();
   }


   public IFeature[] getClosestPoints(final Geometry geom) {

      final Envelope search = geom.getEnvelopeInternal();
      final List<IFeature> list = m_Tree.query(search);

      return list.toArray(new IFeature[0]);

   }

}
