package es.unex.sextante.gui.dataExplorer;

import javax.swing.tree.DefaultMutableTreeNode;

import es.unex.sextante.dataObjects.IRasterLayer;

public class BandTreeNode
         extends
            DefaultMutableTreeNode {

   private final int          m_iBand;
   private final IRasterLayer m_Layer;


   public BandTreeNode(final Object obj,
                       final IRasterLayer layer,
                       final int iBand) {

      super(obj);

      m_Layer = layer;
      m_iBand = iBand;

   }


   public int getBand() {

      return m_iBand;

   }


   public IRasterLayer getLayer() {

      return m_Layer;

   }

}
