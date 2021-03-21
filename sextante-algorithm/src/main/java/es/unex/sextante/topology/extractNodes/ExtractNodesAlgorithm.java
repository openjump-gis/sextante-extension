

package es.unex.sextante.topology.extractNodes;

import java.util.HashMap;
import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

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


public class ExtractNodesAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Extract_Nodes"));
      setGroup(Sextante.getText("Topology"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Capa de entrada"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE,
                  true);

         addOutputVectorLayer(RESULT, Sextante.getText("Nodes"), OutputVectorLayer.SHAPE_TYPE_POINT);

      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i = 0;
      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(ExtractNodesAlgorithm.LAYER);
      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final Class[] out_ftypes = new Class[1];
      out_ftypes[0] = Integer.class;

      final String[] out_fnames = new String[1];
      out_fnames[0] = "DEGREE";

      final IVectorLayer driver = getNewVectorLayer(ExtractNodesAlgorithm.RESULT, Sextante.getText("Nodes"),
               OutputVectorLayer.SHAPE_TYPE_POINT, out_ftypes, out_fnames);


      final HashMap nodesHashMap = new HashMap();
      final GeometryFactory gf = new GeometryFactory();

      // STEP 01 - Get Nodes
      int count = 0;
      int idx = 0;
      final int iTotal = layerIn.getShapesCount();
      final IFeatureIterator iter = layerIn.iterator();

      idx = 0;
      while (iter.hasNext() && setProgress(count, iTotal)) {
         final IFeature feature = iter.next();
         final Coordinate[] coords = feature.getGeometry().getCoordinates();

         Point point = gf.createPoint(coords[0]);
         processNode(nodesHashMap, point, count, feature, idx);
         count++;
         point = gf.createPoint(coords[coords.length - 1]);
         processNode(nodesHashMap, point, count, feature, idx);

         setProgress(count, iTotal);
         idx++;
      }
      iter.close();

      final Iterator nodesIter = nodesHashMap.values().iterator();
      while (nodesIter.hasNext() && setProgress(i, iTotal)) {
         final Node n = (Node) nodesIter.next();
         final Geometry g = n.getGeometry();
         final Object[] values = { n.getDegree() };
         driver.addFeature(g, values);
         setProgress(i, iTotal);
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }


   public void processNode(final HashMap endpoints,
                           final Geometry point,
                           final int count,
                           final IFeature feat,
                           final int featIdx) {

      if (!endpoints.containsKey(point.toText())) {
         final Node node = new Node(count, point, feat);
         node.addConnectedLine(feat, featIdx);
         endpoints.put(point.toText(), node);

      }
      else {
         //TODO What about SELF-LINE-NODES?
         final Node ep = (Node) endpoints.get(point.toText());
         ep.incrementDegree();
         ep.addConnectedLine(feat, featIdx);
      }
   }

}
