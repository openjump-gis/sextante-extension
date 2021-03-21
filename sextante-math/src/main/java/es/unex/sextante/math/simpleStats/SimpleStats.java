package es.unex.sextante.math.simpleStats;


/**
 * A class to calculate some basic statistics
 * 
 * @author volaya
 * 
 */
public class SimpleStats {

   //private final ArrayList m_Values;
   private double m_dSum;
   private double m_dRMS;
   private double m_dMean;
   private double m_dVariance;
   private double m_dMax, m_dMin;
   private double m_dStdDev;
   //private boolean   m_bCalculated;
   private int    m_iCount;
   private double m_dM2;


   public SimpleStats() {

      //m_Values = new ArrayList();
      //m_bCalculated = false;
      m_iCount = 0;
      m_dMax = Double.NEGATIVE_INFINITY;
      m_dMin = Double.MAX_VALUE;
      m_dSum = 0;
      m_dRMS = 0;
      m_dM2 = 0;

   }


   /**
    * adds a new value to the list of values
    * 
    * @param dValue
    *                the value
    */
   public void addValue(final double dValue) {

      m_iCount++;

      final double dDelta = dValue - m_dMean;
      m_dMean = m_dMean + dDelta / m_iCount;
      m_dM2 = m_dM2 + dDelta * (dValue - m_dMean);
      m_dVariance = m_dM2 / (m_iCount - 1);

      m_dSum += dValue;
      m_dRMS += dValue * dValue;
      m_dMax = Math.max(m_dMax, dValue);
      m_dMin = Math.min(m_dMin, dValue);

      m_dStdDev = Math.sqrt(m_dVariance);
      m_dRMS = Math.sqrt(m_dRMS / m_iCount);

   }


   /**
    * Returns the coefficient of variation
    * 
    * @return the coefficient of variation
    */
   public double getCoeffOfVar() {

      return m_dVariance / m_dMean;

   }


   /**
    * Return the number of values added
    * 
    * @return the number of values added
    */
   public int getCount() {

      return m_iCount;

   }


   /**
    * Returns the maximum value
    * 
    * @return the maximum value
    */
   public double getMax() {

      return m_dMax;

   }


   /**
    * Returns the mean value
    * 
    * @return the mean value
    */
   public double getMean() {

      return m_dMean;

   }


   /**
    * Returns the minimum value
    * 
    * @return the minimum value
    */
   public double getMin() {

      return m_dMin;

   }


   /**
    * Returns the Root Mean Squared
    * 
    * @return the Root Mean Squared
    */
   public double getRMS() {

      return m_dRMS;

   }


   /**
    * Returns the standard deviation
    * 
    * @return the standard deviation
    */
   public double getStdDev() {

      return m_dStdDev;

   }


   /**
    * Returns the sum of all values
    * 
    * @return the sum of all values
    */
   public double getSum() {

      return m_dSum;

   }


   /**
    * Returns the variance
    * 
    * @return the variance
    */
   public double getVariance() {

      return m_dVariance;

   }

}
