package es.unex.sextante.gridCalculus.gridCalculator;

import java.util.ArrayList;

import org.nfunk.jep.JEP;

import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.parameters.RasterLayerAndBand;

public class FormulaParser {

   private static final int MAX_BANDS = 256;


   public static String prepareFormula(final String sFormula,
                                       final ArrayList names) {

      int i;
      int iIndex;
      String sName;
      String sSubstring;
      final StringBuffer sb = new StringBuffer(sFormula);
      iIndex = 0;
      while ((iIndex = sb.indexOf("|", iIndex)) != -1) {
         if ((sb.indexOf("|", iIndex - 1) == -1) && (sb.indexOf("|", iIndex + 1) == -1)) {
            sb.delete(iIndex, iIndex + 1);
            sb.insert(iIndex, " Band ");
            iIndex = 0;
         }
         else {
            iIndex++;
         }
      }
      iIndex = 0;
      for (i = 0; i < names.size(); i++) {
         sName = ((String) names.get(i)).toLowerCase();
         while ((iIndex = sb.indexOf(sName, iIndex)) != -1) {
            sSubstring = sb.substring(iIndex, Math.min(iIndex + sName.length() + " band ".length(), sb.length()));
            if (!sSubstring.toLowerCase().equals(sName + " band ")) {
               sb.delete(iIndex, iIndex + sName.length());
               sb.insert(iIndex, sName + " Band 1");
               iIndex = 0;
            }
            else {
               iIndex++;
            }
         }
      }

      return sb.toString();

   }


   public static ArrayList getBandsFromFormula(String sFormula,
                                               final ArrayList layers) {

      int i, j;

      IRasterLayer layer;
      String sLayerName;
      final ArrayList array = new ArrayList();
      final ArrayList names = new ArrayList();
      final JEP jep = new JEP();
      jep.addStandardConstants();
      jep.addStandardFunctions();

      for (i = 0; i < layers.size(); i++) {
         layer = (IRasterLayer) layers.get(i);
         names.add(layer.getName());
      }

      sFormula = FormulaParser.prepareFormula(sFormula.toLowerCase(), names);
      sFormula = sFormula.toLowerCase().replaceAll(" ", "");
      sFormula = sFormula.replaceAll("\\[", "_");
      sFormula = sFormula.replaceAll("\\]", "_");
      sFormula = FormulaParser.replaceDots(sFormula);
      for (i = 0; i < layers.size(); i++) {
         layer = (IRasterLayer) layers.get(i);
         for (j = 0; j < layer.getBandsCount(); j++) {
            sLayerName = layer.getName() + " Band " + Integer.toString(j + 1);
            sLayerName = sLayerName.toLowerCase();
            sLayerName = sLayerName.replaceAll(" ", "");
            sLayerName = sLayerName.replaceAll("\\[", "_");
            sLayerName = sLayerName.replaceAll("\\]", "_");
            sLayerName = FormulaParser.replaceDots(sLayerName);
            if (sFormula.lastIndexOf(sLayerName) != -1) {
               array.add(new RasterLayerAndBand(layer, j));
               jep.addVariable(sLayerName, 0.0);
            }
         }

      }


      jep.parseExpression(sFormula);

      if (jep.hasError()) {
         Sextante.addErrorToLog(jep.getErrorInfo());
         return null;
      }

      if (array.size() == 0) {
         return null;
      }

      return array;

   }


   public static ArrayList getBandsFromFormulaForModeler(String sFormula,
                                                         final ObjectAndDescription[] layers,
                                                         final ObjectAndDescription[] numerical) {

      int i, j;

      ObjectAndDescription oad;
      String sLayerName, sName;
      final ArrayList<String> array = new ArrayList<String>();
      final ArrayList<String> names = new ArrayList<String>();
      final JEP jep = new JEP();
      jep.addStandardConstants();
      jep.addStandardFunctions();

      for (i = 0; i < layers.length; i++) {
         oad = layers[i];
         names.add((String) oad.getObject());
      }

      for (i = 0; i < numerical.length; i++) {
         jep.addVariable(((String) numerical[i].getObject()).toLowerCase(), 0.0);
      }

      sFormula = FormulaParser.prepareFormula(sFormula.toLowerCase(), names);
      sFormula = sFormula.toLowerCase().replaceAll(" ", "");
      sFormula = sFormula.replaceAll("\\[", "_");
      sFormula = sFormula.replaceAll("\\]", "_");
      sFormula = sFormula.replaceAll("\\:", "_");
      sFormula = sFormula.replaceAll("\"", "_");
      sFormula = FormulaParser.replaceDots(sFormula);
      for (i = 0; i < names.size(); i++) {
         sName = names.get(i);
         for (j = 0; j < MAX_BANDS; j++) {
            sLayerName = sName + " Band " + Integer.toString(j + 1);
            sLayerName = sLayerName.toLowerCase();
            sLayerName = sLayerName.replaceAll(" ", "");
            sLayerName = sLayerName.replaceAll("\\[", "_");
            sLayerName = sLayerName.replaceAll("\\]", "_");
            sLayerName = sLayerName.replaceAll("\\:", "_");
            sLayerName = sLayerName.replaceAll("\"", "_");
            sLayerName = FormulaParser.replaceDots(sLayerName);
            if (sFormula.lastIndexOf(sLayerName) != -1) {
               array.add((String) layers[i].getObject());
               jep.addVariable(sLayerName, 0.0);
            }
         }

      }

      jep.parseExpression(sFormula);

      if (jep.hasError()) {
         return null;
      }

      if (array.size() == 0) {
         return null;
      }

      return array;

   }


   public static String replaceDots(final String s) {

      char c, c2;
      StringBuffer sb = new StringBuffer(s);
      for (int i = 0; i < sb.length() - 1; i++) {
         c = sb.charAt(i);
         c2 = sb.charAt(i + 1);
         if ((c == '.') && !Character.isDigit(c2)) {
            sb = sb.deleteCharAt(i);
         }
      }

      return sb.toString();

   }

}
