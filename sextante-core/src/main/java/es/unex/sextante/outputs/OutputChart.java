package es.unex.sextante.outputs;

import org.jfree.chart.ChartPanel;

import es.unex.sextante.core.Sextante;

/**
 * An output representing a chart
 *
 * @author volaya
 *
 */
public class OutputChart
         extends
            Output {

   @Override
   public String getCommandLineParameter() {
      return null;
   }


   @Override
   public void setOutputObject(final Object obj) {

      if (obj instanceof ChartPanel) {
         m_Object = obj;
      }

   }


   @Override
   public String getTypeDescription() {

      return Sextante.getText("chart");

   }

}
