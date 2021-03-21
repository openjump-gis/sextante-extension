package es.unex.sextante.vegetationIndices.base;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;

public abstract class DistanceBasedAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYERRED  = "LAYERRED";
   public static final String BANDRED   = "BANDRED";
   public static final String LAYERNIR  = "LAYERNIR";
   public static final String BANDNIR   = "BANDNIR";
   public static final String SLOPE     = "SLOPE";
   public static final String INTERCEPT = "INTERCEPT";
   public static final String RESULT    = "RESULT";

   private IRasterLayer       m_LayerRed, m_LayerNIR;

   protected double           m_dNoData;
   protected double           m_dSlope, m_dIntercept;


   @Override
   public void defineCharacteristics() {

      setUserCanDefineAnalysisExtent(true);
      setGroup(Sextante.getText("Vegetation_indices"));
      try {
         m_Parameters.addInputRasterLayer(LAYERRED, Sextante.getText("Red_layer"), true);
         m_Parameters.addBand(BANDRED, Sextante.getText("Red_band"), LAYERRED);
         m_Parameters.addInputRasterLayer(LAYERNIR, Sextante.getText("Near_infrared_layer"), true);
         m_Parameters.addBand(BANDNIR, Sextante.getText("Near_infrared_band"), LAYERNIR);
         m_Parameters.addNumericalValue(SLOPE, Sextante.getText("Soil_line_slope"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1, 0, Double.MAX_VALUE);
         m_Parameters.addNumericalValue(INTERCEPT, Sextante.getText("Soil_line_intercept"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 0, 0, Double.MAX_VALUE);
         addOutputRasterLayer(RESULT, Sextante.getText("Result"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final UndefinedParentParameterNameException e) {
         Sextante.addErrorToLog(e);
      }
      catch (final OptionalParentParameterException e) {
         Sextante.addErrorToLog(e);
      }

   }


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      int x, y;
      int iNX, iNY;
      double dRed;
      double dNIR;
      double dIndex;
      AnalysisExtent extent;

      m_LayerRed = m_Parameters.getParameterValueAsRasterLayer(LAYERRED);
      final int iBandRed = m_Parameters.getParameterValueAsInt(BANDRED);
      m_LayerNIR = m_Parameters.getParameterValueAsRasterLayer(LAYERNIR);
      final int iBandNIR = m_Parameters.getParameterValueAsInt(BANDNIR);

      m_dIntercept = m_Parameters.getParameterValueAsDouble(INTERCEPT);
      m_dSlope = m_Parameters.getParameterValueAsDouble(SLOPE);

      final IRasterLayer result = getNewRasterLayer(RESULT, getName(), IRasterLayer.RASTER_DATA_TYPE_DOUBLE);

      extent = result.getWindowGridExtent();

      m_LayerRed.setWindowExtent(extent);
      m_LayerNIR.setWindowExtent(extent);
      m_LayerRed.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);
      m_LayerNIR.setInterpolationMethod(IRasterLayer.INTERPOLATION_NearestNeighbour);

      iNX = m_LayerRed.getNX();
      iNY = m_LayerRed.getNY();

      m_dNoData = result.getNoDataValue();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            dRed = m_LayerRed.getCellValueAsDouble(x, y, iBandRed);
            dNIR = m_LayerNIR.getCellValueAsDouble(x, y, iBandNIR);
            if (!m_LayerRed.isNoDataValue(dRed) && !m_LayerNIR.isNoDataValue(dNIR)) {
               dIndex = getIndex(dRed, dNIR);
               result.setCellValue(x, y, dIndex);
            }
            else {
               result.setNoData(x, y);
            }
         }
      }

      return !m_Task.isCanceled();

   }


   protected abstract double getIndex(double dRed,
                                      double dNIR);

}
