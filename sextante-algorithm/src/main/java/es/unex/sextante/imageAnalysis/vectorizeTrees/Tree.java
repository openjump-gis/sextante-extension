package es.unex.sextante.imageAnalysis.vectorizeTrees;

import java.awt.geom.Point2D;

public class Tree {

   Point2D center;
   double  dRadius;
   double  dArea;
   double  dPerimeter;


   public double getAreaPerimeterRatio() {

      return dArea / dPerimeter;
   }


   public double getRadius() {

      return Math.sqrt(dArea / Math.PI);

   }

}
