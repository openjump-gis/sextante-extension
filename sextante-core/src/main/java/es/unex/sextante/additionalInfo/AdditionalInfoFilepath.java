package es.unex.sextante.additionalInfo;

/**
 * Additional info for a parameter representing a filepath
 * 
 * @author Victor Olaya volaya@unex.es
 */
public class AdditionalInfoFilepath
         implements
            AdditionalInfo {

   private boolean  m_bFolder      = false;
   private boolean  m_bOpenDialog  = false;
   private String[] m_sExtensions  = null;
   //true if the file is a voxel data file
   private boolean  m_bIsVoxelData = false;


   public AdditionalInfoFilepath() {}


   /**
    * 
    * @param bFolder
    *                true if the parameter contains a folder and not a file path
    * @param bOpenDialog
    *                true if it is a file to be opened. False if it is a filename to be used to save something. This will define
    *                the type of file chooser dialog to show.
    * @param sExtension
    *                the allowed extensions extensions
    */
   public AdditionalInfoFilepath(final boolean bFolder,
                                 final boolean bOpenDialog,
                                 final String[] sExtensions) {

      m_bFolder = bFolder;
      m_bOpenDialog = bOpenDialog;
      m_sExtensions = sExtensions;

   }


   /**
    * Returns the extensions that the file must have
    * 
    * @return the file extension
    */
   public String[] getExtensions() {

      return m_sExtensions;

   }


   /**
    * Sets a new mandatory extension for the filename
    * 
    * @param sExt
    *                the new extension
    */
   public void setExtensions(final String[] sExt) {

      m_sExtensions = sExt;

   }


   /**
    * Return true if the parameter contains a folder and not a file path
    * 
    * @return whether the paramete contais a folder and not a file path
    */
   public boolean isFolder() {

      return m_bFolder;

   }


   /**
    * Sets whether the parameter contains a folder and not a file path
    * 
    * @param folder
    */
   public void setIsFolder(final boolean folder) {

      m_bFolder = folder;

   }


   /**
    * Returns whether a open file dialog or a save file dialog should be used to set the value of this parameter
    * 
    * @return whether a open file dialog or a save file dialog should be used to set the value of this parameter
    */
   public boolean isOpenDialog() {

      return m_bOpenDialog;

   }


   /**
    * Sets whether a open file dialog or a save file dialog should be used to set the value of this parameter
    * 
    * @param openDialog
    *                true if a open file dialog should be used
    */
   public void setIsOpenDialog(final boolean openDialog) {

      m_bOpenDialog = openDialog;

   }


   /**
    * Sets whether the file is a voxel data file.
    * 
    * @param isVoxelData
    *                true if it is a voxel data file.
    */
   public void setIsVoxelData(final boolean isVoxelData) {
      m_bIsVoxelData = isVoxelData;
   }


   /**
    * Checks whether the file is a voxel data file.
    * 
    * @return true if it is a voxel data file, false otherwise.
    * 
    */
   public boolean getIsVoxelData() {
      return (m_bIsVoxelData);
   }


   public String getTextDescription() {

      final StringBuffer sb = new StringBuffer();
      sb.append("Folder: " + new Boolean(m_bFolder).toString() + "\n");
      String sOpen;
      if (m_bOpenDialog) {
         sOpen = "Open";
      }
      else {
         sOpen = "Save";
      }
      sb.append("Open/Save: " + sOpen + "\n");
      if (!m_bFolder) {
         sb.append("Extensions: ");
         for (int i = 0; i < m_sExtensions.length; i++) {
            sb.append(m_sExtensions[i]);
            if (i < m_sExtensions.length - 1) {
               sb.append(",");
            }
         }
      }

      return sb.toString();

   }


}
