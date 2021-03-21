package es.unex.sextante.gui.grass;

import java.util.ArrayList;
import java.util.List;

import es.unex.sextante.core.Sextante;


/**
 * A hard-coded list of those GRASS algorithms not compatible with SEXTANTE, that should not be shown in the toolbox or any other
 * SEXTANTE component
 *
 * @author volaya
 *
 */
public class GrassBlackList {

   private static List           m_List;
   private static final String[] ALGS = { "v.build.polylines", "v.build", "v.category", "v.convert", "v.db.connect",
            "v.digit", "v.in.db", "v.in.sites", "v.kernel", "v.label.sa",
            "v.label", "v.lrs.create", "v.lrs.label", "v.lrs.segment", "v.lrs.where", "v.proj", "v.support", "v.to.rast3",
            "v.what", "v.what.rast", "r.compress", "r.random.surface", "r.region",
            "r.support", "r.support.stats", "r.timestamp", "r.to.rast3", "r.to.rast3elev", "r.what",
            "r.what.color", "v.net.alloc", "v.net", "v.net.iso", "v.net.path", "v.net.salesman", "v.net.steiner", "v.net.visibility",
            "r.le.setup", "r.le.patch","r.le.pixel","r.le.trace","r.li.cwed","r.li.dominance","r.li.edgedensity",
            "r.li.mpa","r.li.mps","r.li.padcv","r.li.padrange","r.li.padsd","r.li.patchdensity","r.li.patchnum",
            "r.li.richness","r.li.setup","r.li.shannon","r.li.shape","r.li.simpson","r.series",
            "r.blend","r.cats", "r.mask", "r.tileset","v.build.all", "v.centroids", "v.convert.all",
            "v.db.addcol","v.db.addtable","v.db.dropcol","v.db.droptable","v.db.join","v.db.reconnect.all",
            "v.db.renamecol","v.db.univar","v.db.update","v.in.e00","v.in.sites.all","v.univar.sh",
            "r.external","v.external", "v.colors", "v.in.garmin", "v.in.gpsbabel", "v.out.gpsbabel",
            "r.proj", "v.proj", "r.category"}; 
   
   static {

      m_List = new ArrayList();
      for (int i = 0; i < ALGS.length; i++) {
         m_List.add(ALGS[i]);
      }

   }


   public static boolean isInBlackList(final GrassAlgorithm alg) {

	   return ( isInBlackList (alg.getName()) );

   }


   public static boolean isInBlackList(final String sName) {
	   if ( m_List.contains(sName)) {
		   Sextante.addWarningToLog("SEXTANTE GRASS interface: Module " + sName + " disabled: blacklisted.");
		   return ( true );
	   }
	   return ( false );
   }
   

}
