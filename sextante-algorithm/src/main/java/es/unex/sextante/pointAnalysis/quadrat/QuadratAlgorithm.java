

package es.unex.sextante.pointAnalysis.quadrat;

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
import es.unex.sextante.math.pdf.PDF;


public class QuadratAlgorithm
         extends
            GeoAlgorithm {

   private static final String POINTS = "POINTS";
   private static final String RESULT = "RESULT";
   private IVectorLayer        m_Layer;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Quadrant_analysis"));
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


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i, j;
      int iRow, iCol;
      int iRows, iCols;
      int iQuadratCount[][];
      double x, y;
      double dArea;
      double dWidth;
      double dXMin, dYMin;
      int iCount;

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      if (!m_bIsAutoExtent) {
         m_Layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      iCount = m_Layer.getShapesCount();
      if (iCount == 0) {
         throw new GeoAlgorithmExecutionException("0 points in layer");
      }
      dArea = m_Layer.getFullExtent().getHeight() * m_Layer.getFullExtent().getWidth();
      dXMin = m_Layer.getFullExtent().getMinX();
      dYMin = m_Layer.getFullExtent().getMinY();
      dWidth = Math.sqrt(dArea * 2 / iCount);
      iCols = (int) Math.ceil(m_Layer.getFullExtent().getWidth() / dWidth);
      iRows = (int) Math.ceil(m_Layer.getFullExtent().getHeight() / dWidth);
      iQuadratCount = new int[iRows][iCols];
      setProgressText(Sextante.getText("Creating_quadrants"));
      i = 0;
      final IFeatureIterator iter = m_Layer.iterator();
      while (iter.hasNext() && setProgress(i, iCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate coord = geom.getCoordinate();
         x = coord.x;
         y = coord.y;
         iCol = (int) Math.floor((x - dXMin) / dWidth);
         iRow = (int) Math.floor((y - dYMin) / dWidth);
         iQuadratCount[iRow][iCol]++;
         i++;
      }
      iter.close();

      final int iQuadratsVector[] = new int[iRows * iCols];
      for (i = 0; i < iRows; i++) {
         for (j = 0; j < iCols; j++) {
            iQuadratsVector[j + i * iCols] = iQuadratCount[i][j];
         }
      }

      setProgressText(Sextante.getText("Analyzing_quadrants"));
      calculateStats(iQuadratsVector);


      return !m_Task.isCanceled();
   }


   private void calculateStats(final int[] quadrats) {

      int i;
      int iMax = 0;
      double dMean = 0;
      double dVar = 0;
      double dClustering;

      for (i = 0; i < quadrats.length; i++) {
         dMean += quadrats[i];
         dVar += (quadrats[i] * quadrats[i]);
         iMax = Math.max(iMax, quadrats[i]);
      }

      dMean /= quadrats.length;
      dVar = dVar / quadrats.length - dMean * dMean;
      dClustering = dVar / dMean;

      final int quadratsFreq[] = new int[iMax + 1];
      for (i = 0; i < quadrats.length; i++) {
         quadratsFreq[quadrats[i]]++;
      }

      double dObsProb = 0;
      double dPoissonProb = 0;
      double dMaxDiff = 0, dDiff = 0;
      for (i = 0; i < quadratsFreq.length; i++) {
         dObsProb += ((double) quadratsFreq[i] / (double) quadrats.length);
         dPoissonProb += PDF.poisson(i, dMean);
         dDiff = Math.abs(dObsProb - dPoissonProb);
         dMaxDiff = Math.max(dMaxDiff, dDiff);
      }

      final double dKS = 1.36 / Math.sqrt(quadrats.length);

      final DecimalFormat df = new DecimalFormat("##.######");
      final HTMLDoc doc = new HTMLDoc();
      doc.open(Sextante.getText("Quadrant_analysis"));
      doc.addHeader(Sextante.getText("Quadrant_analysis"), 1);
      doc.startUnorderedList();
      doc.addListElement(Sextante.getText("Mean") + ": " + df.format(dMean));
      doc.addListElement(Sextante.getText("Variance") + ": " + df.format(dVar));
      doc.addListElement(Sextante.getText("Coefficient_of_variation_[~0") + ": " + df.format(dClustering));
      doc.addListElement(Sextante.getText("D_value__Kolmogorov-Smirnov") + ": " + df.format(dMaxDiff));
      doc.addListElement(Sextante.getText("Critical_value__at_5%") + ": " + df.format(dKS));
      doc.closeUnorderedList();
      doc.close();
      addOutputText(RESULT, Sextante.getText("Statistics") + "[" + m_Layer.getName() + "]", doc.getHTMLCode());

   }


}
