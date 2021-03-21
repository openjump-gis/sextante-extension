package es.unex.sextante.vectorize.vectorizeLines;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class VectorizeLinesAlgorithm
         extends
            GeoAlgorithm {

   private final static int      m_iOffsetX[]      = { 0, 1, 0, -1 };
   private final static int      m_iOffsetY[]      = { -1, 0, 1, 0 };

   private final static int      m_iOffsetXDiag[]  = { -1, 1, 1, -1 };
   private final static int      m_iOffsetYDiag[]  = { -1, -1, 1, 1 };

   public static final String    LAYER             = "LAYER";
   public static final String    RESULT            = "RESULT";

   private IRasterLayer          m_Window;
   private IRasterLayer          m_Visited;
   private IVectorLayer          m_Lines;
   private int                   m_iNX, m_iNY;
   private int                   m_iLine           = 1;
   private IRasterLayer          m_Visited2;
   private final GeometryFactory m_GeometryFactory = new GeometryFactory();


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Vectorize_raster_layer__lines"));
      setGroup(Sextante.getText("Vectorization"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Input_layer"), true);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      double dValue;
      final String sFields[] = new String[1];
      final Class types[] = { Integer.class };

      m_Window = m_Parameters.getParameterValueAsRasterLayer(LAYER);
      m_Window.setWindowExtent(m_AnalysisExtent);

      m_Visited = this.getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_BYTE, m_Window.getWindowGridExtent());
      m_Visited2 = this.getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_BYTE, m_Window.getWindowGridExtent());

      m_iNX = m_Window.getNX();
      m_iNY = m_Window.getNY();

      for (y = 0; y < m_iNY; y++) {
         for (x = 0; x < m_iNX; x++) {
            dValue = m_Window.getCellValueAsDouble(x, y);
            if (m_Window.isNoDataValue(dValue) || (dValue == 0)) {
               m_Visited.setCellValue(x, y, 0.0);
            }
            else {
               m_Visited.setCellValue(x, y, 1.0);
            }
         }
      }

      sFields[0] = "ID";

      m_Lines = getNewVectorLayer(RESULT, Sextante.getText("Result"), IVectorLayer.SHAPE_TYPE_LINE, types, sFields);

      createLines();

      return !m_Task.isCanceled();

   }


   private void createLines() {

      int x, y;
      byte byValue;

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            byValue = m_Visited.getCellValueAsByte(x, y);
            if (byValue == 1) {
               createLine(x, y, m_Visited.getWindowGridExtent().getWorldCoordsFromGridCoords(x, y));
            }
         }
      }

   }


   private void createLine(int x,
                           int y,
                           Point2D pt2d2) {

      boolean bContinue = false;
      boolean bIsNotNull = false;
      Point pt;
      final Object values[] = new Object[1];

      final ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
      coordinates.add(new Coordinate(pt2d2.getX(), pt2d2.getY()));
      pt2d2 = m_Visited.getWindowGridExtent().getWorldCoordsFromGridCoords(x, y);
      coordinates.add(new Coordinate(pt2d2.getX(), pt2d2.getY()));

      do {
         m_Visited.setCellValue(x, y, 0);
         final ArrayList cells = getSurroundingLineCells(x, y);
         m_Visited2.setCellValue(x, y, cells.size());
         if (cells.size() == 0) {
            final Coordinate[] coords = new Coordinate[coordinates.size()];
            for (int i = 0; i < coords.length; i++) {
               coords[i] = coordinates.get(i);
            }
            final Geometry line = m_GeometryFactory.createLineString(coords);
            values[0] = new Integer(m_iLine++);
            m_Lines.addFeature(line, values);
            bContinue = false;
         }
         else if (cells.size() == 1) {
            pt = (Point) cells.get(0);
            pt2d2 = m_Visited.getWindowGridExtent().getWorldCoordsFromGridCoords(pt.x, pt.y);
            coordinates.add(new Coordinate(pt2d2.getX(), pt2d2.getY()));
            x = pt.x;
            y = pt.y;
            bContinue = true;
            bIsNotNull = true;
         }
         else {
            if (bIsNotNull) {
               final Coordinate[] coords = new Coordinate[coordinates.size()];
               for (int i = 0; i < coords.length; i++) {
                  coords[i] = coordinates.get(i);
               }
               final Geometry line = m_GeometryFactory.createLineString(coords);
               values[0] = new Integer(m_iLine++);
               m_Lines.addFeature(line, values);
            }
            for (int i = 0; i < cells.size(); i++) {
               pt = (Point) cells.get(i);
               m_Visited.setCellValue(pt.x, pt.y, 0);
            }
            for (int i = 0; i < cells.size(); i++) {
               pt = (Point) cells.get(i);
               pt2d2 = m_Visited.getWindowGridExtent().getWorldCoordsFromGridCoords(x, y);
               createLine(pt.x, pt.y, pt2d2);
            }

         }
      }
      while (bContinue && !m_Task.isCanceled());

   }


   private ArrayList getSurroundingLineCells(final int x,
                                             final int y) {

      int i;
      final int j;
      final ArrayList cells = new ArrayList();
      final boolean bBlocked[] = new boolean[4];

      for (i = 0; i < 4; i++) {
         if (m_Visited.getCellValueAsByte(x + m_iOffsetX[i], y + m_iOffsetY[i]) == 1) {
            cells.add(new Point(x + m_iOffsetX[i], y + m_iOffsetY[i]));
            bBlocked[i] = true;
            bBlocked[(i + 1) % 4] = true;
         }
      }

      for (i = 0; i < 4; i++) {
         if ((m_Visited.getCellValueAsByte(x + m_iOffsetXDiag[i], y + m_iOffsetYDiag[i]) == 1) && !bBlocked[i]) {
            cells.add(new Point(x + m_iOffsetXDiag[i], y + m_iOffsetYDiag[i]));
         }
      }

      return cells;

   }

}
