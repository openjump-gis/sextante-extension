package es.unex.sextante.imageAnalysis.his2rgb;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.OptionalParentParameterException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;
import es.unex.sextante.exceptions.UndefinedParentParameterNameException;

public class HIS2RGBAlgorithm
         extends
            GeoAlgorithm {

   public static final String LAYERH = "LAYERH";
   public static final String LAYERI = "LAYERI";
   public static final String LAYERS = "LAYERS";
   public static final String BANDH  = "BANDH";
   public static final String BANDI  = "BANDI";
   public static final String BANDS  = "BANDS";
   public static final String R      = "R";
   public static final String G      = "G";
   public static final String B      = "B";


   @Override
   public void defineCharacteristics() {

      setName("HIS -> RGB");
      setGroup(Sextante.getText("Image_processing"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(LAYERH, Sextante.getText("H_layer"), true);
         m_Parameters.addBand(BANDH, Sextante.getText("H_band"), LAYERH);
         m_Parameters.addInputRasterLayer("LAYERI", Sextante.getText("I_layer"), true);
         m_Parameters.addBand(BANDI, Sextante.getText("I_band"), LAYERI);
         m_Parameters.addInputRasterLayer("LAYERS", Sextante.getText("S_layer"), true);
         m_Parameters.addBand(BANDS, Sextante.getText("S_band"), LAYERS);
         addOutputRasterLayer(R, "R");
         addOutputRasterLayer(G, "G");
         addOutputRasterLayer(B, "B");
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
      byte r, g, b, i, s;
      int h;
      IRasterLayer rLayer, gLayer, bLayer;

      final IRasterLayer hLayer = m_Parameters.getParameterValueAsRasterLayer(LAYERH);
      final IRasterLayer iLayer = m_Parameters.getParameterValueAsRasterLayer(LAYERI);
      final IRasterLayer sLayer = m_Parameters.getParameterValueAsRasterLayer(LAYERS);
      final int hBand = m_Parameters.getParameterValueAsInt(BANDH);
      final int iBand = m_Parameters.getParameterValueAsInt(BANDI);
      final int sBand = m_Parameters.getParameterValueAsInt(BANDS);

      rLayer = getNewRasterLayer(R, "R", IRasterLayer.RASTER_DATA_TYPE_BYTE);
      gLayer = getNewRasterLayer(G, "G", IRasterLayer.RASTER_DATA_TYPE_BYTE);
      bLayer = getNewRasterLayer(B, "B", IRasterLayer.RASTER_DATA_TYPE_BYTE);

      final AnalysisExtent extent = rLayer.getWindowGridExtent();

      hLayer.setWindowExtent(extent);
      sLayer.setWindowExtent(extent);
      iLayer.setWindowExtent(extent);

      iNX = extent.getNX();
      iNY = extent.getNY();

      for (y = 0; (y < iNY) && setProgress(y, iNY); y++) {
         for (x = 0; x < iNX; x++) {
            h = hLayer.getCellValueAsInt(x, y, hBand);
            i = iLayer.getCellValueAsByte(x, y, iBand);
            s = sLayer.getCellValueAsByte(x, y, sBand);

            if (hLayer.isNoDataValue(h) || iLayer.isNoDataValue(i) || sLayer.isNoDataValue(s)) {
               rLayer.setNoData(x, y);
               gLayer.setNoData(x, y);
               bLayer.setNoData(x, y);
            }
            else {
               if ((h <= 0) && (h < 120)) {
                  r = (byte) (1 + ((s * Math.cos(h)) / (Math.cos(Math.toRadians(60 - h)))) * i);
                  b = (byte) (i * (1 - s));
                  g = (byte) (1 - r - b);
               }
               else if (h < 240) {
                  h = h - 120;
                  g = (byte) (1 + ((s * Math.cos(h)) / (Math.cos(Math.toRadians(60 - h)))) * i);
                  r = (byte) (i * (1 - s));
                  b = (byte) (1 - r - g);
               }
               else {
                  h = h - 240;
                  b = (byte) (1 + ((s * Math.cos(h)) / (Math.cos(Math.toRadians(60 - h)))) * i);
                  g = (byte) (i * (1 - s));
                  r = (byte) (1 - g - b);
               }

               rLayer.setCellValue(x, y, r);
               gLayer.setCellValue(x, y, g);
               bLayer.setCellValue(x, y, b);
            }

         }
      }

      return !m_Task.isCanceled();

   }


}
