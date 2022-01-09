package es.unex.sextante.openjump.extensions;

import javax.swing.ImageIcon;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import es.unex.sextante.gui.core.SextanteGUI;

import java.util.Objects;

public class SextanteDataExplorerPlugin extends AbstractPlugIn {

  private final I18N i18n = I18N.getInstance("es.unex.sextante.openjump");


  public boolean execute(final PlugInContext context) {
    SextanteGUI.getGUIFactory().showDataExplorer();
    return true;
  }


  public ImageIcon getIcon() {
    return new ImageIcon(Objects.requireNonNull(
        getClass().getResource("documenter.png"),
        "Could not get resource documenter.png from " + getClass())
    );
  }


  public String getName() {
    return i18n.get("es.unex.sextante.kosmo.extensions.SextanteDataExplorerPlugin.dataexplorer");
  }


  public void initialize(final PlugInContext context) throws Exception {
      super.initialize(context);
    
      context.getFeatureInstaller().addMainMenuPlugin(this,
          new String[] { "Sextante" }, getName(), false, getIcon(), null);
  }

  @Override
  public EnableCheck getEnableCheck() {
    EnableCheckFactory checkFactory = EnableCheckFactory.getInstance(getContext().getWorkbenchContext());

    return checkFactory.createTaskWindowMustBeActiveCheck();
  }
}
