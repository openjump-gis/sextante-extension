package es.unex.sextante.outputs;

import es.unex.sextante.dataObjects.IVectorLayer;

public class OverwriteOutputChannel
         implements
            IOutputChannel {

   private final IVectorLayer m_Layer;


   public OverwriteOutputChannel(final IVectorLayer layer) {

      m_Layer = layer;

   }


   public IVectorLayer getLayer() {

      return m_Layer;

   }


   @Override
   public String getAsCommandLineParameter() {

      return "@";

   }

}
