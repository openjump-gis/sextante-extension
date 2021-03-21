package es.unex.sextante.gui.modeler;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.Collator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import es.unex.sextante.additionalInfo.AdditionalInfoDataObject;
import es.unex.sextante.additionalInfo.AdditionalInfoMultipleInput;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.exceptions.NullParameterAdditionalInfoException;
import es.unex.sextante.exceptions.WrongParameterIDException;
import es.unex.sextante.gui.core.IGUIFactory;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.help.HelpIO;
import es.unex.sextante.gui.history.History;
import es.unex.sextante.gui.settings.SextanteGeneralSettings;
import es.unex.sextante.gui.toolbox.AlgorithmGroupConfiguration;
import es.unex.sextante.gui.toolbox.AlgorithmGroupsOrganizer;
import es.unex.sextante.modeler.elements.ModelElement3DRasterLayer;
import es.unex.sextante.modeler.elements.ModelElementPoint;
import es.unex.sextante.modeler.elements.ModelElementRasterLayer;
import es.unex.sextante.modeler.elements.ModelElementTable;
import es.unex.sextante.modeler.elements.ModelElementVectorLayer;
import es.unex.sextante.parameters.Parameter;
import es.unex.sextante.parameters.ParameterDataObject;
import es.unex.sextante.parameters.ParameterMultipleInput;

public class AlgorithmsPanel
         extends
            JPanel {

   private int                m_iAlgorithm = 0;
   private final HashMap      m_DataObjects;
   private ModelAlgorithm     m_Algorithm;
   private JTree              jTree;
   private JScrollPane        jScrollPane;
   private JButton            jButtonSearch;
   private JTextField         jTextFieldSearch;
   private final ModelerPanel m_ModelerPanel;
   private final JDialog      m_Parent;


   public AlgorithmsPanel(final ModelerPanel modelerPanel,
                          final JDialog parent) {

      super();

      m_DataObjects = modelerPanel.getDataObjects();
      m_ModelerPanel = modelerPanel;
      m_Parent = parent;

      initGUI();

   }


   private void initGUI() {

      final TableLayout thisLayout = new TableLayout(new double[][] { { 3.0, TableLayoutConstants.FILL, 65.0, 3.0 },
               { TableLayoutConstants.FILL, TableLayoutConstants.MINIMUM, 3.0 } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      this.setLayout(thisLayout);
      jTree = new JTree();
      jTree.setCellRenderer(new AlgorithmCellRenderer(this));
      final MouseListener ml = new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            TreePath path = jTree.getPathForLocation(e.getX(), e.getY());
            if (e.getClickCount() == 2) {
               addSelectedProcess(path);
            }
         }
      };
      jTree.addMouseListener(ml);

      fillTree(null, false);
      jScrollPane = new JScrollPane(jTree, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      jScrollPane.setPreferredSize(new java.awt.Dimension(200, 294));
      this.add(jScrollPane, "0, 0, 3, 0");
      this.add(getJTextFieldSearch(), "1, 1");
      this.add(getJButtonSearch(), "2, 1");

   }


   public void fillTree(final String sSearchString,
                        final boolean bSearchInHelpFiles) {

      String sGroup, sSubgroup;
      final DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(Sextante.getText("Algorithms"));
      final HashMap<String, HashMap<String, HashMap<String, DefaultMutableTreeNode>>> groups = new HashMap<String, HashMap<String, HashMap<String, DefaultMutableTreeNode>>>();

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
            if (alg.isSuitableForModelling()) {
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

      final Set<String> set = groups.keySet();
      final Iterator<String> iter = set.iterator();
      while (iter.hasNext()) {
         final String sKey = iter.next();
         final DefaultMutableTreeNode node = new DefaultMutableTreeNode(sKey);
         addNodeInSortedOrder(mainNode, node);
         final HashMap<String, HashMap<String, DefaultMutableTreeNode>> g = groups.get(sKey);
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

      jTree.setModel(new DefaultTreeModel(mainNode));

      collapseAll();

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
         catch (final Exception e) {}
      }
      parent.add(child);

   }


   public void setAlgorithm(final ModelAlgorithm alg) {

      m_Algorithm = alg;

   }


   private void addSelectedProcess(final TreePath path) {

      if (path == null) {
         return;
      }

      try {
         final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
         final GeoAlgorithm alg = (GeoAlgorithm) node.getUserObject();
         if (isAlgorithmEnabled(alg)) {
            final String sName = getValidName();
            final String sDescription = Sextante.getText("Process") + " " + Integer.toString(m_iAlgorithm) + ": " + alg.getName();

            final int iRet = SextanteGUI.getGUIFactory().showAlgorithmDialogForModeler(alg, sName, sDescription, m_Algorithm,
                     m_DataObjects, m_Parent);
            if (iRet == IGUIFactory.OK) {
               m_iAlgorithm++;
               m_ModelerPanel.setHasChanged(true);
               m_ModelerPanel.getGraph().storeCoords();
               m_ModelerPanel.getGraph().addAlgorithm(sName);
               m_ModelerPanel.updatePanel(true);
            }
         }
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }

   }


   public boolean isAlgorithmEnabled(final GeoAlgorithm alg) {

      boolean bRequiresRasterLayers = alg.requiresRasterLayers();
      boolean bRequires3DRasterLayers = alg.requires3DRasterLayers();
      boolean bRequiresVectorLayers = alg.requiresVectorLayers();
      boolean bRequiresTables = alg.requiresTables();;
      boolean bRequiresPoints = alg.requiresPoints();
      boolean bRequiresPointLayers = alg.requiresPointVectorLayers();
      boolean bRequiresLineLayers = alg.requiresLineVectorLayers();
      boolean bRequiresPolygonLayers = alg.requiresPolygonVectorLayers();

      boolean bTableRequirementsMet = !bRequiresTables;
      boolean bRasterRequirementsMet = !bRequiresRasterLayers;
      boolean b3DRasterRequirementsMet = !bRequires3DRasterLayers;
      boolean bVectorRequirementsMet = !bRequiresVectorLayers;
      boolean bPointRequirementsMet = !bRequiresPoints;
      boolean bPolygonLayerRequirementsMet = !bRequiresPolygonLayers;
      boolean bLineLayerRequirementsMet = !bRequiresLineLayers;
      boolean bPointLayerRequirementsMet = !bRequiresPointLayers;
      boolean bAllRequirementsMet;

      final Set set = m_DataObjects.keySet();
      final Iterator iter = set.iterator();
      String sKey;
      Class dataClass;
      ObjectAndDescription oad;

      bAllRequirementsMet = bRasterRequirementsMet && b3DRasterRequirementsMet && bVectorRequirementsMet && bTableRequirementsMet
                            && bPointRequirementsMet && bPolygonLayerRequirementsMet && bLineLayerRequirementsMet
                            && bPointLayerRequirementsMet;

      while (iter.hasNext() && !bAllRequirementsMet) {
         sKey = (String) iter.next();
         oad = (ObjectAndDescription) m_DataObjects.get(sKey);
         dataClass = oad.getObject().getClass();
         if (dataClass == ModelElementRasterLayer.class) {
            if (isParameterMandatory(sKey)) {
               bRasterRequirementsMet = true;
            }
         }
         if (dataClass == ModelElement3DRasterLayer.class) {
            if (isParameterMandatory(sKey)) {
               b3DRasterRequirementsMet = true;
            }
         }
         if (dataClass == ModelElementPoint.class) {
            bPointRequirementsMet = true;
         }
         if (dataClass == ModelElementVectorLayer.class) {
            if (isParameterMandatory(sKey)) {
               final ModelElementVectorLayer mevl = (ModelElementVectorLayer) oad.getObject();
               bVectorRequirementsMet = true;
               switch (mevl.getShapeType()) {
                  case ModelElementVectorLayer.SHAPE_TYPE_LINE:
                     bLineLayerRequirementsMet = true;
                     break;
                  case ModelElementVectorLayer.SHAPE_TYPE_POINT:
                     bPointLayerRequirementsMet = true;
                     break;
                  case ModelElementVectorLayer.SHAPE_TYPE_POLYGON:
                     bPolygonLayerRequirementsMet = true;
                     break;
                  case ModelElementVectorLayer.SHAPE_TYPE_UNDEFINED:
                  default:
               }
            }
         }
         else if (dataClass == ModelElementTable.class) {
            if (isParameterMandatory(sKey)) {
               bTableRequirementsMet = true;
            }
         }

         bAllRequirementsMet = bRasterRequirementsMet && bVectorRequirementsMet && bTableRequirementsMet && bPointRequirementsMet
                               && bPolygonLayerRequirementsMet && bLineLayerRequirementsMet && bPointLayerRequirementsMet;

      }

      return bAllRequirementsMet;

   }


   private boolean isParameterMandatory(final String sKey) {

      final ParametersSet ps = m_Algorithm.getParameters();
      Parameter param;
      try {
         param = ps.getParameter(sKey);
      }
      catch (final WrongParameterIDException e) {
         return true;
      }

      if (param != null) {
         if (param instanceof ParameterDataObject) {
            AdditionalInfoDataObject additionalInfo;
            try {
               additionalInfo = (AdditionalInfoDataObject) ((ParameterDataObject) param).getParameterAdditionalInfo();
               return additionalInfo.getIsMandatory();
            }
            catch (final NullParameterAdditionalInfoException e) {
               return false;
            }

         }
         else if (param instanceof ParameterMultipleInput) {
            AdditionalInfoMultipleInput additionalInfo;
            try {
               additionalInfo = (AdditionalInfoMultipleInput) ((ParameterMultipleInput) param).getParameterAdditionalInfo();
               return additionalInfo.getIsMandatory();
            }
            catch (final NullParameterAdditionalInfoException e) {
               return false;
            }
         }
         else {
            return true;
         }
      }
      else {
         return true;
      }


   }


   public String getValidName() {

      boolean bFound;
      int i = 0;
      String sName;

      do {
         sName = "PROC" + Integer.toString(i);
         if (m_Algorithm.getAlgorithmKeys().contains(sName)) {
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


   public void addSelectedProcess() {

      addSelectedProcess(jTree.getSelectionPath());

   }


   public void setAlgorithmCount(final int iCount) {

      m_iAlgorithm = iCount;

   }


   private JTextField getJTextFieldSearch() {

      if (jTextFieldSearch == null) {
         jTextFieldSearch = new JTextField();
         jTextFieldSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(final KeyEvent event) {
               processKeyPresssed(event);
            }
         });
      }
      return jTextFieldSearch;

   }


   protected void processKeyPresssed(final KeyEvent event) {

      String sString;
      switch (event.getKeyChar()) {
         case KeyEvent.VK_ENTER:
            sString = escape(jTextFieldSearch.getText().trim().toLowerCase());
            searchString(sString, true);
            break;
         default:
            sString = jTextFieldSearch.getText() + event.getKeyChar();
            sString = escape(sString.trim().toLowerCase());
            searchString(sString, false);
            break;
      }

   }


   private JButton getJButtonSearch() {
      if (jButtonSearch == null) {
         jButtonSearch = new JButton();
         jButtonSearch.setText(Sextante.getText("Search"));
         jButtonSearch.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               final String sString = escape(jTextFieldSearch.getText().trim().toLowerCase());
               searchString(sString, true);
            }
         });
      }
      return jButtonSearch;
   }


   /*private void search() {

      final String sSearchString = escape(jTextFieldSearch.getText().trim().toLowerCase());

      fillTree(sSearchString);

      collapseAll();

   }*/


   protected void searchString(final String sString,
                               final boolean bSearchInHelpFiles) {

      fillTree(sString, bSearchInHelpFiles);

      if ((sString == null) || sString.trim().equals("")) {
         collapseAll();
      }
      else {
         expandAll();
      }

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


   /////////////methods adapted from Apache Jakarta Library////////////////
   private StringWriter createStringWriter(final String str) {
      return new StringWriter((int) (str.length() + (str.length() * 0.1)));
   }


   private String escape(final String str) {
      final StringWriter stringWriter = createStringWriter(str);
      try {
         this.escape(stringWriter, str);
      }
      catch (final IOException e) {
         // This should never happen because ALL the StringWriter methods called by #escape(Writer, String) do not
         // throw IOExceptions.
         //throw new UnhandledException(e);
      }
      return stringWriter.toString();
   }


   private void escape(final Writer writer,
                       final String str) throws IOException {

      final int len = str.length();
      for (int i = 0; i < len; i++) {
         final char c = str.charAt(i);
         if (c > 0x7F) {
            writer.write("&#");
            writer.write(Integer.toString(c, 10));
            writer.write(';');
         }
         else {
            writer.write(c);
         }
      }
   }
   //////////////////////////////////////////////////////////////////////

}
