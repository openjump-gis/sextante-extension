package es.unex.sextante.openjump.core;

import javax.swing.JDialog;

import com.vividsolutions.jump.workbench.WorkbenchContext;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.ITaskMonitor;
import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.gui.core.DefaultTaskMonitor;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;

/**
 * This output factory generates objects based on the openJUMP data object model.
 * 
 * An output factory is used by SEXTANTE to generate new data objects as output from geoalgorithms
 * 
 * @author volaya
 * 
 */
public class OpenJUMPOutputFactory extends OutputFactory {

   /**
    * 
    */
   private final WorkbenchContext m_Context;


   /**
    * constructor for this factory
    * 
    * @param context
    *                the workbench context in openJUMP. This will be used later by methods from this and other classes dealing
    *                with results within openJUMP.
    */
   public OpenJUMPOutputFactory(final WorkbenchContext context) {

      m_Context = context;

   }


   @Override
   public IVectorLayer getNewVectorLayer(final String sName,
                                         final int iShapeType,
                                         final Class<?>[] types,
                                         final String[] sFields,
                                         final IOutputChannel channel,
                                         final Object crs) throws UnsupportedOutputChannelException {

      if (channel instanceof FileOutputChannel) {
         final String sFilename = ((FileOutputChannel) channel).getFilename();
         final OpenJUMPVectorLayer vectorLayer = new OpenJUMPVectorLayer();
         vectorLayer.create(sName, types, sFields, sFilename, crs);
         return vectorLayer;
      }
      else {
         throw new UnsupportedOutputChannelException();
      }


   }


   @Override
   public IVectorLayer getNewVectorLayer(final String name,
                                         final int shapeType,
                                         final Class<?>[] types,
                                         final String[] fields,
                                         final IOutputChannel channel,
                                         final Object crs,
                                         final int[] fieldSize) throws UnsupportedOutputChannelException {

      return getNewVectorLayer(name, shapeType, types, fields, channel, crs);

   }


   @Override
   public IRasterLayer getNewRasterLayer(final String sName,
                                         int iDataType,
                                         final AnalysisExtent extent,
                                         final int iBands,
                                         final IOutputChannel channel,
                                         final Object crs) throws UnsupportedOutputChannelException {

      //a quick fix to avoid the "unsupported data type" exception thrown
      // by jai when trying to create a tiff from a raster with double values
      if (iDataType == IRasterLayer.RASTER_DATA_TYPE_DOUBLE) {
         iDataType = IRasterLayer.RASTER_DATA_TYPE_FLOAT;
      }
      if (channel instanceof FileOutputChannel) {
         final String sFilename = ((FileOutputChannel) channel).getFilename();
         final OpenJUMPRasterLayer rasterLayer = new OpenJUMPRasterLayer();
         rasterLayer.create(sName, sFilename, extent, iDataType, iBands, crs);
         return rasterLayer;
      }
      else {
         throw new UnsupportedOutputChannelException();
      }

   }


   @Override
   public ITable getNewTable(final String sName,
                             final Class<?>[] types,
                             final String[] sFields,
                             final IOutputChannel channel) throws UnsupportedOutputChannelException {

      if (channel instanceof FileOutputChannel) {
         final String sFilename = ((FileOutputChannel) channel).getFilename();
         final OpenJUMPTable table = new OpenJUMPTable();
         table.create(sName, types, sFields, sFilename);
         return table;
      }
      else {
         throw new UnsupportedOutputChannelException();
      }

   }


   @Override
   public String getTempFolder() {

      return System.getProperty("java.io.tmpdir");

   }


   @Override
   public String[] getRasterLayerOutputExtensions() {

      return new String[] { "tif" };

   }


   @Override
   public String[] getVectorLayerOutputExtensions() {

      return new String[] { "shp" };

   }


   @Override
   public String[] getTableOutputExtensions() {

      return new String[] { "dbf" };

   }


   @Override
   public ITaskMonitor getTaskMonitor(final String sTitle,
                                      final boolean bDeterminate,
                                      final JDialog parent) {

      return new DefaultTaskMonitor(sTitle, bDeterminate, parent);

   }


   /**
    * Returns the openJUMP plugin context
    * 
    * @return the WorkbenchContext
    */
   public WorkbenchContext getContext() {

      return m_Context;

   }


   @Override
   public Object getDefaultCRS() {

      return null;

   }

}
