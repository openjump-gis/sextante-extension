package es.unex.sextante.tables.vectorSplitField;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.shapesTools.ShapesTools;


public class VectorSplitFieldAlgorithm
         extends
            GeoAlgorithm {

   private static final Object NO_DATA     = new Double(-99999.);

   public static final String  FIELD       = "FIELD";
   public static final String  BREAKPOINTS = "BREAKPOINTS";
   public static final String  LAYER       = "LAYER";
   public static final String  RESULT      = "RESULT";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final int i, j;
      final double dValue;
      final boolean bOK;
      final String sVariable;

      final IVectorLayer layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final int iField = m_Parameters.getParameterValueAsInt(FIELD);
      final String sBreakpoints = m_Parameters.getParameterValueAsString(BREAKPOINTS);
      final String[] sTokens = sBreakpoints.split(",");

      final int[] breakpoints = new int[sTokens.length + 2];
      breakpoints[0] = 0;
      breakpoints[breakpoints.length - 1] = 1000;
      for (int k = 0; k < sTokens.length; k++) {
         breakpoints[k + 1] = Integer.parseInt(sTokens[k]);
      }

      final Class[] types = new Class[sTokens.length + 1];
      for (int k = 0; k < types.length; k++) {
         types[k] = String.class;
      }

      final String sBaseName = layer.getFieldName(iField);
      final String[] sFields = new String[sTokens.length + 1];
      for (int k = 0; k < sFields.length; k++) {
         sFields[k] = sBaseName + "_" + Integer.toString(k);
      }


      final int iTotalCount = layer.getShapesCount();
      final String[][] values = new String[breakpoints.length][iTotalCount];
      final IFeatureIterator iter = layer.iterator();
      IFeature feat;
      int iCount = 0;
      while (iter.hasNext() && setProgress(iCount, iTotalCount)) {
         feat = iter.next();
         final String s = feat.getRecord().getValue(iField).toString();
         System.out.println(s);
         for (int k = 1; k < breakpoints.length; k++) {
            final int iMin = Math.min(breakpoints[k - 1], s.length());
            final int iMin2 = Math.min(breakpoints[k], s.length());
            values[k - 1][iCount] = s.substring(iMin, iMin2);
         }
         iCount++;
      }


      final IOutputChannel channel = getOutputChannel(RESULT);
      final OutputVectorLayer out = new OutputVectorLayer();
      out.setName(RESULT);
      out.setOutputChannel(channel);
      out.setDescription(Sextante.getText("Result"));
      out.setOutputObject(ShapesTools.addFields(m_OutputFactory, layer, channel, sFields, values, types));
      addOutputObject(out);


      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Split_field"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addString(BREAKPOINTS, Sextante.getText("Breakpoints"));
         m_Parameters.addTableField(FIELD, Sextante.getText("Field"), LAYER);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }
   }


}
