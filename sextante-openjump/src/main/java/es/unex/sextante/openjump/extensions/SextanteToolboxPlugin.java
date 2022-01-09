package es.unex.sextante.openjump.extensions;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.plugin.AbstractPlugIn;
import com.vividsolutions.jump.workbench.plugin.EnableCheck;
import com.vividsolutions.jump.workbench.plugin.EnableCheckFactory;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import es.unex.sextante.core.AlgorithmsAndResources;
import es.unex.sextante.core.GeoAlgorithm;
import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.GUIResources;
import es.unex.sextante.gui.core.IAlgorithmProvider;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.openjump.Finder;
import es.unex.sextante.openjump.core.OpenJUMPOutputFactory;
import es.unex.sextante.openjump.gui.OpenJUMPGUIFactory;
import es.unex.sextante.openjump.gui.OpenJUMPInputFactory;
import es.unex.sextante.openjump.gui.OpenJUMPPostProcessTaskFactory;

public class SextanteToolboxPlugin extends AbstractPlugIn {

  private final I18N i18n = I18N.getInstance("es.unex.sextante.openjump");


  @Override
  public boolean execute(final PlugInContext context) {
    // [Giuseppe Aruta 2018-04-08] open as OJ internal frame
    // Deactivated to further tests as algorithms are not correctly selected
    // in the JInternalFrame

    // JFrame frame = context.getWorkbenchFrame();
    // for (JInternalFrame iFrame : wFrame.getInternalFrames()) {
    // if (iFrame instanceof ToolboxFrame) {
    //
    // iFrame.toFront();
    // return false;
    //
    // }
    // }
    // ToolboxFrame tframe = new ToolboxFrame(frame);
    // wFrame.addInternalFrame(tframe, true, true);
    // SextanteGUI.getInputFactory().clearDataObjects();

    SextanteGUI.getGUIFactory().showToolBoxDialog();

    return true;
  }


  @Override
  public String getName() {
    return i18n.get("es.unex.sextante.kosmo.extensions.SextanteToolboxPlugin.Sextante-toolbox");
  }

  @Override
  public void initialize(PlugInContext context) throws Exception {
    super.initialize(context);

    File folder = context.getWorkbenchContext().getWorkbench().getPlugInManager().findFileOrFolderInExtensionDirs("sextante");
    if ( folder == null || !folder.exists() || !folder.isDirectory() ) {
      throw new IllegalArgumentException("Invalid sextante folder '"+folder+"'! Initialize failed.");
    }

    String sextantePath = folder.getAbsolutePath();

    // find algos/i18n files
    List<Class<GeoAlgorithm>> algorithmClasses = Finder.findClassesImplementingOrExtending(GeoAlgorithm.class,null);
    List<String> paths = Finder.findResourcesMatching(Pattern.compile(".*i18n/[^_/]+\\.properties$", Pattern.CASE_INSENSITIVE));
    
    List<String> algorithmClassNames = algorithmClasses.stream().map(o -> o.getName()).collect(Collectors.toList());
    AlgorithmsAndResources.addAlgorithmClassNames(algorithmClassNames.toArray(new String[0]));
    paths = paths.stream().map(p -> p.substring(0, p.lastIndexOf('.'))).collect(Collectors.toList());
    AlgorithmsAndResources.addPropertiesFilenames(paths.toArray(new String[0]));
    Sextante.initialize();

    // find and add algorithmProviderClasses
    final List<Class<IAlgorithmProvider>> algorithmProviderClasses = Finder.findClassesImplementingOrExtending(IAlgorithmProvider.class,null);
    for (final Class<IAlgorithmProvider> algoProviderClass : algorithmProviderClasses) {
      SextanteGUI.addAlgorithmProvider(algoProviderClass.newInstance());
    }
    // find and add ParametersPanels
    List<String> panels = Finder.findResourcesMatching(Pattern.compile("(?i).*[^/]+parameterspanel.class$"));
    for (String sName : panels) {
      if (sName.toLowerCase().endsWith("parameterspanel.class")) {
         final String sClassName = sName.substring(0, sName.lastIndexOf('.')).replace('/', '.');
         GUIResources.addParameterPanelClassNames(sClassName);
      }
      if (sName.toLowerCase().endsWith("modelerparameterspanel.class")) {
         final String sClassName = sName.substring(0, sName.lastIndexOf('.')).replace('/', '.');
         GUIResources.addModelerParameterPanelClassNames(sClassName);
      }
    }

    SextanteGUI.setSextantePath(sextantePath);
    SextanteGUI.initialize();
    SextanteGUI.setMainFrame(context.getWorkbenchFrame());
    SextanteGUI.setOutputFactory(new OpenJUMPOutputFactory(context
        .getWorkbenchContext()));
    SextanteGUI.setGUIFactory(new OpenJUMPGUIFactory());
    SextanteGUI.setInputFactory(new OpenJUMPInputFactory(context
        .getWorkbenchContext()));
    SextanteGUI
        .setPostProcessTaskFactory(new OpenJUMPPostProcessTaskFactory());
    Logger.info("Sextante help files in folder: "
        + SextanteGUI.getHelpPath());

    context.getFeatureInstaller().addMainMenuPlugin(this,
        new String[]{"Sextante"}, getName(), false, getIcon(), null);

  }

  public ImageIcon getIcon() {
    return new ImageIcon(Objects.requireNonNull(
        getClass().getResource("module2.png"),
        "Could not get resource module2.png from " + getClass())
    );
  }

  @Override
  public EnableCheck getEnableCheck() {
    EnableCheckFactory checkFactory = EnableCheckFactory.getInstance(getContext().getWorkbenchContext());

    return checkFactory.createTaskWindowMustBeActiveCheck();
  }


//  private List<IAlgorithmProvider> getAlgorithmProvidersFromFolderOld(
//      File folder) {
//    final Set<String> algorithmProviderNames = new TreeSet<String>();
//    final List<IAlgorithmProvider> providers = new ArrayList<IAlgorithmProvider>();
//
//    final File[] directoryFiles = folder.listFiles();
//    for (int i = 0; i < directoryFiles.length; i++) {
//      if (directoryFiles[i].isDirectory()) {
//        
//      } else {
//        final String sFilename = directoryFiles[i].getName();
//        if (sFilename.toLowerCase().endsWith(".jar")) {
//          algorithmProviderNames
//              .addAll(addAlgorithmProvidersFromZipfile(directoryFiles[i]
//                  .getAbsolutePath()));
//        }
//      }
//    }
//    for (final String algProviderName : algorithmProviderNames) {
//      try {
//        final Class<?> clazz = Class.forName(algProviderName);
//        if (!clazz.isInterface()) {
//          final Object obj = clazz.newInstance();
//          if ((obj instanceof IAlgorithmProvider)) {
//            providers.add((IAlgorithmProvider) obj);
//          }
//        }
//      } catch (final Exception ex) {
//        Logger.error("Error on loading Sextante algorithm provider: ", ex);
//      }
//    }
//    return providers;
//  }

//  // TODO: remove
//  private List<String> addAlgorithmProvidersFromZipfile(String sFilename) {
//    final ArrayList<String> algorithmProviders = new ArrayList<String>();
//    ZipFile zip = null;
//    try {
//      zip = new ZipFile(sFilename);
//      final Enumeration<? extends ZipEntry> entries = zip.entries();
//      while (entries.hasMoreElements()) {
//        final ZipEntry entry = entries.nextElement();
//        final String sName = entry.getName();
//        if ((!entry.isDirectory())
//            && (sName.toLowerCase()
//            .endsWith("algorithmprovider.class"))) {
//          final String sClassName = sName.substring(0,
//              sName.lastIndexOf('.')).replace('/', '.');
//
//          algorithmProviders.add(sClassName);
//        }
//      }
//      return algorithmProviders;
//    } catch (final Exception e) {
//      Logger.error("Error on adding Sextante algorithm provider: ", e);
//    } finally {
//      if (zip != null) {
//        try {
//          zip.close();
//        } catch (final IOException ignored) {
//        }
//      }
//    }
//    return algorithmProviders;
//  }
}
