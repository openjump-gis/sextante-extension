package es.unex.sextante.io3d;

import java.awt.geom.Rectangle2D;
import java.io.File;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.dataObjects.I3DRasterLayer;
import es.unex.sextante.outputs.FileOutputChannel;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.rasterWrappers.Grid3DCell;

public class Default3DRasterLayer
         implements
            I3DRasterLayer {

   private AnalysisExtent m_Extent;
   private double         m_dData[][][];
   private double         m_dNoDataValue = -99999d;
   private String         m_sFilename;
   private String         m_sName;
   private Object         m_CRS;


   public void create(final String sName,
                      final String sFilename,
                      final AnalysisExtent ae,
                      final Object crs) {

      m_CRS = crs;
      m_sName = sName;
      m_sFilename = sFilename;
      m_Extent = ae;
      m_dData = new double[ae.getNX()][ae.getNY()][ae.getNZ()];


   }


   @Override
   public byte getCellValueAsByte(final int x,
                                  final int y,
                                  final int z) {

      return (byte) getCellValueAsDouble(x, y, z);

   }


   @Override
   public double getCellValueAsDouble(final int x,
                                      final int y,
                                      final int z) {

      try {
         return m_dData[x][y][z];
      }
      catch (final Exception e) {
         return m_dNoDataValue;
      }

   }


   @Override
   public float getCellValueAsFloat(final int x,
                                    final int y,
                                    final int z) {

      return (float) getCellValueAsDouble(x, y, z);

   }


   @Override
   public int getCellValueAsInt(final int x,
                                final int y,
                                final int z) {

      return (int) getCellValueAsDouble(x, y, z);

   }


   @Override
   public short getCellValueAsShort(final int x,
                                    final int y,
                                    final int z) {

      return (short) getCellValueAsDouble(x, y, z);

   }


   @Override
   public int getDataType() {

      return RASTER_DATA_TYPE_DOUBLE;

   }


   @Override
   public double getCellSize() {

      return m_Extent.getCellSize();

   }


   @Override
   public double getCellSizeZ() {

      return m_Extent.getCellSizeZ();

   }


   @Override
   public AnalysisExtent getLayerExtent() {

      return m_Extent;

   }


   @Override
   public int getNX() {

      return m_dData.length;

   }


   @Override
   public int getNY() {

      return m_dData[0].length;
   }


   @Override
   public int getNZ() {

      return m_dData[0][0].length;

   }


   @Override
   public double getNoDataValue() {

      return m_dNoDataValue;

   }


   @Override
   public double getValueAt(final double x,
                            final double y,
                            final double z) {

      final Grid3DCell cell = m_Extent.getGridCoordsFromWorldCoords(x, y, z);
      return getCellValueAsDouble(cell.getX(), cell.getY(), cell.getZ());

   }


   @Override
   public boolean isInWindow(final int x,
                             final int y,
                             final int z) {

      return (x >= 0) && (y >= 0) && (z >= 0) && (x < getNX()) && (y < getNY()) && (z < getNZ());

   }


   @Override
   public boolean isNoDataValue(final double dNoDataValue) {

      return dNoDataValue == m_dNoDataValue;
   }


   @Override
   public void setCellValue(final int x,
                            final int y,
                            final int z,
                            final double dValue) {

      try {
         m_dData[x][y][z] = dValue;
      }
      catch (final Exception e) {}

   }


   @Override
   public void setNoData(final int x,
                         final int y,
                         final int z) {

      try {
         m_dData[x][y][z] = m_dNoDataValue;
      }
      catch (final Exception e) {}

   }


   @Override
   public void setNoDataValue(final double dNoDataValue) {

      m_dNoDataValue = dNoDataValue;

   }


   @Override
   public Object getCRS() {

      return m_CRS;

   }


   @Override
   public Rectangle2D getFullExtent() {

      return m_Extent.getAsRectangle2D();

   }


   @Override
   public void close() {}


   @Override
   public Object getBaseDataObject() {

      return m_dData;

   }


   @Override
   public IOutputChannel getOutputChannel() {

      return new FileOutputChannel(m_sFilename);

   }


   @Override
   public String getName() {

      return m_sName;

   }


   @Override
   public void open() {}


   @Override
   public void postProcess() throws Exception {

      ASCII3DFileTools.writeFile(this, new File(m_sFilename));

   }


   @Override
   public void setName(final String sName) {

      m_sName = sName;

   }


   @Override
   public void free() {

      m_dData = null;

   }

}
