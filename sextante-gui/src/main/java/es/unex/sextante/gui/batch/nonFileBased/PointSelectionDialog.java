package es.unex.sextante.gui.batch.nonFileBased;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import es.unex.sextante.core.Sextante;

public class PointSelectionDialog
         extends
            javax.swing.JDialog {

   private JPanel              jPanelMain;
   private JSeparator          jSeparator;
   private JButton             jButtonCancel;
   private JButton             jButtonOK;
   private final Point2D       m_Point;
   private PointSelectionPanel pointPanel;
   private boolean             m_bOK = false;


   public PointSelectionDialog(final Frame window,
                               final Point2D pt) {


      super(window, "", true);

      this.setResizable(false);

      m_Point = pt;

      initGUI();

   }


   private void initGUI() {
      try {
         {
            this.setTitle(Sextante.getText("point"));
            {
               jPanelMain = new JPanel();
               getContentPane().add(jPanelMain, BorderLayout.CENTER);
               jPanelMain.setPreferredSize(new java.awt.Dimension(300, 90));
               {
                  pointPanel = new PointSelectionPanel();
                  pointPanel.setPreferredSize(new java.awt.Dimension(250, 20));
                  jPanelMain.add(pointPanel);

               }
               {
                  jSeparator = new JSeparator();
                  jPanelMain.add(jSeparator);
                  jSeparator.setPreferredSize(new java.awt.Dimension(250, 20));
               }
               {
                  jButtonOK = new JButton();
                  jPanelMain.add(jButtonOK);
                  jButtonOK.setText(Sextante.getText("OK"));
                  jButtonOK.addActionListener(new ActionListener() {
                     public void actionPerformed(final ActionEvent evt) {
                        btnOKActionPerformed(evt);
                     }
                  });
               }
               {
                  jButtonCancel = new JButton();
                  jPanelMain.add(jButtonCancel);
                  jButtonCancel.setText(Sextante.getText("Cancel"));
                  jButtonCancel.addActionListener(new ActionListener() {
                     public void actionPerformed(final ActionEvent evt) {
                        btnCancelActionPerformed(evt);
                     }
                  });
               }
            }
         }
         this.setSize(350, 100);
      }
      catch (final Exception e) {
         Sextante.addErrorToLog(e);
      }
   }


   private void btnOKActionPerformed(final ActionEvent evt) {

      final Point2D pt = pointPanel.getPoint();

      if (pt == null) {
         Toolkit.getDefaultToolkit().beep();
         return;
      }

      m_Point.setLocation(pt);

      m_bOK = true;
      dispose();
      setVisible(false);

   }


   private void btnCancelActionPerformed(final ActionEvent evt) {

      m_bOK = false;
      dispose();
      setVisible(false);


   }


   public boolean getOK() {

      return m_bOK;

   }

}
