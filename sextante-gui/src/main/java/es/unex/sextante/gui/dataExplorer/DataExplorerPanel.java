

package es.unex.sextante.gui.dataExplorer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import es.unex.sextante.core.AbstractInputFactory;
import es.unex.sextante.core.AnalysisExtent;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.dataObjects.IRasterLayer;
import es.unex.sextante.dataObjects.ITable;
import es.unex.sextante.dataObjects.IVectorLayer;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.toolbox.TransparentScrollPane;


public class DataExplorerPanel
         extends
            JPanel {

   private static final String[] DATA_TYPE = { "BYTE", "", "SHORT", "INT", "FLOAT", "DOUBLE" };

   private static final String[] TYPE_NAME = { "POINT", "LINE", "POLYGON" };

   private JTree                 jTree;
   private JScrollPane           jScrollPane;

   private JMenuItem             menuItem;
   private JPopupMenu            popupMenu;
   private TreePath              m_Path;
   private Action                m_Action;


   public DataExplorerPanel() {

      super();

      init();

   }


   private void init() {

      this.setPreferredSize(new java.awt.Dimension(350, 380));
      this.setSize(new java.awt.Dimension(350, 380));

      final BorderLayout thisLayout = new BorderLayout();
      this.setLayout(thisLayout);

      jTree = new JTree();
      jTree.setOpaque(false);
      jTree.setCellRenderer(new DataExplorerTreeCellRenderer());
      final MouseListener ml = new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            m_Path = jTree.getPathForLocation(e.getX(), e.getY());
            if (m_Path != null) {
               try {
                  DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
                  ObjectAndDescription oad = ((ObjectAndDescription) node.getUserObject());
                  m_Action = (Action) oad.getObject();
                  if (e.getButton() == MouseEvent.BUTTON3) {
                     showPopupMenu(e);
                  }
               }
               catch (Exception ex) {
                  //ex.printStackTrace();
               }
            }
         }
      };
      jTree.addMouseListener(ml);

      final TreeExpansionListener tel = new TreeExpansionListener() {
         public void treeCollapsed(TreeExpansionEvent e) {
         }


         public void treeExpanded(TreeExpansionEvent e) {
            m_Path = e.getPath();
            Object obj = m_Path.getLastPathComponent();
            if (obj instanceof BandTreeNode) {
               BandTreeNode bandNode = (BandTreeNode) obj;
               if (bandNode.getChildCount() == 1) {
                  IRasterLayer rasterLayer = bandNode.getLayer();
                  int iBand = bandNode.getBand();
                  bandNode.removeAllChildren();
                  rasterLayer.open();
                  bandNode.add(new DefaultMutableTreeNode("AVG:" + Double.toString(rasterLayer.getMeanValue(iBand))));
                  bandNode.add(new DefaultMutableTreeNode("MIN:" + Double.toString(rasterLayer.getMinValue(iBand))));
                  bandNode.add(new DefaultMutableTreeNode("MAX:" + Double.toString(rasterLayer.getMaxValue(iBand))));
                  bandNode.add(new DefaultMutableTreeNode("VAR:" + Double.toString(rasterLayer.getVariance(iBand))));
                  rasterLayer.close();
                  ((DefaultTreeModel) jTree.getModel()).reload(bandNode);
                  jTree.repaint();
               }
            }
         }
      };
      jTree.addTreeExpansionListener(tel);

      fillTree();

      jScrollPane = new TransparentScrollPane(jTree);
      jScrollPane.setSize(new java.awt.Dimension(350, 380));
      this.add(jScrollPane, BorderLayout.CENTER);

   }


   private void fillTree() {

      final DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(Sextante.getText("Data"));

      final DefaultMutableTreeNode rasterLayersNode = new DefaultMutableTreeNode(Sextante.getText("Raster_layers"));
      mainNode.add(rasterLayersNode);

      final IRasterLayer[] rasterLayers = SextanteGUI.getInputFactory().getRasterLayers();
      for (int i = 0; i < rasterLayers.length; i++) {
         final IRasterLayer rasterLayer = rasterLayers[i];
         rasterLayer.open();
         final DefaultMutableTreeNode rasterLayerNode = new DefaultMutableTreeNode(rasterLayer.getName());

         rasterLayerNode.add(new DefaultMutableTreeNode(
                  /*new ObjectAndDescription(*/"NODATA:" + Double.toString(rasterLayer.getNoDataValue())/*,
                                    new ModifyNoDataValue(rasterLayer))*/));
         final AnalysisExtent ge = rasterLayer.getLayerGridExtent();
         rasterLayerNode.add(new DefaultMutableTreeNode("X:" + Double.toString(ge.getXMin()) + "-"
                                                        + Double.toString(ge.getXMax())));
         rasterLayerNode.add(new DefaultMutableTreeNode("Y:" + Double.toString(ge.getYMin()) + "-"
                                                        + Double.toString(ge.getYMax())));
         rasterLayerNode.add(new DefaultMutableTreeNode("NX:" + Integer.toString(ge.getNX()) + " | NY:"
                                                        + Integer.toString(ge.getNY())));
         rasterLayerNode.add(new DefaultMutableTreeNode("DATA_TYPE: " + DATA_TYPE[rasterLayer.getDataType()]));
         final DefaultMutableTreeNode bandsNode = new DefaultMutableTreeNode("BANDS:"
                                                                             + Integer.toString(rasterLayer.getBandsCount()));
         rasterLayerNode.add(bandsNode);
         for (int j = 0; j < rasterLayer.getBandsCount(); j++) {
            final DefaultMutableTreeNode bandNode = new BandTreeNode("Band " + Integer.toString(j + 1), rasterLayer, j);
            bandNode.add(new DefaultMutableTreeNode(Sextante.getText("Calculando") + "..."));
            bandsNode.add(bandNode);
         }
         rasterLayersNode.add(rasterLayerNode);
         rasterLayer.close();
      }

      final DefaultMutableTreeNode vectorLayersNode = new DefaultMutableTreeNode(Sextante.getText("Vector_layers"));
      mainNode.add(vectorLayersNode);

      final IVectorLayer[] vectorLayers = SextanteGUI.getInputFactory().getVectorLayers(AbstractInputFactory.SHAPE_TYPE_ANY);
      for (int i = 0; i < vectorLayers.length; i++) {
         final IVectorLayer vectorLayer = vectorLayers[i];
         final DefaultMutableTreeNode vectorLayerNode = new DefaultMutableTreeNode(new ObjectAndDescription(
                  vectorLayer.getName(), new ShowTable(vectorLayer)));
         vectorLayerNode.add(new DefaultMutableTreeNode(Sextante.getText("Number_of_features") + ":"
                                                        + vectorLayer.getShapesCount()));
         vectorLayerNode.add(new DefaultMutableTreeNode(Sextante.getText("Type") + ":" + TYPE_NAME[vectorLayer.getShapeType()]));
         final DefaultMutableTreeNode fieldsNode = new DefaultMutableTreeNode(Sextante.getText("fields") + ":"
                                                                              + vectorLayer.getFieldCount());
         for (int j = 0; j < vectorLayer.getFieldCount(); j++) {
            fieldsNode.add(new DefaultMutableTreeNode(vectorLayer.getFieldName(j) + ":"
                                                      + vectorLayer.getFieldType(j).getSimpleName()));
         }
         vectorLayerNode.add(fieldsNode);
         vectorLayersNode.add(vectorLayerNode);

      }

      final DefaultMutableTreeNode tablesNode = new DefaultMutableTreeNode(Sextante.getText("Tables"));
      mainNode.add(tablesNode);

      final ITable[] tables = SextanteGUI.getInputFactory().getTables();
      for (int i = 0; i < tables.length; i++) {
         final ITable table = tables[i];
         final DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(new ObjectAndDescription(table.getName(),
                  new ShowTable(table)));
         tableNode.add(new DefaultMutableTreeNode(Sextante.getText("Number_of_records") + ":" + table.getRecordCount()));
         final DefaultMutableTreeNode fieldsNode = new DefaultMutableTreeNode(Sextante.getText("fields") + ":"
                                                                              + table.getFieldCount());
         for (int j = 0; j < table.getFieldCount(); j++) {
            fieldsNode.add(new DefaultMutableTreeNode(table.getFieldName(j) + ":" + table.getFieldType(j).getSimpleName()));
         }
         tableNode.add(fieldsNode);
         tablesNode.add(tableNode);
      }

      jTree.setModel(new DefaultTreeModel(mainNode));

   }


   protected void showPopupMenu(final MouseEvent e) {

      jTree.setSelectionPath(m_Path);
      popupMenu = new JPopupMenu("Menu");
      menuItem = new JMenuItem(m_Action.getDescription());
      menuItem.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            if (m_Action.execute()) {
               fillTree();
            }
         }
      });
      popupMenu.add(menuItem);
      popupMenu.show(e.getComponent(), e.getX(), e.getY());

   }


   public void update() {

      fillTree();

   }

}
