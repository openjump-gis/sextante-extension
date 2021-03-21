package es.unex.sextante.gui.algorithm;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.NamedPoint;
import es.unex.sextante.gui.core.SextanteGUI;

/**
 * A panel with two text fields, to introduce point coordinates. It also features a button to access pre-stored coordinates
 * 
 * @author volaya
 * 
 */
public class PointSelectionPanel
         extends
            JPanel {

   private JTextField textFieldX;
   private JButton    jButton;
   private JTextField textFieldY;
   private JLabel     labelX;
   private JLabel     labelY;


   PointSelectionPanel() {

      super();

      initGUI();

   }


   private void initGUI() {

      textFieldX = new JTextField("0");
      textFieldY = new JTextField("0");
      labelX = new JLabel(" X:");
      labelY = new JLabel(" Y:");
      jButton = new JButton();
      jButton.setText("...");

      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 25.0, TableLayoutConstants.FILL, 25.0, TableLayoutConstants.FILL, 5.0, 25.0, 5.0 },
               { TableLayoutConstants.FILL } });
      this.setLayout(thisLayout);
      this.setPreferredSize(new java.awt.Dimension(128, 22));
      this.add(labelX, "0,  0");
      this.add(textFieldX, "1,  0");
      this.add(labelY, "2,  0");
      this.add(textFieldY, "3,  0");
      this.add(jButton, "5, 0");
      jButton.addActionListener(new ActionListener() {
         public void actionPerformed(final ActionEvent evt) {
            showPointSelector();
         }
      });

   }


   protected void showPointSelector() {

      final ArrayList<NamedPoint> coordsList = SextanteGUI.getGUIFactory().getCoordinatesList();

      if (coordsList.size() != 0) {
         final NamedPoint[] pts = coordsList.toArray(new NamedPoint[0]);
         final NamedPoint pt = (NamedPoint) JOptionPane.showInputDialog(this, Sextante.getText("Select_coordinates"),
                  Sextante.getText("Coordinates"), JOptionPane.PLAIN_MESSAGE, null, pts, null);
         if (pt != null) {
            textFieldX.setText(Double.toString(pt.getPoint().getX()));
            textFieldY.setText(Double.toString(pt.getPoint().getY()));
         }
      }

   }


   /**
    * Returns the selected point
    * 
    * @return the point introduced using this panel
    */
   public Point2D getPoint() {

      try {
         final Point2D pt = new Point2D.Double(Double.parseDouble(textFieldX.getText()), Double.parseDouble(textFieldY.getText()));
         return pt;
      }
      catch (final Exception e) {
         return null;
      }

   }


   /**
    * Sets the current point
    * 
    * @param point
    *                the new point to set
    */
   public void setPoint(final Point2D point) {

      textFieldX.setText(Double.toString(Math.floor(point.getX() * 10000.) / 10000.));
      textFieldY.setText(Double.toString(Math.floor(point.getY() * 10000.) / 10000.));

   }

}
