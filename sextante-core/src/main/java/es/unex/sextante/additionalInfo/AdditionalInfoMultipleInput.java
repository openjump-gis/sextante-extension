package es.unex.sextante.additionalInfo;

/**
 * Additional information for a parameter representing a multiple input
 * 
 * @author user
 * 
 */
public class AdditionalInfoMultipleInput
         implements
            AdditionalInfo {

   //public static final int DATA_TYPE_UNDEFINED = 0;
   public static final int DATA_TYPE_RASTER         = 1;
   public static final int DATA_TYPE_VECTOR_POINT   = 2;
   public static final int DATA_TYPE_VECTOR_LINE    = 3;
   public static final int DATA_TYPE_VECTOR_POLYGON = 4;
   public static final int DATA_TYPE_VECTOR_ANY     = 5;
   public static final int DATA_TYPE_TABLE          = 6;
   public static final int DATA_TYPE_BAND           = 7;
   //public static final int DATA_TYPE_FIELD          = 8;

   private int             m_iDataType              = 0;
   private boolean         m_bIsMandatory           = false;


   public AdditionalInfoMultipleInput(final int iDataType,
                                      final boolean bIsMandatory) {

      m_iDataType = iDataType;
      m_bIsMandatory = bIsMandatory;

   }


   public int getDataType() {

      return m_iDataType;

   }


   public void setDataType(final int dataType) {

      m_iDataType = dataType;

   }


   public void setIsMandatory(final boolean bIsMandatory) {

      m_bIsMandatory = bIsMandatory;

   }


   public boolean getIsMandatory() {

      return m_bIsMandatory;

   }


   public String getTextDescription() {
      // TODO Auto-generated method stub
      return null;
   }


}
