package es.unex.sextante.modeler.elements;


public class ModelElementInputArray
         implements
            IModelElement {

   private int             m_iType;

   public static final int DATA_TYPE_RASTER         = 1;
   public static final int DATA_TYPE_VECTOR_POINT   = 2;
   public static final int DATA_TYPE_VECTOR_LINE    = 3;
   public static final int DATA_TYPE_VECTOR_POLYGON = 4;
   public static final int DATA_TYPE_VECTOR_ANY     = 5;
   public static final int DATA_TYPE_TABLE          = 6;
   public static final int DATA_TYPE_BAND           = 7;


   public int getType() {

      return m_iType;

   }


   public void setType(final int type) {

      m_iType = type;

   }

}
