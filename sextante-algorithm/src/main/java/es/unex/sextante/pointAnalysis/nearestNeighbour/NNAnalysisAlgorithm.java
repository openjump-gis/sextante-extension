

package es.unex.sextante.pointAnalysis.nearestNeighbour;

import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public class NNAnalysisAlgorithm
         extends
            GeoAlgorithm {

   private static final String RESULT = "RESULT";
   private static final String POINTS = "POINTS";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i, j;
      double x1, x2, y1, y2;
      double dDifX, dDifY;
      double dArea;
      double dPerimeter;
      double dMin;
      double dAvg;
      double dExpectedAvg, dExpectedAvgC;
      double dVar, dVarC;
      double dNNI, dNNIC;
      double dZ, dZC;
      double dSum = 0;
      int iCount;

      final IVectorLayer layer = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      if (!m_bIsAutoExtent) {
         layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      final Rectangle2D rect = layer.getFullExtent();
      dArea = rect.getWidth() * rect.getHeight();
      dPerimeter = 2 * rect.getWidth() + 2 * rect.getHeight();
      iCount = layer.getShapesCount();
      i = 0;
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(i, iCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate coord = geom.getCoordinate();
         x1 = coord.x;
         y1 = coord.y;
         dMin = Double.MAX_VALUE;
         j = 0;
         final IFeatureIterator iter2 = layer.iterator();
         while (iter2.hasNext()) {
            final IFeature feature2 = iter2.next();
            if (i != j) {
               final Geometry geom2 = feature2.getGeometry();
               final Coordinate coord2 = geom2.getCoordinate();
               x2 = coord2.x;
               y2 = coord2.y;
               dDifX = x2 - x1;
               dDifY = y2 - y1;
               dMin = Math.min(dMin, Math.sqrt(dDifX * dDifX + dDifY * dDifY));
            }
            j++;
         }
         iter2.close();
         dSum += dMin;
         i++;
      }
      iter.close();

      dAvg = dSum / iCount;
      dExpectedAvg = 0.5 * Math.sqrt(dArea / iCount);
      dExpectedAvgC = 0.5 * Math.sqrt(dArea / iCount) + (0.0514 + 0.041 / Math.sqrt(iCount)) * dPerimeter / iCount;
      dVar = (1. / Math.PI - 0.25) * dArea / Math.pow(iCount, 2.);
      dVarC = 0.0703 * dArea / Math.pow(iCount, 2.) + 0.037 * dPerimeter * Math.sqrt(dArea / Math.pow(iCount, 5.));
      dNNI = dAvg / dExpectedAvg;
      dNNIC = dAvg / dExpectedAvgC;
      dZ = (dAvg - dExpectedAvg) / Math.sqrt(dVar);
      dZC = (dAvg - dExpectedAvgC) / Math.sqrt(dVarC);

      final DecimalFormat df = new DecimalFormat("##.###");
      final HTMLDoc doc = new HTMLDoc();
      doc.open(Sextante.getText("Nearest_neighbour"));
      doc.addHeader(Sextante.getText("Nearest_neighbour"), 2);
      doc.startUnorderedList();
      doc.addListElement(Sextante.getText("Mean_distance") + ": " + df.format(dAvg));
      doc.addListElement(Sextante.getText("Expected_distance") + ": " + df.format(dExpectedAvg));
      doc.addListElement(Sextante.getText("Edge-corrected_expected_distance") + ": " + df.format(dExpectedAvgC));
      doc.addListElement(Sextante.getText("Variance") + ": " + df.format(dVar));
      doc.addListElement(Sextante.getText("Edge-corrected_variance") + ": " + df.format(dVarC));
      doc.addListElement(Sextante.getText("Nearest_neighbour_index") + ": " + df.format(dNNI));
      doc.addListElement(Sextante.getText("Corrected_nearest_neighbour_index") + ": " + df.format(dNNIC));
      doc.addListElement(Sextante.getText("Z_value") + ": " + df.format(dZ));
      doc.addListElement(Sextante.getText("Corrected_Z_value") + ": " + df.format(dZC));
      doc.closeUnorderedList();
      doc.close();
      addOutputText(RESULT, Sextante.getText("Statistics") + "[" + layer.getName() + "]", doc.getHTMLCode());

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Nearest_neighbour_analysis"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
         addOutputText(RESULT, Sextante.getText("Statistics"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
