package es.unex.sextante.gui.core;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.grass.GrassUtils;

/**
 * A progress monitor showing a progress bar and a cancel button
 * 
 * @author volaya
 * 
 */
public class ProgressMonitor
         extends
            JDialog {

   private JLabel       jTitle;
   private JButton      jButtonCancel;
   private JProgressBar jProgressBar;
   private JLabel       jProgressText;
   private boolean      m_bCanceled;
   private String       m_sPrefix      = "";
   private String       m_sDescription = "";


   /**
    * Constructor
    * 
    * @param sText
    *                The title text to show
    * @param bDeterminate
    *                true if the process to monitor is determinate
    * @param parent
    *                the parent dialog
    */
   public ProgressMonitor(final String sText,
                          final boolean bDeterminate,
                          final JDialog parent) {

      super(parent, "", false);

      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      setLocationRelativeTo(null);

      m_bCanceled = false;
      m_sDescription = sText;

      initGUI(bDeterminate);

   }


   /**
    * Constructor. Uses the main frame as the parent component
    * 
    * @param sText
    *                The title text to show
    * @param bDeterminate
    *                true if the process to monitor is determinate
    */
   public ProgressMonitor(final String sText,
                          final boolean bDeterminate) {

      super(SextanteGUI.getMainFrame(), "", false);

      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      setLocationRelativeTo(null);

      m_bCanceled = false;
      m_sDescription = sText;

      initGUI(bDeterminate);

   }


   private void initGUI(boolean bDeterminate) {

      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 6.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 6.0 },
               { 1.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 1, 15.0, 1.0, 25.0, 1.0 } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      setPreferredSize(new Dimension(350, 150));
      setSize(new Dimension(350, 150));
      getContentPane().setLayout(thisLayout);
      {
         jTitle = new JLabel(m_sDescription);
         getContentPane().add(jTitle, "1, 1, 2, 1");
      }
      {
         jButtonCancel = new JButton();
         getContentPane().add(jButtonCancel, "2, 6");
         jButtonCancel.setText(Sextante.getText("Cancel"));
         jButtonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent evt) {
               cancel();
            }
         });
      }
      {
         jProgressBar = new JProgressBar();
         jProgressBar.setIndeterminate(!bDeterminate);
         if (bDeterminate) {
            jProgressBar.setMinimum(0);
            jProgressBar.setMaximum(100);
            jProgressBar.setValue(0);
         }
         getContentPane().add(jProgressBar, "1, 4, 2, 4");
      }
      {
         jProgressText = new JLabel();
         getContentPane().add(jProgressText, "1, 2");
      }

   }


   private void cancel() {

      //If this is a running GRASS process: kill it
      GrassUtils.cancelProcess();

      m_bCanceled = true;

   }


   /**
    * Returns true if the process has been canceled using the cancel button
    * 
    * @return true if the process has been canceled
    */
   public boolean isCanceled() {

      return m_bCanceled;

   }


   /**
    * Sets the text describing the current phase of the process being monitored
    * 
    * @param sText
    *                the description
    */
   public void setProgressText(final String sText) {

      try {
         final Runnable runnable = new Runnable() {
            public void run() {
               jProgressText.setText(sText);
            }
         };
         if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
         }
         else {
            SwingUtilities.invokeAndWait(runnable);
         }
      }
      catch (final Exception e) {}


   }


   /**
    * Sets the description to show at the top of the progress monitor
    * 
    * @param sDescription
    *                the description
    */
   public void setDescription(final String sDescription) {

      m_sDescription = sDescription;
      updateTitle();

   }


   private void updateTitle() {

      try {

         final Runnable runnable = new Runnable() {
            public void run() {
               jTitle.setText(m_sPrefix + m_sDescription);
            }
         };
         if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
         }
         else {
            SwingUtilities.invokeAndWait(runnable);
         }
      }
      catch (final Exception e) {}

   }


   /**
    * Sets the current progress value (in the range 1-100)
    * 
    * @param iValue
    */
   public void setProgress(final int iValue) {

      try {
         final Runnable runnable = new Runnable() {
            public void run() {
               jProgressBar.setValue(iValue);
            }
         };
         if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
         }
         else {
            SwingUtilities.invokeAndWait(runnable);
         }
      }
      catch (final Exception e) {}

   }


   /**
    * Sets whether the process being monitored is determinate or not
    * 
    * @param bDeterminate
    */
   public void setDeterminate(final boolean bDeterminate) {

      try {
         final Runnable runnable = new Runnable() {
            public void run() {
               jProgressBar.setIndeterminate(!bDeterminate);
            }
         };
         if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
         }
         else {
            SwingUtilities.invokeAndWait(runnable);
         }
      }
      catch (final Exception e) {}

   }


   /**
    * this method sets a prefix to be prepended to the description title. This can be used for processes that include several
    * algorithms, so when each algorithm sets its owns description, it will also contain a string indicating the part of the
    * global process that it represents. This method should, therefore, not be called by simple algorithms, but just from complex
    * processes
    * 
    * @param sPrefix
    *                the prefix to prepend to the description title.
    */
   public void setDescriptionPrefix(final String sPrefix) {

      m_sPrefix = sPrefix;
      updateTitle();


   }

}
