package es.unex.sextante.openjump.extensions;

import java.util.ArrayList;
import java.util.Objects;

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


public class SextanteResultsPlugin extends AbstractUiPlugIn {

  private final I18N i18n = I18N.getInstance("es.unex.sextante.openjump");

  public String NO_RESULTS = i18n
      .get("es.unex.sextante.kosmo.extensions.SextanteResultsPlugin.Results.no_results");
  private final String sName = I18N.JUMP
      .get("org.openjump.sextante.gui.additionalResults.AdditionalResultsPlugIn.Result-viewer");
  private final String sWarning = I18N.JUMP
      .get("org.openjump.sextante.gui.additionalResults.AdditionalResultsPlugIn.List-of-results-is-empty");


  @Override
  public boolean execute(final PlugInContext context) {
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
  public String getName() {
    return i18n.get("es.unex.sextante.kosmo.extensions.SextanteResultsPlugin.Results");
  }


  @Override
  public Icon getIcon() {
    return new ImageIcon(Objects.requireNonNull(
        getClass().getResource("application_view.png"),
        "Could not get resource application_view.png from " + getClass())
    );

  }


  @Override
  public void initialize(final PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuPlugin(this,
        new String[]{"Sextante"}, getName(), false, getIcon(), null);
  }

}
