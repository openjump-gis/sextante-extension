package es.unex.sextante.dataObjects;

import java.awt.image.DataBuffer;

import es.unex.sextante.core.AnalysisExtent;

/**
 * This is the base interface that all 3D raster object have to implement to be able to be used by SEXTANTE algorithms.
 * 
 * @author Victor Olaya. volaya@unex.es
 * 
 */
public interface I3DRasterLayer
         extends
            ILayer {

   public static final int RASTER_DATA_TYPE_FLOAT  = DataBuffer.TYPE_FLOAT;
   public static final int RASTER_DATA_TYPE_DOUBLE = DataBuffer.TYPE_DOUBLE;
   public static final int RASTER_DATA_TYPE_INT    = DataBuffer.TYPE_INT;
   public static final int RASTER_DATA_TYPE_SHORT  = DataBuffer.TYPE_SHORT;
   public static final int RASTER_DATA_TYPE_BYTE   = DataBuffer.TYPE_BYTE;


   /**
    * Returns the data type of the layer
    * 
    * @return the data type of the layer
    */
   public int getDataType();


   /**
    * Returns the extent of the whole layer
    * 
    * @return the extent of the whole layer
    */
   public AnalysisExtent getLayerExtent();


   /**
    * Return the original cellsize of the layer. X and Y cellsizes are assumed to be equal
    * 
    * @return the original cellsize of the layer
    */
   public double getCellSize();


   /**
    * Return the original vertical cellsize (Z) of the layer.
    * 
    * @return the original vertical cellsize (Z) of the layer
    */
   public double getCellSizeZ();


   /**
    * Sets the value at a cell
    * 
    * @param x
    *                the x coordinate
    * @param y
    *                the y coordinate
    * @param z
    *                the z coordinate
    * @param dValue
    *                the new value
    */
   public void setCellValue(int x,
                            int y,
                            int z,
                            double dValue);


   /**
    * Gets the no-data value of the layer
    * 
    * @return the no-data value of the layer
    */
   public double getNoDataValue();


   /**
    * Sets the no-data value of the layer
    * 
    * @param dNoDataValue
    *                the new no-data value
    */
   public void setNoDataValue(double dNoDataValue);


   /**
    * Set the value of a cell to the no-data value
    * 
    * @param x
    *                the x coordinate of the cell to set to no-data
    * @param y
    *                the y coordinate of the cell to set to no-data
    * @param z
    *                the z coordinate of the cell to set to no-data
    */
   public void setNoData(int x,
                         int y,
                         int z);


   /**
    * Checks if the given value equals the no-data value of the layer
    * 
    * @param dNoDataValue
    *                a value to check
    * @return true if the given value equals the no-data value of the layer
    */
   public boolean isNoDataValue(double dNoDataValue);


   public byte getCellValueAsByte(int x,
                                  int y,
                                  int z);


   public short getCellValueAsShort(int x,
                                    int y,
                                    int z);


   public int getCellValueAsInt(int x,
                                int y,
                                int z);


   public float getCellValueAsFloat(int x,
                                    int y,
                                    int z);


   public double getCellValueAsDouble(int x,
                                      int y,
                                      int z);


   /**
    * Returns the value at a given world coordinate.
    * 
    * @param x
    *                the x coordinate
    * @param y
    *                the y coordinate
    * @param z
    *                the y coordinate
    * @return the value at the given world coordinate
    */
   public double getValueAt(double x,
                            double y,
                            double z);


   /**
    * 
    * @param x
    *                the x coordinate(col) of the cell
    * @param y
    *                the y coordinate(row) of the cell
    * @param z
    *                the z coordinate of the cell
    * 
    * @return true if the given cell is within the query window extent
    */
   public boolean isInWindow(int x,
                             int y,
                             int z);


   /**
    * Returns the number of columns in the query window of this layer
    * 
    * @return the number of columns in the query window
    */
   public int getNX();


   /**
    * Returns the number of rows in the query window of this layer
    * 
    * @return the number of rows in the query window
    */
   public int getNY();


   /**
    * Returns the number of vertical (z) cols in the query window
    * 
    * @return the number of vertical (z) cols in the query window
    */
   public int getNZ();

}
