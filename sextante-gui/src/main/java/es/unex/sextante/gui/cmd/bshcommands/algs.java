package es.unex.sextante.gui.cmd.bshcommands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import bsh.CallStack;
import bsh.Interpreter;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.Sextante;

/**
 * A BeanShell command to show all available SEXTANTE algorithms
 * 
 * @author volaya
 * 
 */
public class algs {

   public static void invoke(final Interpreter env,
                             final CallStack callstack) {

      int i;

      final HashMap<String, HashMap<String, GeoAlgorithm>> map = Sextante.getAlgorithms();
      final Set<String> set = map.keySet();
      final Iterator<String> iter = set.iterator();

      final ArrayList<ObjectAndDescription> list = new ArrayList<ObjectAndDescription>();
      while (iter.hasNext()) {
         final HashMap<String, GeoAlgorithm> map2 = map.get(iter.next());
         final Set<String> set2 = map2.keySet();
         final Iterator<String> iter2 = set2.iterator();
         while (iter2.hasNext()) {
            final GeoAlgorithm alg = map2.get(iter2.next());
            list.add(new ObjectAndDescription(alg.getName(), alg.getCommandLineName()));
         }
      }

      final ObjectAndDescription[] algs = list.toArray(new ObjectAndDescription[0]);
      Arrays.sort(algs);

      final StringBuffer sb = new StringBuffer();
      for (i = 0; i < algs.length; i++) {
         try {
            sb.append(getfixedLengthString(algs[i].getDescription()) + ": ");
            sb.append((String) algs[i].getObject() + "\n");
         }
         catch (final Exception e) {}
      }
      sb.append("\n" + Integer.toString(algs.length) + " algorithms found");

      env.println(sb.toString());

   }


   private static String getfixedLengthString(final String s) {

      final String sResult = s + "--------------------------------------------------------------------";

      return sResult.substring(0, 50);
   }
}
