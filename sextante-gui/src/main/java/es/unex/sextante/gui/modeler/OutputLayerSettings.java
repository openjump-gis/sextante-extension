package es.unex.sextante.gui.modeler;

public class OutputLayerSettings {

   private String  sDescription;
   private boolean bAddToView;


   public OutputLayerSettings(final boolean addToView,
                              final String desc) {

      bAddToView = addToView;
      sDescription = desc;

   }


   public boolean getAddToView() {

      return bAddToView;

   }


   public void setAddToView(final boolean addToView) {

      bAddToView = addToView;

   }


   public String getDescription() {

      return sDescription;

   }


   public void setDescription(final String description) {

      sDescription = description;

   }


}
