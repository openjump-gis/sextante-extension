package es.unex.sextante.additionalInfo;

/**
 * Additional information for a parameter representing a numerical value
 * 
 * @author volaya
 * 
 */
public class AdditionalInfoNumericalValue
         implements
            AdditionalInfo {

   public static final int NUMERICAL_VALUE_INTEGER = 1;
   public static final int NUMERICAL_VALUE_DOUBLE  = 2;

   private int             m_iType;
   private double          m_dMinValue             = Double.NEGATIVE_INFINITY;
   private double          m_dMaxValue             = Double.MAX_VALUE;
   private double          m_dDefaultValue         = Double.MAX_VALUE;


   /**
    * Constructor
    * 
    * @param iType
    *                the type of numerical value(integer or double)
    * @param dDefaultValue
    *                the default value
    * @param dMinValue
    *                the minimum value allowed for this parameter
    * @param dMaxValue
    *                the maximum value allowed for this parameter
    */
   public AdditionalInfoNumericalValue(final int iType,
                                       final double dDefaultValue,
                                       final double dMinValue,
                                       final double dMaxValue) {

      m_iType = iType;
      m_dDefaultValue = dDefaultValue;
      m_dMaxValue = dMaxValue;
      m_dMinValue = dMinValue;

   }


   /**
    * Returns the maximum value allowed for this parameter
    * 
    * @return the maximum value allowed for this parameter
    */
   public double getMaxValue() {

      return m_dMaxValue;

   }


   /**
    * Sets the maximum value allowed for this parameter
    * 
    * @param dMaxValue
    *                the maximum value allowed for this parameter
    */
   public void setMaxValue(final double dMaxValue) {

      m_dMaxValue = dMaxValue;

   }


   /**
    * Returns the maximum value allowed for this parameter
    * 
    * @return the maximum value allowed for this parameter
    */
   public double getMinValue() {

      return m_dMinValue;

   }


   /**
    * Sets the minimum value allowed for this parameter
    * 
    * @param dMinValue
    *                the minimum value allowed for this parameter
    */
   public void setMinValue(final double dMinValue) {

      m_dMinValue = dMinValue;

   }


   /**
    * Returns the type of numerical value (integer or double)
    * 
    * @return the type of numerical value (integer or double)
    */
   public int getType() {

      return m_iType;

   }


   /**
    * Sets the type of numerical value (integer or double)
    * 
    * @param iType
    *                the type of numerical value (integer or double)
    */
   public void setType(final int iType) {

      m_iType = iType;

   }


   /**
    * Returns the default value
    * 
    * @return the default value
    */
   public double getDefaultValue() {

      return m_dDefaultValue;

   }


   /**
    * Sets the default value
    * 
    * @param defaultValue
    *                the default value
    */
   public void setDefaultValue(final double defaultValue) {

      m_dDefaultValue = defaultValue;

   }


   public String getTextDescription() {

      final StringBuffer sb = new StringBuffer();
      if (m_iType == NUMERICAL_VALUE_DOUBLE) {
         sb.append("Value type: double\n");

      }
      else {
         sb.append("Value type: integer\n");
      }
      sb.append("Max value: " + Double.toString(m_dMaxValue) + "\n");
      sb.append("Min value: " + Double.toString(m_dMinValue) + "\n");
      sb.append("Default value: " + Double.toString(m_dDefaultValue) + "\n");

      return sb.toString();

   }

}
