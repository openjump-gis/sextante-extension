package es.unex.sextante.openjump.extensions;

import javax.swing.ImageIcon;

import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.history.History;
import es.unex.sextante.openjump.language.I18NPlug;

public class SextanteHistoryPlugin implements PlugIn {

    public boolean execute(final PlugInContext context) throws Exception {

        SextanteGUI.getGUIFactory().showHistoryDialog();

        return true;

    }

    public String getName() {// Giuseppe Aruta - PlugIn Internationalized
                             // 2013_05_25//

        return I18NPlug
                .getI18N("es.unex.sextante.kosmo.extensions.SextanteHistoryPlugin.History");

    }

    public void initialize(final PlugInContext context) throws Exception {

        History.startSession();

        context.getFeatureInstaller().addMainMenuPlugin(this,
                new String[] { "Sextante" }, getName(), false, getIcon(), null);

    }

    /*
     * public ImageIcon getIcon() { return new
     * ImageIcon(getClass().getResource("book.png")); }
     */

    public ImageIcon getIcon() {

        return new ImageIcon(SextanteGUI.class.getClassLoader().getResource(
                "images/history.gif"));

    }

}
