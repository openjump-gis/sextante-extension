package es.unex.sextante.gridCategorical.fragstatsArea;

import es.unex.sextante.math.simpleStats.SimpleStats;

public class ClassInfo {

   private double            m_dArea               = 0;
   private int               m_iAreaInCells        = 0;
   private double            m_dPerimeter          = 0;
   private int               m_iPerimeterInCells   = 0;
   private double            m_dLargestPatchArea   = 0;
   private double            m_dTotalLandscapeArea = 0;
   private int               m_iPatches            = 0;
   private final SimpleStats m_Area;
   private final SimpleStats m_RadiusOfGyration;


   public ClassInfo() {

      m_Area = new SimpleStats();
      m_RadiusOfGyration = new SimpleStats();

   }


   public void setTotalLandscapeArea(final double dArea) {

      m_dTotalLandscapeArea = dArea;

   }


   public void add(final PatchInfo info) {

      m_iPatches++;
      m_dArea += info.getArea();
      m_iAreaInCells += info.getAreaInCells();
      m_dPerimeter += info.getPerimeter();
      m_iPerimeterInCells += info.getPerimeterInCells();
      m_dLargestPatchArea = Math.max(m_dLargestPatchArea, info.getArea());
      m_Area.addValue(info.getArea());
      m_RadiusOfGyration.addValue(info.getRadiusOfGyration());

   }


   public double getTotalArea() {

      return m_dArea;

   }


   public double getPercentageOfLandscape() {

      return m_dArea / m_dTotalLandscapeArea * 100;

   }


   public int getPatchesCount() {

      return m_iPatches;

   }


   public double getPatchDensity() {

      return m_iPatches * 10000. / m_dTotalLandscapeArea * 100;

   }


   public double getTotalEdge() {

      return m_dPerimeter;

   }


   public double getEdgeDensity() {

      return m_dPerimeter * 10000. / m_dTotalLandscapeArea;

   }


   public double getLandscapeShapeIndex() {

      int min;
      final int n = (int) Math.floor(Math.sqrt(m_dArea));
      final double m = m_dArea - n * n;

      if (m == 0) {
         min = 4 * n;
      }
      else if (m_dArea > n * (n + 1)) {
         min = 4 * n + 4;
      }
      else {
         min = 4 * n + 2;
      }

      return (double) m_iPerimeterInCells / (double) min;
   }


   public double getLargestPatchIndex() {

      return m_dLargestPatchArea / m_dTotalLandscapeArea * 100;

   }


   public SimpleStats getPatchAreaDistribution() {

      return m_Area;

   }


   public SimpleStats getRadiusOfGyrationDistribution() {

      return m_RadiusOfGyration;

   }

}
