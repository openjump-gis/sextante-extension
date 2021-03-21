package es.unex.sextante.gui.history;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import bsh.EvalError;
import bsh.Interpreter;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.LogElement;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.core.SextanteLogHandler;
import es.unex.sextante.dataObjects.IDataObject;
import es.unex.sextante.gui.core.NamedPoint;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.toolbox.TransparentScrollPane;

public class HistoryPanel
         extends
            JPanel {

   private JTree                                          jTree;
   private JScrollPane                                    jScrollPane;
   private JMenuItem                                      menuItemExecute;
   private JMenuItem                                      menuItemShowExecuteDialog;
   private JPopupMenu                                     popupMenu;
   private TreePath                                       m_Path;
   private DateAndCommand                                 m_Command;
   private ArrayList<DateAndCommand>                      m_Commands;
   private final HashMap<String, DefaultMutableTreeNode>  m_NodesMap   = new HashMap<String, DefaultMutableTreeNode>();
   private final HashMap<DefaultMutableTreeNode, Boolean> m_NodeStatus = new HashMap<DefaultMutableTreeNode, Boolean>();
   private JSplitPane                                     jSplitPane;
   private NonWordWrapPane                                jNonWordWrapPane;
   private TransparentScrollPane                          jScrollPaneText;


   //   private final ArrayList<LoggingPanel>                  m_LoggingPanels = new ArrayList<LoggingPanel>();


   public HistoryPanel() {

      super();

      init();

   }


   private void init() {

      this.setPreferredSize(new java.awt.Dimension(650, 380));
      this.setSize(new java.awt.Dimension(650, 380));

      final BorderLayout thisLayout = new BorderLayout();
      this.setLayout(thisLayout);

      jSplitPane = new JSplitPane();

      jTree = new JTree();
      final MouseListener ml = new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            m_Command = null;
            menuItemExecute.setVisible(false);
            menuItemShowExecuteDialog.setVisible(false);
            m_Path = jTree.getPathForLocation(e.getX(), e.getY());
            if (m_Path != null) {
               try {
                  DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
                  Object obj = node.getUserObject();
                  if (obj instanceof DateAndCommand) {
                     m_Command = (DateAndCommand) obj;
                     menuItemExecute.setVisible(true);
                     String sAlg = m_Command.getCommand();
                     if (sAlg.startsWith("runalg")) {
                        sAlg = sAlg.substring("runalg(\"".length());
                        sAlg = sAlg.substring(0, sAlg.indexOf("\""));
                        GeoAlgorithm alg = Sextante.getAlgorithmFromCommandLineName(sAlg);
                        IDataObject[] objs = SextanteGUI.getInputFactory().getDataObjects();
                        if (alg.meetsDataRequirements(objs)) {
                           menuItemShowExecuteDialog.setVisible(true);
                        }
                     }
                     jNonWordWrapPane.setText(m_Command.getAsFullText());
                  }
                  else if (obj instanceof LogElement) {
                     jNonWordWrapPane.setText(((LogElement) obj).getAsText());
                  }

               }
               catch (Exception ex) {}
            }
            if (e.getButton() == MouseEvent.BUTTON1) {
               if ((e.getClickCount() == 2) && (m_Command != null)) {
                  executeSelectedCommand();
               }
            }
            else if ((e.getButton() == MouseEvent.BUTTON3) && (m_Command != null)) {
               showPopupMenu(e);
            }
         }
      };
      jTree.addMouseListener(ml);

      jScrollPane = new JScrollPane();
      jScrollPane.setViewportView(jTree);
      jScrollPane.setSize(new java.awt.Dimension(650, 380));

      jSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
      jSplitPane.setLeftComponent(jScrollPane);

      jNonWordWrapPane = new NonWordWrapPane();
      jNonWordWrapPane.setStyledDocument(new DefaultStyledDocument());
      jNonWordWrapPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
      jNonWordWrapPane.setContentType("text/plain");
      jNonWordWrapPane.setEditable(false);
      jScrollPaneText = new TransparentScrollPane();
      jScrollPaneText.setBackground(Color.white);
      jScrollPaneText.setSize(new java.awt.Dimension(650, 380));
      jScrollPaneText.setViewportView(jNonWordWrapPane);
      jSplitPane.setRightComponent(jScrollPaneText);

      this.add(jSplitPane);

      jSplitPane.setDividerLocation(150);

      updateContent();

      popupMenu = new JPopupMenu("Menu");
      menuItemExecute = new JMenuItem(Sextante.getText("Run"));
      menuItemExecute.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            executeSelectedCommand();
         }
      });
      popupMenu.add(menuItemExecute);
      menuItemShowExecuteDialog = new JMenuItem(Sextante.getText("Show_algorithm_dialog"));
      menuItemShowExecuteDialog.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            showExecuteDialog();
         }
      });
      popupMenu.add(menuItemShowExecuteDialog);
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

   }


   protected void showExecuteDialog() {

      if (m_Command == null) {
         return;
      }

      String sAlg = m_Command.getCommand();

      if (!sAlg.startsWith("runalg")) {
         return;
      }

      sAlg = sAlg.substring("runalg(\"".length());
      sAlg = sAlg.substring(0, sAlg.indexOf("\""));

      final GeoAlgorithm alg = Sextante.getAlgorithmFromCommandLineName(sAlg);
      final ArrayList<DateAndCommand> list = new ArrayList<DateAndCommand>();
      list.add(m_Command);

      SextanteGUI.getGUIFactory().showAlgorithmDialog(alg, null, list);

   }


   protected void showPopupMenu(final MouseEvent e) {

      jTree.setSelectionPath(m_Path);
      popupMenu.show(e.getComponent(), e.getX(), e.getY());

   }


   public void collapseAll() {

      try {
         final TreeNode root = (TreeNode) jTree.getModel().getRoot();
         final TreePath path = new TreePath(root);
         expandAll(jTree, path, false);
         jTree.expandPath(path);
         final TreeNode node = root.getChildAt(0);
         final TreePath subpath = path.pathByAddingChild(node);
         jTree.expandPath(subpath);
      }
      catch (final Exception e) {}

   }


   public void expandAll() {

      final TreeNode root = (TreeNode) jTree.getModel().getRoot();
      expandAll(jTree, new TreePath(root), true);

   }


   private void expandAll(final JTree tree,
                          final TreePath parent,
                          final boolean expand) {

      try {
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
      catch (final Exception e) {}

   }


   private void executeSelectedCommand() {

      if (m_Command == null) {
         return;
      }

      final Runnable run = new Runnable() {

         public void run() {
            try {
               Interpreter interpreter = new Interpreter();
               interpreter.getNameSpace().importCommands("es.unex.sextante.gui.cmd.bshcommands");
               interpreter.eval(m_Command.getCommand());
            }
            catch (EvalError e) {
               JOptionPane.showMessageDialog(null, e.getMessage(), Sextante.getText("Warning"), JOptionPane.ERROR_MESSAGE);
            }
         }
      };

      final Thread th = new Thread(run);
      th.start();

   }


   public void updateContent() {

      jNonWordWrapPane.setText("");

      DefaultMutableTreeNode node;
      final DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(Sextante.getText("History"));

      final DefaultMutableTreeNode commandsNode = new DefaultMutableTreeNode(Sextante.getText("Commands"));

      DefaultMutableTreeNode child;
      DateAndCommand dac;

      m_Commands = History.getHistory();
      String sDay;

      Set<String> set = m_NodesMap.keySet();
      Iterator<String> iterator = set.iterator();
      while (iterator.hasNext()) {
         final String key = iterator.next();
         node = m_NodesMap.get(key);
         if (node != null) {
            final boolean bExpanded = jTree.isExpanded(getPath(node));
            m_NodeStatus.put(node, new Boolean(bExpanded));
            node.removeAllChildren();
         }
      }

      for (int j = 0; j < m_Commands.size(); j++) {
         dac = m_Commands.get(j);
         child = new DefaultMutableTreeNode(dac);
         sDay = dac.getDay();
         if (m_NodesMap.containsKey(sDay)) {
            node = m_NodesMap.get(sDay);
         }
         else {
            node = new DefaultMutableTreeNode(sDay);
            m_NodesMap.put(sDay, node);
         }
         node.insert(child, 0);
      }

      node = m_NodesMap.get(Sextante.getText("This_session"));
      if (node != null) {
         if (node.getChildCount() != 0) {
            commandsNode.add(node);
         }
      }
      node = m_NodesMap.get(Sextante.getText("Today"));
      if (node != null) {
         if (node.getChildCount() != 0) {
            commandsNode.add(node);
         }
      }
      node = m_NodesMap.get(Sextante.getText("Yesterday"));
      if (node != null) {
         if (node.getChildCount() != 0) {
            commandsNode.add(node);
         }
      }
      for (int i = 0; i < 30; i++) {
         String s = Sextante.getText("XXX_days_ago");
         s = s.replace("XXX", Integer.toString(i));
         node = m_NodesMap.get(s);
         if (node != null) {
            if (node.getChildCount() != 0) {
               commandsNode.add(node);
            }
         }
      }
      node = m_NodesMap.get(Sextante.getText("More_than_one_month_ago"));
      if (node != null) {
         if (node.getChildCount() != 0) {
            commandsNode.add(node);
         }
      }

      mainNode.add(commandsNode);

      //add log elements
      final DefaultMutableTreeNode logElementsNode = new DefaultMutableTreeNode(Sextante.getText("Log_messages"));
      mainNode.add(logElementsNode);

      final HashMap<String, DefaultMutableTreeNode> map = new HashMap<String, DefaultMutableTreeNode>();
      final ArrayList<LogElement> elements = Sextante.getLogger().getLogElements();
      for (int i = 0; i < elements.size(); i++) {
         final LogElement element = elements.get(elements.size() - 1 - i);
         if (map.containsKey(element.getType())) {
            node = map.get(element.getType());
         }
         else {
            node = new DefaultMutableTreeNode(element.getType());
            map.put(element.getType(), node);
         }
         node.add(new DefaultMutableTreeNode(element));
      }

      set = map.keySet();
      iterator = set.iterator();
      while (iterator.hasNext()) {
         final String key = iterator.next();
         logElementsNode.add(map.get(key));
      }

      //add points
      final DefaultMutableTreeNode pointsNode = new DefaultMutableTreeNode(Sextante.getText("Points"));
      mainNode.add(pointsNode);

      final ArrayList<NamedPoint> coords = SextanteGUI.getGUIFactory().getCoordinatesList();

      for (int i = 0; i < coords.size(); i++) {
         pointsNode.add(new DefaultMutableTreeNode(coords.get(i).toStringFull()));
      }

      jTree.setModel(new DefaultTreeModel(mainNode));

      set = m_NodesMap.keySet();
      iterator = set.iterator();
      while (iterator.hasNext()) {
         final String key = iterator.next();
         node = m_NodesMap.get(key);
         if (node != null) {
            final Boolean expanded = m_NodeStatus.get(node);
            if (expanded != null) {
               if (expanded.booleanValue()) {
                  jTree.expandPath(getPath(node));
               }
            }
         }
      }


   }


   private TreePath getPath(TreeNode node) {

      final ArrayList list = new ArrayList();

      // Add all nodes to list
      while (node != null) {
         list.add(node);
         node = node.getParent();
      }
      Collections.reverse(list);

      // Convert array of nodes to TreePath
      return new TreePath(list.toArray());
   }


   public void clearHistory() {

      History.clear();
      updateContent();

   }


   public void clearLog() {

      final SextanteLogHandler handler = Sextante.getLogger();
      handler.clear();

      updateContent();

   }

}
