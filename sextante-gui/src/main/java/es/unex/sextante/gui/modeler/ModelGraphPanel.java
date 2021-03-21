

package es.unex.sextante.gui.modeler;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.ObjectAndDescription;
import es.unex.sextante.core.ParametersSet;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.IGUIFactory;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.modeler.parameters.ParameterPanel;
import es.unex.sextante.gui.modeler.parameters.ParameterPanelFactory;
import es.unex.sextante.modeler.elements.ModelElementFactory;
import es.unex.sextante.parameters.Parameter;


public class ModelGraphPanel
         extends
            JScrollPane {

   private ModelAlgorithm     m_Algorithm;
   private final HashMap      m_DataObjects;
   private final ArrayList    m_InputKeys;
   private JGraph             jGraph;
   private final Icon         m_InputIcon;
   private final Icon         m_ProcessIcon;
   private int                m_iInputs;
   private final ModelerPanel m_ModelerPanel;
   private JPopupMenu         popupMenu;
   private DefaultGraphCell   m_ActiveCell;
   private final HashMap      m_Coords;
   private final JDialog      m_Parent;
   private JMenuItem          menuItemEdit;


   public ModelGraphPanel(final ModelAlgorithm algorithm,
                          final HashMap dataObjects,
                          final ArrayList inputKeys,
                          final ModelerPanel modelerPanel,
                          final JDialog parent) {

      m_DataObjects = dataObjects;
      m_InputKeys = inputKeys;
      m_Algorithm = algorithm;
      m_ModelerPanel = modelerPanel;
      m_Coords = new HashMap();
      m_Parent = parent;

      m_InputIcon = new ImageIcon(getClass().getClassLoader().getResource("images/list-add.png"));
      m_ProcessIcon = new ImageIcon(getClass().getClassLoader().getResource("images/module2.png"));

      initGUI();

   }


   private void initGUI() {

      JMenuItem menuItem;
      popupMenu = new JPopupMenu("Menu");


      menuItemEdit = new JMenuItem(Sextante.getText("Edit"));

      menuItemEdit.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            final Object ob = m_ActiveCell.getUserObject();
            System.out.println(ob.getClass());
            editCell((ObjectAndDescription) ob);
         }
      });
      popupMenu.add(menuItemEdit);


      menuItem = new JMenuItem(Sextante.getText("Remove"));
      menuItem.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            final Object ob = m_ActiveCell.getUserObject();
            final String sKey = (String) ((ObjectAndDescription) ob).getObject();
            m_ModelerPanel.removeElement(sKey);
         }
      });
      popupMenu.add(menuItem);

      updateGraph();

   }


   @Override
   public void update(final Graphics g) {

      super.update(g);

   }


   public void updateGraph() {

      int i;
      String sKey;

      m_iInputs = 0;

      initGraph();

      for (i = 0; i < m_InputKeys.size(); i++) {
         sKey = (String) m_InputKeys.get(i);
         addInput(sKey);
      }

      final ArrayList algKeys = m_Algorithm.getAlgorithmKeys();

      for (i = 0; i < algKeys.size(); i++) {
         sKey = (String) algKeys.get(i);
         addAlgorithm(sKey);
      }

      this.setViewportView(jGraph);

   }


   private void initGraph() {

      final GraphModel model = new DefaultGraphModel();
      jGraph = new JGraph(model);

      jGraph.setCloneable(false);
      jGraph.setInvokesStopCellEditing(true);
      jGraph.setJumpToDefaultPort(true);
      jGraph.setConnectable(false);
      jGraph.setDisconnectable(false);
      jGraph.setEditable(false);
      jGraph.setEnabled(true);

      jGraph.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(final MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON3) {
               showPopupMenu(e);
            }
            if (e.getClickCount() == 2) {
               editCell(e);
            }
         }
      });

   }


   void storeCoords() {

      if (jGraph == null) {
         return;
      }

      int i;
      final GraphModel model = jGraph.getModel();
      DefaultGraphCell cell;
      ObjectAndDescription oad;
      String sCellKey;

      m_Coords.clear();

      for (i = 0; i < model.getRootCount(); i++) {
         try {
            cell = (DefaultGraphCell) model.getRootAt(i);
            oad = (ObjectAndDescription) cell.getUserObject();
            sCellKey = (String) oad.getObject();
            cell.getAttributes();
            final Rectangle2D rect = GraphConstants.getBounds(cell.getAttributes());
            m_Coords.put(sCellKey, rect);
         }
         catch (final Exception e) {
         }
      }

   }


   protected void showPopupMenu(final MouseEvent e) {

      final int x = e.getX();
      final int y = e.getY();

      m_ActiveCell = (DefaultGraphCell) jGraph.getFirstCellForLocation(x, y);

      if (m_ActiveCell == null) {
         return;
      }

      final Object ob = m_ActiveCell.getUserObject();

      if (ob != null) {
         //ObjectAndDescription oad = (ObjectAndDescription) ob;
         //menuItemEdit.setVisible(oad.getObject() instanceof GeoAlgorithm);
         popupMenu.show(e.getComponent(), x, y);
      }


   }


   private void editCell(final MouseEvent e) {

      final int x = e.getX();
      final int y = e.getY();
      final DefaultGraphCell cell = (DefaultGraphCell) jGraph.getFirstCellForLocation(x, y);

      if (cell != null) {
         try {
            final ObjectAndDescription oad = (ObjectAndDescription) cell.getUserObject();
            editCell(oad);
         }
         catch (final Exception ex) {
            ex.printStackTrace();
         }
      }

   }


   private void editCell(ObjectAndDescription oad) {

      try {
         final String sDescription = oad.getDescription();
         final String sKey = (String) oad.getObject();
         final Object usrObj = m_ModelerPanel.getObjectFromKey(sKey);

         if (usrObj instanceof GeoAlgorithm) {
            final GeoAlgorithm alg = (GeoAlgorithm) usrObj;
            GeoAlgorithmModelerParametersPanel paramPanel = null;
            Class paramPanelClass = SextanteGUI.getModelerParametersPanel(alg.getCommandLineName());
            if (paramPanelClass == null) {
               paramPanelClass = SextanteGUI.getGUIFactory().getDefaultModelerParametersPanel();
            }
            try {
               paramPanel = (GeoAlgorithmModelerParametersPanel) paramPanelClass.newInstance();
            }
            catch (final Exception e) {
               try {
                  paramPanel = SextanteGUI.getGUIFactory().getDefaultModelerParametersPanel().newInstance();
               }
               catch (final Exception e1) {
               }
            }
            AlgorithmDialog dialog;
            if (m_Parent == null) {
               dialog = new AlgorithmDialog(alg, sKey, sDescription, m_Algorithm, paramPanel, m_DataObjects);
            }
            else {
               dialog = new AlgorithmDialog(alg, sKey, sDescription, m_Algorithm, paramPanel, m_DataObjects, m_Parent);
            }
            dialog.pack();
            dialog.setVisible(true);
            if (dialog.getDialogReturn() == IGUIFactory.OK) {
               m_ModelerPanel.setHasChanged(true);
               m_ModelerPanel.updatePanel(true);
            }
         }
         else if (usrObj instanceof ObjectAndDescription) {
            final ParametersSet params = m_ModelerPanel.getAlgorithm().getParameters();
            Parameter param = params.getParameter(sKey);
            final ParameterPanel paramPanel = getParameterPanel(param);
            if (paramPanel != null) {
               paramPanel.updateOptions();
               paramPanel.setParameter(param);
               paramPanel.pack();
               paramPanel.setVisible(true);
               param = paramPanel.getParameter();
               if (param != null) {
                  param.setParameterName(sKey);
                  oad = new ObjectAndDescription(param.getParameterDescription(),
                           ModelElementFactory.getParameterAsModelElement(param));
                  m_DataObjects.put(sKey, oad);
                  m_ModelerPanel.setHasChanged(true);
                  m_ModelerPanel.updatePanel(true);
               }
            }
         }
      }
      catch (final Exception ex) {
         ex.printStackTrace();
      }

   }


   private ParameterPanel getParameterPanel(final Parameter param) {

      return ParameterPanelFactory.getParameterPanel(param, m_ModelerPanel, m_Parent);

   }


   public void addInput(final String sKey) {

      double x, y, w, h;

      try {
         final ObjectAndDescription oad = (ObjectAndDescription) m_ModelerPanel.getObjectFromKey(sKey);
         if (oad != null) {
            final Rectangle2D box = getBoundingBox();
            x = box.getMaxX() + 20;
            y = 20;
            w = 45;
            h = 45;
            final Rectangle2D rect = (Rectangle2D) m_Coords.get(sKey);
            if (rect != null) {
               x = rect.getMinX();
               y = rect.getMinY();
               w = rect.getWidth();
               h = rect.getHeight();
            }
            final DefaultGraphCell cell = createInputVertex(new ObjectAndDescription(oad.getDescription(), sKey), x, y, w, h);
            jGraph.getGraphLayoutCache().insert(cell);

            m_iInputs++;
         }
      }
      catch (final ClassCastException e) {
      }

   }


   public void addAlgorithm(String sKey) {

      int i;
      double x, y, w, h;
      ArrayList dependences;
      DefaultGraphCell cell, parentCell;
      DefaultEdge edge;

      final Object obj = m_ModelerPanel.getObjectFromKey(sKey);
      if (obj instanceof GeoAlgorithm) {
         final GeoAlgorithm alg = (GeoAlgorithm) obj;
         final ObjectAndDescription oad = new ObjectAndDescription(alg.getName(), sKey);
         final Rectangle2D box = getBoundingBox();
         x = box.getMaxX() + 20;
         y = box.getMaxY() + 20;
         w = 45;
         h = 45;
         if (!isAlgorithmAlreadyAdded(sKey)) {
            final Rectangle2D rect = (Rectangle2D) m_Coords.get(sKey);
            if (rect != null) {
               x = rect.getMinX();
               y = rect.getMinY();
               w = rect.getWidth();
               h = rect.getHeight();
            }
            cell = createProcessVertex(oad, x, y, w, h);
            jGraph.getGraphLayoutCache().insert(cell);
            dependences = m_ModelerPanel.getDependences(sKey);
            for (i = 0; i < dependences.size(); i++) {
               sKey = (String) dependences.get(i);
               addAlgorithm(sKey);
               parentCell = getCellFromKey(sKey);
               edge = createEdge(cell, parentCell);
               jGraph.getGraphLayoutCache().insert(edge);
            }
         }
      }

   }


   private Rectangle2D getBoundingBox() {

      double dMaxX = 0;
      double dMaxY = 0;
      Rectangle2D rect;
      String sKey;

      final Set set = m_Coords.keySet();
      final Iterator iter = set.iterator();
      while (iter.hasNext()) {
         sKey = (String) iter.next();
         rect = (Rectangle2D) m_Coords.get(sKey);
         dMaxX = Math.max(rect.getMaxX(), dMaxX);
         dMaxY = Math.max(rect.getMaxY(), dMaxY);
      }


      return new Rectangle2D.Double(0, 0, dMaxX, dMaxY);

   }


   private boolean isAlgorithmAlreadyAdded(final String sKey) {

      final DefaultGraphCell cell = getCellFromKey(sKey);

      return (cell != null);

   }


   private DefaultGraphCell getCellFromKey(final String sKey) {

      int i;
      final GraphModel model = jGraph.getModel();
      DefaultGraphCell cell;
      ObjectAndDescription oad;
      String sCellKey;

      for (i = 0; i < model.getRootCount(); i++) {
         try {
            cell = (DefaultGraphCell) model.getRootAt(i);
            oad = (ObjectAndDescription) cell.getUserObject();
            sCellKey = (String) oad.getObject();
            if (sKey.equals(sCellKey)) {
               return cell;
            }
         }
         catch (final Exception e) {
         }
      }

      return null;

   }


   private DefaultEdge createEdge(final DefaultGraphCell cell,
                                  final DefaultGraphCell parentCell) {

      DefaultEdge edge;

      edge = new DefaultEdge();

      edge.setSource(parentCell.getChildAt(0));
      edge.setTarget(cell.getChildAt(0));

      GraphConstants.setLineEnd(edge.getAttributes(), GraphConstants.ARROW_CLASSIC);
      GraphConstants.setEndFill(edge.getAttributes(), true);

      return edge;

   }


   private DefaultGraphCell createInputVertex(final ObjectAndDescription oad,
                                              final double x,
                                              final double y,
                                              final double w,
                                              final double h) {

      return createVertex(oad, x, y, w, h, m_InputIcon);


   }


   private DefaultGraphCell createProcessVertex(final ObjectAndDescription oad,
                                                final double x,
                                                final double y,
                                                final double w,
                                                final double h) {

      final GeoAlgorithm alg = (GeoAlgorithm) m_ModelerPanel.getObjectFromKey((String) oad.getObject());

      return createVertex(oad, x, y, w, h, SextanteGUI.getAlgorithmIcon(alg));

   }


   private DefaultGraphCell createVertex(final Object obj,
                                         final double x,
                                         final double y,
                                         final double w,
                                         final double h,
                                         final Icon icon) {

      final DefaultGraphCell cell = new DefaultGraphCell(obj);

      GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(x, y, w, h));

      GraphConstants.setIcon(cell.getAttributes(), icon);
      GraphConstants.setResize(cell.getAttributes(), true);
      GraphConstants.setAutoSize(cell.getAttributes(), true);

      GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createEtchedBorder());
      GraphConstants.setBorderColor(cell.getAttributes(), Color.black);

      GraphConstants.setInset(cell.getAttributes(), 10);
      GraphConstants.setMoveable(cell.getAttributes(), true);

      cell.addPort();

      return cell;

   }


   public void removeCell(final String sKey) {

      int i, j;
      Object obj;
      String sCellKey;
      final GraphModel model = jGraph.getModel();
      DefaultGraphCell cell;
      Object[] elementsToRemove;

      for (i = 0; i < model.getRootCount(); i++) {
         obj = model.getRootAt(i);
         if (obj instanceof DefaultGraphCell) {
            cell = (DefaultGraphCell) obj;
            sCellKey = (String) ((ObjectAndDescription) cell.getUserObject()).getObject();
            if (sCellKey.equals(sKey)) {
               final ArrayList listEdges = new ArrayList();
               final int numChildren = model.getChildCount(cell);
               for (j = 0; j < numChildren; j++) {
                  final Object port = model.getChild(cell, j);
                  if (model.isPort(port)) {
                     final Iterator iter = model.edges(port);
                     while (iter.hasNext()) {
                        listEdges.add(iter.next());
                     }
                  }
               }
               elementsToRemove = new Object[listEdges.size() + 1];
               for (j = 0; j < listEdges.size(); j++) {
                  elementsToRemove[j] = listEdges.get(j);
               }
               elementsToRemove[j] = cell;
               model.remove(elementsToRemove);
               return;
            }
         }
      }

   }


   public void setAlgorithm(final ModelAlgorithm alg) {

      m_Algorithm = alg;

   }


   public void resetCoords() {

      initGraph();
      m_Coords.clear();

   }


   public HashMap getCoords() {

      return m_Coords;

   }

}
