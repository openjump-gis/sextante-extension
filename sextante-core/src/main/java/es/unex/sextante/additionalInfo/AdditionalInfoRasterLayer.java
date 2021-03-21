package es.unex.sextante.additionalInfo;

public class AdditionalInfoRasterLayer
         extends
            AdditionalInfoDataObject {


   //true, if this is a 3D raster (=voxel data)
   //private boolean m_bIs3d = false;


   public AdditionalInfoRasterLayer(final boolean bIsMandatory) {

      super(bIsMandatory);

   }


   /**
    * Checks whether the raster layer is 3D.
    * 
    * @return true if it is a 3D layer.
    * 
    */
   //   public boolean getIs3d() {
   //     return m_bIs3d;
   //   }

   /**
    * Sets whether the raster layer is 3D.
    * 
    * @param is3d
    *                true if it is a 3D layer.
    */
   //   public void setIs3d(final boolean is3d) {
   //      m_bIs3d = is3d;
   //   }

   public String getTextDescription() {
      // TODO Auto-generated method stub
      return null;
   }

}
