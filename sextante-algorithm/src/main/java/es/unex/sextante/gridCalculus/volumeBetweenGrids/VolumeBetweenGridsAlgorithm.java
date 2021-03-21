package es.unex.sextante.gridCalculus.volumeBetweenGrids;

import java.text.DecimalFormat;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class VolumeBetweenGridsAlgorithm
         extends
            GeoAlgorithm {

   public static final String VOL   = "VOL";
   public static final String LGRID = "LGRID";
   public static final String UGRID = "UGRID";

   private int                m_iNX, m_iNY;
   private IRasterLayer       m_LowerGrid, m_UpperGrid;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_LowerGrid = m_Parameters.getParameterValueAsRasterLayer(LGRID);
      m_UpperGrid = m_Parameters.getParameterValueAsRasterLayer(UGRID);

      m_LowerGrid.setWindowExtent(m_AnalysisExtent);
      m_UpperGrid.setWindowExtent(m_AnalysisExtent);

      m_iNX = m_LowerGrid.getNX();
      m_iNY = m_LowerGrid.getNY();

      return calculateVolumes();

   }


   @Override
   public void defineCharacteristics() {

      setName(Sextante.getText("Volumen_between_two_layers"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(LGRID, Sextante.getText("Lower_layer"), true);
         m_Parameters.addInputRasterLayer(UGRID, Sextante.getText("Upper_layer"), true);
         addOutputText(VOL, Sextante.getText("Volume"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private boolean calculateVolumes() {

      int x, y;
      double z, z2;
      double dVolumePos = 0;
      double dVolumeNeg = 0;
      double dVolume = 0;
      double dDif;
      final double dArea = m_LowerGrid.getWindowCellSize() * m_LowerGrid.getWindowCellSize();

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            z = m_LowerGrid.getCellValueAsDouble(x, y);
            z2 = m_UpperGrid.getCellValueAsDouble(x, y);
            if (!m_LowerGrid.isNoDataValue(z) && !m_UpperGrid.isNoDataValue(z2)) {
               dDif = (z2 - z);
               dVolume += Math.abs(dDif);
               if (dDif > 0) {
                  dVolumePos += dDif;
               }
               else {
                  dVolumeNeg += -dDif;
               }
            }
         }
      }

      if (m_Task.isCanceled()) {
         return false;
      }
      else {
         dVolume *= dArea;
         dVolumePos *= dArea;
         dVolumeNeg *= dArea;

         final DecimalFormat df = new DecimalFormat("##.##");
         final HTMLDoc doc = new HTMLDoc();
         doc.open(Sextante.getText("Volumes"));
         doc.addHeader(Sextante.getText("Volumes"), 2);
         doc.startUnorderedList();
         doc.addListElement(Sextante.getText("Volume_+") + df.format(dVolumePos));
         doc.addListElement(Sextante.getText("Volume_-") + df.format(dVolumeNeg));
         doc.addListElement(Sextante.getText("Total_volume") + df.format(dVolume));
         doc.close();
         addOutputText(VOL, Sextante.getText("Volume"), doc.getHTMLCode());
         return true;
      }

   }

}
