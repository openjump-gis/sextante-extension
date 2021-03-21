package es.unex.sextante.gui.algorithm;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.NamedPoint;
import es.unex.sextante.gui.core.SextanteGUI;


public class ExtentFromSavedPointsDialog
         extends
            JDialog {

   private double[]  m_dPoints;
   private JButton   jButtonCancel;
   private JLabel    jLabelPt1;
   private JComboBox jComboBoxPt2;
   private JComboBox jComboBoxPt1;
   private JLabel    jLabelPt2;
   private JButton   jButtonOK;


   public ExtentFromSavedPointsDialog() {

      super((JFrame) null, true);
      initGUI();
      setLocationRelativeTo(null);

   }


   private void initGUI() {

      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 6.0, TableLayout.FILL, TableLayout.FILL, 6.0, TableLayout.FILL, 6.0 },
               { TableLayout.FILL, TableLayout.MINIMUM, 6.0, TableLayout.MINIMUM, TableLayout.FILL, TableLayout.MINIMUM, 6.0 } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      getContentPane().setLayout(thisLayout);
      {
         jButtonOK = new JButton();
         getContentPane().add(jButtonOK, "2, 5");
         jButtonOK.setText(Sextante.getText("OK"));
         jButtonOK.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
               final Point2D pt1 = ((NamedPoint) jComboBoxPt1.getSelectedItem()).getPoint();
               final Point2D pt2 = ((NamedPoint) jComboBoxPt2.getSelectedItem()).getPoint();
               m_dPoints = new double[4];
               m_dPoints[0] = Math.min(pt1.getX(), pt2.getX());
               m_dPoints[1] = Math.min(pt1.getY(), pt2.getY());
               m_dPoints[2] = Math.max(pt1.getX(), pt2.getX());
               m_dPoints[3] = Math.max(pt1.getY(), pt2.getY());
               setVisible(false);
               dispose();
            }

         });
      }
      {
         jButtonCancel = new JButton();
         getContentPane().add(jButtonCancel, "4, 5");
         jButtonCancel.setText(Sextante.getText("Cancel"));
         jButtonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent arg0) {
               m_dPoints = null;
               setVisible(false);
               dispose();
            }

         });
      }
      {
         jLabelPt1 = new JLabel();
         getContentPane().add(jLabelPt1, "1, 1");
         jLabelPt1.setText("Pt1");
      }
      {
         jLabelPt2 = new JLabel();
         getContentPane().add(jLabelPt2, "1, 3");
         jLabelPt2.setText("Pt2");
      }
      final ArrayList<NamedPoint> coordsList = SextanteGUI.getGUIFactory().getCoordinatesList();
      final NamedPoint[] pts = coordsList.toArray(new NamedPoint[0]);
      {
         final ComboBoxModel jComboBoxPt1Model = new DefaultComboBoxModel(pts);
         jComboBoxPt1 = new JComboBox();
         getContentPane().add(jComboBoxPt1, "2, 1, 4, 1");
         jComboBoxPt1.setModel(jComboBoxPt1Model);
      }
      {
         final ComboBoxModel jComboBoxPt2Model = new DefaultComboBoxModel(pts);
         jComboBoxPt2 = new JComboBox();
         getContentPane().add(jComboBoxPt2, "2, 3, 4, 3");
         jComboBoxPt2.setModel(jComboBoxPt2Model);
      }
      {
         this.setSize(358, 223);
      }

   }


   public double[] getPoints() {


      return m_dPoints;

   }

}
