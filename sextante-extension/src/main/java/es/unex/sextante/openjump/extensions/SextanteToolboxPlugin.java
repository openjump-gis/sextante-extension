package es.unex.sextante.openjump.extensions;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.ImageIcon;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.JUMPWorkbench;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.plugin.PlugIn;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

import es.unex.sextante.core.Sextante;
import es.unex.sextante.gui.core.IAlgorithmProvider;
import es.unex.sextante.gui.core.SextanteGUI;
import es.unex.sextante.openjump.core.OpenJUMPOutputFactory;
import es.unex.sextante.openjump.gui.OpenJUMPGUIFactory;
import es.unex.sextante.openjump.gui.OpenJUMPInputFactory;
import es.unex.sextante.openjump.gui.OpenJUMPPostProcessTaskFactory;

public class SextanteToolboxPlugin implements PlugIn {

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


  public void initialize_old(final PlugInContext context) {
    context.getFeatureInstaller().addMainMenuPlugin(this,
        new String[]{"Sextante"}, getName(), false, getIcon(), null);
  }


  @Override
  public void initialize(PlugInContext context) throws Exception {
    final String sextantePath = getJarsFolder();
    Sextante.initialize(sextantePath);
    SextanteGUI.setSextantePath(sextantePath);

    final List<IAlgorithmProvider> algorithmProviders = getAlgorithmProvidersFromFolder(sextantePath);
    for (final IAlgorithmProvider provider : algorithmProviders) {
      if (!containsProvider(provider)) {
        SextanteGUI.addAlgorithmProvider(provider);
      }
    }
    SextanteGUI.initialize(sextantePath);
    SextanteGUI.setMainFrame(context.getWorkbenchFrame());
    SextanteGUI.setOutputFactory(new OpenJUMPOutputFactory(context
        .getWorkbenchContext()));
    SextanteGUI.setGUIFactory(new OpenJUMPGUIFactory());
    SextanteGUI.setInputFactory(new OpenJUMPInputFactory(context
        .getWorkbenchContext()));
    SextanteGUI
        .setPostProcessTaskFactory(new OpenJUMPPostProcessTaskFactory());
    Logger.info("Sextante help file in folder: "
        + getJarsFolder().concat(File.separator).concat("help"));

    context.getFeatureInstaller().addMainMenuPlugin(this,
        new String[]{"Sextante"}, getName(), false, getIcon(), null);

  }


  private String getJarsFolder() {
    final String path = JUMPWorkbench.getInstance().getPlugInManager()
        .getPlugInDirectory().getAbsolutePath();
    final String sPath = path.concat(File.separator).concat("sextante");
    //   LOGGER.info("Sextante jar folder: " + sPath);
    return sPath;
  }


  // [Giuseppe Aruta 2018-04-08] Activated connection to external providers (Grass, Saga, R...)
  private boolean containsProvider(IAlgorithmProvider provider) {
    final List<IAlgorithmProvider> algorithmProviders = SextanteGUI
        .getAlgorithmProviders();
    for (final IAlgorithmProvider iAlgorithmProvider : algorithmProviders) {
      if (iAlgorithmProvider.getClass().isAssignableFrom(
          provider.getClass())) {
        return true;
      }
    }
    return false;
  }


  public ImageIcon getIcon() {
    return new ImageIcon(Objects.requireNonNull(
        getClass().getResource("module2.png"),
        "Could not get resource module2.png from " + getClass())
    );
  }


  private List<IAlgorithmProvider> getAlgorithmProvidersFromFolder(
      String sFolder) {
    final Set<String> algorithmProviderNames = new TreeSet<String>();
    final List<IAlgorithmProvider> providers = new ArrayList<IAlgorithmProvider>();

    final File folder = new File(sFolder);
    final File[] directoryFiles = folder.listFiles();
    for (int i = 0; i < directoryFiles.length; i++) {
      if (!directoryFiles[i].isDirectory()) {
        final String sFilename = directoryFiles[i].getName();
        if (sFilename.endsWith(".jar")) {
          algorithmProviderNames
              .addAll(addAlgorithmProvidersFromFolder(directoryFiles[i]
                  .getAbsolutePath()));
        }
      }
    }
    for (final String algProviderName : algorithmProviderNames) {
      try {
        final Class<?> clazz = Class.forName(algProviderName);
        if (!clazz.isInterface()) {
          final Object obj = clazz.newInstance();
          if ((obj instanceof IAlgorithmProvider)) {
            providers.add((IAlgorithmProvider) obj);
          }
        }
      } catch (final Exception ex) {
        Logger.error("Error on loading Sextante algorithm provider: ", ex);
      }
    }
    return providers;
  }


  private List<String> addAlgorithmProvidersFromFolder(String sFilename) {
    final ArrayList<String> algorithmProviders = new ArrayList<String>();
    ZipFile zip = null;
    try {
      zip = new ZipFile(sFilename);
      final Enumeration<? extends ZipEntry> entries = zip.entries();
      while (entries.hasMoreElements()) {
        final ZipEntry entry = entries.nextElement();
        final String sName = entry.getName();
        if ((!entry.isDirectory())
            && (sName.toLowerCase()
            .endsWith("algorithmprovider.class"))) {
          final String sClassName = sName.substring(0,
              sName.lastIndexOf('.')).replace('/', '.');

          algorithmProviders.add(sClassName);
        }
      }
      return algorithmProviders;
    } catch (final Exception e) {
      Logger.error("Error on adding Sextante algorithm provider: ", e);
    } finally {
      if (zip != null) {
        try {
          zip.close();
        } catch (final IOException ignored) {
        }
      }
    }
    return algorithmProviders;
  }

}
