package es.unex.sextante.gui.algorithm.iterative;

import java.awt.geom.Rectangle2D;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.dataObjects.AbstractVectorLayer;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.IVectorLayerFilter;
import es.unex.sextante.outputs.IOutputChannel;

public class SingleFeatureVectorLayer
         extends
            AbstractVectorLayer {


   private String         m_sName;
   private Object[]       m_Record;
   private Geometry       m_Geometry;
   private final String[] m_sFields;
   private final Object   m_CRS;
   private IVectorLayer   m_OriginalLayer;
   private final int      m_iShapeType;


   public SingleFeatureVectorLayer(final IVectorLayer layer) {

      m_sFields = layer.getFieldNames();
      m_CRS = layer.getCRS();
      m_OriginalLayer = layer;
      m_sName = layer.getName();
      m_iShapeType = layer.getShapeType();

   }


   public void setFeature(final Geometry geometry,
                          final Object[] record) {

      m_Geometry = geometry;
      m_Record = record;

   }


   public void addFeature(final Geometry geometry,
                          final Object[] attributes) {}


   public int getFieldCount() {

      return m_Record.length;

   }


   public String getFieldName(final int iIndex) {

      return m_sFields[iIndex];

   }


   public Class getFieldType(final int iIndex) {

      return m_Record[iIndex].getClass();
   }


   public int getShapeType() {

      return m_iShapeType;

   }


   public IFeatureIterator iterator() {

      return new SingleFeatureIterator(m_Geometry, m_Record);

   }


   public Object getCRS() {

      return m_CRS;

   }


   @Override
   public Rectangle2D getFullExtent() {

      final Envelope env = m_Geometry.getEnvelopeInternal();
      return new Rectangle2D.Double(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());

   }


   public void close() {}


   public IOutputChannel getOutputChannel() {

      return null;

   }


   public String getName() {

      return m_sName;

   }


   public void open() {}


   public void postProcess() throws Exception {}


   public void setName(final String sName) {

      m_sName = sName;

   }


   public IVectorLayer getOriginalLayer() {

      return m_OriginalLayer;

   }


   @Override
   public void addFilter(final IVectorLayerFilter filter) {}


   @Override
   public void removeFilters() {}


   @Override
   public Object getBaseDataObject() {

      return null;

   }


   public void free() {

      m_OriginalLayer = null;

   }


   public boolean canBeEdited() {

      return false;

   }


}
