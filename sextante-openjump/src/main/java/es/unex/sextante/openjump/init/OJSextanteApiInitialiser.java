package es.unex.sextante.openjump.init;

import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import es.unex.sextante.core.OutputFactory;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.openjump.core.OpenJUMPOutputFactory;

/**
 * Use this class to initialize Sextante from within an OpenJUMP plugin, and to
 * check if Sextante is already initialized. I.e. first test if
 * OJSextanteApiInitialiser.isInitialized is false and if so, then call
 * OJSextanteApiInitialiser.initializeSextante()
 * 
 * @author sstein
 *
 */
public class OJSextanteApiInitialiser {

    public static boolean isInitialized = false;

    public static void initializeSextante(PlugInContext context) {
        System.out.println("initializing Sextante version: "
                + Sextante.getVersionNumber());
        OutputFactory outputFactory = new OpenJUMPOutputFactory(
                context.getWorkbenchContext());
        Sextante.initialize();
        SextanteGUI.initialize();
        SextanteGUI.setOutputFactory(outputFactory);

        OJSextanteApiInitialiser.isInitialized = true;
    }

}
