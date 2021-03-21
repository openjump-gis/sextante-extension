package es.unex.sextante.gridAnalysis.supervisedClassification;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.math.simpleStats.SimpleStats;
import es.unex.sextante.parameters.RasterLayerAndBand;

public class SupervisedClassificationAlgorithm
         extends
            GeoAlgorithm {

   public static final String INPUT                 = "INPUT";
   public static final String POLYGONS              = "POLYGONS";
   public static final String FIELD                 = "FIELD";
   public static final String METHOD                = "METHOD";
   public static final String CLASSIFICATION        = "CLASSIFICATION";
   public static final String CLASSES               = "CLASSES";

   public static final int    METHOD_PARALELLPIPED  = 0;
   public static final int    METHOD_MIN_DISTANCE   = 1;
   public static final int    METHOD_MAX_LIKELIHOOD = 2;

   private int                m_iMinX, m_iMinY;
   private int                m_iField;
   private IRasterLayer[]     m_Window;
   private IRasterLayer       m_Output;
   private ArrayList          m_Bands;
   private IVectorLayer       m_Polygons;
   private ITable             m_Table;
   private HashMap            m_Classes;
   private int[]              m_iBands;


   @Override
   public void defineCharacteristics() {

      final String sMethod[] = { Sextante.getText("Parallelepiped"), Sextante.getText("Minimum_distance"),
               Sextante.getText("Maximum_likelihood") };

      setName(Sextante.getText("Supervised_classification"));
      setGroup(Sextante.getText("Raster_layer_analysis"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addMultipleInput(INPUT, Sextante.getText("Bands"), AdditionalInfoMultipleInput.DATA_TYPE_BAND, true);
         m_Parameters.addInputVectorLayer(POLYGONS, Sextante.getText("Polygons"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON,
                  true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field_with_class_value"), "POLYGONS");
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         addOutputRasterLayer(CLASSIFICATION, Sextante.getText("Classification"));
         addOutputTable(CLASSES, Sextante.getText("Classes"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final OptionalParentParameterException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final UndefinedParentParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      AnalysisExtent ge;

      final int iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      m_iField = m_Parameters.getParameterValueAsInt(FIELD);
      m_Bands = m_Parameters.getParameterValueAsArrayList(INPUT);
      m_Polygons = m_Parameters.getParameterValueAsVectorLayer(POLYGONS);

      if (m_Bands.size() == 0) {
         throw new GeoAlgorithmExecutionException("At least one band is needed to run this algorithm");
      }

      m_Classes = new HashMap();

      getClassInformation();

      if (m_Task.isCanceled()) {
         return false;
      }

      m_Output = getNewRasterLayer(CLASSIFICATION, Sextante.getText("Classification"), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);
      m_Output.setNoDataValue(-1);
      ge = m_Output.getWindowGridExtent();

      for (i = 0; i < m_Window.length; i++) {
         m_Window[i].setWindowExtent(ge);
      }

      switch (iMethod) {
         case 0:
            doParalellpiped();
         case 1:
         default:
            doMinimumDistance();
         case 2:
            doMaximumLikelihood();
      }

      return !m_Task.isCanceled();

   }


   private void getClassInformation() throws UnsupportedOutputChannelException {

      int i, j;
      int iGrid;
      Object classID;
      Set set;
      Iterator iter;
      RasterLayerAndBand band;
      ArrayList stats;
      String sClass;
      final String sField[] = new String[1 + m_Bands.size() * 2];
      final Class types[] = new Class[1 + m_Bands.size() * 2];
      final Object values[] = new Object[m_Bands.size() * 2 + 1];
      SimpleStats substats;

      sField[0] = Sextante.getText("Name");
      types[0] = String.class;
      m_Window = new IRasterLayer[m_Bands.size()];
      for (i = 0; i < m_Bands.size(); i++) {
         band = (RasterLayerAndBand) m_Bands.get(i);
         m_iBands = new int[m_Bands.size()];
         m_iBands[i] = band.getBand();
         m_Window[i] = band.getRasterLayer();
         final AnalysisExtent extent = getAdjustedGridExtent(i);
         m_Window[i].setWindowExtent(extent);
         sField[i * 2 + 1] = band.getRasterLayer().getName() + "|" + Integer.toString(band.getBand());
         sField[i * 2 + 2] = band.getRasterLayer().getName() + "|" + Integer.toString(band.getBand());
         types[i * 2 + 1] = Double.class;
         types[i * 2 + 2] = Double.class;
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
            final Object[] record = feature.getRecord().getValues();
            sClass = record[m_iField].toString();
            if (m_Classes.containsKey(sClass)) {
               stats = (ArrayList) m_Classes.get(sClass);
            }
            else {
               stats = new ArrayList();
               for (j = 0; j < m_Bands.size(); j++) {
                  stats.add(new SimpleStats());
               }
               m_Classes.put(sClass, stats);
            }
            doPolygon(geometry, stats);
            i++;
         }
         catch (final IteratorException e) {
            //we ignore wrong features
         }
      }
      featureIter.close();

      if (m_Task.isCanceled()) {
         return;
      }

      m_Table = getNewTable(CLASSES, Sextante.getText("Classes"), types, sField);

      set = m_Classes.keySet();
      iter = set.iterator();

      while (iter.hasNext()) {
         classID = iter.next();
         stats = (ArrayList) m_Classes.get(classID);
         values[0] = new String(classID.toString());
         for (iGrid = 0; iGrid < m_Bands.size(); iGrid++) {
            substats = (SimpleStats) stats.get(iGrid);
            values[1 + iGrid * 2] = new Double(substats.getMean());
            values[2 + iGrid * 2] = new Double(substats.getStdDev());
         }
         m_Table.addRecord(values);
      }

   }


   private void doPolygon(final Geometry geom,
                          final ArrayList stats) {

      for (int i = 0; i < geom.getNumGeometries(); i++) {
         final Geometry part = geom.getGeometryN(i);
         doPolygonPart(part, stats);
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


   private void doPolygonPart(final Geometry geom,
                              final ArrayList stats) {

      boolean bFill;
      boolean bCrossing[];
      int iNX, iNY;
      int i;
      int x, y, ix, xStart, xStop;
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
         substats = (SimpleStats) stats.get(i);
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
                     dValue = m_Window[i].getCellValueAsDouble(x /*+ m_iMinX - 1*/, y /*+ m_iMinY - 1*/);
                     if (!m_Window[i].isNoDataValue(dValue)) {
                        substats.addValue(dValue);
                     }
                  }
               }
            }
         }
      }

   }


   private void doParalellpiped() {

      int iNX, iNY;
      int x, y;
      int iMatchingClass = 0;
      int iClass, iGrid;
      final double dMean[][] = new double[m_Classes.size()][m_Window.length];
      final double dStdDev[][] = new double[m_Classes.size()][m_Window.length];
      double dValue;
      ArrayList stats;
      SimpleStats substats;
      Set set;
      Iterator iter;

      iNX = m_Output.getWindowGridExtent().getNX();
      iNY = m_Output.getWindowGridExtent().getNY();

      set = m_Classes.keySet();
      iter = set.iterator();
      iClass = 0;
      while (iter.hasNext()) {
         stats = (ArrayList) m_Classes.get(iter.next());
         for (iGrid = 0; iGrid < m_Window.length; iGrid++) {
            substats = ((SimpleStats) stats.get(iGrid));
            dMean[iClass][iGrid] = substats.getMean();
            dStdDev[iClass][iGrid] = Math.sqrt(substats.getVariance());
         }
         iClass++;
      }

      for (y = 0; y < iNY; y++) {
         for (x = 0; x < iNX; x++) {
            for (iClass = 0; iClass < m_Classes.size(); iClass++) {
               iMatchingClass = iClass;
               for (iGrid = 0; iGrid < m_Window.length; iGrid++) {
                  dValue = m_Window[iGrid].getCellValueAsDouble(x, y);
                  if (!m_Window[iGrid].isNoDataValue(dValue)) {
                     if (Math.abs(m_Window[iGrid].getCellValueAsDouble(x, y) - dMean[iClass][iGrid]) > dStdDev[iClass][iGrid]) {
                        iMatchingClass = -1;
                        break;
                     }
                  }
                  else {
                     break;
                  }
               }
               if (iMatchingClass != -1) {
                  break;
               }
            }
            if (iMatchingClass != -1) {
               m_Output.setCellValue(x, y, iMatchingClass + 1);
            }
            else {
               m_Output.setNoData(x, y);
            }
         }
      }

   }


   private void doMinimumDistance() {

      int iNX, iNY;
      int x, y;
      int iClass, iGrid, iMin = 0;
      final double dMean[][] = new double[m_Classes.size()][m_Window.length];
      double dMin, d, e;
      double dValue;
      ArrayList stats;
      Set set;
      Iterator iter;

      iNX = m_Output.getWindowGridExtent().getNX();
      iNY = m_Output.getWindowGridExtent().getNY();

      set = m_Classes.keySet();
      iter = set.iterator();
      iClass = 0;
      while (iter.hasNext()) {
         stats = (ArrayList) m_Classes.get(iter.next());
         for (iGrid = 0; iGrid < m_Window.length; iGrid++) {
            dMean[iClass][iGrid] = ((SimpleStats) stats.get(iGrid)).getMean();
         }
         iClass++;
      }

      for (y = 0; y < iNY; y++) {
         for (x = 0; x < iNX; x++) {
            for (iClass = 0, dMin = -1.0; iClass < m_Classes.size(); iClass++) {
               for (iGrid = 0, d = 0.0; iGrid < m_Window.length; iGrid++) {
                  dValue = m_Window[iGrid].getCellValueAsDouble(x, y);
                  if (!m_Window[iGrid].isNoDataValue(dValue)) {
                     e = m_Window[iGrid].getCellValueAsDouble(x, y) - dMean[iClass][iGrid];
                     d += e * e;
                     if ((dMin < 0.0) || (dMin > d)) {
                        dMin = d;
                        iMin = iClass;
                     }
                  }
                  else {
                     dMin = -1;
                  }
               }
            }

            if (dMin >= 0.0) {
               m_Output.setCellValue(x, y, iMin + 1);
            }
            else {
               m_Output.setNoData(x, y);
            }
         }
      }

   }


   private void doMaximumLikelihood() {

      int iNX, iNY;
      int x, y;
      int iClass, iGrid, iMax = 0;
      final double dMean[][] = new double[m_Classes.size()][m_Window.length];
      final double dStdDev[][] = new double[m_Classes.size()][m_Window.length];
      final double dK[][] = new double[m_Classes.size()][m_Window.length];
      double dMax, d, e;
      double dValue;
      ArrayList stats;
      SimpleStats substats;
      Set set;
      Iterator iter;

      iNX = m_Output.getWindowGridExtent().getNX();
      iNY = m_Output.getWindowGridExtent().getNY();

      set = m_Classes.keySet();
      iter = set.iterator();
      iClass = 0;
      while (iter.hasNext()) {
         stats = (ArrayList) m_Classes.get(iter.next());
         for (iGrid = 0; iGrid < m_Window.length; iGrid++) {
            substats = ((SimpleStats) stats.get(iGrid));
            dMean[iClass][iGrid] = substats.getMean();
            dStdDev[iClass][iGrid] = Math.sqrt(substats.getVariance());
            dK[iClass][iGrid] = 1.0 / (dStdDev[iClass][iGrid] * Math.sqrt(2.0 * Math.PI));
         }
         iClass++;
      }

      for (y = 0; y < iNY; y++) {
         for (x = 0; x < iNX; x++) {
            for (iClass = 0, dMax = 0.0; iClass < m_Classes.size(); iClass++) {
               for (iGrid = 0, d = 0.0; iGrid < m_Window.length; iGrid++) {
                  dValue = m_Window[iGrid].getCellValueAsDouble(x, y);
                  if (!m_Window[iGrid].isNoDataValue(dValue)) {
                     e = (m_Window[iGrid].getCellValueAsDouble(x, y) - dMean[iClass][iGrid]) / dStdDev[iClass][iGrid];
                     e = dK[iClass][iGrid] * Math.exp(-0.5 * e * e);
                     d += e * e;
                     if (dMax < d) {
                        dMax = d;
                        iMax = iClass;
                     }
                  }
                  else {
                     dMax = -1;
                  }
               }
            }

            if (dMax > 0.0) {
               m_Output.setCellValue(x, y, iMax + 1);
            }
            else {
               m_Output.setNoData(x, y);
            }
         }
      }

   }


}
