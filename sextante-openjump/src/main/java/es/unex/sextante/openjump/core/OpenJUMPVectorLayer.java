package es.unex.sextante.openjump.core;

import java.util.List;

import org.locationtech.jts.geom.*;
import org.openjump.core.apitools.IOTools;

import com.vividsolutions.jump.coordsys.CoordinateSystem;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerManager;

import es.unex.sextante.dataObjects.AbstractVectorLayer;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;

/**
 * This class wraps an OpenJUMP vector layer, so it can be used by SEXTANTE.
 * It will also be used by SEXTANTE algorithms to create
 * new vector layers from algorithms, so they are based on the OpenJUMP data model
 * 
 * @author volaya
 * 
 */
public class OpenJUMPVectorLayer extends AbstractVectorLayer {

   /**
    * The name of the layer
    */
   private String m_sName;

   /**
    * The filename associated with this layer
    */
   private String m_sFilename;

   private Layer  m_BaseLayer;


   /**
    * Creates a new vector layer from an OpenJUMp Layer
    * 
    * @param layer
    *                the layer to wrap
    */
   public void create(final Layer layer) {

      m_BaseLayer = layer;
      m_sName = layer.getName();

   }


   /**
    * Creates a new vector layer from scratch, based on a set of characteristics
    * 
    * @param sName
    *                the name to give to the layer
    * @param types
    *                an array of classes representing data types of the fields in the attributes table of the layer
    * @param fields
    *                the name of the fields in the attributes table of the layer
    * @param sFilename
    *                the filename associated with this layer
    * @param crs
    *                the coordinate system to set for this layer
    */
   public void create(final String sName,
                      final Class<?>[] types,
                      final String[] fields,
                      final String sFilename,
                      final Object crs) {

      final FeatureSchema schema = new FeatureSchema();
      schema.addAttribute("geom", AttributeType.GEOMETRY);
      for (int i = 0; i < fields.length; i++) {
         schema.addAttribute(fields[i], AttributeType.toAttributeType(types[i]));
      }
      schema.setCoordinateSystem((CoordinateSystem) crs);
      final FeatureCollection fc = new FeatureDataset(schema);
      final LayerManager layerManager = new LayerManager();
      layerManager.setFiringEvents(false);
      m_BaseLayer = new Layer(sName, layerManager.generateLayerFillColor(), fc, layerManager);
      m_sName = sName;
      m_sFilename = sFilename;
   }


   /**
    * Adds a new feature to the layer
    * 
    * @param geom geometry of the feature to add
    * @param attrs attributes of the feature to add
    */
   public void addFeature(final Geometry geom,
                          final Object[] attrs) {

      if (m_BaseLayer != null) {
         final Object[] allAttrs = new Object[attrs.length + 1];
         allAttrs[0] = geom;
         System.arraycopy(attrs, 0, allAttrs, 1, attrs.length);
         final FeatureCollectionWrapper fc = m_BaseLayer.getFeatureCollectionWrapper();
         final Feature feature = new BasicFeature(fc.getFeatureSchema());
         feature.setAttributes(allAttrs);
         fc.add(feature);
      }

   }


   /**
    * Returns the number of fields in the attributes table
    * 
    * @return the number of attributes associates to each geometry
    */
   public int getFieldCount() {

      if (m_BaseLayer != null) {
         final FeatureCollectionWrapper fc = m_BaseLayer.getFeatureCollectionWrapper();

         // We assume that the first attribute of the feature is its geometry,
         // and that there is always a geometry
         return fc.getFeatureSchema().getAttributeCount() - 1;
      }
      else {
         return 0;
      }

   }


   /**
    * Returns the name of a given field in the attributes table
    * 
    * @param iIndex the zero-based field index
    * @return the name of the selected attribute field
    */
   public String getFieldName(final int iIndex) {

      if (m_BaseLayer != null) {

         final FeatureCollectionWrapper fc = m_BaseLayer.getFeatureCollectionWrapper();

         // We assume that the first attribute of the feature is its geometry,
         // and that there is always a geometry
         return fc.getFeatureSchema().getAttributeName(iIndex + 1);
      }
      else {
         return null;
      }

   }


   /**
    * Return a class representing the data type of a given field
    * 
    * @param iIndex the zero-based field index
    * @return the data type of the selected attribute field
    */
   public Class<?> getFieldType(final int iIndex) {

      if (m_BaseLayer != null) {
         final FeatureCollectionWrapper fc = m_BaseLayer.getFeatureCollectionWrapper();

         // We assume that the first attribute of the feature is its geometry, and that there is always a geometry
         return fc.getFeatureSchema().getAttributeType(iIndex + 1).toJavaClass();
      }
      else {
         return Object.class;
      }

   }


   /**
    * Returns the type of geometries in this layer
    * 
    * @return the type of geometries in this layer
    */
   public int getShapeType() {

      if (m_BaseLayer != null) {
         final FeatureCollectionWrapper fc = m_BaseLayer.getFeatureCollectionWrapper();

         // we just take the first geometry in the layer and check its type
         final List<Feature> features = fc.getFeatures();
         final Feature feature = features.get(0);
         final Geometry geom = feature.getGeometry();

         if ((geom instanceof LineString) || (geom instanceof MultiLineString)) {
            return IVectorLayer.SHAPE_TYPE_LINE;
         }
         else if ((geom instanceof Polygon) || (geom instanceof MultiPolygon)) {
            return IVectorLayer.SHAPE_TYPE_POLYGON;
         }
         else {
            return IVectorLayer.SHAPE_TYPE_POINT;
         }

      }
      else {
         return IVectorLayer.SHAPE_TYPE_WRONG;
      }

   }


   /**
    * Returns the number of features in this layer
    * 
    * @return the number of features in this layer
    */
   @Override
   public int getShapesCount() {

      if (m_BaseLayer != null) {
         final Layer layer = m_BaseLayer;
         final FeatureCollectionWrapper fc = layer.getFeatureCollectionWrapper();
         return fc.size();
      }
      else {
         return 0;
      }


   }


   /**
    * Returns the CRS used for this layer
    * 
    * @return the CRS used for this layer
    */
   public Object getCRS() {

      if (m_BaseLayer != null) {
         final FeatureCollectionWrapper fc = m_BaseLayer.getFeatureCollectionWrapper();
         return fc.getWrappee().getFeatureSchema().getCoordinateSystem();
      }
      else {
         return null;
      }

   }


   /**
    * This method closes the data object, which was opened using the open() method.
    */
   public void close() {
     // We are not doing anything here, since it is not needed.
   }


   /**
    * Returns the name of the layer
    * 
    * @return the name of the layer
    */
   public String getName() {

      return m_sName;

   }


   /**
    * This methods initialize the data object, so it is ready to be accessed
    */
   public void open() {

     // There is really nothing to do here. Everything was done in the constructor.

   }


   /**
    * This method is called once the algorithm has finished creating this layer, so everything needed to finished the creation of
    * the layer should be done here. In this case, we just take the feature collection and save it to the selected file. After
    * that, that layer is opened and the feature collection that we had is not used anymore, thus freeing memory. After this
    * operation, the layer represented by this layer will be file-based.
    */
   public void postProcess() throws Exception {

      if (m_BaseLayer != null) {
         final FeatureCollectionWrapper fcw = m_BaseLayer.getFeatureCollectionWrapper();

         IOTools.saveShapefile(fcw, m_sFilename);
         final FeatureCollection fc = IOTools.loadShapefile(m_sFilename);
         final OpenJUMPOutputFactory fact = (OpenJUMPOutputFactory) SextanteGUI.getOutputFactory();
         final LayerManager layerManager = fact.getContext().getLayerManager();
         m_BaseLayer = new Layer(m_sName, layerManager.generateLayerFillColor(), fc, layerManager);

      }

   }


   /**
    * Sets a new name for this layer
    * 
    * @param sName
    *                the new name
    */
   public void setName(final String sName) {

      m_sName = sName;

   }


   @Override
   public Object getBaseDataObject() {

      return m_BaseLayer;

   }


   public IFeatureIterator iterator() {

      return new OpenJUMPFeatureIterator(m_BaseLayer, getFilters());

   }


   public boolean canBeEdited() {

      return false;

   }


   public void free() {}


   public IOutputChannel getOutputChannel() {

      return new FileOutputChannel(m_sFilename);

   }

   //[sstein 26. Oct. 2012] added method back-in
   public String getFilename() {
	return m_sFilename;
   }
}
