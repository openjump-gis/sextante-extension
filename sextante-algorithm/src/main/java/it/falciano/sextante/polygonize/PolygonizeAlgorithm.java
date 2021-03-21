

package it.falciano.sextante.polygonize;

import java.util.ArrayList;
import java.util.Collection;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import org.locationtech.jts.precision.EnhancedPrecisionOp;

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


public class PolygonizeAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";

   private IVectorLayer       m_Layer;
   private IVectorLayer       m_Output;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Polygonize"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Polylines"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Polygons"), OutputVectorLayer.SHAPE_TYPE_POLYGON);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   @SuppressWarnings("unchecked")
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_Layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         m_Layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final IFeatureIterator iter = m_Layer.iterator();
      int i = 0;
      int k = 0;
      final Collection<LineString> cLineStrings = new ArrayList<LineString>();

      // Converting MultiLineString to Linestring
      while (iter.hasNext()) {
         final IFeature feature = iter.next();
         final Geometry geom = feature.getGeometry();
         if (MultiLineString.class.isAssignableFrom(geom.getClass())) {
            final MultiLineString mls = (MultiLineString) geom;
            final int N = mls.getNumGeometries();
            for (int j = 0; j < N; j++) {
               cLineStrings.add((LineString) mls.getGeometryN(j));
               k++;
            }
         }
         i++;
      }

      try {
         // Noding a set of linestrings
         final GeometryFactory factory = new GeometryFactory();
         final GeometryCollection geometryCollection = (GeometryCollection) factory.buildGeometry(cLineStrings);
         Geometry nodedLineStrings = (geometryCollection).getGeometryN(0);

         // Union of noded linestrings
         for (int j = 1; j < cLineStrings.size(); j++) {
            // here there could be a TopologyException, so I enhance the precision!!!
            final LineString newLineString = ((ArrayList<LineString>) cLineStrings).get(j);
            nodedLineStrings = EnhancedPrecisionOp.union(nodedLineStrings, newLineString);
            setProgress(j, cLineStrings.size());
         }

         // Polygonization
         final Polygonizer polygonizer = new Polygonizer();
         polygonizer.add(nodedLineStrings);
         final Collection<Polygon> cPolygons = polygonizer.getPolygons();

         // Preparing the output(s)
         final Class[] outputFieldTypes = new Class[1];
         final String[] outputFieldNames = new String[1];
         outputFieldTypes[0] = Integer.class;
         outputFieldNames[0] = "ID";

         m_Output = getNewVectorLayer(RESULT, m_Layer.getName(), IVectorLayer.SHAPE_TYPE_POLYGON, outputFieldTypes,
                  outputFieldNames);

         final Polygon[] aPolygons = cPolygons.toArray(new Polygon[cPolygons.size()]);
         final Integer[] outputValues = new Integer[1];

         for (int k1 = 0; k1 < aPolygons.length; k1++) {
            outputValues[0] = k1;
            m_Output.addFeature(aPolygons[k1].getGeometryN(0), outputValues);
         }
      }
      catch (final TopologyException e) {
         Sextante.addErrorToLog(e);
      }

      return !m_Task.isCanceled();

   }

}
