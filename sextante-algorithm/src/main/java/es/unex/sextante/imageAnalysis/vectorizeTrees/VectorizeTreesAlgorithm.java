package es.unex.sextante.imageAnalysis.vectorizeTrees;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.math.simpleStats.SimpleStats;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.parameters.RasterLayerAndBand;
import es.unex.sextante.rasterWrappers.GridCell;

public class VectorizeTreesAlgorithm
         extends
            GeoAlgorithm {

   private final static int   m_iOffsetX[]       = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final static int   m_iOffsetY[]       = { 1, 1, 0, -1, -1, -1, 0, 1 };

   public static final String INPUT              = "INPUT";
   public static final String POLYGONS           = "POLYGONS";
   public static final String TOLERANCE_SPECTRAL = "TOLERANCE_SPECTRAL";
   public static final String TOLERANCE_SIZE     = "TOLERANCE_SIZE";
   public static final String MINSIZE            = "MINSIZE";
   public static final String MAXSIZE            = "MAXSIZE";
   public static final String TREES              = "TREES";

   private static final int   ANALIZED           = 2;

   private int                m_iMinX, m_iMinY;
   private IRasterLayer[]     m_Window;
   private IRasterLayer       m_BinaryImage;
   private ArrayList          m_Bands;
   private IVectorLayer       m_Polygons;
   private ArrayList          m_Stats;
   private SimpleStats        m_SizeStats;
   private IVectorLayer       m_Trees;
   private ArrayList          m_TreesArray;
   private double             m_dTolerance;
   private double             m_dToleranceSize;
   private double             m_dMinSize;
   private double             m_dMaxSize;
   private GeometryFactory    m_GeometryFactory;
   private int                m_iBand[];


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Detect_and_vectorize_individual_trees"));
      setGroup(Sextante.getText("Image_processing"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addMultipleInput(INPUT, Sextante.getText("Bands"), AdditionalInfoMultipleInput.DATA_TYPE_BAND, true);
         m_Parameters.addInputVectorLayer(POLYGONS, Sextante.getText("Sample_trees"),
                  AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         m_Parameters.addNumericalValue(TOLERANCE_SPECTRAL, Sextante.getText("Tolerance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, 0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(TOLERANCE_SIZE, Sextante.getText("Tolerance_[size]"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, 0, Double.MAX_VALUE);
         //			m_Parameters.addNumericalValue(MINSIZE,
         //				    						Sextante.getText( "Tamano_min"),
         //				    						AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE,
         //				    						0,0, Double.MAX_VALUE);
         //			m_Parameters.addNumericalValue(MAXSIZE,
         //											Sextante.getText( "Tamano_max"),
         //											AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE,
         //											100,0, Double.MAX_VALUE);
         addOutputVectorLayer(TREES, Sextante.getText("Trees"), OutputVectorLayer.SHAPE_TYPE_POINT);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;

      m_GeometryFactory = new GeometryFactory();

      m_Bands = m_Parameters.getParameterValueAsArrayList(INPUT);
      m_Polygons = m_Parameters.getParameterValueAsVectorLayer(POLYGONS);
      m_dTolerance = m_Parameters.getParameterValueAsDouble(TOLERANCE_SPECTRAL);
      m_dToleranceSize = m_Parameters.getParameterValueAsDouble(TOLERANCE_SIZE);
      //		m_dMinSize = m_Parameters.getParameterValueAsDouble(MINSIZE);
      //		m_dMaxSize = m_Parameters.getParameterValueAsDouble(MAXSIZE);

      m_TreesArray = new ArrayList();

      m_Trees = getNewVectorLayer(TREES, Sextante.getText("Trees"), IVectorLayer.SHAPE_TYPE_POINT, new Class[] { Integer.class,
               Double.class, Double.class },
               new String[] { "ID", Sextante.getText("Superficie"), Sextante.getText("coef_forma") });

      m_SizeStats = new SimpleStats();

      getClassInformation();

      m_BinaryImage = this.getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_BYTE, m_AnalysisExtent, 1);
      m_BinaryImage.setNoDataValue(0.);

      for (i = 0; i < m_Window.length; i++) {
         m_Window[i].setWindowExtent(m_AnalysisExtent);
      }

      doParalellpiped();
      detectAndVectorizeTrees();

      //addOutputObject("out", "out", null, m_BinaryImage);

      return !m_Task.isCanceled();


   }


   private void detectAndVectorizeTrees() {

      int x, y;
      int iNX, iNY;
      double dValue;

      iNX = m_BinaryImage.getWindowGridExtent().getNX();
      iNY = m_BinaryImage.getWindowGridExtent().getNY();

      setProgressText(Sextante.getText("Vectorizing"));
      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dValue = m_BinaryImage.getCellValueAsDouble(x, y);
            if (!m_BinaryImage.isNoDataValue(dValue) && (dValue != ANALIZED)) {
               detectTree(x, y);
            }
         }
      }

      vectorize();

   }


   private void detectTree(int x,
                           int y) {

      int x2, y2;
      int iPt;
      int n;
      int iCells = 0;
      int xMax = Integer.MIN_VALUE;
      int yMax = Integer.MIN_VALUE;
      int xMin = Integer.MAX_VALUE;
      int yMin = Integer.MAX_VALUE;
      double xCenter, yCenter;
      double dValue;
      ArrayList centralPoints = new ArrayList();
      ArrayList adjPoints = new ArrayList();
      Point point;

      xMin = Math.min(xMin, x);
      yMin = Math.min(yMin, y);
      xMax = Math.max(xMax, x);
      yMax = Math.max(yMax, y);
      iCells++;

      centralPoints.add(new Point(x, y));
      //m_BinaryImage.setNoData(x,y);
      m_BinaryImage.setCellValue(x, y, ANALIZED);
      while (centralPoints.size() != 0) {
         for (iPt = 0; iPt < centralPoints.size(); iPt++) {
            point = (Point) centralPoints.get(iPt);
            x = point.x;
            y = point.y;
            for (n = 0; n < 8; n++) {
               x2 = x + m_iOffsetX[n];
               y2 = y + m_iOffsetY[n];
               dValue = m_BinaryImage.getCellValueAsDouble(x2, y2);
               if (!m_BinaryImage.isNoDataValue(dValue) && (dValue != ANALIZED)) {
                  //m_BinaryImage.setNoData(x2,y2);
                  m_BinaryImage.setCellValue(x2, y2, ANALIZED);
                  adjPoints.add(new Point(x2, y2));
                  xMin = Math.min(xMin, x2);
                  yMin = Math.min(yMin, y2);
                  xMax = Math.max(xMax, x2);
                  yMax = Math.max(yMax, y2);
                  iCells++;
               }
            }
         }

         centralPoints = adjPoints;
         adjPoints = new ArrayList();

         if (m_Task.isCanceled()) {
            return;
         }

      }

      if (Math.abs(iCells - m_SizeStats.getMean()) < m_SizeStats.getStdDev() * m_dToleranceSize) {
         final Tree tree = new Tree();
         final GridCell cellMin = new GridCell(xMin, yMin, 0);
         final GridCell cellMax = new GridCell(xMax, yMax, 0);
         final Point2D ptMin = m_AnalysisExtent.getWorldCoordsFromGridCoords(cellMin);
         final Point2D ptMax = m_AnalysisExtent.getWorldCoordsFromGridCoords(cellMax);
         xCenter = (ptMax.getX() + ptMin.getX()) / 2.;
         yCenter = (ptMax.getY() + ptMin.getY()) / 2.;

         tree.center = new Point2D.Double(xCenter, yCenter);
         tree.dArea = iCells;
         tree.dPerimeter = ((xMax - xMin + 1) * (yMax - yMin + 1));
         m_TreesArray.add(tree);
      }

   }


   private void vectorize() {

      int iTree = 1;
      Tree tree;

      for (int i = 0; i < m_TreesArray.size(); i++) {
         tree = (Tree) m_TreesArray.get(i);
         final Object[] value = new Object[3];
         value[0] = new Integer(iTree);
         value[1] = new Double(tree.dArea);
         value[2] = new Double(tree.getAreaPerimeterRatio());
         final org.locationtech.jts.geom.Point pt = m_GeometryFactory.createPoint(new Coordinate(tree.center.getX(),
                  tree.center.getY()));
         m_Trees.addFeature(pt, value);
         iTree++;
      }

   }


   private void getClassInformation() {

      int i;
      final RasterLayerAndBand band;
      m_Stats = new ArrayList();

      m_Window = new IRasterLayer[m_Bands.size()];
      m_iBand = new int[m_Bands.size()];
      for (i = 0; i < m_Bands.size(); i++) {
         final RasterLayerAndBand rab = (RasterLayerAndBand) m_Bands.get(i);
         m_iBand[i] = rab.getBand();
         m_Window[i] = rab.getRasterLayer();
         final AnalysisExtent extent = getAdjustedGridExtent(i);
         m_Stats.add(new SimpleStats());
         m_Window[i].setWindowExtent(extent);
      }

      setProgressText(Sextante.getText("Calculating_spectral_signatures"));
      i = 0;
      final int iShapeCount = m_Polygons.getShapesCount();
      final IFeatureIterator featureIter = m_Polygons.iterator();
      while (featureIter.hasNext() && setProgress(i, iShapeCount)) {
         IFeature feature;
         try {
            feature = featureIter.next();
            final Geometry geometry = feature.getGeometry();
            doPolygon(geometry);
         }
         catch (final IteratorException e) {
            //we ignore wrong features
         }

      }
      featureIter.close();

   }


   private void doPolygon(final Geometry geom) {

      for (int i = 0; i < geom.getNumGeometries(); i++) {
         final Geometry part = geom.getGeometryN(i);
         doPolygonPart(part);
      }

   }


   private void doPolygonPart(final Geometry geom) {

      boolean bFill;
      boolean bCrossing[];
      int iNX, iNY;
      int i;
      int x, y, ix, xStart, xStop;
      int iSize = 0;
      int iPoint;
      double yPos;
      double dValue;
      AnalysisExtent ge;
      SimpleStats substats;
      Coordinate pLeft, pRight, pa, pb, p;

      final Envelope extent = geom.getEnvelopeInternal();

      final Coordinate[] points = geom.getCoordinates();

      for (i = 0; i < m_Window.length; i++) {
         p = new Coordinate();
         substats = (SimpleStats) m_Stats.get(i);
         ge = m_Window[i].getWindowGridExtent();
         iNX = ge.getNX();
         iNY = ge.getNY();
         bCrossing = new boolean[iNX];

         xStart = (int) ((extent.getMinX() - ge.getXMin()) / ge.getCellSize()) - 1;
         if (xStart < 0) {
            xStart = 0;
         }

         xStop = (int) ((extent.getMaxX() - ge.getXMin()) / ge.getCellSize()) + 1;
         if (xStop >= iNX) {
            xStop = iNX - 1;
         }

         for (y = 0, yPos = ge.getYMax(); y < iNY; y++, yPos -= ge.getCellSize()) {
            if ((yPos >= extent.getMinY()) && (yPos <= extent.getMaxY())) {
               Arrays.fill(bCrossing, false);
               pLeft = new Coordinate(ge.getXMin() - 1.0, yPos);
               pRight = new Coordinate(ge.getXMax() + 1.0, yPos);

               pb = points[points.length - 1];

               for (iPoint = 0; iPoint < points.length; iPoint++) {
                  pa = pb;
                  pb = points[iPoint];

                  if ((((pa.y <= yPos) && (yPos < pb.y)) || ((pa.y > yPos) && (yPos >= pb.y)))) {
                     getCrossing(p, pa, pb, pLeft, pRight);

                     ix = (int) ((p.x - ge.getXMin()) / ge.getCellSize() + 1.0);

                     if (ix < 0) {
                        ix = 0;
                     }
                     else if (ix >= iNX) {
                        ix = iNX - 1;
                     }

                     bCrossing[ix] = !bCrossing[ix];
                  }
               }

               for (x = xStart, bFill = false; x <= xStop; x++) {
                  if (bCrossing[x]) {
                     bFill = !bFill;
                  }
                  if (bFill) {
                     iSize++;
                     dValue = m_Window[i].getCellValueAsDouble(x /*+ m_iMinX - 1*/, y /*+ m_iMinY - 1*/, m_iBand[i]);
                     if (!m_Window[i].isNoDataValue(dValue)) {
                        substats.addValue(dValue);
                     }
                  }
               }
            }
         }

         if (i == 0) {
            m_SizeStats.addValue(iSize);
         }
      }


   }


   private boolean getCrossing(final Coordinate crossing,
                               final Coordinate a1,
                               final Coordinate a2,
                               final Coordinate b1,
                               final Coordinate b2) {

      double lambda, div, a_dx, a_dy, b_dx, b_dy;

      a_dx = a2.x - a1.x;
      a_dy = a2.y - a1.y;

      b_dx = b2.x - b1.x;
      b_dy = b2.y - b1.y;

      if ((div = a_dx * b_dy - b_dx * a_dy) != 0.0) {
         lambda = ((b1.x - a1.x) * b_dy - b_dx * (b1.y - a1.y)) / div;

         crossing.x = a1.x + lambda * a_dx;
         crossing.y = a1.y + lambda * a_dy;

         return true;

      }

      return false;

   }


   private AnalysisExtent getAdjustedGridExtent(final int iLayer) {

      double iMaxX, iMaxY;
      double dMinX, dMinY;
      double dMinX2, dMinY2, dMaxX2, dMaxY2;
      double dCellSize;
      final AnalysisExtent ge = new AnalysisExtent();

      final Rectangle2D rect = m_Polygons.getFullExtent();
      dMinX = m_Window[iLayer].getLayerGridExtent().getXMin();
      dMinY = m_Window[iLayer].getLayerGridExtent().getYMin();
      dCellSize = m_Window[iLayer].getLayerGridExtent().getCellSize();

      m_iMinX = (int) Math.floor((rect.getMinX() - dMinX) / dCellSize);
      iMaxX = Math.ceil((rect.getMaxX() - dMinX) / dCellSize);
      m_iMinY = (int) Math.floor((rect.getMinY() - dMinY) / dCellSize);
      iMaxY = Math.ceil((rect.getMaxY() - dMinY) / dCellSize);

      dMinX2 = dMinX + m_iMinX * dCellSize;
      dMinY2 = dMinY + m_iMinY * dCellSize;
      dMaxX2 = dMinX + iMaxX * dCellSize;
      dMaxY2 = dMinY + iMaxY * dCellSize;

      ge.setCellSize(dCellSize);
      ge.setXRange(dMinX2, dMaxX2, true);
      ge.setYRange(dMinY2, dMaxY2, true);

      return ge;

   }


   private boolean doParalellpiped() {

      boolean bIsInParalellpiped;
      int iNX, iNY;
      int x, y;
      int iGrid;
      final double dMean[] = new double[m_Window.length];
      final double dStdDev[] = new double[m_Window.length];
      double dValue;
      SimpleStats substats;

      iNX = m_BinaryImage.getWindowGridExtent().getNX();
      iNY = m_BinaryImage.getWindowGridExtent().getNY();

      for (iGrid = 0; iGrid < m_Window.length; iGrid++) {
         substats = ((SimpleStats) m_Stats.get(iGrid));
         dMean[iGrid] = substats.getMean();
         dStdDev[iGrid] = Math.sqrt(substats.getVariance());
      }

      setProgressText(Sextante.getText("Classifying"));
      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            bIsInParalellpiped = true;
            for (iGrid = 0; iGrid < m_Window.length; iGrid++) {
               dValue = m_Window[iGrid].getCellValueAsDouble(x, y, m_iBand[iGrid]);
               if (!m_Window[iGrid].isNoDataValue(dValue)) {
                  if (Math.abs(dValue - dMean[iGrid]) > dStdDev[iGrid] * m_dTolerance) {
                     bIsInParalellpiped = false;
                     break;
                  }
               }
               else {
                  bIsInParalellpiped = false;
                  break;
               }
            }

            if (bIsInParalellpiped) {
               m_BinaryImage.setCellValue(x, y, 1);
            }
            else {
               m_BinaryImage.setNoData(x, y);
            }
         }
      }

      return true;

   }

}
