

package es.unex.sextante.pointAnalysis.ripleysK;

import java.awt.geom.Rectangle2D;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;


public class RipleysKAlgorithm
         extends
            GeoAlgorithm {

   private static final String POINTS     = "POINTS";
   private static final String RESULT     = "RESULT";

   private final int           CLASSES    = 30;

   private int                 m_iCount;
   private double              m_dMaxDist = 0;
   private double              m_dInterval;
   private double              m_dArea;
   private double              m_dK[];
   private IVectorLayer        m_Layer;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i, j;
      double x1, x2, y1, y2;
      double dDifX, dDifY;
      Rectangle2D rect;

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(POINTS);
      if (!m_bIsAutoExtent) {
         m_Layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      m_iCount = m_Layer.getShapesCount();
      if (m_iCount == 0) {
         throw new GeoAlgorithmExecutionException("0 points in layer");
      }
      rect = m_Layer.getFullExtent();
      m_dArea = rect.getHeight() * rect.getWidth();
      m_dMaxDist = Math.min(rect.getHeight(), rect.getWidth()) / 3;
      m_dInterval = m_dMaxDist / CLASSES;
      final double[][] d = new double[m_iCount][m_iCount];

      i = 0;
      final IFeatureIterator iter = m_Layer.iterator();
      while (iter.hasNext() && setProgress(i, m_iCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         final Coordinate coord = geom.getCoordinate();
         x1 = coord.x;
         y1 = coord.y;
         j = 0;
         final IFeatureIterator iter2 = m_Layer.iterator();
         while (iter2.hasNext()) {
            final IFeature feature2 = iter2.next();
            final Geometry geom2 = feature2.getGeometry();
            final Coordinate coord2 = geom2.getCoordinate();
            x2 = coord2.x;
            y2 = coord2.y;
            dDifX = x2 - x1;
            dDifY = y2 - y1;
            d[i][j] = Math.sqrt(dDifX * dDifX + dDifY * dDifY);
            j++;
         }
         i++;
      }
      iter.close();

      calculate(d);
      createTables();

      return !m_Task.isCanceled();

   }


   private void createTables() throws UnsupportedOutputChannelException {

      int i;
      double dKPoisson;
      double dL;
      final String[] sFields = { Sextante.getText("Distance"), Sextante.getText("K"), Sextante.getText("K_Poisson"),
               Sextante.getText("L") };
      final Class[] types = { Double.class, Double.class, Double.class, Double.class };
      final String sTableName = Sextante.getText("Ripley_K") + "[" + m_Layer.getName() + "]";
      final Object[] values = new Object[4];
      final ITable driver = getNewTable(RESULT, sTableName, types, sFields);

      for (i = 0; i < CLASSES; i++) {
         values[0] = new Double(m_dInterval * i);
         values[1] = new Double(m_dK[i]);
         dKPoisson = Math.PI * Math.pow(i * m_dInterval, 2.);
         values[2] = new Double(dKPoisson);
         dL = Math.sqrt(m_dK[i] / Math.PI) - (i * m_dInterval);
         values[3] = new Double(dL);
         driver.addRecord(values);
      }

   }


   private void calculate(final double[][] dist) {

      int i, j;
      int iCount;
      int iClass;
      double dDist;

      m_dK = new double[CLASSES];

      for (iClass = 1; iClass < CLASSES; iClass++) {
         dDist = iClass * m_dInterval;
         iCount = 0;
         for (i = 0; i < dist.length; i++) {
            for (j = 0; j < dist.length; j++) {
               if (dist[i][j] < dDist) {
                  iCount++;
               }
            }
            setProgress(i, dist.length);
         }
         m_dK[iClass] = m_dArea / (m_iCount * m_iCount) * iCount;
      }

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Ripley_K"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);
      try {
         m_Parameters.addInputVectorLayer(POINTS, Sextante.getText("Points"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
         addOutputTable(RESULT, Sextante.getText("Ripley_K"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
