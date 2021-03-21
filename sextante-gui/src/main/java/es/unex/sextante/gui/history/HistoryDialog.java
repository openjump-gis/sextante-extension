package es.unex.sextante.gui.history;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;

import es.unex.sextante.core.Sextante;

public class HistoryDialog
         extends
            JDialog {

   private HistoryPanel m_HistoryPanel;
   private JButton      jButtonClearLog;
   private JButton      jButtonClearHistory;


   public HistoryDialog(final Frame parent) {

      super(parent, Sextante.getText("History"), true);

      this.setResizable(true);

      init();
      setLocationRelativeTo(null);

   }


   private void init() {

      this.setPreferredSize(new java.awt.Dimension(650, 380));
      this.setSize(new java.awt.Dimension(650, 380));

      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 7.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL, TableLayoutConstants.FILL,
                        7.0 }, { TableLayoutConstants.FILL, 3.0, 30.0, 3.0 } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      getContentPane().setLayout(thisLayout);

      m_HistoryPanel = new HistoryPanel();
      getContentPane().add(m_HistoryPanel, "1, 0, 4, 0");
      {
         jButtonClearHistory = new JButton();
         getContentPane().add(jButtonClearHistory, "1, 2, 2, 2");
         jButtonClearHistory.setText(Sextante.getText("Clear_history"));
         jButtonClearHistory.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               clearHistory(evt);
            }
         });
      }
      {
         jButtonClearLog = new JButton();
         getContentPane().add(jButtonClearLog, "3, 2, 4, 2");
         jButtonClearLog.setText(Sextante.getText("Clear_log"));
         jButtonClearLog.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               clearLog(evt);
            }
         });
      }

   }


   public HistoryPanel getHistoryPanel() {

      return m_HistoryPanel;

   }


   private void clearLog(final ActionEvent evt) {

      m_HistoryPanel.clearLog();

   }


   private void clearHistory(final ActionEvent evt) {

      m_HistoryPanel.clearHistory();

   }

}
