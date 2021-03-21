package es.unex.sextante.gui.settings;


import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.IAlgorithmProvider;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.exceptions.WrongSettingValuesException;
import es.unex.sextante.gui.toolbox.ToolboxPanel;

public class SextanteConfigurationDialog
         extends
            JDialog {

   private final Setting[]               m_Settings;
   private SettingPanel                  m_CurrentPanel;
   private TreePath                      m_Path;
   private final HashMap<String, String> m_Values;

   private JButton                       jButtonCancel;
   private JTree                         jTree;
   private JSplitPane                    jSplitPane;
   private JButton                       jButtonOk;


   /**
    * Constructor
    * 
    * @param panel
    *                the current toolbox panel. It will be updated with the changes made in this dialog
    * @param parent
    *                The parent dialog of this settings dialog
    */
   public SextanteConfigurationDialog(final ToolboxPanel panel,
                                      final JDialog parent) {

      super(parent, Sextante.getText("Settings"), true);

      final Setting[] settings = new Setting[] { new SextanteGeneralSettings(), new SextanteFolderSettings() };
      final ArrayList<IAlgorithmProvider> providers = SextanteGUI.getAlgorithmProviders();
      m_Settings = new Setting[settings.length + providers.size()];
      System.arraycopy(settings, 0, m_Settings, 0, settings.length);
      for (int i = 0; i < providers.size(); i++) {
         m_Settings[i + settings.length] = providers.get(i).getSettings();
      }

      m_Values = new HashMap<String, String>();

      initGUI();
      setLocationRelativeTo(null);

   }


   /**
    * Constructor. Uses the main frame as the parent component
    * 
    * @param panel
    *                the current toolbox panel. It will be updated with the changes made in this dialog
    */
   public SextanteConfigurationDialog(final ToolboxPanel panel) {

      super(SextanteGUI.getMainFrame(), Sextante.getText("Settings"), true);

      final Setting[] settings = new Setting[] { new SextanteGeneralSettings(), new SextanteFolderSettings() };
      final ArrayList<IAlgorithmProvider> providers = SextanteGUI.getAlgorithmProviders();
      m_Settings = new Setting[settings.length + providers.size()];
      System.arraycopy(settings, 0, m_Settings, 0, settings.length);
      for (int i = 0; i < providers.size(); i++) {
         m_Settings[i + settings.length] = providers.get(i).getSettings();
      }

      m_Values = new HashMap<String, String>();

      initGUI();
      setLocationRelativeTo(null);

   }


   protected void changeSetting() {

      if (m_Path != null) {
         final DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_Path.getLastPathComponent();
         final Object obj = node.getUserObject();
         if (obj instanceof Setting) {
            final Setting setting = (Setting) obj;
            try {
               final HashMap<String, String> values = m_CurrentPanel.getValues();
               jSplitPane.remove(m_CurrentPanel);
               m_CurrentPanel = setting.getPanel();
               jSplitPane.add(m_CurrentPanel, JSplitPane.RIGHT);
               m_CurrentPanel.setPreferredSize(new java.awt.Dimension(389, 211));
               final Set<String> set = values.keySet();
               final Iterator<String> iter = set.iterator();
               while (iter.hasNext()) {
                  final String key = iter.next();
                  m_Values.put(key, values.get(key));
               }
            }
            catch (final WrongSettingValuesException e) {
               JOptionPane.showMessageDialog(null, e.getMessage(), "", JOptionPane.WARNING_MESSAGE);
            }
         }
      }

   }


   private void fillTree() {

      final DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode(Sextante.getText("Settings"));
      for (final Setting element : m_Settings) {
         final DefaultMutableTreeNode node = new DefaultMutableTreeNode(element);
         mainNode.add(node);
      }
      final DefaultTreeModel model = new DefaultTreeModel(mainNode);
      jTree.setModel(model);

   }


   private void initGUI() {

      final TableLayout thisLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL, 100.0, 3.0, 100.0, 3.0 },
               { TableLayoutConstants.FILL, 3.0, 30.0, 3.0 } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      getContentPane().setLayout(thisLayout);
      this.setResizable(false);
      {
         jButtonCancel = new JButton();
         getContentPane().add(jButtonCancel, "3, 2");
         jButtonCancel.setText(Sextante.getText("Cancel"));
         jButtonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               jButtonCancelActionPerformed(evt);
            }
         });
      }
      {
         jButtonOk = new JButton();
         getContentPane().add(jButtonOk, "1, 2");
         jButtonOk.setText(Sextante.getText("OK"));
         jButtonOk.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               jButtonOkActionPerformed(evt);
            }
         });
      }
      {
         jSplitPane = new JSplitPane();
         getContentPane().add(jSplitPane, "0, 0, 3, 1");
         {
            jTree = new JTree();
            jTree.setPreferredSize(new java.awt.Dimension(164, 422));
            jSplitPane.add(jTree, JSplitPane.LEFT);
            final MouseListener ml = new MouseAdapter() {
               @Override
               public void mousePressed(MouseEvent e) {
                  m_Path = jTree.getPathForLocation(e.getX(), e.getY());
                  changeSetting();
               }
            };
            jTree.addMouseListener(ml);
            fillTree();
         }
      }
      this.setSize(614, 466);

      m_CurrentPanel = m_Settings[0].getPanel();
      jSplitPane.add(m_CurrentPanel, JSplitPane.RIGHT);
      jTree.setSelectionRow(1);

   }


   private void jButtonOkActionPerformed(final ActionEvent evt) {

      try {
         final HashMap<String, String> values = m_CurrentPanel.getValues();
         final Set<String> set = values.keySet();
         final Iterator<String> iter = set.iterator();
         while (iter.hasNext()) {
            final String key = iter.next();
            m_Values.put(key, values.get(key));
         }

         SextanteGUI.setSettings(m_Values);
         SextanteGUI.saveSettings();

         dispose();
         setVisible(false);

         SextanteGUI.getGUIFactory().updateToolbox();
      }
      catch (final WrongSettingValuesException e) {
         JOptionPane.showMessageDialog(null, e.getMessage(), "", JOptionPane.WARNING_MESSAGE);
      }

   }


   private void jButtonCancelActionPerformed(final ActionEvent evt) {

      dispose();
      setVisible(false);

   }

}
