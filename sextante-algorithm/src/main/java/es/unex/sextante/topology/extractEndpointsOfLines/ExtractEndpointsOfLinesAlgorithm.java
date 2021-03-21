

package es.unex.sextante.topology.extractEndpointsOfLines;

import org.locationtech.jts.geom.Coordinate;
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


public class ExtractEndpointsOfLinesAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER  = "LAYER";
   public static final String RESULT = "RESULT";


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Extract_endpoints_of_lines"));
      setGroup(Sextante.getText("Topology"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Input_Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_LINE, true);

         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POINT);

      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i = 0;
      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(ExtractEndpointsOfLinesAlgorithm.LAYER);
      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      final Class[] in_ftypes = layerIn.getFieldTypes();
      final Class[] out_ftypes = new Class[in_ftypes.length + 2];
      System.arraycopy(in_ftypes, 0, out_ftypes, 0, in_ftypes.length);
      out_ftypes[out_ftypes.length - 2] = Integer.class;
      out_ftypes[out_ftypes.length - 1] = Integer.class;

      final String[] in_fnames = layerIn.getFieldNames();
      final String[] out_fnames = new String[in_ftypes.length + 2];
      System.arraycopy(in_fnames, 0, out_fnames, 0, in_fnames.length);
      out_fnames[out_fnames.length - 2] = "LINEFID";
      out_fnames[out_fnames.length - 1] = "ISSTART";

      final IVectorLayer driver = getNewVectorLayer(ExtractEndpointsOfLinesAlgorithm.RESULT, Sextante.getText("Endpoints"),
               OutputVectorLayer.SHAPE_TYPE_POINT, out_ftypes, out_fnames);

      final IFeatureIterator iter = layerIn.iterator();
      final int iTotal = layerIn.getShapesCount();
      final Object[] values = new Object[out_fnames.length];
      int count = 0;
      final GeometryFactory gf = new GeometryFactory();
      while (iter.hasNext() && setProgress(i, iTotal)) {
         final IFeature feature = iter.next();
         final Coordinate[] coords = feature.getGeometry().getCoordinates();
         final Object[] aux_values = feature.getRecord().getValues();
         System.arraycopy(aux_values, 0, values, 0, aux_values.length);
         values[values.length - 2] = count;
         values[values.length - 1] = 0;
         final Point startPoint = gf.createPoint(coords[0]);
         driver.addFeature(startPoint, values);

         count++;

         System.arraycopy(aux_values, 0, values, 0, aux_values.length);
         values[values.length - 2] = count;
         values[values.length - 1] = 1;
         final Point endPoint = gf.createPoint(coords[coords.length - 1]);
         driver.addFeature(endPoint, values);
         setProgress(i, iTotal);
         i++;
      }
      iter.close();

      return !m_Task.isCanceled();

   }

}
