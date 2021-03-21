package es.unex.sextante.gui.toolbox;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import es.unex.sextante.core.IGeoAlgorithmFilter;

/**
 * A panel containing a list of algorithms and a text box to filter that list
 * 
 * @author volaya
 * 
 */
public class ToolboxPanel
         extends
            JPanel {

   private AlgorithmsPanel           algoritmhsPanel;
   private TextSearchPanel           jTextSearchPanel;
   private final IToolboxDialog      m_Parent;
   private int                       m_iCount;
   private final IGeoAlgorithmFilter m_Filter;
   private final ImageIcon           m_BackgroundImage;


   /**
    * Constructor
    * 
    * @param parent
    *                the parent toolbox dialog
    * @param filter
    *                the filter to apply to the list of all available algorithms to configure this toolbox panel
    * @param img
    *                the background image for the algorithm panel. If null, will use the default one
    */
   public ToolboxPanel(final IToolboxDialog parent,
                       final IGeoAlgorithmFilter filter,
                       ImageIcon img) {

      if (img == null) {
         final URL res = getClass().getClassLoader().getResource("images/sextante_toolbox.gif");
         if (res != null) {
            img = new ImageIcon(res);
         }
         else {
            img = null;
         }
      }

      m_Parent = parent;
      m_Filter = filter;
      m_BackgroundImage = img;
      initialize();

   }


   /**
    * Creates the interface and populates the list of algorithms
    */
   public void initialize() {

      this.setSize(new java.awt.Dimension(400, 500));
      {

         final TableLayout thisLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL },
                  { TableLayoutConstants.FILL, TableLayoutConstants.MINIMUM } });
         thisLayout.setHGap(5);
         thisLayout.setVGap(5);
         this.setLayout(thisLayout);

         algoritmhsPanel = createAlgorithmsPanel(m_BackgroundImage, m_Filter, m_Parent);
         this.add(algoritmhsPanel, "0, 0");

         jTextSearchPanel = new TextSearchPanel(this, m_Parent.getDialog());
         this.add(jTextSearchPanel, "0, 1");

      }

      fillTreesWithAllAlgorithms();

   }


   protected AlgorithmsPanel createAlgorithmsPanel(final ImageIcon img,
                                                   final IGeoAlgorithmFilter filter,
                                                   final IToolboxDialog parent) {

      return new AlgorithmsPanel(parent, filter, img);
   }


   private void collapseAll() {

      algoritmhsPanel.collapseAll();

   }


   /**
    * fills the tree of algorithms with all the available ones, not aplying any filter
    */
   public void fillTreesWithAllAlgorithms() {

      fillTreesWithSelectedAlgorithms(null, false);

      collapseAll();

   }


   /**
    * fills the tree of algorithms with all the algorithms containing a given string in their help file
    * 
    * @param string
    *                the string to search
    * @param bIncludeHelpFiles
    *                true if it should search in help files. if false, it will only search in algorithm names
    */
   public void fillTreesWithSelectedAlgorithms(final String string,
                                               final boolean bIncludeHelpFile) {

      m_iCount = algoritmhsPanel.fillTree(string, bIncludeHelpFile);

   }


   /**
    * Returns the number of algorithms currently in the panel
    * 
    * @return the number of algorithms in the panel
    */
   public int getAlgorithmsCount() {

      return m_iCount;

   }


}
