package es.unex.sextante.core;

import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.dataObjects.ILayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.parameters.RasterLayerAndBand;

public abstract class AbstractInputFactory
         implements
            IInputFactory {

   public static final int SHAPE_TYPE_ANY = AdditionalInfoVectorLayer.SHAPE_TYPE_ANY;

   protected IDataObject[] m_Objects;


   public void clearDataObjects() {

      m_Objects = null;

   }


   public void removeDataObject(final String sName) {

      final ArrayList<IDataObject> list = new ArrayList<IDataObject>();
      for (int i = 0; i < m_Objects.length; i++) {
         if (!m_Objects[i].getName().equals(sName)) {
            list.add(m_Objects[i]);
         }
      }

      m_Objects = list.toArray(new IDataObject[0]);

   }


   public void addDataObject(final IDataObject obj) {

      final IDataObject[] newObjects = new IDataObject[m_Objects.length + 1];
      System.arraycopy(m_Objects, 0, newObjects, 0, m_Objects.length);
      newObjects[m_Objects.length] = obj;
      m_Objects = newObjects;

   }


   public void removeObject(final IDataObject obj) {

      final ArrayList<IDataObject> list = new ArrayList();
      for (int i = 0; i < m_Objects.length; i++) {
         if (m_Objects[i] != obj) {
            list.add(m_Objects[i]);
         }
      }
      m_Objects = list.toArray(new IDataObject[0]);
   }


   public RasterLayerAndBand[] getBands() {

      final ArrayList list = new ArrayList();

      final IRasterLayer[] layers = getRasterLayers();

      for (int i = 0; i < layers.length; i++) {
         for (int j = 0; j < layers[i].getBandsCount(); j++) {
            final RasterLayerAndBand rab = new RasterLayerAndBand(layers[i], j);
            list.add(rab);
         }
      }

      final RasterLayerAndBand[] bands = new RasterLayerAndBand[list.size()];
      for (int i = 0; i < list.size(); i++) {
         bands[i] = (RasterLayerAndBand) list.get(i);
      }

      return bands;

   }


   public ILayer[] getLayers() {

      final ArrayList list = new ArrayList();

      final Object[] objs = getDataObjects();

      for (int i = 0; i < objs.length; i++) {
         if (objs[i] instanceof ILayer) {
            list.add(objs[i]);
         }
      }

      final ILayer[] layers = new ILayer[list.size()];
      for (int i = 0; i < layers.length; i++) {
         layers[i] = (ILayer) list.get(i);
      }

      return layers;

   }


   public IRasterLayer[] getRasterLayers() {

      final ArrayList list = new ArrayList();

      final Object[] objs = getDataObjects();

      for (int i = 0; i < objs.length; i++) {
         if (objs[i] instanceof IRasterLayer) {
            list.add(objs[i]);
         }
      }

      final IRasterLayer[] layers = new IRasterLayer[list.size()];
      for (int i = 0; i < layers.length; i++) {
         layers[i] = (IRasterLayer) list.get(i);
      }

      return layers;

   }


   public I3DRasterLayer[] get3DRasterLayers() {

      final ArrayList list = new ArrayList();

      final Object[] objs = getDataObjects();

      for (int i = 0; i < objs.length; i++) {
         if (objs[i] instanceof I3DRasterLayer) {
            list.add(objs[i]);
         }
      }

      final I3DRasterLayer[] layers = new I3DRasterLayer[list.size()];
      for (int i = 0; i < layers.length; i++) {
         layers[i] = (I3DRasterLayer) list.get(i);
      }

      return layers;

   }


   public IVectorLayer[] getVectorLayers(final int shapeType) {

      final ArrayList list = new ArrayList();

      final Object[] objs = getDataObjects();

      for (int i = 0; i < objs.length; i++) {
         if (objs[i] instanceof IVectorLayer) {
            final IVectorLayer layer = (IVectorLayer) objs[i];
            if (layer.getShapeType() != IVectorLayer.SHAPE_TYPE_WRONG) {
               if ((layer.getShapeType() == shapeType) || (shapeType == SHAPE_TYPE_ANY)) {
                  list.add(objs[i]);
               }
            }
         }
      }

      final IVectorLayer[] layers = new IVectorLayer[list.size()];
      for (int i = 0; i < layers.length; i++) {
         layers[i] = (IVectorLayer) list.get(i);
      }

      return layers;

   }


   public ITable[] getTables() {

      final ArrayList list = new ArrayList();

      final Object[] objs = getDataObjects();

      for (int i = 0; i < objs.length; i++) {
         if (objs[i] instanceof ITable) {
            list.add(objs[i]);
         }
      }

      final ITable[] tables = new ITable[list.size()];
      for (int i = 0; i < tables.length; i++) {
         tables[i] = (ITable) list.get(i);
      }

      return tables;

   }


   public IDataObject[] getDataObjects() {

      return m_Objects;

   }


   public IDataObject getInputFromName(final String sName) {

      for (int i = 0; i < m_Objects.length; i++) {
         if (m_Objects[i].getName().equals(sName)) {
            return m_Objects[i];
         }
      }

      return null;

   }

}
