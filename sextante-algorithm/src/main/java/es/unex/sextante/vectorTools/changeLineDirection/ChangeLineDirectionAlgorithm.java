

package es.unex.sextante.vectorTools.changeLineDirection;

import java.util.ArrayList;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

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


public class ChangeLineDirectionAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT = "RESULT";
   public static final String LINES  = "LINES";

   private IVectorLayer       m_Output;
   private ArrayList          m_Lines;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Change_line_direction"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
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
      final IFeatureIterator iter = lines.iterator();
      final GeometryFactory gf = new GeometryFactory();
      while (iter.hasNext() && setProgress(i, iShapeCount)) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         m_Lines = new ArrayList();
         for (int j = 0; j < geom.getNumGeometries(); j++) {
            final Geometry line = geom.getGeometryN(j);
            addLine(line);
         }
         final LineString[] lineStrings = new LineString[m_Lines.size()];
         for (int j = 0; j < lineStrings.length; j++) {
            lineStrings[j] = (LineString) m_Lines.get(j);
         }
         m_Output.addFeature(gf.createMultiLineString(lineStrings), feature.getRecord().getValues());
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   private void addLine(final Geometry line) {

      final Coordinate[] coords = line.getCoordinates();
      final Coordinate[] newCoords = new Coordinate[coords.length];
      for (int i = 0; i < coords.length; i++) {
         newCoords[i] = coords[coords.length - i - 1];
      }
      final GeometryFactory gf = new GeometryFactory();
      m_Lines.add(gf.createLineString(newCoords));

   }


}
