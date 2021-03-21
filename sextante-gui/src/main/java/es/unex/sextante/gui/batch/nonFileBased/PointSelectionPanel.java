package es.unex.sextante.gui.batch.nonFileBased;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.geom.Point2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PointSelectionPanel
         extends
            JPanel {

   private JTextField textFieldX;
   private JTextField textFieldY;
   private JLabel     labelX;
   private JLabel     labelY;


   PointSelectionPanel() {

      super();

      InitGUI();

   }


   private void InitGUI() {

      textFieldX = new JTextField("0");
      textFieldY = new JTextField("0");
      labelX = new JLabel(" X:");
      labelY = new JLabel(" Y:");

      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 25.0, TableLayoutConstants.FILL, 25.0, TableLayoutConstants.FILL, 35.0 }, { TableLayoutConstants.FILL } });
      this.setLayout(thisLayout);
      this.add(labelX, "0,  0");
      this.add(textFieldX, "1,  0");
      this.add(labelY, "2,  0");
      this.add(textFieldY, "3,  0");

   }


   public Point2D getPoint() {

      try {
         final Point2D pt = new Point2D.Double(Double.parseDouble(textFieldX.getText()), Double.parseDouble(textFieldY.getText()));
         return pt;
      }
      catch (final Exception e) {
         return null;
      }


   }


   public void setPoint(final Point2D point) {

      textFieldX.setText(Double.toString(Math.floor(point.getX() * 10000.) / 10000.));
      textFieldY.setText(Double.toString(Math.floor(point.getY() * 10000.) / 10000.));

   }


}
