package es.unex.sextante.openjump.extensions;

import javax.swing.ImageIcon;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.task.TaskMonitor;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.plugin.ThreadedPlugIn;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;

import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.gui.help.SextanteHelpWindow;

import java.util.Objects;


public class SextanteHelpPlugIn implements ThreadedPlugIn {

  private final I18N i18n = I18N.getInstance("es.unex.sextante.openjump");


  public static final ImageIcon ICON = IconLoader.icon("information_16x16.png");


  @Override
  public void run(TaskMonitor monitor, PlugInContext context) { }


  @Override
  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuPlugin(this,
        new String[] { "Sextante" }, getName(), false, ICON, null);
  }

  @Override
  public String getName() {
    return i18n.get("es.unex.sextante.kosmo.extensions.SextanteHelpPlugin.help");
  }


  public void execute(String actionCommand) {
    if (actionCommand.compareTo("ACTION_PROCESSING_SEXTANTE_HELP") == 0) {
      SextanteGUI.getGUIFactory().showHelpWindow();
    }
  }
 

  @Override
  public boolean execute(PlugInContext context) {
    // [Giuseppe Aruta 2018-14-08] Recativated old way to open Help window. see comment below
    final SextanteHelpWindow window = new SextanteHelpWindow();

    window.pack();
    window.setAlwaysOnTop(true);
    window.setVisible(true);
        
    // [Giuseppe Aruta 2017-12-12] open as OJ internal frame
    // [Giuseppe Aruta 2018-14-08] Removed. This should be defined at
    // Sextante-GUI class level
    // final SextanteHelpFrame window = new SextanteHelpFrame();
    // JFrame frame = context.getWorkbenchFrame();
    // // for (JInternalFrame iFrame : wFrame.getInternalFrames()) {
    // if (iFrame instanceof ToolboxFrame) {
    //
    // iFrame.toFront();
    // return false;
    //
    // }
    // }

    // wFrame.addInternalFrame(window, true, true);

    // return true;
    return true;
  }


  public boolean isEnabled() {
        return true;
    }


  public boolean isVisible() {
        return true;
    }


  public ImageIcon getIcon() {
    return new ImageIcon(Objects.requireNonNull(
        getClass().getResource("info.gif"),
        "Could not get resource info.gif from " + getClass())
    );
  }

}
