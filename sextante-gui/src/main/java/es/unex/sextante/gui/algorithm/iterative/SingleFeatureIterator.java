package es.unex.sextante.gui.algorithm.iterative;

import java.awt.geom.Rectangle2D;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.dataObjects.FeatureImpl;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;

public class SingleFeatureIterator
         implements
            IFeatureIterator {


   private final Geometry m_Geometry;
   private final Object[] m_Record;
   private boolean        m_bHasNext;


   public SingleFeatureIterator(final Geometry geometry,
                                final Object[] record) {

      m_Geometry = geometry;
      m_Record = record;
      m_bHasNext = true;

   }


   public void close() {}


   public int getFeatureCount() {

      return 1;

   }


   public boolean hasNext() {

      return m_bHasNext;
   }


   public IFeature next() {

      m_bHasNext = false;
      return new FeatureImpl(m_Geometry, m_Record);

   }


   public Rectangle2D getExtent() {

      final Envelope envelope = m_Geometry.getEnvelopeInternal();
      return new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(), envelope.getHeight());

   }

}
