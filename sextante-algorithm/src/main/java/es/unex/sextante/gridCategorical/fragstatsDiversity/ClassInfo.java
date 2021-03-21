package es.unex.sextante.gridCategorical.fragstatsDiversity;

public class ClassInfo {

   private double m_dArea    = 0;
   private int    m_iPatches = 0;
   private double m_dTotalLandscapeArea;


   public ClassInfo() {}


   public void setTotalLandscapeArea(final double dArea) {

      m_dTotalLandscapeArea = dArea;

   }


   public void add(final PatchInfo info) {

      m_iPatches++;
      m_dArea += info.getArea();


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

      return m_iPatches / m_dTotalLandscapeArea * 100;

   }

}
