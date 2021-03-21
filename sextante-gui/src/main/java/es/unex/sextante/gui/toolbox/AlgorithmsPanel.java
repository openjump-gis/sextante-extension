

package es.unex.sextante.gui.toolbox;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import es.unex.sextante.additionalInfo.AdditionalInfoVectorLayer;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.IGeoAlgorithmFilter;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.gui.core.GeoAlgorithmExecutors;
import es.unex.sextante.gui.core.IGUIFactory;
import es.unex.sextante.gui.core.IToolboxRightButtonAction;
import es.unex.sextante.gui.core.NameAndIcon;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.core.ToolboxAction;
import es.unex.sextante.gui.help.HelpIO;
import es.unex.sextante.gui.history.History;
import es.unex.sextante.gui.settings.SextanteGeneralSettings;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterVectorLayer;


/**
 * A panel showing the list of available algorithms, from which they can be executed
 * 
 * @author volaya
 * 
 */
public class AlgorithmsPanel
         extends
            JPanel {
   private static final int       EXECUTE_ITERATIVE_MENU_COUNT = 20;

   public static Font             TREE_FONT                    = new Font("Tahoma", Font.BOLD, 11);

   protected JTree                jTree;
   private TransparentScrollPane  jScrollPane;
   private JMenuItem              menuItemExecute;
   private JMenuItem              menuItemExecuteAsBatch;
   private JMenuItem              menuItemExecuteAsBatchFromGIS;
   private JMenuItem              menuItemExecuteIterative[];
   private JMenuItem              menuItemShowHelp;
   private JCheckBoxMenuItem      menuItemShowOnlyActive;
   private JPopupMenu             popupMenu;
   private TreePath               m_Path;
   private GeoAlgorithm           m_Alg;
   protected final IToolboxDialog m_ParentDialog;
   protected IGeoAlgorithmFilter  m_Filter;
   private final ImageIcon        m_BackgroundImg;
   protected String               m_sLastSearchString          = "";
   private int                    m_iExecuteIterativeMenuCount;

   protected boolean              m_bLastSearchIncludedHelpFiles;

   private ToolboxAction          m_Action;

   private JMenuItem[]            menuItemToolboxAction;

   private JMenuItem              menuItemSetOutputRendering;


   /**
    * Constructor
    * 
    * @param parentDialog
    *                the parent dialog. It will be the parent dialog of parameters dialog opened from this panel
    * @param filter
    *                the filter to apply to the list of all available algorithms
    */
   public AlgorithmsPanel(final IToolboxDialog parentDialog,
                          final IGeoAlgorithmFilter filter,
                          final ImageIcon img) {

      m_BackgroundImg = img;
      m_ParentDialog = parentDialog;
      m_Filter = filter;

      if (m_Filter == null) {
         m_Filter = new IGeoAlgorithmFilter() {
            public boolean accept(final GeoAlgorithm alg) {
               if (SextanteGUI.getShowOnlyActiveAlgorithms()) {
                  final Object[] objs = SextanteGUI.getInputFactory().getDataObjects();
                  return alg.meetsDataRequirements(objs);
               }
               else {
                  return true;
               }
            }
         };
      }

      init();

   }


   private void updateSelectedAlgorithm() {

      m_Alg = null;
      m_Action = null;

      if (m_Path != null) {
         menuItemExecuteAsBatch.setVisible(false);
         menuItemExecuteAsBatchFromGIS.setVisible(false);
         menuItemExecute.setVisible(false);
         menuItemShowHelp.setVisible(false);
         for (int i = 0; i < menuItemExecuteIterative.length; i++) {
            menuItemExecuteIterative[i].setVisible(false);
         }
         final DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
         if (!(node.getUserObject() instanceof GeoAlgorithm)) {
            if ((node.getUserObject() instanceof ToolboxAction)) {
               m_Action = (ToolboxAction) node.getUserObject();
            }
            return;
         }
         m_Alg = ((GeoAlgorithm) node.getUserObject());
         menuItemExecuteAsBatch.setVisible(true);
         menuItemShowHelp.setVisible(true);
         menuItemExecute.setVisible(true);
         final Object[] objs = SextanteGUI.getInputFactory().getDataObjects();
         menuItemExecuteAsBatchFromGIS.setVisible(true);
         final boolean bMeets = m_Alg.meetsDataRequirements(objs);
         menuItemExecuteAsBatchFromGIS.setEnabled(bMeets);
         menuItemExecute.setEnabled(bMeets);

         final IToolboxRightButtonAction[] actions = SextanteGUI.getToolboxRightButtonActions();
         for (int i = 0; i < menuItemToolboxAction.length; i++) {
            menuItemToolboxAction[i].setVisible(actions[i].canBeExecutedOnAlgorithm(m_Alg));
         }

         if (bMeets && m_Alg.requiresIndividualVectorLayers()) {
            m_iExecuteIterativeMenuCount = 0;
            final int iParams = m_Alg.getParameters().getNumberOfParameters();
            for (int i = 0; i < iParams; i++) {
               final Parameter param = m_Alg.getParameters().getParameter(i);
               if (param instanceof ParameterVectorLayer) {
                  final ParameterVectorLayer pvl = (ParameterVectorLayer) param;
                  boolean bMandatory;
                  try {
                     bMandatory = ((AdditionalInfoVectorLayer) pvl.getParameterAdditionalInfo()).getIsMandatory();
                     if (bMandatory) {
                        menuItemExecuteIterative[m_iExecuteIterativeMenuCount].setText(Sextante.getText("Run_iterative") + "["
                                                                                       + param.getParameterDescription() + "]");
                        menuItemExecuteIterative[m_iExecuteIterativeMenuCount].setVisible(true);
                        m_iExecuteIterativeMenuCount++;
                     }
                  }
                  catch (final NullParameterAdditionalInfoException e) {
                  }
               }
            }
         }
      }

   }


   private void init() {

      m_iExecuteIterativeMenuCount = 0;
      this.setPreferredSize(new java.awt.Dimension(350, 380));
      this.setSize(new java.awt.Dimension(350, 380));
      final BorderLayout thisLayout = new BorderLayout();
      this.setLayout(thisLayout);
      jTree = new JTree();
      jTree.setOpaque(false);
      jTree.setCellRenderer(new AlgorithmTreeCellRenderer());
      final MouseListener ml = new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            m_Alg = null;
            m_Action = null;
            menuItemExecute.setVisible(false);
            menuItemExecuteAsBatch.setVisible(false);
            for (int i = 0; i < EXECUTE_ITERATIVE_MENU_COUNT; i++) {
               menuItemExecuteIterative[i].setVisible(false);
            }
            for (int j = 0; j < menuItemToolboxAction.length; j++) {
               menuItemToolboxAction[j].setVisible(false);
            }
            menuItemExecuteAsBatchFromGIS.setVisible(false);
            menuItemShowHelp.setVisible(false);
            m_Path = jTree.getPathForLocation(e.getX(), e.getY());
            updateSelectedAlgorithm();

            // Create again DataObjects here to get feature selections changes (if there are)
            SextanteGUI.getInputFactory().createDataObjects();

            if (e.getButton() == MouseEvent.BUTTON1) {
               if (e.getClickCount() == 2) {
                  executeSelectedAlgorithm();
               }
            }
            else if (e.getButton() == MouseEvent.BUTTON3) {
               showPopupMenu(e);
            }
         }
      };
      jTree.addMouseListener(ml);
      jTree.addKeyListener(new KeyListener() {
         public void keyPressed(final KeyEvent e) {
         }


         public void keyReleased(final KeyEvent e) {
         }


         public void keyTyped(final KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
               executeSelectedAlgorithm();
            }

            if (e.getKeyChar() == KeyEvent.VK_SPACE) {
               showPopupMenu(e);
            }
         }
      });

      jTree.addTreeSelectionListener(new TreeSelectionListener() {

         public void valueChanged(final TreeSelectionEvent e) {
            m_Path = e.getPath();
            updateSelectedAlgorithm();
         }

      });

      jScrollPane = new TransparentScrollPane(jTree);
      jScrollPane.setSize(new java.awt.Dimension(350, 380));
      if (m_BackgroundImg != null) {
         jScrollPane.setBackgroundImage(m_BackgroundImg);
      }
      this.add(jScrollPane, BorderLayout.CENTER);

      popupMenu = new JPopupMenu("Menu");

      menuItemExecute = new JMenuItem(Sextante.getText("Run"));
      menuItemExecute.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            executeSelectedAlgorithm();
         }
      });
      popupMenu.add(menuItemExecute);

      menuItemExecuteIterative = new JMenuItem[EXECUTE_ITERATIVE_MENU_COUNT];
      for (int i = 0; i < menuItemExecuteIterative.length; i++) {
         final int iParameter = i;
         menuItemExecuteIterative[i] = new JMenuItem();
         menuItemExecuteIterative[i].addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               executeSelectedAlgorithmIteratively(iParameter);
            }
         });
         popupMenu.add(menuItemExecuteIterative[i]);
      }

      final IToolboxRightButtonAction[] actions = SextanteGUI.getToolboxRightButtonActions();
      menuItemToolboxAction = new JMenuItem[actions.length];
      for (int i = 0; i < actions.length; i++) {
         final IToolboxRightButtonAction action = actions[i];
         menuItemToolboxAction[i] = new JMenuItem();
         menuItemToolboxAction[i].setText(action.getDescription());
         menuItemToolboxAction[i].addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               action.execute(m_Alg);
            }
         });
         popupMenu.add(menuItemToolboxAction[i]);
      }


      menuItemExecuteAsBatch = new JMenuItem(Sextante.getText("Execute_as_batch_process"));
      menuItemExecuteAsBatch.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            executeAsBatch();
         }
      });
      popupMenu.add(menuItemExecuteAsBatch);

      menuItemExecuteAsBatchFromGIS = new JMenuItem(Sextante.getText("Execute_as_batch_process__using_layers_from_GIS_app"));
      menuItemExecuteAsBatchFromGIS.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            executeAsBatchInGIS();
         }
      });
      popupMenu.add(menuItemExecuteAsBatchFromGIS);
      popupMenu.addSeparator();

      menuItemSetOutputRendering = new JMenuItem(Sextante.getText("set_output_rendering"));
      menuItemSetOutputRendering.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            setOutputRendering();
         }
      });
      popupMenu.add(menuItemSetOutputRendering);
      popupMenu.addSeparator();

      final JMenuItem menuItemExpand = new JMenuItem(Sextante.getText("Expand_all"));
      menuItemExpand.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            expandAll();
         }
      });
      popupMenu.add(menuItemExpand);

      final JMenuItem menuItemCollapse = new JMenuItem(Sextante.getText("Collapse_all"));
      menuItemCollapse.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            collapseAll();
         }
      });
      popupMenu.add(menuItemCollapse);

      menuItemShowOnlyActive = new JCheckBoxMenuItem(Sextante.getText("Show_active_only"));
      menuItemShowOnlyActive.addItemListener(new ItemListener() {
         public void itemStateChanged(final ItemEvent e) {
            SextanteGUI.setShowOnlyActiveAlgorithms(e.getStateChange() == ItemEvent.SELECTED);
            fillTree(m_sLastSearchString, m_bLastSearchIncludedHelpFiles);
            collapseAll();
         }
      });
      menuItemShowOnlyActive.setSelected(SextanteGUI.getShowOnlyActiveAlgorithms());
      popupMenu.add(menuItemShowOnlyActive);

      popupMenu.addSeparator();

      menuItemShowHelp = new JMenuItem(Sextante.getText("Show_help"));
      menuItemShowHelp.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            showHelp();
         }
      });
      popupMenu.add(menuItemShowHelp);

   }


   protected void setOutputRendering() {

      if (m_Alg != null) {
         final OutputRenderingSettingsDialog dialog = new OutputRenderingSettingsDialog(m_Alg);
         dialog.setVisible(true);
         final HashMap<String, Object> set = dialog.getSettings();
         if (set != null) {
            SextanteGUI.getDataRenderer().setRenderingForAlgorithm(m_Alg.getCommandLineName(), set);
            SextanteGUI.getDataRenderer().save();
         }
      }

   }


   protected void showHelp() {

      if (m_Alg != null) {
         SextanteGUI.getGUIFactory().showHelpDialog(m_Alg);
      }

   }


   protected void showPopupMenu(final MouseEvent e) {

      jTree.setSelectionPath(m_Path);
      popupMenu.show(e.getComponent(), e.getX(), e.getY());

   }


   protected void showPopupMenu(final KeyEvent e) {

      jTree.setSelectionPath(m_Path);
      final Rectangle pathBounds = jTree.getPathBounds(m_Path);
      popupMenu.show(e.getComponent(), pathBounds.x, pathBounds.y);

   }


   /**
    * Collapses the tree of algorithms
    */
   public void collapseAll() {

      final TreeNode root = (TreeNode) jTree.getModel().getRoot();
      final TreePath path = new TreePath(root);
      expandAll(jTree, path, false);
      jTree.expandPath(path);
      final int iChildCount = root.getChildCount();
      for (int i = 0; i < iChildCount; i++) {
         final TreeNode node = root.getChildAt(i);
         final TreePath subpath = path.pathByAddingChild(node);
         jTree.expandPath(subpath);
      }

   }


   /**
    * Expands the tree of algorithms
    */
   public void expandAll() {

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


   protected void executeAsBatch() {

      if (m_Alg != null) {
         SextanteGUI.getGUIFactory().showBatchProcessingDialog(m_Alg, m_ParentDialog.getDialog());
      }

   }


   protected void executeAsBatchInGIS() {

      if (m_Alg != null) {
         SextanteGUI.getGUIFactory().showBatchProcessingFromGISDialog(m_Alg, m_ParentDialog.getDialog());
      }

   }


   protected void executeSelectedAlgorithm() {

      try {
         if (m_Alg != null) {
            final GeoAlgorithm alg = m_Alg.getNewInstance();
            final int iRet = SextanteGUI.getGUIFactory().showAlgorithmDialog(alg, m_ParentDialog.getDialog(), null);
            if (iRet == IGUIFactory.OK) {
               final String[] cmd = alg.getAlgorithmAsCommandLineSentences();
               if (cmd != null) {
                  History.addToHistory(cmd);
               }
               GeoAlgorithmExecutors.execute(alg, m_ParentDialog.getDialog());
               updateListOfMostRecentAlgorithms();
            }
         }
         else if (m_Action != null) {
            m_Action.execute();
         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   private void updateListOfMostRecentAlgorithms() {

      final boolean bShowMostRecent = new Boolean(SextanteGUI.getSettingParameterValue(SextanteGeneralSettings.SHOW_MOST_RECENT)).booleanValue();
      if (bShowMostRecent) {
         final GeoAlgorithm[] recent = History.getRecentlyUsedAlgs();
         final DefaultMutableTreeNode recentNode = new DefaultMutableTreeNode(Sextante.getText("RecentAlgorithms"));
         for (int i = 0; i < recent.length; i++) {
            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(recent[i]);
            recentNode.add(node);
         }
         final DefaultMutableTreeNode mainNode = (DefaultMutableTreeNode) new TreePath(jTree.getModel().getRoot()).getLastPathComponent();
         mainNode.remove(0);
         mainNode.insert(recentNode, 0);
      }

   }


   protected void executeSelectedAlgorithmIteratively(final int iParameterToIterateOver) {

      try {
         if (m_Alg != null) {
            final GeoAlgorithm alg = m_Alg.getNewInstance();
            final int iRet = SextanteGUI.getGUIFactory().showAlgorithmDialog(alg, m_ParentDialog.getDialog(), null);
            if (iRet == IGUIFactory.OK) {
               final ParametersSet params = m_Alg.getParameters();
               final int iParamCount = params.getNumberOfParameters();
               int iVectorLayers = 0;
               for (int i = 0; i < iParamCount; i++) {
                  final Parameter param = m_Alg.getParameters().getParameter(i);
                  if (param instanceof ParameterVectorLayer) {
                     if (iVectorLayers == iParameterToIterateOver) {
                        GeoAlgorithmExecutors.executeIterative(alg, m_ParentDialog.getDialog(), param.getParameterName());
                        break;
                     }
                     iVectorLayers++;
                  }
               }
            }
         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   public GeoAlgorithm getSelectedAlgorithm() {

      return m_Alg;

   }


   /**
    * Fills the tree with the algorithms that match a search criteria
    * 
    * @param sSearchString
    *                The search string to look for in the algorithms context help
    * @param bSearchInFiles
    *                true if it should search in help files. if false, it will only search in algorithm names
    * @return the number of algorithms that match the given criteria
    */
   public int fillTree(final String sSearchString,
                       final boolean bSearchInHelpFiles) {

      m_sLastSearchString = sSearchString;
      m_bLastSearchIncludedHelpFiles = bSearchInHelpFiles;

      int iCount = 0;
      String sGroup, sSubgroup;
      final DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(Sextante.getText("Algorithms"));
      final HashMap<Object, HashMap<String, HashMap<String, DefaultMutableTreeNode>>> groups = new HashMap<Object, HashMap<String, HashMap<String, DefaultMutableTreeNode>>>();

      setCursor(new Cursor(Cursor.WAIT_CURSOR));

      //algorithms
      final HashMap<String, HashMap<String, GeoAlgorithm>> algs = Sextante.getAlgorithms();
      final Set<String> groupKeys = algs.keySet();
      final Iterator<String> groupIter = groupKeys.iterator();
      while (groupIter.hasNext()) {
         final String groupKey = groupIter.next();
         final HashMap<String, GeoAlgorithm> groupAlgs = algs.get(groupKey);
         final Set keys = groupAlgs.keySet();
         final Iterator iter = keys.iterator();
         while (iter.hasNext()) {
            final GeoAlgorithm alg = groupAlgs.get(iter.next());
            if (m_Filter.accept(alg)) {
               if (bSearchInHelpFiles) {
                  if (!HelpIO.containsStringInHelpFile(alg, sSearchString)) {
                     continue;
                  }
               }
               else {
                  if ((sSearchString != null) && !alg.getName().toLowerCase().contains(sSearchString)) {
                     continue;
                  }
               }
               iCount++;
               final AlgorithmGroupConfiguration conf = AlgorithmGroupsOrganizer.getGroupConfiguration(alg);
               if (conf != null) {
                  if (!conf.isShow()) {
                     continue;
                  }
                  sGroup = conf.getGroup();
                  sSubgroup = conf.getSubgroup();
               }
               else {
                  sGroup = groupKey;
                  sSubgroup = alg.getGroup();
               }
               HashMap<String, HashMap<String, DefaultMutableTreeNode>> group = groups.get(sGroup);
               if (group == null) {
                  group = new HashMap<String, HashMap<String, DefaultMutableTreeNode>>();
                  groups.put(sGroup, group);
               }
               HashMap<String, DefaultMutableTreeNode> subgroup = group.get(sSubgroup);
               if (subgroup == null) {
                  subgroup = new HashMap<String, DefaultMutableTreeNode>();
                  group.put(sSubgroup, subgroup);
               }
               subgroup.put(alg.getName(), new DefaultMutableTreeNode(alg));
            }
         }
      }

      //toolbox actions
      final HashMap<NameAndIcon, ArrayList<ToolboxAction>> allActions = SextanteGUI.getToolboxActions();
      final Set<NameAndIcon> actionsKeys = allActions.keySet();
      final Iterator<NameAndIcon> actionsIter = actionsKeys.iterator();
      while (actionsIter.hasNext()) {
         final NameAndIcon nai = actionsIter.next();
         final ArrayList<ToolboxAction> actions = allActions.get(nai);
         for (int i = 0; i < actions.size(); i++) {
            final ToolboxAction ita = actions.get(i);
            if ((sSearchString != null) && !ita.getName().toLowerCase().contains(sSearchString)) {
               continue;
            }
            iCount++;
            sSubgroup = ita.getGroup();
            HashMap<String, HashMap<String, DefaultMutableTreeNode>> group = groups.get(nai.getName());
            if (group == null) {
               group = groups.get(nai);
            }
            if (group == null) {
               group = new HashMap<String, HashMap<String, DefaultMutableTreeNode>>();
               groups.put(nai, group);
            }
            HashMap<String, DefaultMutableTreeNode> subgroup = group.get(sSubgroup);
            if (subgroup == null) {
               subgroup = new HashMap<String, DefaultMutableTreeNode>();
               group.put(sSubgroup, subgroup);
            }
            subgroup.put(ita.getName(), new DefaultMutableTreeNode(ita));

         }
      }


      final Set<Object> set = groups.keySet();
      final Iterator<Object> iter = set.iterator();
      while (iter.hasNext()) {
         final Object key = iter.next();
         final DefaultMutableTreeNode node = new DefaultMutableTreeNode(key);
         addNodeInSortedOrder(mainNode, node);
         final HashMap<String, HashMap<String, DefaultMutableTreeNode>> g = groups.get(key);
         final Set<String> set2 = g.keySet();
         final Iterator<String> iter2 = set2.iterator();
         while (iter2.hasNext()) {
            final String sKey2 = iter2.next();
            final DefaultMutableTreeNode node2 = new DefaultMutableTreeNode(sKey2);
            addNodeInSortedOrder(node, node2);
            final HashMap<String, DefaultMutableTreeNode> g2 = g.get(sKey2);
            final Set<String> set3 = g2.keySet();
            final Iterator<String> iter3 = set3.iterator();
            while (iter3.hasNext()) {
               final String sKey3 = iter3.next();
               final DefaultMutableTreeNode node3 = g2.get(sKey3);
               addNodeInSortedOrder(node2, node3);
            }
         }
      }

      final boolean bShowMostRecent = new Boolean(SextanteGUI.getSettingParameterValue(SextanteGeneralSettings.SHOW_MOST_RECENT)).booleanValue();
      if (bShowMostRecent) {
         final GeoAlgorithm[] recent = History.getRecentlyUsedAlgs();
         final DefaultMutableTreeNode recentNode = new DefaultMutableTreeNode(Sextante.getText("RecentAlgorithms"));
         for (int i = 0; i < recent.length; i++) {
            final DefaultMutableTreeNode node = new DefaultMutableTreeNode(recent[i]);
            recentNode.add(node);
         }
         mainNode.insert(recentNode, 0);
      }

      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

      jTree.setModel(new DefaultTreeModel(mainNode));

      if (sSearchString != null) {
         expandAll();
      }

      m_ParentDialog.setAlgorithmsCount(iCount);

      return iCount;

   }


   private void addNodeInSortedOrder(final DefaultMutableTreeNode parent,
                                     final DefaultMutableTreeNode child) {

      final int n = parent.getChildCount();
      if (n == 0) {
         parent.add(child);
         return;
      }
      final Collator collator = Collator.getInstance();
      collator.setStrength(Collator.PRIMARY);
      DefaultMutableTreeNode node = null;
      for (int i = 0; i < n; i++) {
         node = (DefaultMutableTreeNode) parent.getChildAt(i);
         try {
            if (collator.compare(node.toString(), child.toString()) > 0) {
               parent.insert(child, i);
               return;
            }
         }
         catch (final Exception e) {
         }
      }
      parent.add(child);

   }

}
