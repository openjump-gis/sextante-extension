package es.unex.sextante.closestpts;

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
   private final double  m_dX[];
   private final double  m_dY[];
   private final double  m_dZ[];


   public NearestNeighbourFinder(final IVectorLayer layer,
                                 final int iField,
                                 final ITaskMonitor task) throws IteratorException {

      int i;
      float x, y;

      task.setProgressText(Sextante.getText("Creating_index"));

      m_Tree = new STRtree();

      m_iPoints = layer.getShapesCount();
      m_dX = new double[m_iPoints];
      m_dY = new double[m_iPoints];
      m_dZ = new double[m_iPoints];
      i = 0;
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && !task.isCanceled()) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate coord = geom.getCoordinate();
         x = (float) coord.x;
         y = (float) coord.y;
         final Envelope bounds = geom.getEnvelopeInternal();
         m_Tree.insert(bounds, new Integer(i));
         m_dX[i] = x;
         m_dY[i] = y;
         try {
            m_dZ[i] = Double.parseDouble(feature.getRecord().getValue(iField).toString());
         }
         catch (final NumberFormatException e) {
            m_dZ[i] = 0;
         }
         if (i % 50 == 0) {
            task.setProgress(i, m_iPoints);
         }
         i++;
      }
      iter.close();
   }


   public PtAndDistance[] getClosestPoints(final double x,
                                           final double y,
                                           final double dDistance) {

      int i;
      int iID;
      double dDist;
      final Coordinate pt = new Coordinate(x, y);
      final Envelope search = new Envelope(pt);
      search.expandBy(dDistance);
      final List<Integer> list = m_Tree.query(search);
      final int iSize = list.size();
      final PtAndDistance pts[] = new PtAndDistance[iSize];
      for (i = 0; i < iSize; i++) {
         iID = list.get(i).intValue();
         dDist = pt.distance(new Coordinate(m_dX[iID], m_dY[iID]));
         pts[i] = new PtAndDistance(new Point3D(m_dX[iID], m_dY[iID], m_dZ[iID]), dDist);
      }

      return pts;

   }

}
