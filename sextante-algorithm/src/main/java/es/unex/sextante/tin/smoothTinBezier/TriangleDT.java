/**
 *    @author      	Josef Bezdek, ZCU Plzen
 *	  @version     	1.0
 *    @since 		JDK1.5
 */


package es.unex.sextante.tin.smoothTinBezier;

import java.awt.geom.GeneralPath;
import java.io.Serializable;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

public class TriangleDT
         implements
            Serializable {
   public Coordinate A;
   public Coordinate B;
   public Coordinate C;
   public boolean    haveBreakLine = false;
   public int        typeBreakLine = -1;


   /******************************************************************************************************************************
    * Constructor
    * 
    * @param T -
    *                triangle will be cloned
    * 
    */
   public TriangleDT(final TriangleDT T) {
      A = T.A;
      B = T.B;
      C = T.C;
   }


   /******************************************************************************************************************************
    * Constructor
    * 
    * @param A -
    *                first vertex
    * @param B -
    *                second vertex
    * @param C -
    *                third vertex
    * 
    */
   public TriangleDT(final Coordinate A,
                     final Coordinate B,
                     final Coordinate C) {
      this.A = A;
      this.B = B;
      this.C = C;
   }


   public TriangleDT(final Coordinate[] coords) {
      this.A = new Coordinate(coords[0].x, coords[0].y, coords[0].z);
      this.B = new Coordinate(coords[1].x, coords[1].y, coords[1].z);
      this.C = new Coordinate(coords[2].x, coords[2].y, coords[2].z);
   }


   /******************************************************************************************************************************
    * implicit Constructor
    */
   public TriangleDT() {}


   /******************************************************************************************************************************
    * The method which testing, if the line intersect the triangle
    * 
    * @param newL -
    *                Geometry of line
    * 
    * @return boolean true - line intersect triangle false - line doesn't intersect triangle
    */
   public boolean containsLine(final LineString newL) {
      final Coordinate[] newPoints = { A, B, C, A };
      final CoordinateArraySequence newPointsTriangle = new CoordinateArraySequence(newPoints);
      final LinearRing trianglesPoints = new LinearRing(newPointsTriangle, new GeometryFactory());
      return newL.crosses(trianglesPoints.convexHull());
   }


   protected Coordinate getCentroid() {
      return new Coordinate((A.x + B.x + C.x) / 3, (A.y + B.y + C.y) / 3);
   }


   /******************************************************************************************************************************
    * The method which testing, if the triangle contains the point
    * 
    * @param P -
    *                point which will be tested
    * 
    * @return boolean true - the triangle contains the point false - the triangle doesn't contains point
    * 
    */
   public boolean contains(final Coordinate P) {
      final GeneralPath triangle = new GeneralPath();
      triangle.moveTo((float) A.x, (float) A.y);
      triangle.lineTo((float) B.x, (float) B.y);
      triangle.lineTo((float) C.x, (float) C.y);
      triangle.lineTo((float) A.x, (float) A.y);
      triangle.closePath();
      return triangle.contains(P.x, P.y);
   }


   /******************************************************************************************************************************
    * The method which testing, if the triangle contains the point
    * 
    * @param P -
    *                point which will be tested
    * 
    * @return boolean true - the triangle contains the point false - the triangle doesn't contains point
    * 
    */
   public boolean containsPointAsVertex(final Coordinate P) {
      if (A.equals2D(P) || B.equals2D(P) || C.equals2D(P)) {
         return true;
      }
      else {
         return false;
      }
   }


   /******************************************************************************************************************************
    * The method which testing two triangles, if the triangles have one same point
    * 
    * @param T -
    *                triangle to test
    * 
    * @return boolean true - the triangles have one same point false - the triangles haven't one same point
    * 
    */
   protected boolean containsOneSamePointWith(final TriangleDT T) {
      if (T.A.equals2D(A) || T.A.equals2D(B) || T.A.equals2D(C)) {
         return true;
      }
      if (T.B.equals2D(A) || T.B.equals2D(B) || T.B.equals2D(C)) {
         return true;
      }
      if (T.C.equals2D(A) || T.C.equals2D(B) || T.C.equals2D(C)) {
         return true;
      }
      else {
         return false;
      }
   }


   /******************************************************************************************************************************
    * The method which testing two triangles, if the triangles have two same points
    * 
    * @param P1 -
    *                point for testing
    * @param P2 -
    *                point for testing
    * @return boolean true - the triangles have two same point false - the triangles haven't two same point
    * 
    */
   public boolean containsTwoPoints(final Coordinate P1,
                                    final Coordinate P2) {
      if ((A.equals2D(P1) || B.equals2D(P1) || C.equals2D(P1)) && (A.equals2D(P2) || B.equals2D(P2) || C.equals2D(P2))) {
         return true;
      }
      return false;
   }


   /******************************************************************************************************************************
    * The method for converting to String
    * 
    * 
    */
   /*	public void toStringa() {
   		//return ("TriangleDT: " + A.toString() + B.toString() + C.toString()+" KEY "+key[0]+" "+key[1]+"  neighbour>"+neighbour_idx[0][0]+ " ");
   		System.out.println("--------------------------------------------------------------------");
   		  System.out.println("TDT: " + A.toString() + B.toString() + C.toString());
   		  System.out.println(" k:"+typeBreakLine);
   		System.out.println("--------------------------------------------------------------------");
   	}*/

   /******************************************************************************************************************************
    * The method which testing triangle, if the triangles is'nt line
    * 
    * @return boolean true - the triangle is triangle false - the triangle is line
    * 
    */
   protected boolean isTriangle() {
      final Coordinate[] newPoint = new Coordinate[4];
      newPoint[0] = A;
      newPoint[1] = B;
      newPoint[2] = C;
      newPoint[3] = A;
      final CoordinateArraySequence newPointsTriangle = new CoordinateArraySequence(newPoint);
      final LinearRing trianglesPoints = new LinearRing(newPointsTriangle, new GeometryFactory());
      if (trianglesPoints.convexHull().getGeometryType() == "Polygon") {
         return true;
      }
      else {
         return false;
      }
   }


   /******************************************************************************************************************************
    * The method which comparing two triangles, if the triangles have same coordinates of vertexes
    * 
    * @param T -
    *                triangle to test
    * 
    * @return boolean true - the triangles are same false - the triangles aren't same
    * 
    */
   public boolean compare(final TriangleDT T) {
      if ((T.A.equals2D(A) || T.A.equals2D(B) || T.A.equals2D(C)) && (T.B.equals2D(A) || T.B.equals2D(B) || T.B.equals2D(C))
          && (T.C.equals2D(A) || T.C.equals2D(B) || T.C.equals2D(C))) {

         return true;
      }

      return false;
   }


   /******************************************************************************************************************************
    * The method which compare points
    * 
    * @param P -
    *                points for comparing
    * @return index A,B,C which point is same or N if point P not exist in triangle
    */
   public char compareReturnIndex(final Coordinate P) {
      if (P.equals2D(A)) {
         return 'A';
      }
      if (P.equals2D(B)) {
         return 'B';
      }
      if (P.equals2D(C)) {
         return 'C';
      }
      return 'N';

   }


   /******************************************************************************************************************************
    * Protected method for getting envelope of triangle
    * 
    * @return envelope of triangle
    */
   public Envelope getEnvelope() {
      final Coordinate[] newPoint = new Coordinate[4];
      newPoint[0] = A;
      newPoint[1] = B;
      newPoint[2] = C;
      newPoint[3] = A;
      final CoordinateArraySequence newPointsTriangle = new CoordinateArraySequence(newPoint);

      final LinearRing trianglesPoints = new LinearRing(newPointsTriangle, new GeometryFactory());

      return trianglesPoints.getEnvelopeInternal();
   }


   public void normalizePolygon() {
      Coordinate[] coords = new Coordinate[4];
      final GeometryFactory gf = new GeometryFactory();
      coords[0] = A;
      coords[1] = B;
      coords[2] = C;
      coords[3] = A;
      final LinearRing ring = gf.createLinearRing(coords);
      final Polygon poly = gf.createPolygon(ring, null);
      poly.normalize();
      coords = poly.getCoordinates();
      A = coords[0];
      B = coords[1];
      C = coords[2];
   }

}
