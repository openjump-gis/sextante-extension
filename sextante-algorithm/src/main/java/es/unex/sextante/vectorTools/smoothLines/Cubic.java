/*********************************************************
 * Code adapted from Tim Lambert's Java Classes
 * http://www.cse.unsw.edu.au/~lambert/
 *********************************************************/
package es.unex.sextante.vectorTools.smoothLines;

/** this class represents a cubic polynomial */

public class Cubic {

   double a, b, c, d; /* a + b*u + c*u^2 +d*u^3 */


   public Cubic(final double a,
                final double b,
                final double c,
                final double d) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
   }


   /** evaluate cubic */
   public double eval(final double u) {
      return (((d * u) + c) * u + b) * u + a;
   }
}
