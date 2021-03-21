

package es.unex.sextante.core;

import java.io.File;

import javax.swing.JDialog;

import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.io3d.Default3DRasterLayer;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;


/**
 * An OutputFactory defines how new data objects (layers and tables) are created. Method in this class are called from
 * geoalgorithms to create output objects
 * 
 * @author Victor Olaya volaya@unex.es
 * 
 */
public abstract class OutputFactory {

   private int    m_iCount       = 0;
   private double m_dNoDataValue = -99999;


   /**
    * Use this method to create a new IVectorLayer that can be used to generate new vector layers as output from the geo-algorithm
    * 
    * @param sName
    *                the name of the layer
    * @param iShapeType
    *                the type of shapes in the layer
    * @param types
    *                the data type of the fields
    * @param sFields
    *                the name of the fields in the attributes table
    * @param channel
    *                the output channel associated to the output layer
    * @param crs
    *                An object with information to set the CRS of this layer (i.e. a string with a EPSG code)
    * @return an empty vector layer
    */
   public abstract IVectorLayer getNewVectorLayer(String sName,
                                                  int iShapeType,
                                                  Class[] types,
                                                  String[] sFields,
                                                  IOutputChannel channel,
                                                  Object crs) throws UnsupportedOutputChannelException;


   /**
    * Use this method to create a new IVectorLayer that can be used to generate new vector layers as output from the geo-algorithm
    * 
    * @param sName
    *                the name of the layer
    * @param iShapeType
    *                the type of shapes in the layer
    * @param types
    *                the data type of the fields
    * @param sFields
    *                the name of the fields in the attributes table
    * @param channel
    *                the output channel associated to the output layer
    * @param crs
    *                An object with information to set the CRS of this layer (i.e. a string with a EPSG code)
    * @param fieldSize
    *                A list of integers. Each value represents the size of a table field.
    * @return an empty vector layer
    */
   public abstract IVectorLayer getNewVectorLayer(String sName,
                                                  int iShapeType,
                                                  Class[] types,
                                                  String[] sFields,
                                                  IOutputChannel channel,
                                                  Object crs,
                                                  int[] fieldSize) throws UnsupportedOutputChannelException;


   /**
    * Use this method to create a new raster layer as output from the geo-algorithm. The characteristics of the raster layer
    * (extent and cellsize) are taken from the extent parameter.
    * 
    * @param sName
    *                the name of the layer
    * @param iDataType
    *                the type of data in the layer
    * @param extent
    *                the AnalysisExtent to use
    * @param iBands
    *                the number of bands of the new layer
    * @param channel
    *                the output channel associated to the output layer
    * @param crs
    *                An object with information to set the CRS of this layer (i.e. a string with a EPSG code)
    * @return a raster layer
    */
   public abstract IRasterLayer getNewRasterLayer(String sName,
                                                  int iDataType,
                                                  AnalysisExtent extent,
                                                  int iBands,
                                                  IOutputChannel channel,
                                                  Object crs) throws UnsupportedOutputChannelException;


   /**
    * Use this method to create a new 3D raster layer as output from the geo-algorithm. The characteristics of the raster layer
    * (extent and cellsizes) are taken from the extent parameter.
    * 
    * A default implementation is given, which uses the Default3dRasterLayer class. This way, 3D algorithms can be executed within
    * the context of a GIS that does not support 3D layer, and there is no need to overwrite this method in the corresponding
    * binding
    * 
    * @param sName
    *                the name of the layer
    * @param iDataType
    *                the type of data in the layer
    * @param extent
    *                the AnalysisExtent to use
    * @param channel
    *                the output channel associated to the output layer
    * @param crs
    *                An object with information to set the CRS of this layer (i.e. a string with a EPSG code)
    * @return a 3D raster layer
    */
   public I3DRasterLayer getNew3DRasterLayer(final String sName,
                                             final int iDataType,
                                             final AnalysisExtent extent,
                                             final IOutputChannel channel,
                                             final Object crs) throws UnsupportedOutputChannelException {

      if (channel instanceof FileOutputChannel) {
         final String sFilename = ((FileOutputChannel) channel).getFilename();
         final Default3DRasterLayer layer = new Default3DRasterLayer();
         layer.create(sName, sFilename, extent, crs);
         return layer;
      }
      else {
         throw new UnsupportedOutputChannelException();
      }


   }


   /**
    * Use this method to create a new table.
    * 
    * @param sName
    *                the name of the table
    * @param types
    *                the data type of the fields
    * @param sFields
    *                The names of the fields in the table
    * @param channel
    *                the output channel associated to the output layer
    * @return a new empty table object
    */
   public abstract ITable getNewTable(String sName,
                                      Class[] types,
                                      String[] sFields,
                                      IOutputChannel channel) throws UnsupportedOutputChannelException;


   /**
    * 
    * @return A temporary filename with the default file extension for vector layers
    */
   public String getTempVectorLayerFilename() {

      return getTempFilenameWithoutExtension() + "." + getVectorLayerOutputExtensions()[0];

   }


   /**
    * 
    * @return A temporary filename with the default file extension for raster layers
    */
   public String getTempRasterLayerFilename() {

      return getTempFilenameWithoutExtension() + "." + getRasterLayerOutputExtensions()[0];

   }


   /**
    * 
    * @return A temporary filename with the default file extension for 3D raster layers
    */
   public String getTemp3DRasterLayerFilename() {

      return getTempFilenameWithoutExtension() + "." + get3DRasterLayerOutputExtensions()[0];

   }


   /**
    * 
    * @return A temporary filename with the default file extension for tables
    */
   public String getTempTableFilename() {

      return getTempFilenameWithoutExtension() + "." + getTableOutputExtensions()[0];
   }


   /**
    * 
    * @return A temporary folder name
    */
   public String getTempFoldername() {

      return getTempFilenameWithoutExtension();
   }


   /**
    * 
    * @param out
    *                an Output object
    * @return A temporary filename with the default file extension for the specified type of output
    */
   public String getTempFilename(final Output out) {

      if (out instanceof OutputVectorLayer) {
         return getTempVectorLayerFilename();
      }
      if (out instanceof OutputRasterLayer) {
         return getTempRasterLayerFilename();
      }
      if (out instanceof OutputTable) {
         return getTempTableFilename();
      }
      if (out instanceof Output3DRasterLayer) {
         return getTemp3DRasterLayerFilename();
      }

      else {
         return null;
      }

   }


   /**
    * Returns a temporary filename with no extension
    * 
    * @returns a temporary filename with no extension.
    */
   public String getTempFilenameWithoutExtension() {

      return getTempFolder() + File.separator + Long.toString(System.currentTimeMillis()) + Integer.toString(m_iCount++);

   }


   /**
    * Returns the temporary folder. This will be used to store outputs when they do not have an associated filename an it is
    * needed
    * 
    * @return a temporary folder
    */
   public abstract String getTempFolder();


   /**
    * Returns the extensions supported by this factory for creating raster layers. If a filename with an extension not found in
    * this list of extensions is used to create a raster layer, the default extension (the first one of the list) will be added to
    * the filename when creating that layer.
    * 
    * @return the list of supported extensions for raster layers
    */
   public abstract String[] getRasterLayerOutputExtensions();


   /**
    * Returns the extensions supported by this factory for creating 3d raster layers. If a filename with an extension not found in
    * this list of extensions is used to create a 3d raster layer, the default extension (the first one of the list) will be added
    * to the filename when creating that layer.
    * 
    * A default implementation is given, which returns just the "asc" extension used by the default implementation of the
    * getNew3DRasterLayer method, which uses the Default3DRasterLayer class
    * 
    * @return the list of supported extensions for 3d raster layers
    */
   public String[] get3DRasterLayerOutputExtensions() {

      return new String[] { "asc3d" };

   }


   /**
    * Returns the extensions supported by this factory for creating vector layers. If a filename with an extension not found in
    * this list of extensions is used to create a vector layer, the default extension (the first one of the list) will be added to
    * the filename when creating that layer.
    * 
    * @return the list of supported extensions for vector layers
    */
   public abstract String[] getVectorLayerOutputExtensions();


   /**
    * Returns the extensions supported by this factory for creating raster layers. If a filename with an extension not found in
    * this list of extensions is used to create a raster layer, the default extension (the first one of the list) will be added to
    * the filename when creating that layer.
    * 
    * @return the list of supported extensions for raster layers
    */
   public abstract String[] getTableOutputExtensions();


   /**
    * Returns the task monitor that will be used to monitor algorithm execution
    * 
    * @param sTitle
    *                a String used to identify the monitor. This will be used, for instance, as the title string of a progress
    *                dialog.
    * @param bDeterminate
    *                true if the task monitor will monitor a determinated process (i.e. number of steps to complete the process is
    *                know)
    * @return the task monitor used to monitor algorithm execution
    */
   public abstract ITaskMonitor getTaskMonitor(String sTitle,
                                               boolean bDeterminate,
                                               JDialog parent);


   /**
    * Returns the default CRS for new layers. This will be used when the geoalgorithm generates new layers but does not take any
    * other layer as input, so the CRS cannot be defined from the inputs
    * 
    * @return an object containing information about the default CRS (i.e. a String with the EPSG code of the CRS)
    */
   public abstract Object getDefaultCRS();


   /**
    * Returns the default no data value for raster layers created by this output factory
    * 
    * @return the default no data value for raster layers created by this output factory
    */
   public double getDefaultNoDataValue() {

      return m_dNoDataValue;

   }


   /**
    * Sets the default no data value to use when this output factory cretes a new raster layer
    * 
    * @param dValue
    *                the default no data value to use when this output factory cretes a new raster layer
    */
   public void setDefaultNoDataValue(final double dValue) {

      m_dNoDataValue = dValue;

   }


}
