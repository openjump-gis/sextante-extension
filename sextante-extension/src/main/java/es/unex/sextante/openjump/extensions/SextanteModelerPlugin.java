package es.unex.sextante.openjump.extensions;

import javax.swing.ImageIcon;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import es.unex.sextante.gui.core.SextanteGUI;

import java.util.Objects;

public class SextanteModelerPlugin implements PlugIn {

  private final I18N i18n = I18N.getInstance("es.unex.sextante.openjump");


  public boolean execute(final PlugInContext context) {
    SextanteGUI.getGUIFactory().showModelerDialog();
    return true;
  }


  public String getName() {
    return i18n.get("es.unex.sextante.kosmo.extensions.SextanteModelerPlugin.Modeler");
  }


  public void initialize(final PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuPlugin(this,
        new String[] { "Sextante" }, getName(), false, getIcon(), null);
  }


  public ImageIcon getIcon() {
    return new ImageIcon(Objects.requireNonNull(
        getClass().getResource("model.png"),
        "Could not get resource model.png from " + getClass())
    );
  }

}
