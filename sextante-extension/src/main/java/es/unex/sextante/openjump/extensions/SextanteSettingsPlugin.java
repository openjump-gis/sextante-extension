package es.unex.sextante.openjump.extensions;

import javax.swing.ImageIcon;

import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;

import java.util.Objects;

public class SextanteSettingsPlugin implements PlugIn {


  public void initialize() { }


  public void execute(final String actionCommand) {
    if (actionCommand.compareTo("ACTION_PROCESSING_SEXTANTE_SETTINGS") == 0) {
      SextanteGUI.getGUIFactory().showSettingsDialog(null, null);
    }
  }


  public boolean isEnabled() {
    return true;
  }


  public boolean isVisible() {
    return true;
  }


  @Override
  public boolean execute(PlugInContext arg0) {
    SextanteGUI.getGUIFactory().showSettingsDialog(null, null);
    return true;
  }


  @Override
  public String getName() {
    return Sextante.getText("Settings");
  }


  @Override
  public void initialize(final PlugInContext context) throws Exception {

    context.getFeatureInstaller().addMainMenuPlugin(this,
        new String[]{"Sextante"}, getName(), false, getIcon(), null);

  }

  public ImageIcon getIcon() {
    return new ImageIcon(Objects.requireNonNull(
        getClass().getResource("config.png"),
        "Could not get resource config.png from " + getClass())
    );
  }

}
