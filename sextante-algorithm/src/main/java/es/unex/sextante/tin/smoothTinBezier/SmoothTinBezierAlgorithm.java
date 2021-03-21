/**
 *    @author      	Josef Bezdek, ZCU Plzen
 *	  @version     	1.0
 *    @since 		JDK1.5
 */


package es.unex.sextante.tin.smoothTinBezier;

import java.util.TreeMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.index.strtree.STRtree;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRecord;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class SmoothTinBezierAlgorithm
         extends
            GeoAlgorithm {

   public static final String TIN        = "TIN";
   public static final String TINB       = "TINB";
   public static final String LoD        = "LoD";
   public static final String Smooth     = "Smooth";

   private IVectorLayer       m_Triangles;
   private IVectorLayer       m_TrianglesOut;
   private int                m_LoD;
   private double             m_Smooth;

   private STRtree            trianglesIndex;
   Coordinate[][]             triangles;
   TreeMap                    breakLines = new TreeMap();
   Bezier                     miniBezierTriangles[];


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Bezier_surface"));
      setGroup(Sextante.getText("TIN"));
      setUserCanDefineAnalysisExtent(false);
      final String[] sDistance = { "1", "2", "3", "4", "5", "6", "7", "8", "9" };

      try {
         m_Parameters.addInputVectorLayer(TIN, Sextante.getText("TIN"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);

         m_Parameters.addSelection(LoD, Sextante.getText("Level_of_detail"), sDistance);
         m_Parameters.addNumericalValue(Smooth, Sextante.getText("Smoothing_coef"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, 0.1, 1);

         addOutputVectorLayer(TINB, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;
      double scaleZ = Double.NEGATIVE_INFINITY;


      m_Triangles = m_Parameters.getParameterValueAsVectorLayer(TIN);
      m_LoD = m_Parameters.getParameterValueAsInt(LoD);
      m_Smooth = m_Parameters.getParameterValueAsDouble(Smooth);

      final Class types[] = { Integer.class, String.class, Integer.class };
      final String sNames[] = { "ID", "HardLines", "type" };
      m_TrianglesOut = getNewVectorLayer(TINB, m_Triangles.getName() + "_bezier", IVectorLayer.SHAPE_TYPE_POLYGON, types, sNames);


      i = 0;
      iShapeCount = m_Triangles.getShapesCount();
      triangles = new Coordinate[iShapeCount][3];
      IFeatureIterator iter = m_Triangles.iterator();
      try {
         //			dd.addField(Integer.class);
         //			PageStore ps = new MemoryPageStore(dd);
         trianglesIndex = new STRtree();
         while (iter.hasNext()) {
            final IFeature feature = iter.next();
            //TODO: this is supposed to be TIN input, so we should check an throw an error if
            //TODO: we get a polygon which is not a triangle!

            final IRecord record = feature.getRecord();
            if (((String) record.getValue(1)) == "Y") {
               breakLines.put(i, record.getValue(2));
            }

            triangles[i][0] = (Coordinate) feature.getGeometry().getCoordinates()[0].clone();
            triangles[i][1] = (Coordinate) feature.getGeometry().getCoordinates()[1].clone();
            triangles[i][2] = (Coordinate) feature.getGeometry().getCoordinates()[2].clone();
            //TODO: add fake Z-coordinates (0.0) if this is a 2D input TIN

            //				data = new Data(dd);
            //				data.addValue(i);

            //trianglesIndex.insert(trianglePolygon.getEnvelopeInternal(), new Integer(i));
            trianglesIndex.insert(feature.getGeometry().getEnvelopeInternal(), new Integer(i));

            /* DEBUG */
            System.err.println("TRIANGLE " + i + " = " + triangles[i][0] + ", " + triangles[i][1] + ", " + triangles[i][2]);

            for (int k = 0; k < 2; k++) {
               final double diffZ = triangles[i][k].z - triangles[i][k + 1].z;
               final double diffXY = Math.sqrt(Math.pow((triangles[i][k].x - triangles[i][k + 1].x), 2)
                                               + Math.pow((triangles[i][k].y - triangles[i][k + 1].y), 2));

               //TODO: if
               if (scaleZ < Math.abs(diffZ / diffXY)) {
                  scaleZ = Math.abs(diffZ / diffXY);
               }
            }
            setProgress(i, 2 * iShapeCount);
            i++;
         }
         iter.close();
      }
      catch (final Exception e) {
         e.printStackTrace();
      }

      // TODO: what happens if we have only fake Z coordinates (=0.0) ???
      // TODO: in that case, scaleZ*m_smooth (see below) will be = 0
      // TODO: would that be a problem?
      System.err.println("SCALEZ = " + scaleZ);

      m_Triangles = null;
      iter = null;
      final BezierSurface bezierSurface = new BezierSurface(triangles, trianglesIndex, breakLines, scaleZ * m_Smooth, m_LoD + 1);


      int indexOfInterpolatedTriangles = 0;
      while (bezierSurface.hasNext()) {
         setProgress(i++, 2 * iShapeCount);
         final Coordinate newTin[][] = bezierSurface.nextTrinagle();
         System.err.println("GOT " + newTin.length + " TRIANGLES");
         for (int l = 0; l < newTin.length; l++) {
            final Object[] record = { new Integer(indexOfInterpolatedTriangles), "", -1 };
            final GeometryFactory gf = new GeometryFactory();
            final Coordinate[] coords = new Coordinate[4];
            for (int m = 0; m < 3; m++) {
               coords[m] = newTin[l][m];
               if (coords[m] != null) {
                  /* DEBUG */
                  System.err.println(coords[m]);
               }

            }
            coords[3] = newTin[l][0];

            /* DEBUG */
            System.err.println(coords[3]);

            /* BUG */
            //TODO: for some reason, bezierSurface.nextTriangle() only writes invalid triangles into newTin[][]
            //TODO: this leads to gf.createLinearRing() falling over
            final LinearRing ring = gf.createLinearRing(coords);
            m_TrianglesOut.addFeature(gf.createPolygon(ring, null), record);
            indexOfInterpolatedTriangles++;
         }
      }
      return !m_Task.isCanceled();
   }

}
