

package es.unex.sextante.vectorize.vectorizeB;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UnsupportedOutputChannelException;
import es.unex.sextante.outputs.OutputVectorLayer;


public class VectorizeBAlgorithm
         extends
            GeoAlgorithm {

   public static final String    LAYER  = "LAYER";
   public static final String    RESULT = "RESULT";

   private IRasterLayer          m_Input;
   private IRasterLayer          m_Edge;
   private IVectorLayer          m_Polygons;
   private int                   m_iNX, m_iNY;
   private char[][]              m_Lock;
   private ArrayList<Coordinate> m_Coords;
   private ArrayList<Geometry>   m_Polyg;

   private static final int      m_IX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private static final int      m_IY[] = { -1, -1, 0, 1, 1, 1, 0, -1 };


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Vectorize_raster_layer__polygons") + "(B)");
      setGroup(Sextante.getText("Vectorization"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Input_layer"), true);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final String sFields[] = new String[2];
      final Class types[] = { Integer.class, Double.class };

      m_Input = m_Parameters.getParameterValueAsRasterLayer(LAYER);
      m_Input.setWindowExtent(m_AnalysisExtent);

      sFields[0] = "ID";
      sFields[1] = m_Input.getName();

      m_Polygons = getNewVectorLayer(RESULT, Sextante.getText("Result"), IVectorLayer.SHAPE_TYPE_POLYGON, types, sFields);

      createPolygons();

      return !m_Task.isCanceled();

   }


   private void createPolygons() throws UnsupportedOutputChannelException {

      int x, y, ID;
      double dValue;

      m_iNX = m_Input.getNX();
      m_iNY = m_Input.getNY();

      m_Coords = new ArrayList<Coordinate>();
      m_Polyg = new ArrayList<Geometry>();

      m_Lock = new char[m_iNY][m_iNX];

      final AnalysisExtent ge = new AnalysisExtent();
      final AnalysisExtent wge = m_Input.getWindowGridExtent();
      ge.setCellSize(wge.getCellSize() * .5);
      ge.setXRange(wge.getXMin() - wge.getCellSize() * .5, wge.getXMax() + wge.getCellSize() * .5, true);
      ge.setYRange(wge.getYMin() - wge.getCellSize() * .5, wge.getYMax() + wge.getCellSize() * .5, true);
      m_Edge = getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_BYTE, ge);

      //m_Area = new char[m_iNY + 1][m_iNX + 1];

      for (y = 0, ID = 1; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            dValue = m_Input.getCellValueAsDouble(x, y);
            if (!m_Input.isNoDataValue(dValue) && (m_Lock[y][x] == 0)) {
               Get_Class(dValue);
               final Object values[] = new Object[2];
               values[0] = new Integer(ID++);
               values[1] = new Double(dValue);
               final GeometryFactory gf = new GeometryFactory();
               m_Polygons.addFeature(gf.createMultiPolygon(m_Polyg.toArray(new Polygon[0])), values);
            }
         }
      }

   }


   private void Get_Class(final double dClassValue) {

      int x, y, i, ix, iy, n, nEdgeCells;

      m_Polyg.clear();

      for (y = 0, nEdgeCells = 0; (y < m_iNY) && !this.m_Task.isCanceled(); y++) {
         for (x = 0; x < m_iNX; x++) {
            double dValue = m_Input.getCellValueAsDouble(x, y);
            if ((m_Lock[y][x] == 0) && (dValue == dClassValue)) {
               m_Lock[y][x] = 1;
               for (i = 0, n = 0; i < 8; i += 2) {
                  ix = Get_xTo(i, x);
                  iy = Get_yTo(i, y);
                  dValue = m_Input.getCellValueAsDouble(ix, iy);
                  if (dValue != dClassValue) {
                     ix = Get_xTo(i, 1 + 2 * x);
                     iy = Get_yTo(i, 1 + 2 * y);
                     m_Edge.setCellValue(ix, iy, i + 2);

                     ix = Get_xTo(i - 1, 1 + 2 * x);
                     iy = Get_yTo(i - 1, 1 + 2 * y);
                     m_Edge.setCellValue(ix, iy, m_Edge.getCellValueAsInt(ix, iy) != 0 ? -1 : i + 2);

                     n++;
                  }
               }

               if (n == 4) {
                  Get_Square(1 + 2 * x, 1 + 2 * y);
               }
               else {
                  nEdgeCells++;
               }
            }
         }
      }

      if (nEdgeCells > 0) {
         Get_Polygons();
      }

   }


   private void Get_Square(final int x,
                           final int y) {

      int i, ix, iy;
      final double dCellSize = m_Edge.getWindowCellSize() * .5;

      for (i = 0; i < 8; i++) {
         ix = Get_xTo(i, x);
         iy = Get_yTo(i, y);

         m_Edge.setCellValue(ix, iy, m_Edge.getCellValueAsInt(ix, iy) > 0 ? 0 : (i > 1 ? i - 1 : i + 7));

         if (i % 2 != 0) {
            Point2D pt;
            pt = m_Edge.getWindowGridExtent().getWorldCoordsFromGridCoords(ix, iy);
            m_Coords.add(new Coordinate(pt.getX() + dCellSize, pt.getY() - dCellSize));
         }
      }
   }


   private void Get_Polygons() {

      for (int y = 0; (y < m_Edge.getNY()) && !m_Task.isCanceled(); y++) {
         for (int x = 0; x < m_Edge.getNX(); x++) {
            if (m_Edge.getCellValueAsInt(x, y) > 0) {
               m_Coords.clear();
               Get_Polygon(x, y);
               final Coordinate coord = m_Coords.get(0);
               m_Coords.add(new Coordinate(coord.x, coord.y));
               final Coordinate[] coords = m_Coords.toArray(new Coordinate[0]);
               final GeometryFactory gf = new GeometryFactory();
               try {
                  final LinearRing ring = gf.createLinearRing(coords);
                  m_Polyg.add(gf.createPolygon(ring, null));
               }
               catch (final Exception e) {
                  // might reach this if we try to create a polygon with less than 3 coords.
                  // we just ignore it
               }
            }
         }
      }

   }


   private void Get_Polygon(int x,
                            int y) {

      int i, iLast = -1;
      final double dCellSize = m_Edge.getWindowCellSize() * .5;

      while ((i = m_Edge.getCellValueAsInt(x, y)) != 0) {
         if (i < 0) {
            i = iLast + 2;

            m_Edge.setCellValue(x, y, (iLast == 2 ? 8 : iLast - 2));
         }
         else {
            m_Edge.setCellValue(x, y, 0);
         }

         if (i != iLast) {
            final Point2D pt = m_Edge.getWindowGridExtent().getWorldCoordsFromGridCoords(x, y);
            iLast = i;
            m_Coords.add(new Coordinate(pt.getX() + dCellSize, pt.getY() - dCellSize));
         }

         x = Get_xTo(i, x);
         y = Get_yTo(i, y);
      }
   }


   private int Get_xTo(int Direction,
                       final int x) {

      Direction %= 8;

      if (Direction < 0) {
         Direction += 8;
      }

      return (x + m_IX[Direction]);

   }


   private int Get_yTo(int Direction,
                       final int x) {

      Direction %= 8;

      if (Direction < 0) {
         Direction += 8;
      }

      return (x + m_IY[Direction]);

   }


}
