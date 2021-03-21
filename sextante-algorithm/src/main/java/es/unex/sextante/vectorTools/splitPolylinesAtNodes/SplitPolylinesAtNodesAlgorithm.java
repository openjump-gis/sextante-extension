

package es.unex.sextante.vectorTools.splitPolylinesAtNodes;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

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


public class SplitPolylinesAtNodesAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT = "RESULT";
   public static final String LINES  = "LINES";

   private IVectorLayer       m_Output;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Split_polylines_at_nodes"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LINES, Sextante.getText("Lines"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Lines"), OutputVectorLayer.SHAPE_TYPE_LINE);
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
      final IFeatureIterator iter = lines.iterator();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         for (int j = 0; j < geom.getNumGeometries(); j++) {
            final Geometry line = geom.getGeometryN(j);
            addLine(line, feature.getRecord().getValues());
         }
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   private void addLine(final Geometry line,
                        final Object[] attributes) {

      final GeometryFactory gf = new GeometryFactory();

      final Coordinate[] coords = line.getCoordinates();
      final Coordinate[] segmentCoords = new Coordinate[2];

      for (int i = 0; i < coords.length - 1; i++) {
         segmentCoords[0] = new Coordinate(coords[i].x, coords[i].y);
         segmentCoords[1] = new Coordinate(coords[i + 1].x, coords[i + 1].y);
         m_Output.addFeature(gf.createLineString(segmentCoords), attributes);
      }

   }


}
