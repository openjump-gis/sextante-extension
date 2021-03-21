package es.unex.sextante.rTree;

import java.util.Properties;

import com.infomatiq.jsi.Point;
import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import gnu.trove.TIntProcedure;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.closestpts.Point3D;
import es.unex.sextante.core.ITaskMonitor;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.IteratorException;

public class SextanteRTree {

   private final RTree  m_Tree;
   private final int    m_iPoints;
   private final double m_dX[];
   private final double m_dY[];
   private final double m_dZ[];


   public SextanteRTree(final IVectorLayer layer,
                        final int iField,
                        final ITaskMonitor task) {

      int i;
      float x, y;

      task.setProgressText(Sextante.getText("Creating_index"));
      m_Tree = new RTree();
      m_Tree.init(new Properties());
      m_iPoints = layer.getShapesCount();
      m_dX = new double[m_iPoints];
      m_dY = new double[m_iPoints];
      m_dZ = new double[m_iPoints];
      i = 0;
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && !task.isCanceled()) {
         IFeature feature;
         try {
            feature = iter.next();
            final Geometry geom = feature.getGeometry();
            final Coordinate coord = geom.getCoordinate();
            x = (float) coord.x;
            y = (float) coord.y;
            final Rectangle rectangle = new Rectangle(x, y, x, y);
            m_Tree.add(rectangle, i);
            m_dX[i] = x;
            m_dY[i] = y;
            try {
               m_dZ[i] = Double.parseDouble(feature.getRecord().getValue(iField).toString());
            }
            catch (final Exception e) {
               m_dZ[i] = 0;
            }
            if (i % 50 == 0) {
               task.setProgress(i, m_iPoints);
            }
            i++;
         }
         catch (final IteratorException e1) {
            //skip point
         }
      }

   }


   public Point3D getClosestPoint(final double x,
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
      return new Point3D(m_dX[iIdx[0]], m_dY[iIdx[0]], m_dZ[iIdx[0]]);

   }


}
