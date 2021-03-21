/**
 * @author Josef Bezdek, ZCU Plzen
 * @version 1.0
 * @since JDK1.5
 */


//TODO: This currently returns an empty result.
//TODO: Before extracting the isolines, it attempts a Bezier interpolation.
//TODO: So I assume the problem(s) here to be identical with smoothTinBezier/SmoothTinBezierAlgorithm.java.
//TODO: If we fix the former, the same fixes should be applied here!
package es.unex.sextante.tin.linearIsolinesFromTin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
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
import es.unex.sextante.tin.smoothTinBezier.Bezier;
import es.unex.sextante.tin.smoothTinBezier.BezierSurface;

public class LinearIsolinesFromTinAlgorithm
         extends
            GeoAlgorithm {

   public static final String TIN          = "TIN";
   public static final String ISOLINES     = "ISOLINES";
   public static final String EQUIDISTANCE = "EQUIDISTANCE";
   public static final String LoD          = "LoD";
   public static final String ClusterTol   = "ClusterTol";
   public static final String Smooth       = "Smooth";

   private IVectorLayer       m_Triangles;
   private IVectorLayer       m_Isolines;
   private double             m_EquiDistance;
   private int                m_LoD;
   private double             m_ClusterTol;
   private double             m_Smooth;

   private STRtree            trianglesIndex;
   Coordinate[][]             triangles;
   TreeMap                    breakLines   = new TreeMap();
   Bezier                     miniBezierTriangles[];
   double                     scaleZ;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Extract_isolines"));
      setGroup(Sextante.getText("TIN"));
      setUserCanDefineAnalysisExtent(false);
      final String[] sDistance = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };

      try {
         m_Parameters.addInputVectorLayer(TIN, Sextante.getText("TIN"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);

         m_Parameters.addNumericalValue(EQUIDISTANCE, Sextante.getText("Equidistance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 10, 0, Double.MAX_VALUE);

         m_Parameters.addSelection(LoD, Sextante.getText("Level_of_detail"), sDistance);

         m_Parameters.addNumericalValue(ClusterTol, Sextante.getText("Cluster_tolerance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 0.001, 0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(Smooth, Sextante.getText("Smoothing_coef"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, 0.1, 1);


         addOutputVectorLayer(ISOLINES, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iShapeCount;
      final double maxZValue = Double.NEGATIVE_INFINITY;
      final double minZValue = Double.POSITIVE_INFINITY;

      m_Triangles = m_Parameters.getParameterValueAsVectorLayer(TIN);
      m_EquiDistance = m_Parameters.getParameterValueAsDouble(EQUIDISTANCE);
      m_LoD = m_Parameters.getParameterValueAsInt(LoD);
      m_ClusterTol = m_Parameters.getParameterValueAsDouble(ClusterTol);
      m_Smooth = m_Parameters.getParameterValueAsDouble(Smooth);

      final Class types[] = { Integer.class, Double.class };
      final String sNames[] = { "ID", "Value" };
      m_Isolines = getNewVectorLayer(ISOLINES, m_Triangles.getName() + "_Isolines", IVectorLayer.SHAPE_TYPE_LINE, types, sNames);

      i = 0;
      iShapeCount = m_Triangles.getShapesCount();
      triangles = new Coordinate[iShapeCount][3];
      IFeatureIterator iter = m_Triangles.iterator();
      try {
         //			dd.addField(Integer.class);
         //			PageStore ps = new MemoryPageStore(dd);
         trianglesIndex = new STRtree();
         while (iter.hasNext()) {
            //TODO: We assume the input to be a TIN. So if this feature != triangle, then we need to throw an error here
            final IFeature feature = iter.next();
            final IRecord record = feature.getRecord();
            if (((String) record.getValue(1)) == "Y") {
               breakLines.put(i, record.getValue(2));
            }

            triangles[i][0] = (Coordinate) feature.getGeometry().getCoordinates()[0].clone();
            triangles[i][1] = (Coordinate) feature.getGeometry().getCoordinates()[1].clone();
            triangles[i][2] = (Coordinate) feature.getGeometry().getCoordinates()[2].clone();

            //				data = new Data(dd);
            //				data.addValue(i);
            trianglesIndex.insert(feature.getGeometry().getEnvelopeInternal(), new Integer(i));

            for (int k = 0; k < 2; k++) {
               final double diffZ = triangles[i][k].z - triangles[i][k + 1].z;
               final double diffXY = Math.sqrt(Math.pow((triangles[i][k].x - triangles[i][k + 1].x), 2)
                                               + Math.pow((triangles[i][k].y - triangles[i][k + 1].y), 2));

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
      m_Triangles = null;
      iter = null;

      final LinearContourLines isoLineFactory = new LinearContourLines(m_EquiDistance, m_ClusterTol);


      if (m_LoD != 0) {
         final BezierSurface bezierSurface = new BezierSurface(triangles, trianglesIndex, breakLines, scaleZ * m_Smooth, m_LoD);
         while (bezierSurface.hasNext()) {
            setProgress(i++, 2 * iShapeCount);
            final Coordinate newTin[][] = bezierSurface.nextTrinagle();
            isoLineFactory.countIsolines(newTin);
         }
      }
      else {
         isoLineFactory.countIsolines(triangles);
      }

      final ArrayList isolines = isoLineFactory.getIsolines();
      final Iterator iterIso = isolines.iterator();
      int j = 0;
      for (int l = 0; l < isolines.size(); l++) {
         final Object o = isolines.get(l);
         if (o != null) {
            final GeometryFactory gf = new GeometryFactory();
            final Iterator isoL = ((LinkedList) o).iterator();
            final Coordinate[] coords = new Coordinate[((LinkedList) o).size()];
            int k = 0;
            while (isoL.hasNext()) {
               coords[k] = (Coordinate) isoL.next();
               k++;
            }
            final Object[] record = { new Integer(j), coords[0].z };
            final LineString isoline = gf.createLineString(coords);
            m_Isolines.addFeature(isoline, record);
            j++;

         }
      }

      return !m_Task.isCanceled();
   }
}
