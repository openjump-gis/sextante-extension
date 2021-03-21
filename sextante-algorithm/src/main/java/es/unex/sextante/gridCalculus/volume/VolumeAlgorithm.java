package es.unex.sextante.gridCalculus.volume;

import java.text.DecimalFormat;

import es.unex.sextante.additionalInfo.AdditionalInfoNumericalValue;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.docEngines.html.HTMLDoc;
import es.unex.sextante.exceptions.GeoAlgorithmExecutionException;
import es.unex.sextante.exceptions.RepeatedParameterNameException;

public class VolumeAlgorithm
         extends
            GeoAlgorithm {

   public static final String BASE                                           = "BASE";
   public static final String METHOD                                         = "METHOD";
   public static final String GRID                                           = "GRID";
   public static final String VOL                                            = "VOL";

   public static final int    METHOD_ONLY_OVER_BASE_LEVEL                    = 0;
   public static final int    METHOD_ONLY_UNDER_BASE_LEVEL                   = 1;
   public static final int    SUM_OVER_BASE_LEVEL_SUBSTRACT_UNDER_BASE_LEVEL = 2;
   public static final int    SUM_VOLUMES_BOTH_OVER_AND_UNDER_BASE_LEVEL     = 3;

   private int                m_iNX, m_iNY;
   private double             m_BaseLevel;
   private IRasterLayer       m_Grid                                         = null;
   private int                m_iMethod;


   @Override
   public boolean processAlgorithm() throws GeoAlgorithmExecutionException {

      m_Grid = m_Parameters.getParameterValueAsRasterLayer(GRID);
      m_iMethod = m_Parameters.getParameterValueAsInt(METHOD);
      m_BaseLevel = m_Parameters.getParameterValueAsDouble(BASE);

      m_Grid.setWindowExtent(m_AnalysisExtent);

      m_iNX = m_Grid.getNX();
      m_iNY = m_Grid.getNY();

      return calculateVolumes();

   }


   @Override
   public void defineCharacteristics() {

      final String sMethod[] = { Sextante.getText("Only_volumes_over_base_level"),
               Sextante.getText("Only_volumes_below_base_level"),
               Sextante.getText("Add_volumes_over_base_level_and_substract_volumes_below_it"),
               Sextante.getText("Add_volumes_both_over_and_below_base_level") };

      setName(Sextante.getText("Volume_calculation"));
      setGroup(Sextante.getText("Basic_tools_for_raster_layers"));
      setUserCanDefineAnalysisExtent(true);

      try {
         m_Parameters.addInputRasterLayer(GRID, Sextante.getText("Elevation"), true);
         m_Parameters.addNumericalValue(BASE, Sextante.getText("Base_level"),
                  AdditionalInfoNumericalValue.NUMERICAL_VALUE_DOUBLE, 1000, Double.NEGATIVE_INFINITY, Double.MAX_VALUE);
         m_Parameters.addSelection(METHOD, Sextante.getText("Method"), sMethod);
         addOutputText(VOL, Sextante.getText("Volume"));
      }
      catch (final RepeatedParameterNameException e) {
         Sextante.addErrorToLog(e);
      }

   }


   private boolean calculateVolumes() {

      int x, y;
      double z;
      double dVolume = 0;

      for (y = 0; (y < m_iNY) && setProgress(y, m_iNY); y++) {
         for (x = 0; x < m_iNX; x++) {
            z = m_Grid.getCellValueAsDouble(x, y);
            if (!m_Grid.isNoDataValue(z)) {
               z = z - m_BaseLevel;
               switch (m_iMethod) {
                  case 0:
                     if (z > 0.0) {
                        dVolume += z;
                     }
                     break;
                  case 1:
                     if (z < 0.0) {
                        dVolume -= z;
                     }
                     break;
                  case 2:
                     dVolume += z;
                     break;

                  case 3:
                     dVolume += Math.abs(z);
                     break;
               }
            }
         }
      }

      if (m_Task.isCanceled()) {
         return false;
      }
      else {
         dVolume *= (m_Grid.getWindowCellSize() * m_Grid.getWindowCellSize());

         final DecimalFormat df = new DecimalFormat("##.##");
         final HTMLDoc doc = new HTMLDoc();
         doc.open(Sextante.getText("Volumes"));
         doc.addHeader(Sextante.getText("Volumes"), 2);
         doc.startUnorderedList();
         doc.addListElement(Sextante.getText("Calculated_volume") + ": " + df.format(dVolume));
         doc.close();
         addOutputText(VOL, Sextante.getText("Volume") + "[" + m_Grid.getName() + "]", doc.getHTMLCode());
         return true;
      }

   }

}
