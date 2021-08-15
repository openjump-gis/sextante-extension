package es.unex.sextante.openjump.extensions;

import javax.swing.ImageIcon;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;

import java.util.Objects;


/**
 * Following Victor Oyala Blog
 * (http://sextantegis.blogspot.it/2009/05/herramientas-para-usuarios-gvsig.html)
 * this functionality allows to interactive get coordinates of points from a view.
 * Those points can be used later on Sextante Algorithms,
 * 
 * @author Giuseppe Aruta oct 2016
 *
 */
public class SextantePickCoordinatesPlugIn extends AbstractPlugIn {

  private static final I18N i18n = I18N.getInstance("es.unex.sextante.openjump");

  public static final String NAME = i18n
      .get("es.unex.sextante.kosmo.extensions.SextantePickCoordinates.pick-coordinates");


  @Override
  public boolean execute(PlugInContext context) {
    reportNothingToUndoYet(context);
    context.getLayerViewPanel().setCurrentCursorTool(
        QuasimodeTool.createWithDefaults(
            new SextantePickCoordinatesTool(context)
        )
    );
    return true;
  }


  @Override
  public ImageIcon getIcon() {
    return new ImageIcon(Objects.requireNonNull(
        getClass().getResource("bullseye.png"),
        "Could not get resource bullseye.png from " + getClass())
    );
  }


  @Override
  public String getName() {
    return NAME;
  }


  @Override
  public void initialize(PlugInContext context) throws Exception {
    context.getFeatureInstaller().addMainMenuPlugin(this,
        new String[] { "Sextante" }, getName(), false, getIcon(), null);
  }

}
