package es.unex.sextante.topology.extractNodes;

import java.util.ArrayList;

import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.dataObjects.IFeature;

public class Node {

   private int             ID = 0;
   private final Geometry  geom;
   IFeature                lineFeature;
   private int             degree;
   //FIDs connectedLines
   private final ArrayList lines;
   private final ArrayList linesIdx;
   //IDs near endpoints
   private final ArrayList closeNodes;


   public Node(final int iD,
               final Geometry geom,
               final IFeature lineFeature) {
      ID = iD;
      this.geom = geom;
      this.degree = 1;
      this.lineFeature = lineFeature;
      lines = new ArrayList();
      linesIdx = new ArrayList();
      closeNodes = new ArrayList();
   }


   public void incrementDegree() {
      degree++;
   }


   public int getDegree() {
      return degree;
   }


   public Geometry getGeometry() {
      return geom;
   }


   public void addConnectedLine(final IFeature feat,
                                final int idx) {
      if (!lines.contains(feat)) {
         lines.add(feat);
      }
      if (!linesIdx.contains(idx)) {
         linesIdx.add(idx);
      }
   }


   public void processIfClose(final Node n,
                              final double tolerance) {

      if (closeNodes.contains(n) || n.getGeometry().equals(getGeometry())) {
         //System.out.println(n + " " + this + "   equals?" + (n.getGeometry().equals(getGeometry())));
         return;
      }
      final Geometry g = n.getGeometry();
      final double dist = geom.distance(g);
      if (dist < tolerance) {
         System.out.println("Added a node a " + dist);
         closeNodes.add(n);
      }

   }


   public ArrayList getCloseNodes() {
      return closeNodes;
   }


   public ArrayList getConnectedLines() {
      return lines;
   }


   public ArrayList getConnectedLinesIdx() {
      return linesIdx;
   }

}
