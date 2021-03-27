package es.unex.sextante.openjump.extensions;

import javax.swing.ImageIcon;

import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;

public class SextanteSettingsPlugin implements PlugIn {

    public void initialize() {
    }

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
    public boolean execute(PlugInContext arg0) throws Exception {
        SextanteGUI.getGUIFactory().showSettingsDialog(null, null);
        return true;
    }

    public String getName() {// Giuseppe Aruta - PlugIn Internationalized
        // 2013_05_25//

        return Sextante.getText("Settings");// "Sextante configuration";

    }

    public void initialize(final PlugInContext context) throws Exception {

        context.getFeatureInstaller().addMainMenuPlugin(this,
                new String[] { "Sextante" }, getName(), false, getIcon(), null);

    }

    public ImageIcon getIcon() {

        return new ImageIcon(SextanteGUI.class.getClassLoader().getResource(
                "images/config.png"));

    }

}
