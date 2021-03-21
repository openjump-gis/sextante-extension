package es.unex.sextante.gridCategorical.fragstatsArea;

import es.unex.sextante.dataObjects.IRasterLayer;

public class PatchInfo {

   private final int          m_iOffsetX[] = { -1, 0, 0, 1 };
   private final int          m_iOffsetY[] = { 0, -1, 1, 0 };

   private final int          m_iClass;
   private int                m_iCells;
   private int                m_iPerimeter;
   private int                m_iSumX, m_iSumY;
   private double             m_dCentroidX;
   private double             m_dCentroidY;
   private double             m_dRadiusOfGyration;

   private final IRasterLayer m_Window;


   public PatchInfo(final int iClass,
                    final IRasterLayer window) {

      m_Window = window;

      m_iCells = 0;
      m_iPerimeter = 0;
      m_iSumX = 0;
      m_iSumY = 0;
      m_iClass = iClass;

   }


   public void add(final int x,
                   final int y) {

      int n;
      int x2, y2;
      int iClass;

      m_iCells++;
      m_iSumX += x;
      m_iSumY += y;
      for (n = 0; n < 4; n++) {
         x2 = x + m_iOffsetX[n];
         y2 = y + m_iOffsetY[n];
         iClass = m_Window.getCellValueAsInt(x2, y2);
         if (m_Window.isNoDataValue(iClass) || (iClass != m_iClass)) {
            m_iPerimeter++;
         }
      }
   }


   public void addForRadiusOfGyration(final int x,
                                      final int y) {

      m_dRadiusOfGyration += Math.sqrt(Math.pow(x - m_dCentroidX, 2.0) + Math.pow(y - m_dCentroidY, 2.0));

   }


   public double getPerimeter() {

      return m_Window.getWindowCellSize() * m_iPerimeter;

   }


   public void calculateCentroid() {

      m_dCentroidX = m_iSumX / (double) m_iCells;
      m_dCentroidY = m_iSumY / (double) m_iCells;

   }


   public double getArea() {

      return m_Window.getWindowCellSize() * m_Window.getWindowCellSize() * m_iCells;

   }


   public double getRadiusOfGyration() {

      return m_dRadiusOfGyration / m_iCells * m_Window.getWindowCellSize();

   }


   public int getClassID() {

      return m_iClass;

   }


   public int getAreaInCells() {

      return m_iCells;

   }


   public int getPerimeterInCells() {

      return m_iPerimeter;

   }

}
