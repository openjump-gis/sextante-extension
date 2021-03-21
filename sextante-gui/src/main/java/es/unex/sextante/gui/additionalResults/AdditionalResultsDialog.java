

package es.unex.sextante.gui.additionalResults;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;

import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.algorithm.GenericFileFilter;


public class AdditionalResultsDialog
extends
JDialog {

	private JSplitPane  jSplitPane;
	private JTree       jTree;
	private TreePath    m_Path;
	private JScrollPane jScrollPane;
	private JMenuItem   menuItemSave;
	private JPopupMenu  popupMenu;
	private JMenuItem   menuItemRemove;
	private JMenuItem   menuItemRename;


	public AdditionalResultsDialog(final ArrayList components,
			final Frame mainFrame) {

		super(mainFrame, Sextante.getText("Result"), true);

		initGUI(components);
		setLocationRelativeTo(null);

	}


	private boolean initGUI(final ArrayList components) {

		final JPanel panel = new JPanel();
		final BorderLayout thisLayout = new BorderLayout();
		panel.setLayout(thisLayout);
		this.setContentPane(panel);

		if (components.size() == 0) {
			return false;
		}
		try {
			{
				this.setPreferredSize(new java.awt.Dimension(700, 350));
				this.setSize(new java.awt.Dimension(700, 350));
				{
					jSplitPane = new JSplitPane();
					panel.add(jSplitPane, BorderLayout.CENTER);
					{
						jTree = new JTree();
						jTree.setCellRenderer(new AdditionalResultsTreeCellRenderer());
						final MouseListener ml = new MouseAdapter() {
							@Override
							public void mousePressed(MouseEvent e) {
								m_Path = jTree.getPathForLocation(e.getX(), e.getY());
								showComponent();
								if ((e.getButton() == MouseEvent.BUTTON3) && (m_Path != null)) {
									DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
									if (node.getUserObject() instanceof ObjectAndDescription) {
										showPopupMenu(e);
									}
								}
							}
						};
						jTree.addMouseListener(ml);

						fillTree(components);

						if (components.size() > 0) {
							final DefaultMutableTreeNode node = findNode((ObjectAndDescription) components.get(components.size() - 1));
							final DefaultTreeModel model = (DefaultTreeModel) jTree.getModel();
							final TreePath path = new TreePath(model.getPathToRoot(node));
							jTree.setSelectionPath(path);
							jTree.scrollPathToVisible(path);
							m_Path = path;
							showComponent();
						}

						jScrollPane = new JScrollPane(jTree, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
								ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

						jScrollPane.setPreferredSize(new Dimension(200, 450));
						jScrollPane.setMinimumSize(new Dimension(200, 450));
						jScrollPane.setMaximumSize(new Dimension(200, 450));
					}
					{
						jSplitPane.add(jScrollPane, JSplitPane.LEFT);
					}
				}
			}

			popupMenu = new JPopupMenu("Menu");

			menuItemSave = new JMenuItem(Sextante.getText("Save"));
			menuItemSave.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent evt) {
					if (m_Path != null) {
						try {
							final DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
							if (node.getUserObject() instanceof ObjectAndDescription) {
								save();
							}
						}
						catch (final Exception e) {
						}
					}
				}
			});
			popupMenu.add(menuItemSave);

			menuItemRemove = new JMenuItem(Sextante.getText("Remove"));
			menuItemRemove.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent evt) {
					if (m_Path != null) {
						try {
							final DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
							if (node.getUserObject() instanceof ObjectAndDescription) {
								remove();
							}
						}
						catch (final Exception e) {
						}
					};
				}
			});
			popupMenu.add(menuItemRemove);

			menuItemRename = new JMenuItem(Sextante.getText("Rename"));
			menuItemRename.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent evt) {
					if (m_Path != null) {
						try {
							final DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
							if (node.getUserObject() instanceof ObjectAndDescription) {
								rename();
							}
						}
						catch (final Exception e) {
						}
					};
				}

			});
			popupMenu.add(menuItemRename);

			panel.updateUI();
			return true;
		}
		catch (final Exception e) {
			Sextante.addErrorToLog(e);
			return false;
		}

	}


	protected void showPopupMenu(final MouseEvent e) {

		jTree.setSelectionPath(m_Path);

		final DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
		final ObjectAndDescription oad = (ObjectAndDescription) node.getUserObject();
		final Component c = (Component) oad.getObject();
		menuItemSave.setEnabled(true);
		popupMenu.show(e.getComponent(), e.getX(), e.getY());

	}


	private void rename() {

		if (m_Path != null) {
			try {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
				final ObjectAndDescription oad = (ObjectAndDescription) node.getUserObject();
				final String sName = oad.getDescription();

				final JOptionPane pane = new JOptionPane();
				pane.setMessage(Sextante.getText("introduce_nombre"));
				pane.setMessageType(JOptionPane.QUESTION_MESSAGE);
				pane.setWantsInput(true);
				pane.setInitialSelectionValue(sName);
				pane.setInputValue(sName);
				final JDialog dlg = pane.createDialog(null, Sextante.getText("renombrar"));
				dlg.setModal(true);
				dlg.setVisible(true);

				final String sNewName = pane.getInputValue().toString().trim();

				if ((sNewName != null) && (sNewName.length() != 0)) {
					oad.setDescription(sNewName);
				}

				update();
			}
			catch (final Exception e) {
			}
		}


	}


	protected void remove() {

		if (m_Path != null) {
			try {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
				final ObjectAndDescription oad = (ObjectAndDescription) node.getUserObject();
				AdditionalResults.removeComponent(oad);
				update();
			}
			catch (final Exception e) {
			}
		}

	}


	protected void save() {

		if (m_Path != null) {
			try {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
				final ObjectAndDescription oad = (ObjectAndDescription) node.getUserObject();
				final Component c = (Component) oad.getObject();
				if (c instanceof JScrollPane) {
					final JScrollPane pane = (JScrollPane) c;
					final Component view = pane.getViewport().getView();
					if (view instanceof JTextPane) {
						final JTextPane text = (JTextPane) pane.getViewport().getView();
						final JFileChooser fc = new JFileChooser();
						fc.setFileFilter(new GenericFileFilter(new String[] { "htm" }, "HTML"));
						final int returnVal = fc.showSaveDialog(this);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							try {
								final File file = fc.getSelectedFile();
								final FileWriter fileWriter = new FileWriter(file);
								final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
								bufferedWriter.write(text.getText());
								bufferedWriter.close();
							}
							catch (final Exception e) {
								JOptionPane.showMessageDialog(null, "Could not save selected result", Sextante.getText("Warning"),
										JOptionPane.WARNING_MESSAGE);
							}
						}
					}
					else if (view instanceof JTable) {
						final JTable table = (JTable) pane.getViewport().getView();
						final TableModel model = table.getModel();
						final JFileChooser fc = new JFileChooser();
						fc.setFileFilter(new GenericFileFilter(new String[] { "csv" }, "Comma-Separated Values (csv)"));
						final int returnVal = fc.showSaveDialog(this);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							try {
								final File file = fc.getSelectedFile();
								final FileWriter fileWriter = new FileWriter(file);
								final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
								for (int j = 0; j < model.getColumnCount(); j++) {
									bufferedWriter.write(model.getColumnName(j));
									if (j == model.getColumnCount() - 1) {
										bufferedWriter.write("\n");
									}
									else {
										bufferedWriter.write(",");
									}
								}
								for (int i = 0; i < model.getRowCount(); i++) {
									for (int j = 0; j < model.getColumnCount(); j++) {
										bufferedWriter.write(model.getValueAt(i, j).toString());
										if (j == model.getColumnCount() - 1) {
											bufferedWriter.write("\n");
										}
										else {
											bufferedWriter.write(",");
										}
									}
								}
								bufferedWriter.close();
							}
							catch (final Exception e) {
								JOptionPane.showMessageDialog(null, "Could not save selected result", Sextante.getText("Warning"),
										JOptionPane.WARNING_MESSAGE);
							}
						}
					}
				}
				else if (c instanceof ChartPanel) {
					final ChartPanel panel = (ChartPanel) c;
					final JFileChooser fc = new JFileChooser();
					fc.setFileFilter(new GenericFileFilter(new String[] { "png" }, "png"));
					final int returnVal = fc.showSaveDialog(this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						ChartUtils.saveChartAsPNG(fc.getSelectedFile(), panel.getChart(), 600, 350);
					}
				}

			}
			catch (final Exception e) {
				Sextante.addErrorToLog(e);
			}
		}

	}


	protected void showComponent() {

		if (m_Path != null) {
			try {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
				final ObjectAndDescription oad = (ObjectAndDescription) node.getUserObject();
				final Component c = (Component) oad.getObject();
				c.setMinimumSize(new Dimension(300, 200));
				jSplitPane.setRightComponent(c);
			}
			catch (final Exception e) {
				Sextante.addErrorToLog(e);
			}

		}

	}


	public void fillTree(final ArrayList components) {

		DefaultMutableTreeNode node;
		final DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(Sextante.getText("SEXTANTE"));;
		final DefaultMutableTreeNode componentsNode = new DefaultMutableTreeNode(Sextante.getText("Result"));

		for (int i = 0; i < components.size(); i++) {
			node = new DefaultMutableTreeNode(components.get(i));
			componentsNode.add(node);
		}

		mainNode.add(componentsNode);

		jTree.setModel(new DefaultTreeModel(mainNode));

	}


	public void update() {

		if (!initGUI(AdditionalResults.getComponents())) {
			dispose();
			setVisible(false);
		}

	}


	private DefaultMutableTreeNode findNode(final ObjectAndDescription oad) {

		Object ob;
		final DefaultTreeModel data = (DefaultTreeModel) jTree.getModel();
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) data.getRoot();
		DefaultMutableTreeNode node = null;

		if (root != null) {
			for (final Enumeration e = root.breadthFirstEnumeration(); e.hasMoreElements();) {
				final DefaultMutableTreeNode current = (DefaultMutableTreeNode) e.nextElement();
				ob = current.getUserObject();
				if (ob instanceof ObjectAndDescription) {
					if (ob == oad) {
						node = current;
						break;
					}
				}
			}
		}

		return node;

	}

}
