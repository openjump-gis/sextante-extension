package es.unex.sextante.tables.vectorFieldCorrelation;

import java.text.DecimalFormat;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.additionalResults.CorrelationGraphCreator;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;
import es.unex.sextante.math.regression.Regression;

public class VectorFieldCorrelationAlgorithm
         extends
            GeoAlgorithm {

   public static final String FIELD2          = "FIELD2";
   public static final String FIELD           = "FIELD";
   public static final String LAYER           = "LAYER";
   public static final String METHOD          = "METHOD";
   public static final String REGRESSION      = "REGRESSION";
   public static final String REGRESSION_DATA = "REGRESSION_DATA";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      int iCount;
      int iField, iField2;
      double dValue, dValue2;
      IVectorLayer layer;
      final Regression regression = new Regression();

      final int iType = m_Parameters.getParameterValueAsInt(METHOD);
      layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      iField = m_Parameters.getParameterValueAsInt(FIELD);
      iField2 = m_Parameters.getParameterValueAsInt(FIELD2);

      i = 0;
      iCount = layer.getShapesCount();
      final IFeatureIterator iter = layer.iterator();
      while (iter.hasNext() && setProgress(i, iCount)) {
         final IFeature feature = iter.next();
         try {
            dValue = Double.parseDouble(feature.getRecord().getValue(iField).toString());
            dValue2 = Double.parseDouble(feature.getRecord().getValue(iField2).toString());
            regression.addValue(dValue, dValue2);
         }
         catch (final Exception e) {}
         i++;
      }
      iter.close();

      regression.calculate(iType);
      final CorrelationGraphCreator panel = new CorrelationGraphCreator(regression);

      addOutputChart(REGRESSION, Sextante.getText("Regression"), panel.getChartPanel());

      final DecimalFormat df = new DecimalFormat("####.###");
      final HTMLDoc doc = new HTMLDoc();
      doc.open(Sextante.getText("Regression"));
      doc.addHeader(Sextante.getText("Regression"), 2);
      doc.startUnorderedList();
      doc.addListElement(regression.getExpression());
      doc.addListElement("R� = " + df.format(regression.getR2()));
      doc.closeUnorderedList();
      doc.close();

      addOutputText(REGRESSION_DATA, Sextante.getText("Regression_values"), doc.getHTMLCode());

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      final String sMethod[] = { Sextante.getText("Best_fit"), "y = a + b * x", "y = a + b / x", "y = a / (b - x)",
               "y = a * x^b", "y = a e^(b * x)", "y = a + b * ln(x)" };

      setName(Sextante.getText("Correlation_between_fields"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(false);
      try {
         m_Parameters.addInputVectorLayer("LAYER", Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), LAYER);
         m_Parameters.addTableField(FIELD2, Sextante.getText("Field") + "2", LAYER);
         m_Parameters.addSelection(METHOD, Sextante.getText("Equation"), sMethod);
         addOutputChart(REGRESSION, Sextante.getText("Regression"));
         addOutputText(REGRESSION_DATA, Sextante.getText("Regression_values"));
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
