

package es.unex.sextante.tables.normalityTest;

import java.text.DecimalFormat;
import java.util.ArrayList;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;


public class NormalityTestAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT = "RESULT";
   public static final String FIELD  = "FIELD";
   public static final String LAYER  = "LAYER";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iField;
      int iCount;
      double dValue;
      IVectorLayer layer;
      final ArrayList<Double> array = new ArrayList<Double>();

      layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      iField = m_Parameters.getParameterValueAsInt(FIELD);

      i = 0;
      iCount = layer.getShapesCount();
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(i, iCount)) {
         final IFeature feature = iter.next();
         try {
            dValue = Double.parseDouble(feature.getRecord().getValue(iField).toString());
            array.add(new Double(dValue));
         }
         catch (final Exception e) {
         }
         i++;
      }
      iter.close();

      if (array.size() < 3) {
         throw new GeoAlgorithmExecutionException(Sextante.getText("At_least_three_features_are_needed_for_using_this_algorithm"));
      }

      if (array.size() > 5000) {
         Sextante.addWarningToLog("Mas_de_5000puntos_shapiro");
      }

      final double[] values = new double[array.size() + 1];
      for (int j = 0; j < array.size(); j++) {
         values[j + 1] = array.get(j).doubleValue();
      }

      final double[] pw = new double[1];
      final double[] w = new double[1];
      final double[] a = new double[array.size() / 2];
      final int n = Math.min(5000, array.size());
      final boolean[] init = new boolean[1];
      init[0] = false;
      final int[] ifault = new int[] { -1 };

      SWilk.swilk(init, values, n, n, n / 2, a, w, pw, ifault);

      if ((ifault[0] != 0) && (ifault[0] != 2)) {
         throw new GeoAlgorithmExecutionException("Error_calculando_shapiro");
      }


      if (m_Task.isCanceled()) {
         return false;
      }
      else {
         final DecimalFormat df = new DecimalFormat("##.###");
         final HTMLDoc doc = new HTMLDoc();
         doc.open(Sextante.getText("Normality_test"));
         doc.addHeader(Sextante.getText("Normality_test"), 2);
         doc.startUnorderedList();
         doc.addListElement(Sextante.getText("Shapiro-Wilk_W") + df.format(w[0]));
         doc.closeUnorderedList();
         doc.close();

         addOutputText(RESULT, Sextante.getText("Statistics") + "[" + layer.getName() + "]", doc.getHTMLCode());
         return true;
      }

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Normality_test"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);
      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), "LAYER");
         addOutputText(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final UndefinedParentParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final OptionalParentParameterException e) {
         Sextante.addErrorToLog(e);
      }

   }

}
