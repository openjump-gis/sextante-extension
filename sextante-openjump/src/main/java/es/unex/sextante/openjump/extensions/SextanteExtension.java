package es.unex.sextante.openjump.extensions;

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.I18N;

public class SextanteExtension extends Extension {

  public static final I18N I18N = com.vividsolutions.jump.I18N.getInstance("es.unex.sextante.openjump");

  /*
   * Version History
   *
   * @ Sextante 1.0 2013-04-01 Sextante 1.0 Internationalized 2013-05-25 (Add
   * Language codes from Kosmo Sextante (ca,es,fi,hr,it), add French)
   *
   * @ Sextante 1.0.1 2016-08-10 Correct bug #480. Added Sextante Data
   * Explorer
   *
   * @ Sextante 1.0.2 2016-10-31 Added Pick coordinates plugin. Better
   * integration between OpenJUMP and Sextante data model Upgraded
   * documentation with a new help dialog style Fix bugs #410 and #427
   *
   * @ Sextante 1.0.2b 2016-10-31 Help dialog now shows all documentation
   * related to algorithm. Convert Help Dialog to Detached windows
   *
   * @ Sextante 1.0 OpenJUMP binding 2016-11-28 Help framework now works also
   * on other plugins and algorithms. Solved bug related on Advanced option
   * panel. Added new icons to Results, Pick coordinates and Layer explorer.
   * Changed number version as Sextante embedded into GvSIGCE is 1.0.0 and it
   * is newer than OpenJUMP one (2009?)
   */

  private static final String NAME = "Sextante 2.1.0";
  private static final String VERSION = "OpenJUMP binding 2021-08-15";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getVersion() {
    return VERSION;
  }

  @Override
  public void configure(PlugInContext context) throws Exception {

    new SextanteToolboxPlugin().initialize(context);
    new SextanteModelerPlugin().initialize(context);
    new SextanteHistoryPlugin().initialize(context);
    new SextanteCommandLinePlugin().initialize(context);
    // new AdditionalResultsPlugIn().initialize(context);
    new SextanteResultsPlugin().initialize(context);
    new SextanteDataExplorerPlugin().initialize(context);
    new SextantePickCoordinatesPlugIn().initialize(context);

    context.getFeatureInstaller().addMenuSeparator(new String[]{"Sextante"});

    new SextanteSettingsPlugin().initialize(context);

    context.getFeatureInstaller().addMenuSeparator(new String[]{"Sextante"});

    new SextanteHelpPlugIn().initialize(context);

  }

}
