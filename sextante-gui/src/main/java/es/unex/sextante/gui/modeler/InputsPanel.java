package es.unex.sextante.gui.modeler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.modeler.parameters.BooleanPanel;
import es.unex.sextante.gui.modeler.parameters.FixedTablePanel;
import es.unex.sextante.gui.modeler.parameters.MultipleInputPanel;
import es.unex.sextante.gui.modeler.parameters.NumericalValuePanel;
import es.unex.sextante.gui.modeler.parameters.ParameterPanel;
import es.unex.sextante.gui.modeler.parameters.PointPanel;
import es.unex.sextante.gui.modeler.parameters.Raster3DLayerPanel;
import es.unex.sextante.gui.modeler.parameters.RasterBandPanel;
import es.unex.sextante.gui.modeler.parameters.RasterLayerPanel;
import es.unex.sextante.gui.modeler.parameters.SelectionPanel;
import es.unex.sextante.gui.modeler.parameters.StringPanel;
import es.unex.sextante.gui.modeler.parameters.TableFieldPanel;
import es.unex.sextante.gui.modeler.parameters.TablePanel;
import es.unex.sextante.gui.modeler.parameters.VectorLayerPanel;
import es.unex.sextante.modeler.elements.ModelElementFactory;
import es.unex.sextante.parameters.Parameter;

public class InputsPanel
         extends
            JPanel {

   private JTree              jTree;
   private JScrollPane        jScrollPaneTree;
   private final HashMap      m_DataObjects;
   private final ArrayList    m_InputKeys;
   private final ModelerPanel m_ModelerPanel;
   private final JDialog      m_Parent;


   public InputsPanel(final ModelerPanel modelerPanel,
                      final JDialog parent) {

      super();

      //m_iParameterID = 1;
      m_DataObjects = modelerPanel.getDataObjects();
      m_InputKeys = modelerPanel.getInputKeys();
      m_ModelerPanel = modelerPanel;
      m_Parent = parent;

      initGUI();

   }


   private void initGUI() {

      jScrollPaneTree = new JScrollPane();
      jTree = new JTree();
      jScrollPaneTree.setViewportView(jTree);
      final MouseListener ml = new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            TreePath path = jTree.getPathForLocation(e.getX(), e.getY());
            if (e.getClickCount() == 2) {
               addSelectedInput(path);
            }
         }
      };
      jTree.addMouseListener(ml);
      jTree.setCellRenderer(new MyCellRenderer());
      final BorderLayout thisLayout = new BorderLayout();
      this.setLayout(thisLayout);
      this.add(jScrollPaneTree, BorderLayout.CENTER);
      fillTree();

   }


   private void fillTree() {

      DefaultMutableTreeNode node;
      final DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(Sextante.getText("Inputs"));

      node = new DefaultMutableTreeNode(getNewNumericalValuePanel());
      mainNode.add(node);
      node = new DefaultMutableTreeNode(getNewBooleanPanel());
      mainNode.add(node);
      node = new DefaultMutableTreeNode(getNewRasterLayerPanel());
      mainNode.add(node);
      node = new DefaultMutableTreeNode(getNewRasterBandPanel());
      mainNode.add(node);
      node = new DefaultMutableTreeNode(getNew3DRasterLayerPanel());
      mainNode.add(node);
      node = new DefaultMutableTreeNode(getNewTableFieldPanel());
      mainNode.add(node);
      node = new DefaultMutableTreeNode(getNewStringPanel());
      mainNode.add(node);
      node = new DefaultMutableTreeNode(getNewTablePanel());
      mainNode.add(node);
      node = new DefaultMutableTreeNode(getNewPointPanel());
      mainNode.add(node);
      node = new DefaultMutableTreeNode(getNewMultipleInputPanel());
      mainNode.add(node);
      node = new DefaultMutableTreeNode(getNewFixedTablePanel());
      mainNode.add(node);
      node = new DefaultMutableTreeNode(getNewSelectionPanel());
      mainNode.add(node);
      node = new DefaultMutableTreeNode(getNewVectorLayerPanel());
      mainNode.add(node);
      jTree.setModel(new DefaultTreeModel(mainNode));

   }


   private Object getNewNumericalValuePanel() {

      if (m_Parent != null) {
         return new NumericalValuePanel(m_Parent, m_ModelerPanel);
      }
      else {
         return new NumericalValuePanel(m_ModelerPanel);
      }

   }


   private Object getNewBooleanPanel() {

      if (m_Parent != null) {
         return new BooleanPanel(m_Parent, m_ModelerPanel);
      }
      else {
         return new BooleanPanel(m_ModelerPanel);
      }

   }


   private Object getNewRasterLayerPanel() {

      if (m_Parent != null) {
         return new RasterLayerPanel(m_Parent, m_ModelerPanel);
      }
      else {
         return new RasterLayerPanel(m_ModelerPanel);
      }

   }


   private Object getNewRasterBandPanel() {

      if (m_Parent != null) {
         return new RasterBandPanel(m_Parent, m_ModelerPanel);
      }
      else {
         return new RasterBandPanel(m_ModelerPanel);
      }

   }


   private Object getNew3DRasterLayerPanel() {

      if (m_Parent != null) {
         return new Raster3DLayerPanel(m_Parent, m_ModelerPanel);
      }
      else {
         return new Raster3DLayerPanel(m_ModelerPanel);
      }

   }


   private Object getNewVectorLayerPanel() {

      if (m_Parent != null) {
         return new VectorLayerPanel(m_Parent, m_ModelerPanel);
      }
      else {
         return new VectorLayerPanel(m_ModelerPanel);
      }

   }


   private Object getNewStringPanel() {

      if (m_Parent != null) {
         return new StringPanel(m_Parent, m_ModelerPanel);
      }
      else {
         return new StringPanel(m_ModelerPanel);
      }

   }


   private Object getNewTableFieldPanel() {

      if (m_Parent != null) {
         return new TableFieldPanel(m_Parent, m_ModelerPanel);
      }
      else {
         return new TableFieldPanel(m_ModelerPanel);
      }

   }


   private Object getNewTablePanel() {

      if (m_Parent != null) {
         return new TablePanel(m_Parent, m_ModelerPanel);
      }
      else {
         return new TablePanel(m_ModelerPanel);
      }

   }


   private Object getNewPointPanel() {

      if (m_Parent != null) {
         return new PointPanel(m_Parent, m_ModelerPanel);
      }
      else {
         return new PointPanel(m_ModelerPanel);
      }

   }


   private Object getNewFixedTablePanel() {

      if (m_Parent != null) {
         return new FixedTablePanel(m_Parent, m_ModelerPanel);
      }
      else {
         return new FixedTablePanel(m_ModelerPanel);
      }

   }


   private Object getNewSelectionPanel() {

      if (m_Parent != null) {
         return new SelectionPanel(m_Parent, m_ModelerPanel);
      }
      else {
         return new SelectionPanel(m_ModelerPanel);
      }

   }


   private Object getNewMultipleInputPanel() {

      if (m_Parent != null) {
         return new MultipleInputPanel(m_Parent, m_ModelerPanel);
      }
      else {
         return new MultipleInputPanel(m_ModelerPanel);
      }

   }


   public void addSelectedInput() {

      addSelectedInput(jTree.getSelectionPath());

   }


   protected void addSelectedInput(final TreePath path) {

      Parameter param;
      ObjectAndDescription oad;
      String sKey;

      if (path == null) {
         return;
      }

      try {
         final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
         if (node.getUserObject() instanceof ParameterPanel) {
            final ParameterPanel paramPanel = (ParameterPanel) node.getUserObject();
            if (paramPanel.parameterCanBeAdded()) {
               paramPanel.updateOptions();
               paramPanel.pack();
               paramPanel.setVisible(true);
               param = paramPanel.getParameter();
               if (param != null) {
                  sKey = getValidName();
                  param.setParameterName(sKey);
                  m_ModelerPanel.getAlgorithm().addInput(param);
                  oad = new ObjectAndDescription(param.getParameterDescription(),
                           ModelElementFactory.getParameterAsModelElement(param));
                  m_DataObjects.put(sKey, oad);
                  m_InputKeys.add(sKey);
                  m_ModelerPanel.setHasChanged(true);
                  m_ModelerPanel.getGraph().addInput(sKey);
                  m_ModelerPanel.updatePanel(true);
               }
            }
         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   public String getValidName() {

      boolean bFound;
      int i = 0;
      String sName;

      do {
         sName = "INPUT" + Integer.toString(i);
         if (this.m_InputKeys.contains(sName)) {
            i++;
            bFound = false;
         }
         else {
            bFound = true;
         }

      }
      while (!bFound);

      return sName;

   }

   class MyCellRenderer
            extends
               JLabel
            implements
               TreeCellRenderer {

      ImageIcon m_ModuleIcon = new ImageIcon(getClass().getClassLoader().getResource("images/list-add.png"));


      protected Icon getCustomIcon(final Object value) {

         if (((DefaultMutableTreeNode) value).getUserObject() instanceof ParameterPanel) {
            return m_ModuleIcon;
         }
         else {
            return null;
         }

      }


      public Component getTreeCellRendererComponent(final JTree tree,
                                                    final Object value,
                                                    final boolean sel,
                                                    final boolean expanded,
                                                    boolean leaf,
                                                    final int row,
                                                    final boolean hasFocus) {

         String sName;

         setFont(tree.getFont());
         this.setIcon(getCustomIcon(value));

         sName = tree.convertValueToText(value, sel, expanded, leaf, row, hasFocus);


         setEnabled(tree.isEnabled());
         setText(sName);
         try {
            if (!leaf) {
               setFont(new java.awt.Font("Tahoma", 1, 11));
               setForeground(Color.black);
            }
            else {
               final ParameterPanel panel = ((ParameterPanel) ((DefaultMutableTreeNode) value).getUserObject());
               if (!panel.parameterCanBeAdded()) {
                  setForeground(Color.gray);
               }
               else if (sel) {
                  setForeground(Color.blue);
               }
               else {
                  setForeground(Color.black);
               }
            }
         }
         catch (final ClassCastException e) {
            setForeground(Color.black);
         }

         return this;

      }

   }


}
