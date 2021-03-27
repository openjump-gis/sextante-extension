package es.unex.sextante.openjump.extensions;

import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import org.openjump.core.ui.plugin.AbstractUiPlugIn;
import org.openjump.sextante.core.ObjectAndDescription;
import org.openjump.sextante.gui.additionalResults.AdditionalResults;
import org.openjump.sextante.gui.additionalResults.AdditionalResultsFrame;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;


import es.unex.sextante.openjump.language.I18NPlug;


public class SextanteResultsPlugin extends AbstractUiPlugIn {

    public String NO_RESULTS = I18NPlug
            .getI18N("es.unex.sextante.kosmo.extensions.SextanteResultsPlugin.Results.no_results");
    private final String sName = I18N
            .get("org.openjump.sextante.gui.additionalResults.AdditionalResultsPlugIn.Result-viewer");
    private static String sWarning = I18N
            .get("org.openjump.sextante.gui.additionalResults.AdditionalResultsPlugIn.List-of-results-is-empty");

    @Override
    public boolean execute(final PlugInContext context) throws Exception {
//[Giuseppe Aruta 2018-04-07] Now it opens OpenJUMP Additional results frame
        
        final ArrayList<ObjectAndDescription> m_Components = AdditionalResults.m_Components;
        if (m_Components == null || m_Components.size() == 0) {
            JOptionPane.showMessageDialog(null, sWarning, sName,
                    JOptionPane.WARNING_MESSAGE);
            return false;
        } else {

            for (final JInternalFrame iFrame : context.getWorkbenchFrame()
                    .getInternalFrames()) {
                if (iFrame instanceof AdditionalResultsFrame) {

                    iFrame.toFront();
                    return true;

                }
            }
            final AdditionalResultsFrame additionalResultsFrame = new AdditionalResultsFrame(
                    m_Components);

            context.getWorkbenchFrame()
                    .addInternalFrame(additionalResultsFrame);

        }
        return true;

        // final ArrayList<?> results = AdditionalResults.getComponents();
        // if (results.size() != 0) {
        // SextanteGUI.getGUIFactory().showAdditionalResultsDialog(results);
        // }
        //
        // else {
        // JOptionPane.showMessageDialog(null, NO_RESULTS,
        // Sextante.getText("Warning"), JOptionPane.WARNING_MESSAGE);
        // }

        // return true;

    }

    @Override
    public String getName() {// Giuseppe Aruta - PlugIn Internationalized
                             // 2013_05_25//

        return I18NPlug
                .getI18N("es.unex.sextante.kosmo.extensions.SextanteResultsPlugin.Results");

    }

    @Override
    /*
     * public ImageIcon getIcon() {
     * 
     * return new ImageIcon(SextanteGUI.class.getClassLoader().getResource(
     * "images/chart.gif"));
     * 
     * }
     */
    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("application_view.png"));

    }

    @Override
    public void initialize(final PlugInContext context) throws Exception {

        // context.getFeatureInstaller().addMainMenuPlugin(this,
        // new String[] { MenuNames.WINDOW }, sName, false,
        // getColorIcon(), getEnableCheck()

        context.getFeatureInstaller().addMainMenuPlugin(this,
                new String[] { "Sextante" }, getName(), false, getIcon(), null);

    }

}
