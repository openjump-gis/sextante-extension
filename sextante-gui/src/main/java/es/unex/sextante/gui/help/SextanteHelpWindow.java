package es.unex.sextante.gui.help;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.IAlgorithmProvider;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.grass.GrassAlgorithm;

public class SextanteHelpWindow
extends
JFrame {

	private JSplitPane   jSplitPane;
	private JTree        jTree;
	private JEditorPane  jEditorPane;
	private JScrollPane  jScrollPane;
	private JScrollPane  jScrollPanePage;
	private TreePath     m_Path;
	private JMenuItem    menuItemEditHelp;
	private JPopupMenu   popupMenu;
	private GeoAlgorithm m_Alg;


	public SextanteHelpWindow() {

		super(Sextante.getText("Help"));

		initGUI();

	}


	private void initGUI() {

		try {
			{
				final BorderLayout thisLayout = new BorderLayout();
				this.setLayout(thisLayout);
				this.setPreferredSize(new java.awt.Dimension(800, 500));
				this.setSize(new java.awt.Dimension(800, 500));
				{
					jSplitPane = new JSplitPane();
					this.add(jSplitPane, BorderLayout.CENTER);
					{
						jTree = new JTree();
						jTree.setCellRenderer(new AlgorithmTreeCellRenderer());
						final MouseListener ml = new MouseAdapter() {
							@Override
							public void mousePressed(MouseEvent e) {
								m_Path = jTree.getPathForLocation(e.getX(), e.getY());
								showHelp(m_Path);
								jTree.setSelectionPath(m_Path);
								if ((e.getButton() == MouseEvent.BUTTON3) && (m_Path != null)) {
									DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
									Object ob = node.getUserObject();
									if (ob instanceof GeoAlgorithm) {
										m_Alg = ((GeoAlgorithm) ob);
										showPopupMenu(e);
									}
								}
							}
						};
						jTree.addMouseListener(ml);
						jTree.addTreeSelectionListener(new TreeSelectionListener() {
							@Override
							public void valueChanged(final TreeSelectionEvent e) {
								m_Path = e.getPath();
								if (m_Path != null) {
									showHelp(m_Path);
								}
								final DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
								final Object ob = node.getUserObject();
								if (ob instanceof GeoAlgorithm) {
									m_Alg = ((GeoAlgorithm) ob);
								}
							}
						});

						jTree.addKeyListener(new KeyListener() {
							@Override
							public void keyPressed(final KeyEvent e) {}


							@Override
							public void keyReleased(final KeyEvent e) {}


							@Override
							public void keyTyped(final KeyEvent e) {
								if (e.getKeyChar() == KeyEvent.VK_SPACE) {
									final DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
									final Object ob = node.getUserObject();
									if (ob instanceof GeoAlgorithm) {
										showPopupMenu(e);
									}
								}
							}
						});

						fillTree();

						jEditorPane = new JEditorPane();
						jEditorPane.setEditable(false);
						jEditorPane.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
						jEditorPane.setContentType("text/html");
						jScrollPane = new JScrollPane(jTree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
								ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						jScrollPanePage = new JScrollPane(jEditorPane, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
								ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						jScrollPane.setPreferredSize(new Dimension(300, 450));
						jScrollPane.setMinimumSize(new Dimension(300, 450));
					}
					{
						jSplitPane.add(jScrollPanePage, JSplitPane.RIGHT);
						jSplitPane.add(jScrollPane, JSplitPane.LEFT);
					}
				}
			}
			popupMenu = new JPopupMenu("Menu");

			menuItemEditHelp = new JMenuItem(Sextante.getText("Edit_help"));
			menuItemEditHelp.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent evt) {
					editHelp();
				}
			});
			popupMenu.add(menuItemEditHelp);

		}
		catch (final Exception e) {
			Sextante.addErrorToLog(e);
		}
	}


	protected void editHelp() {

		SextanteGUI.getGUIFactory().showHelpEditionDialog(m_Alg);

	}


	protected void showPopupMenu(final MouseEvent e) {

		boolean bCanEdit = true;
		final ArrayList<IAlgorithmProvider> providers = SextanteGUI.getAlgorithmProviders();
		final String sName = Sextante.getAlgorithmProviderName(m_Alg);
		for (int i = 0; i < providers.size(); i++) {
			if (providers.get(i).getName().equals(sName)) {
				bCanEdit = providers.get(i).canEditHelp();
			}
		}

		if ((m_Alg != null) && bCanEdit) {
			jTree.setSelectionPath(m_Path);
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}

	}


	protected void showPopupMenu(final KeyEvent e) {

		if ((m_Alg != null) && !(m_Alg instanceof GrassAlgorithm)) {
			jTree.setSelectionPath(m_Path);
			final Rectangle pathBounds = jTree.getPathBounds(m_Path);
			popupMenu.show(e.getComponent(), pathBounds.x, pathBounds.y);
		}

	}


	protected void showHelp(final TreePath path) {

		if (path != null) {
			try {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				final Object ob = node.getUserObject();
				if (ob instanceof GeoAlgorithm) {
					final Object help = SextanteGUI.getAlgorithmHelp((GeoAlgorithm) ob);
					if (help instanceof String) {
						jEditorPane.setText((String) help);
					}
					else if (help instanceof URL) {
						jEditorPane.setPage((URL) help);
					}
				}
				else if (ob instanceof ObjectAndDescription) {
					final ObjectAndDescription oad = (ObjectAndDescription) ob;
					final String sHtmlFile = (String) oad.getObject();
					try {
						final URL url = new URL("file:///" + sHtmlFile);
						jEditorPane.setPage(url);
					}
					catch (final Exception e) {
						//will show a blank page
					}
				}
				jEditorPane.setCaretPosition(0);
			}
			catch (final Exception e) {
				//Sextante.addErrorToLog(e);
			}

		}

	}


	public void fillTree() {

		DefaultMutableTreeNode node;
		final DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(Sextante.getText("Help"));
		final DefaultMutableTreeNode algsNode = new DefaultMutableTreeNode(Sextante.getText("Algorithms"));
		DefaultMutableTreeNode child;

		final DefaultMutableTreeNode generalNode1 = new DefaultMutableTreeNode(Sextante.getText("Basic_concepts"));
		generalNode1.add(new DefaultMutableTreeNode(new ObjectAndDescription("Introduction",
				HelpIO.getHelpFile("intro"))));
		generalNode1.add(new DefaultMutableTreeNode(new ObjectAndDescription("Sextante and OpenJUMP",
				HelpIO.getHelpFile("openjump"))));
		generalNode1.add(new DefaultMutableTreeNode(new ObjectAndDescription("List of algorithms",
				HelpIO.getHelpFile("sextante_algo"))));
		generalNode1.add(new DefaultMutableTreeNode(new ObjectAndDescription("Change log",
				HelpIO.getHelpFile("changeLog"))));

		mainNode.add(generalNode1);
		final DefaultMutableTreeNode generalNode = new DefaultMutableTreeNode("PlugIns");

		generalNode.add(new DefaultMutableTreeNode(new ObjectAndDescription(Sextante.getText("SEXTANTE_toolbox"),
				HelpIO.getHelpFile("toolbox"))));
		generalNode.add(new DefaultMutableTreeNode(new ObjectAndDescription(Sextante.getText("Batch_processing"),
				HelpIO.getHelpFile("batch"))));
		generalNode.add(new DefaultMutableTreeNode(new ObjectAndDescription(Sextante.getText("Models"),
				HelpIO.getHelpFile("modeler"))));
		generalNode.add(new DefaultMutableTreeNode(new ObjectAndDescription(Sextante.getText("Command_line"),
				HelpIO.getHelpFile("cmd"))));
		generalNode.add(new DefaultMutableTreeNode(new ObjectAndDescription(Sextante.getText("History"),
				HelpIO.getHelpFile("history"))));
		generalNode.add(new DefaultMutableTreeNode(new ObjectAndDescription("Explore data",
				HelpIO.getHelpFile("explorer"))));
		generalNode.add(new DefaultMutableTreeNode(new ObjectAndDescription("Pick coordinates",
				HelpIO.getHelpFile("coordinates"))));
		generalNode.add(new DefaultMutableTreeNode(new ObjectAndDescription(Sextante.getText("ConfiguringProviders"),
				HelpIO.getHelpFile("providers"))));

		mainNode.add(generalNode);

		mainNode.add(algsNode);

		setCursor(new Cursor(Cursor.WAIT_CURSOR));

		final HashMap<String, HashMap<String, GeoAlgorithm>> algs = Sextante.getAlgorithms();
		final Set<String> groupKeys = algs.keySet();
		final Iterator<String> groupIter = groupKeys.iterator();
		while (groupIter.hasNext()) {
			final HashMap<String, DefaultMutableTreeNode> baseGroups = new HashMap<String, DefaultMutableTreeNode>();
			final String groupKey = groupIter.next();
			final DefaultMutableTreeNode toolsNode = new DefaultMutableTreeNode(groupKey);
			algsNode.add(toolsNode);
			final HashMap<String, GeoAlgorithm> groupAlgs = algs.get(groupKey);
			final Set keys = groupAlgs.keySet();
			final Iterator iter = keys.iterator();
			while (iter.hasNext()) {
				final GeoAlgorithm alg = groupAlgs.get(iter.next());
				child = new DefaultMutableTreeNode(alg);
				node = baseGroups.get(alg.getGroup());
				if (node == null) {
					node = new DefaultMutableTreeNode(alg.getGroup());
					baseGroups.put(alg.getGroup(), node);
					addNodeInSortedOrder(toolsNode, node);
				}
				addNodeInSortedOrder(node, child);
			}

		}
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

		jTree.setModel(new DefaultTreeModel(mainNode));

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


	private DefaultMutableTreeNode findNode(final GeoAlgorithm alg) {

		Object ob;
		final String sName = alg.getName();
		final DefaultTreeModel data = (DefaultTreeModel) jTree.getModel();
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) data.getRoot();
		DefaultMutableTreeNode node = null;

		//      if (alg instanceof GrassAlgorithm) {//GRASS algorithms: need to check for siblings
		//         if (sName.contains("(")) {
		//            //We have a sibling algorithm: link to parent's node instead
		//            sName = sName.substring(0, sName.indexOf("(") - 1);
		//         }
		//      }

		if (root != null) {
			for (final Enumeration e = root.breadthFirstEnumeration(); e.hasMoreElements();) {
				final DefaultMutableTreeNode current = (DefaultMutableTreeNode) e.nextElement();
				ob = current.getUserObject();
				if (ob instanceof GeoAlgorithm) {
					if (((GeoAlgorithm) ob).getName().equals(sName)) {
						node = current;
						break;
					}
				}
			}
		}

		return node;

	}


	//   private DefaultMutableTreeNode findNode(final String sNodeName) {
	//
	//      Object ob;
	//      final DefaultTreeModel data = (DefaultTreeModel) jTree.getModel();
	//      final DefaultMutableTreeNode root = (DefaultMutableTreeNode) data.getRoot();
	//      DefaultMutableTreeNode node = null;
	//
	//      if (root != null) {
	//         for (final Enumeration e = root.breadthFirstEnumeration(); e.hasMoreElements();) {
	//            final DefaultMutableTreeNode current = (DefaultMutableTreeNode) e.nextElement();
	//            ob = current.getUserObject();
	//            if (ob instanceof ObjectAndDescription) {
	//               final ObjectAndDescription oad = (ObjectAndDescription) ob;
	//               final String s = oad.getDescription();
	//               if (s.equals(sNodeName)) {
	//                  node = current;
	//                  break;
	//               }
	//
	//            }
	//         }
	//      }
	//
	//      return node;
	//
	//   }

}
