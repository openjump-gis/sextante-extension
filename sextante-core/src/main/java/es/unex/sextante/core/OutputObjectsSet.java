package es.unex.sextante.core;

import java.util.ArrayList;

import es.unex.sextante.exceptions.WrongOutputIDException;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.Output3DRasterLayer;
import es.unex.sextante.outputs.OutputRasterLayer;
import es.unex.sextante.outputs.OutputTable;
import es.unex.sextante.outputs.OutputVectorLayer;


public class OutputObjectsSet {

   private final ArrayList m_Outputs;


   public OutputObjectsSet() {

      m_Outputs = new ArrayList();

   }


   /**
    * adds a new output to the set. If an output with the same name exists, it will change its attributes
    * 
    * @param output
    *                the output object
    */
   public void add(final Output output) {

      try {
         final Output out = getOutput(output.getName());
         out.setObjectData(output);
         //m_Outputs.remove(out);
      }
      catch (final WrongOutputIDException e) {
         m_Outputs.add(output);
      }


   }


   /**
    * Returns an output of the set, identified by its name
    * 
    * @param sName
    *                the name of the output object
    * @return the output object
    * @throws WrongOutputIDException
    *                 if no output with the specified name exists in the set
    */
   public Output getOutput(final String sName) throws WrongOutputIDException {

      int i;

      for (i = 0; i < m_Outputs.size(); i++) {
         if (((Output) m_Outputs.get(i)).getName().equals(sName)) {
            return ((Output) m_Outputs.get(i));
         }
      }

      throw new WrongOutputIDException();

   }


   /**
    * Returns an output of the set, identified by its index in it
    * 
    * @param iIndex
    *                the index of the output in the set
    * @return the output object
    * @throws ArrayIndexOutOfBoundsException
    *                 if iIndex is not a valid array index
    */
   public Output getOutput(final int iIndex) throws ArrayIndexOutOfBoundsException {

      if ((iIndex >= 0) && (iIndex < m_Outputs.size())) {
         return (Output) m_Outputs.get(iIndex);
      }
      else {
         throw new ArrayIndexOutOfBoundsException();
      }

   }


   /**
    * Returns the number of data objects (layers and tables) in the set
    * 
    * @return the number of data objects in the set
    */
   public int getOutputDataObjectsCount() {

      int iCount = 0;

      for (int i = 0; i < getOutputObjectsCount(); i++) {
         final Output out = getOutput(i);
         if ((out instanceof OutputRasterLayer) || (out instanceof OutputVectorLayer) || (out instanceof OutputTable)
             || (out instanceof Output3DRasterLayer)) {
            iCount++;
         }
      }

      return iCount;

   }


   /**
    * Returns true if the set contains output layers or tables
    * 
    * @return true if the set contains output layers or tables
    */
   public boolean hasDataObjects() {

      return getOutputDataObjectsCount() != 0;

   }


   /**
    * Return true if the set contains output layers
    * 
    * @return true if the set contains output layers
    */
   public boolean hasLayers() {

      return getOutputLayersCount() != 0;

   }


   /**
    * Returns the number of output layers in the set
    * 
    * @return the number of output layers in the set
    */
   public int getOutputLayersCount() {

      int iCount = 0;

      for (int i = 0; i < getOutputObjectsCount(); i++) {
         final Output out = getOutput(i);
         if ((out instanceof OutputRasterLayer) || (out instanceof OutputVectorLayer) || (out instanceof Output3DRasterLayer)) {
            iCount++;
         }
      }

      return iCount;

   }


   /**
    * Returns the number of output raster layers in the set
    * 
    * @return the number of output raster layers in the set
    */
   public int getRasterLayersCount() {

      int iCount = 0;

      for (int i = 0; i < getOutputObjectsCount(); i++) {
         final Output out = getOutput(i);
         if (out instanceof OutputRasterLayer) {
            iCount++;
         }
      }

      return iCount;

   }


   /**
    * Returns the number of output 3D raster layers in the set
    * 
    * @return the number of output 3D raster layers in the set
    */
   public int get3DRasterLayersCount() {

      int iCount = 0;

      for (int i = 0; i < getOutputObjectsCount(); i++) {
         final Output out = getOutput(i);
         if (out instanceof Output3DRasterLayer) {
            iCount++;
         }
      }

      return iCount;

   }


   /**
    * Returns the number of output vector layers in the set
    * 
    * @return the number of output vector layers in the set
    */
   public int getVectorLayersCount() {

      int iCount = 0;

      for (int i = 0; i < getOutputObjectsCount(); i++) {
         final Output out = getOutput(i);
         if (out instanceof OutputVectorLayer) {
            iCount++;
         }
      }

      return iCount;

   }


   /**
    * Removes an output object from the set
    * 
    * @param out
    *                the output to remove
    * @return true if the given output was found in the set
    */
   public boolean remove(final Output out) {

      return m_Outputs.remove(out);

   }


   /**
    * Removes an output object from the set
    * 
    * @param sName
    *                the name of output to remove
    * @return true if the given output was found in the set
    */
   public boolean remove(final String sName) {

      Output out;
      try {
         out = this.getOutput(sName);
         return m_Outputs.remove(out);
      }
      catch (final WrongOutputIDException e) {
         return false;
      }

   }


   /**
    * Returns a new instance of the set and all its elements
    * 
    * @return a new instance of the set
    */
   public OutputObjectsSet getNewInstance() {

      final OutputObjectsSet oos = new OutputObjectsSet();

      for (int i = 0; i < m_Outputs.size(); i++) {
         final Output out = (Output) m_Outputs.get(i);
         oos.add(out.getNewInstance());
      }

      return oos;
   }


   /**
    * Returns the total number of output objects
    * 
    * @return the total number of output objects
    */
   public int getOutputObjectsCount() {

      return m_Outputs.size();

   }


   /**
    * Return true if the set contains an object associated to the passed key
    * 
    * @param key
    *                a string key
    * @return true if the set contains the key
    */
   public boolean containsKey(final String key) {

      int i;

      for (i = 0; i < m_Outputs.size(); i++) {
         if (((Output) m_Outputs.get(i)).getName().equals(key)) {
            return true;
         }
      }

      return false;

   }


}
