package es.unex.sextante.gridCategorical.fragstatsArea;

import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class FragstatsAreaAlgorithm
         extends
            GeoAlgorithm {

   public static final String METRICS      = "METRICS";
   public static final String INPUT        = "INPUT";

   private final int          m_iOffsetX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final int          m_iOffsetY[] = { 1, 1, 0, -1, -1, -1, 0, 1 };

   private int                m_iNX, m_iNY;
   private IRasterLayer       m_Grid;
   private IRasterLayer       m_Visited;
   private ArrayList          m_Patches;
   private HashMap            m_Classes;
   private ClassInfo          m_Landscape;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Fragstats__area-density-edge_metrics"));
      setGroup(Sextante.getText("Raster_categories_analysis"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         addOutputText(METRICS, Sextante.getText("Metrics"), null);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      byte value;
      int x, y;
      Integer ClassID;
      PatchInfo patchInfo;
      ClassInfo classInfo;

      m_Grid = m_Parameters.getParameterValueAsRasterLayer(INPUT);

      m_Grid.setFullExtent();

      m_iNX = m_Grid.getNX();
      m_iNY = m_Grid.getNY();

      m_Patches = new ArrayList();
      m_Classes = new HashMap();
      m_Landscape = new ClassInfo();

      m_Visited = this.getTempRasterLayer(IRasterLayer.RASTER_DATA_TYPE_BYTE, m_Grid.getWindowGridExtent());

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            value = m_Visited.getCellValueAsByte(x, y);
            if (value == 0) {
               patchInfo = analysePatch(x, y);
               if (patchInfo != null) {
                  m_Patches.add(patchInfo);
                  ClassID = new Integer(patchInfo.getClassID());
                  classInfo = (ClassInfo) m_Classes.get(ClassID);
                  if (classInfo == null) {
                     classInfo = new ClassInfo();
                     classInfo.add(patchInfo);
                     m_Classes.put(ClassID, classInfo);
                  }
                  else {
                     classInfo.add(patchInfo);
                  }
                  m_Landscape.add(patchInfo);
               }
            }
         }

      }

      if (m_Task.isCanceled()) {
         return false;
      }
      else {
         createOutput();
         return true;
      }

   }


   private void createOutput() {

      int i;
      Integer Class;
      final HTMLDoc doc = new HTMLDoc();
      PatchInfo patchInfo;
      ClassInfo classInfo;
      String sTable[][];
      final DecimalFormat df = new DecimalFormat("##.##");

      sTable = new String[m_Patches.size() + 1][4];
      doc.open(Sextante.getText("area-density-edge_metrics"));
      doc.addHeader(Sextante.getText("area-density-edge_metrics"), 1);
      doc.addHeader(Sextante.getText("Patch_metrics"), 2);
      sTable[0][0] = Sextante.getText("Class");
      sTable[0][1] = Sextante.getText("Area");
      sTable[0][2] = Sextante.getText("Perimeter");
      sTable[0][3] = Sextante.getText("Radius_of_gyration");
      for (i = 0; i < m_Patches.size(); i++) {
         patchInfo = (PatchInfo) m_Patches.get(i);
         sTable[i + 1][0] = Integer.toString(i);
         sTable[i + 1][1] = df.format(patchInfo.getArea());
         sTable[i + 1][2] = df.format(patchInfo.getPerimeter());
         sTable[i + 1][3] = df.format(patchInfo.getRadiusOfGyration());
      }
      doc.addTable(sTable, "", true, true);

      doc.addHeader(Sextante.getText("Class_metrics"), 2);

      final Set set = m_Classes.keySet();
      final Iterator iter = set.iterator();
      sTable = new String[m_Classes.size() + 1][9];
      int iRow = 1;
      sTable[0][0] = Sextante.getText("Class");
      sTable[0][1] = Sextante.getText("total_area_Total");
      sTable[0][2] = Sextante.getText("Percentage_of_Landscape");
      sTable[0][3] = Sextante.getText("Number_of_patches");
      sTable[0][4] = Sextante.getText("Patch_Density");
      sTable[0][5] = Sextante.getText("Total_perimeter");
      sTable[0][6] = Sextante.getText("Edge_Density");
      sTable[0][7] = Sextante.getText("Landscape_shape_index");
      sTable[0][8] = Sextante.getText("Largest_patch_index");
      while (iter.hasNext()) {
         Class = (Integer) iter.next();
         classInfo = (ClassInfo) m_Classes.get(Class);
         classInfo.setTotalLandscapeArea(m_Landscape.getTotalArea());
         sTable[iRow][0] = Integer.toString(Class.intValue());
         sTable[iRow][1] = df.format(classInfo.getTotalArea());
         sTable[iRow][2] = df.format(classInfo.getPercentageOfLandscape());
         sTable[iRow][3] = df.format(classInfo.getPatchesCount());
         sTable[iRow][4] = df.format(classInfo.getPatchDensity());
         sTable[iRow][5] = df.format(classInfo.getTotalEdge());
         sTable[iRow][6] = df.format(classInfo.getEdgeDensity());
         sTable[iRow][7] = df.format(classInfo.getLandscapeShapeIndex());
         sTable[iRow][8] = df.format(classInfo.getLargestPatchIndex());
         iRow++;
      }
      doc.addTable(sTable, "", true, true);

      m_Landscape.setTotalLandscapeArea(m_Landscape.getTotalArea());
      doc.addHeader(Sextante.getText("Landscape_metrics"), 2);
      doc.startUnorderedList();
      doc.addListElement(Sextante.getText("Total_area") + ": " + df.format(m_Landscape.getTotalArea()));
      doc.addListElement(Sextante.getText("Number_of_patches") + ": " + df.format(m_Landscape.getPatchesCount()));
      doc.addListElement(Sextante.getText("Patch_Density") + ": " + df.format(m_Landscape.getPatchDensity()));
      doc.addListElement(Sextante.getText("Total_perimeter") + ": " + df.format(m_Landscape.getTotalEdge()));
      doc.addListElement(Sextante.getText("Edge_Density") + ": " + df.format(m_Landscape.getEdgeDensity()));
      doc.addListElement(Sextante.getText("Landscape_shape_index") + ": " + df.format(m_Landscape.getLandscapeShapeIndex()));
      doc.addListElement(Sextante.getText("Largest_patch_index") + ": " + df.format(m_Landscape.getLargestPatchIndex()));
      doc.closeUnorderedList();
      doc.close();

      addOutputText(METRICS, Sextante.getText("Metrics") + "[" + m_Grid.getName() + "]", doc.getHTMLCode());

      //TODO: what about distributions of landscape and patch values!!???
   }


   private PatchInfo analysePatch(int x,
                                  int y) {

      int iInitX, iInitY;
      int x2, y2;
      int iInitClass;
      int iPt;
      int i, n;
      int iClass;
      final int iVisitedValue = 1;
      byte visited;
      ArrayList centralPoints = new ArrayList();
      ArrayList adjPoints = new ArrayList();
      Point point;
      PatchInfo info;
      iInitClass = m_Grid.getCellValueAsInt(x, y);

      if (m_Grid.isNoDataValue(iInitClass)) {
         return null;
      }

      iInitX = x;
      iInitY = y;

      info = new PatchInfo(iInitClass, m_Grid);

      centralPoints.add(new Point(x, y));
      info.add(x, y);
      m_Visited.setCellValue(x, y, 1);

      for (i = 0; i < 2; i++) {
         while (centralPoints.size() != 0) {
            for (iPt = 0; iPt < centralPoints.size(); iPt++) {
               point = (Point) centralPoints.get(iPt);
               x = point.x;
               y = point.y;
               iClass = m_Grid.getCellValueAsInt(x, y);
               if (!m_Grid.isNoDataValue(iClass)) {
                  for (n = 0; n < 8; n++) {
                     x2 = x + m_iOffsetX[n];
                     y2 = y + m_iOffsetY[n];
                     iClass = m_Grid.getCellValueAsInt(x2, y2);
                     if (!m_Grid.isNoDataValue(iClass)) {
                        visited = m_Visited.getCellValueAsByte(x2, y2);
                        if (visited == i) {
                           if (iInitClass == iClass) {
                              m_Visited.setCellValue(x2, y2, i + 1);
                              if (iVisitedValue == 0) {
                                 info.add(x2, y2);
                              }
                              else { //1
                                 info.addForRadiusOfGyration(x2, y2);
                              }
                              adjPoints.add(new Point(x2, y2));
                           }
                        }
                     }
                  }
               }
            }
            centralPoints = adjPoints;
            adjPoints = new ArrayList();
         }
         centralPoints.clear();
         centralPoints.add(new Point(iInitX, iInitY));
         info.addForRadiusOfGyration(iInitX, iInitX);
         if (i == 0) {
            info.calculateCentroid();
         }
         /*m_Visited.setCellValue(x, y, 2);
         iVisitedValue = 2;*/
      }

      return info;

   }


}
