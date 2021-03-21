package es.unex.sextante.gui.help;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;

import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;

public class ElementEditionPanel
         extends
            JPanel {

   private HelpElement  m_Element;
   private JPanel       jPanelImages;
   private JTextArea    jTextAreaDescription;
   private JScrollPane  jScrollPaneTextArea;
   private JButton      jButtonRemoveImage;
   private JButton      jButtonAddImage;
   private JList        jListImages;
   private JPanel       jPanelText;
   private final GeoAlgorithm m_Alg;


   public ElementEditionPanel(final GeoAlgorithm ext) {

      super();

      m_Alg = ext;

      initGUI();

   }


   private void initGUI() {
      {
         final TableLayout thisLayout = new TableLayout(new double[][] { { 5.0, TableLayoutConstants.FILL, 5.0 },
                  { 5.0, TableLayoutConstants.FILL, 5.0, TableLayoutConstants.FILL, 5.0 } });
         thisLayout.setHGap(5);
         thisLayout.setVGap(5);
         this.setLayout(thisLayout);
         this.setPreferredSize(new java.awt.Dimension(565, 252));
         {
            jPanelText = new JPanel();
            final BorderLayout jPanelTextLayout = new BorderLayout();
            jPanelText.setLayout(jPanelTextLayout);
            this.add(jPanelText, "1, 1");
            jPanelText.setBorder(BorderFactory.createTitledBorder(Sextante.getText("Description")));
            {
               jTextAreaDescription = new JTextArea();
               jTextAreaDescription.setLineWrap(true);
               jTextAreaDescription.setWrapStyleWord(true);
               jScrollPaneTextArea = new JScrollPane(jTextAreaDescription, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
               jPanelText.add(jScrollPaneTextArea, BorderLayout.CENTER);
               jTextAreaDescription.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            }
         }
         {
            jPanelImages = new JPanel();
            final TableLayout jPanelImagesLayout = new TableLayout(new double[][] { { TableLayoutConstants.FILL, 5.0, 100.0, 5.0 },
                     { TableLayoutConstants.FILL, 30.0, 5.0, 30.0, TableLayoutConstants.FILL } });
            jPanelImagesLayout.setHGap(5);
            jPanelImagesLayout.setVGap(5);
            jPanelImages.setLayout(jPanelImagesLayout);
            this.add(jPanelImages, "1, 3");
            jPanelImages.setBorder(BorderFactory.createTitledBorder(Sextante.getText("Images")));
            {
               final ListModel jListImagesModel = new DefaultListModel();
               jListImages = new JList();
               jListImages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
               jPanelImages.add(jListImages, "0, 0, 0, 4");
               jListImages.setModel(jListImagesModel);
               jListImages.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            }
            {
               jButtonAddImage = new JButton();
               jButtonAddImage.setText(Sextante.getText("Add_image"));
               jButtonAddImage.addActionListener(new ActionListener() {
                  public void actionPerformed(final ActionEvent evt) {
                     addImage();
                  }
               });
               jPanelImages.add(jButtonAddImage, "2, 1");
            }
            {
               jButtonRemoveImage = new JButton();
               jButtonRemoveImage.setText(Sextante.getText("Remove_image"));
               jButtonRemoveImage.addActionListener(new ActionListener() {
                  public void actionPerformed(final ActionEvent evt) {
                     removeImage();
                  }
               });
               jPanelImages.add(jButtonRemoveImage, "2, 3");
            }
         }
      }

   }


   protected void removeImage() {

      final DefaultListModel model = (DefaultListModel) jListImages.getModel();
      final int selection = jListImages.getSelectedIndex();
      model.remove(selection);

   }


   private void addImage() {

      final Frame window = new Frame();
      final ImageAndDescription iad = new ImageAndDescription();
      final ImageSelectionDialog dialog = new ImageSelectionDialog(window, iad, HelpIO.getHelpPath(m_Alg, true));

      dialog.pack();
      dialog.setVisible(true);

      if (dialog.getOK()) {
         final DefaultListModel model = (DefaultListModel) jListImages.getModel();
         model.addElement(iad);
      }

   }


   public void setElement(final HelpElement element) {

      m_Element = element;

      jTextAreaDescription.setText(element.getText());
      final ArrayList images = element.getImages();
      final DefaultListModel model = (DefaultListModel) jListImages.getModel();
      model.clear();
      for (int i = 0; i < images.size(); i++) {
         model.addElement(images.get(i));
      }

   }


   public void saveElement() {

      if (m_Element != null) {
         m_Element.setText(jTextAreaDescription.getText());
         final ArrayList list = new ArrayList();
         m_Element.setImages(list);
         final DefaultListModel model = (DefaultListModel) jListImages.getModel();
         for (int i = 0; i < model.size(); i++) {
            list.add(model.get(i));
         }
      }

   }

}
