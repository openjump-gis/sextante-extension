

package es.unex.sextante.dataObjects;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Envelope;

import es.unex.sextante.dataObjects.vectorFilters.IVectorLayerFilter;
import es.unex.sextante.exceptions.IteratorException;


/**
 * A convenience class which implements some of the methods of the IVectorLayer interface. Extending this class is recommended
 * instead of implementing the interface directly
 * 
 * @author volaya
 * 
 */
public abstract class AbstractVectorLayer
         implements
            IVectorLayer {

   private int                                 m_iShapesCount;
   private boolean                             m_bShapesCountAndExtentCalculated = false;
   private Rectangle2D                         m_Extent;
   private final ArrayList<IVectorLayerFilter> m_Filters                         = new ArrayList<IVectorLayerFilter>();


   public abstract Object getBaseDataObject();


   public String[] getFieldNames() {

      final String[] names = new String[getFieldCount()];

      for (int i = 0; i < names.length; i++) {
         names[i] = getFieldName(i);
      }

      return names;

   }


   public int getFieldIndexByName(final String sFieldName) {

      for (int i = 0; i < this.getFieldCount(); i++) {
         final String sName = getFieldName(i);
         if (sName.equalsIgnoreCase(sFieldName)) {
            return i;
         }
      }

      return -1;

   }


   public Class[] getFieldTypes() {

      final Class[] types = new Class[getFieldCount()];

      for (int i = 0; i < types.length; i++) {
         types[i] = getFieldType(i);
      }

      return types;

   }


   @Override
   public String toString() {

      return this.getName();

   }


   public void addFeature(final IFeature feature) {

      addFeature(feature.getGeometry(), feature.getRecord().getValues());

   }


   public int getShapesCount() {

      if (!m_bShapesCountAndExtentCalculated) {
         calculateShapesCountAndExtent();
      }
      return m_iShapesCount;

   }


   public Rectangle2D getFullExtent() {

      if (!m_bShapesCountAndExtentCalculated) {
         calculateShapesCountAndExtent();
      }
      return m_Extent;

   }


   private void calculateShapesCountAndExtent() {

      Envelope envelope = null;
      final IFeatureIterator iter = iterator();
      m_iShapesCount = 0;

      while (iter.hasNext()) {
         IFeature feature;
         try {
            feature = iter.next();
         }
         catch (final IteratorException e) {
            m_Extent = new Rectangle2D.Double(0, 0, 0, 0);
            m_iShapesCount = 0;
            return;
         }
         if (m_iShapesCount == 0) {
            envelope = feature.getGeometry().getEnvelopeInternal();
         }
         else {
            envelope.expandToInclude(feature.getGeometry().getEnvelopeInternal());
         }
         m_iShapesCount++;
      }

      if (m_iShapesCount == 0) {
         m_Extent = new Rectangle2D.Double();
      }
      else {
         m_Extent = new Rectangle2D.Double(envelope.getMinX(), envelope.getMinY(), envelope.getWidth(), envelope.getHeight());
      }

      m_bShapesCountAndExtentCalculated = true;

   }


   @Override
   public void addFilter(final IVectorLayerFilter filter) {

      m_Filters.add(filter);
      m_bShapesCountAndExtentCalculated = false;

   }


   @Override
   public void removeFilters() {

      m_Filters.clear();
      m_bShapesCountAndExtentCalculated = false;

   }


   public List<IVectorLayerFilter> getFilters() {

      return m_Filters;

   }


}
