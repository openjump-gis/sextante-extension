/**
 *    @author      	Josef Bezdek, ZCU Plzen
 *	  @version     	1.0
 *    @since 		JDK1.5
 */


package es.unex.sextante.tin.linearIsolinesFromTin;

import java.util.LinkedList;

import org.locationtech.jts.geom.Coordinate;

class DVertex {
   Coordinate pointKey;
   Integer    data;
   DVertex    left;
   DVertex    right;


   DVertex(final Coordinate pointKey,
           final Integer data) {
      this.pointKey = pointKey;
      this.data = data;
   }
}


public class BinaryTree {
   DVertex    root = null;
   LinkedList helpList;


   /******************************************************************************************************************************
    * The method searchs object with pointKey index
    * 
    * @param pointKey -
    *                index of object
    * @return object with current index
    */
   public Object search(final Coordinate pointKey) {
      return searchV(root, pointKey);
   }


   /******************************************************************************************************************************
    * The private method for recursive searching
    * 
    * @param v -
    *                vertex of tree
    * @param pointKey -
    *                index of object
    * @return object with current index
    */
   private Object searchV(final DVertex v,
                          final Coordinate pointKey) {
      if (v == null) {
         return null;
      }
      if (pointKey.x == v.pointKey.x) {
         if (pointKey.y == v.pointKey.y) {
            return v;
         }
         else {
            return searchV(v.left, pointKey);
         }
      }
      if (pointKey.x < v.pointKey.x) {
         return searchV(v.left, pointKey);
      }
      else {
         return searchV(v.right, pointKey);
      }
   }


   /******************************************************************************************************************************
    * The method inserts new object into tree
    * 
    * @param pointKey -
    *                index of object
    * @param data -
    *                integer index of object to main data structure
    */
   public void insert(final Coordinate pointKey,
                      final Integer data) {
      DVertex v = null;
      DVertex vNext = root;

      while (vNext != null) {
         v = vNext;
         if (pointKey.x <= vNext.pointKey.x) {
            vNext = vNext.left;
         }
         else {
            vNext = vNext.right;
         }
      }
      final DVertex newVertex = new DVertex(pointKey, data);
      if (v == null) {
         root = newVertex;
      }
      else if (pointKey.x <= v.pointKey.x) {
         v.left = newVertex;
      }
      else {
         v.right = newVertex;
      }
   }


   /******************************************************************************************************************************
    * The method removes object from tree
    * 
    * @param pointKey -
    *                index of object
    */
   public void remove(final Coordinate pointKey) {
      DVertex v = root;
      while ((v != null) && (pointKey.x != v.pointKey.x) && (v.pointKey.y != pointKey.y)) {
         if (pointKey.x < v.pointKey.x) {
            v = v.left;
         }
         else {
            v = v.right;
         }
      }

      DVertex y = v;
      if ((v.left != null) && (v.right != null)) {
         y = v.right;
         while (y.left != null) {
            y = y.left;
         }
      }

      DVertex x;
      if (y.left != null) {
         x = y.left;
      }
      else {
         x = y.right;
      }
   }

}
