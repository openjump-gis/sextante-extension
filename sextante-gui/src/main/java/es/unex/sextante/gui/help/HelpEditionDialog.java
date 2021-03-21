package es.unex.sextante.gui.help;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.OutputObjectsSet;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.outputs.Output;
import es.unex.sextante.parameters.Parameter;

public class HelpEditionDialog
         extends
            JDialog {

   private JSplitPane          jSplitPane;
   private JTree               jTree;
   private JScrollPane         jScrollPaneTree;
   private final GeoAlgorithm  m_Alg;
   private ArrayList           m_Elements;
   private JButton             jButtonOK;
   private JButton             jButtonCancel;
   private ElementEditionPanel jElementEditionPanel;

   private TreePath            m_Path = null;


   public HelpEditionDialog(final GeoAlgorithm ga,
                            final Frame parent) {

      super(parent, ga.getName(), true);

      m_Alg = ga;

      initGUI();
      setLocationRelativeTo(null);

   }


   private void initGUI() {
      try {
         {
            final TableLayout thisLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL, 100.0, 100.0, 5.0 },
                     { TableLayoutConstants.FILL, 5.0, 30.0, 5.0 } });
            thisLayout.setHGap(5);
            thisLayout.setVGap(5);
            this.setLayout(thisLayout);
            this.setPreferredSize(new java.awt.Dimension(800, 500));
            this.setSize(new java.awt.Dimension(800, 500));

            jSplitPane = new JSplitPane();
            this.add(jSplitPane, "0, 0, 2, 0");
            {
               jTree = new JTree();
               jTree.setCellRenderer(new AlgorithmTreeCellRenderer());
               final MouseListener ml = new MouseAdapter() {
                  @Override
                  public void mousePressed(MouseEvent e) {
                     m_Path = jTree.getPathForLocation(e.getX(), e.getY());
                     if (m_Path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
                        Object ob = node.getUserObject();
                        if (ob instanceof HelpElement) {
                           setElement((HelpElement) ob);
                        }
                     }
                  }
               };
               jTree.addMouseListener(ml);

               jTree.addTreeSelectionListener(new TreeSelectionListener() {

                  public void valueChanged(final TreeSelectionEvent e) {
                     m_Path = e.getPath();
                     if (m_Path != null) {
                        final DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
                        final Object obj = node.getUserObject();
                        if (obj instanceof HelpElement) {
                           setElement((HelpElement) obj);
                        }
                     }
                  }
               });

               jScrollPaneTree = new JScrollPane(jTree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
               jElementEditionPanel = new ElementEditionPanel(m_Alg);
               jScrollPaneTree.setPreferredSize(new Dimension(200, 450));
            }
            {
               jSplitPane.add(jElementEditionPanel, JSplitPane.RIGHT);
               jSplitPane.add(jScrollPaneTree, JSplitPane.LEFT);
            }
            {
               jButtonOK = new JButton();
               this.add(jButtonOK, "1, 2");
               jButtonOK.setText(Sextante.getText("OK"));
               jButtonOK.addActionListener(new ActionListener() {
                  public void actionPerformed(final ActionEvent evt) {
                     saveHelp();
                  }
               });
            }
            {
               jButtonCancel = new JButton();
               this.add(jButtonCancel, "2, 2");
               jButtonCancel.setText(Sextante.getText("Cancel"));
               jButtonCancel.addActionListener(new ActionListener() {
                  public void actionPerformed(final ActionEvent evt) {
                     cancel();
                  }
               });
            }

            createElementsList();
            fillTree();
            expandAll();

         }

      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }
   }


   private void expandAll() {

      final TreeNode root = (TreeNode) jTree.getModel().getRoot();
      expandAll(jTree, new TreePath(root), true);

   }


   private void expandAll(final JTree tree,
                          final TreePath parent,
                          final boolean expand) {

      final TreeNode node = (TreeNode) parent.getLastPathComponent();
      if (node.getChildCount() >= 0) {
         for (final Enumeration e = node.children(); e.hasMoreElements();) {
            final TreeNode n = (TreeNode) e.nextElement();
            final TreePath path = parent.pathByAddingChild(n);
            expandAll(tree, path, expand);
         }
      }

      if (expand) {
         tree.expandPath(parent);
      }
      else {
         tree.collapsePath(parent);
      }

   }


   protected void setElement(final HelpElement element) {

      jElementEditionPanel.saveElement();
      jElementEditionPanel.setElement(element);

   }


   protected void cancel() {

      dispose();
      setVisible(false);

   }


   protected void saveHelp() {

      jElementEditionPanel.saveElement();

      HelpIO.save(m_Elements, SextanteGUI.getAlgorithmHelpFilename(m_Alg, true));

      cancel();

   }


   private void createElementsList() {

      if (!createElementsListFromFile()) {
         m_Elements = new ArrayList();
         m_Elements.add(new HelpElement("DESCRIPTION", Sextante.getText("Description"), HelpElement.TYPE_ADDITIONAL_INFO));
         m_Elements.add(new HelpElement("ADDITIONAL_INFO", Sextante.getText("Additional_information"),
                  HelpElement.TYPE_ADDITIONAL_INFO));
         m_Elements.add(new HelpElement("EXTENSION_AUTHOR", Sextante.getText("Algorithm_created_by"),
                  HelpElement.TYPE_ADDITIONAL_INFO));
         m_Elements.add(new HelpElement("HELP_AUTHOR", Sextante.getText("Help_file_created_by"), HelpElement.TYPE_ADDITIONAL_INFO));
         m_Elements.add(new HelpElement("USER_NOTES", Sextante.getText("User_notes"), HelpElement.TYPE_ADDITIONAL_INFO));


         final ParametersSet params = m_Alg.getParameters();
         for (int i = 0; i < params.getNumberOfParameters(); i++) {
            final Parameter param = params.getParameter(i);
            m_Elements.add(new HelpElement(param.getParameterName(), param.getParameterDescription(), HelpElement.TYPE_PARAMETER));

         }

         m_Elements.add(new HelpElement("OUTPUT_DESCRIPTION", Sextante.getText("Description"), HelpElement.TYPE_OUTPUT));

         final OutputObjectsSet oo = m_Alg.getOutputObjects();
         for (int i = 0; i < oo.getOutputObjectsCount(); i++) {
            final Output out = oo.getOutput(i);
            m_Elements.add(new HelpElement(out.getName(), out.getDescription(), HelpElement.TYPE_OUTPUT));
         }

      }

   }


   private boolean createElementsListFromFile() {

      final String sFilename = SextanteGUI.getAlgorithmHelpFilename(m_Alg, true);

      m_Elements = HelpIO.open(sFilename);

      return m_Elements != null;

   }


   public void fillTree() {

      DefaultMutableTreeNode node;
      final DefaultMutableTreeNode outputsNode = new DefaultMutableTreeNode(Sextante.getText("Outputs"));
      final DefaultMutableTreeNode paramsNode = new DefaultMutableTreeNode(Sextante.getText("Parameters"));
      final DefaultMutableTreeNode infoNode = new DefaultMutableTreeNode(Sextante.getText("Information"));

      for (int i = 0; i < m_Elements.size(); i++) {
         final HelpElement element = (HelpElement) m_Elements.get(i);
         node = new DefaultMutableTreeNode(element);
         switch (element.getType()) {
            case HelpElement.TYPE_ADDITIONAL_INFO:
               infoNode.add(node);
               break;
            case HelpElement.TYPE_OUTPUT:
               outputsNode.add(node);
               break;
            case HelpElement.TYPE_PARAMETER:
               paramsNode.add(node);
               break;
         }
      }

      DefaultMutableTreeNode selectedNode = null;
      final DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(Sextante.getText("Elements"));
      if (infoNode.getChildCount() != 0) {
         mainNode.add(infoNode);
         selectedNode = (DefaultMutableTreeNode) infoNode.getChildAt(0);

      }
      if (paramsNode.getChildCount() != 0) {
         mainNode.add(paramsNode);
      }

      if (outputsNode.getChildCount() != 0) {
         mainNode.add(outputsNode);
      }
      final DefaultTreeModel model = new DefaultTreeModel(mainNode);
      jTree.setModel(model);
      final TreePath path = new TreePath(model.getPathToRoot(selectedNode));
      jTree.setSelectionPath(path);
      jTree.scrollPathToVisible(path);
      jElementEditionPanel.setElement((HelpElement) selectedNode.getUserObject());

   }


}
