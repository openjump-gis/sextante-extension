

package es.unex.sextante.gui.saga;

public class SagaBlackList {

   public static boolean isInBlackList(final String sName,
                                       final String sGroup) {

      if (sGroup.equals("Lectures")) {
         return true;
      }
      if (sGroup.equals("pointcloud_viewer")) {
         return true;
      }
      if (sGroup.startsWith("docs")) {
         return true;
      }

      return false;

   }

}
