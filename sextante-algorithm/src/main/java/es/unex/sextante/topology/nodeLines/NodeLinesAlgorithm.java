

package es.unex.sextante.topology.nodeLines;

import org.locationtech.jts.geom.Geometry;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;


public class NodeLinesAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT = "RESULT";
   public static final String LINES  = "LINES";

   private IVectorLayer       m_Output;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Node_lines"));
      setGroup(Sextante.getText("Topology"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LINES, Sextante.getText("Lines"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Lines"), OutputVectorLayer.SHAPE_TYPE_LINE, LINES);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer lines = m_Parameters.getParameterValueAsVectorLayer(LINES);
      if (!m_bIsAutoExtent) {
         lines.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      m_Output = getNewVectorLayer(RESULT, Sextante.getText("Result"), IVectorLayer.SHAPE_TYPE_LINE, lines.getFieldTypes(),
               lines.getFieldNames());

      int i = 0;
      final int iShapeCount = lines.getShapesCount();
      IFeatureIterator iter = lines.iterator();
      IFeature feature = iter.next();
      Geometry geom = feature.getGeometry();
      final Geometry merged = geom;
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         feature = iter.next();
         geom = feature.getGeometry();
         merged.union(geom);
         i++;
      }
      iter.close();

      i = 0;
      iter = lines.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         feature = iter.next();
         m_Output.addFeature(merged.getGeometryN(i), feature.getRecord().getValues());
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }

}
