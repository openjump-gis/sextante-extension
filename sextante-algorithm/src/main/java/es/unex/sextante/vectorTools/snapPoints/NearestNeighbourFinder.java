package es.unex.sextante.vectorTools.snapPoints;

import java.util.List;

import org.locationtech.jts.geom.Coordinate;
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
   private final int     m_iPoints;


   public NearestNeighbourFinder(final IVectorLayer layer,
                                 final ITaskMonitor task) throws IteratorException {

      int i;
      float x, y;

      task.setProgressText(Sextante.getText("Creating_index"));

      m_Tree = new STRtree();

      m_iPoints = layer.getShapesCount();
      i = 0;
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && !task.isCanceled()) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate coord = geom.getCoordinate();
         x = (float) coord.x;
         y = (float) coord.y;
         final Envelope bounds = geom.getEnvelopeInternal();
         m_Tree.insert(bounds, geom);
         if (i % 50 == 0) {
            task.setProgress(i, m_iPoints);
         }
         i++;
      }
      iter.close();
   }


   public List<Geometry> getClosestGeometries(final Coordinate c,
                                              final double dDistance) {

      final Envelope search = new Envelope(c);
      search.expandBy(dDistance);
      final List<Geometry> list = m_Tree.query(search);

      return list;

   }

}
