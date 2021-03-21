

package es.unex.sextante.topology.joinAdjacentLines;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.index.strtree.STRtree;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.FeatureImpl;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.outputs.OutputVectorLayer;


public class JoinAdjacentLinesAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER     = "LAYER";
   public static final String TOLERANCE = "TOLERANCE";
   public static final String RESULT    = "RESULT";
   private double             m_dTolerance;


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Join_near_lines"));
      setGroup(Sextante.getText("Tools_for_line_layers"));
      setIsDeterminatedProcess(false);
      setUserCanDefineAnalysisExtent(true);
      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Input_Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);

         m_Parameters.addNumericalValue(TOLERANCE, Sextante.getText("Tolerance"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 5.0, 0.0001, Double.MAX_VALUE);

         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_LINE);

      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      m_dTolerance = m_Parameters.getParameterValueAsDouble(TOLERANCE);

      int idx = 0;
      final int iTotal = layerIn.getShapesCount();
      final IFeatureIterator iter = layerIn.iterator();
      IFeature[] newFeatures = new IFeature[iTotal];
      while (iter.hasNext()) {
         newFeatures[idx] = iter.next();
         idx++;
      }

      IFeature[] features;
      int i = 1;
      setProgressText(Sextante.getText("Iteration") + " 1");
      do {
         features = newFeatures;
         newFeatures = joinLines(features);
         i++;
         setProgressText(Sextante.getText("Iteration") + " " + Integer.toString(i) + "("
                         + Integer.toString(features.length - newFeatures.length) + " " + Sextante.getText("lines_joined"));
      }
      while ((newFeatures.length != features.length) && !m_Task.isCanceled());

      final IVectorLayer out = getNewVectorLayer(RESULT, layerIn.getName(), OutputVectorLayer.SHAPE_TYPE_LINE,
               layerIn.getFieldTypes(), layerIn.getFieldNames());
      for (final IFeature element : features) {
         out.addFeature(element);
      }

      return !m_Task.isCanceled();

   }


   private IFeature[] joinLines(final IFeature[] features) {

      final GeometryFactory gf = new GeometryFactory();
      final ArrayList<IFeature> newFeatures = new ArrayList<IFeature>();
      final STRtree tree = new STRtree();
      final BitSet bitset = new BitSet(features.length);

      for (int i = 0; i < features.length; i++) {
         final Envelope envelope = features[i].getGeometry().getEnvelopeInternal();
         tree.insert(envelope, new Integer(i));
      }

      Coordinate[] coords2;
      for (int i = 0; i < features.length; i++) {
         if (!bitset.get(i)) {
            final Geometry geom = features[i].getGeometry();
            final Coordinate[] coords = geom.getCoordinates();
            final Envelope envelope = features[i].getGeometry().getEnvelopeInternal();
            envelope.expandBy(m_dTolerance);
            final List geoms = tree.query(envelope);
            boolean bFound = false;
            bitset.set(i);
            for (int j = 0; j < geoms.size(); j++) {
               final int idx = ((Integer) geoms.get(j)).intValue();
               if ((i != idx) && !bitset.get(idx)) {
                  final Geometry geom2 = features[idx].getGeometry();
                  coords2 = geom2.getCoordinates();
                  if (coords2[0].distance(coords[coords.length - 1]) < m_dTolerance) {
                     Coordinate[] newCoords = new Coordinate[coords.length + coords2.length];
                     System.arraycopy(coords, 0, newCoords, 0, coords.length);
                     System.arraycopy(coords2, 0, newCoords, coords.length, coords2.length);
                     final FeatureImpl newFeature = new FeatureImpl(gf.createLineString(newCoords),
                              features[i].getRecord().getValues());
                     newFeatures.add(newFeature);
                     newCoords = null;
                     bFound = true;
                     bitset.set(idx);
                     break;
                  }
                  else if (coords2[coords2.length - 1].distance(coords[0]) < m_dTolerance) {
                     Coordinate[] newCoords = new Coordinate[coords.length + coords2.length];
                     System.arraycopy(coords2, 0, newCoords, 0, coords2.length);
                     System.arraycopy(coords, 0, newCoords, coords2.length, coords.length);
                     final FeatureImpl newFeature = new FeatureImpl(gf.createLineString(newCoords),
                              features[i].getRecord().getValues());
                     newFeatures.add(newFeature);
                     newCoords = null;
                     bFound = true;
                     bitset.set(idx);
                     break;
                  }
                  else if (coords2[0].distance(coords[0]) < m_dTolerance) {
                     Coordinate[] newCoords = new Coordinate[coords.length + coords2.length];
                     for (int k = 0; k < coords.length; k++) {
                        newCoords[k] = coords[coords.length - 1 - k];
                     }
                     System.arraycopy(coords2, 0, newCoords, coords.length, coords2.length);
                     final FeatureImpl newFeature = new FeatureImpl(gf.createLineString(newCoords),
                              features[i].getRecord().getValues());
                     newFeatures.add(newFeature);
                     newCoords = null;
                     bFound = true;
                     bitset.set(idx);
                     break;
                  }
                  else if (coords2[coords2.length - 1].distance(coords[coords.length - 1]) < m_dTolerance) {
                     Coordinate[] newCoords = new Coordinate[coords.length + coords2.length];
                     System.arraycopy(coords, 0, newCoords, 0, coords.length);
                     for (int k = 0; k < coords2.length; k++) {
                        newCoords[k + coords.length] = coords2[coords2.length - 1 - k];
                     }

                     final FeatureImpl newFeature = new FeatureImpl(gf.createLineString(newCoords),
                              features[i].getRecord().getValues());
                     newFeatures.add(newFeature);
                     newCoords = null;
                     bFound = true;
                     bitset.set(idx);
                     break;
                  }
               }
            }
            if (!bFound) {
               newFeatures.add(features[i]);
               System.out.println(i + " " + coords.length);
            }
         }
      }

      System.gc();

      return newFeatures.toArray(new IFeature[0]);

   }

   /**
    * Create nodes, increment its degree and add connectedline.
    * 
    * @param endpoints
    * @param point
    * @param count
    * @param feat
    */
   //	public void processEndpoint(HashMap endpoints, Geometry point, int count, IFeature feat, int featIdx){
   //
   //		if (!endpoints.containsKey(point.toText())) {
   //			Node node = new Node(count, point, feat);
   //			node.addConnectedLine(feat, featIdx);
   //			endpoints.put(point.toText(), node);
   //
   //		} else {
   //			//TODO What about SELF-LINE-NODES?
   //			Node ep = (Node)endpoints.get(point.toText());
   //			ep.incrementDegree();
   //			ep.addConnectedLine(feat, featIdx);
   //		}
   //
   //	}
   //
   //	private Geometry changeEndpointVertex(Geometry lineGeom, Geometry currentVertex,
   //			Geometry newVertex) {
   //
   //		// Detect if add the new vertex as first or last point
   //		Coordinate[] coords = lineGeom.getCoordinates();
   //		if (coords[0].equals2D(currentVertex.getCoordinate())) {
   //			coords[0] = newVertex.getCoordinate();
   //		} else {
   //			coords[coords.length-1] = newVertex.getCoordinate();
   //		}
   //		GeometryFactory gf = new GeometryFactory();
   //		return gf.createLineString(coords);
   //	}
   //
   //	public boolean processAlgorithm() throws GeoAlgorithmExecutionException {
   //
   //		HashMap nodesHashMap = new HashMap();
   //		GeometryFactory gf = new GeometryFactory();
   //
   //		IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(this.LAYER);
   //		double tolerance = m_Parameters.getParameterValueAsDouble(this.TOLERANCE);
   //
   //		// STEP 01 - Get Nodes
   //		int count = 0;
   //		int idx = 0;
   //		int iTotal = layerIn.getShapesCount();
   //		IFeatureIterator iter = layerIn.iterator();
   //		IFeature[] featArray = new IFeature[iTotal];
   //		Geometry[] geomArray = new Geometry[iTotal];
   //		while (iter.hasNext() && setProgress(idx, iTotal)){
   //			IFeature feature = iter.next();
   //			featArray[idx] = feature;
   //			geomArray[idx] = feature.getGeometry();
   //			idx++;
   //		}
   //
   //		idx= 0;
   //		nodesHashMap.clear();
   //		while (idx < featArray.length && setProgress(count, iTotal)){
   //			IFeature feature = featArray[idx];
   //			Coordinate[] coords = geomArray[idx].getCoordinates();
   //
   //			Point point = gf.createPoint(coords[0]);
   //			processEndpoint(nodesHashMap, point, count, feature, idx);
   //			count++;
   //			point = gf.createPoint(coords[coords.length-1]);
   //			processEndpoint(nodesHashMap, point, count, feature, idx);
   //
   //			setProgress(idx, iTotal);
   //			idx++;
   //		}
   //		iter.close();
   //
   //		//STEP 02 - Get close endpoints
   //		Collection nodes = nodesHashMap.values();
   //		int i = 0;
   //		double dist;
   //		for (Iterator it1 = nodes.iterator(); it1.hasNext(); i++){
   //			Node node1 = (Node)it1.next();
   //			int j = 0;
   //			for (Iterator it2 = nodes.iterator(); it2.hasNext(); j++){
   //				if (i == j){
   //					continue;
   //				}
   //				Node node2 = (Node)it2.next();
   //				node1.processIfClose(node2, tolerance);
   //			}
   //		}
   //
   //		//STEP 03 - For simple cases when degree 1 and just one closeNode
   //		//          Get closest and modified
   //		IVectorLayer driver2 = getNewVectorLayer(this.RESULT,
   //				Sextante.getText("Result"),
   //				OutputVectorLayer.SHAPE_TYPE_LINE,
   //				layerIn.getFieldTypes(),
   //				layerIn.getFieldNames());
   //
   //		ArrayList removedNodes = new ArrayList();
   //		for (Iterator it1 = nodes.iterator(); it1.hasNext(); i++){
   //			Node node1 = (Node)it1.next();
   //			if (removedNodes.contains(node1)){
   //				continue;
   //			}
   //			//TODO It must works with any closeNodes number. Find the one with more grade and the closest one
   //			if (node1.getDegree() == 1 && node1.getCloseNodes().size() == 1){
   //				//IFeature line = (IFeature)node1.getConnectedLines().toArray()[0];
   //				int lineIdx = (Integer) node1.getConnectedLinesIdx().toArray()[0];
   //				Geometry lineGeom = geomArray[lineIdx];
   //				//IRecord record = line.getRecord();
   //				Node closeNode = (Node)node1.getCloseNodes().toArray()[0];
   //				Geometry currentEndpointGeom = node1.getGeometry();
   //				Geometry newEndpointGeom = closeNode.getGeometry();
   //				Geometry newLineGeom = changeEndpointVertex(lineGeom, currentEndpointGeom, newEndpointGeom);
   //				geomArray[lineIdx] = newLineGeom;
   //				removedNodes.add(closeNode);
   //			}
   //		}
   //
   //		iter = layerIn.iterator();
   //		for (idx = 0; iter.hasNext(); idx++){
   //			IFeature feat = iter.next();
   //			driver2.addFeature(geomArray[idx], feat.getRecord().getValues());
   //		}
   //		iter.close();
   //
   //		return !m_Task.isCanceled();
   //	}
   //
}
