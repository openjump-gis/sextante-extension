package es.unex.sextante.gridCategorical.fragstatsDiversity;

import es.unex.sextante.dataObjects.IRasterLayer;

public class PatchInfo {

   private final int          m_iClass;
   private int          m_iCells;
   private final IRasterLayer m_Window;


   public int getClassID() {

      return m_iClass;

   }


   public PatchInfo(final int iClass,
                    final IRasterLayer window) {

      m_iClass = iClass;
      m_Window = window;
      m_iCells = 0;

   }


   public void addCell() {

      m_iCells++;

   }


   public double getArea() {

      return m_Window.getWindowCellSize() * m_Window.getWindowCellSize() * m_iCells;

   }

}
