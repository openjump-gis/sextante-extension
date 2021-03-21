

package es.unex.sextante.vectorTools.saveToWKT;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.dataObjects.vectorFilters.BoundingBoxFilter;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.IteratorException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;


public class SaveToWKTAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYER    = "LAYER";
   public static final String RESULT   = "RESULT";
   public static final String FILENAME = "FILENAME";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      final IVectorLayer layer = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      final String sFilename = m_Parameters.getParameterValueAsString(FILENAME);

      if (!m_bIsAutoExtent) {
         layer.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      if (sFilename != null) {
         Writer output = null;
         final WKTWriter wkt = new WKTWriter();
         try {
            output = new BufferedWriter(new FileWriter(sFilename));
            final int iShapeCount = layer.getShapesCount();
            int i = 0;
            final IFeatureIterator iter = layer.iterator();
            while (iter.hasNext() && setProgress(i, iShapeCount)) {
               try {
                  final IFeature feature = iter.next();
                  final Geometry geom = feature.getGeometry();
                  output.write("WKT=" + wkt.write(geom) + "\n");
                  final int iFieldCount = feature.getRecord().getValues().length;
                  for (int j = 0; j < iFieldCount; j++) {
                     //output.write(feature.getRecord().getValue(j).toString() + "\n");
                  }
                  i++;
               }
               catch (final IteratorException e) {

               }
            }
         }
         catch (final IOException e) {
            throw new GeoAlgorithmExecutionException(e.getMessage());
         }
         finally {
            if (output != null) {
               try {
                  output.close();
               }
               catch (final IOException e) {
                  Sextante.addErrorToLog(e);
               }
            }
         }

      }

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Save_geometries_as_WKT"));
      setGroup(Sextante.getText("Tools_for_vector_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_ANY, true);
         m_Parameters.addFilepath(FILENAME, Sextante.getText("File"), false, false, "wkt");
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean isSuitableForModelling() {

      return false;

   }

}
