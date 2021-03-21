package es.unex.sextante.additionalInfo;

public class AdditionalInfoVectorLayer
         extends
            AdditionalInfoDataObject {

   public static final int SHAPE_TYPE_POINT   = 0;
   public static final int SHAPE_TYPE_LINE    = 1;
   public static final int SHAPE_TYPE_POLYGON = 2;
   public static final int SHAPE_TYPE_ANY     = -1;

   private int             m_iShapeType       = -1;
   //true, if this is a 3D raster (=voxel data)
   private boolean         m_bIs3d            = false;


   public AdditionalInfoVectorLayer(final int iShapeType,
                                    final boolean bIsMandatory) {

      super(bIsMandatory);

      m_iShapeType = iShapeType;

   }


   /**
    * Checks whether the vector layer is 3D.
    * 
    * @return true if it is a 3D layer.
    * 
    */
   public boolean getIs3d() {
      return m_bIs3d;
   }


   /**
    * Sets whether the vector layer is 3D.
    * 
    * @param is3d
    *                true if it is a 3D layer.
    */
   public void setIs3d(final boolean is3d) {
      m_bIs3d = is3d;
   }


   public void setShapeType(final int iShapeType) {

      m_iShapeType = iShapeType;

   }


   public int getShapeType() {

      return m_iShapeType;
   }


   public String getTextDescription() {
      // TODO Auto-generated method stub
      return null;
   }

}
