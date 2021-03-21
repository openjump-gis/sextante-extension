package es.unex.sextante.vectorTools.NNSpatialJoin;

import java.util.Properties;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import gnu.trove.TIntProcedure;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.core.ITaskMonitor;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.IteratorException;

public class SextanteRTree {

   private final RTree      m_Tree;
   private final int        m_iShapes;
   private final IFeature[] m_Features;


   //TODO:this tree loads all features in memory, so it will not work with large datasets
   public SextanteRTree(final IVectorLayer layer,
                        final ITaskMonitor task) {

      int i;

      task.setProgressText(Sextante.getText("Creating_index"));
      m_Tree = new RTree();
      m_Tree.init(new Properties());

      m_iShapes = layer.getShapesCount();
      m_Features = new IFeature[m_iShapes];
      i = 0;
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && !task.isCanceled()) {
         try {
            final IFeature feature = iter.next();
            final Geometry geom = feature.getGeometry();
            final Envelope bounds = geom.getEnvelopeInternal();
            final Rectangle rect = new Rectangle((float) bounds.getMinX(), (float) bounds.getMinY(), (float) bounds.getMaxX(),
                     (float) bounds.getMaxY());
            m_Tree.add(rect, new Integer(i));
            m_Features[i] = feature;

            if (i % 50 == 0) {
               task.setProgress(i, m_iShapes);
            }
            i++;
         }
         catch (final IteratorException e1) {
            //skip feature
         }
      }
      iter.close();

   }


   public IFeature getClosestFeature(final double x,
                                     final double y) {

      final Point pt = new Point((float) x, (float) y);
      //final int iIdx = m_Tree.nearest(pt);
      final int[] iIdx = new int[]{-1};
      m_Tree.nearestN(pt, new TIntProcedure() {
         public boolean execute(int i) {
            iIdx[0] = i;
            return true;
         }
      }, 1, Float.MAX_VALUE);
      return m_Features[iIdx[0]];

   }


}
