package es.unex.sextante.openjump.extensions;

import javax.swing.ImageIcon;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.history.History;

import java.util.Objects;

public class SextanteHistoryPlugin implements PlugIn {

  private final I18N i18n = I18N.getInstance("es.unex.sextante.openjump");


  public boolean execute(final PlugInContext context) {
    SextanteGUI.getGUIFactory().showHistoryDialog();
    return true;
  }


  public String getName() {
    return i18n.get("es.unex.sextante.kosmo.extensions.SextanteHistoryPlugin.History");
  }


  public void initialize(final PlugInContext context) throws Exception {
    History.startSession();
    context.getFeatureInstaller().addMainMenuPlugin(this,
        new String[] { "Sextante" }, getName(), false, getIcon(), null);
  }


  public ImageIcon getIcon() {
    return new ImageIcon(Objects.requireNonNull(
        getClass().getResource("history.gif"),
        "Could not get resource history.gif from " + getClass())
    );
  }

}
