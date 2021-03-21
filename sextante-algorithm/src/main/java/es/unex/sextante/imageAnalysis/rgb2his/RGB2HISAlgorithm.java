package es.unex.sextante.imageAnalysis.rgb2his;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;

public class RGB2HISAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYERR = "LAYERR";
   public static final String LAYERB = "LAYERB";
   public static final String LAYERG = "LAYERG";
   public static final String BANDR  = "BANDR";
   public static final String BANDG  = "BANDG";
   public static final String BANDB  = "BANDB";
   public static final String H      = "H";
   public static final String I      = "I";
   public static final String S      = "S";


   @Override
   public void defineCharacteristics() {

      setName("RGB -> HIS");
      setGroup(Sextante.getText("Image_processing"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(LAYERR, Sextante.getText("R_layer"), true);
         m_Parameters.addBand(BANDR, Sextante.getText("R_band"), LAYERR);
         m_Parameters.addInputRasterLayer("LAYERG", Sextante.getText("G_layer"), true);
         m_Parameters.addBand(BANDG, Sextante.getText("G_band"), LAYERG);
         m_Parameters.addInputRasterLayer(LAYERB, Sextante.getText("B_layer"), true);
         m_Parameters.addBand(BANDB, Sextante.getText("B_band"), LAYERB);
         addOutputRasterLayer(H, "H");
         addOutputRasterLayer(I, "I");
         addOutputRasterLayer(S, "S");
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
      byte r, g, b, i;
      double h, s;
      IRasterLayer hLayer, iLayer, sLayer;

      final IRasterLayer rLayer = m_Parameters.getParameterValueAsRasterLayer(LAYERR);
      final IRasterLayer gLayer = m_Parameters.getParameterValueAsRasterLayer(LAYERG);
      final IRasterLayer bLayer = m_Parameters.getParameterValueAsRasterLayer(LAYERB);
      final int rBand = m_Parameters.getParameterValueAsInt(BANDR);
      final int gBand = m_Parameters.getParameterValueAsInt(BANDG);
      final int bBand = m_Parameters.getParameterValueAsInt(BANDB);

      hLayer = getNewRasterLayer(H, "H", IRasterLayer.RASTER_DATA_TYPE_FLOAT);
      iLayer = getNewRasterLayer(I, "I", IRasterLayer.RASTER_DATA_TYPE_BYTE);
      sLayer = getNewRasterLayer(S, "S", IRasterLayer.RASTER_DATA_TYPE_FLOAT);

      final AnalysisExtent gridExtent = hLayer.getWindowGridExtent();

      rLayer.setWindowExtent(gridExtent);
      gLayer.setWindowExtent(gridExtent);
      bLayer.setWindowExtent(gridExtent);

      iNX = gridExtent.getNX();
      iNY = gridExtent.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            r = rLayer.getCellValueAsByte(x, y, rBand);
            g = gLayer.getCellValueAsByte(x, y, gBand);
            b = bLayer.getCellValueAsByte(x, y, bBand);

            if (rLayer.isNoDataValue(r) || gLayer.isNoDataValue(g) || bLayer.isNoDataValue(b)) {
               hLayer.setNoData(x, y);
               sLayer.setNoData(x, y);
               iLayer.setNoData(x, y);
            }
            else {
               h = (((r - g) + (r - b)) * .5) / Math.sqrt((r - g) * (r - g) + (r - b) * (g - b) + 0.0000000001);
               h = Math.acos(h);
               h = h / Math.PI * 180.;
               if (Double.isNaN(h)) {
                  h = 0;
               }
               if (b > g) {
                  h = 360 - h;
               }
               i = (byte) ((r + g + b) / Math.sqrt(3.));
               if ((r == g) && (g == b)) {
                  s = 0;
               }
               else {
                  s = (float) (1. - Math.sqrt(3) / (i) * Math.min(Math.min(r, g), b));
               }

               hLayer.setCellValue(x, y, h);
               iLayer.setCellValue(x, y, i);
               sLayer.setCellValue(x, y, s);
            }
         }
      }

      return !m_Task.isCanceled();

   }


}
