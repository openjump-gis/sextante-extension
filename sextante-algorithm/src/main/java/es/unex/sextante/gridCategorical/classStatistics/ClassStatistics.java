package es.unex.sextante.gridCategorical.classStatistics;

public class ClassStatistics {

   private final int    m_iClass;
   private int    m_iZonesCount = 0;
   private double m_dSum        = 0;
   private double m_dVar        = 0;
   private double m_dMin        = Double.MAX_VALUE;
   private double m_dMax        = 0;


   ClassStatistics(final int iClass) {

      m_iClass = iClass;

   }


   public void add(final double dArea) {

      m_iZonesCount++;
      m_dSum += dArea;
      m_dVar += (dArea * dArea);
      m_dMax = Math.max(m_dMax, dArea);
      m_dMin = Math.min(m_dMin, dArea);

   }


   public int getClassID() {

      return m_iClass;

   }


   public double getTotalArea() {

      return m_dSum;

   }


   public double getMinArea() {

      return m_dMin;

   }


   public double getMaxArea() {

      return m_dMax;

   }


   public double getMeanArea() {

      return (m_dSum / (double) m_iZonesCount);

   }


   public double getVarianceArea() {

      final double dMean = getMeanArea();

      return (m_dVar / (double) m_iZonesCount - dMean * dMean);

   }


   public int getZonesCount() {

      return m_iZonesCount;

   }

}
