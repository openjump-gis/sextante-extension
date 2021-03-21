

package es.unex.sextante.vectorTools.vectorSpatialCluster;

import java.util.Arrays;

import org.locationtech.jts.geom.Coordinate;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
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
import es.unex.sextante.outputs.Output;
import es.unex.sextante.outputs.OutputVectorLayer;
import es.unex.sextante.shapesTools.ShapesTools;


public class VectorSpatialClusterAlgorithm
         extends
            GeoAlgorithm {

   public static final String RESULT        = "RESULT";
   public static final String NUMCLASS      = "NUMCLASS";
   public static final String LAYER         = "LAYER";

   private ValueAndClass[]    m_Classes;
   private double             m_dMean[][];
   private int                m_iClasses;
   private int                m_iThreshold;
   private IVectorLayer       m_LayerIn;

   private static double      NO_DATA       = Double.NEGATIVE_INFINITY;
   private static int         NO_DATA_CLASS = Integer.MAX_VALUE;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int i;
      final Class[] types = { Integer.class };
      final String[] sFields = { Sextante.getText("Class") };

      m_iClasses = m_Parameters.getParameterValueAsInt(NUMCLASS);
      m_LayerIn = m_Parameters.getParameterValueAsVectorLayer(LAYER);
      if (!m_bIsAutoExtent) {
         m_LayerIn.addFilter(new BoundingBoxFilter(m_AnalysisExtent));
      }

      m_Classes = new ValueAndClass[m_LayerIn.getShapesCount()];
      final IFeatureIterator iter = m_LayerIn.iterator();
      i = 0;
      while (iter.hasNext()) {
         final IFeature feature = iter.next();
         final Coordinate coord = feature.getGeometry().getCoordinate();
         m_Classes[i] = new ValueAndClass(2);
         m_Classes[i].dValue[0] = coord.x;
         m_Classes[i].dValue[1] = coord.y;
         i++;
      }

      classify();

      final Object[][] values = new Object[1][m_LayerIn.getShapesCount()];
      for (i = 0; i < m_Classes.length; i++) {
         values[0][i] = new Integer(m_Classes[i].iClass);
      }
      final IOutputChannel channel = getOutputChannel(RESULT);
      final Output out = new OutputVectorLayer();
      out.setDescription(Sextante.getText("Result"));
      out.setName(RESULT);
      out.setOutputChannel(channel);
      out.setOutputObject(ShapesTools.addFields(m_OutputFactory, m_LayerIn, channel, sFields, values, types));
      addOutputObject(out);

      return !m_Task.isCanceled();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Spatial_cluster"));
      setGroup(Sextante.getText("Tools_for_point_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputVectorLayer(LAYER, Sextante.getText("Layer"), AdditionalInfoVectorLayer.SHAPE_TYPE_POINT, true);
         m_Parameters.addNumericalValue(NUMCLASS, Sextante.getText("Number_of_classes"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_INTEGER, 3, 2, Integer.MAX_VALUE);
         addOutputVectorLayer(RESULT, Sextante.getText("Result"), OutputVectorLayer.SHAPE_TYPE_POINT);
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void initValues() {

      int i;
      int iValues = 0;
      boolean bNoData;
      double dStep;
      double dValue;
      final double dMin[] = new double[2];
      final double dMax[] = new double[2];

      for (i = 0; i < 2; i++) {
         dMin[i] = Double.MAX_VALUE;
         dMax[i] = Double.NEGATIVE_INFINITY;
      }

      for (i = 0; i < m_Classes.length; i++) {
         bNoData = false;
         for (int j = 0; j < m_Classes[i].dValue.length; j++) {
            dValue = m_Classes[i].dValue[j];
            if (dValue != NO_DATA) {
               dMin[j] = Math.min(dMin[j], dValue);
               dMax[j] = Math.max(dMax[j], dValue);
            }
            else {
               bNoData = true;
            }
         }
         if (bNoData) {
            m_Classes[i].iClass = NO_DATA_CLASS;
         }
         else {
            iValues++;
            m_Classes[i].iClass = 0;
         }
      }

      m_dMean = new double[m_iClasses][2];

      for (i = 0; i < 2; i++) {
         dStep = (dMax[i] - dMin[i]) / ((m_iClasses + 1));
         for (int j = 0; j < m_iClasses; j++) {
            m_dMean[j][i] = dMin[i] + dStep * (j + 1);
         }
      }

      m_iThreshold = (int) (iValues * 0.02);

   }


   private boolean classify() {

      int i, j;
      int iChangedCells;
      int iPrevClass;
      int iClass;
      final int iCells[] = new int[m_iClasses];
      double dNewMean[][];
      double swap[][];

      initValues();

      dNewMean = new double[m_iClasses][2];

      do {
         Arrays.fill(iCells, 0);
         iChangedCells = 0;

         for (i = 0; i < m_iClasses; i++) {
            Arrays.fill(dNewMean[i], 0.0);
         }
         for (i = 0; i < m_Classes.length; i++) {
            iPrevClass = m_Classes[i].iClass;
            if (iPrevClass != NO_DATA_CLASS) {
               iClass = getClass(m_Classes[i].dValue);
               m_Classes[i].iClass = iClass;
               for (j = 0; j < 2; j++) {
                  dNewMean[iClass][j] += m_Classes[i].dValue[j];
               }
               iCells[iClass]++;
               if (iClass != iPrevClass) {
                  iChangedCells++;
               }
            }
         }

         for (i = 0; i < 2; i++) {
            for (j = 0; j < m_iClasses; j++) {
               dNewMean[j][i] /= iCells[j];
            }
         }

         swap = m_dMean;
         m_dMean = dNewMean;
         dNewMean = swap;

         setProgressText(Sextante.getText("Modified_classes") + Integer.toString(iChangedCells));

         if (m_Task.isCanceled()) {
            return false;
         }

      }
      while (iChangedCells > m_iThreshold);

      return true;

   }


   private int getClass(final double[] dValues) {

      int iClass = 0;
      double dMinDist = Double.MAX_VALUE;
      double dDist;
      double dDif;

      for (int i = 0; i < m_iClasses; i++) {
         dDist = 0;
         for (int j = 0; j < dValues.length; j++) {
            dDif = m_dMean[i][j] - dValues[j];
            dDist += (dDif * dDif);
         }
         if (dDist < dMinDist) {
            dMinDist = dDist;
            iClass = i;
         }
      }

      return iClass;
   }

   private class ValueAndClass {

      public double dValue[];
      public int    iClass;


      public ValueAndClass(final int i) {

         dValue = new double[i];

      }

   }


}
