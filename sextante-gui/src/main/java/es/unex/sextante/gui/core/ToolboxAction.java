

package es.unex.sextante.gui.core;

public abstract class ToolboxAction {

   public abstract void execute();


   public abstract String getName();


   public abstract String getGroup();


   public abstract boolean isActive();
   
   @Override
   public String toString() {
      
      return getName();
      
   }

}
