package es.unex.sextante.gui.grass;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultStyledDocument;

import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.toolbox.TransparentScrollPane;

/**
 * An extended progress monitor for GRASS modules showing a progress bar and a text output area.
 *
 * @author volaya
 *
 */
public class GrassProgressMonitor
         extends
            JDialog {

   private StringBuffer sTextBuffer;

   private JLabel       jTitle;
   private JButton      jButtonCancel;
   private JProgressBar jProgressBar;
   private JScrollPane  jScrollPaneGRASS;
   private JTextPane    jTextGRASS;
   private boolean      m_bCanceled;


   /**
    * Constructor
    *
    * @param sText
    *                The text to show
    * @param bDeterminate
    *                true if the process to monitor is determinate
    * @param parent
    *                the parent dialog
    */
   public GrassProgressMonitor(final String sText,
                               final boolean bDeterminate,
                               final JDialog parent) {

      super(parent, "", false);

      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      setLocationRelativeTo(null);

      m_bCanceled = false;

      initGUI(sText, bDeterminate);

   }


   /**
    * Constructor. Uses the main frame as the parent component
    *
    * @param sText
    *                The text to show
    * @param bDeterminate
    *                true if the process to monitor is determinate
    */
   public GrassProgressMonitor(final String sText,
                               final boolean bDeterminate) {

      super(SextanteGUI.getMainFrame(), "", false);

      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      setLocationRelativeTo(null);

      m_bCanceled = false;

      initGUI(sText, bDeterminate);

   }


   private void initGUI(final String sText,
                        boolean bDeterminate) {

      final TableLayout thisLayout = new TableLayout(new double[][] {
               { 6.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 6.0 },
               { 1.0, TableLayoutConstants.FILL, TableLayoutConstants.FILL, 1, 15.0, 1.0, 25.0, 1.0 } });
      thisLayout.setHGap(5);
      thisLayout.setVGap(5);
      setPreferredSize(new Dimension(650, 450));
      setSize(new Dimension(350, 450));
      getContentPane().setLayout(thisLayout);
      {
         jTitle = new JLabel(sText);
         getContentPane().add(jTitle, "1, 1, 2, 1");
      }
      {
         jButtonCancel = new JButton();
         getContentPane().add(jButtonCancel, "2, 6");
         jButtonCancel.setText("Cancelar");
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
         jTextGRASS = new NonWordWrapPane();
         jTextGRASS.setStyledDocument(new DefaultStyledDocument());
         jTextGRASS.setFont(new Font("Monospaced", Font.PLAIN, 12));
         jTextGRASS.setContentType("text/plain");
         jTextGRASS.setEditable(false);
         jScrollPaneGRASS = new TransparentScrollPane();
         jScrollPaneGRASS.setBackground(Color.white);
         jScrollPaneGRASS.setSize(new java.awt.Dimension(650, 450));
         jScrollPaneGRASS.setViewportView(jTextGRASS);
         getContentPane().add(jScrollPaneGRASS, "1, 2, 2, 2");
      }

      sTextBuffer = new StringBuffer("");

   }


   private void cancel() {

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
    * Adds a line of status text output to the text area.
    *
    * @param sText
    *                the description
    */
   public void addText(final String sText) {

      try {
         SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
               sTextBuffer.append(sText);
               jTextGRASS.setText("");
            }
         });
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

		try {
			Runnable runnable = new Runnable() {
				public void run() {
					jTitle.setText(sDescription);
				}
			};
			if (SwingUtilities.isEventDispatchThread()){
				runnable.run();
			}
			else{
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
			Runnable runnable = new Runnable() {
				public void run() {
					jProgressBar.setValue(iValue);
				}
			};
			if (SwingUtilities.isEventDispatchThread()){
				runnable.run();
			}
			else{
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
			Runnable runnable = new Runnable() {
				public void run() {
					jProgressBar.setIndeterminate(!bDeterminate);
				}
			};
			if (SwingUtilities.isEventDispatchThread()){
				runnable.run();
			}
			else{
				SwingUtilities.invokeAndWait(runnable);
			}
		}
		catch (final Exception e) {}

	}


   private class NonWordWrapPane
            extends
               JTextPane {

      public NonWordWrapPane() {

         super();

      }


      @Override
      public boolean getScrollableTracksViewportWidth() {

         return false;

      }

   }

}
