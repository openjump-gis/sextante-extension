package es.unex.sextante.openjump.extensions;

import javax.swing.ImageIcon;

import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.cursortool.QuasimodeTool;

import es.unex.sextante.openjump.language.I18NPlug;

/**
 * @description: Following Victor Oyala Blog
 *               (http://sextantegis.blogspot.it/2009
 *               /05/herramientas-para-usuarios-gvsig.html) this functionality
 *               allows to interactive get coordinates of points from a view.
 *               Thos points can be used later on Sextante Algorithms,
 * 
 * @author Giuseppe Aruta oct 2016
 *
 **/

public class SextantePickCoordinatesPlugIn extends AbstractPlugIn {

    public static final String NAME = I18NPlug
            .getI18N("es.unex.sextante.kosmo.extensions.SextantePickCooridnates.pick-coordinates");

    @Override
    public boolean execute(PlugInContext context) throws Exception {
        reportNothingToUndoYet(context);

        context.getLayerViewPanel().setCurrentCursorTool(
                QuasimodeTool
                        .createWithDefaults(new SextantePickCoordinatesTool(
                                context)));

        return true;
    }

    public ImageIcon getIcon() {
        return new ImageIcon(getClass().getResource("bullseye.png"));
    }

    @Override
    public String getName() {
        return NAME;
    }

    public void initialize(PlugInContext context) throws Exception {
        context.getFeatureInstaller().addMainMenuPlugin(this,
                new String[] { "Sextante" }, getName(), false, getIcon(), null);
    }

}
