package es.unex.sextante.gridCategorical.fragstatsDiversity;

import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

;

public class FragstatsDiversityAlgorithm
         extends
            GeoAlgorithm {

   public static final String METRICS      = "METRICS";
   public static final String INPUT        = "INPUT";
   public static final String MAX          = "MAX";

   private final int          m_iOffsetX[] = { 0, 1, 1, 1, 0, -1, -1, -1 };
   private final int          m_iOffsetY[] = { 1, 1, 0, -1, -1, -1, 0, 1 };

   private int                m_iNX, m_iNY;
   private int                m_iMax;
   private double             m_dLandscapeArea;
   private IRasterLayer       m_Grid;
   private IRasterLayer       m_Visited;
   private ArrayList          m_Patches;
   private HashMap            m_Classes;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Fragstats__diversity_metrics"));
      setGroup(Sextante.getText("Raster_categories_analysis"));
      setUserCanDefineAnalysisExtent(false);

      try {
         m_Parameters.addInputRasterLayer(INPUT, Sextante.getText("Layer"), true);
         m_Parameters.addNumericalValue(MAX, Sextante.getText("Maximum_number_of_different_classes"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 10, 0, Integer.MAX_VALUE);
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
      m_iMax = m_Parameters.getParameterValueAsInt(MAX);

      m_Grid.setFullExtent();

      m_iNX = m_Grid.getNX();
      m_iNY = m_Grid.getNY();

      m_Patches = new ArrayList();
      m_Classes = new HashMap();

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
                  m_dLandscapeArea += patchInfo.getArea();
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

      Integer Class;
      final HTMLDoc doc = new HTMLDoc();
      ClassInfo classInfo;
      final DecimalFormat df = new DecimalFormat("##.##");
      double dPercentage;
      double dShannon = 0;
      double dSimpson = 0;
      double dSimpsonMod = 0;
      double dShannonEvenness;
      double dSimpsonEvenness;
      double dSimpsonEvennessMod;

      final Set set = m_Classes.keySet();
      final Iterator iter = set.iterator();
      while (iter.hasNext()) {
         Class = (Integer) iter.next();
         classInfo = (ClassInfo) m_Classes.get(Class);
         classInfo.setTotalLandscapeArea(m_dLandscapeArea);
         dPercentage = classInfo.getPercentageOfLandscape();
         dShannon -= (dPercentage * Math.log(dPercentage));
         dSimpson += (dPercentage * dPercentage);
      }

      dSimpsonMod = -Math.log(dSimpson);
      dSimpson = 1 - dSimpson;
      dShannonEvenness = dShannon / Math.log(m_iMax);
      dSimpsonEvenness = dSimpson / (1 - (1 / (double) m_iMax));
      dSimpsonEvennessMod = dSimpsonMod / Math.log(m_iMax);

      doc.open(Sextante.getText("Diversity_metrics"));
      doc.addHeader(Sextante.getText("Diversity_metrics"), 1);
      doc.addHeader(Sextante.getText("Landscape_metrics"), 2);
      doc.startUnorderedList();
      doc.addListElement(Sextante.getText("Richness") + ": " + Integer.toString(m_Classes.size()));
      doc.addListElement(Sextante.getText("Patch_Richness_Density") + ": "
                         + df.format(m_Classes.size() * 10000. * 100. / m_dLandscapeArea));
      doc.addListElement(Sextante.getText("Relative_richness") + ": " + df.format(m_Classes.size() / (double) m_iMax * 100));
      doc.addListElement(Sextante.getText("Shannon_index") + ": " + df.format(dShannon));
      doc.addListElement(Sextante.getText("Simpson_index") + ": " + df.format(dSimpson));
      doc.addListElement(Sextante.getText("Modified_Simpson_index") + ": " + df.format(dSimpsonMod));
      doc.addListElement(Sextante.getText("Shannon_uniformity_index") + ": " + df.format(dShannonEvenness));
      doc.addListElement(Sextante.getText("Simpson_uniformity_index") + ": " + df.format(dSimpsonEvenness));
      doc.addListElement(Sextante.getText("Modified_Simpson_uniformity_index") + ": " + df.format(dSimpsonEvennessMod));
      doc.closeUnorderedList();
      doc.close();

      addOutputText(METRICS, Sextante.getText("Metrics") + "[" + m_Grid.getName() + "]", doc.getHTMLCode());

   }


   private PatchInfo analysePatch(int x,
                                  int y) {

      int x2, y2;
      int iInitClass;
      int iPt;
      int n;
      int iClass;
      byte visited;
      ArrayList centralPoints = new ArrayList();
      ArrayList adjPoints = new ArrayList();
      Point point;
      PatchInfo info;
      iInitClass = m_Grid.getCellValueAsInt(x, y);

      if (m_Grid.isNoDataValue(iInitClass)) {
         return null;
      }

      centralPoints.add(new Point(x, y));

      info = new PatchInfo(iInitClass, m_Grid);

      while (centralPoints.size() != 0) {
         //int iout = 0;
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
                     if (visited != 1) {
                        if (iInitClass == iClass) {
                           m_Visited.setCellValue(x2, y2, 1);
                           info.addCell();
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


      return info;

   }


}
