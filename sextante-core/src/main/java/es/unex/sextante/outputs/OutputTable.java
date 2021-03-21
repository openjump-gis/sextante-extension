package es.unex.sextante.outputs;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.ITable;

/**
 * An output representing a table
 *
 * @author volaya
 *
 */
public class OutputTable
         extends
            Output {

   @Override
   public void setOutputObject(final Object obj) {

      if (obj instanceof ITable || obj == null) {
         m_Object = obj;
      }

   }


   @Override
   public String getCommandLineParameter() {

      if (m_OutputChannel == null) {
         return "\"#\"";
      }
      else {
         return "\"" + m_OutputChannel.getAsCommandLineParameter() + "\"";
      }

   }


   @Override
   public String getTypeDescription() {

      return Sextante.getText("table");

   }

}
