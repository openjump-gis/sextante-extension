package es.unex.sextante.vectorize.contourLines;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;

public class ContourLinesAlgorithm
         extends
            GeoAlgorithm {

   public static final String    LAYER    = "LAYER";
   public static final String    DISTANCE = "DISTANCE";
   public static final String    MIN      = "MIN";
   public static final String    MAX      = "MAX";
   public static final String    RESULT   = "RESULT";

   private IRasterLayer          m_Window;
   private IVectorLayer          m_Contour;
   private char[][]              m_Row;
   private char[][]              m_Col;
   private final GeometryFactory m_GF     = new GeometryFactory();


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Contour_lines"));
      setGroup(Sextante.getText("Vectorization"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(LAYER, Sextante.getText("Input_layer"), true);
         m_Parameters.addNumericalValue(DISTANCE, Sextante.getText("Equidistance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 100, 0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(MIN, Sextante.getText("Min_value"), 0,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         m_Parameters.addNumericalValue(MAX, Sextante.getText("Max_value"), 10000.,
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE);
         addOutputVectorLayer(RESULT, Sextante.getText("Contour_lines"), OutputVectorLayer.SHAPE_TYPE_LINE);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() {

      double dMin, dMax;
      double dDistance;
      final String sFields[] = new String[2];
      final Class types[] = { Integer.class, Double.class };

      try {

         m_Window = m_Parameters.getParameterValueAsRasterLayer(LAYER);
         dMin = m_Parameters.getParameterValueAsDouble(MIN);
         dMax = m_Parameters.getParameterValueAsDouble(MAX);
         dDistance = m_Parameters.getParameterValueAsDouble(DISTANCE);

         m_Window.setFullExtent();

         if ((dMin <= dMax) && (dDistance > 0)) {
            if (dMin < m_Window.getMinValue()) {
               dMin += dDistance * (int) ((m_Window.getMinValue() - dMin) / dDistance);
            }
            if (dMax > m_Window.getMaxValue()) {
               dMax = m_Window.getMaxValue();
            }

            sFields[0] = "ID";
            sFields[1] = m_Window.getName();

            m_Contour = getNewVectorLayer(RESULT, Sextante.getText("Contour_lines"), IVectorLayer.SHAPE_TYPE_LINE, types, sFields);

            createContours(dMin, dMax, dDistance);

            return !m_Task.isCanceled();
         }

         return false;

      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
         return false;
      }

   }


   private void createContours(final double dMin,
                               final double dMax,
                               double dDistance) {

      int x, y;
      int i;
      int ID;
      int iNX, iNY;
      double dZ;
      double dValue = 0;

      iNX = m_Window.getNX();
      iNY = m_Window.getNY();

      m_Row = new char[iNY][iNX];
      m_Col = new char[iNY][iNX];

      if (dDistance <= 0) {
         dDistance = 1;
      }

      for (dZ = dMin, ID = 0; (dZ <= dMax) && setProgress((int) (dZ - dMin), (int) (dMax - dMin)); dZ += dDistance) {
         for (y = 0; y < iNY - 1; y++) {
            for (x = 0; x < iNX - 1; x++) {
               dValue = m_Window.getCellValueAsDouble(x, y);
               if (dValue >= dZ) {
                  m_Row[y][x] = (char) (m_Window.getCellValueAsDouble(x + 1, y) < dZ ? 1 : 0);
                  m_Col[y][x] = (char) (m_Window.getCellValueAsDouble(x, y + 1) < dZ ? 1 : 0);
               }
               else {
                  m_Row[y][x] = (char) (m_Window.getCellValueAsDouble(x + 1, y) >= dZ ? 1 : 0);
                  m_Col[y][x] = (char) (m_Window.getCellValueAsDouble(x, y + 1) >= dZ ? 1 : 0);
               }
            }
         }

         for (y = 0; y < iNY - 1; y++) {
            for (x = 0; x < iNX - 1; x++) {
               if (m_Row[y][x] != 0) {
                  for (i = 0; i < 2; i++) {
                     findContour(x, y, dZ, true, ID++);
                  }
                  m_Row[y][x] = 0;
               }

               if (m_Col[y][x] != 0) {
                  for (i = 0; i < 2; i++) {
                     findContour(x, y, dZ, false, ID++);
                  }
                  m_Col[y][x] = 0;
               }
            }
         }

      }

   }


   private void findContour(final int x,
                            final int y,
                            final double z,
                            final boolean doRow,
                            final int ID) {

      boolean doContinue = true;
      final boolean bIsFirstPoint = true;
      int zx = doRow ? x + 1 : x;
      int zy = doRow ? y : y + 1;
      double d;
      double xPos, yPos;
      final double xMin = m_Window.getWindowGridExtent().getXMin();
      final double yMax = m_Window.getWindowGridExtent().getYMax();
      Geometry line;
      final Object values[] = new Object[2];
      final NextContourInfo info = new NextContourInfo();
      final ArrayList<Coordinate> coords = new ArrayList<Coordinate>();

      info.x = x;
      info.y = y;
      info.iDir = 0;
      info.doRow = doRow;

      do {

         d = m_Window.getCellValueAsDouble(info.x, info.y);
         d = (d - z) / (d - m_Window.getCellValueAsDouble(zx, zy));

         xPos = xMin + m_Window.getWindowCellSize() * (info.x + d * (zx - info.x) + 0.5);
         yPos = yMax - m_Window.getWindowCellSize() * (info.y + d * (zy - info.y) + 0.5);

         coords.add(new Coordinate(xPos, yPos));

         if (!findNextContour(info)) {
            doContinue = findNextContour(info);
         }

         info.iDir = (info.iDir + 5) % 8;

         if (info.doRow) {
            m_Row[info.y][info.x] = 0;
            zx = info.x + 1;
            zy = info.y;
         }
         else {
            m_Col[info.y][info.x] = 0;
            zx = info.x;
            zy = info.y + 1;
         }

      }
      while (doContinue);

      values[0] = new Integer(ID);
      values[1] = new Double(z);

      final Coordinate[] coordinates = new Coordinate[coords.size()];
      for (int i = 0; i < coordinates.length; i++) {
         coordinates[i] = coords.get(i);
      }

      if (coordinates.length > 1) {
         line = m_GF.createLineString(coordinates);
         m_Contour.addFeature(line, values);
      }

   }


   private boolean findNextContour(final NextContourInfo info) {

      boolean doContinue;

      if (info.doRow) {
         switch (info.iDir) {
            case 0:
               if (m_Row[info.y + 1][info.x] != 0) {
                  info.y++;
                  info.iDir = 0;
                  doContinue = true;
                  break;
               }
            case 1:
               if (m_Col[info.y][info.x + 1] != 0) {
                  info.x++;
                  info.iDir = 1;
                  info.doRow = false;
                  doContinue = true;
                  break;
               }
            case 2:
            case 3:
               if (info.y - 1 >= 0) {
                  if (m_Col[info.y - 1][info.x + 1] != 0) {
                     info.x++;
                     info.y--;
                     info.doRow = false;
                     info.iDir = 3;
                     doContinue = true;
                     break;
                  }
               }
            case 4:
               if (info.y - 1 >= 0) {
                  if (m_Row[info.y - 1][info.x] != 0) {
                     info.y--;
                     info.iDir = 4;
                     doContinue = true;
                     break;
                  }
               }
            case 5:
               if (info.y - 1 >= 0) {
                  if (m_Col[info.y - 1][info.x] != 0) {
                     info.y--;
                     info.doRow = false;
                     info.iDir = 5;
                     doContinue = true;
                     break;
                  }
               }
            case 6:
            case 7:
               if (m_Col[info.y][info.x] != 0) {
                  info.doRow = false;
                  info.iDir = 7;
                  doContinue = true;
                  break;
               }
            default:
               info.iDir = 0;
               doContinue = false;
         }
      }
      else {
         switch (info.iDir) {
            case 0:
            case 1:
               if (m_Row[info.y + 1][info.x] != 0) {
                  info.y++;
                  info.doRow = true;
                  info.iDir = 1;
                  doContinue = true;
                  break;
               }
            case 2:
               if (m_Col[info.y][info.x + 1] != 0) {
                  info.x++;
                  info.iDir = 2;
                  doContinue = true;
                  break;
               }
            case 3:
               if (m_Row[info.y][info.x] != 0) {
                  info.doRow = true;
                  info.iDir = 3;
                  doContinue = true;
                  break;
               }
            case 4:
            case 5:
               if (info.x - 1 >= 0) {
                  if (m_Row[info.y][info.x - 1] != 0) {
                     info.x--;
                     info.doRow = true;
                     info.iDir = 5;
                     doContinue = true;
                     break;
                  }
               }
            case 6:
               if (info.x - 1 >= 0) {
                  if (m_Col[info.y][info.x - 1] != 0) {
                     info.x--;
                     info.iDir = 6;
                     doContinue = true;
                     break;
                  }
               }
            case 7:
               if (info.x - 1 >= 0) {
                  if (m_Row[info.y + 1][info.x - 1] != 0) {
                     info.x--;
                     info.y++;
                     info.doRow = true;
                     info.iDir = 7;
                     doContinue = true;
                     break;
                  }
               }
            default:
               info.iDir = 0;
               doContinue = false;
         }
      }

      return (doContinue);
   }

   private class NextContourInfo {

      public int     iDir;
      public int     x;
      public int     y;
      public boolean doRow;

   }

}
