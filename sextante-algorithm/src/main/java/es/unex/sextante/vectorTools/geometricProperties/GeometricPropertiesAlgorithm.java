

package es.unex.sextante.vectorTools.geometricProperties;


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
import es.unex.sextante.outputs.IOutputChannel;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.shapesTools.ShapesTools;


public class GeometricPropertiesAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT = "RESULT";
   public static final String LAYER  = "LAYER";


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      final Class[] types = { Double.class, Double.class, Double.class, Double.class, Double.class, Double.class, Double.class,
               Double.class, Double.class };
      AreaAndPerimeter ap;
      final String[] sFields = { Sextante.getText("AREA"), Sextante.getText("PERIMETER"), "THICK", "APRel", "APRel2", "QDR",
               "FD", "RC", "SHAPE" };

      final IVectorLayer layerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         layerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }
      final int iShapesCount = layerIn.getShapesCount();
      final Double[][] values = new Double[9][iShapesCount];
      final IFeatureIterator iter = layerIn.iterator();
      i = 0;
      while (iter.hasNext() && setProgress(i, iShapesCount)) {
         final IFeature feature = iter.next();
         ap = getAreaAndPerimeter(feature.getGeometry());
         values[0][i] = new Double(ap.dArea);
         values[1][i] = new Double(ap.dPerimeter);
         values[2][i] = new Double(getThickness(ap));
         values[3][i] = new Double(getAPRelation(ap));
         values[4][i] = new Double(getAPRelation2(ap));
         values[5][i] = new Double(getQDR(ap));
         values[6][i] = new Double(getFD(ap));
         values[7][i] = new Double(getRC(ap));
         values[8][i] = new Double(getSHAPE(ap));
         i++;
      }
      final IOutputChannel channel = getOutputChannel(RESULT);
      final OutputVectorLayer out = new OutputVectorLayer();
      out.setName(RESULT);
      out.setOutputChannel(channel);
      out.setDescription(layerIn.getName());
      out.setOutputObject(ShapesTools.addFields(m_OutputFactory, layerIn, channel, sFields, values, types));
      addOutputObject(out);

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Geometric_properties_of_polygons"));
      setGroup(Sextante.getText("Tools_for_polygon_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Polygons"), AdditionalInfoVectorLayer.SHAPE_TYPE_POLYGON, true);
         addOutputVectorLayer(RESULT, Sextante.getText("Polygons"), OutputVectorLayer.SHAPE_TYPE_POLYGON, LAYER);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private AreaAndPerimeter getAreaAndPerimeter(final Geometry geom) {

      final AreaAndPerimeter ap = new AreaAndPerimeter();
      ap.dArea = geom.getArea();
      ap.dPerimeter = geom.getLength();
      return ap;

   }


   private double getThickness(final AreaAndPerimeter ap) {
      double thickness;
      thickness = (4 * Math.PI * ap.dArea) / Math.pow(ap.dPerimeter, 2);
      return thickness;
   }


   private double getAPRelation(final AreaAndPerimeter ap) {
      double APR;
      APR = Math.pow(ap.dPerimeter, 2) / ap.dArea;
      return APR;
   }


   private double getAPRelation2(final AreaAndPerimeter ap) {
      double APR2;
      APR2 = (5 * ap.dPerimeter) / ap.dArea;
      return APR2;
   }


   private double getQDR(final AreaAndPerimeter ap) {
      double QDR;
      QDR = (16 * ap.dArea) / Math.pow(ap.dPerimeter, 2);
      return QDR;
   }


   private double getRC(final AreaAndPerimeter ap) {
      double RC;
      RC = Math.pow((4 * Math.PI * ap.dArea) / Math.pow(ap.dPerimeter, 2), 0.5);
      return RC;
   }


   private double getFD(final AreaAndPerimeter ap) {

      double FD;
      FD = (2 * Math.log(ap.dPerimeter)) / Math.log(ap.dArea);
      return FD;
   }


   private double getSHAPE(final AreaAndPerimeter ap) {

      double SHAPE;
      SHAPE = ap.dPerimeter / (2 * (Math.sqrt(ap.dArea * Math.PI)));
      return SHAPE;
   }

   private class AreaAndPerimeter {

      public double dArea, dPerimeter;

   }


}
