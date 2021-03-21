package es.unex.sextante.vectorTools.splitLinesWithPoints;

public class PtAndAttributes {

   public Object[] attrs;
   public double   x;
   public double   y;
   public int      id;


   public PtAndAttributes(final double x,
                          final double y,
                          final int id,
                          final Object[] attrs) {

      this.x = x;
      this.y = y;
      this.id = id;
      this.attrs = attrs;

   }

}
