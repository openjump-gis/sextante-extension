/**
 *    @author      	Josef Bezdek, ZCU Plzen
 *	  @version     	1.0
 *    @since 		JDK1.5
 */

package es.unex.sextante.tin.smoothTinBezier;

import java.util.Iterator;
import java.util.LinkedList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

class Bezier2 {
   Coordinate normalN1 = null;
   Coordinate normalN2 = null;
   Coordinate normalN3 = null;
   Coordinate b300;
   Coordinate b030;
   Coordinate b003;
   Coordinate b012     = null;
   Coordinate b021     = null;
   Coordinate b102     = null;
   Coordinate b120     = null;
   Coordinate b210     = null;
   Coordinate b201     = null;
   Coordinate A111     = null;
   Coordinate B111     = null;
   Coordinate C111     = null;

   Coordinate G        = null;

   Coordinate A201, A102, A012, A021, B201, B102;

   Coordinate n200     = null;
   Coordinate n020     = null;
   Coordinate n002     = null;
   Coordinate n110     = null;
   Coordinate n011     = null;
   Coordinate n101     = null;


   /******************************************************************************************************************************
    * Constructor
    * 
    * @param T
    *                Trinagle fom original TIN
    * @param listA -
    *                normal vectors of planes in point A of triangle T
    * @param listB -
    *                normal vectors of planes in point B of triangle T
    * @param listC -
    *                normal vectors of planes in point C of triangle T
    */
   Bezier2(final TriangleDT T,
           final LinkedList listA,
           final LinkedList listB,
           final LinkedList listC,
           final int typeOfBreakLine) {
      b300 = T.A;
      b030 = T.B;
      b003 = T.C;
      setNormalVector(listA, listB, listC);
      setControlPoints(typeOfBreakLine);

   }


   /******************************************************************************************************************************
    * Constructor
    * 
    * @param coords -
    *                array of three vertexes which generate triangle
    */
   Bezier2(final Coordinate[] coords) {
      b300 = coords[0];
      b030 = coords[1];
      b003 = coords[2];
   }


   /******************************************************************************************************************************
    * Constructor
    * 
    * @param coords -
    *                array of three vertexes which generate triangle
    * @param listA -
    *                list of normal vektors around vertex A
    * @param listB -
    *                list of normal vektors around vertex B
    * @param listC -
    *                list of normal vektors around vertex C
    * @param typeOfBreakLine -
    *                type of break line which triangle contains
    */
   Bezier2(final Coordinate[] coords,
           final LinkedList listA,
           final LinkedList listB,
           final LinkedList listC,
           final int typeOfBreakLine) {
      b300 = coords[0];
      b030 = coords[1];
      b003 = coords[2];
      setNormalVector(listA, listB, listC);
      setControlPoints(typeOfBreakLine);
   }


   /******************************************************************************************************************************
    * The Protected method for setting one normals for each vertex of T
    * 
    * @param listA -
    *                normal vectors of planes in point A of triangle T
    * @param listB -
    *                normal vectors of planes in point B of triangle T
    * @param listC -
    *                normal vectors of planes in point C of triangle T
    */
   protected void setNormalVector(final LinkedList listA,
                                  final LinkedList listB,
                                  final LinkedList listC) {
      normalN1 = countVector(listA, b300);
      normalN2 = countVector(listB, b030);
      normalN3 = countVector(listC, b003);
   }


   /******************************************************************************************************************************
    * The method which sets new vecter between points A and B
    * 
    * @param A -
    *                start point
    * @param B -
    *                stop point
    * @return vector AB
    */
   protected static Coordinate setVector(final Coordinate A,
                                         final Coordinate B) {
      return new Coordinate(B.x - A.x, B.y - A.y, B.z - A.z);

   }


   /******************************************************************************************************************************
    * The method for setting normal vectors of two vectors
    * 
    * @param A -
    *                vector A
    * @param B -
    *                vector B
    * @return normal vector
    */
   protected static Coordinate setNormalVector(final Coordinate A,
                                               final Coordinate B) {
      final Coordinate normal = new Coordinate(A.y * B.z - A.z * B.y, A.z * B.x - A.x * B.z, (A.x * B.y - A.y * B.x));
      final double sum = Math.sqrt(Math.pow(normal.x, 2) + Math.pow(normal.y, 2) + Math.pow(normal.z, 2));
      //double sum = 1;
      if (normal.z > 0) {
         return new Coordinate((normal.x / sum), (normal.y / sum), (normal.z / sum));
      }
      else {
         return new Coordinate((-1) * (normal.x / sum), (-1) * (normal.y / sum), (-1) * (normal.z / sum));
      }
   }


   /******************************************************************************************************************************
    * The private method counts one normal vector from normal vectors of every plane in vertex of triangle
    * 
    * @param list -
    *                normal vectors of planes in point of triangle T
    * @param P /
    *                vertex of T
    * @return normal vector
    */
   private static Coordinate countVector(final LinkedList list,
                                         final Coordinate P) {
      final Iterator iter = list.iterator();
      //double koeficient = 1D;
      double sumX = 0;
      double sumY = 0;
      double sumZ = 0;
      while (iter.hasNext()) {
         final Coordinate X = (Coordinate) iter.next();
         sumX += X.x;
         sumY += X.y;
         sumZ += X.z;
      }
      final double sum = Math.sqrt(Math.pow(sumX, 2) + Math.pow(sumY, 2) + Math.pow(sumZ, 2));
      return new Coordinate((sumX / sum), (sumY / sum), (sumZ / sum));
   }


   /******************************************************************************************************************************
    * The protected method counts Scalar product of two vectors v1,v2
    * 
    * @param v1 -
    *                vector
    * @param v2 -
    *                vector
    * @return - scalar product
    */
   protected static double countScalarProduct(final Coordinate v1,
                                              final Coordinate v2) {
      final double scalar = v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
      return scalar;
   }


   /******************************************************************************************************************************
    * The protected function counts cross vector product of two vectors A,B
    * 
    * @param A -
    *                vector
    * @param B -
    *                vector
    * @return - cross product
    */
   protected static Coordinate countCrossProduct(final Coordinate A,
                                                 final Coordinate B) {
      final Coordinate normal = new Coordinate(A.y * B.z - A.z * B.y, A.z * B.x - A.x * B.z, (A.x * B.y - A.y * B.x));
      final double sum = Math.sqrt(Math.pow(normal.x, 2) + Math.pow(normal.y, 2) + Math.pow(normal.z, 2));
      if (normal.z > 0) {
         return new Coordinate((normal.x / sum), (normal.y / sum), (normal.z / sum));
      }
      else {
         return new Coordinate((-1) * (normal.x / sum), (-1) * (normal.y / sum), (-1) * (normal.z / sum));
      }

   }


   /******************************************************************************************************************************
    * The protected method counts difference of two vectors v1, v2
    * 
    * @param v1 -
    *                vector
    * @param v2 -
    *                vector
    * @return difference vector
    */
   protected static Coordinate countDifferenceProduct(final Coordinate v1,
                                                      final Coordinate v2) {
      return new Coordinate(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
   }


   /******************************************************************************************************************************
    * The protected function counts sum of two vectors v1, v2
    * 
    * @param v1 -
    *                vector
    * @param v2 -
    *                vector
    * @return sum vector
    */
   protected static Coordinate countSumProduct(final Coordinate v1,
                                               final Coordinate v2) {
      return new Coordinate(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
   }


   /******************************************************************************************************************************
    * The protected function which normalize vector v
    * 
    * @param v -
    *                vector
    * @return normalized vector
    */
   protected static Coordinate normalizeVect(final Coordinate v) {
      final double sum = Math.sqrt(Math.pow(v.x, 2) + Math.pow(v.y, 2) + Math.pow(v.z, 2));
      return new Coordinate((v.x / sum), (v.y / sum), (v.z / sum));
   }


   /******************************************************************************************************************************
    * The private function for help calculation
    */
   private double helpCount(final Coordinate Pi,
                            final Coordinate Pj,
                            final Coordinate Ni,
                            final Coordinate Nj) {
      return 2 * (countScalarProduct(countDifferenceProduct(Pj, Pi), countSumProduct(Ni, Nj)) / countScalarProduct(
               countDifferenceProduct(Pj, Pi), countDifferenceProduct(Pj, Pi)));
   }


   /******************************************************************************************************************************
    * The protected method sets Quadratic normals of Bezier triangle
    */
   protected void setQuadraticNormals() {
      n200 = normalN1;
      n020 = normalN2;
      n002 = normalN3;


      double help = helpCount(b300, b030, normalN1, normalN2);
      Coordinate dif = countDifferenceProduct(b030, b300);
      dif.x = dif.x * help;
      dif.y = dif.y * help;
      dif.z = dif.z * help;
      n110 = normalizeVect(countDifferenceProduct(countSumProduct(normalN1, normalN2), dif));
      help = helpCount(b030, b003, normalN2, normalN3);
      dif = countDifferenceProduct(b003, b030);
      dif.x = dif.x * help;
      dif.y = dif.y * help;
      dif.z = dif.z * help;
      n011 = normalizeVect(countDifferenceProduct(countSumProduct(normalN2, normalN3), dif));
      help = helpCount(b003, b300, normalN3, normalN1);
      dif = countDifferenceProduct(b300, b003);
      dif.x = dif.x * help;
      dif.y = dif.y * help;
      dif.z = dif.z * help;
      n101 = normalizeVect(countDifferenceProduct(countSumProduct(normalN3, normalN1), dif));
   }


   /******************************************************************************************************************************
    * The protected method gets normal into Bezier triangle with barycentric coordinate
    * 
    * @param u -
    *                barycentric coordinate u
    * @param v -
    *                barycentric coordinate u
    * @return normal of surface
    */
   protected Coordinate getNormal(final double u,
                                  final double v) {
      final double w = 1 - (u + v);
      final double x = n200.x * Math.pow(w, 2) + n020.x * Math.pow(u, 2) + n002.x * Math.pow(v, 2) + n110.x * w * u + n011.x * u
                       * v + n101.x * w * v;

      final double y = n200.y * Math.pow(w, 2) + n020.y * Math.pow(u, 2) + n002.y * Math.pow(v, 2) + n110.y * w * u + n011.y * u
                       * v + n101.y * w * v;

      final double z = n200.z * Math.pow(w, 2) + n020.z * Math.pow(u, 2) + n002.z * Math.pow(v, 2) + n110.z * w * u + n011.z * u
                       * v + n101.z * w * v;
      return new Coordinate(x, y, z);
   }


   /******************************************************************************************************************************
    * The protected method counts project point to plane
    * 
    * @param pointOfPlane -
    *                point which is contained into plane
    * @param normalOfPlane -
    *                normal vector of plane
    * @param pointOfLine -
    *                point which will project to plane
    * @param normalOfLine -
    *                normal wich get direction of projection to plane
    * @return coordinates of projected point in plane
    */
   protected Coordinate countProjectOnToPlane(final Coordinate pointOfPlane,
                                              final Coordinate normalOfPlane,
                                              final Coordinate pointOfLine,
                                              final Coordinate normalOfLine) {
      final double d = -normalOfPlane.x * pointOfPlane.x - normalOfPlane.y * pointOfPlane.y - normalOfPlane.z * pointOfPlane.z;
      final double param = (-pointOfLine.x * normalOfPlane.x - pointOfLine.y * normalOfPlane.y - pointOfLine.z * normalOfPlane.z - d)
                           / (normalOfLine.x * normalOfPlane.x + normalOfLine.y * normalOfPlane.y + normalOfLine.z
                                                                                                    * normalOfPlane.z);
      return new Coordinate(pointOfLine.x + param * normalOfLine.x, pointOfLine.y + param * normalOfLine.y, pointOfLine.z + param
                                                                                                            * normalOfLine.z);

   }


   /******************************************************************************************************************************
    * The method for setting control points of bezier triangle
    * 
    * @param typeOfBreakLine -
    *                type of break which triangle contains
    */
   protected void setControlPoints(final int typeOfBreakLine) {
      setQuadraticNormals();

      switch (typeOfBreakLine) {
         case (-1): {
            b210 = new Coordinate((2 * b300.x + b030.x - countScalarProduct(countDifferenceProduct(b030, b300), normalN1)
                                                         * normalN1.x) / 3, (2 * b300.y + b030.y - countScalarProduct(
                     countDifferenceProduct(b030, b300), normalN1)
                                                                                                   * normalN1.y) / 3,
                     (2 * b300.z + b030.z - countScalarProduct(countDifferenceProduct(b030, b300), normalN1) * normalN1.z) / 3);

            b120 = new Coordinate((2 * b030.x + b300.x - countScalarProduct(countDifferenceProduct(b300, b030), normalN2)
                                                         * normalN2.x) / 3, (2 * b030.y + b300.y - countScalarProduct(
                     countDifferenceProduct(b300, b030), normalN2)
                                                                                                   * normalN2.y) / 3,
                     (2 * b030.z + b300.z - countScalarProduct(countDifferenceProduct(b300, b030), normalN2) * normalN2.z) / 3);

            b021 = new Coordinate((2 * b030.x + b003.x - countScalarProduct(countDifferenceProduct(b003, b030), normalN2)
                                                         * normalN2.x) / 3, (2 * b030.y + b003.y - countScalarProduct(
                     countDifferenceProduct(b003, b030), normalN2)
                                                                                                   * normalN2.y) / 3,
                     (2 * b030.z + b003.z - countScalarProduct(countDifferenceProduct(b003, b030), normalN2) * normalN2.z) / 3);

            b012 = new Coordinate((2 * b003.x + b030.x - countScalarProduct(countDifferenceProduct(b030, b003), normalN3)
                                                         * normalN3.x) / 3, (2 * b003.y + b030.y - countScalarProduct(
                     countDifferenceProduct(b030, b003), normalN3)
                                                                                                   * normalN3.y) / 3,
                     (2 * b003.z + b030.z - countScalarProduct(countDifferenceProduct(b030, b003), normalN3) * normalN3.z) / 3);

            b201 = new Coordinate((2 * b300.x + b003.x - countScalarProduct(countDifferenceProduct(b003, b300), normalN1)
                                                         * normalN1.x) / 3, (2 * b300.y + b003.y - countScalarProduct(
                     countDifferenceProduct(b003, b300), normalN1)
                                                                                                   * normalN1.y) / 3,
                     (2 * b300.z + b003.z - countScalarProduct(countDifferenceProduct(b003, b300), normalN1) * normalN1.z) / 3);

            b102 = new Coordinate((2 * b003.x + b300.x - countScalarProduct(countDifferenceProduct(b300, b003), normalN3)
                                                         * normalN3.x) / 3, (2 * b003.y + b300.y - countScalarProduct(
                     countDifferenceProduct(b300, b003), normalN3)
                                                                                                   * normalN3.y) / 3,
                     (2 * b003.z + b300.z - countScalarProduct(countDifferenceProduct(b300, b003), normalN3) * normalN3.z) / 3);
            break;
         }
         case (0): {
            b210 = new Coordinate((2 * b300.x + b030.x) / 3, (2 * b300.y + b030.y) / 3, (2 * b300.z + b030.z) / 3);

            b120 = new Coordinate((2 * b030.x + b300.x) / 3, (2 * b030.y + b300.y) / 3, (2 * b030.z + b300.z) / 3);

            b021 = new Coordinate((2 * b030.x + b003.x - countScalarProduct(countDifferenceProduct(b003, b030), normalN2)
                                                         * normalN2.x) / 3, (2 * b030.y + b003.y - countScalarProduct(
                     countDifferenceProduct(b003, b030), normalN2)
                                                                                                   * normalN2.y) / 3,
                     (2 * b030.z + b003.z - countScalarProduct(countDifferenceProduct(b003, b030), normalN2) * normalN2.z) / 3);

            b012 = new Coordinate((2 * b003.x + b030.x - countScalarProduct(countDifferenceProduct(b030, b003), normalN3)
                                                         * normalN3.x) / 3, (2 * b003.y + b030.y - countScalarProduct(
                     countDifferenceProduct(b030, b003), normalN3)
                                                                                                   * normalN3.y) / 3,
                     (2 * b003.z + b030.z - countScalarProduct(countDifferenceProduct(b030, b003), normalN3) * normalN3.z) / 3);

            b201 = new Coordinate((2 * b300.x + b003.x - countScalarProduct(countDifferenceProduct(b003, b300), normalN1)
                                                         * normalN1.x) / 3, (2 * b300.y + b003.y - countScalarProduct(
                     countDifferenceProduct(b003, b300), normalN1)
                                                                                                   * normalN1.y) / 3,
                     (2 * b300.z + b003.z - countScalarProduct(countDifferenceProduct(b003, b300), normalN1) * normalN1.z) / 3);

            b102 = new Coordinate((2 * b003.x + b300.x - countScalarProduct(countDifferenceProduct(b300, b003), normalN3)
                                                         * normalN3.x) / 3, (2 * b003.y + b300.y - countScalarProduct(
                     countDifferenceProduct(b300, b003), normalN3)
                                                                                                   * normalN3.y) / 3,
                     (2 * b003.z + b300.z - countScalarProduct(countDifferenceProduct(b300, b003), normalN3) * normalN3.z) / 3);
            break;
         }
         case (1): {
            b210 = new Coordinate((2 * b300.x + b030.x - countScalarProduct(countDifferenceProduct(b030, b300), normalN1)
                                                         * normalN1.x) / 3, (2 * b300.y + b030.y - countScalarProduct(
                     countDifferenceProduct(b030, b300), normalN1)
                                                                                                   * normalN1.y) / 3,
                     (2 * b300.z + b030.z - countScalarProduct(countDifferenceProduct(b030, b300), normalN1) * normalN1.z) / 3);

            b120 = new Coordinate((2 * b030.x + b300.x - countScalarProduct(countDifferenceProduct(b300, b030), normalN2)
                                                         * normalN2.x) / 3, (2 * b030.y + b300.y - countScalarProduct(
                     countDifferenceProduct(b300, b030), normalN2)
                                                                                                   * normalN2.y) / 3,
                     (2 * b030.z + b300.z - countScalarProduct(countDifferenceProduct(b300, b030), normalN2) * normalN2.z) / 3);

            b021 = new Coordinate((2 * b030.x + b003.x) / 3, (2 * b030.y + b003.y) / 3, (2 * b030.z + b003.z) / 3);

            b012 = new Coordinate((2 * b003.x + b030.x) / 3, (2 * b003.y + b030.y) / 3, (2 * b003.z + b030.z) / 3);

            b201 = new Coordinate((2 * b300.x + b003.x - countScalarProduct(countDifferenceProduct(b003, b300), normalN1)
                                                         * normalN1.x) / 3, (2 * b300.y + b003.y - countScalarProduct(
                     countDifferenceProduct(b003, b300), normalN1)
                                                                                                   * normalN1.y) / 3,
                     (2 * b300.z + b003.z - countScalarProduct(countDifferenceProduct(b003, b300), normalN1) * normalN1.z) / 3);

            b102 = new Coordinate((2 * b003.x + b300.x - countScalarProduct(countDifferenceProduct(b300, b003), normalN3)
                                                         * normalN3.x) / 3, (2 * b003.y + b300.y - countScalarProduct(
                     countDifferenceProduct(b300, b003), normalN3)
                                                                                                   * normalN3.y) / 3,
                     (2 * b003.z + b300.z - countScalarProduct(countDifferenceProduct(b300, b003), normalN3) * normalN3.z) / 3);
            break;
         }
         case (2): {
            b210 = new Coordinate((2 * b300.x + b030.x - countScalarProduct(countDifferenceProduct(b030, b300), normalN1)
                                                         * normalN1.x) / 3, (2 * b300.y + b030.y - countScalarProduct(
                     countDifferenceProduct(b030, b300), normalN1)
                                                                                                   * normalN1.y) / 3,
                     (2 * b300.z + b030.z - countScalarProduct(countDifferenceProduct(b030, b300), normalN1) * normalN1.z) / 3);

            b120 = new Coordinate((2 * b030.x + b300.x - countScalarProduct(countDifferenceProduct(b300, b030), normalN2)
                                                         * normalN2.x) / 3, (2 * b030.y + b300.y - countScalarProduct(
                     countDifferenceProduct(b300, b030), normalN2)
                                                                                                   * normalN2.y) / 3,
                     (2 * b030.z + b300.z - countScalarProduct(countDifferenceProduct(b300, b030), normalN2) * normalN2.z) / 3);

            b021 = new Coordinate((2 * b030.x + b003.x - countScalarProduct(countDifferenceProduct(b003, b030), normalN2)
                                                         * normalN2.x) / 3, (2 * b030.y + b003.y - countScalarProduct(
                     countDifferenceProduct(b003, b030), normalN2)
                                                                                                   * normalN2.y) / 3,
                     (2 * b030.z + b003.z - countScalarProduct(countDifferenceProduct(b003, b030), normalN2) * normalN2.z) / 3);

            b012 = new Coordinate((2 * b003.x + b030.x - countScalarProduct(countDifferenceProduct(b030, b003), normalN3)
                                                         * normalN3.x) / 3, (2 * b003.y + b030.y - countScalarProduct(
                     countDifferenceProduct(b030, b003), normalN3)
                                                                                                   * normalN3.y) / 3,
                     (2 * b003.z + b030.z - countScalarProduct(countDifferenceProduct(b030, b003), normalN3) * normalN3.z) / 3);

            b201 = new Coordinate((2 * b300.x + b003.x) / 3, (2 * b300.y + b003.y) / 3, (2 * b300.z + b003.z) / 3);

            b102 = new Coordinate((2 * b003.x + b300.x) / 3, (2 * b003.y + b300.y) / 3, (2 * b003.z + b300.z) / 3);
            break;
         }
         case (3): {
            b210 = new Coordinate((2 * b300.x + b030.x) / 3, (2 * b300.y + b030.y) / 3, (2 * b300.z + b030.z) / 3);

            b120 = new Coordinate((2 * b030.x + b300.x) / 3, (2 * b030.y + b300.y) / 3, (2 * b030.z + b300.z) / 3);

            b021 = new Coordinate((2 * b030.x + b003.x - countScalarProduct(countDifferenceProduct(b003, b030), normalN2)
                                                         * normalN2.x) / 3, (2 * b030.y + b003.y - countScalarProduct(
                     countDifferenceProduct(b003, b030), normalN2)
                                                                                                   * normalN2.y) / 3,
                     (2 * b030.z + b003.z - countScalarProduct(countDifferenceProduct(b003, b030), normalN2) * normalN2.z) / 3);

            b012 = new Coordinate((2 * b003.x + b030.x - countScalarProduct(countDifferenceProduct(b030, b003), normalN3)
                                                         * normalN3.x) / 3, (2 * b003.y + b030.y - countScalarProduct(
                     countDifferenceProduct(b030, b003), normalN3)
                                                                                                   * normalN3.y) / 3,
                     (2 * b003.z + b030.z - countScalarProduct(countDifferenceProduct(b030, b003), normalN3) * normalN3.z) / 3);

            b201 = new Coordinate((2 * b300.x + b003.x) / 3, (2 * b300.y + b003.y) / 3, (2 * b300.z + b003.z) / 3);

            b102 = new Coordinate((2 * b003.x + b300.x) / 3, (2 * b003.y + b300.y) / 3, (2 * b003.z + b300.z) / 3);
            break;
         }

         case (5): {
            b210 = new Coordinate((2 * b300.x + b030.x - countScalarProduct(countDifferenceProduct(b030, b300), normalN1)
                                                         * normalN1.x) / 3, (2 * b300.y + b030.y - countScalarProduct(
                     countDifferenceProduct(b030, b300), normalN1)
                                                                                                   * normalN1.y) / 3,
                     (2 * b300.z + b030.z - countScalarProduct(countDifferenceProduct(b030, b300), normalN1) * normalN1.z) / 3);

            b120 = new Coordinate((2 * b030.x + b300.x - countScalarProduct(countDifferenceProduct(b300, b030), normalN2)
                                                         * normalN2.x) / 3, (2 * b030.y + b300.y - countScalarProduct(
                     countDifferenceProduct(b300, b030), normalN2)
                                                                                                   * normalN2.y) / 3,
                     (2 * b030.z + b300.z - countScalarProduct(countDifferenceProduct(b300, b030), normalN2) * normalN2.z) / 3);

            b021 = new Coordinate((2 * b030.x + b003.x) / 3, (2 * b030.y + b003.y) / 3, (2 * b030.z + b003.z) / 3);

            b012 = new Coordinate((2 * b003.x + b030.x) / 3, (2 * b003.y + b030.y) / 3, (2 * b003.z + b030.z) / 3);

            b201 = new Coordinate((2 * b300.x + b003.x) / 3, (2 * b300.y + b003.y) / 3, (2 * b300.z + b003.z) / 3);

            b102 = new Coordinate((2 * b003.x + b300.x) / 3, (2 * b003.y + b300.y) / 3, (2 * b003.z + b300.z) / 3);
            break;
         }
         case (4): {
            b210 = new Coordinate((2 * b300.x + b030.x) / 3, (2 * b300.y + b030.y) / 3, (2 * b300.z + b030.z) / 3);

            b120 = new Coordinate((2 * b030.x + b300.x) / 3, (2 * b030.y + b300.y) / 3, (2 * b030.z + b300.z) / 3);

            b021 = new Coordinate((2 * b030.x + b003.x) / 3, (2 * b030.y + b003.y) / 3, (2 * b030.z + b003.z) / 3);

            b012 = new Coordinate((2 * b003.x + b030.x) / 3, (2 * b003.y + b030.y) / 3, (2 * b003.z + b030.z) / 3);

            b201 = new Coordinate((2 * b300.x + b003.x - countScalarProduct(countDifferenceProduct(b003, b300), normalN1)
                                                         * normalN1.x) / 3, (2 * b300.y + b003.y - countScalarProduct(
                     countDifferenceProduct(b003, b300), normalN1)
                                                                                                   * normalN1.y) / 3,
                     (2 * b300.z + b003.z - countScalarProduct(countDifferenceProduct(b003, b300), normalN1) * normalN1.z) / 3);

            b102 = new Coordinate((2 * b003.x + b300.x - countScalarProduct(countDifferenceProduct(b300, b003), normalN3)
                                                         * normalN3.x) / 3, (2 * b003.y + b300.y - countScalarProduct(
                     countDifferenceProduct(b300, b003), normalN3)
                                                                                                   * normalN3.y) / 3,
                     (2 * b003.z + b300.z - countScalarProduct(countDifferenceProduct(b300, b003), normalN3) * normalN3.z) / 3);
            break;
         }
         case (6): {
            b210 = new Coordinate((2 * b300.x + b030.x) / 3, (2 * b300.y + b030.y) / 3, (2 * b300.z + b030.z) / 3);

            b120 = new Coordinate((2 * b030.x + b300.x) / 3, (2 * b030.y + b300.y) / 3, (2 * b030.z + b300.z) / 3);

            b021 = new Coordinate((2 * b030.x + b003.x) / 3, (2 * b030.y + b003.y) / 3, (2 * b030.z + b003.z) / 3);

            b012 = new Coordinate((2 * b003.x + b030.x) / 3, (2 * b003.y + b030.y) / 3, (2 * b003.z + b030.z) / 3);

            b201 = new Coordinate((2 * b300.x + b003.x) / 3, (2 * b300.y + b003.y) / 3, (2 * b300.z + b003.z) / 3);

            b102 = new Coordinate((2 * b003.x + b300.x) / 3, (2 * b003.y + b300.y) / 3, (2 * b003.z + b300.z) / 3);
         }
      }


      G = new Coordinate((b300.x + b030.x + b003.x) / 3, (b300.y + b030.y + b003.y) / 3, (b300.z + b030.z + b003.z) / 3);
      final Coordinate normalOfT = new Coordinate(0, 0, 1);

      A111 = new Coordinate((b300.x + b030.x + G.x) / 3, (b300.y + b030.y + G.y) / 3, (b300.z + b030.z + G.z) / 3);

      Coordinate e1 = normalizeVect(getNormal(1D / 2D, 0D));
      Coordinate e2 = normalizeVect(countDifferenceProduct(b120, b210));
      Coordinate normalOfPlane = setNormalVector(countCrossProduct(e1, e2), e2);

      A111 = new Coordinate(countProjectOnToPlane(b120, normalOfPlane, A111, normalOfT));//getNormal(4D/9D, 1D/9D)));

      B111 = new Coordinate((b003.x + b030.x + G.x) / 3, (b003.y + b030.y + G.y) / 3, (b003.z + b030.z + G.z) / 3);

      e1 = normalizeVect(getNormal(1D / 2D, 1D / 2D));
      e2 = normalizeVect(countDifferenceProduct(b012, b021));
      normalOfPlane = setNormalVector(countCrossProduct(e1, e2), e2);
      B111 = new Coordinate(countProjectOnToPlane(b012, normalOfPlane, B111, normalOfT));//getNormal(4D/9D, 4D/9D)));


      C111 = new Coordinate((b003.x + b300.x + G.x) / 3, (b003.y + b300.y + G.y) / 3, (b003.z + b300.z + G.z) / 3);

      e1 = normalizeVect(getNormal(0, 1 / 2D));
      e2 = normalizeVect(countDifferenceProduct(b201, b102));
      normalOfPlane = setNormalVector(countCrossProduct(e1, e2), e2);
      C111 = new Coordinate(countProjectOnToPlane(b201, normalOfPlane, C111, normalOfT));//getNormal(1D/9D, 4D/9D)));


      A201 = new Coordinate((b300.x + b210.x + b201.x) / 3, (b300.y + b210.y + b201.y) / 3, (b300.z + b210.z + b201.z) / 3);
      A021 = new Coordinate((b030.x + b120.x + b021.x) / 3, (b030.y + b120.y + b021.y) / 3, (b030.z + b120.z + b021.z) / 3);
      B102 = new Coordinate((b012.x + b102.x + b003.x) / 3, (b012.y + b102.y + b003.y) / 3, (b012.z + b102.z + b003.z) / 3);

      A102 = new Coordinate((A111.x + A201.x + C111.x) / 3, (A111.y + A201.y + C111.y) / 3, (A111.z + A201.z + C111.z) / 3);
      A012 = new Coordinate((B111.x + A021.x + A111.x) / 3, (B111.y + A021.y + A111.y) / 3, (B111.z + A021.z + A111.z) / 3);
      B201 = new Coordinate((C111.x + B111.x + B102.x) / 3, (C111.y + B111.y + B102.y) / 3, (C111.z + B111.z + B102.z) / 3);

      G = new Coordinate((A102.x + A012.x + B201.x) / 3, (A102.y + A012.y + B201.y) / 3, (A102.z + A012.z + B201.z) / 3);
   }


   /******************************************************************************************************************************
    * The method to print Bezier triangle to console
    */
   protected void printToConsole() {
      System.out.println("=========MACRO TRIANGLE=============================");
      System.out.println(b300.toString());
      System.out.println(b030.toString());
      System.out.println(b003.toString());
      System.out.println("Normals:");
      if (normalN1 != null) {
         System.out.println(normalN1.toString());
         System.out.println(normalN2.toString());
         System.out.println(normalN3.toString());
         System.out.println("Koeficients");

         System.out.println(b012.toString());
         System.out.println(b021.toString());
         System.out.println(b102.toString());
         System.out.println(b120.toString());
         System.out.println(b210.toString());
         System.out.println(b201.toString());
         //System.out.println(b111.toString());

      }
      System.out.println("======================================");
   }


   /******************************************************************************************************************************
    * The protected method for getting envelope of triangle
    * 
    * @return envelope of triangle
    */
   protected Envelope getEnvelope() {
      final Coordinate[] newPoint = new Coordinate[4];
      newPoint[0] = b003;
      newPoint[1] = b030;
      newPoint[2] = b300;
      newPoint[3] = b003;
      final CoordinateArraySequence newPointsTriangle = new CoordinateArraySequence(newPoint);

      final LinearRing trianglesPoints = new LinearRing(newPointsTriangle, new GeometryFactory());

      return trianglesPoints.getEnvelopeInternal();
   }


   /******************************************************************************************************************************
    * The protected method gets normal into Bezier triangle with barycentric coordinate
    * 
    * @param u -
    *                barycentric coordinate u
    * @param v -
    *                barycentric coordinate u
    * @return normal of surface
    */
   protected Bezier getBezierPatch(final int index) {
      switch (index) {
         case 0: {
            return new Bezier(b300, b030, G, b210, b120, A021, A012, A102, A201, A111);
         }
         case 1: {
            return new Bezier(b030, b003, G, b021, b012, B102, B201, A012, A021, B111);
         }
         case 2: {
            return new Bezier(b003, b300, G, b102, b201, A201, A102, B201, B102, C111);
         }
      }
      return null;
   }
}
